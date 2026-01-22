package com.hoops.auth.domain.vo;

import com.hoops.auth.domain.exception.InvalidNicknameException;

/**
 * 닉네임 Value Object
 * 2~20자 사이의 유효한 닉네임을 보장합니다
 */
public record Nickname(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 20;

    public Nickname {
        validate(value);
    }

    public static Nickname of(String value) {
        return new Nickname(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidNicknameException(value, "Nickname is required");
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
            throw new InvalidNicknameException(value);
        }
    }
}
