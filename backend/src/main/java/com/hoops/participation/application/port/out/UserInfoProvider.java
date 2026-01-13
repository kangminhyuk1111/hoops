package com.hoops.participation.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 사용자 정보 제공 포트
 *
 * Participation Context에서 사용자 정보를 조회하기 위한 아웃바운드 포트입니다.
 * User Context에 직접 의존하지 않고 이 인터페이스를 통해 필요한 정보만 조회합니다.
 */
public interface UserInfoProvider {

    /**
     * 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 정보 (사용자가 없으면 empty)
     */
    Optional<UserInfo> getUserInfo(Long userId);

    /**
     * 여러 사용자의 정보를 일괄 조회합니다.
     *
     * @param userIds 사용자 ID 목록
     * @return 사용자 ID를 키로, 사용자 정보를 값으로 하는 맵
     */
    Map<Long, UserInfo> getUserInfoByIds(List<Long> userIds);
}
