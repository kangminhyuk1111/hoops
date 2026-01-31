package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class InvalidSortTypeException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_SORT_TYPE";

    public InvalidSortTypeException(String sortType) {
        super(DEFAULT_ERROR_CODE,
                String.format("잘못된 정렬 타입입니다: %s (허용: DISTANCE, URGENCY)", sortType));
    }
}
