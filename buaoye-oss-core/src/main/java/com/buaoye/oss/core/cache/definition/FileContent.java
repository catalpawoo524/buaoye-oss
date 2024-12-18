package com.buaoye.oss.core.cache.definition;

import com.buaoye.oss.common.exception.BuaoyeException;
import com.buaoye.oss.core.cache.BayOssCacheManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 文件内容
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class FileContent {

    /**
     * 文件列表
     */
    private final Map<Long, File> files = new ConcurrentHashMap<>();

    /**
     * 新增文件
     *
     * @param startChunk 起始分块
     * @return 文件对象
     */
    public File add(long startChunk) {
        try {
            File tempFile = File.createTempFile("buaoye_file_cache_", null);
            this.files.put(startChunk, tempFile);
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new BuaoyeException(e);
        }
    }

    /**
     * 新增文件
     *
     * @return 文件对象
     */
    public File add() {
        return add(1L);
    }

    /**
     * 逻辑删除（此时存储的物理文件未被删除，正在读取的线程仍能正常执行）
     */
    public long logicDelete() {
        long filesize = 0;
        for (File file : this.files.values()) {
            filesize += file.length();
        }
        BayOssCacheManager.delayDelete(LocalDateTime.now(), this.files.values());
        this.files.clear();
        return filesize;
    }

    /**
     * 获取总大小（单位：字节）
     *
     * @return 总大小
     */
    public long getTotalSize() {
        long filesize = 0;
        for (File file : this.files.values()) {
            filesize += file.length();
        }
        return filesize;
    }

    /**
     * 获取文件列表（升序排列）
     *
     * @return 文件列表
     */
    public List<File> getFiles() {
        List<Long> chunkList = files.keySet().stream().sorted(Long::compare).collect(Collectors.toList());
        List<File> fileList = new ArrayList<>();
        for (Long chunk : chunkList) {
            Optional.ofNullable(this.files.get(chunk)).ifPresent(fileList::add);
        }
        return fileList;
    }

}
