package com.college.repository;

import com.college.model.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {
    
    // 根据用户ID和通知ID查找
    Optional<NotificationUser> findByUserIdAndNotificationId(Long userId, Long notificationId);
    
    // 根据用户ID查找所有通知状态
    List<NotificationUser> findByUserId(Long userId);
    
    // 根据用户ID查找未读通知数量
    @Query("SELECT COUNT(nu) FROM NotificationUser nu WHERE nu.userId = :userId AND nu.isRead = false")
    Long countUnreadNotificationsByUserId(@Param("userId") Long userId);
    
    // 根据通知ID查找所有用户状态
    List<NotificationUser> findByNotificationId(Long notificationId);
}