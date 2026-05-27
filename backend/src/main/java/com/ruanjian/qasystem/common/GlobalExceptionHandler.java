package com.ruanjian.qasystem.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
/**
 * 全局异常处理。
 * 保证接口异常时仍返回统一格式，方便前端处理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("请求参数不合法");

        return ApiResponse.fail(400,message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("请求参数不合法");

        return ApiResponse.fail(400,message);
    }
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.fail(400, e.getMessage());
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("请求参数不合法");

        return ApiResponse.fail(400, message);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        return ApiResponse.fail(404, "接口不存在");
    }
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        return ApiResponse.fail(500, e.getClass().getName());
    }


}
