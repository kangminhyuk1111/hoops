package com.hoops.auth.domain.port;

import com.hoops.user.domain.User;

import java.util.Optional;

/**
 * Port for accessing User information from Auth context.
 *
 * Used for cross-context communication instead of direct UserRepository access.
 */
public interface UserInfoPort {

    Optional<User> findById(Long userId);

    User save(User user);

    boolean existsByNickname(String nickname);
}
