package com.example.handler;

import com.example.result.ResultBean;
import jakarta.validation.ValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 处理validate校验的错误
 */
@RestControllerAdvice
public class ValidationHandler {
    @ExceptionHandler(ValidationException.class)
    public ResultBean validateHandler(ValidationException exception){
        return ResultBean.failure(400,"请求参数有误!");
    }
}
