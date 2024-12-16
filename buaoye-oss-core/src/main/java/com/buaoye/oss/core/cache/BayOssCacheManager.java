package com.buaoye.oss.core.cache;

import com.buaoye.oss.core.cache.definition.DefaultFileCacheDefinition;
import com.buaoye.oss.core.cache.definition.FileCacheDefinition;
import com.buaoye.oss.core.cache.definition.FileDeleteDefinition;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
     * 待物理删除文件列表
     */
    private static final List<FileDeleteDefinition> FILE_DELETE_DEFINITIONS = new ArrayList<>();

    /**
     * 创建文件缓存定义类
     *
     * @param filename 文件名
     * @param id     主键ID
     * @return 文件缓存定义类
     */
    public static FileCacheDefinition create(String filename, String id) {
        FileCacheDefinition defaultFileCacheDefinition = FILE_CACHE_DEFINITIONS.get(id);
        if (defaultFileCacheDefinition == null) {
            defaultFileCacheDefinition = new DefaultFileCacheDefinition(id, filename);
            FILE_CACHE_DEFINITIONS.put(id, defaultFileCacheDefinition);
        }
        return defaultFileCacheDefinition.access();
    }

    /**
     * 延迟删除
     *
     * @param deleteTime 删除时间
     * @param files 文件列表
     */
    public static void delayDelete(LocalDateTime deleteTime, Collection<File> files){
        if (files == null || files.isEmpty()) {
            return;
        }
        if (deleteTime == null) {
            deleteTime = LocalDateTime.now();
        }
        FILE_DELETE_DEFINITIONS.add(new FileDeleteDefinition(deleteTime, new ArrayList<>(files)));
    }

    public static Map<String, FileCacheDefinition> getFileCacheDefinitions() {
        return FILE_CACHE_DEFINITIONS;
    }

    public static List<FileDeleteDefinition> getFileDeleteDefinitions() {
        return FILE_DELETE_DEFINITIONS;
    }

}
