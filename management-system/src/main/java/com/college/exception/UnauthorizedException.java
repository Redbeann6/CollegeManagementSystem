package com.college.exception;

/**
 * 未授权异常类
 * 用于表示用户未授权的情况，如登录失败、访问受限等
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
