package com.college.service;

import com.college.model.Semester;

import java.util.List;
import java.util.Optional;

public interface SemesterService {
    List<Semester> getAllSemesters();
    Optional<Semester> getSemesterById(Long id);
    Semester saveSemester(Semester semester);
    Semester updateSemester(Long id, Semester semesterDetails);
    void deleteSemester(Long id);
    boolean semesterExists(String name);
}
