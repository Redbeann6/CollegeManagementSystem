package com.college.service;

import com.college.model.ExamResult;
import com.college.dto.ExamResultDTO;
import java.util.List;
import java.util.Optional;

public interface ExamResultService {
    ExamResult save(ExamResult examResult);
    Optional<ExamResult> findById(Long id);
    List<ExamResult> findAll();
    void deleteById(Long id);
    List<ExamResult> findByStudentId(Long studentId);
    List<ExamResult> findByCourseId(Long courseId);
    List<ExamResult> findByExamId(Long examId);
    Optional<ExamResult> findByStudentIdAndExamId(Long studentId, Long examId);
    ExamResultDTO convertToDTO(ExamResult examResult);
    ExamResult convertToEntity(ExamResultDTO examResultDTO);
    
    // 新增方法：根据教师ID统计未评分数量
    long countUngradedByTeacherId(Long teacherId);
    
    // 新增方法：根据教师ID统计考试结果数量
    long countByTeacherId(Long teacherId);
    
    // 批量保存成绩
    List<ExamResult> batchSave(List<ExamResult> examResults);
    
    // 获取考试成绩列表并计算排名
    
    // 学生相关成绩查询方法
    List<ExamResult> findRecentResultsByStudentId(Long studentId, java.time.LocalDate sinceDate);
    
    int countRecentResultsByStudentId(Long studentId, java.time.LocalDate sinceDate);
}