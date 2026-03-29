package com.college.repository;

import com.college.model.Enrollment;
import com.college.model.Course;
import com.college.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 根据学生查询选课记录
    List<Enrollment> findByStudent(Student student);
    
    // 根据课程查询选课记录
    List<Enrollment> findByCourse(Course course);
    
    // 根据学期查询选课记录
    List<Enrollment> findBySemesterId(Long semesterId);
    
    // 根据学生ID查询选课记录
    List<Enrollment> findByStudentId(Long studentId);
    
    // 根据课程ID查询选课记录
    List<Enrollment> findByCourseId(Long courseId);
    
    // 检查学生是否已经选过某门课程
    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);
    
    // 根据学生ID、课程ID和学期ID查询选课记录
    Optional<Enrollment> findByStudentIdAndCourseIdAndSemesterId(Long studentId, Long courseId, Long semesterId);
    
    // 根据学生ID和学期ID查询选课记录
    List<Enrollment> findByStudentIdAndSemesterId(Long studentId, Long semesterId);
    
    // 根据课程ID和学期ID查询选课记录
    List<Enrollment> findByCourseIdAndSemesterId(Long courseId, Long semesterId);
    
    // 根据学生ID和课程ID查询选课记录
    List<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    // 根据状态查询选课记录
    List<Enrollment> findByStatus(Enrollment.Status status);
    
    // 多参数组合查询方法
    List<Enrollment> findBySemesterIdAndCourseId(Long semesterId, Long courseId);
    
    List<Enrollment> findBySemesterIdAndStudentId(Long semesterId, Long studentId);
    
    List<Enrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);
    
    List<Enrollment> findBySemesterIdAndCourseIdAndStudentId(Long semesterId, Long courseId, Long studentId);
    
    // 删除指定学生和课程的选课记录
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
    
    // 统计某门课程的选课人数
    Long countByCourseId(Long courseId);
    
    // 统计某学期的总选课数
    Long countBySemesterId(Long semesterId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.enrollDate BETWEEN ?1 AND ?2")
    long countByEnrollDateBetween(LocalDateTime start, LocalDateTime end);
}