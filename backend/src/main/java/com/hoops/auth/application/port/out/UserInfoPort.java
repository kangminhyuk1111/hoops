package com.hoops.auth.application.port.out;

import com.hoops.user.domain.User;

import java.util.Optional;

/**
 * Auth Context에서 User 정보에 접근하기 위한 Port
 *
 * Cross-context 통신을 위해 UserRepository 직접 접근 대신 사용합니다.
 */
public interface UserInfoPort {

    Optional<User> findById(Long userId);

    User save(User user);

    boolean existsByNickname(String nickname);
}
