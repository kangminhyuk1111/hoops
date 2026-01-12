package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestResponse;
import org.springframework.stereotype.Component;

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
}
