package com.college.dto;

import com.college.model.Enrollment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String major;  // 学生专业
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Integer credits;  // 课程学分
    private Long semesterId;
    private String semesterName;  // 学期名称
    private LocalDateTime enrollDate;
    private Enrollment.Status status;
    private Double score;
    private String remark;
    private Long teacherId;
    private String teacherName;

    public static EnrollmentDTO fromEnrollment(Enrollment enrollment, com.college.repository.SemesterRepository semesterRepository) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        
        if (enrollment.getStudent() != null) {
            dto.setStudentId(enrollment.getStudent().getId());
            dto.setStudentName(enrollment.getStudent().getName());
            dto.setStudentNo(enrollment.getStudent().getStudentId());
            // 设置学生专业信息
            dto.setMajor(enrollment.getStudent().getMajor());
        }
        
        if (enrollment.getCourse() != null) {
            dto.setCourseId(enrollment.getCourse().getId());
            dto.setCourseName(enrollment.getCourse().getName());
            dto.setCourseCode(enrollment.getCourse().getCourseCode());
            // 设置课程学分信息
            dto.setCredits(enrollment.getCourse().getCredits());
            if (enrollment.getCourse().getTeacher() != null) {
                dto.setTeacherId(enrollment.getCourse().getTeacher().getId());
                dto.setTeacherName(enrollment.getCourse().getTeacher().getName());
            }
        }
        
        dto.setSemesterId(enrollment.getSemesterId());
        // 获取学期名称
        if (enrollment.getSemesterId() != null && semesterRepository != null) {
            semesterRepository.findById(enrollment.getSemesterId())
                    .ifPresent(semester -> dto.setSemesterName(semester.getName()));
        }
        dto.setEnrollDate(enrollment.getEnrollDate());
        dto.setStatus(enrollment.getStatus());
        dto.setScore(enrollment.getScore());
        dto.setRemark(enrollment.getRemark());
        
        return dto;
    }

    public static EnrollmentDTO fromEnrollment(Enrollment enrollment) {
        return fromEnrollment(enrollment, null);
    }
}