package com.college.dto;

import lombok.Data;

@Data
public class ProfileDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String avatar; // 头像URL
    
    // 学生特有字段
    private String studentId;
    private String className;
    private String major;
    private String gender;
    private String address;
    private String idCard;
    private String politicalStatus;
    
    // 教师特有字段
    private String teacherId;
    private String title;
    private String education;
    private String office;
    private String officePhone;
    private String mobilePhone;
    
    // 通用字段
    private String role;
}