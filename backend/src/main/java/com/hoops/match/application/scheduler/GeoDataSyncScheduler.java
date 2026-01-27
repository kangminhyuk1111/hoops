package com.hoops.match.application.scheduler;

import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.application.port.out.MatchGeoIndexPort.GeoIndexEntry;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.model.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MySQL과 Redis Geo Index 간 정합성 체크 및 복구 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeoDataSyncScheduler {

    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;

    @Scheduled(cron = "0 0 * * * *") // 매시간 실행
    @SchedulerLock(name = "GeoDataSyncScheduler", lockAtMostFor = "PT30M", lockAtLeastFor = "PT5M")
    public void syncGeoData() {
        log.info("Starting Geo Index sync check...");

        // 메모리 효율: ID만 조회
        Set<Long> mysqlIds = new HashSet<>(matchRepository.findSearchableMatchIds());
        Set<Long> redisIds = new HashSet<>(matchGeoIndex.findAllMatchIds());

        // MySQL에는 있지만 Redis에는 없는 매치 추가
        List<Long> missingIds = mysqlIds.stream()
                .filter(id -> !redisIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            // 누락된 매치만 DB에서 조회하여 추가
            List<Match> missingMatches = matchRepository.findAllByIds(missingIds);
            List<GeoIndexEntry> entries = missingMatches.stream()
                    .map(match -> new GeoIndexEntry(
                            match.getId(),
                            match.getLongitude(),
                            match.getLatitude()
                    ))
                    .toList();

            matchGeoIndex.addMatchesBulk(entries);

            for (Long matchId : missingIds) {
                log.warn("Added missing match to Redis: matchId={}", matchId);
            }
        }

        // Redis에는 있지만 MySQL에는 없는 매치 제거
        int removedCount = 0;
        for (Long redisId : redisIds) {
            if (!mysqlIds.contains(redisId)) {
                matchGeoIndex.removeMatch(redisId);
                removedCount++;
                log.warn("Removed orphan match from Redis: matchId={}", redisId);
            }
        }

        log.info("Geo Index sync completed. Added: {}, Removed: {}", missingIds.size(), removedCount);
    }
}
