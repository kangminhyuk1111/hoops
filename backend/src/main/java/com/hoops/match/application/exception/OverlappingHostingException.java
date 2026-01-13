package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

public class OverlappingHostingException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "OVERLAPPING_HOSTING";

    public OverlappingHostingException(Long existingMatchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("이미 같은 시간대에 호스팅 중인 경기가 있습니다. (기존 경기 ID: %d)", existingMatchId));
    }
}
