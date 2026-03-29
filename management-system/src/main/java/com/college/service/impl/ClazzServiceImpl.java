package com.college.service.impl;

import com.college.dto.ClazzDTO;
import com.college.model.Clazz;
import com.college.model.Major;
import com.college.repository.ClazzRepository;
import com.college.repository.MajorRepository;
import com.college.service.ClazzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClazzServiceImpl implements ClazzService {

    @Autowired
    private ClazzRepository clazzRepository;
    
    @Autowired
    private MajorRepository majorRepository;

    @Override
    @Transactional
    public Clazz save(Clazz clazz) {
        return clazzRepository.save(clazz);
    }

    @Override
    public Optional<Clazz> findById(Long id) {
        return clazzRepository.findById(id);
    }

    @Override
    public List<Clazz> findAll() {
        return clazzRepository.findAll();
    }

    @Override
    public List<Clazz> findByMajorId(Long majorId) {
        return clazzRepository.findByMajorId(majorId);
    }

    @Override
    public List<Clazz> findByDepartmentId(Long departmentId) {
        return clazzRepository.findByDepartmentId(departmentId);
    }

    @Override
    public void deleteById(Long id) {
        clazzRepository.deleteById(id);
    }

    @Override
    public ClazzDTO convertToDTO(Clazz clazz) {
        if (clazz == null) {
            return null;
        }

        ClazzDTO dto = new ClazzDTO();
        dto.setId(clazz.getId());
        dto.setName(clazz.getName());
        dto.setMajorId(clazz.getMajorId());
        // 如果有专业信息，设置系部ID
        if (clazz.getMajorId() != null) {
            // 通过Repository获取专业信息以获取系部ID
            Optional<Major> majorOpt = majorRepository.findById(clazz.getMajorId());
            if (majorOpt.isPresent()) {
                dto.setDepartmentId(majorOpt.get().getDepartmentId());
            }
        }
        dto.setEnrollmentYear(clazz.getEnrollmentYear());
        dto.setCapacity(clazz.getCapacity());

        return dto;
    }

    @Override
    public Clazz convertToEntity(ClazzDTO clazzDTO) {
        if (clazzDTO == null) {
            return null;
        }

        Clazz clazz = new Clazz();
        clazz.setId(clazzDTO.getId());
        clazz.setName(clazzDTO.getName());
        clazz.setMajorId(clazzDTO.getMajorId());
        clazz.setEnrollmentYear(clazzDTO.getEnrollmentYear());
        clazz.setCapacity(clazzDTO.getCapacity());

        return clazz;
    }
}