package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        // 发生熔断，上游服务会调用此方法执行降级逻辑
        // 可以拿到熔断的异常信息
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String objectName) throws IOException {
                log.debug("远程调用上传接口熔断：{}", throwable.toString(), throwable);
                return null;
            }
        };
    }
}
