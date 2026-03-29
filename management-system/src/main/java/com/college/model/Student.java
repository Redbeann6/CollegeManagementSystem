package com.college.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"courses", "examResults", "leaveRequests"})
@Entity
@Table(name = "students")
// 移除了错误的@PrimaryKeyJoinColumn(name = "user_id")注解，因为数据库表中使用的是"id"作为主键和外键
public class Student extends User {

    @Column(nullable = false, length = 20)
    private String studentId;

    @Column(length = 50)
    private String className;

    @Column(length = 100)
    private String major;

    private LocalDate enrollmentDate;

    private String gender;

    private LocalDate birthDate;

    // 新增字段
    @Column(length = 200)
    private String address;

    @Column(length = 18, unique = true)
    private String idCard;

    @Column(length = 20)
    private String politicalStatus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    // 覆盖toString()方法，避免访问懒加载属性
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", className='" + className + '\'' +
                ", major='" + major + '\'' +
                ", enrollmentDate=" + enrollmentDate +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", address='" + address + '\'' +
                ", idCard='" + idCard + '\'' +
                ", politicalStatus='" + politicalStatus + '\'' +
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major majorRef;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Clazz clazz;

    @JsonIgnore
    @ManyToMany(mappedBy = "students", cascade = CascadeType.ALL)
    private Set<Course> courses = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExamResult> examResults = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LeaveRequest> leaveRequests = new HashSet<>();
}