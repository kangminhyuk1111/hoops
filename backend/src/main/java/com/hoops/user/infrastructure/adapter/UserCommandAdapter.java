package com.hoops.user.infrastructure.adapter;

import com.hoops.user.application.port.out.UserCommandPort;
import com.hoops.user.application.port.out.UserQueryPort.UserDetails;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 외부 Context용 사용자 명령 어댑터
 *
 * UserCommandPort를 구현하여 외부 Context에서 사용자를 생성할 수 있게 합니다.
 * 내부적으로 UserRepository를 사용하지만, 외부에는 필요한 기능만 노출합니다.
 */
@Component
@RequiredArgsConstructor
public class UserCommandAdapter implements UserCommandPort {

    private final UserRepository userRepository;

    @Override
    public UserDetails createUser(CreateUserCommand command) {
        User newUser = User.createNew(
                command.email(),
                command.nickname(),
                command.profileImage()
        );
        User savedUser = userRepository.save(newUser);
        return toUserDetails(savedUser);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private UserDetails toUserDetails(User user) {
        return new UserDetails(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getRating(),
                user.getTotalMatches()
        );
    }
}
