package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    //执行课程发布任务的逻辑，抛出异常说明任务执行失败

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
        SaveCourseIndex(mqMessage, courseId);
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
        int i = 1/0;    // 制造一个异常
        // 任务处理完成后设置任务状态为完成
        mqMessageService.completedStageOne(taskId);

    }

    private void SaveCourseIndex(MqMessage mqMessage, long courseId) {
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


        // 完成本阶段任务
        mqMessageService.completedStageTwo(taskId);

    }


}