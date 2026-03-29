package com.college.dto;

import com.college.model.ExamResult;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamResultDTO {

    private Long id;
    private Double score;
    private String grade;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long studentId;
    private String studentName;
    private String studentIdNumber;
    private Long courseId;
    private String courseName;
    private String courseCode;  // 课程号
    private Double credits;     // 学分
    private String courseType;  // 课程类型
    private String semester;    // 学期
    private String teacherName; // 授课教师
    private String statusText;  // 状态文本
    private LocalDateTime publishTime; // 发布时间
    private Long examId;
    private String examTitle;
    // 新增字段
    private String examName;
    private Double percentage;
    private String department;
    private LocalDateTime examDate;
    private LocalDateTime entryDate;
    private String entryBy;
    private String status;
    private Double totalMarks;
    private String studentAvatar;

    public static ExamResultDTO fromExamResult(ExamResult examResult) {
        ExamResultDTO dto = new ExamResultDTO();
        dto.setId(examResult.getId());
        dto.setScore(examResult.getScore());
        dto.setGrade(examResult.getGrade());
        dto.setFeedback(examResult.getFeedback());
        dto.setCreatedAt(examResult.getCreatedAt());
        dto.setUpdatedAt(examResult.getUpdatedAt());
        if (examResult.getStudent() != null) {
            dto.setStudentId(examResult.getStudent().getId());
            dto.setStudentName(examResult.getStudent().getName());
            dto.setStudentIdNumber(examResult.getStudent().getStudentId());
            // 设置学生头像
            if (examResult.getStudent().getAvatar() != null && !examResult.getStudent().getAvatar().isEmpty()) {
                dto.setStudentAvatar("/avatar/" + examResult.getStudent().getAvatar());
            } else {
                dto.setStudentAvatar("/avatar/default.jpg");
            }
        }
        if (examResult.getCourse() != null) {
            dto.setCourseId(examResult.getCourse().getId());
            dto.setCourseName(examResult.getCourse().getName());
            dto.setCourseCode(examResult.getCourse().getCourseCode()); // 课程号
            dto.setCredits(examResult.getCourse().getCredits() != null ? examResult.getCourse().getCredits().doubleValue() : null); // 学分
            dto.setCourseType(examResult.getCourse().getCourseType()); // 课程类型
            if (examResult.getCourse().getTeacher() != null) {
                dto.setTeacherName(examResult.getCourse().getTeacher().getName()); // 授课教师
            }
            if (examResult.getCourse().getSemester() != null) {
                dto.setSemester(examResult.getCourse().getSemester().getName()); // 学期
            }
        }
        if (examResult.getExam() != null) {
            dto.setExamId(examResult.getExam().getId());
            dto.setExamTitle(examResult.getExam().getTitle());
            // 设置考试名称和考试日期
            dto.setExamName(examResult.getExam().getTitle());
            dto.setExamDate(examResult.getExam().getExamTime());
            
            // 设置总分
            if (examResult.getExam().getTotalMarks() != null) {
                dto.setTotalMarks(examResult.getExam().getTotalMarks().doubleValue());
            } else {
                dto.setTotalMarks(100.0);  // 默认总分为100
            }
        }
        
        // 计算百分比
        if (examResult.getScore() != null && dto.getTotalMarks() != null && dto.getTotalMarks() > 0) {
            dto.setPercentage((examResult.getScore() / dto.getTotalMarks()) * 100);
        }
        
        // 设置录入日期为更新时间或创建时间
        if (examResult.getUpdatedAt() != null) {
            dto.setEntryDate(examResult.getUpdatedAt());
        } else if (examResult.getCreatedAt() != null) {
            dto.setEntryDate(examResult.getCreatedAt());
        }
        
        // 设置录入人信息
        dto.setEntryBy(examResult.getEntryBy());
        
        // 设置考试状态
        dto.setStatus(examResult.getStatus());
        
        // 设置状态文本
        if ("published".equalsIgnoreCase(examResult.getStatus()) || "ATTENDED".equalsIgnoreCase(examResult.getStatus())) {
            dto.setStatusText("已发布");
        } else {
            dto.setStatusText("未发布");
        }
        
        // 设置发布时间为更新时间或创建时间
        if (examResult.getUpdatedAt() != null) {
            dto.setPublishTime(examResult.getUpdatedAt());
        } else if (examResult.getCreatedAt() != null) {
            dto.setPublishTime(examResult.getCreatedAt());
        }
        
        return dto;
    }
}