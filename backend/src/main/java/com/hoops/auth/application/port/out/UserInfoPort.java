package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.vo.AuthUserInfo;
import com.hoops.auth.application.dto.CreateUserRequest;

import java.util.Optional;

/**
 * User 도메인 통신 Port
 */
public interface UserInfoPort {

    /**
     * 사용자 정보를 조회합니다.
     */
    Optional<AuthUserInfo> findById(Long userId);

    /**
     * 새로운 사용자를 생성합니다.
     */
    AuthUserInfo createUser(CreateUserRequest request);

    /**
     * 닉네임 중복 여부를 확인합니다.
     */
    boolean existsByNickname(String nickname);
}
