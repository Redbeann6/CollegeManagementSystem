package com.college.controller;

import com.college.dto.Response;
import com.college.model.Teacher;
import com.college.model.User;
import com.college.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher/notifications")
@CrossOrigin(origins = "*")
public class TeacherNotificationController {

    @Autowired
    private NotificationService notificationService;

    // 获取教师通知
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Response<List<Object>>> getTeacherNotifications(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser instanceof Teacher) {
                Teacher teacher = (Teacher) currentUser;
                
                // 获取教师通知
                Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
                org.springframework.data.domain.Page<com.college.dto.NotificationDTO> notifications = notificationService.findPublishedNotificationsForUser(teacher.getId(), pageable);
                
                // 转换为前端需要的格式
                List<Object> notificationList = notifications.getContent().stream()
                        .map(notification -> {
                            Map<String, Object> notificationMap = new HashMap<>();
                            notificationMap.put("id", notification.getId());
                            notificationMap.put("title", notification.getTitle());
                            notificationMap.put("content", notification.getContent());
                            notificationMap.put("sender", notification.getPublisherName());
                            notificationMap.put("createTime", notification.getCreatedAt());
                            notificationMap.put("status", "unread");
                            notificationMap.put("isImportant", notification.getType().equals("IMPORTANT"));
                            notificationMap.put("attachments", Collections.emptyList());
                            return notificationMap;
                        })
                        .collect(Collectors.toList());
                
                return ResponseEntity.ok(Response.success("获取通知成功", notificationList));
            } else {
                return ResponseEntity.badRequest().body(Response.error("当前用户不是教师身份", null));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Response.error("获取通知失败: " + e.getMessage(), null));
        }
    }
}
