package com.college.service.impl;

import com.college.model.Department;
import com.college.model.Teacher;
import com.college.dto.TeacherDTO;
import com.college.repository.DepartmentRepository;
import com.college.repository.TeacherRepository;
import com.college.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Override
    @Transactional
    public Teacher save(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @Override
    public Optional<Teacher> findById(Long id) {
        return teacherRepository.findById(id);
    }

    @Override
    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }

    @Override
    public List<Teacher> findByDepartmentId(Long departmentId) {
        return teacherRepository.findByDepartmentId(departmentId);
    }

    @Override
    public TeacherDTO convertToDTO(Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setUsername(teacher.getUsername());
        dto.setName(teacher.getName());
        dto.setEmail(teacher.getEmail());
        dto.setPhone(teacher.getPhone());
        dto.setTeacherId(teacher.getTeacherId());
        dto.setDepartment(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null);
        dto.setDepartmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null);
        dto.setTitle(teacher.getTitle());
        dto.setEducation(teacher.getEducation());
        dto.setEnabled(teacher.isEnabled());
        dto.setGender(teacher.getGender());
        dto.setBirthDate(teacher.getBirthDate());
        dto.setJoinDate(teacher.getJoinDate());
        dto.setGraduateSchool(teacher.getGraduateSchool());
        dto.setMajor(teacher.getMajor());
        dto.setBiography(teacher.getBiography());
        dto.setOffice(teacher.getOffice());
        dto.setOfficePhone(teacher.getOfficePhone());
        dto.setMobilePhone(teacher.getMobilePhone());
        dto.setAddress(teacher.getAddress());

        return dto;
    }

    @Override
    public Teacher convertToEntity(TeacherDTO teacherDTO) {
        if (teacherDTO == null) {
            return null;
        }

        Teacher teacher = new Teacher();
        teacher.setId(teacherDTO.getId());
        teacher.setUsername(teacherDTO.getUsername());
        teacher.setName(teacherDTO.getName());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setPhone(teacherDTO.getPhone());
        teacher.setTeacherId(teacherDTO.getTeacherId());
        
        // 设置部门（通过Repository查找Department对象）
        if (teacherDTO.getDepartmentId() != null) {
            Optional<Department> departmentOpt = departmentRepository.findById(teacherDTO.getDepartmentId());
            departmentOpt.ifPresent(teacher::setDepartment);
        }
        
        teacher.setTitle(teacherDTO.getTitle());
        teacher.setEducation(teacherDTO.getEducation());
        teacher.setEnabled(teacherDTO.getEnabled() != null ? teacherDTO.getEnabled() : true);
        teacher.setGender(teacherDTO.getGender());
        teacher.setBirthDate(teacherDTO.getBirthDate());
        teacher.setJoinDate(teacherDTO.getJoinDate());
        teacher.setGraduateSchool(teacherDTO.getGraduateSchool());
        teacher.setMajor(teacherDTO.getMajor());
        teacher.setBiography(teacherDTO.getBiography());
        teacher.setOffice(teacherDTO.getOffice());
        teacher.setOfficePhone(teacherDTO.getOfficePhone());
        teacher.setMobilePhone(teacherDTO.getMobilePhone());
        teacher.setAddress(teacherDTO.getAddress());

        return teacher;
    }

    @Override
    public long countAllTeachers() {
        return teacherRepository.count();
    }

    @Override
    public long countTeachersInPreviousMonth() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDateTime start = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime end = previousMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return teacherRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public long countTeachersInCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return teacherRepository.countByCreatedAtBetween(start, end);
    }
    
    @Override
    public long countByDepartmentId(Long departmentId) {
        return teacherRepository.countByDepartmentId(departmentId);
    }
}