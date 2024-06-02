package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.po.CourseIndex;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    //执行课程发布任务的逻辑，抛出异常说明任务执行失败

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    CoursePublishMapper coursePublishMapper;


    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        // 课程静态化上传到minio
        generateCourseHtml(mqMessage, courseId);
        // 向elasticsearch写索引数据
        saveCourseIndex(mqMessage, courseId);
        // 向redis写缓存

        // 返回true表示完成
        return true;
    }

    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        // 任务幂等性处理
        // 查询数据库取出该阶段执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne > 0){
            log.debug("课程静态化任务完成，无需处理");
            return ;
        }
        // 开始进行课程静态化
        // 生成html页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file == null){
            XueChengPlusException.cast("生成的静态页面为空");
        }

        // 将html上传到minio
        coursePublishService.uploadCourseHtml(courseId, file);

        mqMessageService.completedStageOne(taskId);

    }

    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        MqMessageService mqMessageService = this.getMqMessageService();
        // 任务幂等性判断
        Long taskId = mqMessage.getId();
        // 第二个阶段
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if(stageTwo > 0){
            log.debug("课程索引信息已经写入，无需处理");
            return ;
        }
        // 查询课程信息，调用搜索服务添加索引
        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(courseId);
        // 远程调用
        CourseIndex courseIndex = new CourseIndex();
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        BeanUtils.copyProperties(coursePublish, courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            log.error("索引添加失败，courseId:{}", courseId);
            XueChengPlusException.cast("远程调用搜索服务添加课程索引失败");
        }
        // 完成本阶段任务
        mqMessageService.completedStageTwo(taskId);

    }


}