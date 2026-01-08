package com.hoops.user.infrastructure;

import com.hoops.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(nullable = false)
    private Integer totalMatches;

    protected UserEntity() {
    }

    public UserEntity(String email, String nickname, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.rating = BigDecimal.ZERO;
        this.totalMatches = 0;
    }

    // Domain Logic - Removed as per "Create only when needed" principle

    public void update(String nickname, String profileImage, BigDecimal rating,
            Integer totalMatches) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.rating = rating;
        this.totalMatches = totalMatches;
    }

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
