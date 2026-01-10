package com.hoops.match.adapter.out.adapter;

import com.hoops.match.application.exception.HostNotFoundException;
import com.hoops.match.application.port.out.HostInfo;
import com.hoops.match.application.port.out.HostInfoProvider;
import com.hoops.user.application.port.out.UserQueryPort;
import org.springframework.stereotype.Component;

/**
 * User Context를 통한 호스트 정보 제공 어댑터
 *
 * Match Context의 HostInfoProvider 포트를 구현하여
 * User Context가 제공하는 UserQueryPort를 통해 호스트 정보를 조회합니다.
 *
 * User Context의 내부 구현(Repository)에 직접 의존하지 않고,
 * User Context가 외부에 제공하는 Port만 사용합니다.
 */
@Component
public class UserHostInfoAdapter implements HostInfoProvider {

    private final UserQueryPort userQueryPort;

    public UserHostInfoAdapter(UserQueryPort userQueryPort) {
        this.userQueryPort = userQueryPort;
    }

    @Override
    public HostInfo getHostInfo(Long hostId) {
        String nickname = userQueryPort.findNicknameById(hostId)
                .orElseThrow(() -> new HostNotFoundException(hostId));

        return new HostInfo(hostId, nickname);
    }
}
