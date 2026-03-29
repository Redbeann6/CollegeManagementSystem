package com.college.service.impl;

import com.college.model.Department;
import com.college.repository.DepartmentRepository;
import com.college.service.DepartmentService;
import com.college.service.StudentService;
import com.college.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private StudentService studentService;

    @Override
    @Transactional
    public Department save(Department department) {
        // 自动计算统计数量
        calculateStatistics(department);
        return departmentRepository.save(department);
    }
    
    // 计算部门的统计数量
    private void calculateStatistics(Department department) {
        if (department.getId() != null) {
            department.setTeacherCount((int) teacherService.countByDepartmentId(department.getId()));
            department.setStudentCount((int) studentService.countByDepartmentId(department.getId()));
        }
    }
    
    // 计算多个部门的统计数量
    private void calculateStatistics(List<Department> departments) {
        for (Department dept : departments) {
            calculateStatistics(dept);
        }
    }

    @Override
    public Optional<Department> findById(Long id) {
        Optional<Department> departmentOpt = departmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            Department department = departmentOpt.get();
            calculateStatistics(department);
        }
        return departmentOpt;
    }

    @Override
    public List<Department> findAll() {
        List<Department> departments = departmentRepository.findAll();
        calculateStatistics(departments);
        return departments;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public List<Department> findByNameContaining(String name) {
        List<Department> departments = departmentRepository.findByNameContaining(name);
        calculateStatistics(departments);
        return departments;
    }

    @Override
    public Optional<Department> findByName(String name) {
        Optional<Department> departmentOpt = departmentRepository.findByName(name);
        if (departmentOpt.isPresent()) {
            Department department = departmentOpt.get();
            calculateStatistics(department);
        }
        return departmentOpt;
    }
}