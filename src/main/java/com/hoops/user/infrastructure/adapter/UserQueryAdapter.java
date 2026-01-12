package com.hoops.user.infrastructure.adapter;

import com.hoops.user.application.port.out.UserQueryPort;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 외부 Context용 사용자 조회 어댑터
 *
 * UserQueryPort를 구현하여 외부 Context에서 사용자 정보를 조회할 수 있게 합니다.
 * 내부적으로 UserRepository를 사용하지만, 외부에는 필요한 정보만 노출합니다.
 */
@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserRepository userRepository;

    @Override
    public Optional<String> findNicknameById(Long userId) {
        return userRepository.findById(userId)
                .map(User::getNickname);
    }

    @Override
    public Optional<UserDetails> findUserDetailsById(Long userId) {
        return userRepository.findById(userId)
                .map(this::toUserDetails);
    }

    @Override
    public Map<Long, UserDetails> findUserDetailsByIds(List<Long> userIds) {
        return userRepository.findAllByIds(new java.util.HashSet<>(userIds)).stream()
                .map(this::toUserDetails)
                .collect(Collectors.toMap(UserDetails::userId, Function.identity()));
    }

    private UserDetails toUserDetails(User user) {
        return new UserDetails(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getRating(),
                user.getTotalMatches()
        );
    }
}
