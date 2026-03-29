package com.college.dto;

import com.college.model.Notification;
import com.college.util.CustomLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationDTO {

    private Long id;
    private String title;
    private String content;
    private String type;
    private String target;
    private List<Long> targetIds;
    private String status;
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime publishTime;
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime expireTime;
    private Long publisherId;
    private String publisherName;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isRead;  // 用户是否已读

    public static NotificationDTO fromEntity(Notification notification, String publisherName) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType().name());
        dto.setTarget(notification.getTarget().name());
        dto.setStatus(notification.getStatus().name());
        dto.setPublishTime(notification.getPublishTime());
        dto.setExpireTime(notification.getExpireTime());
        dto.setPublisherId(notification.getPublisherId());
        dto.setPublisherName(publisherName);
        dto.setViewCount(notification.getViewCount());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        
        // 默认情况下，通知未被阅读
        dto.setIsRead(false);

        // 解析targetIds字符串为列表
        if (notification.getTargetIds() != null && !notification.getTargetIds().isEmpty()) {
            String[] ids = notification.getTargetIds().split(",");
            List<Long> idList = java.util.Arrays.stream(ids)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(java.util.stream.Collectors.toList());
            dto.setTargetIds(idList);
        }

        return dto;
    }
    
    public static NotificationDTO fromEntityWithReadStatus(Notification notification, String publisherName, Boolean isRead) {
        NotificationDTO dto = fromEntity(notification, publisherName);
        dto.setIsRead(isRead != null ? isRead : false);
        return dto;
    }

    public static Notification toEntity(NotificationDTO dto) {
        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        
        // 验证并设置枚举类型，添加错误处理
        try {
            notification.setType(Notification.NotificationType.valueOf(dto.getType()));
        } catch (IllegalArgumentException e) {
            System.err.println("无效的通知类型: " + dto.getType() + ", 使用默认值 SYSTEM");
            notification.setType(Notification.NotificationType.SYSTEM);
        }
        
        try {
            notification.setTarget(Notification.NotificationTarget.valueOf(dto.getTarget()));
        } catch (IllegalArgumentException e) {
            System.err.println("无效的通知目标: " + dto.getTarget() + ", 使用默认值 STUDENTS");
            notification.setTarget(Notification.NotificationTarget.STUDENTS);
        }
        
        try {
            notification.setStatus(Notification.NotificationStatus.valueOf(dto.getStatus()));
        } catch (IllegalArgumentException e) {
            System.err.println("无效的通知状态: " + dto.getStatus() + ", 使用默认值 DRAFT");
            notification.setStatus(Notification.NotificationStatus.DRAFT);
        }
        
        notification.setPublishTime(dto.getPublishTime());
        notification.setExpireTime(dto.getExpireTime());
        notification.setPublisherId(dto.getPublisherId());
        notification.setViewCount(dto.getViewCount());

        // 将targetIds列表转换为逗号分隔的字符串
        if (dto.getTargetIds() != null && !dto.getTargetIds().isEmpty()) {
            try {
                String targetIdsStr = String.join(",", dto.getTargetIds().stream()
                        .map(String::valueOf)
                        .toArray(String[]::new));
                notification.setTargetIds(targetIdsStr);
            } catch (Exception e) {
                // 如果targetIds转换失败，记录错误但不中断整个过程
                System.err.println("targetIds转换失败: " + e.getMessage());
                notification.setTargetIds(null);
            }
        }

        return notification;
    }
}