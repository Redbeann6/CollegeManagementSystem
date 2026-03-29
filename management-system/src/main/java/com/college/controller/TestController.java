package com.college.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Test endpoint is working!");
        return response;
    }

    @GetMapping("/course-schedules-test")
    public Map<String, Object> testCourseSchedules() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Course schedules test endpoint is working!");
        return response;
    }
}