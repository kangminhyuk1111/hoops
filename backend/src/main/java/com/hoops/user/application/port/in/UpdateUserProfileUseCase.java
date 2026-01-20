package com.hoops.user.application.port.in;

import com.hoops.user.domain.model.User;

/**
 * 사용자 프로필 수정 UseCase
 */
public interface UpdateUserProfileUseCase {

    /**
     * 사용자 프로필을 수정합니다.
     *
     * @param command 프로필 수정 커맨드
     * @return 수정된 사용자 정보
     * @throws com.hoops.user.application.exception.UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws com.hoops.user.application.exception.DuplicateNicknameException 중복된 닉네임인 경우
     */
    User updateUserProfile(UpdateUserProfileCommand command);
}
