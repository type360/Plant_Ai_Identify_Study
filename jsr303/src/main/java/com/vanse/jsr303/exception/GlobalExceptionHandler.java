package com.vanse.jsr303.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Auther: vanse(lc)
 * @Date: 2025/10/30-10-30-下午2:16
 * @Description：aop
 */
@RestControllerAdvice // 通知
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String exception(MethodArgumentNotValidException e, BindingResult result) {
        if (result.hasErrors()) {
            return result.getAllErrors().get(0).getDefaultMessage();
        }
        return "";
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleException(ConstraintViolationException e) {
        // removeStudentByIds.arg0: 学生id列表不能为空
        // 获取异常信息，格式如下：方法名.参数名：异常信息，……
        String message = e.getMessage();

        return  message.split(" ")[1];
    }
}
