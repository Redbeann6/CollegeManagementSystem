package com.college.service;

import com.college.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    Department save(Department department);
    Optional<Department> findById(Long id);
    List<Department> findAll();
    void deleteById(Long id);
    List<Department> findByNameContaining(String name);
    Optional<Department> findByName(String name);
}