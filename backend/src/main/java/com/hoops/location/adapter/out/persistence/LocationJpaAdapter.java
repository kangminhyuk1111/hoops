package com.hoops.location.adapter.out.persistence;

import com.hoops.location.domain.model.Location;
import com.hoops.location.domain.repository.LocationRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocationJpaAdapter implements LocationRepository {

    private final SpringDataLocationRepository springDataLocationRepository;

    @Override
    public Location save(Location location) {
        LocationJpaEntity entity = LocationMapper.toEntity(location);
        LocationJpaEntity savedEntity = springDataLocationRepository.save(entity);
        return LocationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Location> findById(Long id) {
        return springDataLocationRepository.findById(id).map(LocationMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataLocationRepository.existsByAlias(name);
    }

    @Override
    public List<Location> findAll() {
        return springDataLocationRepository.findAll().stream()
                .map(LocationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Location> searchByKeyword(String keyword) {
        return springDataLocationRepository.searchByKeyword(keyword).stream()
                .map(LocationMapper::toDomain)
                .toList();
    }
}
