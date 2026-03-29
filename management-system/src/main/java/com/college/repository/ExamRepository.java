package com.college.repository;

import com.college.model.Course;
import com.college.model.Exam;
import com.college.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCourse(Course course);

    List<Exam> findByCreator(Teacher creator);

    List<Exam> findByExamTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Exam> findByCourseIdInAndExamTimeBetween(List<Long> courseIds, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Exam> findByCourseIdIn(List<Long> courseIds);
    
    // 注意：这里需要根据实际的实体关系来实现查询考试学生的方法
    // 由于Exam和User(学生)之间没有直接关联，可能需要通过Enrollment或其他关联实体来查询
}