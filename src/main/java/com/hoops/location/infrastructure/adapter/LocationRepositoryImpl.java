package com.hoops.location.infrastructure.adapter;

import com.hoops.location.domain.Location;
import com.hoops.location.domain.repository.LocationRepository;
import com.hoops.location.infrastructure.LocationEntity;
import com.hoops.location.infrastructure.jpa.JpaLocationRepository;
import com.hoops.location.infrastructure.mapper.LocationMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final JpaLocationRepository jpaLocationRepository;

    @Override
    public Location save(Location location) {
        LocationEntity entity = LocationMapper.toEntity(location);
        LocationEntity savedEntity = jpaLocationRepository.save(entity);
        return LocationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Location> findById(Long id) {
        return jpaLocationRepository.findById(id).map(LocationMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaLocationRepository.existsByAlias(name);
    }
}
