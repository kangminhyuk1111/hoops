package com.hoops.user.infrastructure.jpa;

import com.hoops.user.infrastructure.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
}
