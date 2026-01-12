package com.hoops.user.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 사용자 조회 실패 예외
 * 요청한 사용자 ID로 사용자를 찾을 수 없을 때 발생합니다
 */
public class UserNotFoundException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(Long userId) {
        super(DEFAULT_ERROR_CODE,
                String.format("사용자를 찾을 수 없습니다. (userId: %d)", userId));
    }
}
