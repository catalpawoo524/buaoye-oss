package com.buaoye.oss.core.handler;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.buaoye.oss.common.exception.BuaoyeException;
import com.buaoye.oss.common.thread.BayThreadPool;
import com.buaoye.oss.common.util.StringUtil;
import com.buaoye.oss.core.cache.BayOssCacheManager;
import com.buaoye.oss.core.cache.definition.FileCacheDefinition;
import com.buaoye.oss.core.client.BayOssClientManager;
import com.buaoye.oss.core.req.UploadReq;
import com.buaoye.oss.core.resp.UploadResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Amazon S3 处理类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class BayOssHandler implements OssHandler {

    private static final Logger log = LoggerFactory.getLogger(BayOssHandler.class);

    @Autowired
    private BayOssClientManager bayOssClientManager;

    @Autowired
    private BayThreadPool bayThreadPool;

    @Override
    public void createBucket(CannedAccessControlList accessControl, String bucketName, String endpointUrl, String keyId, String keySecret) {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName).withCannedAcl(accessControl);
        bayOssClientManager.getClient(endpointUrl, keyId, keySecret).createBucket(createBucketRequest);
    }

    @Override
    public void deleteBucket(String bucketName, String endpointUrl, String keyId, String keySecret) {
        bayOssClientManager.getClient(endpointUrl, keyId, keySecret).deleteBucket(bucketName);
    }

    /**
     * 校验客户端下是否存在对应名称的桶
     *
     * @param client     客户端
     * @param bucketName 桶名称
     */
    public void bucketExist(AmazonS3 client, String bucketName) {
        String bucketLocation = client.getBucketLocation(new GetBucketLocationRequest(bucketName));
        if (StringUtil.isNullOrUndefined(bucketLocation)) {
            log.error("Buaoye Oss - 桶不存在或已删除，参数：client={}，bucket={}", client, bucketName);
            throw new BuaoyeException("桶不存在或已删除");
        }
    }

    @Override
    public UploadResp uploadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, UploadReq uploadReq) {
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        bucketExist(client, bucketName);
        try (ByteArrayInputStream inputStream = uploadReq.getInputStream()) {
            if (inputStream == null) {
                log.error("Buaoye Oss - 上传文件失败，上传文件流为空，参数：url={}，bucket={}，keyId={}，objectName={}", endpointUrl, bucketName, keyId, objectName);
                throw new BuaoyeException("上传文件失败，上传文件流为空");
            }
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(inputStream.available());
            // 被读取过的流，需要 Reset 才能再次读取
            inputStream.reset();
            // 执行文件上传
            long starTime = System.currentTimeMillis();
            PutObjectResult result = client.putObject(bucketName, objectName, inputStream, metadata);
            if (result == null) {
                log.error("Buaoye Oss - 上传文件失败，响应结果为空，参数：url={}，bucket={}，keyId={}，objectName={}", endpointUrl, bucketName, keyId, objectName);
                throw new BuaoyeException("上传文件失败，响应结果为空");
            }
            long endTime = System.currentTimeMillis();
            log.debug("Buaoye Oss - 上传文件执行成功，文件大小为{}字节，耗时{}毫秒", metadata.getContentLength(), endTime - starTime);
            return new UploadResp(client.getUrl(bucketName, objectName).toString(), metadata.getContentLength(), result.getContentMd5());
        } catch (IOException e) {
            log.error("Buaoye Oss - 上传文件失败，获取上传文件流异常，参数：url={}，bucket={}，keyId={}，objectName={}", endpointUrl, bucketName, keyId, objectName);
            throw new BuaoyeException(e);
        }
    }

    @Override
    public void downloadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, String filename, String fileId, OutputStream outputStream) {
        FileCacheDefinition fileCacheDefinition = BayOssCacheManager.create(filename, fileId);
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        bucketExist(client, bucketName);
        ObjectMetadata metadata = client.getObjectMetadata(bucketName, objectName);
        fileCacheDefinition.load((content) -> {
            try (
                    S3Object object = client.getObject(bucketName, objectName);
                    S3ObjectInputStream inputStream = object.getObjectContent()
            ) {
                if (inputStream == null) {
                    log.error("Buaoye Oss - 下载文件至请求响应流失败，文件流为空，参数：url={}，bucket={}，keyId={}，objectName={}", endpointUrl, bucketName, keyId, objectName);
                    return;
                }
                File tempFile = content.add();
                try (FileOutputStream fileStream = new FileOutputStream(tempFile)) {
                    IOUtils.copy(inputStream, fileStream);
                }
            } catch (IOException e) {
                log.error("Buaoye Oss - 下载文件至请求响应流失败，流处理异常，参数：url={}，bucket={}，keyId={}，objectName={}", endpointUrl, bucketName, keyId, objectName);
                throw new BuaoyeException(e);
            }
        }, metadata.getETag());
        fileCacheDefinition.read(outputStream);
    }

    @Override
    public long downloadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, String filename, String fileId, long chunkSize, OutputStream outputStream) {
        FileCacheDefinition fileCacheDefinition = BayOssCacheManager.create(filename, fileId);
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        bucketExist(client, bucketName);
        ObjectMetadata metadata = client.getObjectMetadata(bucketName, objectName);
        long totalSize = metadata.getContentLength();
        long numChunks = (totalSize + chunkSize - 1) / chunkSize;
        fileCacheDefinition.load((content) -> {
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                    LongStream.range(0, numChunks)
                            .mapToObj(chunk -> CompletableFuture.supplyAsync(() -> {
                                long start = chunk * chunkSize;
                                long end = Math.min((chunk + 1) * chunkSize - 1, totalSize - 1);
                                log.debug("Buaoye Oss - 开始从对象存储下载分块，参数：start={}，end={}", start, end);
                                GetObjectRequest request = new GetObjectRequest(bucketName, objectName).withRange(start, end);
                                File tempFile;
                                try (
                                        S3Object object = client.getObject(request);
                                        S3ObjectInputStream inputStream = object.getObjectContent()
                                ) {
                                    tempFile = content.add(start);
                                    try (FileOutputStream fileStream = new FileOutputStream(tempFile)) {
                                        IOUtils.copy(inputStream, fileStream);
                                    }
                                } catch (IOException e) {
                                    log.error("Buaoye Oss - 分块下载完整文件至请求响应流失败，流处理异常，参数：url={}，bucket={}，keyId={}，objectName={}，start={}，end={}", endpointUrl, bucketName, keyId, objectName, start, end);
                                    throw new BuaoyeException(e);
                                }
                                log.debug("Buaoye Oss - 完成从对象存储下载分块，参数：start={}，end={}", start, end);
                                return tempFile;
                            }, bayThreadPool.getBayAsyncExecutor()))
                            .toArray(CompletableFuture[]::new)
            );
            // 等待所有分块下载完成
            allTasks.join();
        }, metadata.getETag());
        fileCacheDefinition.read(outputStream);
        return totalSize;
    }

    @Override
    public long downloadFile(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, String filename, String fileId, long start, long end, OutputStream outputStream) {
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        bucketExist(client, bucketName);
        ObjectMetadata metadata = client.getObjectMetadata(bucketName, objectName);
        long totalSize = metadata.getContentLength();
        end = Math.min(end, totalSize - 1);
        if (start >= end) {
            return 0;
        }
        GetObjectRequest request = new GetObjectRequest(bucketName, objectName).withRange(start, end);
        try (
                S3Object object = client.getObject(request);
                S3ObjectInputStream inputStream = object.getObjectContent()
        ) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Buaoye Oss - 分块下载文件至请求响应流失败，流转换异常，参数：url={}，bucket={}，keyId={}，objectName={}，start={}，end={}", endpointUrl, bucketName, keyId, objectName, start, end);
            throw new BuaoyeException(e);
        }
        return end - start + 1;
    }

    @Override
    public URL getPresignedUrl(String endpointUrl, String bucketName, String keyId, String keySecret, String objectName, long expireSecond) {
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        Date expiration = new Date(System.currentTimeMillis() + expireSecond * 1000);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectName)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        return client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    @Override
    public void deleteFile(String endpointUrl, String bucketName, String keyId, String keySecret, String... objectNames) {
        if (objectNames == null || objectNames.length < 1) {
            return;
        }
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        bucketExist(client, bucketName);
        List<DeleteObjectsRequest.KeyVersion> objectsToDelete = Arrays.stream(objectNames).map(DeleteObjectsRequest.KeyVersion::new).collect(Collectors.toList());
        client.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(objectsToDelete).withQuiet(false));
    }

}
