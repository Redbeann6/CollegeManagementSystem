package com.college.controller;

import com.college.dto.Response;
import com.college.model.*;
import com.college.repository.UserRepository;
import com.college.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    // 简单测试端点
    @GetMapping("/test")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'TEACHER')")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Dashboard test endpoint is working!");
    }
    
    // 学生专用测试端点
    @GetMapping("/student/test")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<String> studentTest() {
        return ResponseEntity.ok("Student dashboard test endpoint is working!");
    }

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private LeaveRequestService leaveRequestService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private ExamResultService examResultService;
    
    @Autowired
    private CourseScheduleService courseScheduleService;

    // 获取管理员仪表盘统计数据
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> getAdminDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> stats = new HashMap<>();
        
        // 基本统计信息
        stats.put("totalStudents", studentService.countAllStudents());
        stats.put("totalTeachers", teacherService.countAllTeachers());
        stats.put("totalCourses", courseService.countAllCourses());
        stats.put("totalEnrollments", enrollmentService.countAllEnrollments());
        
        // 计算变化率（相对于上个月）
        long lastMonthStudents = studentService.countStudentsInPreviousMonth();
        long currentMonthStudents = studentService.countStudentsInCurrentMonth();
        double studentChange = calculateChangeRate(lastMonthStudents, currentMonthStudents);
        
        long lastMonthTeachers = teacherService.countTeachersInPreviousMonth();
        long currentMonthTeachers = teacherService.countTeachersInCurrentMonth();
        double teacherChange = calculateChangeRate(lastMonthTeachers, currentMonthTeachers);
        
        long lastMonthCourses = courseService.countCoursesInPreviousMonth();
        long currentMonthCourses = courseService.countCoursesInCurrentMonth();
        double courseChange = calculateChangeRate(lastMonthCourses, currentMonthCourses);
        
        long lastMonthEnrollments = enrollmentService.countEnrollmentsInPreviousMonth();
        long currentMonthEnrollments = enrollmentService.countEnrollmentsInCurrentMonth();
        double enrollmentChange = calculateChangeRate(lastMonthEnrollments, currentMonthEnrollments);
        
        stats.put("studentChange", studentChange);
        stats.put("teacherChange", teacherChange);
        stats.put("courseChange", courseChange);
        stats.put("enrollmentChange", enrollmentChange);
        
        // 获取待处理任务
        List<Map<String, Object>> pendingTasks = getPendingTasks();
        stats.put("pendingTasks", pendingTasks);
        
        // 获取最近活动
        List<Map<String, Object>> recentActivities = getRecentActivities();
        stats.put("recentActivities", recentActivities);
        
        // 获取系统信息
        stats.put("systemInfo", getSystemInfo());
        
        return ResponseEntity.ok(Response.<Map<String, Object>>success("获取仪表盘数据成功", stats));
    }
    

    
    // 计算变化率
    private double calculateChangeRate(long previous, long current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double)(current - previous) / previous) * 100;
    }
    
    // 获取系统信息
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("version", "v1.0.0");
        systemInfo.put("uptime", "系统运行时间"); // 实际项目中应计算真实运行时间
        systemInfo.put("serverTime", java.time.LocalDateTime.now());
        return systemInfo;
    }
    
    // 获取待处理任务
    private List<Map<String, Object>> getPendingTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        // 获取待处理的请假申请
        List<LeaveRequest> pendingLeaveRequests = leaveRequestService.findByStatus(LeaveRequest.Status.PENDING);
        
        for (LeaveRequest request : pendingLeaveRequests) {
            Map<String, Object> task = new HashMap<>();
            task.put("id", request.getId());
            task.put("type", "leave");
            task.put("title", request.getStudent().getName() + "同学的请假申请");
            task.put("source", "请假系统");
            task.put("time", request.getCreatedAt());
            task.put("priority", "high");
            
            tasks.add(task);
        }
        
        // 这里可以继续添加其他类型的待处理任务
        // 例如：新课程申请、考试安排等
        
        return tasks;
    }
    
    // 获取最近活动
    private List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // 获取最近的请假申请
        List<LeaveRequest> recentLeaveRequests = leaveRequestService.findByStatus(LeaveRequest.Status.PENDING);
        for (LeaveRequest request : recentLeaveRequests) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", "leave_" + request.getId());
            activity.put("type", "leave");
            activity.put("user", request.getStudent().getName());
            activity.put("action", "提交了请假申请");
            activity.put("target", request.getLeaveType() + " - " + (request.getReason() != null && request.getReason().length() > 20 ? 
                request.getReason().substring(0, 20) + "..." : request.getReason()));
            activity.put("timestamp", request.getCreatedAt());
            activities.add(activity);
        }
        
        // 限制活动数量，避免过多数据
        if (activities.size() > 10) {
            activities = activities.subList(0, 10);
        }
        
        // 按时间倒序排序，最新的在前面
        activities.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeB.compareTo(timeA);
        });
        
        return activities;
    }
    

}