package com.buaoye.oss.core.client;

import com.amazonaws.services.s3.AmazonS3;

import java.time.LocalDateTime;

/**
 * 代理客户端
 *
 * @author Jayson Wu
 * @since 2024-12-18
 */
public class ProxyClient {

    /**
     * Amazon S3 客户端
     */
    private final AmazonS3 client;

    /**
     * 创建时间
     */
    private final LocalDateTime createTime = LocalDateTime.now();

    /**
     * 最近复用时间
     */
    private LocalDateTime reuseTime = null;

    /**
     * 复用次数
     */
    private long reuseNum = 0L;

    /**
     * 端点URL
     */
    private final String endpointUrl;

    /**
     * ID
     */
    private final String keyId;

    public ProxyClient(AmazonS3 client, String endpointUrl, String keyId) {
        this.client = client;
        this.endpointUrl = endpointUrl;
        this.keyId = keyId;
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public synchronized AmazonS3 getClient() {
        LocalDateTime reuseTime = LocalDateTime.now();
        if (this.reuseTime == null || !this.reuseTime.isAfter(reuseTime)) {
            this.reuseTime = reuseTime;
        }
        this.reuseNum += 1L;
        return client;
    }

    /**
     * 关闭客户端连接
     */
    public void shutdown() {
        this.client.shutdown();
    }

    /**
     * 获取连接状态
     *
     * @return True：正常，False：已断开
     */
    public boolean isConn() {
        // 测试连接是否正常
        try {
            client.listBuckets();
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    public LocalDateTime getReuseTime() {
        return this.reuseTime;
    }

    public long getReuseNum() {
        return this.reuseNum;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getKeyId() {
        return keyId;
    }

    @Override
    public String toString() {
        return "{\n" +
                "\"endpointUrl\": \"" + this.endpointUrl + "\",\n" +
                "\"keyId\": \"" + this.keyId + "\"\n" +
                "}";
    }

}
