package com.ruanjian.qasystem.common;

import lombok.Getter;

/**
 * 业务异常，用于返回明确的业务错误码。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}