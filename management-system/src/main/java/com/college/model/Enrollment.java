package com.college.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"student", "course"})
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @CreationTimestamp
    @Column(name = "enroll_date", nullable = false)
    private LocalDateTime enrollDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "score")
    private Double score;

    @Column(name = "remark")
    private String remark;

    public enum Status {
        NORMAL, IN_PROGRESS, WITHDRAWN, COMPLETED, FAILED
    }
    
    @PrePersist
    protected void onCreate() {
        if (enrollDate == null) {
            enrollDate = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.NORMAL;
        }
    }
}