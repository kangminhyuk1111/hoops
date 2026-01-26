package com.hoops.location.adapter.in.web;

import com.hoops.location.adapter.in.web.dto.CreateLocationRequest;
import com.hoops.location.adapter.in.web.dto.LocationResponse;
import com.hoops.location.application.port.in.CreateLocationUseCase;
import com.hoops.location.application.port.in.LocationQueryUseCase;
import com.hoops.location.domain.model.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Tag(name = "Location", description = "장소 관련 API")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final CreateLocationUseCase createLocationUseCase;
    private final LocationQueryUseCase locationQueryUseCase;

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

    @Operation(summary = "장소 목록 조회", description = "등록된 전체 장소 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<Location> locations = locationQueryUseCase.getAllLocations();
        List<LocationResponse> responses = locations.stream()
                .map(LocationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "장소 검색", description = "키워드로 장소를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<List<LocationResponse>> searchLocations(@RequestParam String keyword) {
        List<Location> locations = locationQueryUseCase.searchLocations(keyword);
        List<LocationResponse> responses = locations.stream()
                .map(LocationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "장소 상세 조회", description = "장소 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @GetMapping("/{locationId}")
    public ResponseEntity<LocationResponse> getLocation(@PathVariable Long locationId) {
        Location location = locationQueryUseCase.getLocationById(locationId);
        return ResponseEntity.ok(LocationResponse.from(location));
    }
}
