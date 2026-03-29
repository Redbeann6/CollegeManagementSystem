package com.college.service;

import com.college.dto.StudentDTO;
import java.util.List;

public interface StudentService {
    List<StudentDTO> getAllStudents();
    StudentDTO getStudentById(Long id);
    StudentDTO createStudent(StudentDTO studentDTO);
    StudentDTO updateStudent(Long id, StudentDTO studentDTO);
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);
    long countAllStudents();
    long countStudentsInPreviousMonth();
    long countStudentsInCurrentMonth();
    long countByDepartmentId(Long departmentId);
    StudentDTO updateStudentStatus(Long id, boolean enabled);
}