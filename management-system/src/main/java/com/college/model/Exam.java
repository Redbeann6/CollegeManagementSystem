package com.college.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exams")
@Data
@EqualsAndHashCode(exclude = {"examResults"})
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "exam_time")
    private LocalDateTime examTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(length = 20)
    private String classroom;

    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 20)
    private String examType;
    
    @Column(name = "total_marks")
    private Integer totalMarks;
    
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'NOT_STARTED'")
    private String status = "NOT_STARTED";

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Teacher creator;

    @JsonIgnore
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExamResult> examResults = new HashSet<>();
}