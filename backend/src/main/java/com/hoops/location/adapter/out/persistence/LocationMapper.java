package com.hoops.location.adapter.out.persistence;

import com.hoops.location.domain.model.Location;

public class LocationMapper {

    public static Location toDomain(LocationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Location(
                entity.getId(),
                entity.getUserId(),
                entity.getAlias(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getAddress());
    }

    public static LocationJpaEntity toEntity(Location domain) {
        if (domain == null) {
            return null;
        }
        return new LocationJpaEntity(
                domain.getUserId(),
                domain.getAlias(),
                domain.getLatitude(),
                domain.getLongitude(),
                domain.getAddress());
    }
}
