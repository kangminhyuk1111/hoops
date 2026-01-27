package com.hoops.match.application.scheduler;

import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.application.port.out.MatchGeoIndexPort.GeoIndexEntry;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.model.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 시작 시 MySQL의 검색 대상 매치를 Redis Geo Index에 로드
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeoDataInitializer {

    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeGeoData() {
        log.info("Starting Geo Index initialization...");

        matchGeoIndex.clearAll();

        List<Match> searchableMatches = matchRepository.findAllSearchableMatches();

        List<GeoIndexEntry> entries = searchableMatches.stream()
                .map(match -> new GeoIndexEntry(
                        match.getId(),
                        match.getLongitude(),
                        match.getLatitude()
                ))
                .toList();

        matchGeoIndex.addMatchesBulk(entries);

        log.info("Geo Index initialization completed. Loaded {} matches.", entries.size());
    }
}
