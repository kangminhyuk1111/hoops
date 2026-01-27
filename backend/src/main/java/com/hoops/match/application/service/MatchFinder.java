package com.hoops.match.application.service;

import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchFinder implements MatchQueryUseCase {

    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;

    @Override
    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    @Override
    public List<Match> getMatchesByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance, int page, int size) {
        double radiusKm = distance.doubleValue() / 1000.0;
        int offset = page * size;

        List<Long> matchIds = matchGeoIndex.findMatchIdsWithinRadius(longitude, latitude, radiusKm, offset, size);

        if (matchIds.isEmpty()) {
            return List.of();
        }

        List<Match> matches = matchRepository.findAllByIds(matchIds);

        // Redis 순서(거리순)대로 재정렬
        Map<Long, Match> matchMap = matches.stream()
                .collect(Collectors.toMap(Match::getId, Function.identity()));

        return matchIds.stream()
                .map(matchMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Match> getMyHostedMatches(Long hostId) {
        return matchRepository.findByHostId(hostId);
    }
}
