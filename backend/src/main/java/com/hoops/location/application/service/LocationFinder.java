package com.hoops.location.application.service;

import com.hoops.location.application.exception.LocationNotFoundException;
import com.hoops.location.application.port.in.LocationQueryUseCase;
import com.hoops.location.domain.model.Location;
import com.hoops.location.domain.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 장소 조회 서비스
 */
@Service
@Transactional(readOnly = true)
public class LocationFinder implements LocationQueryUseCase {

    private final LocationRepository locationRepository;

    public LocationFinder(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<Location> findAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public List<Location> searchLocations(String keyword) {
        return locationRepository.searchByKeyword(keyword);
    }

    @Override
    public Location findLocationById(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException(locationId));
    }
}
