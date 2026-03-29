package com.college.service;

import com.college.dto.MajorDTO;
import com.college.model.Major;

import java.util.List;
import java.util.Optional;

public interface MajorService {
    Major save(Major major);
    Optional<Major> findById(Long id);
    List<Major> findAll();
    List<Major> findByDepartmentId(Long departmentId);
    void deleteById(Long id);
    MajorDTO convertToDTO(Major major);
    Major convertToEntity(MajorDTO majorDTO);
}