package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class InvalidSearchDistanceException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_SEARCH_DISTANCE";

    public InvalidSearchDistanceException(int distance) {
        super(DEFAULT_ERROR_CODE,
                String.format("허용되지 않는 검색 거리입니다: %dkm (허용: 1, 3, 5, 10km)", distance));
    }
}
