package com.hoops.user.application.port.out;

import com.hoops.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * User 영속성 포트 인터페이스
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAllByIds(Set<Long> ids);

    boolean existsByNickname(String nickname);
}
