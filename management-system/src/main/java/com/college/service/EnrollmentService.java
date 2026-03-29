package com.college.service;

import com.college.model.Enrollment;
import com.college.dto.EnrollmentDTO;
import java.util.List;
import java.util.Optional;

public interface EnrollmentService {
    // 基本CRUD操作
    Enrollment save(Enrollment enrollment);
    Optional<Enrollment> findById(Long id);
    List<Enrollment> findAll();
    void deleteById(Long id);
    
    // 选课操作
    Enrollment enrollStudent(Long studentId, Long courseId, Long semesterId);
    void withdrawStudent(Long enrollmentId);
    
    // 批量操作
    void deleteBatch(List<Long> ids);
    List<Enrollment> batchSave(List<Enrollment> enrollments);
    List<Enrollment> batchDelete(List<Long> ids);
    
    // 查询功能
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);
    List<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean isCourseAvailable(Long courseId);
    List<Enrollment> findBySemesterId(Long semesterId);
    List<Enrollment> findBySemester(String semester);
    List<Enrollment> findByStudentIdAndSemesterId(Long studentId, Long semesterId);
    List<Enrollment> findByCourseIdAndSemesterId(Long courseId, Long semesterId);
    List<Enrollment> findByStatus(Enrollment.Status status);
    
    // 多条件查询
    List<Enrollment> searchEnrollments(Long studentId, Long courseId, Long semesterId, Enrollment.Status status);
    
    // 多参数组合查询方法
    List<Enrollment> findBySemesterIdAndCourseId(Long semesterId, Long courseId);
    
    List<Enrollment> findBySemesterIdAndStudentId(Long semesterId, Long studentId);
    
    List<Enrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);
    
    List<Enrollment> findBySemesterIdAndCourseIdAndStudentId(Long semesterId, Long courseId, Long studentId);
    
    // 验证功能
    boolean existsByStudentIdAndCourseIdAndSemesterId(Long studentId, Long courseId, Long semesterId);
    
    // 成绩管理
    Enrollment updateScore(Long enrollmentId, Double score);
    
    // 实体转换
    EnrollmentDTO convertToDTO(Enrollment enrollment);
    List<EnrollmentDTO> convertToDTOList(List<Enrollment> enrollments);
    Enrollment convertToEntity(EnrollmentDTO enrollmentDTO);
    
    // 辅助方法
    Long getCourseIdByEnrollmentId(Long enrollmentId);
    
    // 统计相关方法
    long countAllEnrollments();
    long countEnrollmentsInPreviousMonth();
    long countEnrollmentsInCurrentMonth();
    
    // 按学生ID统计选课数量
    int countEnrollmentsByStudentId(Long studentId);
    
    // 按课程ID统计注册人数
    int countEnrollmentsByCourseId(Long courseId);
}