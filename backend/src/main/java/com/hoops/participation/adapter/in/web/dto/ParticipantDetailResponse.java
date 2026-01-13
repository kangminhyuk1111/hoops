package com.hoops.participation.adapter.in.web.dto;

import com.hoops.participation.application.port.out.UserInfo;
import com.hoops.participation.domain.Participation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 참가자 상세 정보 응답 DTO
 *
 * 참가 정보와 함께 사용자의 상세 정보(닉네임, 프로필 이미지 등)를 포함합니다.
 */
public record ParticipantDetailResponse(
        Long id,
        Long matchId,
        Long userId,
        String nickname,
        String profileImage,
        BigDecimal rating,
        Integer totalMatches,
        String status,
        LocalDateTime joinedAt
) {
    public static ParticipantDetailResponse of(Participation participation, UserInfo userInfo) {
        return new ParticipantDetailResponse(
                participation.getId(),
                participation.getMatchId(),
                participation.getUserId(),
                userInfo != null ? userInfo.nickname() : null,
                userInfo != null ? userInfo.profileImage() : null,
                userInfo != null ? userInfo.rating() : null,
                userInfo != null ? userInfo.totalMatches() : null,
                participation.getStatus().name(),
                participation.getJoinedAt()
        );
    }
}
