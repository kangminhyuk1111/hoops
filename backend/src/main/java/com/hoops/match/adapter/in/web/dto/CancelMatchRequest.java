package com.hoops.match.adapter.in.web.dto;

import com.hoops.match.application.port.in.CancelMatchCommand;

/**
 * 경기 취소 요청 DTO
 *
 * HTTP 요청을 CancelMatchCommand로 변환합니다.
 * 취소 사유의 유효성은 서비스 레이어에서 검증합니다.
 */
public record CancelMatchRequest(
        String reason
) {

    /**
     * CancelMatchRequest를 CancelMatchCommand로 변환합니다.
     *
     * @param matchId 경기 ID
     * @param userId JWT Token에서 추출한 사용자 ID
     * @return CancelMatchCommand
     */
    public CancelMatchCommand toCommand(Long matchId, Long userId) {
        return new CancelMatchCommand(matchId, userId, reason);
    }
}
