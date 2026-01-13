package com.hoops.acceptance.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RestTemplate 기반 TestAdapter 구현체
 *
 * Spring Boot의 TestRestTemplate을 사용하여
 * 실제 HTTP 요청을 수행합니다.
 */
@Component
public class RestTestAdapter implements TestAdapter {

    private final TestRestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestTestAdapter(TestRestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public TestResponse get(String path) {
        ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);
        return createTestResponse(response);
    }

    @Override
    public TestResponse post(String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                request,
                String.class
        );

        return createTestResponse(response);
    }

    @Override
    public TestResponse getWithAuth(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                request,
                String.class
        );

        return createTestResponse(response);
    }

    @Override
    public TestResponse postWithAuth(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                path,
                HttpMethod.POST,
                request,
                String.class
        );

        return createTestResponse(response);
    }

    @Override
    public TestResponse deleteWithAuth(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                path,
                HttpMethod.DELETE,
                request,
                String.class
        );

        return createTestResponse(response);
    }

    @Override
    public TestResponse deleteWithAuthAndBody(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                path,
                HttpMethod.DELETE,
                request,
                String.class
        );

        return createTestResponse(response);
    }

    @Override
    public TestResponse putWithAuth(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                path,
                HttpMethod.PUT,
                request,
                String.class
        );

        return createTestResponse(response);
    }

    @Override
    public boolean isApplicationRunning() {
        try {
            TestResponse response = get("/actuator/health");
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private TestResponse createTestResponse(ResponseEntity<String> response) {
        Map<String, Object> jsonBody = null;
        String body = response.getBody();

        if (body != null && !body.isEmpty()) {
            try {
                jsonBody = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                // JSON 파싱 실패 시 null 유지
            }
        }

        return new TestResponse(
                response.getStatusCode().value(),
                body,
                jsonBody
        );
    }
}
