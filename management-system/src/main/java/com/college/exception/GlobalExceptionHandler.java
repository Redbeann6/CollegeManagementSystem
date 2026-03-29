package com.college.exception;

import com.college.dto.Response;
import com.college.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 捕获系统中所有未处理的异常，并返回统一格式的响应
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<Response<?>> handleBusinessException(BusinessException ex) {
        logger.error("业务异常: {}", ex.getMessage(), ex);
        Response<?> response = Response.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Response<?>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.error("参数验证异常: {}", errors);
        Response<?> response = Response.error("参数验证失败", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response<?> handleAccessDeniedException(AccessDeniedException ex) {
        logger.error("访问拒绝异常: {}", ex.getMessage());
        return Response.error("您没有权限执行此操作");
    }
    
    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<?> handleUnauthorizedException(UnauthorizedException ex) {
        logger.error("未授权异常: {}", ex.getMessage());
        return Response.error(ex.getMessage());
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("资源未找到异常: {}", ex.getMessage());
        return Response.error(ex.getMessage());
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<?> handleGenericException(Exception ex, WebRequest request) {
        logger.error("发生未预期的异常: {}", ex.getMessage(), ex);
        // 生产环境下不返回具体异常信息给客户端
        String message = "系统内部错误，请稍后重试";
        // 开发环境可以返回更详细的错误信息
        // String message = ex.getMessage();
        return Response.error(message);
    }
}
