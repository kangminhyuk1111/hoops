package com.hoops.notification.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

    List<NotificationJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n WHERE n.userId = :userId AND n.isRead = false")
    int countUnreadByUserId(@Param("userId") Long userId);
}
