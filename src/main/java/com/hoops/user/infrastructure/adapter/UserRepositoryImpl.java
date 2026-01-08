package com.hoops.user.infrastructure.adapter;

import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import com.hoops.user.infrastructure.UserEntity;
import com.hoops.user.infrastructure.jpa.JpaUserRepository;
import com.hoops.user.infrastructure.mapper.UserMapper;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryImpl(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

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
}
