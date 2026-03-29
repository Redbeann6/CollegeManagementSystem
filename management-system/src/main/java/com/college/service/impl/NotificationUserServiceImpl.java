package com.college.service.impl;

import com.college.model.Notification;
import com.college.model.NotificationUser;
import com.college.repository.NotificationUserRepository;
import com.college.service.NotificationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationUserServiceImpl implements NotificationUserService {

    @Autowired
    private NotificationUserRepository notificationUserRepository;

    @Override
    public NotificationUser save(NotificationUser notificationUser) {
        return notificationUserRepository.save(notificationUser);
    }

    @Override
    public Optional<NotificationUser> findById(Long id) {
        return notificationUserRepository.findById(id);
    }

    @Override
    public List<NotificationUser> findAll() {
        return notificationUserRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        notificationUserRepository.deleteById(id);
    }

    @Override
    public Optional<NotificationUser> findByUserIdAndNotificationId(Long userId, Long notificationId) {
        return notificationUserRepository.findByUserIdAndNotificationId(userId, notificationId);
    }

    @Override
    public List<NotificationUser> findByUserId(Long userId) {
        return notificationUserRepository.findByUserId(userId);
    }

    @Override
    public Long countUnreadNotificationsByUserId(Long userId) {
        Long count = notificationUserRepository.countUnreadNotificationsByUserId(userId);
        return count != null ? count : 0L;
    }

    @Override
    public List<NotificationUser> findByNotificationId(Long notificationId) {
        return notificationUserRepository.findByNotificationId(notificationId);
    }

    @Override
    @Transactional
    public NotificationUser markAsRead(Long userId, Long notificationId) {
        Optional<NotificationUser> existingRecord = findByUserIdAndNotificationId(userId, notificationId);
        
        NotificationUser notificationUser;
        if (existingRecord.isPresent()) {
            notificationUser = existingRecord.get();
            if (!notificationUser.getIsRead()) {
                notificationUser.setIsRead(true);
                notificationUser.setReadTime(LocalDateTime.now());
            }
        } else {
            // 如果没有记录，则创建一个新记录
            notificationUser = new NotificationUser();
            notificationUser.setUserId(userId);
            
            // 创建Notification实体并设置ID
            Notification notification = new Notification();
            notification.setId(notificationId);
            notificationUser.setNotification(notification);
            
            notificationUser.setIsRead(true);
            notificationUser.setReadTime(LocalDateTime.now());
        }
        
        return save(notificationUser);
    }

    @Override
    @Transactional
    public List<NotificationUser> createNotificationUsersForUsers(Long notificationId, List<Long> userIds) {
        List<NotificationUser> notificationUsers = userIds.stream().map(userId -> {
            NotificationUser notificationUser = new NotificationUser();
            
            // 创建Notification实体并设置ID
            Notification notification = new Notification();
            notification.setId(notificationId);
            notificationUser.setNotification(notification);
            
            notificationUser.setUserId(userId);
            notificationUser.setIsRead(false);
            return notificationUser;
        }).collect(Collectors.toList());
        
        return notificationUserRepository.saveAll(notificationUsers);
    }
}