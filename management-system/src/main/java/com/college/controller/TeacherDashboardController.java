package com.college.controller;

import com.college.dto.Response;
import com.college.model.Course;
import com.college.model.Exam;
import com.college.model.ExamResult;
import com.college.model.Teacher;
import com.college.model.User;
import com.college.service.CourseService;
import com.college.service.ExamResultService;
import com.college.service.ExamService;
import com.college.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher")
@CrossOrigin(origins = "*")
public class TeacherDashboardController {

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private ExamResultService examResultService;

    // 获取教师仪表盘统计数据
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> getTeacherDashboardStats(Principal principal) {
        try {
            // 获取当前登录教师信息
            User currentUser = userService.findByUsername(principal.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }

            Map<String, Object> stats = new HashMap<>();
            
            // 获取教师授课课程数量
            List<Course> teachingCourses = courseService.findByTeacherId(currentUser.getId());
            stats.put("coursesCount", teachingCourses.size());
            
            // 计算授课学生总数
            long totalStudents = teachingCourses.stream()
                    .mapToLong(course -> course.getStudents().size())
                    .sum();
            stats.put("studentsCount", totalStudents);
            
            // 获取待评分成绩数量
            long ungradedCount = examResultService.countUngradedByTeacherId(currentUser.getId());
            stats.put("ungradedCount", ungradedCount);
            
            // 获取教师相关的考试数量
            List<Exam> teacherExams = examService.findByCreatorId(currentUser.getId());
            stats.put("examsCount", teacherExams.size());
            
            // 获取考试结果数量
            long examResultsCount = examResultService.countByTeacherId(currentUser.getId());
            stats.put("resultsCount", examResultsCount);
            
            // 获取教师相关的请假申请数量
            long leaveRequestsCount = 0; // 暂时设为0，可根据实际业务需求实现
            stats.put("leaveRequestsCount", leaveRequestsCount);
            
            // 计算待办事项数量（这里可以根据实际需求扩展）
            stats.put("todosCount", 0); // 暂时设为0，可根据实际业务需求扩展
            stats.put("urgentTodosCount", 0); // 暂时设为0
            
            // 获取教师信息
            Map<String, Object> teacherInfo = new HashMap<>();
            teacherInfo.put("id", currentUser.getId());
            teacherInfo.put("name", currentUser.getName());
            teacherInfo.put("username", currentUser.getUsername());
            teacherInfo.put("email", currentUser.getEmail());
            teacherInfo.put("phone", currentUser.getPhone());
            teacherInfo.put("departmentName", currentUser instanceof Teacher ? ((Teacher) currentUser).getDepartment() != null ? ((Teacher) currentUser).getDepartment().getName() : null : null);
            teacherInfo.put("title", currentUser instanceof Teacher ? ((Teacher) currentUser).getTitle() : null);
            stats.put("teacher", teacherInfo);

            // 返回教学课程列表
            List<Map<String, Object>> courseList = teachingCourses.stream().map(course -> {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("id", course.getId());
                courseMap.put("name", course.getName());
                courseMap.put("courseCode", course.getCourseCode());
                courseMap.put("credits", course.getCredits());
                courseMap.put("hours", course.getTotalHours());
                courseMap.put("studentCount", course.getStudents().size());
                courseMap.put("semester", course.getSemester() != null ? course.getSemester().getName() : null);
                return courseMap;
            }).collect(Collectors.toList());
            stats.put("teachingCourses", courseList);

            return ResponseEntity.ok(Response.success("获取教师仪表盘数据成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取教师仪表盘数据失败: " + e.getMessage(), null));
        }
    }

    // 获取教师课程成绩统计
    @GetMapping("/grades/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> getCourseGradeStats(@PathVariable Long courseId, Principal principal) {
        try {
            User currentUser = userService.findByUsername(principal.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Response.error("用户不存在", null));
            }

            Map<String, Object> gradeStats = new HashMap<>();
            
            // 获取课程成绩统计
            List<ExamResult> examResults = examResultService.findByCourseId(courseId);
            
            if (!examResults.isEmpty()) {
                // 计算平均分
                double averageScore = examResults.stream()
                        .filter(result -> result.getScore() != null)
                        .mapToDouble(ExamResult::getScore)
                        .average()
                        .orElse(0.0);
                
                // 获取最高分和最低分
                Double maxScore = examResults.stream()
                        .filter(result -> result.getScore() != null)
                        .map(ExamResult::getScore)
                        .max(Double::compareTo)
                        .orElse(0.0);
                        
                Double minScore = examResults.stream()
                        .filter(result -> result.getScore() != null)
                        .map(ExamResult::getScore)
                        .min(Double::compareTo)
                        .orElse(0.0);
                
                // 计算及格率
                long totalStudents = examResults.size();
                long passedStudents = examResults.stream()
                        .filter(result -> result.getScore() != null && result.getScore() >= 60)
                        .count();
                double passRate = totalStudents > 0 ? (passedStudents * 100.0 / totalStudents) : 0.0;
                
                gradeStats.put("averageScore", Math.round(averageScore * 10) / 10.0);
                gradeStats.put("maxScore", maxScore);
                gradeStats.put("minScore", minScore);
                gradeStats.put("passRate", Math.round(passRate * 10) / 10.0);
                
                // 分数段统计
                Map<String, Integer> scoreRanges = new HashMap<>();
                scoreRanges.put("90-100", (int) examResults.stream().filter(r -> r.getScore() != null && r.getScore() >= 90).count());
                scoreRanges.put("80-89", (int) examResults.stream().filter(r -> r.getScore() != null && r.getScore() >= 80 && r.getScore() < 90).count());
                scoreRanges.put("70-79", (int) examResults.stream().filter(r -> r.getScore() != null && r.getScore() >= 70 && r.getScore() < 80).count());
                scoreRanges.put("60-69", (int) examResults.stream().filter(r -> r.getScore() != null && r.getScore() >= 60 && r.getScore() < 70).count());
                scoreRanges.put("0-59", (int) examResults.stream().filter(r -> r.getScore() != null && r.getScore() < 60).count());
                
                gradeStats.put("scoreRanges", scoreRanges);
            } else {
                gradeStats.put("averageScore", 0.0);
                gradeStats.put("maxScore", 0.0);
                gradeStats.put("minScore", 0.0);
                gradeStats.put("passRate", 0.0);
                gradeStats.put("scoreRanges", new HashMap<>());
            }

            return ResponseEntity.ok(Response.success("获取课程成绩统计成功", gradeStats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.error("获取课程成绩统计失败: " + e.getMessage(), null));
        }
    }
}