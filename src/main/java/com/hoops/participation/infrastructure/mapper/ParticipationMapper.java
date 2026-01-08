package com.hoops.participation.infrastructure.mapper;

import com.hoops.participation.domain.Participation;
import com.hoops.participation.infrastructure.ParticipationEntity;

public class ParticipationMapper {

    public static Participation toDomain(ParticipationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Participation(
                entity.getId(),
                entity.getMatchId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getJoinedAt());
    }

    public static ParticipationEntity toEntity(Participation domain) {
        if (domain == null) {
            return null;
        }
        return new ParticipationEntity(
                domain.getMatchId(),
                domain.getUserId(),
                domain.getStatus(),
                domain.getJoinedAt());
    }
}
