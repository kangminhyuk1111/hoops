package com.hoops.acceptance;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Cucumber 인수 테스트 실행 진입점
 *
 * JUnit Platform을 사용하여 Cucumber 테스트를 실행합니다.
 *
 * @Suite: JUnit 5 테스트 스위트 설정
 * @IncludeEngines: Cucumber 엔진 사용
 * @SelectClasspathResource: Feature 파일 위치 지정
 * @ConfigurationParameter: Cucumber 설정
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.hoops.acceptance")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:build/reports/cucumber/cucumber-report.html, json:build/reports/cucumber/cucumber.json"
)
public class AcceptanceTest {
    // Cucumber Runner 클래스
    // 실제 테스트는 Feature 파일과 Step Definitions에서 실행됨
}
