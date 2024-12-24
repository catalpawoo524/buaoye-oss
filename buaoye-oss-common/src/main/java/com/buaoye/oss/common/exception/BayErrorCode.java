package com.buaoye.oss.common.exception;

/**
 * 错误码
 *
 * @author Jayson Wu
 * @since 2024-12-20
 */
public class BayErrorCode {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 提示信息
     */
    private final String msg;

    public BayErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static BayErrorCode create(Integer code, String msg) {
        return new BayErrorCode(code, msg);
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return "Buaoye Oss - " + msg;
    }

}
