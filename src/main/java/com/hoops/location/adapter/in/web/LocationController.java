package com.hoops.location.adapter.in.web;

import com.hoops.location.adapter.in.web.dto.CreateLocationRequest;
import com.hoops.location.adapter.in.web.dto.LocationResponse;
import com.hoops.location.application.port.in.CreateLocationUseCase;
import com.hoops.location.domain.Location;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장소 관련 REST API Controller
 *
 * Hexagonal Architecture의 Inbound Adapter로서,
 * HTTP 요청을 받아 UseCase를 호출하고 응답을 반환합니다.
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final CreateLocationUseCase createLocationUseCase;

    public LocationController(CreateLocationUseCase createLocationUseCase) {
        this.createLocationUseCase = createLocationUseCase;
    }

    /**
     * 장소 생성 API
     *
     * 사용자가 새로운 장소를 등록합니다.
     *
     * @param request 장소 생성 요청 DTO
     * @return 생성된 장소 응답 (201 Created)
     */
    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@RequestBody CreateLocationRequest request) {
        Location location = createLocationUseCase.createLocation(request.toCommand());

        LocationResponse response = LocationResponse.from(location);

        return ResponseEntity.created(URI.create("/api/locations/" + location.getId()))
                .body(response);
    }
}
