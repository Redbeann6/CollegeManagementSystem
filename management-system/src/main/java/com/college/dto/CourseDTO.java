package com.college.dto;

import com.college.model.Course;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CourseDTO {

    private Long id;
    private String name;
    private String courseCode;
    private Integer credits;
    private Integer totalHours;
    private String classroom;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long teacherId;
    private String teacherName;
    private Integer studentCount;
    
    // 为CourseManagement.vue添加的字段
    private String courseType;  // 课程类型
    private Integer hours;      // 课时（与totalHours保持一致）
    private String departmentName; // 所属系部名称
    private String semesterName;   // 开设学期名称
    private Integer maxStudents;   // 最大选课人数
    private Boolean status;        // 课程状态
    
    // 用于关联的ID字段
    private Long departmentId;     // 系部ID
    private Long semesterId;       // 学期ID
    
    // 添加上课时间安排字段
    private String schedule;         // 上课时间安排
    
    // 课程已选人数
    private Integer enrollmentCount; // 已选人数
    
    // 班级名称
    private String className; // 班级名称
    
    // 为前端CourseManagement.vue页面添加的字段
    private Integer credit;        // 学分（与credits对应）
    private String semester;       // 学期（与semesterName对应）
    private Integer studentsCount; // 学生人数（与studentCount对应）
    private LocalDate startDate;   // 开始日期
    private LocalDate endDate;     // 结束日期
    private String statusText;     // 课程状态文本
    private Integer theoryHours;   // 理论学时
    private Integer practiceHours; // 实践学时
    private String assessmentMethod; // 考核方式
    
    // 添加别名以支持前端字段名
    @JsonProperty("courseName")
    public String getCourseName() {
        return this.name;
    }
    
    @JsonProperty("courseName")
    public void setCourseName(String courseName) {
        this.name = courseName;
    }
    
    public Integer getEnrollmentCount() {
        return enrollmentCount;
    }
    
    public void setEnrollmentCount(Integer enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }
    
    // 保持原有的name getter/setter
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public static CourseDTO fromCourse(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCourseCode(course.getCourseCode());
        dto.setCredits(course.getCredits());
        dto.setTotalHours(course.getTotalHours());
        dto.setHours(course.getTotalHours()); // 设置课时
        dto.setClassroom(course.getClassroom());
        dto.setDayOfWeek(course.getDayOfWeek());
        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        // 设置课程类型
        dto.setCourseType(course.getCourseType());
        // 设置最大选课人数
        dto.setMaxStudents(course.getMaxStudents());
        // 设置课程状态
        dto.setStatus(course.getStatus() != null ? course.getStatus() : true);
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            dto.setTeacherName(course.getTeacher().getName());
        }
        // 设置系部信息
        if (course.getDepartment() != null) {
            dto.setDepartmentId(course.getDepartment().getId());
            dto.setDepartmentName(course.getDepartment().getName());
        }
        // 设置学期信息
        if (course.getSemester() != null) {
            dto.setSemesterId(course.getSemester().getId());
            dto.setSemesterName(course.getSemester().getName());
        }
        // 设置学生数量 - 使用安全的访问方式
        try {
            if (course.getStudents() != null) {
                dto.setStudentCount(course.getStudents().size());
            } else {
                dto.setStudentCount(0);
            }
        } catch (Exception e) {
            // 如果出现懒加载或其他异常，设置为0
            dto.setStudentCount(0);
        }
        // 设置上课时间安排（schedule）
        StringBuilder schedule = new StringBuilder();
        if (course.getDayOfWeek() != null) {
            schedule.append(course.getDayOfWeek());
        }
        if (course.getStartTime() != null && course.getEndTime() != null) {
            if (schedule.length() > 0) {
                schedule.append(" ");
            }
            schedule.append(course.getStartTime().toString()).append("-").append(course.getEndTime().toString());
        }
        dto.setSchedule(schedule.toString());
        
        // 设置开始和结束日期
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        
        // 初始化已选人数为0，具体数值将在Controller中设置
        dto.setEnrollmentCount(0);
        return dto;
    }
}