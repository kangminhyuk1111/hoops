package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.SignupCommand;

/**
 * 회원가입 UseCase
 */
public interface SignupUseCase {

    /**
     * 회원가입을 완료합니다.
     */
    AuthResult signup(SignupCommand command);
}
