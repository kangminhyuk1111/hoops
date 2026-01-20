package com.hoops.match.application.dto;

/**
 * 경기 호스트 정보
 *
 * Match 도메인에서 필요한 호스트 정보만 정의합니다.
 * User 도메인에 직접 의존하지 않고 필요한 정보만 전달받습니다.
 */
public record HostInfo(
        Long hostId,
        String nickname
) {
}
