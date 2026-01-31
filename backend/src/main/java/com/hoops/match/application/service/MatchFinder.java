package com.hoops.match.application.service;

import com.hoops.match.application.dto.MatchLocationQueryResult;
import com.hoops.match.application.dto.MatchWithDistance;
import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.application.port.out.MatchGeoIndexPort.GeoMatchResult;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchSortType;
import com.hoops.match.domain.vo.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchFinder implements MatchQueryUseCase {

    private static final double DEFAULT_MAX_RADIUS_KM = 50.0;
    private static final int MAX_FETCH_SIZE = 1000;

    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;

    @Override
    public Match getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    @Override
    public MatchLocationQueryResult getMatchesByLocation(BigDecimal latitude, BigDecimal longitude,
                                                          Double radiusKm, int page, int size,
                                                          MatchStatus status, MatchSortType sortType) {
        double effectiveRadius = radiusKm != null ? radiusKm : DEFAULT_MAX_RADIUS_KM;
        boolean needsPostProcessing = status != null || sortType == MatchSortType.URGENCY;

        List<GeoMatchResult> geoResults;
        if (needsPostProcessing) {
            geoResults = matchGeoIndex.findMatchesWithinRadius(longitude, latitude, effectiveRadius, 0, MAX_FETCH_SIZE);
        } else {
            int offset = page * size;
            geoResults = matchGeoIndex.findMatchesWithinRadius(longitude, latitude, effectiveRadius, offset, size + 1);
        }

        if (geoResults.isEmpty()) {
            return new MatchLocationQueryResult(List.of(), 0, false);
        }

        List<Long> matchIds = geoResults.stream().map(GeoMatchResult::matchId).toList();
        Map<Long, Double> distanceMap = geoResults.stream()
                .collect(Collectors.toMap(GeoMatchResult::matchId, GeoMatchResult::distanceKm));

        List<Match> matches = matchRepository.findAllByIds(matchIds);
        Map<Long, Match> matchMap = matches.stream()
                .collect(Collectors.toMap(Match::getId, Function.identity()));

        List<MatchWithDistance> ordered = matchIds.stream()
                .map(matchMap::get)
                .filter(Objects::nonNull)
                .map(match -> new MatchWithDistance(match, distanceMap.getOrDefault(match.getId(), 0.0)))
                .collect(Collectors.toList());

        if (status != null) {
            ordered = ordered.stream()
                    .filter(mwd -> mwd.match().getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (sortType == MatchSortType.URGENCY) {
            ordered.sort(Comparator.comparingInt(mwd -> mwd.match().getRemainingSlots()));
        }

        if (needsPostProcessing) {
            int offset = page * size;
            int fromIndex = Math.min(offset, ordered.size());
            int toIndex = Math.min(offset + size, ordered.size());
            int totalCount = ordered.size();
            boolean hasMore = toIndex < ordered.size();
            ordered = ordered.subList(fromIndex, toIndex);
            return new MatchLocationQueryResult(ordered, totalCount, hasMore);
        }

        boolean hasMore = ordered.size() > size;
        int totalCount = hasMore ? ordered.size() - 1 : ordered.size();
        if (hasMore) {
            ordered = ordered.subList(0, size);
        }

        return new MatchLocationQueryResult(ordered, totalCount, hasMore);
    }

    @Override
    public List<Match> getMyHostedMatches(Long hostId) {
        return matchRepository.findByHostId(hostId);
    }
}
