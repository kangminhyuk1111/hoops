package com.hoops.user.domain;

import java.math.BigDecimal;

public class User {

    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private BigDecimal rating;
    private Integer totalMatches;

    public User(Long id, String email, String nickname, String profileImage, BigDecimal rating,
            Integer totalMatches) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.rating = rating;
        this.totalMatches = totalMatches;
    }

    // Domain Logic - None strictly required yet as per YAGNI

    // Getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public Integer getTotalMatches() {
        return totalMatches;
    }
}
