package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.AuthResult;

/**
 * 회원가입 완료 Use Case
 *
 * 닉네임을 입력받아 회원가입을 완료합니다.
 */
public interface SignupUseCase {

    /**
     * 회원가입을 완료합니다.
     *
     * @param command 회원가입 커맨드 (임시토큰, 닉네임)
     * @return 인증 결과 (JWT 토큰, 사용자 정보)
     */
    AuthResult signup(SignupCommand command);
}
