package com.buaoye.oss.core.cache.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件缓存配置参数类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
@ConfigurationProperties(prefix = "buaoye-oss.cache")
public class BayOssCacheProperty {

    /**
     * 缓存时间（单位：分钟）
     */
    private int cacheTime = 60 * 24 * 5;

    /**
     * 最大缓存大小（单位：字节）
     */
    private long maxCacheSize = 1024L * 1024 * 1024 * 5;

    public int getCacheTime() {
        return cacheTime;
    }

    public BayOssCacheProperty setCacheTime(int cacheTime) {
        this.cacheTime = cacheTime;
        return this;
    }

    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    public BayOssCacheProperty setMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        return this;
    }

}
