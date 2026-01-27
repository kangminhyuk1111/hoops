package com.hoops.match.adapter.out.redis;

import com.hoops.match.application.port.out.MatchGeoIndexPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchGeoRedisAdapter implements MatchGeoIndexPort {

    private static final String GEO_KEY = "matches:geo";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addMatch(Long matchId, BigDecimal longitude, BigDecimal latitude) {
        String member = formatMatchId(matchId);
        Point point = new Point(longitude.doubleValue(), latitude.doubleValue());

        redisTemplate.opsForGeo().add(GEO_KEY, point, member);
        log.debug("Added match to geo index: matchId={}, lng={}, lat={}", matchId, longitude, latitude);
    }

    @Override
    public void removeMatch(Long matchId) {
        String member = formatMatchId(matchId);

        Long removed = redisTemplate.opsForZSet().remove(GEO_KEY, member);
        log.debug("Removed match from geo index: matchId={}, removed={}", matchId, removed);
    }

    @Override
    public List<Long> findMatchIdsWithinRadius(BigDecimal longitude, BigDecimal latitude, double radiusKm, int limit) {
        Point center = new Point(longitude.doubleValue(), latitude.doubleValue());
        Distance radius = new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle circle = new Circle(center, radius);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .sortAscending()
                .limit(limit);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(GEO_KEY, circle, args);

        if (results == null) {
            return List.of();
        }

        return results.getContent().stream()
                .map(result -> parseMatchId(result.getContent().getName()))
                .toList();
    }

    @Override
    public List<Long> findAllMatchIds() {
        Set<String> members = redisTemplate.opsForZSet().range(GEO_KEY, 0, -1);

        if (members == null) {
            return List.of();
        }

        return members.stream()
                .map(this::parseMatchId)
                .toList();
    }

    @Override
    public void clearAll() {
        redisTemplate.delete(GEO_KEY);
        log.info("Cleared all matches from geo index");
    }

    private String formatMatchId(Long matchId) {
        return "match:" + matchId;
    }

    private Long parseMatchId(String member) {
        return Long.parseLong(member.replace("match:", ""));
    }
}
