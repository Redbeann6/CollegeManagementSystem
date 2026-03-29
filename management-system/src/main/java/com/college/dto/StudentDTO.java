package com.college.dto;

import com.college.model.Student;
import lombok.Data;

import java.time.LocalDate;
import java.time.Period;

@Data
public class StudentDTO {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String role;
    private boolean enabled;
    private String studentId;
    private String className;
    private String major;
    private LocalDate enrollmentDate;
    private String gender;
    private LocalDate birthDate;
    private Long departmentId;
    private Long majorId;
    private Long classId;
    
    // 新增字段
    private String address;
    private String idCard;
    private String politicalStatus;
    
    // 添加以下字段用于显示名称
    private String departmentName;
    private String majorName;
    private Integer admissionYear;
    
    // 添加年龄和平均成绩字段
    private Integer age;
    private Double avgScore;
    private String courseStatus; // 课程状态

    public static StudentDTO fromStudent(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setUsername(student.getUsername());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setRole(student.getRole().name());
        dto.setEnabled(student.isEnabled());
        dto.setStudentId(student.getStudentId());
        dto.setClassName(student.getClassName()); // 从学生实体获取班级名称
        dto.setMajor(student.getMajor());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        // 转换性别值为前端期望的格式
        String gender = student.getGender();
        if ("男".equals(gender)) {
            dto.setGender("male");
        } else if ("女".equals(gender)) {
            dto.setGender("female");
        } else {
            dto.setGender(gender); // 保留原始值
        }
        dto.setBirthDate(student.getBirthDate());
        
        // 新增字段
        dto.setAddress(student.getAddress());
        dto.setIdCard(student.getIdCard());
        dto.setPoliticalStatus(student.getPoliticalStatus());
        
        // 设置关联ID和名称
        if (student.getDepartment() != null) {
            dto.setDepartmentId(student.getDepartment().getId());
            dto.setDepartmentName(student.getDepartment().getName()); // 添加系部名称
        }
        if (student.getMajorRef() != null) {
            dto.setMajorId(student.getMajorRef().getId());
            dto.setMajorName(student.getMajorRef().getName()); // 添加专业名称
        }
        if (student.getClazz() != null && student.getClazz().getName() != null) {
            dto.setClassId(student.getClazz().getId());
            dto.setClassName(student.getClazz().getName()); // 使用关联的班级实体名称覆盖，如果存在的话
        }
        
        // 计算入学年份
        if (student.getEnrollmentDate() != null) {
            dto.setAdmissionYear(student.getEnrollmentDate().getYear());
        }
        
        // 计算年龄
        if (student.getBirthDate() != null) {
            dto.setAge(Period.between(student.getBirthDate(), LocalDate.now()).getYears());
        }
        
        // 设置平均成绩（暂时设为0，后续可以从成绩表中计算）
        dto.setAvgScore(0.0);
        
        return dto;
    }
}