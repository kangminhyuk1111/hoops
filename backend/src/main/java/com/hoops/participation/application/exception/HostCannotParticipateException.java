package com.hoops.participation.application.exception;

import com.hoops.common.exception.ApplicationException;

/**
 * 호스트 참가 불가 예외
 * 경기 호스트가 자신의 경기에 참가 신청할 때 발생합니다
 */
public class HostCannotParticipateException extends ApplicationException {

    private static final String DEFAULT_ERROR_CODE = "HOST_CANNOT_PARTICIPATE";

    public HostCannotParticipateException(Long matchId, Long userId) {
        super(DEFAULT_ERROR_CODE,
                String.format("호스트는 자신의 경기에 참가 신청할 수 없습니다. (경기 ID: %d, 사용자 ID: %d)", matchId, userId));
    }
}
