package com.hoops.participation.application.exception;

import com.hoops.common.exception.ApplicationException;

/**
 * 본인의 참가가 아닌 예외
 * 다른 사용자의 참가를 취소하려 할 때 발생합니다
 */
public class NotParticipantException extends ApplicationException {

    private static final String DEFAULT_ERROR_CODE = "NOT_PARTICIPANT";

    public NotParticipantException(Long participationId, Long userId) {
        super(DEFAULT_ERROR_CODE,
                String.format("본인의 참가만 취소할 수 있습니다. (participationId: %d, userId: %d)",
                        participationId, userId));
    }
}
