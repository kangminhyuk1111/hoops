package com.hoops.user.application.port.out;

import java.util.Optional;

/**
 * 외부 Context에 제공하는 사용자 조회 포트
 *
 * User Context 외부에서 사용자 정보를 조회할 때 사용합니다.
 * 내부 Repository를 직접 노출하지 않고, 필요한 정보만 제공합니다.
 */
public interface UserQueryPort {

    /**
     * 사용자 닉네임을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 닉네임 (사용자가 없으면 empty)
     */
    Optional<String> findNicknameById(Long userId);
}
