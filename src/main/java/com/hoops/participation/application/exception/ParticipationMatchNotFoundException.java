package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 참가 신청 시 경기 조회 실패 예외
 *
 * 참가 신청 시 경기 정보를 찾을 수 없을 때 발생합니다.
 */
public class ParticipationMatchNotFoundException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "PARTICIPATION_MATCH_NOT_FOUND";

    public ParticipationMatchNotFoundException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("참가 신청할 경기를 찾을 수 없습니다. (matchId: %d)", matchId));
    }
}
