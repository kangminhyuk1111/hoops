package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.location.domain.Location;
import com.hoops.match.domain.model.Match;
import com.hoops.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 테스트 간 공유되는 컨텍스트
 *
 * 여러 StepDefs 클래스에서 공유해야 하는 상태를 관리합니다.
 * Cucumber-Spring의 Glue 코드 간 상태 공유에 사용됩니다.
 */
@Component
@io.cucumber.spring.ScenarioScope
public class SharedTestContext {

    private TestResponse lastResponse;
    private String accessToken;
    private String refreshToken;
    private String tempToken;
    private String kakaoCode;
    private String kakaoId;
    private User testUser;
    private User hostUser;
    private List<Match> testMatches = new ArrayList<>();
    private Long participationId;
    private String userAccessToken;
    private String hostAccessToken;
    private Location testLocation;

    public TestResponse getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(TestResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTempToken() {
        return tempToken;
    }

    public void setTempToken(String tempToken) {
        this.tempToken = tempToken;
    }

    public String getKakaoCode() {
        return kakaoCode;
    }

    public void setKakaoCode(String kakaoCode) {
        this.kakaoCode = kakaoCode;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public User getTestUser() {
        return testUser;
    }

    public void setTestUser(User testUser) {
        this.testUser = testUser;
    }

    public User getHostUser() {
        return hostUser;
    }

    public void setHostUser(User hostUser) {
        this.hostUser = hostUser;
    }

    public List<Match> getTestMatches() {
        return testMatches;
    }

    public void addTestMatch(Match match) {
        this.testMatches.add(match);
    }

    public void clearTestMatches() {
        this.testMatches.clear();
    }

    public Long getParticipationId() {
        return participationId;
    }

    public void setParticipationId(Long participationId) {
        this.participationId = participationId;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public String getHostAccessToken() {
        return hostAccessToken;
    }

    public void setHostAccessToken(String hostAccessToken) {
        this.hostAccessToken = hostAccessToken;
    }

    public Location getTestLocation() {
        return testLocation;
    }

    public void setTestLocation(Location testLocation) {
        this.testLocation = testLocation;
    }
}
