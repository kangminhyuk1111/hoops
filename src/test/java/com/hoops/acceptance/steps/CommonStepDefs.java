package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestResponse;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 공통 Step Definitions
 *
 * 여러 feature 파일에서 공유되는 스텝을 정의합니다.
 * 중복 스텝 정의로 인한 DuplicateStepDefinitionException을 방지합니다.
 */
public class CommonStepDefs {

    private final SharedTestContext sharedContext;

    public CommonStepDefs(SharedTestContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    @그러면("응답 상태 코드는 {int} 이다")
    public void 응답_상태_코드는_이다(int expectedStatusCode) {
        assertThat(sharedContext.getLastResponse().statusCode())
                .as("HTTP 상태 코드가 %d 이어야 합니다", expectedStatusCode)
                .isEqualTo(expectedStatusCode);
    }

    @그리고("응답에 경기 ID가 포함되어 있다")
    public void 응답에_경기_ID가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.hasJsonField("id"))
                .as("응답에 경기 ID가 포함되어야 합니다")
                .isTrue();
    }

    @그리고("응답의 상태가 {string} 이다")
    public void 응답의_상태가_이다(String expectedStatus) {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.getJsonValue("status"))
                .as("응답의 상태가 %s 이어야 합니다", expectedStatus)
                .isEqualTo(expectedStatus);
    }
}
