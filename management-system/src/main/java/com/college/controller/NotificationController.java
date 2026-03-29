package com.college.controller;

import com.college.dto.NotificationDTO;
import com.college.dto.Response;
import com.college.model.Notification;
import com.college.model.User;
import com.college.service.NotificationService;
import com.college.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        
        // 根据用户名查询用户ID
        try {
            User user = userService.findByUsername(username);
            if (user != null) {
                return user.getId();
            }
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Page<NotificationDTO>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 如果没有任何筛选条件，直接获取所有通知
        Page<NotificationDTO> notifications;
        if (type == null && status == null && target == null && keyword == null) {
            notifications = notificationService.findAll(pageable);
        } else {
            notifications = notificationService.findByFilters(type, status, target, keyword, pageable);
        }
        
        return ResponseEntity.ok(Response.<Page<NotificationDTO>>success("获取通知列表成功", notifications));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<NotificationDTO>> getNotificationById(@PathVariable Long id) {
        return notificationService.findById(id)
                .map(notification -> ResponseEntity.ok(Response.<NotificationDTO>success("获取通知成功", notification)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<NotificationDTO>> createNotification(
            @RequestBody NotificationDTO notificationDTO, Authentication authentication) {
        
        try {
            System.out.println("创建通知: " + notificationDTO.getTitle());
            System.out.println("TargetIds: " + notificationDTO.getTargetIds());
            System.out.println("Type: " + notificationDTO.getType());
            System.out.println("Target: " + notificationDTO.getTarget());
            System.out.println("Status: " + notificationDTO.getStatus());
            
            // 从认证信息中获取当前用户ID并设置为发布者
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                // 在实际应用中，需要根据用户名查询用户ID
                // 这里暂时设置为示例值，实际实现需要查询用户表
                notificationDTO.setPublisherId(getUserIdFromAuthentication(authentication));
            }
            
            NotificationDTO savedNotification = notificationService.save(notificationDTO);
            return ResponseEntity.ok(Response.<NotificationDTO>success("创建通知成功", savedNotification));
        } catch (IllegalArgumentException e) {
            System.err.println("参数错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Response.<NotificationDTO>error("参数错误: " + e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("创建通知失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.<NotificationDTO>error("创建通知失败: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<NotificationDTO>> updateNotification(
            @PathVariable Long id, @RequestBody NotificationDTO notificationDTO, Authentication authentication) {
        
        try {
            System.out.println("更新通知ID: " + id);
            System.out.println("更新通知: " + notificationDTO.getTitle());
            System.out.println("TargetIds: " + notificationDTO.getTargetIds());
            System.out.println("Type: " + notificationDTO.getType());
            System.out.println("Target: " + notificationDTO.getTarget());
            System.out.println("Status: " + notificationDTO.getStatus());
            
            // 从认证信息中获取当前用户ID并设置为发布者
            if (authentication != null && authentication.isAuthenticated()) {
                notificationDTO.setPublisherId(getUserIdFromAuthentication(authentication));
            }
            
            notificationDTO.setId(id);
            NotificationDTO updatedNotification = notificationService.save(notificationDTO);
            return ResponseEntity.ok(Response.<NotificationDTO>success("更新通知成功", updatedNotification));
        } catch (IllegalArgumentException e) {
            System.err.println("参数错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Response.<NotificationDTO>error("参数错误: " + e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("更新通知失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.<NotificationDTO>error("更新通知失败: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteById(id);
        return ResponseEntity.ok(Response.<Void>success("删除通知成功", null));
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> batchDeleteNotifications(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> rawIds = (List<Object>) request.get("ids");
            List<Long> ids = rawIds.stream()
                    .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                    .collect(java.util.stream.Collectors.toList());
            notificationService.batchDelete(ids);
            return ResponseEntity.ok(Response.<Void>success("批量删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<Void>error("批量删除失败: " + e.getMessage(), null));
        }
    }

    @PostMapping("/batch-publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> batchPublishNotifications(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> rawIds = (List<Object>) request.get("ids");
            List<Long> ids = rawIds.stream()
                    .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                    .collect(java.util.stream.Collectors.toList());
            notificationService.batchPublish(ids);
            return ResponseEntity.ok(Response.<Void>success("批量发布成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Response.<Void>error("批量发布失败: " + e.getMessage(), null));
        }
    }

    // 获取统计数据
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", notificationService.countPublishedNotifications());
        // 可以添加更多统计信息
        
        return ResponseEntity.ok(Response.<Map<String, Object>>success("获取统计信息成功", stats));
    }
}