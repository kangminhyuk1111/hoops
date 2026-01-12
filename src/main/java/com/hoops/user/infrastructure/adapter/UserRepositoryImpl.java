package com.hoops.user.infrastructure.adapter;

import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import com.hoops.user.infrastructure.UserEntity;
import com.hoops.user.infrastructure.jpa.JpaUserRepository;
import com.hoops.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAllByIds(Set<Long> ids) {
        return jpaUserRepository.findAllById(ids).stream()
                .map(UserMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaUserRepository.existsByNickname(nickname);
    }
}
