package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class InvalidMaxParticipantsException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_MAX_PARTICIPANTS";

    public InvalidMaxParticipantsException(Integer maxParticipants) {
        super(DEFAULT_ERROR_CODE,
                String.format("최대 참가 인원은 최소 4명 이상이어야 합니다. (입력값: %d)", maxParticipants));
    }
}
