package com.college.service;

import com.college.dto.ClazzDTO;
import com.college.model.Clazz;

import java.util.List;
import java.util.Optional;

public interface ClazzService {
    Clazz save(Clazz clazz);
    Optional<Clazz> findById(Long id);
    List<Clazz> findAll();
    List<Clazz> findByMajorId(Long majorId);
    List<Clazz> findByDepartmentId(Long departmentId);
    void deleteById(Long id);
    ClazzDTO convertToDTO(Clazz clazz);
    Clazz convertToEntity(ClazzDTO clazzDTO);
}