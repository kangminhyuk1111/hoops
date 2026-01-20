package com.hoops.participation.adapter.out.persistence;

import com.hoops.participation.domain.model.Participation;

public class ParticipationMapper {

    private ParticipationMapper() {
    }

    public static Participation toDomain(ParticipationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Participation(
                entity.getId(),
                entity.getVersion(),
                entity.getMatchId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getJoinedAt());
    }

    public static ParticipationJpaEntity toEntity(Participation domain) {
        if (domain == null) {
            return null;
        }
        if (domain.getId() != null) {
            return new ParticipationJpaEntity(
                    domain.getId(),
                    domain.getVersion(),
                    domain.getMatchId(),
                    domain.getUserId(),
                    domain.getStatus(),
                    domain.getJoinedAt());
        }
        return new ParticipationJpaEntity(
                domain.getMatchId(),
                domain.getUserId(),
                domain.getStatus(),
                domain.getJoinedAt());
    }
}
