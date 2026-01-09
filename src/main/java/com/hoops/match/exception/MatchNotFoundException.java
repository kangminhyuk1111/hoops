package com.hoops.match.exception;

import com.hoops.common.exception.BusinessException;

public class MatchNotFoundException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_NOT_FOUND";

    public MatchNotFoundException(Long matchId) {
        super(DEFAULT_ERROR_CODE, String.format("해당 매치를 찾을 수 없습니다. (ID: %s)", matchId));
    }
}