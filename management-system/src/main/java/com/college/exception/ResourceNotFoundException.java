package com.college.exception;

/**
 * 资源未找到异常类
 * 用于表示请求的资源不存在的情况
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 创建指定资源类型和ID的资源未找到异常
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s不存在，ID: %s", resourceType, resourceId));
    }
    
    /**
     * 创建指定资源类型、字段名和字段值的资源未找到异常
     */
    public ResourceNotFoundException(String resourceType, String fieldName, String fieldValue) {
        super(String.format("%s不存在，%s: %s", resourceType, fieldName, fieldValue));
    }
}
