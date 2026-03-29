package com.college.controller;

import com.college.dto.CourseScheduleDTO;
import com.college.dto.CourseScheduleDetailDTO;
import com.college.exception.ResourceNotFoundException;
import com.college.service.CourseScheduleService;
import com.college.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course-schedules")
public class CourseScheduleController {

    @Autowired
    private CourseScheduleService courseScheduleService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCourseSchedules() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseScheduleDTO> schedules = courseScheduleService.getAllCourseSchedules();
            response.put("success", true);
            response.put("data", schedules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程安排列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourseScheduleById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            CourseScheduleDTO schedule = courseScheduleService.getCourseScheduleById(id);
            response.put("success", true);
            response.put("data", schedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 获取包含课程和系部信息的课程安排详情
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getAllCourseSchedulesWithDetails() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseScheduleDetailDTO> schedules = courseScheduleService.getAllCourseSchedulesWithDetails();
            response.put("success", true);
            response.put("data", schedules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程安排详情列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // 获取单个课程安排的详细信息
    @GetMapping("/details/{id}")
    public ResponseEntity<Map<String, Object>> getCourseScheduleDetailById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            CourseScheduleDetailDTO schedule = courseScheduleService.getCourseScheduleDetailById(id);
            response.put("success", true);
            response.put("data", schedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourseSchedule(@RequestBody CourseScheduleDTO courseScheduleDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查时间冲突
            boolean hasConflict = courseScheduleService.checkScheduleConflict(
                    courseScheduleDTO.getTeacherId(),
                    courseScheduleDTO.getClassId(),
                    courseScheduleDTO.getLocation(),
                    courseScheduleDTO.getDay(),
                    courseScheduleDTO.getSection()
            );
            
            if (hasConflict) {
                response.put("success", false);
                response.put("message", "时间冲突：该时间段已有课程安排");
                return ResponseEntity.badRequest().body(response);
            }

            CourseScheduleDTO createdSchedule = courseScheduleService.createCourseSchedule(courseScheduleDTO);
            response.put("success", true);
            response.put("data", createdSchedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourseSchedule(
            @PathVariable Long id, 
            @RequestBody CourseScheduleDTO courseScheduleDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 首先检查课程安排是否存在
            CourseScheduleDTO existingSchedule = courseScheduleService.getCourseScheduleById(id);
            
            // 检查时间冲突，排除当前记录本身
            boolean hasConflict = courseScheduleService.checkScheduleConflict(
                    courseScheduleDTO.getTeacherId(),
                    courseScheduleDTO.getClassId(),
                    courseScheduleDTO.getLocation(),
                    courseScheduleDTO.getDay(),
                    courseScheduleDTO.getSection(),
                    id
            );
            
            // 检查是否正在更新相同的课程安排记录
            boolean isSameRecord = existingSchedule.getId().equals(courseScheduleDTO.getId());
            boolean isSameSchedule = existingSchedule.getTeacherId().equals(courseScheduleDTO.getTeacherId()) &&
                                  existingSchedule.getClassId().equals(courseScheduleDTO.getClassId()) &&
                                  existingSchedule.getDay().equals(courseScheduleDTO.getDay()) &&
                                  existingSchedule.getSection().equals(courseScheduleDTO.getSection());
            
            // 只有当存在冲突且不是在更新相同的时间安排时才返回错误
            if (hasConflict && !(isSameRecord && isSameSchedule)) {
                response.put("success", false);
                response.put("message", "时间冲突：该时间段已有课程安排");
                return ResponseEntity.badRequest().body(response);
            }

            CourseScheduleDTO updatedSchedule = courseScheduleService.updateCourseSchedule(id, courseScheduleDTO);
            response.put("success", true);
            response.put("data", updatedSchedule);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourseSchedule(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            courseScheduleService.deleteCourseSchedule(id);
            response.put("success", true);
            response.put("message", "课程安排删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or (@courseService.isCourseTaughtByCurrentUser(#courseId, authentication.principal))")
    public ResponseEntity<Map<String, Object>> getCourseSchedulesByCourseId(@PathVariable Long courseId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseScheduleDTO> schedules = courseScheduleService.getCourseSchedulesByCourseId(courseId);
            response.put("success", true);
            response.put("data", schedules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/by-teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getCourseSchedulesByTeacherId(@PathVariable Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseScheduleDTO> schedules = courseScheduleService.getCourseSchedulesByTeacherId(teacherId);
            response.put("success", true);
            response.put("data", schedules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/by-class/{classId}")
    public ResponseEntity<Map<String, Object>> getCourseSchedulesByClassId(@PathVariable Long classId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseScheduleDTO> schedules = courseScheduleService.getCourseSchedulesByClassId(classId);
            response.put("success", true);
            response.put("data", schedules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/by-day/{day}")
    public ResponseEntity<Map<String, Object>> getCourseSchedulesByDay(@PathVariable Integer day) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseScheduleDTO> schedules = courseScheduleService.getCourseSchedulesByDay(day);
            response.put("success", true);
            response.put("data", schedules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/by-section/{section}")
    public ResponseEntity<Map<String, Object>> getCourseSchedulesBySection(@PathVariable Integer section) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseScheduleDTO> schedules = courseScheduleService.getCourseSchedulesBySection(section);
            response.put("success", true);
            response.put("data", schedules);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程安排失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}