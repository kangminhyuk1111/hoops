package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 취소 동시성 충돌 예외
 * 낙관적 락 재시도 초과 시 발생합니다
 */
public class CancellationConflictException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "CANCELLATION_CONFLICT";

    public CancellationConflictException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("동시에 다른 요청이 처리되어 충돌이 발생했습니다. 다시 시도해주세요. (matchId: %d)",
                        matchId));
    }
}
