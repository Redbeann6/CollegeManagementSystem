package com.college.service.impl;

import com.college.dto.MajorDTO;
import com.college.model.Major;
import com.college.repository.MajorRepository;
import com.college.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MajorServiceImpl implements MajorService {

    @Autowired
    private MajorRepository majorRepository;

    @Override
    @Transactional
    public Major save(Major major) {
        return majorRepository.save(major);
    }

    @Override
    public Optional<Major> findById(Long id) {
        return majorRepository.findById(id);
    }

    @Override
    public List<Major> findAll() {
        return majorRepository.findAll();
    }

    @Override
    public List<Major> findByDepartmentId(Long departmentId) {
        return majorRepository.findByDepartmentId(departmentId);
    }

    @Override
    public void deleteById(Long id) {
        majorRepository.deleteById(id);
    }

    @Override
    public MajorDTO convertToDTO(Major major) {
        if (major == null) {
            return null;
        }

        MajorDTO dto = new MajorDTO();
        dto.setId(major.getId());
        dto.setName(major.getName());
        dto.setDepartmentId(major.getDepartmentId());

        return dto;
    }

    @Override
    public Major convertToEntity(MajorDTO majorDTO) {
        if (majorDTO == null) {
            return null;
        }

        Major major = new Major();
        major.setId(majorDTO.getId());
        major.setName(majorDTO.getName());
        major.setDepartmentId(majorDTO.getDepartmentId());

        return major;
    }
}