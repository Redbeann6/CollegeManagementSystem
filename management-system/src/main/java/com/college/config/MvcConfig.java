package com.college.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 为所有API路径添加统一的 /api 前缀，但排除认证、学生API和仪表盘相关路径
        configurer.addPathPrefix("/api", c -> !c.getName().contains("AuthController") && !c.getName().contains("StudentAPIController") && !c.getName().contains("DashboardController") && !c.getName().contains("TeacherDashboardController") && !c.getName().contains("TeacherAPIController") && !c.getName().contains("TeacherExamController") && !c.getName().contains("TeacherNotificationController") && !c.getName().contains("CourseScheduleController") && !c.getName().contains("GradeController") && !c.getName().contains("LeaveRequestController") && !c.getName().contains("ProfileController"));
    }
}