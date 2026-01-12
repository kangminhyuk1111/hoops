package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 호스트가 아닌 사용자가 승인/거절 시도 시 발생하는 예외
 */
public class NotHostException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "NOT_HOST";

    public NotHostException(Long matchId, Long userId) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기 호스트만 참가 승인/거절이 가능합니다. (matchId: %d, userId: %d)",
                        matchId, userId));
    }
}
