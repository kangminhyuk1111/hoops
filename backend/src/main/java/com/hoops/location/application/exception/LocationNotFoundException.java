package com.hoops.location.application.exception;

import com.hoops.common.exception.DomainException;

public class LocationNotFoundException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "LOCATION_NOT_FOUND";

    public LocationNotFoundException(Long locationId) {
        super(DEFAULT_ERROR_CODE, String.format("해당 장소를 찾을 수 없습니다. (ID: %s)", locationId));
    }
}
