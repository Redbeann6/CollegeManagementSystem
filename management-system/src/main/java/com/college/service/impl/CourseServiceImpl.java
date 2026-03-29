package com.college.service.impl;

import com.college.model.Course;
import com.college.model.Teacher;
import com.college.model.Student;
import com.college.model.Department;
import com.college.model.Semester;
import com.college.repository.CourseRepository;
import com.college.repository.TeacherRepository;
import com.college.repository.StudentRepository;
import com.college.repository.DepartmentRepository;
import com.college.repository.SemesterRepository;
import com.college.dto.CourseDTO;
import com.college.service.CourseService;
import com.college.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("courseService")
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Override
    @Transactional
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> findAll() {
        // 使用JOIN FETCH来避免懒加载问题
        return courseRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public List<Course> findByTeacherId(Long teacherId) {
        // 使用预加载学生信息的查询方法
        return courseRepository.findByTeacherWithStudents(teacherId);
    }

    @Override
    public List<Course> findByStudentId(Long studentId) {
        // 先查询学生对象
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            return courseRepository.findByStudentsContaining(studentOpt.get());
        }
        return new ArrayList<>(); // 返回空列表
    }

    @Override
    public List<Course> findByDayOfWeek(String dayOfWeek) {
        return courseRepository.findByDayOfWeek(dayOfWeek);
    }

    @Override
    public boolean existsByCourseCode(String courseCode) {
        return courseRepository.existsByCourseCode(courseCode);
    }

    @Override
    public List<Object> getCourseStudents(Long courseId) {
        // 使用EnrollmentService获取选了这门课的学生
        List<Object> students = new ArrayList<>();
        
        // 获取课程的选课记录
        List<com.college.model.Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        
        for (com.college.model.Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            if (student != null) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", student.getId());
                studentInfo.put("studentId", student.getStudentId());
                studentInfo.put("name", student.getName());
                studentInfo.put("email", student.getEmail());
                studentInfo.put("phone", student.getPhone());
                studentInfo.put("className", student.getClassName());
                studentInfo.put("major", student.getMajor());
                students.add(studentInfo);
            }
        }
        
        return students;
    }

    @Override
    public long countAllCourses() {
        return courseRepository.count();
    }

    @Override
    public long countCoursesInPreviousMonth() {
        java.time.YearMonth previousMonth = java.time.YearMonth.now().minusMonths(1);
        java.time.LocalDateTime start = previousMonth.atDay(1).atStartOfDay();
        java.time.LocalDateTime end = previousMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return courseRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public long countCoursesInCurrentMonth() {
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        java.time.LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        java.time.LocalDateTime end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return courseRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public CourseDTO convertToDTO(Course course) {
        if (course == null) {
            return null;
        }

        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setName(course.getName());
        dto.setCredits(course.getCredits());
        dto.setClassroom(course.getClassroom());
        dto.setDayOfWeek(course.getDayOfWeek());
        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        dto.setTotalHours(course.getTotalHours());
        dto.setHours(course.getTotalHours());
        dto.setCourseType(course.getCourseType());
        dto.setMaxStudents(course.getMaxStudents());
        dto.setStatus(course.getStatus() != null ? course.getStatus() : true); // 默认为true

        // 设置教师信息
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            dto.setTeacherName(course.getTeacher().getName());
        }

        // 设置系部信息
        if (course.getDepartment() != null) {
            dto.setDepartmentId(course.getDepartment().getId());
            dto.setDepartmentName(course.getDepartment().getName());
        }

        // 设置学期信息
        if (course.getSemester() != null) {
            dto.setSemesterName(course.getSemester().getName());
        }

        // 设置学生数量 - 使用安全的访问方式
        try {
            if (course.getStudents() != null) {
                dto.setStudentCount(course.getStudents().size());
            } else {
                dto.setStudentCount(0);
            }
        } catch (Exception e) {
            // 如果出现懒加载或其他异常，设置为0
            dto.setStudentCount(0);
        }
        
        // 设置上课时间安排（schedule）
        StringBuilder schedule = new StringBuilder();
        if (course.getDayOfWeek() != null) {
            schedule.append(course.getDayOfWeek());
        }
        if (course.getStartTime() != null && course.getEndTime() != null) {
            if (schedule.length() > 0) {
                schedule.append(" ");
            }
            schedule.append(course.getStartTime().toString()).append("-").append(course.getEndTime().toString());
        }
        dto.setSchedule(schedule.toString());
        
        // 设置开始和结束日期
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());

        // 设置班级名称 - 通过选课记录获取班级信息
        try {
            List<com.college.model.Enrollment> enrollments = enrollmentService.findByCourseId(course.getId());
            if (enrollments != null && !enrollments.isEmpty()) {
                // 尝试从选课记录中获取班级名称
                for (com.college.model.Enrollment enrollment : enrollments) {
                    Student student = enrollment.getStudent();
                    if (student != null) {
                        if (student.getClassName() != null && !student.getClassName().trim().isEmpty()) {
                            dto.setClassName(student.getClassName());
                            break;
                        }
                        // 如果学生有直接的班级对象，也可以使用
                        if (student.getClazz() != null && student.getClazz().getName() != null) {
                            dto.setClassName(student.getClazz().getName());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果获取班级名称失败，不设置或使用默认值
            // 避免因查询失败影响其他功能
        }
        
        // 设置已选人数
        try {
            int enrollmentCount = enrollmentService.countEnrollmentsByCourseId(course.getId());
            dto.setEnrollmentCount(enrollmentCount);
            // 同时设置studentCount用于前端显示
            dto.setStudentCount(enrollmentCount);
        } catch (Exception e) {
            // 如果获取人数失败，设置为0
            dto.setEnrollmentCount(0);
            dto.setStudentCount(0);
        }

        return dto;
    }

    @Override
    public Course convertToEntity(CourseDTO courseDTO) {
        if (courseDTO == null) {
            return null;
        }

        Course course = new Course();
        course.setId(courseDTO.getId());
        course.setCourseCode(courseDTO.getCourseCode());
        course.setName(courseDTO.getName());
        course.setCredits(courseDTO.getCredits());
        course.setTotalHours(courseDTO.getTotalHours());
        course.setClassroom(courseDTO.getClassroom());
        course.setDayOfWeek(courseDTO.getDayOfWeek());
        course.setStartTime(courseDTO.getStartTime());
        course.setEndTime(courseDTO.getEndTime());
        course.setCourseType(courseDTO.getCourseType());
        course.setMaxStudents(courseDTO.getMaxStudents());
        course.setStatus(courseDTO.getStatus());

        // 设置教师
        if (courseDTO.getTeacherId() != null) {
            Optional<Teacher> teacherOpt = teacherRepository.findById(courseDTO.getTeacherId());
            teacherOpt.ifPresent(course::setTeacher);
        }

        // 设置系部
        if (courseDTO.getDepartmentId() != null) {
            Optional<Department> departmentOpt = departmentRepository.findById(courseDTO.getDepartmentId());
            departmentOpt.ifPresent(course::setDepartment);
        }

        // 设置学期
        if (courseDTO.getSemesterId() != null) {
            Optional<Semester> semesterOpt = semesterRepository.findById(courseDTO.getSemesterId());
            semesterOpt.ifPresent(course::setSemester);
        }

        return course;
    }

    @Override
    public List<Course> findCoursesByFilters(Long departmentId, Long teacherId, Long semesterId, String keyword) {
        return courseRepository.findCoursesByFilters(departmentId, teacherId, semesterId, keyword);
    }
    
    @Override
    public boolean isCourseTaughtByCurrentUser(Long courseId, Object principal) {
        try {
            // 从principal中获取当前用户信息
            if (principal instanceof com.college.model.User) {
                com.college.model.User currentUser = (com.college.model.User) principal;
                
                // 获取课程信息
                Optional<Course> courseOpt = findById(courseId);
                if (!courseOpt.isPresent()) {
                    return false; // 课程不存在
                }
                
                Course course = courseOpt.get();
                
                // 检查当前用户是否为教师且教授此课程
                if (currentUser instanceof com.college.model.Teacher) {
                    com.college.model.Teacher currentTeacher = (com.college.model.Teacher) currentUser;
                    if (course.getTeacher() != null) {
                        return course.getTeacher().getId().equals(currentTeacher.getId());
                    }
                }
                
                return false;
            }
            
            return false;
        } catch (Exception e) {
            // 发生异常时，为安全起见，返回false
            return false;
        }
    }
}