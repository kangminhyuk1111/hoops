package com.hoops.match.adapter.out.persistence;

import com.hoops.match.domain.model.Match;

public class MatchMapper {

    public static Match toDomain(MatchJpaEntity entity) {
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

    public static MatchJpaEntity toEntity(Match domain) {
        if (domain == null) {
            return null;
        }

        MatchJpaEntity entity;
        if (domain.getId() != null) {
            entity = new MatchJpaEntity(
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
            entity = new MatchJpaEntity(
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
