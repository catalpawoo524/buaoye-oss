package com.buaoye.oss.core.cache.definition;

import com.buaoye.oss.common.exception.BuaoyeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * 文件内容
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class FileContent {

    private static final Logger log = LoggerFactory.getLogger(FileContent.class);

    /**
     * 文件列表
     */
    private final Map<Long, File> files = new ConcurrentHashMap<>();

    /**
     * 使用标志
     */
    private final BlockingQueue<Integer> useTag = new LinkedBlockingQueue<>();

    /**
     * 删除标志（false：未删除，true：删除）
     */
    private boolean deleteTag = false;

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
        return add(1);
    }

    /**
     * 使用开始
     */
    public void startUsing() {
        useTag.offer(1);
    }

    /**
     * 使用结束
     */
    public void endUsing() {
        useTag.poll();
        this.deleteIfNeed();
    }

    /**
     * 删除（更新删除标志）
     *
     * @return 文件大小
     */
    public long updateDeleteTag() {
        long filesize = getTotalSize();
        this.deleteTag = true;
        this.deleteIfNeed();
        return filesize;
    }

    /**
     * 按需删除
     */
    private void deleteIfNeed() {
        if (useTag.isEmpty() && deleteTag) {
            for (File file : files.values()) {
                try {
                    if (!file.delete()) {
                        log.warn("Buaoye Oss - 文件物理删除，文件删除执行失败，错误信息：path={}", file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.warn("Buaoye Oss - 文件物理删除，文件删除执行异常，错误信息：path={}", file.getAbsolutePath(), e);
                }
            }
        }
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
