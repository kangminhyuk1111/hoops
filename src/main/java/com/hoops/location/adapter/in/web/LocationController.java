package com.hoops.location.adapter.in.web;

import com.hoops.location.adapter.in.web.dto.CreateLocationRequest;
import com.hoops.location.adapter.in.web.dto.LocationResponse;
import com.hoops.location.application.port.in.CreateLocationUseCase;
import com.hoops.location.domain.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final CreateLocationUseCase createLocationUseCase;

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@RequestBody CreateLocationRequest request) {
        Location location = createLocationUseCase.createLocation(request.toCommand());

        LocationResponse response = LocationResponse.from(location);

        return ResponseEntity.created(URI.create("/api/locations/" + location.getId()))
                .body(response);
    }
}
