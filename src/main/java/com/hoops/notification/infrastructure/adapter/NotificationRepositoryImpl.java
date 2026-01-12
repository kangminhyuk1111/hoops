package com.hoops.notification.infrastructure.adapter;

import com.hoops.notification.domain.Notification;
import com.hoops.notification.domain.repository.NotificationRepository;
import com.hoops.notification.infrastructure.NotificationEntity;
import com.hoops.notification.infrastructure.jpa.JpaNotificationRepository;
import com.hoops.notification.infrastructure.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = NotificationMapper.toEntity(notification);
        NotificationEntity savedEntity = jpaNotificationRepository.save(entity);
        return NotificationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaNotificationRepository.findById(id).map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return jpaNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }

    @Override
    public int countUnreadByUserId(Long userId) {
        return jpaNotificationRepository.countUnreadByUserId(userId);
    }
}
