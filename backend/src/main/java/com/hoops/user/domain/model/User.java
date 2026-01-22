package com.hoops.user.domain.model;

import com.hoops.user.domain.exception.InvalidNicknameException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class User {

    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 20;

    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImage;
    private final BigDecimal rating;
    private final Integer totalMatches;

    private User(Long id, String email, String nickname, String profileImage,
                 BigDecimal rating, Integer totalMatches) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.rating = rating;
        this.totalMatches = totalMatches;
    }

    /**
     * 새로운 사용자를 생성한다.
     * 도메인 불변식을 검증한다.
     */
    public static User createNew(String email, String nickname, String profileImage) {
        validateEmail(email);
        validateNickname(nickname);
        return new User(null, email, nickname, profileImage, BigDecimal.ZERO, 0);
    }

    /**
     * 데이터베이스에서 복원할 때 사용한다.
     * 이미 검증된 데이터이므로 검증을 생략한다.
     */
    public static User reconstitute(Long id, String email, String nickname,
                                     String profileImage, BigDecimal rating, Integer totalMatches) {
        return new User(id, email, nickname, profileImage, rating, totalMatches);
    }

    /**
     * 프로필을 업데이트한다.
     * 닉네임이 변경되는 경우 유효성을 검증한다.
     */
    public User updateProfile(String nickname, String profileImage) {
        String newNickname = nickname != null ? nickname : this.nickname;
        String newProfileImage = profileImage != null ? profileImage : this.profileImage;

        // 닉네임이 변경되는 경우에만 검증
        if (nickname != null && !nickname.equals(this.nickname)) {
            validateNickname(nickname);
        }

        return new User(this.id, this.email, newNickname, newProfileImage,
                this.rating, this.totalMatches);
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new InvalidNicknameException(nickname, "Nickname is required");
        }

        String trimmedNickname = nickname.trim();
        if (trimmedNickname.length() < MIN_NICKNAME_LENGTH ||
                trimmedNickname.length() > MAX_NICKNAME_LENGTH) {
            throw new InvalidNicknameException(nickname);
        }
    }
}
