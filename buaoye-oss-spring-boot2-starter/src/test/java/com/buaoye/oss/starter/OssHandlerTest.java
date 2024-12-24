package com.buaoye.oss.starter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.buaoye.oss.common.exception.BuaoyeException;
import com.buaoye.oss.common.exception.ErrorCodeConstant;
import com.buaoye.oss.common.thread.BayThreadPool;
import com.buaoye.oss.common.util.StringUtil;
import com.buaoye.oss.core.cache.BayOssCacheManager;
import com.buaoye.oss.core.client.BayOssClientManager;
import com.buaoye.oss.core.handler.BayOssHandler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.LongStream;

/**
 * OSS 操作测试
 *
 * @author Jayson Wu
 * @since 2024-12-16
 */
@SpringBootTest(
        classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(locations = "classpath:/test-application.properties")
public class OssHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(OssHandlerTest.class);

    @Autowired
    private BayOssClientManager bayOssClientManager;

    @Autowired
    private BayOssHandler bayOssHandler;

    @Autowired
    private BayThreadPool bayThreadPool;

    @Value("${endpointUrl}")
    private String endpointUrl;

    @Value("${keyId}")
    private String keyId;

    @Value("${keySecret}")
    private String keySecret;

    @Value("${bucketName}")
    private String bucketName;

    @Value("${objectName}")
    private String objectName;

    @Value("${filename}")
    private String filename;

    @Value("${fileId}")
    private String fileId;

    @Test
    public void ossHandlerTest() {
        // 获取客户端测试
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        // 主动断连测试
        client.shutdown();
        client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        // 获取桶测试
        String bucketLocation = client.getBucketLocation(new GetBucketLocationRequest(bucketName));
        if (StringUtil.isNullOrUndefined(bucketLocation)) {
            throw new BuaoyeException(ErrorCodeConstant.TEST_GET_BUCKET_EXCEPTION);
        }
        // 客户端复用测试
        AmazonS3 reuseClient = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        if (reuseClient == null || !Objects.equals(reuseClient, client)) {
            throw new BuaoyeException(ErrorCodeConstant.TEST_CLIENT_REUSE_EXCEPTION);
        }
        log.info("OSS 操作测试，获取到桶{}", bucketLocation);
        // 下载文件测试
        File downloadFile = new File("./" + filename);
        downloadFile.deleteOnExit();
        try (FileOutputStream outputStream = new FileOutputStream(downloadFile)) {
            bayOssHandler.downloadFile(endpointUrl, bucketName, keyId, keySecret, objectName, filename, fileId, 3 * 1024, outputStream);
        } catch (IOException e) {
            log.error("OSS 操作测试异常，下载文件测试异常");
            throw new BuaoyeException(e, ErrorCodeConstant.TEST_DOWNLOAD_EXCEPTION);
        }
        // 缓存测试
        File downloadCacheFile = new File("./cache_" + filename);
        downloadCacheFile.deleteOnExit();
        try (FileOutputStream outputStream = new FileOutputStream(downloadCacheFile)) {
            bayOssHandler.downloadFile(endpointUrl, bucketName, keyId, keySecret, objectName, filename, fileId, 3 * 1024, outputStream);
        } catch (IOException e) {
            log.error("OSS 操作测试异常，缓存文件测试异常");
            throw new BuaoyeException(e, ErrorCodeConstant.TEST_CACHE_FILE_EXCEPTION);
        }
        // 客户端统计测试
        log.info("OSS 操作测试，客户端统计信息{}", bayOssClientManager.getStatistic().toString());
        // 并发缓存回收测试
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                LongStream.range(1, 4)
                        .mapToObj(num -> CompletableFuture.supplyAsync(() -> {
                            BayOssCacheManager.create(filename, fileId).useContent((definition, content) -> {
                                content.startUsing();
                                List<File> files = content.getFiles();
                                definition.delete();
                                log.info("并发缓存回收测试，线程{}逻辑删除后内存数据中文件存在结果为{}", num, files.get(0).exists());
                                // 主动执行缓存清除
                                content.endUsing();
                                log.info("并发缓存回收测试，线程{}物理删除后内存数据中文件存在结果为{}", num, files.get(0).exists());
                            });
                            return true;
                        }, bayThreadPool.getBayAsyncExecutor()))
                        .toArray(CompletableFuture[]::new)
        );
        allTasks.join();
    }

}