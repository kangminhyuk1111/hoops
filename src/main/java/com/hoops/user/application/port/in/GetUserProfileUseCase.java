package com.hoops.user.application.port.in;

import com.hoops.user.domain.User;

/**
 * 사용자 프로필 조회 UseCase
 */
public interface GetUserProfileUseCase {

    /**
     * 사용자 프로필을 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보
     * @throws com.hoops.user.application.exception.UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    User getUserProfile(Long userId);
}
