package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 참가 정보 미존재 예외
 * 참가 정보를 찾을 수 없을 때 발생합니다
 */
public class ParticipationNotFoundException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "PARTICIPATION_NOT_FOUND";

    public ParticipationNotFoundException(Long participationId) {
        super(DEFAULT_ERROR_CODE,
                String.format("참가 정보를 찾을 수 없습니다. (참가 ID: %d)", participationId));
    }
}
