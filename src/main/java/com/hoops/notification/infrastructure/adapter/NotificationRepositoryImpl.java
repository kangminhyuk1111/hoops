package com.hoops.notification.infrastructure.adapter;

import com.hoops.notification.domain.Notification;
import com.hoops.notification.domain.repository.NotificationRepository;
import com.hoops.notification.infrastructure.NotificationEntity;
import com.hoops.notification.infrastructure.jpa.JpaNotificationRepository;
import com.hoops.notification.infrastructure.mapper.NotificationMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final JpaNotificationRepository jpaNotificationRepository;

    public NotificationRepositoryImpl(JpaNotificationRepository jpaNotificationRepository) {
        this.jpaNotificationRepository = jpaNotificationRepository;
    }

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
}
