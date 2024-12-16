package com.buaoye.oss.common.exception;

import java.io.Serializable;

/**
 * 异常类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class BuaoyeException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int status = 400;

    /**
     * 数据
     */
    private Object data;

    public BuaoyeException(String message) {
        super(message);
    }

    public BuaoyeException(Throwable throwable) {
        super(throwable);
    }

    public BuaoyeException(String message, int status) {
        super(message);
        this.status = status;
    }

    public BuaoyeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public int getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

}
