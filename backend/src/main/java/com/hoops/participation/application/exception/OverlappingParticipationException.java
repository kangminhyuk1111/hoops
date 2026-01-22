package com.hoops.participation.application.exception;

import com.hoops.common.exception.ApplicationException;

public class OverlappingParticipationException extends ApplicationException {

    private static final String DEFAULT_ERROR_CODE = "OVERLAPPING_PARTICIPATION";

    public OverlappingParticipationException(Long matchId, Long existingMatchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("이미 같은 시간대에 참가 중인 경기가 있습니다. (신청 경기 ID: %d, 기존 경기 ID: %d)",
                        matchId, existingMatchId));
    }
}
