package com.college.repository;

import com.college.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByStatus(Notification.NotificationStatus status, Pageable pageable);

    Page<Notification> findByType(Notification.NotificationType type, Pageable pageable);

    Page<Notification> findByTarget(Notification.NotificationTarget target, Pageable pageable);

    List<Notification> findByStatusAndExpireTimeBefore(Notification.NotificationStatus status, LocalDateTime currentTime);

    @Query("SELECT n FROM Notification n WHERE " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:target IS NULL OR n.target = :target) AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Notification> findByFilters(@Param("type") Notification.NotificationType type,
                                     @Param("status") Notification.NotificationStatus status,
                                     @Param("target") Notification.NotificationTarget target,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE " +
           "n.status = 'PUBLISHED' AND " +
           "n.publishTime <= :currentTime AND " +
           "n.expireTime >= :currentTime AND " +
           "(:userId IS NULL OR n.targetIds IS NULL OR CONCAT(',', n.targetIds, ',') LIKE CONCAT('%,', :userId, ',%'))")
    Page<Notification> findPublishedNotificationsForUser(@Param("userId") Long userId, 
                                                        @Param("currentTime") LocalDateTime currentTime,
                                                        Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'PUBLISHED'")
    Long countPublishedNotifications();
}