package com.hoops.match.adapter.out;

import com.hoops.location.application.port.out.LocationQueryPort;
import com.hoops.match.application.dto.LocationInfoResult;
import com.hoops.match.application.exception.MatchLocationNotFoundException;
import com.hoops.match.application.port.out.LocationInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Location Context를 통한 장소 정보 제공 어댑터
 *
 * Match Context의 LocationInfoPort를 구현하여
 * Location Context가 제공하는 LocationQueryPort를 통해 장소 정보를 조회합니다.
 */
@Component
@RequiredArgsConstructor
public class LocationInfoAdapter implements LocationInfoPort {

    private final LocationQueryPort locationQueryPort;

    @Override
    public LocationInfoResult getLocationInfo(Long locationId) {
        LocationQueryPort.LocationData data = locationQueryPort.findById(locationId)
                .orElseThrow(() -> new MatchLocationNotFoundException(locationId));

        return new LocationInfoResult(
                data.id(),
                data.latitude(),
                data.longitude(),
                data.address()
        );
    }
}
