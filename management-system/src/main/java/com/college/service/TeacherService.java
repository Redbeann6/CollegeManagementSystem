package com.college.service;

import com.college.model.Teacher;
import com.college.dto.TeacherDTO;
import java.util.List;
import java.util.Optional;

public interface TeacherService {
    Teacher save(Teacher teacher);
    Optional<Teacher> findById(Long id);
    List<Teacher> findAll();
    void deleteById(Long id);
    List<Teacher> findByDepartmentId(Long departmentId);
    TeacherDTO convertToDTO(Teacher teacher);
    Teacher convertToEntity(TeacherDTO teacherDTO);
    
    // 统计相关方法
    long countAllTeachers();
    long countTeachersInPreviousMonth();
    long countTeachersInCurrentMonth();
    long countByDepartmentId(Long departmentId);
}