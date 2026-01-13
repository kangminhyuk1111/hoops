package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.AuthResult;

/**
 * 테스트 로그인 유스케이스
 *
 * 개발/테스트 환경에서 카카오 OAuth 없이 로그인할 수 있도록 합니다.
 */
public interface TestLoginUseCase {

    /**
     * 테스트 계정으로 로그인합니다.
     *
     * @return 인증 결과
     */
    AuthResult testLogin();
}
