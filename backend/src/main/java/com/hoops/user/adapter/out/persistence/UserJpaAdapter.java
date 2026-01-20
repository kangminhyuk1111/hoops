package com.hoops.user.adapter.out.persistence;

import com.hoops.user.domain.model.User;
import com.hoops.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public User save(User user) {
        UserJpaEntity entity;
        if (user.getId() != null) {
            entity = springDataUserRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
            entity.update(user.getNickname(), user.getProfileImage(), user.getRating(), user.getTotalMatches());
        } else {
            entity = UserMapper.toEntity(user);
        }
        UserJpaEntity savedEntity = springDataUserRepository.save(entity);
        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAllByIds(Set<Long> ids) {
        return springDataUserRepository.findAllById(ids).stream()
                .map(UserMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return springDataUserRepository.existsByNickname(nickname);
    }
}
