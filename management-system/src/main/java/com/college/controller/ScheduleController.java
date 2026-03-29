package com.college.controller;

import com.college.dto.CourseScheduleDTO;
import com.college.service.CourseScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    @Autowired
    private CourseScheduleService courseScheduleService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSchedule() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("message", "Schedule API test is working!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSchedules() {
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
}