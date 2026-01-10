package com.hoops.match.adapter.out.adapter;

import com.hoops.location.application.port.out.LocationQueryPort;
import com.hoops.match.application.exception.MatchLocationNotFoundException;
import com.hoops.match.application.port.out.LocationInfo;
import com.hoops.match.application.port.out.LocationInfoProvider;
import org.springframework.stereotype.Component;

/**
 * Location Context를 통한 장소 정보 제공 어댑터
 *
 * Match Context의 LocationInfoProvider 포트를 구현하여
 * Location Context가 제공하는 LocationQueryPort를 통해 장소 정보를 조회합니다.
 *
 * Location Context의 내부 구현(Repository)에 직접 의존하지 않고,
 * Location Context가 외부에 제공하는 Port만 사용합니다.
 */
@Component
public class LocationInfoAdapter implements LocationInfoProvider {

    private final LocationQueryPort locationQueryPort;

    public LocationInfoAdapter(LocationQueryPort locationQueryPort) {
        this.locationQueryPort = locationQueryPort;
    }

    @Override
    public LocationInfo getLocationInfo(Long locationId) {
        LocationQueryPort.LocationData data = locationQueryPort.findById(locationId)
                .orElseThrow(() -> new MatchLocationNotFoundException(locationId));

        return new LocationInfo(
                data.id(),
                data.latitude(),
                data.longitude(),
                data.address()
        );
    }
}
