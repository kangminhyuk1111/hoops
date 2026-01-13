package com.hoops.user.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 닉네임 중복 예외
 * 이미 사용 중인 닉네임으로 회원가입을 시도할 때 발생합니다
 */
public class DuplicateNicknameException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "DUPLICATE_NICKNAME";

    public DuplicateNicknameException(String nickname) {
        super(DEFAULT_ERROR_CODE,
                String.format("이미 사용 중인 닉네임입니다. (닉네임: %s)", nickname));
    }
}
