package com.hoops.match.application.port.out;

/**
 * 호스트 정보 제공 포트
 *
 * Match 도메인에서 호스트 정보를 조회하기 위한 아웃바운드 포트입니다.
 * User 도메인에 직접 의존하지 않고 이 인터페이스를 통해 필요한 정보만 조회합니다.
 *
 * @see HostInfo
 */
public interface HostInfoProvider {

    /**
     * 호스트 정보를 조회합니다.
     *
     * @param hostId 호스트 사용자 ID
     * @return 호스트 정보
     * @throws com.hoops.match.application.exception.HostNotFoundException 호스트를 찾을 수 없는 경우
     */
    HostInfo getHostInfo(Long hostId);
}
