package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 중복 참가 예외
 * 이미 참가 중인 경기에 다시 참가 신청할 때 발생합니다
 */
public class AlreadyParticipatingException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "ALREADY_PARTICIPATING";

    public AlreadyParticipatingException(Long matchId, Long userId) {
        super(DEFAULT_ERROR_CODE,
                String.format("이미 참가 중인 경기입니다. (경기 ID: %d, 사용자 ID: %d)", matchId, userId));
    }
}
