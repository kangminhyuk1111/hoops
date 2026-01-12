package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.그러면;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 헬스 체크 기능에 대한 Step Definitions
 *
 * health-check.feature 파일의 Given/When/Then 단계를 구현합니다.
 * Cucumber의 한글 어노테이션(@먼저, @만일, @그러면)을 사용합니다.
 */
public class HealthCheckStepDefs {

    private final TestAdapter testAdapter;
    private final SharedTestContext sharedContext;

    /**
     * Spring에서 TestAdapter와 SharedTestContext를 주입받습니다.
     * CucumberSpringConfiguration에서 설정한 Spring 컨텍스트를 사용합니다.
     */
    public HealthCheckStepDefs(TestAdapter testAdapter, SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.sharedContext = sharedContext;
    }

    /**
     * Given: 애플리케이션이 정상적으로 실행되고 있다
     *
     * 애플리케이션이 실행 중인지 확인합니다.
     * TestAdapter의 isApplicationRunning() 메서드를 통해 검증합니다.
     */
    @먼저("애플리케이션이 정상적으로 실행되고 있다")
    public void 애플리케이션이_정상적으로_실행되고_있다() {
        boolean isRunning = testAdapter.isApplicationRunning();
        assertThat(isRunning)
                .as("애플리케이션이 실행 중이어야 합니다")
                .isTrue();
    }

    /**
     * When: 헬스 체크 API를 호출한다
     *
     * Spring Boot Actuator의 /actuator/health 엔드포인트를 호출합니다.
     * 응답을 lastResponse 필드에 저장하여 Then 단계에서 검증할 수 있도록 합니다.
     */
    @만일("헬스 체크 API를 호출한다")
    public void 헬스_체크_API를_호출한다() {
        TestResponse response = testAdapter.get("/actuator/health");
        sharedContext.setLastResponse(response);
    }

    /**
     * Then: 응답 본문의 status 필드는 "UP" 이다
     *
     * JSON 응답 본문에서 status 필드 값을 추출하여 검증합니다.
     *
     * @param expectedStatus 기대하는 상태 값 (예: "UP")
     */
    @그러면("응답 본문의 status 필드는 {string} 이다")
    public void 응답_본문의_status_필드는_이다(String expectedStatus) {
        Object actualStatus = sharedContext.getLastResponse().getJsonValue("status");

        assertThat(actualStatus)
                .as("응답 본문에 status 필드가 존재해야 합니다")
                .isNotNull();

        assertThat(actualStatus.toString())
                .as("status 필드 값이 %s 이어야 합니다", expectedStatus)
                .isEqualTo(expectedStatus);
    }

    /**
     * Then: 응답에 데이터베이스 연결 정보가 포함되어 있다
     *
     * Spring Boot Actuator의 헬스 체크 응답에는
     * components 필드에 각 컴포넌트(DB, Kafka 등)의 상태가 포함됩니다.
     *
     * 예시 응답 구조:
     * {
     *   "status": "UP",
     *   "components": {
     *     "db": {
     *       "status": "UP",
     *       "details": {
     *         "database": "MySQL",
     *         "validationQuery": "isValid()"
     *       }
     *     }
     *   }
     * }
     */
    @그러면("응답에 데이터베이스 연결 정보가 포함되어 있다")
    public void 응답에_데이터베이스_연결_정보가_포함되어_있다() {
        TestResponse lastResponse = sharedContext.getLastResponse();

        // components 필드가 존재하는지 확인
        assertThat(lastResponse.hasJsonField("components"))
                .as("응답에 components 필드가 존재해야 합니다")
                .isTrue();

        // components 객체를 가져옴
        Object components = lastResponse.getJsonValue("components");
        assertThat(components)
                .as("components 필드가 null이 아니어야 합니다")
                .isNotNull();

        // components가 Map 타입인지 확인하고 db 키가 있는지 검증
        if (components instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> componentsMap = (java.util.Map<String, Object>) components;

            assertThat(componentsMap)
                    .as("components에 db 정보가 포함되어 있어야 합니다")
                    .containsKey("db");
        } else {
            throw new AssertionError("components 필드가 Map 타입이 아닙니다: " + components.getClass());
        }
    }
}
