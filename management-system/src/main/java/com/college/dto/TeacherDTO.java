package com.college.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String teacherId;
    private String department;
    private String title;
    private String education;
    private Boolean enabled;
    private String gender;
    private LocalDate birthDate;
    private LocalDate joinDate;
    private String graduateSchool;
    private String major;
    private String biography;
    private String office;
    private String officePhone;
    private String mobilePhone;
    private String address;
    private Long departmentId;
}