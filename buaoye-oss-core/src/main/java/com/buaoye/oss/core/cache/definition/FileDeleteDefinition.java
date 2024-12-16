package com.buaoye.oss.core.cache.definition;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件删除定义类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class FileDeleteDefinition {

    /**
     * 删除时间
     */
    protected LocalDateTime deleteTime;

    /**
     * 文件列表
     */
    protected final List<File> files;

    public FileDeleteDefinition(LocalDateTime deleteTime, List<File> files) {
        this.deleteTime = deleteTime;
        this.files = files;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public List<File> getFiles() {
        return files;
    }

}
