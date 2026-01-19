package com.hoops.auth.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * Exception thrown when user is not found for authentication.
 */
public class UserNotFoundForAuthException extends DomainException {

    private static final String ERROR_CODE = "USER_NOT_FOUND_FOR_AUTH";

    public UserNotFoundForAuthException(Long authAccountId, Long userId) {
        super(ERROR_CODE,
            String.format("User not found for auth account: authAccountId=%d, userId=%d",
                authAccountId, userId));
    }
}
