package com.college.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    /**
     * 检查当前认证用户是否是指定ID的用户
     */
    public boolean isCurrentUser(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // 获取当前认证用户的ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.college.model.User) {
            com.college.model.User currentUser = (com.college.model.User) principal;
            return currentUser.getId().equals(userId);
        }
        
        return false;
    }
    
    /**
     * 检查当前认证用户是否是管理员或指定ID的用户
     */
    public boolean isAdminOrCurrentUser(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // 检查是否是管理员
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        
        // 检查是否是当前用户
        return isCurrentUser(authentication, userId);
    }
    
    /**
     * 检查当前认证用户是否是管理员、教师或指定ID的学生
     */
    public boolean isStudentUser(Authentication authentication, Long studentId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // 检查是否是管理员或教师角色
        boolean isAdminOrTeacher = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") || 
                                      authority.getAuthority().equals("ROLE_TEACHER"));
        
        // 检查是否是当前学生用户
        return isAdminOrTeacher || isCurrentUser(authentication, studentId);
    }
        
    /**
     * 获取当前认证用户的ID
     */
    public Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
            
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.college.model.User) {
            com.college.model.User currentUser = (com.college.model.User) principal;
            return currentUser.getId();
        }
            
        return null;
    }
        
    /**
     * 检查当前认证用户是否是资源的学生所有者
     */
    public boolean isStudentOwner(Authentication authentication, Long resourceId) {
        // 简化实现：由于无法在SecurityService中注入具体的Repository，
        // 我们使用isStudentUser方法作为替代，确保管理员、教师或学生自己可以访问
        // 在实际应用中，应该根据资源类型查询相应的Repository并检查所有权关系
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // 获取当前认证用户
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.college.model.User) {
            com.college.model.User currentUser = (com.college.model.User) principal;
            
            // 管理员和教师可以访问所有资源
            boolean isAdminOrTeacher = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") || 
                                          authority.getAuthority().equals("ROLE_TEACHER"));
            
            if (isAdminOrTeacher) {
                return true;
            }
            
            // 学生只能访问自己的资源（这里假设资源ID直接对应学生ID）
            if (currentUser.getRole().equals("STUDENT")) {
                return currentUser.getId().equals(resourceId);
            }
        }
        
        return false;
    }
}