package com.college.service.impl;

import com.college.model.Semester;
import com.college.repository.SemesterRepository;
import com.college.service.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SemesterServiceImpl implements SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Override
    public List<Semester> getAllSemesters() {
        return semesterRepository.findAll();
    }

    @Override
    public Optional<Semester> getSemesterById(Long id) {
        return semesterRepository.findById(id);
    }

    @Override
    public Semester saveSemester(Semester semester) {
        return semesterRepository.save(semester);
    }

    @Override
    public Semester updateSemester(Long id, Semester semesterDetails) {
        Semester semester = semesterRepository.findById(id).orElseThrow(() -> new RuntimeException("Semester not found with id: " + id));
        semester.setName(semesterDetails.getName());
        semester.setStartDate(semesterDetails.getStartDate());
        semester.setEndDate(semesterDetails.getEndDate());
        return semesterRepository.save(semester);
    }

    @Override
    public void deleteSemester(Long id) {
        Semester semester = semesterRepository.findById(id).orElseThrow(() -> new RuntimeException("Semester not found with id: " + id));
        semesterRepository.delete(semester);
    }

    @Override
    public boolean semesterExists(String name) {
        return semesterRepository.existsByName(name);
    }
}
