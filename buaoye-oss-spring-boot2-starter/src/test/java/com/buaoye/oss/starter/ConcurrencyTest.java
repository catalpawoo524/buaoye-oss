package com.buaoye.oss.starter;

import com.amazonaws.services.s3.AmazonS3;
import com.buaoye.oss.common.exception.BuaoyeException;
import com.buaoye.oss.common.thread.BayThreadPool;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.LongStream;

/**
 * 并发操作测试
 *
 * @author Jayson Wu
 * @since 2024-12-17
 */
@SpringBootTest(
        classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(locations = "classpath:/test-application.properties")
public class ConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyTest.class);

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
    public void concurrencyTest() {
        // 获取客户端测试
        AmazonS3 client = bayOssClientManager.getClient(endpointUrl, keyId, keySecret);
        // 多文件并发下载测试
        long startTime = System.currentTimeMillis();
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                LongStream.range(1, 4)
                        .mapToObj(num -> CompletableFuture.supplyAsync(() -> {
                            long threadStartTime = System.currentTimeMillis();
                            log.info("并发测试执行中，正在下载第{}个文件", num);
                            // 执行任务的代码
                            File downloadFile = new File("./" + num +"_" + filename);
                            downloadFile.deleteOnExit();
                            try (FileOutputStream outputStream = new FileOutputStream(downloadFile)) {
                                bayOssHandler.downloadFile(endpointUrl, bucketName, keyId, keySecret, objectName, filename, fileId, 3 * 1024, outputStream);
                            } catch (IOException e) {
                                log.error("并发测试异常，下载文件测试异常");
                                throw new BuaoyeException(e);
                            }
                            log.info("并发测试执行中，第{}个文件下载完成，消耗时长{}毫秒", num, System.currentTimeMillis() - threadStartTime);
                            return downloadFile;
                            }, bayThreadPool.getBayAsyncExecutor()))
                        .toArray(CompletableFuture[]::new)
        );
        allTasks.join();
        log.info("并发测试完成，总消耗时长{}毫秒，文件1大小为{}字节，文件2大小为{}字节，文件3大小为{}字节",
                System.currentTimeMillis() - startTime,
                new File("./1_" + filename).length(),
                new File("./2_" + filename).length(),
                new File("./3_" + filename).length()
        );
    }

}