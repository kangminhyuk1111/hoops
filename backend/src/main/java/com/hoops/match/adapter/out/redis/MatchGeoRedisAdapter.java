package com.hoops.match.adapter.out.redis;

import com.hoops.match.adapter.out.redis.exception.RedisGeoIndexException;
import com.hoops.match.application.port.out.MatchGeoIndexPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
        try {
            String member = formatMatchId(matchId);
            Point point = new Point(longitude.doubleValue(), latitude.doubleValue());

            redisTemplate.opsForGeo().add(GEO_KEY, point, member);
            log.debug("Added match to geo index: matchId={}, lng={}, lat={}", matchId, longitude, latitude);
        } catch (Exception e) {
            throw new RedisGeoIndexException("Failed to add match to geo index: matchId=" + matchId, e);
        }
    }

    @Override
    public void addMatchesBulk(List<GeoIndexEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }

        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                byte[] keyBytes = GEO_KEY.getBytes(StandardCharsets.UTF_8);
                for (GeoIndexEntry entry : entries) {
                    connection.geoCommands().geoAdd(
                            keyBytes,
                            new Point(entry.longitude().doubleValue(), entry.latitude().doubleValue()),
                            formatMatchId(entry.matchId()).getBytes(StandardCharsets.UTF_8)
                    );
                }
                return null;
            });
            log.info("Bulk added {} matches to geo index", entries.size());
        } catch (Exception e) {
            throw new RedisGeoIndexException("Failed to bulk add matches to geo index", e);
        }
    }

    @Override
    public void removeMatch(Long matchId) {
        try {
            String member = formatMatchId(matchId);

            Long removed = redisTemplate.opsForZSet().remove(GEO_KEY, member);
            log.debug("Removed match from geo index: matchId={}, removed={}", matchId, removed);
        } catch (Exception e) {
            throw new RedisGeoIndexException("Failed to remove match from geo index: matchId=" + matchId, e);
        }
    }

    @Override
    public List<Long> findMatchIdsWithinRadius(BigDecimal longitude, BigDecimal latitude, double radiusKm, int offset, int limit) {
        try {
            Point center = new Point(longitude.doubleValue(), latitude.doubleValue());
            Distance radius = new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS);
            Circle circle = new Circle(center, radius);

            // Redis GEORADIUS는 offset을 지원하지 않으므로, offset + limit 만큼 조회 후 skip
            int fetchCount = offset + limit;

            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .sortAscending()
                    .limit(fetchCount);

            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                    redisTemplate.opsForGeo().radius(GEO_KEY, circle, args);

            if (results == null) {
                return List.of();
            }

            return results.getContent().stream()
                    .skip(offset)
                    .limit(limit)
                    .map(result -> parseMatchId(result.getContent().getName()))
                    .toList();
        } catch (Exception e) {
            throw new RedisGeoIndexException("Failed to find matches within radius", e);
        }
    }

    @Override
    public List<Long> findAllMatchIds() {
        try {
            Set<String> members = redisTemplate.opsForZSet().range(GEO_KEY, 0, -1);

            if (members == null) {
                return List.of();
            }

            return members.stream()
                    .map(this::parseMatchId)
                    .toList();
        } catch (Exception e) {
            throw new RedisGeoIndexException("Failed to find all match ids", e);
        }
    }

    @Override
    public void clearAll() {
        try {
            redisTemplate.delete(GEO_KEY);
            log.info("Cleared all matches from geo index");
        } catch (Exception e) {
            throw new RedisGeoIndexException("Failed to clear geo index", e);
        }
    }

    private String formatMatchId(Long matchId) {
        return "match:" + matchId;
    }

    private Long parseMatchId(String member) {
        return Long.parseLong(member.replace("match:", ""));
    }
}
