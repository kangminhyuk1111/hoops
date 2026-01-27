package com.hoops.match.adapter.out.redis.exception;

import com.hoops.common.exception.ApplicationException;

/**
 * Redis Geo Index 작업 실패 시 발생하는 예외
 */
public class RedisGeoIndexException extends ApplicationException {

    private static final String ERROR_CODE = "REDIS_GEO_INDEX_ERROR";

    public RedisGeoIndexException(String message) {
        super(ERROR_CODE, message);
    }

    public RedisGeoIndexException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
