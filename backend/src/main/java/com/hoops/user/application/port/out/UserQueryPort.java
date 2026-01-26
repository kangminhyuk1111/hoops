package com.hoops.user.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 외부 Context에 제공하는 사용자 조회 포트
 *
 * User Context 외부에서 사용자 정보를 조회할 때 사용합니다.
 * 내부 Repository를 직접 노출하지 않고, 필요한 정보만 제공합니다.
 */
public interface UserQueryPort {

    /**
     * 사용자 닉네임을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 닉네임 (사용자가 없으면 empty)
     */
    Optional<String> getNicknameByUserId(Long userId);

    /**
     * 사용자 상세 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 상세 정보 (사용자가 없으면 empty)
     */
    Optional<UserDetails> getUserDetails(Long userId);

    /**
     * 여러 사용자의 상세 정보를 일괄 조회합니다.
     *
     * @param userIds 사용자 ID 목록
     * @return 사용자 ID를 키로, 상세 정보를 값으로 하는 맵
     */
    Map<Long, UserDetails> getBulkUserDetails(List<Long> userIds);

    /**
     * 외부 Context에 제공되는 사용자 상세 정보
     */
    record UserDetails(
            Long userId,
            String email,
            String nickname,
            String profileImage,
            BigDecimal rating,
            Integer totalMatches
    ) {}
}
