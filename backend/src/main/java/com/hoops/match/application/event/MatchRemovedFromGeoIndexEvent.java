package com.hoops.match.application.event;

/**
 * 매치 Geo Index 제거 이벤트
 * 트랜잭션 커밋 후 Redis Geo Index에서 매치 제거
 * 매치 취소, 시작 등으로 검색 대상에서 제외될 때 발생
 */
public record MatchRemovedFromGeoIndexEvent(
        Long matchId
) implements MatchGeoIndexEvent {
}
