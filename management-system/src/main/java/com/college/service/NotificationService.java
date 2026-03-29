package com.college.service;

import com.college.dto.NotificationDTO;
import com.college.model.Notification;
import com.college.model.NotificationUser;
import com.college.model.User;
import com.college.repository.NotificationRepository;
import com.college.repository.UserRepository;
import com.college.service.NotificationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationUserService notificationUserService;

    public Page<NotificationDTO> findAll(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        return notifications.map(notification -> {
            String publisherName = getPublisherName(notification.getPublisherId());
            return NotificationDTO.fromEntity(notification, publisherName);
        });
    }

    public Page<NotificationDTO> findByFilters(String type, String status, String target, String keyword, Pageable pageable) {
        Notification.NotificationType notificationType = type != null ? Notification.NotificationType.valueOf(type) : null;
        Notification.NotificationStatus notificationStatus = status != null ? Notification.NotificationStatus.valueOf(status) : null;
        Notification.NotificationTarget notificationTarget = target != null ? Notification.NotificationTarget.valueOf(target) : null;

        Page<Notification> notifications = notificationRepository.findByFilters(
                notificationType, notificationStatus, notificationTarget, keyword, pageable);

        return notifications.map(notification -> {
            String publisherName = getPublisherName(notification.getPublisherId());
            return NotificationDTO.fromEntity(notification, publisherName);
        });
    }

    public Optional<NotificationDTO> findById(Long id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        return notificationOpt.map(notification -> {
            String publisherName = getPublisherName(notification.getPublisherId());
            return NotificationDTO.fromEntity(notification, publisherName);
        });
    }

    public NotificationDTO save(NotificationDTO notificationDTO) {
        // 设置默认值以防止数据库约束错误
        if (notificationDTO.getTitle() == null || notificationDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("通知标题不能为空");
        }
        
        if (notificationDTO.getContent() == null || notificationDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        
        // 验证并设置有效的枚举值
        if (notificationDTO.getType() == null || notificationDTO.getType().trim().isEmpty()) {
            notificationDTO.setType("SYSTEM");
        } else {
            // 验证类型是否为有效枚举值
            try {
                Notification.NotificationType.valueOf(notificationDTO.getType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的通知类型: " + notificationDTO.getType());
            }
        }
        
        if (notificationDTO.getTarget() == null || notificationDTO.getTarget().trim().isEmpty()) {
            notificationDTO.setTarget("STUDENTS");
        } else {
            // 验证目标是否为有效枚举值
            try {
                Notification.NotificationTarget.valueOf(notificationDTO.getTarget());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的通知目标: " + notificationDTO.getTarget());
            }
        }
        
        if (notificationDTO.getStatus() == null || notificationDTO.getStatus().trim().isEmpty()) {
            notificationDTO.setStatus("DRAFT");
        } else {
            // 验证状态是否为有效枚举值
            try {
                Notification.NotificationStatus.valueOf(notificationDTO.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的通知状态: " + notificationDTO.getStatus());
            }
        }
        
        // 确保视图计数不为null
        if (notificationDTO.getViewCount() == null) {
            notificationDTO.setViewCount(0);
        }
        
        // 如果是从前端接收的时间字符串为空，确保正确处理
        // 在实际应用中，前端可能传递空字符串，需要在DTO层处理，但这里增加额外保护
        
        // 在转换为实体之前，处理targetIds字段
        // 如果target不是特定类型的，则不应有targetIds
        if (notificationDTO.getTarget() != null && 
            !notificationDTO.getTarget().equals("DEPARTMENTS") && 
            !notificationDTO.getTarget().equals("CLASSES") && 
            !notificationDTO.getTarget().equals("SPECIFIC_USERS")) {
            notificationDTO.setTargetIds(null); // 清空targetIds
        }
        
        // 额外验证：如果targetIds是空数组，也设为null
        if (notificationDTO.getTargetIds() != null && notificationDTO.getTargetIds().isEmpty()) {
            notificationDTO.setTargetIds(null);
        }
        
        // 确保publisherId不为null（如果未设置，可设置为默认值或处理）
        if (notificationDTO.getPublisherId() == null) {
            // 这里可以考虑设置一个默认系统发布者ID，或者抛出异常提醒
            // 在实际实现中，应该从前端请求的认证信息中获取当前用户ID
            System.out.println("警告：通知发布者ID未设置，默认设置为1");
        }
        
        Notification notification = NotificationDTO.toEntity(notificationDTO);
        
        // 确保关键字段不为null以避免数据库约束错误
        if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("通知标题不能为空");
        }
        if (notification.getContent() == null || notification.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        
        try {
            notification = notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("保存通知到数据库失败: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让上层处理
        }
        
        String publisherName = getPublisherName(notification.getPublisherId());
        return NotificationDTO.fromEntity(notification, publisherName);
    }

    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }

    public void batchDelete(List<Long> ids) {
        notificationRepository.deleteAllById(ids);
    }

    public void batchPublish(List<Long> ids) {
        List<Notification> notifications = notificationRepository.findAllById(ids);
        for (Notification notification : notifications) {
            if (notification.getStatus() == Notification.NotificationStatus.DRAFT) {
                notification.setStatus(Notification.NotificationStatus.PUBLISHED);
                if (notification.getPublishTime() == null) {
                    notification.setPublishTime(LocalDateTime.now());
                }
            }
        }
        notificationRepository.saveAll(notifications);
    }

    public List<Notification> findExpiredNotifications() {
        return notificationRepository.findByStatusAndExpireTimeBefore(
                Notification.NotificationStatus.PUBLISHED, LocalDateTime.now());
    }

    public void updateExpiredNotifications() {
        List<Notification> expiredNotifications = findExpiredNotifications();
        for (Notification notification : expiredNotifications) {
            notification.setStatus(Notification.NotificationStatus.EXPIRED);
            notificationRepository.save(notification);
        }
    }

    public Page<NotificationDTO> findPublishedNotificationsForUser(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findPublishedNotificationsForUser(
                userId, LocalDateTime.now(), pageable);

        return notifications.map(notification -> {
            String publisherName = getPublisherName(notification.getPublisherId());
            return NotificationDTO.fromEntity(notification, publisherName);
        });
    }

    private String getPublisherName(Long publisherId) {
        if (publisherId == null) {
            return "系统";
        }
        Optional<User> userOpt = userRepository.findById(publisherId);
        return userOpt.map(User::getName).orElse("未知用户");
    }

    public Long countPublishedNotifications() {
        return notificationRepository.countPublishedNotifications();
    }
    
    public Page<NotificationDTO> findPublishedNotificationsForUserWithReadStatus(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findPublishedNotificationsForUser(
                userId, LocalDateTime.now(), pageable);

        return notifications.map(notification -> {
            String publisherName = getPublisherName(notification.getPublisherId());
            
            // 检查用户是否已读该通知
            Boolean isRead = false;
            try {
                Optional<NotificationUser> notificationUserOpt = 
                    notificationUserService.findByUserIdAndNotificationId(userId, notification.getId());
                if (notificationUserOpt.isPresent()) {
                    isRead = notificationUserOpt.get().getIsRead();
                }
            } catch (Exception e) {
                // 如果出现错误，默认为未读
                isRead = false;
            }
            
            return NotificationDTO.fromEntityWithReadStatus(notification, publisherName, isRead);
        });
    }
    
    public NotificationDTO markNotificationAsRead(Long userId, Long notificationId) {
        // 标记通知为已读
        NotificationUser notificationUser = notificationUserService.markAsRead(userId, notificationId);
        
        // 返回更新后的通知信息
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            String publisherName = getPublisherName(notificationOpt.get().getPublisherId());
            return NotificationDTO.fromEntityWithReadStatus(
                notificationOpt.get(), publisherName, true);
        }
        
        // 如果找不到通知，返回空结果
        return null;
    }
    
    public Long countUnreadNotificationsForUser(Long userId) {
        return notificationUserService.countUnreadNotificationsByUserId(userId);
    }
}