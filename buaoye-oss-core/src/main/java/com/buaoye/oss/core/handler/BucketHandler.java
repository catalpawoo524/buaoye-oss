package com.buaoye.oss.core.handler;

import com.amazonaws.services.s3.model.CannedAccessControlList;

/**
 * Amazon S3 桶操作
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public interface BucketHandler {

    /**
     * 创建桶
     *
     * @param accessControl 访问控制
     * @param bucketName    桶名称
     * @param endpointUrl   端点URL
     * @param keyId         ID
     * @param keySecret     密钥
     */
    void createBucket(CannedAccessControlList accessControl, String bucketName, String endpointUrl, String keyId, String keySecret);

    /**
     * 删除桶
     *
     * @param bucketName  桶名称
     * @param endpointUrl 端点URL
     * @param keyId       ID
     * @param keySecret   密钥
     */
    void deleteBucket(String bucketName, String endpointUrl, String keyId, String keySecret);

}
