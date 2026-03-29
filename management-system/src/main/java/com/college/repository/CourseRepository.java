package com.college.repository;

import com.college.model.Course;
import com.college.model.Student;
import com.college.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    List<Course> findByTeacher(Teacher teacher);

    List<Course> findByStudentsContaining(Student student);

    List<Course> findByDayOfWeek(String dayOfWeek);

    boolean existsByCourseCode(String courseCode);
    
    @Override
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.department LEFT JOIN FETCH c.semester LEFT JOIN FETCH c.students")
    List<Course> findAll();
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt BETWEEN ?1 AND ?2")
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.department LEFT JOIN FETCH c.semester WHERE (:departmentId IS NULL OR c.department.id = :departmentId) AND (:teacherId IS NULL OR c.teacher.id = :teacherId) AND (:semesterId IS NULL OR c.semester.id = :semesterId) AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> findCoursesByFilters(
        @Param("departmentId") Long departmentId,
        @Param("teacherId") Long teacherId,
        @Param("semesterId") Long semesterId,
        @Param("keyword") String keyword
    );
    
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.department LEFT JOIN FETCH c.semester LEFT JOIN FETCH c.students WHERE c.teacher.id = :teacherId")
    List<Course> findByTeacherWithStudents(@Param("teacherId") Long teacherId);
}