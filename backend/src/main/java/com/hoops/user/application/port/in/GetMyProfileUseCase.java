package com.hoops.user.application.port.in;

import com.hoops.user.domain.User;

/**
 * 내 프로필 조회 UseCase
 */
public interface GetMyProfileUseCase {

    User getMyProfile(Long userId);
}
