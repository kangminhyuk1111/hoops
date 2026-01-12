package com.hoops.participation.infrastructure.adapter;

import com.hoops.participation.application.port.out.UserInfo;
import com.hoops.participation.application.port.out.UserInfoProvider;
import com.hoops.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User Context를 통한 사용자 정보 제공 어댑터
 *
 * Participation Context의 UserInfoProvider 포트를 구현하여
 * User Context가 제공하는 UserQueryPort를 통해 사용자 정보를 조회합니다.
 */
@Component
@RequiredArgsConstructor
public class UserInfoAdapter implements UserInfoProvider {

    private final UserQueryPort userQueryPort;

    @Override
    public Optional<UserInfo> getUserInfo(Long userId) {
        return userQueryPort.findUserDetailsById(userId)
                .map(this::toUserInfo);
    }

    @Override
    public Map<Long, UserInfo> getUserInfoByIds(List<Long> userIds) {
        return userQueryPort.findUserDetailsByIds(userIds).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toUserInfo(entry.getValue())
                ));
    }

    private UserInfo toUserInfo(UserQueryPort.UserDetails details) {
        return new UserInfo(
                details.userId(),
                details.nickname(),
                details.profileImage(),
                details.rating(),
                details.totalMatches()
        );
    }
}
