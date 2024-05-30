package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.config.MinioConfig;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jdk.nashorn.internal.runtime.SharedPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// 视频处理任务类
@Component
public class VideoTaskJob {
    private static Logger logger = LoggerFactory.getLogger(VideoTaskJob.class);
    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 2、分片广播任务
     */
    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        // 确定cpu核心数
        int processors = Runtime.getRuntime().availableProcessors();
        // 查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        // 创建一个线程池
        int size = mediaProcessList.size();
        logger.debug("取到的视频处理任务数："+size);
        if(size<=0){
            return;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        // 使用的计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            // 文件id就是md5
            String fileId = mediaProcess.getFileId();
            // 任务加入线程池
            executorService.execute(()->{
                try {
                    // 任务id
                    Long taskId = mediaProcess.getId();
                    // 开启任务
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        logger.debug("抢占任务失败，任务id:{}", taskId);
                        return;
                    }
                    // 下载minio视频到本地
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        logger.debug("下载视频出错，任务id:{}, bucket:{}, objectName:{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    // 源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    // 转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    // 转换后mp4文件路径，先创建一个临时文件在本地
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        logger.debug("创建临时文件异常，{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    // 执行视频转码
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, video_path, mp4_name, mp4_path);
                    // 开始视频转换，成功返回success
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        logger.debug("视频转码失败，原因：{}, bucket:{}, objectName:{}", result, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }
                    // 上传到minio
                    // 这个时候要修改objectName后缀是.mp4
                    objectName = objectName.substring(0, objectName.lastIndexOf("."));
                    objectName += ".mp4";
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                    if (!b1) {
                        logger.debug("上传mp4到minio失败，taskId:{}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传mp4到minio失败");
                        return;
                    }
                    // mp4文件的url
                    String url = getFilePath(fileId, ".mp4");
                    // 任务状态成功，保存处理结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "");
                } finally {
                    // 计数器减去1
                    countDownLatch.countDown();
                }
            });
        });
        // 阻塞，指定最大限度等待时间，阻塞最多等待一定时间后就解除阻塞
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5,String fileExt){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
    
}
