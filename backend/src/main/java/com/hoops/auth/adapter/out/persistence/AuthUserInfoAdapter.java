package com.hoops.auth.adapter.out.persistence;

import com.hoops.auth.application.port.out.UserInfoPort;
import com.hoops.auth.domain.vo.AuthUserInfo;
import com.hoops.auth.application.dto.CreateUserCommand;
import com.hoops.user.application.port.out.UserCommandPort;
import com.hoops.user.application.port.out.UserQueryPort;
import com.hoops.user.application.port.out.UserQueryPort.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUserInfoAdapter implements UserInfoPort {

    private final UserQueryPort userQueryPort;
    private final UserCommandPort userCommandPort;

    @Override
    public Optional<AuthUserInfo> findById(Long userId) {
        return userQueryPort.findUserDetailsById(userId)
                .map(this::toAuthUserInfo);
    }

    @Override
    public AuthUserInfo createUser(CreateUserCommand request) {
        UserCommandPort.CreateUserCommand userCommand = UserCommandPort.CreateUserCommand.of(
                request.email(),
                request.nickname(),
                request.profileImage()
        );
        UserDetails userDetails = userCommandPort.createUser(userCommand);
        return toAuthUserInfo(userDetails);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userCommandPort.existsByNickname(nickname);
    }

    private AuthUserInfo toAuthUserInfo(UserDetails userDetails) {
        return AuthUserInfo.of(
                userDetails.userId(),
                userDetails.nickname(),
                userDetails.email(),
                userDetails.profileImage()
        );
    }
}
