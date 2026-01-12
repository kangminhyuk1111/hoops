package com.hoops.location.adapter.in.web;

import com.hoops.location.adapter.in.web.dto.CreateLocationRequest;
import com.hoops.location.adapter.in.web.dto.LocationResponse;
import com.hoops.location.application.port.in.CreateLocationUseCase;
import com.hoops.location.domain.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Location", description = "장소 관련 API")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final CreateLocationUseCase createLocationUseCase;

    @Operation(summary = "장소 등록", description = "새로운 농구 경기장을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "장소 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@RequestBody CreateLocationRequest request) {
        Location location = createLocationUseCase.createLocation(request.toCommand());

        LocationResponse response = LocationResponse.from(location);

        return ResponseEntity.created(URI.create("/api/locations/" + location.getId()))
                .body(response);
    }
}
