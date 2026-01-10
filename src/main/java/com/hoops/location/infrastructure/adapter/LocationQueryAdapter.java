package com.hoops.location.infrastructure.adapter;

import com.hoops.location.application.port.out.LocationQueryPort;
import com.hoops.location.domain.repository.LocationRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 외부 Context용 장소 조회 어댑터
 *
 * LocationQueryPort를 구현하여 외부 Context에서 장소 정보를 조회할 수 있게 합니다.
 * 내부적으로 LocationRepository를 사용하지만, 외부에는 필요한 정보만 노출합니다.
 */
@Component
public class LocationQueryAdapter implements LocationQueryPort {

    private final LocationRepository locationRepository;

    public LocationQueryAdapter(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Optional<LocationData> findById(Long locationId) {
        return locationRepository.findById(locationId)
                .map(location -> new LocationData(
                        location.getId(),
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAddress()
                ));
    }
}
