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
        int limit = size;

        List<Long> matchIds = matchGeoIndex.findMatchIdsWithinRadius(longitude, latitude, radiusKm, limit);

        if (matchIds.isEmpty()) {
            return List.of();
        }

        return matchRepository.findAllByIds(matchIds);
    }

    @Override
    public List<Match> getMyHostedMatches(Long hostId) {
        return matchRepository.findByHostId(hostId);
    }
}
