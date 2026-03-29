package com.college.repository;

import com.college.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByTeacherId(String teacherId);

    Optional<Teacher> findByUsername(String username);

    boolean existsByTeacherId(String teacherId);
    
    List<Teacher> findByDepartmentId(Long departmentId);
    long countByDepartmentId(Long departmentId);
    
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.createdAt BETWEEN ?1 AND ?2")
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}