package com.hoops.match.infrastructure.mapper;

import com.hoops.match.domain.Match;
import com.hoops.match.infrastructure.MatchEntity;

public class MatchMapper {

    public static Match toDomain(MatchEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Match(
                entity.getId(),
                entity.getHostId(),
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
                entity.getStatus());
    }

    public static MatchEntity toEntity(Match domain) {
        if (domain == null) {
            return null;
        }
        // Assuming update logic is handled via JPA dirty checking on the entity
        // returned by mapping,
        // or a new entity creation. For save(new), we construct a new entity.
        // For update, typically we load->update fields.
        // Simple mapping for now as per minimal requirement.
        // Note: MatchEntity constructor provided matches the full fields.
        return new MatchEntity(
                domain.getHostId(),
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
}
