package com.hoops.notification.adapter.out.persistence;

import com.hoops.notification.domain.model.Notification;
import com.hoops.notification.application.port.out.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationJpaAdapter implements NotificationRepositoryPort {

    private final SpringDataNotificationRepository springDataNotificationRepository;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = NotificationMapper.toEntity(notification);
        NotificationJpaEntity savedEntity = springDataNotificationRepository.save(entity);
        return NotificationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return springDataNotificationRepository.findById(id).map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return springDataNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }

    @Override
    public int countUnreadByUserId(Long userId) {
        return springDataNotificationRepository.countUnreadByUserId(userId);
    }
}
