package com.hoops.user.domain.repository;

import com.hoops.user.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAllByIds(Set<Long> ids);

    boolean existsByNickname(String nickname);
}
