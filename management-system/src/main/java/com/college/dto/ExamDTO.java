package com.college.dto;

import com.college.model.Exam;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamDTO {

    private Long id;
    private String name;  // 对应前端期望的name
    private String title;  // 保留原始title
    private LocalDateTime examTime;
    private LocalDateTime startTime;  // 添加startTime
    private LocalDateTime endTime;    // 添加endTime
    private String location;  // 对应前端期望的location
    private String classroom;  // 保留原始classroom
    private Integer duration;
    private String description;
    private String examType;  // 添加考试类型
    private String type;       // 为前端兼容添加type字段
    private Integer totalMarks;  // 添加总分
    private Integer totalScore;  // 为前端兼容添加totalScore字段
    private String status;  // 添加状态
    private Integer studentCount;  // 添加参考人数
    private String teacherName;  // 对应前端期望的teacherName
    private String examDate;  // 添加考试日期，为前端兼容
    private String frontendStatus;  // 添加前端兼容状态字段
    private Long courseId;
    private String courseName;
    private Long creatorId;
    private String creatorName;
    private Long teacherId;  // 添加teacherId字段用于接收前端传来的监考教师ID

    public static ExamDTO fromExam(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        // 将title映射到name以匹配前端需求
        dto.setName(exam.getTitle());
        dto.setTitle(exam.getTitle());
        dto.setExamTime(exam.getExamTime());
        // 正确映射时间字段，优先使用数据库中的startTime和endTime字段
        dto.setStartTime(exam.getStartTime() != null ? exam.getStartTime() : exam.getExamTime());
        // 根据数据库中的endTime或通过examTime+duration计算endTime
        if (exam.getEndTime() != null) {
            dto.setEndTime(exam.getEndTime());
        } else if (exam.getExamTime() != null && exam.getDuration() != null) {
            dto.setEndTime(exam.getExamTime().plusMinutes(exam.getDuration()));
        } else {
            dto.setEndTime(exam.getExamTime());
        }
        
        // 为前端兼容设置examDate字段，使用考试开始时间
        if (dto.getStartTime() != null) {
            // 将LocalDateTime转换为日期字符串格式，供前端使用
            dto.setExamDate(dto.getStartTime().toLocalDate().toString());
        } else if (dto.getExamTime() != null) {
            dto.setExamDate(dto.getExamTime().toLocalDate().toString());
        } else {
            dto.setExamDate(null);
        }

        // 将classroom映射到location以匹配前端需求
        dto.setLocation(exam.getClassroom());
        dto.setClassroom(exam.getClassroom());
        dto.setDuration(exam.getDuration());
        dto.setDescription(exam.getDescription());
        // 使用实体中的考试类型，如果存在的话
        if (exam.getExamType() != null) {
            dto.setExamType(exam.getExamType());
            // 为前端兼容设置type字段，转换为小写格式
            dto.setType(convertExamTypeToFrontendFormat(exam.getExamType()));
        } else {
            // 设置默认考试类型
            dto.setExamType("EXAM");
            dto.setType("exam");  // 为前端兼容设置type字段
        }
        // 设置默认总分
        dto.setTotalMarks(100);
        dto.setTotalScore(100);  // 为前端兼容设置totalScore字段
        // 使用实体中的状态值，如果存在的话
        if (exam.getStatus() != null) {
            dto.setStatus(exam.getStatus());
        } else {
            // 设置默认状态
            if (exam.getExamTime() != null) {
                // 计算考试结束时间
                // 使用系统默认时区的当前时间，确保与数据库时间在同一时区进行比较
                java.time.LocalDateTime currentTime = java.time.LocalDateTime.now(java.time.ZoneId.systemDefault());
                java.time.LocalDateTime examEndTime = exam.getExamTime();
                
                if (exam.getDuration() != null) {
                    examEndTime = exam.getExamTime().plusMinutes(exam.getDuration());
                }
                
                if (currentTime.isBefore(exam.getExamTime())) {
                    // 当前时间早于考试开始时间
                    dto.setStatus("NOT_STARTED");
                } else if (currentTime.isBefore(examEndTime)) {
                    // 当前时间在考试开始和结束之间
                    dto.setStatus("IN_PROGRESS");
                } else {
                    // 当前时间晚于考试结束时间
                    dto.setStatus("COMPLETED");
                }
            } else {
                dto.setStatus("NOT_STARTED");
            }
        }
        
        // 为兼容教师端前端，也接受小写状态值
        if ("completed".equalsIgnoreCase(dto.getStatus())) {
            dto.setStatus("COMPLETED");
        } else if ("not_started".equalsIgnoreCase(dto.getStatus())) {
            dto.setStatus("NOT_STARTED");
        } else if ("in_progress".equalsIgnoreCase(dto.getStatus())) {
            dto.setStatus("IN_PROGRESS");
        } else if ("cancelled".equalsIgnoreCase(dto.getStatus())) {
            dto.setStatus("CANCELLED");
        } else if ("draft".equalsIgnoreCase(dto.getStatus())) {
            dto.setStatus("DRAFT");
        }
        
        // 设置前端兼容的状态字段（小写格式）
        String frontendStatusValue = convertStatusToFrontendFormat(dto.getStatus());
        dto.setFrontendStatus(frontendStatusValue);
        // 为了让前端能够正确显示，我们也设置status字段为前端兼容的值
        dto.setStatus(frontendStatusValue);
        // 计算参考人数 - 获取考试对应课程的选课学生总数
        // 注意：这里需要注入ExamService来获取实际的选课人数，但DTO中无法注入服务
        // 因此在实际使用时，需要在Controller层获取这个信息并传递给DTO
        // 这里暂时保留原来的逻辑，但注释说明了正确的做法
        if (exam.getCourse() != null) {
            // 由于在DTO转换器中无法直接访问服务层，我们暂时使用examResults的数量
            // 在Controller中应该单独获取选课人数
            if (exam.getExamResults() != null) {
                dto.setStudentCount(exam.getExamResults().size());
            } else {
                dto.setStudentCount(0);
            }
        } else {
            dto.setStudentCount(0);
        }
        // 将creatorName映射到teacherName以匹配前端需求
        if (exam.getCreator() != null) {
            dto.setTeacherName(exam.getCreator().getName());
        }
        if (exam.getCourse() != null) {
            dto.setCourseId(exam.getCourse().getId());
            dto.setCourseName(exam.getCourse().getName());
        }
        if (exam.getCreator() != null) {
            dto.setCreatorId(exam.getCreator().getId());
            dto.setCreatorName(exam.getCreator().getName());
            // 同时设置teacherId，以便在编辑时回显
            dto.setTeacherId(exam.getCreator().getId());
        }
        return dto;
    }
    
    // Getter and Setter methods
    public String getExamDate() {
        return examDate;
    }
    
    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }
    
    public String getFrontendStatus() {
        return frontendStatus;
    }
    
    public void setFrontendStatus(String frontendStatus) {
        this.frontendStatus = frontendStatus;
    }
    
    // 工具方法：将考试类型转换为前端兼容格式
    private static String convertExamTypeToFrontendFormat(String backendType) {
        if (backendType == null) return "unknown";
        
        switch (backendType.toUpperCase()) {
            case "MIDTERM":
                return "midterm";
            case "FINAL":
                return "final";
            case "QUIZ":
                return "quiz";
            case "EXPERIMENT":
                return "experiment";
            case "EXAM":
                return "exam";
            default:
                // 如果是其他格式，尝试转换为小写
                return backendType.toLowerCase();
        }
    }
    
    // 工具方法：将状态转换为前端兼容格式
    private static String convertStatusToFrontendFormat(String backendStatus) {
        if (backendStatus == null) return "unknown";
        
        switch (backendStatus.toUpperCase()) {
            case "NOT_STARTED":
                return "scheduled";
            case "IN_PROGRESS":
                return "scheduled"; // 可以考虑为进行中状态
            case "COMPLETED":
                return "completed";
            case "CANCELLED":
                return "cancelled";
            case "DRAFT":
                return "draft";
            default:
                // 如果是其他格式，尝试转换为小写
                return backendStatus.toLowerCase();
        }
    }
}