package com.buaoye.oss.core.cache;

import com.buaoye.oss.core.cache.definition.DefaultFileCacheDefinition;
import com.buaoye.oss.core.cache.definition.FileCacheDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件缓存定义管理类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class BayOssCacheManager {

    /**
     * 文件缓存定义映射
     */
    private static final Map<String, FileCacheDefinition> FILE_CACHE_DEFINITIONS = new ConcurrentHashMap<>();

    /**
     * 创建文件缓存定义类
     *
     * @param filename 文件名
     * @param id       主键ID
     * @return 文件缓存定义类
     */
    public static FileCacheDefinition get(String filename, String id) {
        FileCacheDefinition defaultFileCacheDefinition = FILE_CACHE_DEFINITIONS.computeIfAbsent(id, k -> new DefaultFileCacheDefinition(id, filename));
        return defaultFileCacheDefinition.access();
    }

    public static Map<String, FileCacheDefinition> definitions() {
        return FILE_CACHE_DEFINITIONS;
    }

}
