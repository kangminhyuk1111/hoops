package com.hoops.acceptance.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;

/**
 * Cucumber와 Spring Boot 통합 설정
 *
 * @CucumberContextConfiguration: Cucumber가 Spring 컨텍스트를 사용하도록 설정
 * @SpringBootTest: Spring Boot 애플리케이션 전체를 테스트 환경에서 실행
 * WebEnvironment.RANDOM_PORT: 랜덤 포트로 실제 서버를 시작
 * MySQL Testcontainers 사용
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = CucumberSpringConfiguration.Initializer.class)
public class CucumberSpringConfiguration {

    private static final MySQLContainer<?> mysql;

    static {
        mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
        mysql.start();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                context,
                "spring.datasource.url=" + mysql.getJdbcUrl(),
                "spring.datasource.username=" + mysql.getUsername(),
                "spring.datasource.password=" + mysql.getPassword(),
                "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect"
            );
        }
    }
}
