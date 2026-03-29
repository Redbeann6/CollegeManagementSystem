package com.college.repository;

import com.college.model.Course;
import com.college.model.Exam;
import com.college.model.ExamResult;
import com.college.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findByStudent(Student student);

    List<ExamResult> findByCourse(Course course);

    List<ExamResult> findByExam(Exam exam);

    List<ExamResult> findByExamId(Long examId);

    Optional<ExamResult> findByStudentAndExam(Student student, Exam exam);
    
    // 查询所有成绩记录并预加载关联数据
    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.student LEFT JOIN FETCH er.exam LEFT JOIN FETCH er.course")
    List<ExamResult> findAllWithAssociations();
    
    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.student LEFT JOIN FETCH er.exam LEFT JOIN FETCH er.course WHERE er.exam.id = :examId")
    List<ExamResult> findByExamIdWithAssociations(@Param("examId") Long examId);
    
    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.student LEFT JOIN FETCH er.exam LEFT JOIN FETCH er.course WHERE er.student.id = :studentId")
    List<ExamResult> findByStudentIdWithAssociations(@Param("studentId") Long studentId);
    
    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.student LEFT JOIN FETCH er.exam LEFT JOIN FETCH er.course WHERE er.student.id = :studentId AND er.createdAt > :sinceDate")
    List<ExamResult> findByStudentIdAndCreatedAtAfter(@Param("studentId") Long studentId, @Param("sinceDate") java.time.LocalDateTime sinceDate);
    
    @Query("SELECT COUNT(er) FROM ExamResult er WHERE er.course.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT er FROM ExamResult er LEFT JOIN FETCH er.student LEFT JOIN FETCH er.exam LEFT JOIN FETCH er.course WHERE er.student.id = :studentId AND er.course.teacher.id = :teacherId")
    List<ExamResult> findByStudentIdAndTeacherId(@Param("studentId") Long studentId, @Param("teacherId") Long teacherId);
}