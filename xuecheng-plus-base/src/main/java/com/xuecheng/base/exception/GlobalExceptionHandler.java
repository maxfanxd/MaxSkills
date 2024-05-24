package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 对项目自定义异常进行处理
    @ExceptionHandler(value = XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e) {
        // 打印异常日志
        log.error("系统异常{}", e.getErrMessage(), e);

        // 解析出异常信息
        String errMsg = e.getMessage();
        RestErrorResponse restErrorResponse = new RestErrorResponse(errMsg);
        return restErrorResponse;
    }

    // 非自定义异常
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {
        // 打印异常日志
        log.error("系统异常{}", e.getMessage(), e);

        // 解析出异常信息
        RestErrorResponse restErrorResponse = new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
        return restErrorResponse;
    }
}
