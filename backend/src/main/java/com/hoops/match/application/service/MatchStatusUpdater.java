package com.hoops.match.application.service;

import com.hoops.match.application.port.in.UpdateMatchStatusUseCase;
import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MatchStatusUpdater implements UpdateMatchStatusUseCase {

    private static final List<MatchStatus> STARTABLE_STATUSES = List.of(
            MatchStatus.PENDING,
            MatchStatus.CONFIRMED,
            MatchStatus.FULL
    );

    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;

    @Override
    public int startMatches() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Match> matchesToStart = matchRepository.findMatchesToStart(today, now, STARTABLE_STATUSES);

        int count = 0;
        for (Match match : matchesToStart) {
            match.startMatch();
            matchRepository.save(match);
            matchGeoIndex.removeMatch(match.getId());
            count++;
            log.info("경기 시작 상태로 변경: matchId={}, title={}", match.getId(), match.getTitle());
        }

        return count;
    }

    @Override
    public int endMatches() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Match> matchesToEnd = matchRepository.findMatchesToEnd(today, now, MatchStatus.IN_PROGRESS);

        int count = 0;
        for (Match match : matchesToEnd) {
            match.endMatch();
            matchRepository.save(match);
            count++;
            log.info("경기 종료 상태로 변경: matchId={}, title={}", match.getId(), match.getTitle());
        }

        return count;
    }
}
