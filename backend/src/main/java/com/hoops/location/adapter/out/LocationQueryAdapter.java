package com.hoops.location.adapter.out;

import com.hoops.location.application.port.out.LocationQueryPort;
import com.hoops.location.application.port.out.LocationRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 외부 Context용 장소 조회 어댑터
 *
 * LocationQueryPort를 구현하여 외부 Context에서 장소 정보를 조회할 수 있게 합니다.
 * 내부적으로 LocationRepository를 사용하지만, 외부에는 필요한 정보만 노출합니다.
 */
@Component
@RequiredArgsConstructor
public class LocationQueryAdapter implements LocationQueryPort {

    private final LocationRepositoryPort locationRepository;

    @Override
    public Optional<LocationData> getLocationData(Long locationId) {
        return locationRepository.findById(locationId)
                .map(location -> new LocationData(
                        location.getId(),
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAddress()
                ));
    }
}
