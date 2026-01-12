package com.hoops.location.infrastructure.mapper;

import com.hoops.location.domain.Location;
import com.hoops.location.infrastructure.LocationEntity;

public class LocationMapper {

    public static Location toDomain(LocationEntity entity) {
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

    public static LocationEntity toEntity(Location domain) {
        if (domain == null) {
            return null;
        }
        return new LocationEntity(
                domain.getUserId(),
                domain.getAlias(),
                domain.getLatitude(),
                domain.getLongitude(),
                domain.getAddress());
    }
}
