package com.college.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@Data
@EqualsAndHashCode(exclude = {"teachers", "students"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 20)
    private String abbreviation;

    @Column(length = 20, unique = true)
    private String code;

    @Column(length = 50)
    private String dean;

    @Column(length = 50)
    private String deputyDean;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String email;

    @Column(name = "establish_date")
    private LocalDate establishDate;

    @Column(length = 1000)
    private String history;

    @Column(length = 100)
    private String location;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "student_count")
    private Integer studentCount;

    @Column(name = "teacher_count")
    private Integer teacherCount;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "department")
    private Set<Teacher> teachers = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "department")
    private Set<Student> students = new HashSet<>();
}