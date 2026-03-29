package com.college.controller;

import com.college.dto.Response;
import com.college.dto.CourseDTO;
import com.college.dto.StudentDTO;
import com.college.dto.ExamDTO;
import com.college.dto.ExamResultDTO;
import com.college.model.Course;
import com.college.model.Student;
import com.college.model.Teacher;
import com.college.model.User;
import com.college.model.Enrollment;
import com.college.model.Exam;
import com.college.model.ExamResult;
import com.college.model.Department;
import com.college.model.Semester;
import com.college.service.CourseService;
import com.college.service.StudentService;
import com.college.service.TeacherService;
import com.college.service.UserService;
import com.college.service.EnrollmentService;
import com.college.service.ExamService;
import com.college.service.DepartmentService;
import com.college.service.SemesterService;
import com.college.repository.ExamResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher/data")
@CrossOrigin(origins = "*")
public class TeacherAPIController {

    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private com.college.repository.StudentRepository studentRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private ExamResultRepository examResultRepository;
    
    @Autowired
    private com.college.service.ExamResultService examResultService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private SemesterService semesterService;

    // 获取当前登录教师的课程列表
    @GetMapping("/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<CourseDTO>>> getTeacherCourses(Authentication authentication) {
        try {
            // 从认证信息中获取当前用户的用户名
            String username = authentication.getName();
            
            // 通过用户名获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 获取该教师教授的课程
            List<Course> courses = courseService.findByTeacherId(teacher.getId());
            
            // 转换为DTO并填充完整信息
            List<CourseDTO> courseDTOs = courses.stream()
                    .map(course -> {
                        CourseDTO dto = courseService.convertToDTO(course);
                        // 使用EnrollmentService获取实际选课人数
                        List<Enrollment> enrollments = enrollmentService.findByCourseId(course.getId());
                        // 设置学生人数
                        dto.setStudentsCount(enrollments.size());
                        dto.setEnrollmentCount(enrollments.size());
                        // 确保学分字段被正确设置
                        dto.setCredit(dto.getCredits());
                        // 确保学期字段被正确设置
                        dto.setSemester(dto.getSemesterName());
                        // 设置开始和结束日期
                        dto.setStartDate(course.getStartDate());
                        dto.setEndDate(course.getEndDate());
                        return dto;
                    })
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(Response.success("获取教学课程成功", courseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取教学课程失败: " + e.getMessage(), null));
        }
    }

    // 获取当前教师所教课程的学生列表
    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<StudentDTO>>> getTeacherStudents(Authentication authentication) {
        try {
            // 从认证信息中获取当前用户的用户名
            String username = authentication.getName();
            
            // 通过用户名获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 获取该教师教授的课程
            List<Course> courses = courseService.findByTeacherId(teacher.getId());
            
            // 获取这些课程的学生
            // 使用Set来避免重复的学生
            java.util.Set<Student> uniqueStudents = new java.util.HashSet<>();
            for (Course course : courses) {
                // 通过EnrollmentService获取课程的学生
                List<Enrollment> enrollments = enrollmentService.findByCourseId(course.getId());
                for (Enrollment enrollment : enrollments) {
                    if (enrollment.getStudent() != null) {
                        uniqueStudents.add(enrollment.getStudent());
                    }
                }
            }
            
            List<StudentDTO> studentDTOs = uniqueStudents.stream()
                    .map(student -> {
                        StudentDTO dto = StudentDTO.fromStudent(student);
                        // 为所有课程设置一个总体状态，基于学生在教师任课中的表现
                        List<Enrollment> enrollments = enrollmentService.findByStudentId(student.getId());
                        // 检查是否有任何一门课程的状态需要特别关注
                        boolean hasCompletedCourse = false;
                        boolean hasInProgressCourse = false;
                        boolean hasFailedCourse = false;
                        
                        for (Enrollment enrollment : enrollments) {
                            if (enrollment.getCourse() != null && enrollment.getCourse().getTeacher() != null) {
                                // 确保这是当前教师的课程
                                if (enrollment.getCourse().getTeacher().getId().equals(teacher.getId())) {
                                    if (Enrollment.Status.COMPLETED.equals(enrollment.getStatus())) {
                                        hasCompletedCourse = true;
                                    } else if (Enrollment.Status.IN_PROGRESS.equals(enrollment.getStatus())) {
                                        hasInProgressCourse = true;
                                    } else if (Enrollment.Status.FAILED.equals(enrollment.getStatus())) {
                                        hasFailedCourse = true;
                                    }
                                }
                            }
                        }
                        
                        // 优先级：FAILED > IN_PROGRESS > COMPLETED > NORMAL
                        if (hasFailedCourse) {
                            dto.setCourseStatus("warning"); // 有不及格课程则警告
                        } else if (hasInProgressCourse) {
                            dto.setCourseStatus("active"); // 有进行中课程
                        } else if (hasCompletedCourse) {
                            dto.setCourseStatus("graduated"); // 有已完成课程
                        } else {
                            dto.setCourseStatus("normal"); // 默认正常
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(Response.success("获取学生列表成功", studentDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取学生列表失败: " + e.getMessage(), null));
        }
    }
    
    // 根据课程ID获取该课程的学生列表
    @GetMapping("/classes/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<StudentDTO>>> getStudentsByCourse(@PathVariable Long courseId, Authentication authentication) {
        try {
            // 验证教师是否有权访问该课程
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 检查课程是否属于当前教师
            Optional<Course> courseOpt = courseService.findById(courseId);
            if (!courseOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("课程不存在", null));
            }
            
            Course course = courseOpt.get();
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权访问该课程", null));
            }
            
            // 获取课程的学生
            List<Student> students = new java.util.ArrayList<>();
            List<Enrollment> enrollments = enrollmentService.findByCourseId(course.getId());
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getStudent() != null) {
                    students.add(enrollment.getStudent());
                }
            }
            List<StudentDTO> studentDTOs = students.stream()
                    .map(student -> {
                        StudentDTO dto = StudentDTO.fromStudent(student);
                        // 根据课程和学生之间的选课关系设置课程状态
                        List<Enrollment> courseEnrollments = enrollmentService.findByStudentIdAndCourseId(student.getId(), courseId);
                        if (!courseEnrollments.isEmpty()) {
                            // 如果学生选了这门课，可以根据成绩等信息设置状态
                            Enrollment enrollment = courseEnrollments.get(0);
                            // 根据选课状态设置课程状态
                            if (Enrollment.Status.COMPLETED.equals(enrollment.getStatus())) {
                                dto.setCourseStatus("graduated"); // 已完成
                            } else if (Enrollment.Status.IN_PROGRESS.equals(enrollment.getStatus())) {
                                dto.setCourseStatus("active"); // 进行中
                            } else if (Enrollment.Status.FAILED.equals(enrollment.getStatus())) {
                                dto.setCourseStatus("warning"); // 不及格预警
                            } else {
                                dto.setCourseStatus("normal"); // 正常
                            }
                        } else {
                            dto.setCourseStatus("normal"); // 默认状态
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(Response.success("获取课程学生成功", studentDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取课程学生失败: " + e.getMessage(), null));
        }
    }
    
    // 根据学生ID获取该学生的成绩列表
    @GetMapping("/students/{studentId}/grades")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<Object>>> getGradesByStudent(@PathVariable Long studentId, Authentication authentication) {
        try {
            // 验证教师权限
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 查询该学生在当前教师所教授课程中的成绩
            // 使用自定义查询方法来获取与当前教师相关的成绩
            List<ExamResult> examResults = examResultRepository.findByStudentIdAndTeacherId(studentId, teacher.getId());
            
            // 转换为前端需要的对象格式
            List<Object> studentGrades = examResults.stream()
                    .map(result -> {
                        java.util.Map<String, Object> gradeObj = new java.util.HashMap<>();
                        gradeObj.put("id", result.getId());
                        gradeObj.put("studentId", result.getStudent() != null ? result.getStudent().getId() : null);
                        gradeObj.put("studentName", result.getStudent() != null ? result.getStudent().getName() : null);
                        gradeObj.put("courseId", result.getCourse() != null ? result.getCourse().getId() : null);
                        gradeObj.put("courseName", result.getCourse() != null ? result.getCourse().getName() : null);
                        gradeObj.put("examId", result.getExam() != null ? result.getExam().getId() : null);
                        gradeObj.put("examName", result.getExam() != null ? result.getExam().getTitle() : null);
                        gradeObj.put("score", result.getScore());
                        gradeObj.put("totalMarks", result.getExam() != null ? result.getExam().getTotalMarks() : null);
                        gradeObj.put("percentage", null); // ExamResult没有percentage字段
                        gradeObj.put("grade", result.getGrade());
                        gradeObj.put("comment", result.getFeedback());
                        gradeObj.put("status", result.getStatus());
                        gradeObj.put("createdAt", result.getCreatedAt());
                        gradeObj.put("updatedAt", result.getUpdatedAt());
                        return gradeObj;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(Response.success("获取学生成绩成功", studentGrades));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取学生成绩失败: " + e.getMessage(), null));
        }
    }
    
    // 健康检查端点
    @GetMapping("/health")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<String>> healthCheck(Authentication authentication) {
        return ResponseEntity.ok(Response.success("Teacher API is working", "OK"));
    }
    
    // 创建课程
    @PostMapping("/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<CourseDTO>> createCourse(@RequestBody CourseDTO courseDTO, Authentication authentication) {
        try {
            // 从认证信息中获取当前用户
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 创建课程实体
            Course course = new Course();
            course.setName(courseDTO.getName());
            course.setCourseCode(courseDTO.getCourseCode());
            course.setCredits(courseDTO.getCredit() != null ? courseDTO.getCredit() : courseDTO.getCredits());
            course.setTotalHours(courseDTO.getHours() != null ? courseDTO.getHours() : courseDTO.getTotalHours());
            course.setClassroom(courseDTO.getClassroom());
            course.setDayOfWeek(courseDTO.getDayOfWeek());
            course.setStartTime(courseDTO.getStartTime());
            course.setEndTime(courseDTO.getEndTime());
            course.setCourseType(courseDTO.getCourseType());
            course.setMaxStudents(courseDTO.getMaxStudents());
            course.setStatus(courseDTO.getStatus() != null ? courseDTO.getStatus() : true);
            course.setTeacher(teacher);
            
            // 设置部门和学期信息
            if (courseDTO.getDepartmentId() != null) {
                Optional<Department> deptOpt = departmentService.findById(courseDTO.getDepartmentId());
                if (deptOpt.isPresent()) {
                    course.setDepartment(deptOpt.get());
                }
            }
            if (courseDTO.getSemesterId() != null) {
                Optional<Semester> semOpt = semesterService.getSemesterById(courseDTO.getSemesterId());
                if (semOpt.isPresent()) {
                    course.setSemester(semOpt.get());
                }
            }
            
            // 设置开始和结束日期
            course.setStartDate(courseDTO.getStartDate());
            course.setEndDate(courseDTO.getEndDate());
            
            // 保存课程
            Course savedCourse = courseService.save(course);
            
            // 转换为DTO并返回
            CourseDTO resultDTO = courseService.convertToDTO(savedCourse);
            resultDTO.setStudentsCount(0); // 新课程没有学生
            resultDTO.setEnrollmentCount(0);
            
            return ResponseEntity.ok(Response.success("课程创建成功", resultDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("课程创建失败: " + e.getMessage(), null));
        }
    }
    
    // 更新课程
    @PutMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<CourseDTO>> updateCourse(@PathVariable Long courseId, @RequestBody CourseDTO courseDTO, Authentication authentication) {
        try {
            // 从认证信息中获取当前用户
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 检查课程是否存在且属于当前教师
            Optional<Course> courseOpt = courseService.findById(courseId);
            if (!courseOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("课程不存在", null));
            }
            
            Course course = courseOpt.get();
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权修改该课程", null));
            }
            
            // 更新课程信息
            course.setName(courseDTO.getName());
            course.setCourseCode(courseDTO.getCourseCode());
            course.setCredits(courseDTO.getCredit() != null ? courseDTO.getCredit() : courseDTO.getCredits());
            course.setTotalHours(courseDTO.getHours() != null ? courseDTO.getHours() : courseDTO.getTotalHours());
            course.setClassroom(courseDTO.getClassroom());
            course.setDayOfWeek(courseDTO.getDayOfWeek());
            course.setStartTime(courseDTO.getStartTime());
            course.setEndTime(courseDTO.getEndTime());
            course.setCourseType(courseDTO.getCourseType());
            course.setMaxStudents(courseDTO.getMaxStudents());
            course.setStatus(courseDTO.getStatus());
            
            // 更新部门和学期信息
            if (courseDTO.getDepartmentId() != null) {
                Optional<Department> deptOpt = departmentService.findById(courseDTO.getDepartmentId());
                if (deptOpt.isPresent()) {
                    course.setDepartment(deptOpt.get());
                }
            } else {
                course.setDepartment(null);
            }
            if (courseDTO.getSemesterId() != null) {
                Optional<Semester> semOpt = semesterService.getSemesterById(courseDTO.getSemesterId());
                if (semOpt.isPresent()) {
                    course.setSemester(semOpt.get());
                }
            } else {
                course.setSemester(null);
            }
            
            // 更新开始和结束日期
            course.setStartDate(courseDTO.getStartDate());
            course.setEndDate(courseDTO.getEndDate());
            
            // 保存课程
            Course updatedCourse = courseService.save(course);
            
            // 转换为DTO并返回
            CourseDTO resultDTO = courseService.convertToDTO(updatedCourse);
            // 获取实际选课人数
            List<Enrollment> enrollments = enrollmentService.findByCourseId(updatedCourse.getId());
            resultDTO.setStudentsCount(enrollments.size());
            resultDTO.setEnrollmentCount(enrollments.size());
            
            return ResponseEntity.ok(Response.success("课程更新成功", resultDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("课程更新失败: " + e.getMessage(), null));
        }
    }
    
    // 删除课程
    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<Void>> deleteCourse(@PathVariable Long courseId, Authentication authentication) {
        try {
            // 从认证信息中获取当前用户
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 检查课程是否存在且属于当前教师
            Optional<Course> courseOpt = courseService.findById(courseId);
            if (!courseOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("课程不存在", null));
            }
            
            Course course = courseOpt.get();
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权删除该课程", null));
            }
            
            // 删除课程
            courseService.deleteById(courseId);
            
            return ResponseEntity.ok(Response.success("课程删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("课程删除失败: " + e.getMessage(), null));
        }
    }
    
    // 根据课程ID获取单个课程详情
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<CourseDTO>> getCourseById(@PathVariable Long courseId, Authentication authentication) {
        try {
            // 从认证信息中获取当前用户
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 检查课程是否存在且属于当前教师
            Optional<Course> courseOpt = courseService.findById(courseId);
            if (!courseOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("课程不存在", null));
            }
            
            Course course = courseOpt.get();
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权访问该课程", null));
            }
            
            // 转换为DTO并返回
            CourseDTO courseDTO = courseService.convertToDTO(course);
            
            // 获取实际选课人数
            List<Enrollment> enrollments = enrollmentService.findByCourseId(course.getId());
            courseDTO.setStudentsCount(enrollments.size());
            courseDTO.setEnrollmentCount(enrollments.size());
            
            // 确保学分字段被正确设置
            courseDTO.setCredit(courseDTO.getCredits());
            // 确保学期字段被正确设置
            courseDTO.setSemester(courseDTO.getSemesterName());
            // 设置开始和结束日期
            courseDTO.setStartDate(course.getStartDate());
            courseDTO.setEndDate(course.getEndDate());
            
            return ResponseEntity.ok(Response.success("获取课程详情成功", courseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取课程详情失败: " + e.getMessage(), null));
        }
    }
    
    // 获取当前教师创建的考试列表
    @GetMapping("/exams")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamDTO>>> getTeacherExams(Authentication authentication) {
        try {
            // 从认证信息中获取当前用户的用户名
            String username = authentication.getName();
            
            // 通过用户名获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 获取该教师创建的考试
            List<Exam> exams = examService.findByCreatorId(teacher.getId());
            List<ExamDTO> examDTOs = exams.stream()
                    .map(examService::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Response.success("获取教师考试列表成功", examDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取教师考试列表失败: " + e.getMessage(), null));
        }
    }
    
    // 获取特定考试的成绩列表
    @GetMapping("/exams/{examId}/grades")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<Map<String, Object>>>> getExamGrades(@PathVariable Long examId, Authentication authentication) {
        try {
            // 从认证信息中获取当前用户的用户名
            String username = authentication.getName();
            
            // 通过用户名获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            // 获取教师信息
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 验证考试是否存在且属于当前教师
            Optional<Exam> examOpt = examService.findById(examId);
            if (!examOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("考试不存在", null));
            }
            
            Exam exam = examOpt.get();
            if (exam.getCreator() == null || !exam.getCreator().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权访问该考试的成绩", null));
            }
            
            // 获取考试成绩
            List<ExamResult> examResults = examResultRepository.findByExamId(examId);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> grades = examResults.stream()
                    .map(result -> {
                        Map<String, Object> gradeData = new HashMap<>();
                        
                        // 学生信息
                        if (result.getStudent() != null) {
                            gradeData.put("id", result.getId());
                            gradeData.put("studentId", result.getStudent().getId());
                            gradeData.put("studentName", result.getStudent().getName());
                            gradeData.put("studentCode", result.getStudent().getStudentId());
                            gradeData.put("className", result.getStudent().getClassName());
                        }
                        
                        // 成绩信息
                        gradeData.put("score", result.getScore());
                        gradeData.put("grade", result.getGrade());
                        gradeData.put("remark", result.getFeedback());
                        gradeData.put("examId", result.getExam().getId());
                        gradeData.put("examName", result.getExam().getTitle());
                        
                        // 课程信息
                        if (result.getCourse() != null) {
                            gradeData.put("courseName", result.getCourse().getName());
                        }
                        
                        return gradeData;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Response.success("获取考试成绩成功", grades));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取考试成绩失败: " + e.getMessage(), null));
        }
    }
    
    // 根据学生ID获取学生表现数据
    @GetMapping("/students/{studentId}/performance")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<Object>> getStudentPerformance(@PathVariable Long studentId, Authentication authentication) {
        try {
            // 验证教师权限
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 获取该学生在当前教师课程中的表现数据
            // 获取学生在当前教师所授课程中的成绩
            List<ExamResult> examResults = examResultRepository.findByStudentIdAndTeacherId(studentId, teacher.getId());
            
            // 计算平均分
            double avgScore = 0;
            if (!examResults.isEmpty()) {
                avgScore = examResults.stream()
                    .mapToDouble(ExamResult::getScore)
                    .average()
                    .orElse(0.0);
            }
            
            // 计算优秀课程数量 (成绩>=85)
            long excellentCount = examResults.stream()
                .filter(result -> result.getScore() >= 85)
                .count();
            
            // 计算不及格课程数量 (成绩<60)
            long failCount = examResults.stream()
                .filter(result -> result.getScore() < 60)
                .count();
            
            // 计算总学分
            int totalCredit = examResults.stream()
                .filter(result -> result.getScore() >= 60) // 只计算及格课程的学分
                .mapToInt(result -> {
                    if (result.getCourse() != null) {
                        return result.getCourse().getCredits();
                    }
                    return 0;
                })
                .sum();
            
            // 计算出勤率 (这里假设从Enrollment表获取，如果没有具体数据，使用默认值)
            // 由于EnrollmentService没有findByStudentIdAndTeacherId方法，我们通过examResults间接获取相关数据
            List<Enrollment> enrollments = new java.util.ArrayList<>();
            // 从examResults中获取学生在该教师课程中的选课记录
            for (ExamResult result : examResults) {
                if (result.getCourse() != null && result.getStudent() != null && 
                    result.getStudent().getId().equals(studentId)) {
                    // 查找对应的选课记录
                    List<Enrollment> studentCourseEnrollments = enrollmentService.findByStudentIdAndCourseId(studentId, result.getCourse().getId());
                    enrollments.addAll(studentCourseEnrollments);
                }
            }
            double attendanceRate = 100.0; // 默认100%，如果有具体出勤数据应替换此处
            
            // 计算排名（在当前教师的所有学生中）
            // 由于EnrollmentService没有findByTeacherId方法，我们需要通过其他方式获取
            // 首先获取该教师所有的课程
            List<Course> teacherCourses = courseService.findByTeacherId(teacher.getId());
            // 然后获取这些课程下的所有选课记录
            List<Enrollment> allEnrollments = new java.util.ArrayList<>();
            for (Course course : teacherCourses) {
                allEnrollments.addAll(enrollmentService.findByCourseId(course.getId()));
            }
            List<Long> studentIds = allEnrollments.stream()
                .filter(e -> e.getStudent() != null) // 确保学生不为空
                .map(e -> e.getStudent().getId())
                .distinct()
                .collect(Collectors.toList());
            
            // 获取所有这些学生的平均分来计算排名
            java.util.Map<Long, Double> studentAvgScores = new java.util.HashMap<>();
            for (Long sid : studentIds) {
                List<ExamResult> studentResults = examResultRepository.findByStudentIdAndTeacherId(sid, teacher.getId());
                if (!studentResults.isEmpty()) {
                    double studentAvg = studentResults.stream()
                        .mapToDouble(ExamResult::getScore)
                        .average()
                        .orElse(0.0);
                    studentAvgScores.put(sid, studentAvg);
                }
            }
            
            // 计算排名
            int ranking = 1;
            Double currentStudentAvg = studentAvgScores.get(studentId);
            if (currentStudentAvg != null) {
                for (Double avg : studentAvgScores.values()) {
                    if (avg > currentStudentAvg) {
                        ranking++;
                    }
                }
            }
            
            // 构建返回对象
            java.util.Map<String, Object> performanceData = new java.util.HashMap<>();
            performanceData.put("avgScore", avgScore);
            performanceData.put("ranking", ranking);
            performanceData.put("excellentCount", excellentCount);
            performanceData.put("failCount", failCount);
            performanceData.put("attendanceRate", attendanceRate);
            performanceData.put("credit", totalCredit);
            
            return ResponseEntity.ok(Response.success("获取学生表现数据成功", performanceData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取学生表现数据失败: " + e.getMessage(), null));
        }
    }
    
    // 根据考试ID获取参加考试的学生列表
    @GetMapping("/exams/{examId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<Object>>> getExamStudents(@PathVariable Long examId, Authentication authentication) {
        try {
            // 验证教师权限
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 验证考试是否属于当前教师
            Optional<Exam> examOpt = examService.findById(examId);
            if (!examOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("考试不存在", null));
            }
            
            Exam exam = examOpt.get();
            if (exam.getCreator() == null || !exam.getCreator().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Response.error("无权访问该考试", null));
            }
            
            // 获取考试的学生列表
            List<Map<String, Object>> examStudents = examService.getExamStudents(examId);
            
            return ResponseEntity.ok(Response.success("获取考试学生列表成功", (List<Object>) (List<?>) examStudents));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取考试学生列表失败: " + e.getMessage(), null));
        }
    }
    
    // 批量提交考试成绩
    @PostMapping("/exam-results/batch")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<ExamResultDTO>>> batchSubmitExamResults(@RequestBody List<Map<String, Object>> gradeData, Authentication authentication) {
        try {
            // 验证教师权限
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 准备要保存的成绩列表
            List<ExamResult> examResults = new ArrayList<>();
            
            for (Map<String, Object> gradeDatum : gradeData) {
                Long examId = ((Integer) gradeDatum.get("examId")).longValue();
                Long studentId = ((Integer) gradeDatum.get("studentId")).longValue();
                Double score = (Double) gradeDatum.get("score");
                String remark = (String) gradeDatum.get("remark");
                
                // 验证考试是否属于当前教师
                Optional<Exam> examOpt = examService.findById(examId);
                if (!examOpt.isPresent()) {
                    return ResponseEntity.badRequest().body(Response.error("考试不存在: " + examId, null));
                }
                
                Exam exam = examOpt.get();
                if (exam.getCreator() == null || !exam.getCreator().getId().equals(teacher.getId())) {
                    return ResponseEntity.status(403).body(Response.error("无权为该考试录入成绩", null));
                }
                
                // 验证学生是否存在
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) {
                    return ResponseEntity.badRequest().body(Response.error("学生不存在: " + studentId, null));
                }
                
                // 检查是否已存在该学生该考试的成绩记录
                Optional<ExamResult> existingResult = examResultRepository.findByStudentAndExam(student, exam);
                
                ExamResult examResult;
                if (existingResult.isPresent()) {
                    // 更新现有记录
                    examResult = existingResult.get();
                    examResult.setScore(score);
                    examResult.setFeedback(remark);
                    
                    // 根据分数和考试总分自动计算等级
                    if (exam.getTotalMarks() != null) {
                        double totalMarks = exam.getTotalMarks().doubleValue();
                        examResult.setGrade(calculateGradeFromScore(score, totalMarks));
                    }
                } else {
                    // 创建新记录
                    examResult = new ExamResult();
                    examResult.setScore(score);
                    examResult.setFeedback(remark);
                    examResult.setStudent(student);
                    examResult.setExam(exam);
                    examResult.setCourse(exam.getCourse());
                    
                    // 根据分数和考试总分自动计算等级
                    if (exam.getTotalMarks() != null) {
                        double totalMarks = exam.getTotalMarks().doubleValue();
                        examResult.setGrade(calculateGradeFromScore(score, totalMarks));
                    }
                }
                
                examResults.add(examResult);
            }
            
            // 批量保存成绩
            List<ExamResult> savedResults = examResultService.batchSave(examResults);
            
            // 转换为DTO返回
            List<ExamResultDTO> savedDTOs = savedResults.stream()
                .map(examResultService::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Response.success("批量提交成绩成功", savedDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("批量提交成绩失败: " + e.getMessage(), null));
        }
    }
    
    // 根据分数和总分计算等级
    private String calculateGradeFromScore(Double score, Double totalMarks) {
        if (score == null) {
            return null; // 如果分数为空，则等级也为null
        }
        
        double actualTotalMarks = (totalMarks == null || totalMarks <= 0) ? 100.0 : totalMarks;
        
        double percentage = (score / actualTotalMarks) * 100;
        
        if (percentage >= 90) {
            return "优秀";
        } else if (percentage >= 80) {
            return "良好";
        } else if (percentage >= 70) {
            return "中等";
        } else if (percentage >= 60) {
            return "及格";
        } else {
            return "不及格";
        }
    }
    
    // 根据学生ID获取学生课程数据
    @GetMapping("/students/{studentId}/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<Object>>> getStudentCourses(@PathVariable Long studentId, Authentication authentication) {
        try {
            // 验证教师权限
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }
            
            Optional<Teacher> teacherOpt = teacherService.findById(user.getId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Response.error("教师信息不存在", null));
            }
            
            Teacher teacher = teacherOpt.get();
            
            // 获取该学生在当前教师课程中的成绩数据
            List<ExamResult> examResults = examResultRepository.findByStudentIdAndTeacherId(studentId, teacher.getId());
            
            // 转换为课程数据格式
            List<Object> studentCourses = examResults.stream()
                .map(result -> {
                    java.util.Map<String, Object> courseData = new java.util.HashMap<>();
                    
                    // 课程信息
                    if (result.getCourse() != null) {
                        courseData.put("courseName", result.getCourse().getName());
                        courseData.put("semester", result.getCourse().getSemester() != null ? result.getCourse().getSemester().getName() : "未知学期");
                        courseData.put("credit", result.getCourse().getCredits());
                    } else {
                        courseData.put("courseName", "未知课程");
                        courseData.put("semester", "未知学期");
                        courseData.put("credit", 0);
                    }
                    
                    // 成绩信息
                    courseData.put("score", result.getScore());
                    courseData.put("grade", result.getGrade());
                    
                    return courseData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Response.success("获取学生课程数据成功", studentCourses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取学生课程数据失败: " + e.getMessage(), null));
        }
    }
}