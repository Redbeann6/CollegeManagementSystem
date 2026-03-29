package com.college.service;

import com.college.model.NotificationUser;
import java.util.List;
import java.util.Optional;

public interface NotificationUserService {
    NotificationUser save(NotificationUser notificationUser);
    Optional<NotificationUser> findById(Long id);
    List<NotificationUser> findAll();
    void deleteById(Long id);
    Optional<NotificationUser> findByUserIdAndNotificationId(Long userId, Long notificationId);
    List<NotificationUser> findByUserId(Long userId);
    Long countUnreadNotificationsByUserId(Long userId);
    List<NotificationUser> findByNotificationId(Long notificationId);
    
    // 标记通知为已读
    NotificationUser markAsRead(Long userId, Long notificationId);
    
    // 批量创建用户通知记录
    List<NotificationUser> createNotificationUsersForUsers(Long notificationId, List<Long> userIds);
}