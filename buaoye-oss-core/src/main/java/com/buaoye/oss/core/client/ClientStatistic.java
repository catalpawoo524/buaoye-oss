package com.buaoye.oss.core.client;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 客户端统计
 *
 * @author Jayson Wu
 * @since 2024-12-18
 */
public class ClientStatistic implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 总客户端数
     */
    private long clientCount = 0;

    /**
     * 平均复用次数
     */
    private long avgReuseNum = 0;

    /**
     * 最早创建时间
     */
    private LocalDateTime earliestCreateTime = null;

    /**
     * 最频繁复用的客户端
     */
    private ProxyClient mostFrequentClient = null;

    /**
     * 最久未使用的客户端
     */
    private ProxyClient longestUnusedClient = null;

    /**
     * 已断开连接的客户端列表
     */
    private List<ProxyClient> disconnectClients = new ArrayList<>();

    public long getClientCount() {
        return clientCount;
    }

    public void setClientCount(long clientCount) {
        this.clientCount = clientCount;
    }

    public long getAvgReuseNum() {
        return avgReuseNum;
    }

    public void setAvgReuseNum(long avgReuseNum) {
        this.avgReuseNum = avgReuseNum;
    }

    public LocalDateTime getEarliestCreateTime() {
        return earliestCreateTime;
    }

    public void setEarliestCreateTime(LocalDateTime earliestCreateTime) {
        this.earliestCreateTime = earliestCreateTime;
    }

    public ProxyClient getMostFrequentClient() {
        return mostFrequentClient;
    }

    public void setMostFrequentClient(ProxyClient mostFrequentClient) {
        this.mostFrequentClient = mostFrequentClient;
    }

    public ProxyClient getLongestUnusedClient() {
        return longestUnusedClient;
    }

    public void setLongestUnusedClient(ProxyClient longestUnusedClient) {
        this.longestUnusedClient = longestUnusedClient;
    }

    public List<ProxyClient> getDisconnectClients() {
        return disconnectClients;
    }

    public void setDisconnectClients(List<ProxyClient> disconnectClients) {
        this.disconnectClients = disconnectClients;
    }

    private String disconnectClientsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        Iterator<ProxyClient> iterator = this.disconnectClients.iterator();
        while (iterator.hasNext()) {
            ProxyClient client = iterator.next();
            sb.append(client.toString());
            if (iterator.hasNext()) {
                sb.append(",\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "{\n" +
                "\"clientCount\": " + this.clientCount + ",\n" +
                "\"avgReuseNum\": " + this.avgReuseNum + ",\n" +
                "\"earliestCreateTime\": \"" + this.earliestCreateTime.toString() + "\",\n" +
                "\"mostFrequentClient\": " + (this.mostFrequentClient == null ? "null" : this.mostFrequentClient.toString()) + ",\n" +
                "\"longestUnusedClient\": " + (this.longestUnusedClient == null ? "null" : this.longestUnusedClient.toString()) + ",\n" +
                "\"disconnectClients\": " + this.disconnectClientsToString() + "\n" +
                "}";
    }

}
