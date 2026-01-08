package com.hoops.acceptance.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 테스트용 HTTP 응답 객체
 */
public record TestResponse(int statusCode, String body, Map<String, Object> jsonBody) {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Object getJsonValue(String key) {
        return jsonBody != null ? jsonBody.get(key) : null;
    }

    public boolean hasJsonField(String key) {
        return jsonBody != null && jsonBody.containsKey(key);
    }

    /**
     * JSON 응답에서 리스트를 추출합니다.
     *
     * @param path JSON 경로 (현재는 "$"만 지원 - 루트 배열)
     * @return JSON 리스트
     */
    public List<Map<String, Object>> getJsonList(String path) {
        if (body == null || body.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            if ("$".equals(path)) {
                return objectMapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {});
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
