package com.xuecheng.content.feignclient;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.content.po.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("添加课程索引发生熔断，索引信息：{}，异常信息:{}", courseIndex, throwable.toString(), throwable);
                return false;
            }
        };
    }
}
