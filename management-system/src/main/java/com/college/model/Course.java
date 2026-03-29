package com.college.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Data
@EqualsAndHashCode(exclude = {"students", "exams", "examResults"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String courseCode;

    private Integer credits;

    private Integer totalHours;

    @Column(length = 20)
    private String classroom;

    @Column(length = 20)
    private String dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(length = 20)  // 课程类型字段
    private String courseType;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "is_active")
    private Boolean status;
    
    @Column(name = "start_date")
    private java.time.LocalDate startDate;
    
    @Column(name = "end_date")
    private java.time.LocalDate endDate;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "course_students",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<Student> students = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Exam> exams = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExamResult> examResults = new HashSet<>();
    
    // 获取课程时间安排信息
    public String getScheduleInfo() {
        StringBuilder schedule = new StringBuilder();
        if (dayOfWeek != null) {
            schedule.append(dayOfWeek);
        }
        if (startTime != null && endTime != null) {
            if (schedule.length() > 0) {
                schedule.append(" ");
            }
            schedule.append(startTime.toString()).append("-").append(endTime.toString());
        }
        return schedule.toString();
    }
}