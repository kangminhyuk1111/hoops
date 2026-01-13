package com.hoops.match.adapter.out.mapper;

import com.hoops.match.domain.Match;
import com.hoops.match.adapter.out.MatchEntity;

public class MatchMapper {

    public static Match toDomain(MatchEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Match(
                entity.getId(),
                entity.getVersion(),
                entity.getHostId(),
                entity.getHostNickname(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getAddress(),
                entity.getMatchDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getMaxParticipants(),
                entity.getCurrentParticipants(),
                entity.getStatus(),
                entity.getCancelledAt());
    }

    public static MatchEntity toEntity(Match domain) {
        if (domain == null) {
            return null;
        }

        MatchEntity entity;
        // id가 있으면 업데이트, 없으면 새로 생성
        if (domain.getId() != null) {
            entity = new MatchEntity(
                    domain.getId(),
                    domain.getVersion(),
                    domain.getHostId(),
                    domain.getHostNickname(),
                    domain.getTitle(),
                    domain.getDescription(),
                    domain.getLatitude(),
                    domain.getLongitude(),
                    domain.getAddress(),
                    domain.getMatchDate(),
                    domain.getStartTime(),
                    domain.getEndTime(),
                    domain.getMaxParticipants(),
                    domain.getCurrentParticipants(),
                    domain.getStatus());
        } else {
            entity = new MatchEntity(
                    domain.getHostId(),
                    domain.getHostNickname(),
                    domain.getTitle(),
                    domain.getDescription(),
                    domain.getLatitude(),
                    domain.getLongitude(),
                    domain.getAddress(),
                    domain.getMatchDate(),
                    domain.getStartTime(),
                    domain.getEndTime(),
                    domain.getMaxParticipants(),
                    domain.getCurrentParticipants(),
                    domain.getStatus());
        }
        entity.setCancelledAt(domain.getCancelledAt());
        return entity;
    }
}
