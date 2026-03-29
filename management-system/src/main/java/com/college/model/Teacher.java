package com.college.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"courses", "exams", "leaveRequests"})
@Entity
@Table(name = "teachers")
// 移除了错误的@PrimaryKeyJoinColumn(name = "user_id")注解，因为数据库表中使用的是"id"作为主键和外键
public class Teacher extends User {

    @Column(nullable = false, length = 20)
    private String teacherId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 50)
    private String title;

    private String education;
    
    @Column(length = 10)
    private String gender;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "join_date")
    private LocalDate joinDate;
    
    @Column(name = "graduate_school")
    private String graduateSchool;
    
    private String major;
    
    private String biography;
    
    private String office;
    
    @Column(name = "office_phone")
    
    // 覆盖toString()方法，避免访问懒加载属性
    @Override
    public String toString() {
        return "Teacher{" +
                "teacherId='" + teacherId + '\'' +
                ", title='" + title + '\'' +
                ", education='" + education + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", joinDate=" + joinDate +
                ", graduateSchool='" + graduateSchool + '\'' +
                ", major='" + major + '\'' +
                ", biography='" + biography + '\'' +
                ", office='" + office + '\'' +
                ", id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", role=" + getRole() +
                ", enabled=" + isEnabled() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
    private String officePhone;
    
    @Column(name = "mobile_phone")
    private String mobilePhone;
    
    private String address;

    @JsonIgnore
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private Set<Course> courses = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private Set<Exam> exams = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "handler", cascade = CascadeType.ALL)
    private Set<LeaveRequest> leaveRequests = new HashSet<>();
}