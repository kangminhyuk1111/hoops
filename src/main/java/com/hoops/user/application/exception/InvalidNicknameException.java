package com.hoops.user.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 닉네임 형식 오류 예외
 * 닉네임이 지정된 형식(2~20자)에 맞지 않을 때 발생합니다
 */
public class InvalidNicknameException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_NICKNAME";

    public InvalidNicknameException(String nickname) {
        super(DEFAULT_ERROR_CODE,
                String.format("닉네임은 2~20자 사이여야 합니다. (입력값: %s)", nickname));
    }

    public InvalidNicknameException(String nickname, String reason) {
        super(DEFAULT_ERROR_CODE,
                String.format("유효하지 않은 닉네임입니다. (입력값: %s, 사유: %s)", nickname, reason));
    }
}
