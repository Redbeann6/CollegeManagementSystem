package com.college.repository;

import com.college.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByDepartmentId(Long departmentId);
    long countByDepartmentId(Long departmentId);
    
    // 添加根据身份证号查找学生的方法
    List<Student> findByidCard(String idCard);
    
    // 添加根据学号查找学生的方法
    Student findByStudentId(String studentId);
}