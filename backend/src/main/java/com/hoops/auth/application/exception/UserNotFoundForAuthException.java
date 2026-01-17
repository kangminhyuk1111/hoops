package com.hoops.auth.application.exception;

import com.hoops.common.exception.DomainException;

public class UserNotFoundForAuthException extends DomainException {

    private static final String ERROR_CODE = "USER_NOT_FOUND_FOR_AUTH";

    public UserNotFoundForAuthException(Long authAccountId, Long userId) {
        super(ERROR_CODE,
            String.format("인증 계정에 연결된 사용자를 찾을 수 없습니다. authAccountId=%d, userId=%d",
                authAccountId, userId));
    }
}
