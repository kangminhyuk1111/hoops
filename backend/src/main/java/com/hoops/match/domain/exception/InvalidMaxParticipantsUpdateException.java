package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class InvalidMaxParticipantsUpdateException extends DomainException {

    private static final String ERROR_CODE = "INVALID_MAX_PARTICIPANTS_UPDATE";

    public InvalidMaxParticipantsUpdateException(Long matchId, int currentParticipants, int requestedMax) {
        super(ERROR_CODE,
                String.format("Cannot reduce max participants below current count. (matchId: %d, current: %d, requested: %d)",
                        matchId, currentParticipants, requestedMax));
    }
}
