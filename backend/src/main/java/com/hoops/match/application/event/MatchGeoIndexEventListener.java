package com.hoops.match.application.event;

import com.hoops.match.application.port.out.MatchGeoIndexPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 매치 Geo Index 이벤트 리스너
 * 트랜잭션 커밋 후 Redis Geo Index 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchGeoIndexEventListener {

    private final MatchGeoIndexPort matchGeoIndex;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchCreated(MatchCreatedEvent event) {
        log.debug("Handling MatchCreatedEvent: matchId={}", event.matchId());
        matchGeoIndex.addMatch(event.matchId(), event.longitude(), event.latitude());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchRemovedFromGeoIndex(MatchRemovedFromGeoIndexEvent event) {
        log.debug("Handling MatchRemovedFromGeoIndexEvent: matchId={}", event.matchId());
        matchGeoIndex.removeMatch(event.matchId());
    }
}
