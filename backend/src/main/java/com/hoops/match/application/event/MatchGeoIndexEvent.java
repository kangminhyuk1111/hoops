package com.hoops.match.application.event;

/**
 * 매치 Geo Index 관련 이벤트 마커 인터페이스
 * 트랜잭션 커밋 후 Redis Geo Index 업데이트를 위해 사용
 */
public sealed interface MatchGeoIndexEvent
        permits MatchCreatedEvent, MatchRemovedFromGeoIndexEvent {
}
