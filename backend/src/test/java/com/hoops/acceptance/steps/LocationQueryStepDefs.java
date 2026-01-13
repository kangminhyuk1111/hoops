package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.location.domain.Location;
import com.hoops.location.domain.repository.LocationRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 장소 조회 및 검색 Step Definitions
 */
public class LocationQueryStepDefs {

    private final TestAdapter testAdapter;
    private final LocationRepository locationRepository;
    private final SharedTestContext sharedContext;

    private Location testLocation;

    public LocationQueryStepDefs(
            TestAdapter testAdapter,
            LocationRepository locationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.locationRepository = locationRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("장소가 {int}개 등록되어 있다")
    public void 장소가_N개_등록되어_있다(int count) {
        for (int i = 1; i <= count; i++) {
            Location location = Location.builder()
                    .userId(1L)
                    .alias("테스트 장소 " + i)
                    .latitude(BigDecimal.valueOf(37.5665 + i * 0.01))
                    .longitude(BigDecimal.valueOf(126.9780 + i * 0.01))
                    .address("서울시 테스트구 " + i + "번지")
                    .build();
            locationRepository.save(location);
        }
    }

    @먼저("{string}이라는 장소가 등록되어 있다")
    public void 이름으로_장소가_등록되어_있다(String locationName) {
        Location location = Location.builder()
                .userId(1L)
                .alias(locationName)
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울시 테스트구")
                .build();
        testLocation = locationRepository.save(location);
    }

    @만일("장소 목록 조회 API를 호출한다")
    public void 장소_목록_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/locations", accessToken);
        } else {
            response = testAdapter.get("/api/locations");
        }
        sharedContext.setLastResponse(response);
    }

    @만일("{string} 키워드로 장소 검색 API를 호출한다")
    public void 키워드로_장소_검색_API를_호출한다(String keyword) {
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/locations/search?keyword=" + keyword;
        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("해당 장소 상세 조회 API를 호출한다")
    public void 해당_장소_상세_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/locations/" + testLocation.getId();
        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 장소 상세 조회 API를 호출한다")
    public void 존재하지_않는_장소_상세_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentLocationId = 999999L;

        String path = "/api/locations/" + nonExistentLocationId;
        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 장소가 {int}개 포함되어 있다")
    public void 응답에_장소가_N개_포함되어_있다(int expectedCount) {
        TestResponse response = sharedContext.getLastResponse();
        int actualCount = response.getJsonArraySize();
        assertThat(actualCount)
                .as("응답에 장소가 %d개 포함되어야 합니다", expectedCount)
                .isEqualTo(expectedCount);
    }

    @그리고("응답에 장소 ID가 포함되어 있다")
    public void 응답에_장소_ID가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.hasJsonField("id"))
                .as("응답에 장소 ID가 포함되어야 합니다")
                .isTrue();
    }

    @그리고("응답의 장소명이 {string} 이다")
    public void 응답의_장소명이_이다(String expectedName) {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.getJsonValue("name"))
                .as("응답의 장소명이 %s 이어야 합니다", expectedName)
                .isEqualTo(expectedName);
    }
}
