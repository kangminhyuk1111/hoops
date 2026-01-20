package com.hoops.match.adapter.out;

import com.hoops.match.application.exception.HostNotFoundException;
import com.hoops.match.application.dto.HostInfo;
import com.hoops.match.application.port.out.HostInfoPort;
import com.hoops.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * User Context를 통한 호스트 정보 제공 어댑터
 *
 * Match Context의 HostInfoPort를 구현하여
 * User Context가 제공하는 UserQueryPort를 통해 호스트 정보를 조회합니다.
 */
@Component
@RequiredArgsConstructor
public class UserHostInfoAdapter implements HostInfoPort {

    private final UserQueryPort userQueryPort;

    @Override
    public HostInfo getHostInfo(Long hostId) {
        String nickname = userQueryPort.findNicknameById(hostId)
                .orElseThrow(() -> new HostNotFoundException(hostId));

        return new HostInfo(hostId, nickname);
    }
}
