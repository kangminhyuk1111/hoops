package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.SignupCommand;

/**
 * Signup Completion Use Case.
 *
 * Completes signup with nickname input.
 */
public interface SignupUseCase {

    /**
     * Completes signup.
     *
     * @param command signup command (temp token, nickname)
     * @return auth result (JWT token, user info)
     */
    AuthResult signup(SignupCommand command);
}
