package com.buaoye.oss.core.client;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.buaoye.oss.core.client.property.BayOssClientProperty;
import com.buaoye.oss.common.exception.BuaoyeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端管理类，处理客户端复用
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class BayOssClientManager implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(BayOssClientManager.class);

    /**
     * 客户端连接池
     */
    private static final Map<String, Map<String, ProxyClient>> CLIENT_CONNECTION_POOL = new ConcurrentHashMap<>();

    /**
     * 客户端配置
     */
    private static ClientConfiguration clientConfiguration;

    public BayOssClientManager(BayOssClientProperty bayOssClientProperty) {
        clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxConnections(bayOssClientProperty.getMaxConnections());
        clientConfiguration.setConnectionTimeout(bayOssClientProperty.getConnectionTimeout());
        clientConfiguration.setSocketTimeout(bayOssClientProperty.getSocketTimeout());
    }

    /**
     * 获取客户端
     *
     * @param endpointUrl 端点URL
     * @param keyId       ID
     * @param keySecret   密钥
     * @return 客户端
     */
    public AmazonS3 getClient(String endpointUrl, String keyId, String keySecret) {
        // 根据 endpointUrl 获取或创建 serviceMap
        Map<String, ProxyClient> serviceMap = CLIENT_CONNECTION_POOL.computeIfAbsent(endpointUrl, k -> new ConcurrentHashMap<>());
        // 根据 keyId 获取或创建 AmazonS3
        ProxyClient client = serviceMap.computeIfAbsent(keyId, k -> createClient(endpointUrl, keyId, keySecret));
        // 测试连接是否正常
        if (!client.isConn()) {
            log.warn("Buaoye Oss - 获取到的客户端已断开，即将创建新的连接，参数：endpointUrl={}，keyId={}", endpointUrl, keyId);
            serviceMap.remove(keyId);
            client = serviceMap.computeIfAbsent(keyId, k -> createClient(endpointUrl, keyId, keySecret));
        }
        return client.getClient();
    }

    /**
     * 创建新的客户端
     *
     * @param endpointUrl 端点URl
     * @param keyId       ID
     * @param keySecret   密钥
     * @return 客户端
     */
    private ProxyClient createClient(String endpointUrl, String keyId, String keySecret) {
        try {
            log.info("Buaoye Oss - 未获取到可复用客户端连接，执行客户端创建，参数：endpointUrl={}，keyId={}", endpointUrl, keyId);
            return new ProxyClient(
                    AmazonS3ClientBuilder
                            .standard()
                            .withClientConfiguration(clientConfiguration)
                            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(keyId, keySecret)))
                            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, ""))
                            .withPathStyleAccessEnabled(false)
                            .withChunkedEncodingDisabled(true)
                            .build(),
                    endpointUrl,
                    keyId
            );
        } catch (Exception e) {
            log.error("Buaoye Oss - 客户端创建失败，方法执行异常");
            throw new BuaoyeException(e);
        }
    }

    /**
     * 获取客户端统计
     *
     * @return 客户端统计信息
     */
    public synchronized ClientStatistic getStatistic() {
        ClientStatistic clientStatistic = new ClientStatistic();
        CLIENT_CONNECTION_POOL.forEach((endpointUrl, serviceMap) -> {
            serviceMap.forEach((keyId, client) -> {
                clientStatistic.setClientCount(clientStatistic.getClientCount() + 1);
                clientStatistic.setAvgReuseNum(clientStatistic.getAvgReuseNum() + client.getReuseNum());
                if (clientStatistic.getEarliestCreateTime() == null || !clientStatistic.getEarliestCreateTime().isBefore(client.getCreateTime())) {
                    clientStatistic.setEarliestCreateTime(client.getCreateTime());
                }
                if (clientStatistic.getMostFrequentClient() == null || clientStatistic.getMostFrequentClient().getReuseNum() < client.getReuseNum()) {
                    clientStatistic.setMostFrequentClient(client);
                }
                if (clientStatistic.getLongestUnusedClient() == null || (client.getReuseTime() != null && clientStatistic.getLongestUnusedClient().getReuseTime().isAfter(client.getReuseTime()))) {
                    clientStatistic.setLongestUnusedClient(client);
                }
                if (!client.isConn()) {
                    clientStatistic.getDisconnectClients().add(client);
                }
            });
        });
        if (clientStatistic.getClientCount() != 0) {
            clientStatistic.setAvgReuseNum(clientStatistic.getAvgReuseNum() + clientStatistic.getClientCount());
        }
        return clientStatistic;
    }

    @Override
    public void destroy() {
        shutdownAll();
    }

    /**
     * 关闭所有客户端
     */
    private void shutdownAll() {
        for (String endpointUrl : CLIENT_CONNECTION_POOL.keySet()) {
            Map<String, ProxyClient> serviceMap = CLIENT_CONNECTION_POOL.remove(endpointUrl);
            if (serviceMap != null) {
                serviceMap.forEach((key, client) -> {
                    log.info("Buaoye Oss - 执行客户端关闭，对应密钥：{}", key);
                    client.shutdown();
                });
            }
        }
        CLIENT_CONNECTION_POOL.clear();
    }

}
