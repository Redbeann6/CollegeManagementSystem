package com.college.controller;

import com.college.dto.CourseDTO;
import com.college.dto.EnrollmentDTO;
import com.college.dto.ExamResultDTO;
import com.college.dto.Response;
import com.college.model.Course;
import com.college.model.Enrollment;
import com.college.model.Exam;
import com.college.model.ExamResult;
import com.college.model.Student;
import com.college.model.User;
import com.college.service.CourseService;
import com.college.service.EnrollmentService;
import com.college.service.ExamResultService;
import com.college.service.ExamService;
import com.college.service.LeaveRequestService;
import com.college.service.CourseScheduleService;
import com.college.model.Department;
import com.college.service.DepartmentService;
import com.college.model.Teacher;
import com.college.service.TeacherService;
import com.college.service.NotificationService;
import com.college.dto.TeacherDTO;
import com.college.dto.CourseScheduleDTO;
import com.college.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentAPIController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private ExamResultService examResultService;
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private LeaveRequestService leaveRequestService;
    
    @Autowired
    private CourseScheduleService courseScheduleService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private TeacherService teacherService;
        
    @Autowired
    private NotificationService notificationService;
        
    // 获取学生请假记录
    @GetMapping("/leave-requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<List<com.college.dto.LeaveRequestDTO>>> getStudentLeaveRequests(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                List<com.college.model.LeaveRequest> leaveRequests = leaveRequestService.findByStudentId(student.getId());
                List<com.college.dto.LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream()
                        .map(leaveRequestService::convertToDTO)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(Response.success("获取请假申请列表成功", leaveRequestDTOs));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取请假申请失败: " + e.getMessage(), null));
        }
    }

    // 获取学生本人信息
    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<Student>> getStudentProfile(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                return ResponseEntity.ok(Response.success("获取学生信息成功", student));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取学生信息失败: " + e.getMessage(), null));
        }
    }

    // 获取学生可选的课程列表
    @GetMapping("/courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<List<CourseDTO>>> getAvailableCourses(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                // 获取所有课程，但排除学生已选的课程
                List<Course> allCourses = courseService.findAll();
                List<CourseDTO> allCourseDTOs = allCourses.stream()
                        .map(courseService::convertToDTO)
                        .collect(java.util.stream.Collectors.toList());
                
                // 获取学生已选的课程ID列表
                List<Long> enrolledCourseIds = enrollmentService.findByStudentId(student.getId()).stream()
                        .map(enrollment -> enrollment.getCourse().getId())
                        .collect(Collectors.toList());
                
                // 过滤掉已选的课程
                List<CourseDTO> availableCourses = allCourseDTOs.stream()
                        .filter(course -> !enrolledCourseIds.contains(course.getId()))
                        .collect(Collectors.toList());
                
                return ResponseEntity.ok(Response.success("获取可选课程列表成功", availableCourses));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取课程列表失败: " + e.getMessage(), null));
        }
    }

    // 获取学生已选的课程列表
    @GetMapping("/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<List<EnrollmentDTO>>> getStudentEnrollments(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                List<Enrollment> enrollments = enrollmentService.findByStudentId(student.getId());
                List<EnrollmentDTO> enrollmentDTOs = enrollmentService.convertToDTOList(enrollments);
                return ResponseEntity.ok(Response.success("获取已选课程列表成功", enrollmentDTOs));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取已选课程列表失败: " + e.getMessage(), null));
        }
    }

    // 获取学生成绩列表
    @GetMapping("/grades")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<List<ExamResultDTO>>> getStudentGrades(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                List<ExamResult> examResults = examResultService.findByStudentId(student.getId());
                List<ExamResultDTO> examResultDTOs = examResults.stream()
                        .map(examResultService::convertToDTO)
                        .collect(java.util.stream.Collectors.toList());
                return ResponseEntity.ok(Response.success("获取成绩列表成功", examResultDTOs));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取成绩列表失败: " + e.getMessage(), null));
        }
    }

    // 获取学生考试安排
    @GetMapping("/exams")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<List<Object>>> getStudentExams(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                
                // 获取学生的所有考试
                List<Exam> examList = examService.findExamsByStudentId(student.getId());
                List<Object> examDTOs = new ArrayList<>();
                
                for (Exam exam : examList) {
                    Map<String, Object> examMap = new HashMap<>();
                    examMap.put("id", exam.getId());
                    examMap.put("examName", exam.getTitle());
                    if (exam.getCourse() != null) {
                        examMap.put("courseName", exam.getCourse().getName());
                    } else {
                        examMap.put("courseName", "未知课程");
                    }
                    examMap.put("examDate", exam.getExamTime() != null ? exam.getExamTime().toLocalDate() : null);
                    examMap.put("examTime", (exam.getStartTime() != null ? exam.getStartTime().toLocalTime() : "") + "-" + (exam.getEndTime() != null ? exam.getEndTime().toLocalTime() : ""));
                    examMap.put("location", exam.getClassroom() != null ? exam.getClassroom() : "待定");
                    examMap.put("duration", exam.getDuration() != null ? exam.getDuration() : 0);
                    examMap.put("type", exam.getExamType() != null ? exam.getExamType() : "未知类型");
                    // 转换数据库中的大写状态为前端需要的小写状态
                    String dbStatus = exam.getStatus();
                    String frontendStatus = convertDbStatusToFrontendStatus(dbStatus);
                    examMap.put("status", frontendStatus);
                    
                    // 根据状态设置状态文本
                    String statusText = "未知状态";
                    switch (frontendStatus) {
                        case "not_started":
                            statusText = "未开始";
                            break;
                        case "ongoing":
                            statusText = "进行中";
                            break;
                        case "completed":
                            statusText = "已结束";
                            break;
                    }
                    examMap.put("statusText", statusText);
                    
                    examMap.put("totalPoints", exam.getTotalMarks() != null ? exam.getTotalMarks() : 0);
                    examMap.put("description", exam.getDescription() != null ? exam.getDescription() : "");
                    
                    // 添加考试要求
                    List<String> requirements = new ArrayList<>();
                    if (exam.getDescription() != null) {
                        requirements.add(exam.getDescription());
                    }
                    examMap.put("requirements", requirements);
                    
                    // 添加成绩信息
                    Optional<ExamResult> examResultOpt = examResultService.findByStudentIdAndExamId(student.getId(), exam.getId());
                    if (examResultOpt.isPresent()) {
                        ExamResult examResult = examResultOpt.get();
                        examMap.put("score", examResult.getScore());
                        examMap.put("grade", examResult.getGrade());
                    } else {
                        examMap.put("score", null);
                        examMap.put("grade", null);
                    }
                    
                    examDTOs.add(examMap);
                }
                
                return ResponseEntity.ok(Response.success("获取考试安排成功", examDTOs));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取考试安排失败: " + e.getMessage(), null));
        }
    }
    
    // 获取学生仪表盘统计数据
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> getStudentDashboardStats(Authentication authentication) {
        System.out.println("StudentAPIController.getStudentDashboardStats called");
        System.out.println("Authentication: " + authentication);
        
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (!(currentUser instanceof Student)) {
                System.out.println("用户不是学生身份");
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
            Student currentStudent = (Student) currentUser;
            Long studentId = currentStudent.getId();
            System.out.println("正在获取学生ID: " + studentId + " 的仪表盘数据");

            Map<String, Object> stats = new HashMap<>();

            // 1. 课程统计
            try {
                int courseCount = enrollmentService.countEnrollmentsByStudentId(studentId);
                stats.put("courseCount", courseCount);
                System.out.println("课程统计完成: " + courseCount);
            } catch (Exception e) {
                System.out.println("获取课程统计失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("courseCount", 0);
            }

            // 2. 近期考试统计
            try {
                int examCount = examService.countUpcomingExamsByStudentId(studentId, LocalDate.now(), LocalDate.now().plusDays(30));
                stats.put("examCount", examCount);
                System.out.println("考试统计完成: " + examCount);
            } catch (Exception e) {
                System.out.println("获取考试统计失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("examCount", 0);
            }

            // 3. 新成绩统计（最近一个月的成绩）
            try {
                int resultCount = examResultService.countRecentResultsByStudentId(studentId, LocalDate.now().minusMonths(1));
                stats.put("resultCount", resultCount);
                System.out.println("成绩统计完成: " + resultCount);
            } catch (Exception e) {
                System.out.println("获取成绩统计失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("resultCount", 0);
            }

            // 4. 待处理请假申请统计
            try {
                int leaveCount = (int) leaveRequestService.countPendingByStudentId(studentId);
                stats.put("leaveCount", leaveCount);
                System.out.println("请假统计完成: " + leaveCount);
            } catch (Exception e) {
                System.out.println("获取请假统计失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("leaveCount", 0);
            }

            // 5. 近期考试安排
            try {
                List<Map<String, Object>> upcomingExams = getUpcomingExams(studentId);
                stats.put("upcomingExams", upcomingExams);
                System.out.println("近期考试安排完成: " + upcomingExams.size() + " 条");
            } catch (Exception e) {
                System.out.println("获取近期考试安排失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("upcomingExams", new ArrayList<>());
            }

            // 6. 最新考试成绩
            try {
                List<Map<String, Object>> latestResults = getLatestResults(studentId);
                stats.put("latestResults", latestResults);
                System.out.println("最新成绩完成: " + latestResults.size() + " 条");
            } catch (Exception e) {
                System.out.println("获取最新成绩失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("latestResults", new ArrayList<>());
            }

            // 7. 本月课程表
            try {
                List<Map<String, Object>> thisMonthCourses = getCurrentMonthCourses(studentId);
                stats.put("thisMonthCourses", thisMonthCourses);
                System.out.println("本月课程表完成: " + thisMonthCourses.size() + " 条");
            } catch (Exception e) {
                System.out.println("获取本月课程表失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("thisMonthCourses", new ArrayList<>());
            }

            // 8. 学生课表
            try {
                List<Map<String, Object>> studentTimetable = getStudentTimetable(studentId);
                stats.put("studentTimetable", studentTimetable);
                System.out.println("学生课表完成: " + studentTimetable.size() + " 条");
            } catch (Exception e) {
                System.out.println("获取学生课表失败: " + e.getMessage());
                e.printStackTrace();
                stats.put("studentTimetable", new ArrayList<>());
            }

            System.out.println("仪表盘数据获取完成");
            return ResponseEntity.ok(Response.success("获取学生仪表盘数据成功", stats));
        } catch (Exception e) {
            System.out.println("获取仪表盘数据时发生严重错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Response.error("获取数据失败: " + e.getMessage(), null));
        }
    }
    
    // 将数据库中的考试状态转换为前端需要的状态
    private String convertDbStatusToFrontendStatus(String dbStatus) {
        if (dbStatus == null) {
            return "not_started"; // 默认状态
        }
        
        switch (dbStatus.toUpperCase()) {
            case "NOT_STARTED":
                return "not_started";
            case "IN_PROGRESS":
                return "ongoing";
            case "COMPLETED":
                return "completed";
            default:
                return "not_started"; // 默认状态
        }
    }
    
    // 获取近期考试安排
    private List<Map<String, Object>> getUpcomingExams(Long studentId) {
        List<Map<String, Object>> exams = new ArrayList<>();
        
        // 获取学生未来30天内的考试
        List<Exam> examList = examService.findUpcomingExamsByStudentId(studentId, LocalDate.now(), LocalDate.now().plusDays(30));
        
        for (Exam exam : examList) {
            Map<String, Object> examMap = new HashMap<>();
            examMap.put("id", exam.getId());
            if (exam.getCourse() != null) {
                examMap.put("courseName", exam.getCourse().getName());
            } else {
                examMap.put("courseName", "未知课程");
            }
            examMap.put("examDate", exam.getExamTime() != null ? exam.getExamTime().toLocalDate() : null);
            examMap.put("examTime", (exam.getStartTime() != null ? exam.getStartTime().toLocalTime() : "") + "-" + (exam.getEndTime() != null ? exam.getEndTime().toLocalTime() : ""));
            examMap.put("location", exam.getClassroom() != null ? exam.getClassroom() : "待定");
            // 转换数据库中的大写状态为前端需要的小写状态
            String dbStatus = exam.getStatus();
            String frontendStatus = convertDbStatusToFrontendStatus(dbStatus);
            examMap.put("status", frontendStatus);
            exams.add(examMap);
        }
        
        return exams;
    }
    
    // 获取最新考试成绩
    private List<Map<String, Object>> getLatestResults(Long studentId) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 获取学生最近一个月的成绩
        List<ExamResult> resultList = examResultService.findRecentResultsByStudentId(studentId, LocalDate.now().minusMonths(1));
        
        for (ExamResult result : resultList) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", result.getId());
            if (result.getCourse() != null) {
                resultMap.put("courseName", result.getCourse().getName());
            } else {
                resultMap.put("courseName", "未知课程");
            }
            if (result.getExam() != null) {
                resultMap.put("examName", result.getExam().getTitle());
            } else {
                resultMap.put("examName", "未知考试");
            }
            resultMap.put("score", result.getScore());
            resultMap.put("grade", result.getGrade());
            resultMap.put("examDate", result.getExam().getExamTime() != null ? result.getExam().getExamTime().toLocalDate() : null);
            results.add(resultMap);
        }
        
        return results;
    }
    
    // 获取本月课程表
    private List<Map<String, Object>> getCurrentMonthCourses(Long studentId) {
        List<Map<String, Object>> courses = new ArrayList<>();
        
        // 获取学生当前月的选课
        List<Enrollment> enrollmentList = enrollmentService.findByStudentId(studentId);
        
        for (Enrollment enrollment : enrollmentList) {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", enrollment.getId());
            if (enrollment.getCourse() != null) {
                courseMap.put("courseName", enrollment.getCourse().getName());
                if (enrollment.getCourse().getTeacher() != null) {
                    courseMap.put("teacher", enrollment.getCourse().getTeacher().getName());
                } else {
                    courseMap.put("teacher", "未知教师");
                }
                courseMap.put("schedule", enrollment.getCourse().getScheduleInfo());
                courseMap.put("location", enrollment.getCourse().getClassroom());
            } else {
                courseMap.put("courseName", "未知课程");
                courseMap.put("teacher", "未知教师");
                courseMap.put("schedule", "");
                courseMap.put("location", "");
            }
            courseMap.put("progress", 0); // 暂时不包含进度信息
            courses.add(courseMap);
        }
        
        return courses;
    }
    
    // 获取学生课表
    private List<Map<String, Object>> getStudentTimetable(Long studentId) {
        List<Map<String, Object>> timetable = new ArrayList<>();
        
        try {
            // 首先获取学生的所有课程
            List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);
            
            for (Enrollment enrollment : enrollments) {
                Course course = enrollment.getCourse();
                
                // 根据课程ID获取课程安排
                if (course != null) {
                    List<com.college.dto.CourseScheduleDTO> courseSchedules = courseScheduleService.getCourseSchedulesByCourseId(course.getId());
                                
                    for (com.college.dto.CourseScheduleDTO schedule : courseSchedules) {
                        Map<String, Object> courseMap = new HashMap<>();
                        courseMap.put("id", schedule.getId());
                        courseMap.put("courseId", course.getId());
                        courseMap.put("courseName", course.getName());
                        courseMap.put("teacher", course.getTeacher() != null ? course.getTeacher().getName() : "未知");
                        courseMap.put("location", schedule.getLocation());
                        courseMap.put("day", schedule.getDay());
                        courseMap.put("section", schedule.getSection());
                                    
                        timetable.add(courseMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return timetable;
    }
    
    // 简单的测试端点（需要认证）
    @GetMapping("/test")
    public ResponseEntity<Response<String>> testEndpoint(Authentication authentication) {
        System.out.println("Test endpoint called");
        return ResponseEntity.ok(Response.success("Test endpoint working", "success"));
    }
    
    // 公开测试端点（不需要认证）
    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<Response<String>> healthCheck() {
        System.out.println("Health check endpoint called");
        return ResponseEntity.ok(Response.success("API is healthy", "OK"));
    }
    
    // 学生获取所有教师列表（用于课程选择界面）
    @GetMapping("/teachers")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<TeacherDTO>>> getAllTeachers() {
        try {
            List<Teacher> teachers = teacherService.findAll();
            List<TeacherDTO> teacherDTOs = teachers.stream()
                    .map(teacher -> {
                        TeacherDTO dto = new TeacherDTO();
                        dto.setId(teacher.getId());
                        dto.setName(teacher.getName());
                        dto.setDepartmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null);
                        dto.setDepartment(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null);
                        dto.setTitle(teacher.getTitle());
                        return dto;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Response.success("获取教师列表成功", teacherDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取教师列表失败: " + e.getMessage(), null));
        }
    }
    
    // 学生获取所有系部列表（用于课程选择界面）
    @GetMapping("/departments")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<Department>>> getAllDepartments() {
        try {
            List<Department> departments = departmentService.findAll();
            return ResponseEntity.ok(Response.success("获取系部列表成功", departments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取系部列表失败: " + e.getMessage(), null));
        }
    }
    
    // 学生获取所有课程安排列表（用于课程选择界面）
    @GetMapping("/course-schedules")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<List<CourseScheduleDTO>>> getAllCourseSchedules() {
        try {
            List<CourseScheduleDTO> schedules = courseScheduleService.getAllCourseSchedules();
            return ResponseEntity.ok(Response.success("获取课程安排列表成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取课程安排列表失败: " + e.getMessage(), null));
        }
    }
    
    // 学生选课
    @PostMapping("/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<EnrollmentDTO>> enrollCourse(@RequestBody EnrollmentDTO enrollmentDTO, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (!(currentUser instanceof Student)) {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
            Student currentStudent = (Student) currentUser;
            
            // 验证传入的学生ID与当前登录学生ID是否一致
            // 如果前端没有传递studentId，则跳过此验证，直接使用当前认证用户
            if (enrollmentDTO.getStudentId() != null && 
                !currentStudent.getStudentId().equals(String.valueOf(enrollmentDTO.getStudentId()))) {
                return ResponseEntity.badRequest().body(Response.error("不允许为其他学生选课", null));
            }
            
            // 检查是否已选该课程
            if (enrollmentService.existsByStudentIdAndCourseId(
                    currentStudent.getId(), enrollmentDTO.getCourseId())) {  // 使用数据库主键ID
                return ResponseEntity.badRequest().body(Response.error("您已选择该课程", null));
            }
            
            // 检查课程容量
            if (!enrollmentService.isCourseAvailable(enrollmentDTO.getCourseId())) {
                return ResponseEntity.badRequest().body(Response.error("课程已满", null));
            }
            
            // 检查课程时间冲突
            // 这里可以添加时间冲突检查逻辑
            
            // 创建一个新的EnrollmentDTO，使用数据库主键ID而不是学号
            EnrollmentDTO newEnrollmentDTO = new EnrollmentDTO();
            newEnrollmentDTO.setStudentId(currentStudent.getId());  // 使用数据库主键ID
            newEnrollmentDTO.setCourseId(enrollmentDTO.getCourseId());
            newEnrollmentDTO.setSemesterId(enrollmentDTO.getSemesterId());
            newEnrollmentDTO.setStatus(enrollmentDTO.getStatus());
            newEnrollmentDTO.setEnrollDate(enrollmentDTO.getEnrollDate());
            newEnrollmentDTO.setScore(enrollmentDTO.getScore());
            newEnrollmentDTO.setRemark(enrollmentDTO.getRemark());
            
            Enrollment enrollment = enrollmentService.convertToEntity(newEnrollmentDTO);
            Enrollment savedEnrollment = enrollmentService.save(enrollment);
            EnrollmentDTO savedDTO = enrollmentService.convertToDTO(savedEnrollment);
            return ResponseEntity.ok(Response.success("选课成功", savedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("选课失败: " + e.getMessage(), null));
        }
    }
    
    // 学生退课
    @DeleteMapping("/enrollments/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<Void>> dropCourse(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (!(currentUser instanceof Student)) {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
            Student currentStudent = (Student) currentUser;
            
            // 验证选课记录是否属于当前学生
            Optional<Enrollment> enrollmentOpt = enrollmentService.findById(id);
            if (!enrollmentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Enrollment enrollment = enrollmentOpt.get();
            if (!enrollment.getStudent().getId().equals(currentStudent.getId())) {  // 比较数据库主键ID
                return ResponseEntity.badRequest().body(Response.error("无权退选不属于自己的课程", null));
            }
            
            // 只允许退选状态为NORMAL或IN_PROGRESS的课程
            if (enrollment.getStatus() != Enrollment.Status.NORMAL && 
                enrollment.getStatus() != Enrollment.Status.IN_PROGRESS) {
                return ResponseEntity.badRequest().body(Response.error("该课程状态不允许退课", null));
            }
            
            enrollmentService.deleteById(id);
            return ResponseEntity.ok(Response.success("退课成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("退课失败: " + e.getMessage(), null));
        }
    }
    
    // 获取选课状态
    @GetMapping("/selection-status")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Response<Map<String, Object>>> getSelectionStatus(Authentication authentication) {
        try {
            Map<String, Object> statusData = new HashMap<>();
            
            // 这里可以根据实际的业务逻辑来确定选课状态
            // 简单示例：返回默认的选课状态
            statusData.put("status", "during"); // before, during, after
            statusData.put("semester", "2024-2025学年第一学期");
            statusData.put("startTime", "2024-09-01 08:00:00");
            statusData.put("endTime", "2024-09-10 20:00:00");
            statusData.put("makeupStartTime", "2024-09-15 08:00:00");
            statusData.put("makeupEndTime", "2024-09-20 20:00:00");
            
            return ResponseEntity.ok(Response.success("获取选课状态成功", statusData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取选课状态失败: " + e.getMessage(), null));
        }
    }
    
    // 获取学生通知
    @GetMapping("/notifications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<List<Object>>> getStudentNotifications(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                
                // 获取学生通知（包含阅读状态）
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 50, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
                org.springframework.data.domain.Page<com.college.dto.NotificationDTO> notifications = notificationService.findPublishedNotificationsForUserWithReadStatus(student.getId(), pageable);
                
                // 转换为前端需要的格式
                List<Object> notificationList = notifications.getContent().stream()
                        .map(notification -> {
                            java.util.Map<String, Object> notificationMap = new java.util.HashMap<>();
                            notificationMap.put("id", notification.getId());
                            notificationMap.put("title", notification.getTitle());
                            notificationMap.put("content", notification.getContent());
                            notificationMap.put("sender", notification.getPublisherName());
                            notificationMap.put("createTime", notification.getCreatedAt());
                            notificationMap.put("status", notification.getIsRead() ? "read" : "unread");
                            notificationMap.put("isRead", notification.getIsRead());
                            notificationMap.put("isImportant", notification.getType().equals("IMPORTANT"));
                            notificationMap.put("attachments", java.util.Collections.emptyList());
                            return notificationMap;
                        })
                        .collect(java.util.stream.Collectors.toList());
                
                return ResponseEntity.ok(Response.success("获取通知成功", notificationList));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取通知失败: " + e.getMessage(), null));
        }
    }
    
    // 标记通知为已读
    @PostMapping("/notifications/{id}/mark-read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<Void>> markNotificationAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                
                // 标记通知为已读
                notificationService.markNotificationAsRead(student.getId(), id);
                
                return ResponseEntity.ok(Response.success("标记已读成功", null));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("标记已读失败: " + e.getMessage(), null));
        }
    }
    
    // 获取未读通知数量
    @GetMapping("/notifications/unread-count")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Response<Long>> getUnreadNotificationCount(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Student) {
                Student student = (Student) currentUser;
                
                Long unreadCount = notificationService.countUnreadNotificationsForUser(student.getId());
                
                return ResponseEntity.ok(Response.success("获取未读通知数量成功", unreadCount));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是学生身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取未读通知数量失败: " + e.getMessage(), null));
        }
    }
}
