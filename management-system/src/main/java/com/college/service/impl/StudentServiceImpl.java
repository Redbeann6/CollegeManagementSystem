package com.college.service.impl;

import com.college.dto.StudentDTO;
import com.college.model.*;
import com.college.repository.*;
import com.college.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private com.college.repository.ClazzRepository clazzRepository;

    @Autowired
    private com.college.repository.EnrollmentRepository enrollmentRepository;

    @Override
    public List<StudentDTO> getAllStudents() {
        List<User> users = userRepository.findByRole(User.Role.STUDENT);
        return users.stream()
                .map(user -> {
                    if (user instanceof Student) {
                        return StudentDTO.fromStudent((Student) user);
                    }
                    return null;
                })
                .filter(dto -> dto != null) // 过滤掉可能的null值
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && user instanceof Student && user.getRole() == User.Role.STUDENT) {
            return StudentDTO.fromStudent((Student) user);
        }
        return null;
    }

    @Override
    @Transactional
    public StudentDTO createStudent(StudentDTO studentDTO) {
        // 验证必需字段
        if (studentDTO.getStudentId() == null || studentDTO.getStudentId().trim().isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }
        
        if (studentDTO.getName() == null || studentDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        
        // 检查学号是否已存在
        Optional<User> existingUserOpt = userRepository.findByUsername(studentDTO.getStudentId());
        if (existingUserOpt.isPresent()) {
            throw new RuntimeException("学号已存在");
        }
        
        // 检查身份证号是否已存在（如果提供了身份证号）
        if (studentDTO.getIdCard() != null && !studentDTO.getIdCard().trim().isEmpty()) {
            // 检查身份证号是否重复
            List<Student> existingStudents = studentRepository.findByidCard(studentDTO.getIdCard());
            if (!existingStudents.isEmpty()) {
                throw new RuntimeException("身份证号已存在");
            }
        }
        
        // 直接创建学生对象（继承自User）
        Student student = new Student();
        student.setName(studentDTO.getName());
        
        // 确保studentId不为null后再拼接邮箱
        String studentId = studentDTO.getStudentId();
        if (studentId == null) {
            studentId = "temp_" + System.currentTimeMillis(); // 生成临时ID
        }
        
        student.setEmail(studentDTO.getEmail() != null && !studentDTO.getEmail().trim().isEmpty() 
            ? studentDTO.getEmail() 
            : "student@" + studentId + ".edu"); // 提供默认邮箱
        
        student.setPassword("$2a$10$9S.vUvGbWbWHY3fRDqdj1u9qY9FXknsEKyORUrcVNlwGg6129/v1q"); // 默认密码加密值
        student.setRole(User.Role.STUDENT);
        student.setUsername(studentId);
        student.setPhone(studentDTO.getPhone());
        student.setEnabled(true);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        
        // 设置学生特定的属性
        student.setStudentId(studentId);
        student.setClassName(studentDTO.getClassName());
        student.setMajor(studentDTO.getMajor());
        student.setBirthDate(studentDTO.getBirthDate());
        student.setGender(studentDTO.getGender());
        
        // 设置新增字段
        student.setAddress(studentDTO.getAddress());
        student.setIdCard(studentDTO.getIdCard());
        student.setPoliticalStatus(studentDTO.getPoliticalStatus());
        
        // 如果入学日期为null但入学年份不为null，则使用入学年份创建入学日期
        if (studentDTO.getEnrollmentDate() != null) {
            student.setEnrollmentDate(studentDTO.getEnrollmentDate());
        } else if (studentDTO.getAdmissionYear() != null) {
            // 使用入学年份的1月1日作为入学日期
            student.setEnrollmentDate(LocalDate.of(studentDTO.getAdmissionYear(), 1, 1));
        }

        // 设置部门关联
        if (studentDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(studentDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            student.setDepartment(department);
        }
        
        // 设置专业关联
        if (studentDTO.getMajorId() != null) {
            Major major = majorRepository.findById(studentDTO.getMajorId())
                    .orElseThrow(() -> new RuntimeException("专业不存在"));
            student.setMajorRef(major);
        }

        // 设置班级关联
        if (studentDTO.getClassId() != null) {
            com.college.model.Clazz clazz = clazzRepository.findById(studentDTO.getClassId())
                    .orElseThrow(() -> new RuntimeException("班级不存在"));
            student.setClazz(clazz);
        }

        Student savedStudent = studentRepository.save(student);

        return StudentDTO.fromStudent(savedStudent);
    }
    
    @Override
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        // 查找现有的学生记录
        Student existingStudent = studentRepository.findById(id).orElse(null);
        if (existingStudent == null || existingStudent.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("学生不存在");
        }
        
        // 检查学号是否被其他学生使用（除了当前学生本身）
        Optional<User> existingUserWithSameId = userRepository.findByUsername(studentDTO.getStudentId());
        if (existingUserWithSameId.isPresent() && !existingUserWithSameId.get().getId().equals(id)) {
            throw new RuntimeException("学号已被其他学生使用");
        }
        
        // 检查身份证号是否被其他学生使用（如果提供了身份证号）
        if (studentDTO.getIdCard() != null && !studentDTO.getIdCard().trim().isEmpty()) {
            List<Student> existingStudents = studentRepository.findByidCard(studentDTO.getIdCard());
            if (existingStudents.size() > 1 || 
                (existingStudents.size() == 1 && !existingStudents.get(0).getId().equals(id))) {
                throw new RuntimeException("身份证号已被其他学生使用");
            }
        }
        
        // 确保studentId不为null后再拼接邮箱
        String studentId = studentDTO.getStudentId();
        if (studentId == null) {
            studentId = "temp_" + System.currentTimeMillis(); // 生成临时ID
        }
        
        // 更新学生信息
        existingStudent.setName(studentDTO.getName());
        existingStudent.setEmail(studentDTO.getEmail() != null && !studentDTO.getEmail().trim().isEmpty() 
            ? studentDTO.getEmail() 
            : "student@" + studentId + ".edu"); // 提供默认邮箱
        existingStudent.setPhone(studentDTO.getPhone());
        existingStudent.setUpdatedAt(LocalDateTime.now());
        
        // 更新学生特定的属性
        existingStudent.setStudentId(studentId);
        existingStudent.setClassName(studentDTO.getClassName());
        existingStudent.setMajor(studentDTO.getMajor());
        existingStudent.setBirthDate(studentDTO.getBirthDate());
        existingStudent.setGender(studentDTO.getGender());
        
        // 更新新增字段
        existingStudent.setAddress(studentDTO.getAddress());
        existingStudent.setIdCard(studentDTO.getIdCard());
        existingStudent.setPoliticalStatus(studentDTO.getPoliticalStatus());
        
        // 如果入学年份不为null，则使用入学年份创建入学日期（优先级高于入学日期）
        if (studentDTO.getAdmissionYear() != null) {
            // 使用入学年份的1月1日作为入学日期
            existingStudent.setEnrollmentDate(LocalDate.of(studentDTO.getAdmissionYear(), 1, 1));
        } else if (studentDTO.getEnrollmentDate() != null) {
            existingStudent.setEnrollmentDate(studentDTO.getEnrollmentDate());
        }
        
        // 设置部门关联
        if (studentDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(studentDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("部门不存在"));
            existingStudent.setDepartment(department);
        }
        
        // 设置专业关联
        if (studentDTO.getMajorId() != null) {
            Major major = majorRepository.findById(studentDTO.getMajorId())
                    .orElseThrow(() -> new RuntimeException("专业不存在"));
            existingStudent.setMajorRef(major);
        }

        // 设置班级关联
        if (studentDTO.getClassId() != null) {
            com.college.model.Clazz clazz = clazzRepository.findById(studentDTO.getClassId())
                    .orElseThrow(() -> new RuntimeException("班级不存在"));
            existingStudent.setClazz(clazz);
        }
        
        Student updatedStudent = studentRepository.save(existingStudent);
        
        return StudentDTO.fromStudent(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // 检查学生是否存在
        Student student = studentRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("学生不存在，ID: " + id));
        
        // 删除相关的选课记录
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(id);
        enrollmentRepository.deleteAll(enrollments);
        
        // 删除学生记录（这将自动删除相关的考试成绩和请假申请，因为配置了cascade和orphanRemoval）
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        // 批量删除学生及其相关记录
        for (Long id : ids) {
            // 删除相关的选课记录
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(id);
            enrollmentRepository.deleteAll(enrollments);
            
            // 删除学生记录
            userRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public long countAllStudents() {
        return userRepository.countByRole(User.Role.STUDENT);
    }
    
    @Override
    @Transactional
    public long countStudentsInPreviousMonth() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDateTime start = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime end = previousMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return userRepository.countByRoleAndCreatedAtBetween(User.Role.STUDENT, start, end);
    }
    
    @Override
    @Transactional
    public long countStudentsInCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return userRepository.countByRoleAndCreatedAtBetween(User.Role.STUDENT, start, end);
    }
    
    @Override
    public long countByDepartmentId(Long departmentId) {
        return studentRepository.countByDepartmentId(departmentId);
    }
    
    @Override
    @Transactional
    public StudentDTO updateStudentStatus(Long id, boolean enabled) {
        // 查找现有的学生记录
        Student existingStudent = studentRepository.findById(id).orElse(null);
        if (existingStudent == null || existingStudent.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("学生不存在");
        }
        
        // 更新学生状态
        existingStudent.setEnabled(enabled);
        existingStudent.setUpdatedAt(LocalDateTime.now());
        
        Student updatedStudent = studentRepository.save(existingStudent);
        
        return StudentDTO.fromStudent(updatedStudent);
    }
}