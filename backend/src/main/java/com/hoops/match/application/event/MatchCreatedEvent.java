package com.hoops.match.application.event;

import java.math.BigDecimal;

/**
 * 매치 생성 이벤트
 * 트랜잭션 커밋 후 Redis Geo Index에 매치 추가
 */
public record MatchCreatedEvent(
        Long matchId,
        BigDecimal longitude,
        BigDecimal latitude
) implements MatchGeoIndexEvent {
}
