package com.college.service.impl;

import com.college.model.Enrollment;
import com.college.model.Student;
import com.college.model.Course;
import com.college.model.Semester;
import com.college.repository.EnrollmentRepository;
import com.college.repository.StudentRepository;
import com.college.repository.CourseRepository;
import com.college.repository.SemesterRepository;
import com.college.dto.EnrollmentDTO;
import com.college.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Override
    @Transactional
    public Enrollment save(Enrollment enrollment) {
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepository.findById(id);
    }

    @Override
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        enrollmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Enrollment enrollStudent(Long studentId, Long courseId, Long semesterId) {
        // 验证学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        
        // 验证课程是否存在
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        // 检查是否已经选过这门课程
        if (enrollmentRepository.findByStudentIdAndCourseIdAndSemesterId(studentId, courseId, semesterId).isPresent()) {
            throw new RuntimeException("Student has already enrolled in this course for the given semester");
        }
        
        // 创建新的选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setSemesterId(semesterId);
        enrollment.setStatus(Enrollment.Status.NORMAL);
        
        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public void withdrawStudent(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        
        // 更新状态为已退课
        enrollment.setStatus(Enrollment.Status.WITHDRAWN);
        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public List<Enrollment> batchSave(List<Enrollment> enrollments) {
        return enrollmentRepository.saveAll(enrollments);
    }
    
    @Override
    @Transactional
    public List<Enrollment> batchDelete(List<Long> ids) {
        // 先获取要删除的记录，然后删除它们
        List<Enrollment> enrollments = enrollmentRepository.findAllById(ids);
        enrollmentRepository.deleteAllById(ids);
        return enrollments;
    }
    
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 实现接口中定义的方法，直接调用repository
        enrollmentRepository.deleteAllById(ids);
    }
    
    @Override
    public boolean isCourseAvailable(Long courseId) {
        // 检查课程是否存在且可选课
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            // 这里可以添加更多的课程可用性检查逻辑
            // 例如检查课程是否已满、是否在选课时间段内等
            return true; // 简化实现，假设只要课程存在就是可用的
        }
        return false;
    }
    
    @Override
    public List<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId) {
        // 使用已有的repository方法实现查询
        return enrollmentRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getCourse().getId().equals(courseId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Enrollment> findBySemester(String semester) {
        // 根据学期字符串查找选课记录
        // 这里简化实现，实际应该在Repository中添加对应方法
        return enrollmentRepository.findAll().stream()
                .filter(e -> String.valueOf(e.getSemesterId()).equals(semester))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByStudentIdAndCourseId(Long studentId, Long courseId) {
        // 检查是否存在特定学生和课程的选课记录
        return !findByStudentIdAndCourseId(studentId, courseId).isEmpty();
    }
    
    @Override
    public Long getCourseIdByEnrollmentId(Long enrollmentId) {
        // 根据选课ID获取课程ID
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        return enrollment.getCourse().getId();
    }

    @Override
    public List<Enrollment> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> findByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    public List<Enrollment> findBySemesterId(Long semesterId) {
        return enrollmentRepository.findBySemesterId(semesterId);
    }

    @Override
    public List<Enrollment> findByStudentIdAndSemesterId(Long studentId, Long semesterId) {
        return enrollmentRepository.findByStudentIdAndSemesterId(studentId, semesterId);
    }

    @Override
    public List<Enrollment> findByCourseIdAndSemesterId(Long courseId, Long semesterId) {
        return enrollmentRepository.findByCourseIdAndSemesterId(courseId, semesterId);
    }

    @Override
    public List<Enrollment> findByStatus(Enrollment.Status status) {
        return enrollmentRepository.findByStatus(status);
    }

    @Override
    public List<Enrollment> searchEnrollments(Long studentId, Long courseId, Long semesterId, Enrollment.Status status) {
        // 根据提供的条件进行筛选查询
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        
        if (studentId != null) {
            enrollments = enrollments.stream()
                    .filter(e -> e.getStudent() != null && e.getStudent().getId().equals(studentId))
                    .collect(Collectors.toList());
        }
        
        if (courseId != null) {
            enrollments = enrollments.stream()
                    .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(courseId))
                    .collect(Collectors.toList());
        }
        
        if (semesterId != null) {
            enrollments = enrollments.stream()
                    .filter(e -> e.getSemesterId().equals(semesterId))
                    .collect(Collectors.toList());
        }
        
        if (status != null) {
            enrollments = enrollments.stream()
                    .filter(e -> e.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        
        return enrollments;
    }

    @Override
    public List<Enrollment> findBySemesterIdAndCourseId(Long semesterId, Long courseId) {
        return enrollmentRepository.findBySemesterIdAndCourseId(semesterId, courseId);
    }

    @Override
    public List<Enrollment> findBySemesterIdAndStudentId(Long semesterId, Long studentId) {
        return enrollmentRepository.findBySemesterIdAndStudentId(semesterId, studentId);
    }

    @Override
    public List<Enrollment> findByCourseIdAndStudentId(Long courseId, Long studentId) {
        return enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);
    }

    @Override
    public List<Enrollment> findBySemesterIdAndCourseIdAndStudentId(Long semesterId, Long courseId, Long studentId) {
        return enrollmentRepository.findBySemesterIdAndCourseIdAndStudentId(semesterId, courseId, studentId);
    }

    @Override
    public boolean existsByStudentIdAndCourseIdAndSemesterId(Long studentId, Long courseId, Long semesterId) {
        return enrollmentRepository.findByStudentIdAndCourseIdAndSemesterId(studentId, courseId, semesterId).isPresent();
    }

    @Override
    @Transactional
    public Enrollment updateScore(Long enrollmentId, Double score) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        
        enrollment.setScore(score);
        // 如果成绩设置了，更新状态为已完成
        if (score != null) {
            enrollment.setStatus(Enrollment.Status.COMPLETED);
        }
        
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public EnrollmentDTO convertToDTO(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        
        return EnrollmentDTO.fromEnrollment(enrollment, semesterRepository);
    }

    @Override
    public List<EnrollmentDTO> convertToDTOList(List<Enrollment> enrollments) {
        if (enrollments == null || enrollments.isEmpty()) {
            return new ArrayList<>();
        }
        
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Enrollment convertToEntity(EnrollmentDTO enrollmentDTO) {
        if (enrollmentDTO == null) {
            return null;
        }
        
        Enrollment enrollment = new Enrollment();
        enrollment.setId(enrollmentDTO.getId());
        enrollment.setSemesterId(enrollmentDTO.getSemesterId());
        enrollment.setEnrollDate(enrollmentDTO.getEnrollDate());
        enrollment.setStatus(enrollmentDTO.getStatus());
        enrollment.setScore(enrollmentDTO.getScore());
        enrollment.setRemark(enrollmentDTO.getRemark());
        
        // 设置学生关联
        if (enrollmentDTO.getStudentId() != null) {
            studentRepository.findById(enrollmentDTO.getStudentId())
                    .ifPresent(enrollment::setStudent);
        }
        
        // 设置课程关联
        if (enrollmentDTO.getCourseId() != null) {
            courseRepository.findById(enrollmentDTO.getCourseId())
                    .ifPresent(enrollment::setCourse);
        }
        
        return enrollment;
    }

    @Override
    public long countAllEnrollments() {
        return enrollmentRepository.count();
    }

    @Override
    public long countEnrollmentsInPreviousMonth() {
        java.time.YearMonth previousMonth = java.time.YearMonth.now().minusMonths(1);
        java.time.LocalDateTime start = previousMonth.atDay(1).atStartOfDay();
        java.time.LocalDateTime end = previousMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return enrollmentRepository.countByEnrollDateBetween(start, end);
    }

    @Override
    public long countEnrollmentsInCurrentMonth() {
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        java.time.LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        java.time.LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return enrollmentRepository.countByEnrollDateBetween(start, end);
    }
    
    @Override
    public int countEnrollmentsByStudentId(Long studentId) {
        if (studentId == null) {
            return 0;
        }
        return enrollmentRepository.findByStudentId(studentId).size();
    }
    
    @Override
    public int countEnrollmentsByCourseId(Long courseId) {
        if (courseId == null) {
            return 0;
        }
        return enrollmentRepository.countByCourseId(courseId).intValue();
    }
}