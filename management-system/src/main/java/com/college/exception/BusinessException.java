package com.college.exception;

/**
 * 业务异常类
 * 用于表示业务逻辑层面的异常
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
