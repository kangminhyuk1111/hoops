package com.hoops.participation.application.port.out;

import java.math.BigDecimal;

/**
 * 참가자 상세 조회를 위한 사용자 정보
 *
 * Participation Context에서 사용자 정보를 표현하기 위한 레코드입니다.
 * User Context의 내부 도메인에 직접 의존하지 않고 필요한 정보만 포함합니다.
 */
public record UserInfo(
        Long userId,
        String nickname,
        String profileImage,
        BigDecimal rating,
        Integer totalMatches
) {}
