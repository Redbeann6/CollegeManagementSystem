package com.college.service;

import com.college.model.Exam;
import com.college.dto.ExamDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExamService {
    Exam save(Exam exam);
    Optional<Exam> findById(Long id);
    List<Exam> findAll();
    void deleteById(Long id);
    List<Exam> findByCourseId(Long courseId);
    List<Exam> findByCreatorId(Long creatorId);
    List<Exam> findByExamTimeBetween(LocalDateTime start, LocalDateTime end);
    ExamDTO convertToDTO(Exam exam);
    Exam convertToEntity(ExamDTO examDTO);
    List<Map<String, Object>> getExamStudents(Long examId);
    Map<String, Object> getExamStatistics(Long examId);
    void updateAllExamStatuses();
    int getTotalEnrolledStudentsCount(Long examId);
    
    // 学生相关考试查询方法
    List<Exam> findUpcomingExamsByStudentId(Long studentId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    int countUpcomingExamsByStudentId(Long studentId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    // 获取学生的所有考试
    List<Exam> findExamsByStudentId(Long studentId);
}