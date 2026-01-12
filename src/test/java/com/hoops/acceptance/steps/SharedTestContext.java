package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.domain.Match;
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
    private User testUser;
    private List<Match> testMatches = new ArrayList<>();

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

    public User getTestUser() {
        return testUser;
    }

    public void setTestUser(User testUser) {
        this.testUser = testUser;
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
}
