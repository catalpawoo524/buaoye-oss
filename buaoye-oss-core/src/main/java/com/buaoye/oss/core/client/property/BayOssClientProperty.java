package com.buaoye.oss.core.client.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 客户端配置参数类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
@ConfigurationProperties(prefix = "buaoye-oss.client")
public class BayOssClientProperty {

    /**
     * 连接池大小
     */
    private int maxConnections = 200;

    /**
     * 连接超时时间（单位：毫秒）
     */
    private int connectionTimeout = 5000;

    /**
     * 套接字超时时间（单位：毫秒）
     */
    private int socketTimeout = 8000;

    public int getMaxConnections() {
        return maxConnections;
    }

    public BayOssClientProperty setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public BayOssClientProperty setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public BayOssClientProperty setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }
    
}
