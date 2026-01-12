package com.hoops.acceptance.adapter;

/**
 * 테스트용 어댑터 인터페이스
 *
 * 테스트 코드가 비즈니스 로직에 강결합되지 않도록
 * API 호출 및 검증을 위한 추상화 계층을 제공합니다.
 */
public interface TestAdapter {

    /**
     * HTTP GET 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @return HTTP 응답
     */
    TestResponse get(String path);

    /**
     * HTTP POST 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @param body 요청 본문
     * @return HTTP 응답
     */
    TestResponse post(String path, Object body);

    /**
     * 인증 헤더와 함께 HTTP GET 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @param token JWT 토큰
     * @return HTTP 응답
     */
    TestResponse getWithAuth(String path, String token);

    /**
     * 인증 헤더와 함께 HTTP POST 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @param body 요청 본문
     * @param token JWT 토큰
     * @return HTTP 응답
     */
    TestResponse postWithAuth(String path, Object body, String token);

    /**
     * 애플리케이션이 실행 중인지 확인합니다.
     *
     * @return 실행 중이면 true, 아니면 false
     */
    boolean isApplicationRunning();
}
