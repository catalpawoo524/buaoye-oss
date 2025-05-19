package com.buaoye.oss.core.handler;

import com.buaoye.oss.core.req.UploadReq;
import com.buaoye.oss.core.resp.UploadResp;

import java.io.OutputStream;
import java.net.URL;

/**
 * Amazon S3 文件操作
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public interface FileHandler {

    /**
     * 上传文件
     *
     * @param endpointUrl 端点URL
     * @param bucketName  桶名称
     * @param keyId       ID
     * @param keySecret   密钥
     * @param objectName  对象名称 / 路径
     * @param uploadReq   上传文件对象
     * @return 文件实体类
     */
    UploadResp uploadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, UploadReq uploadReq);

    /**
     * 下载文件至请求响应流
     *
     * @param endpointUrl  端点URL
     * @param bucketName   桶名称
     * @param keyId        ID
     * @param keySecret    密钥
     * @param objectName   对象名称 / 路径
     * @param filename     文件名
     * @param fileId       文件ID
     * @param outputStream 输出流
     */
    void downloadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, String filename, String fileId, OutputStream outputStream);

    /**
     * 分块下载完整文件至请求响应流
     *
     * @param endpointUrl  端点URL
     * @param bucketName   桶名称
     * @param keyId        ID
     * @param keySecret    密钥
     * @param objectName   对象名称 / 路径
     * @param filename     文件名
     * @param fileId       文件ID
     * @param chunkSize    分块大小
     * @param outputStream 输出流
     * @return 大小
     */
    long downloadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, String filename, String fileId, long chunkSize, OutputStream outputStream);

    /**
     * 分块下载文件至请求响应流
     *
     * @param endpointUrl  端点URL
     * @param bucketName   桶名称
     * @param keyId        ID
     * @param keySecret    密钥
     * @param objectName   对象名称 / 路径
     * @param filename     文件名
     * @param fileId       文件ID
     * @param start        起始
     * @param end          结束
     * @param outputStream 输出流
     * @return 当前块大小
     */
    long downloadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, String filename, String fileId, long start, long end, OutputStream outputStream);

    /**
     * 生成预签名URL
     *
     * @param endpointUrl  端点URL
     * @param bucketName   桶名称
     * @param keyId        ID
     * @param keySecret    密钥
     * @param objectName   对象名称 / 路径
     * @param expireSecond 超时时间（单位：秒）
     * @return URL
     */
    URL presignedUrl(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, long expireSecond);

    /**
     * 删除文件
     *
     * @param endpointUrl 端点URL
     * @param bucketName  桶名称
     * @param keyId       ID
     * @param keySecret   密钥
     * @param objectNames 对象名称 / 路径 的数组
     */
    void deleteFile(String endpointUrl, String bucketName, String keyId, String keySecret, String... objectNames);

    /**
     * 复制文件
     *
     * @param endpointUrl      端点URL
     * @param keyId            ID
     * @param keySecret        密钥
     * @param sourceBucket     来源桶
     * @param sourceObjectName 来源对象名称 / 路径
     * @param targetBucket     目标桶
     * @param targetObjectName 目标对象名称 / 路径
     * @return 文件实体类
     */
    UploadResp copyFile(String endpointUrl, String keyId, String keySecret, String sourceBucket, String sourceObjectName, String targetBucket, String targetObjectName);

}
