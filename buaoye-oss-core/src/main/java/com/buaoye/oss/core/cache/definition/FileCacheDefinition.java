package com.buaoye.oss.core.cache.definition;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 文件缓存定义抽象类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public abstract class FileCacheDefinition {

    /**
     * ID
     */
    protected final String id;

    /**
     * 写锁，保证缓存的读写一致性
     */
    protected final Lock writeLock;

    /**
     * 文件名
     */
    protected String filename;

    /**
     * 文件内容
     */
    protected FileContent content;

    /**
     * 实体标签
     */
    protected String eTag;

    /**
     * 最近访问时间
     */
    protected LocalDateTime accessTime;

    /**
     * 访问次数
     */
    protected long accessNum;

    public FileCacheDefinition(String id, String filename) {
        this.id = id;
        this.writeLock = new ReentrantLock();
        this.filename = filename;
        this.content = new FileContent();
        this.accessTime = LocalDateTime.now();
        this.accessNum = 0;
    }

    @FunctionalInterface
    public interface Write {
        void process(FileContent fileContent);
    }

    /**
     * 触发访问
     */
    public synchronized FileCacheDefinition access() {
        this.accessTime = LocalDateTime.now();
        this.accessNum += 1;
        return this;
    }

    /**
     * 获取大小（单位：字节）
     *
     * @return 大小
     */
    public long getSize() {
        return content.getTotalSize();
    }

    /**
     * 载入数据
     *
     * @param write 写入方法
     * @param eTag  实体标签
     */
    public abstract void load(Write write, String eTag);

    /**
     * 特殊情况，需要操作 Content（该方法无锁）
     *
     * @param write 写入方法
     */
    public abstract void useContent(Write write);

    /**
     * 读取文件流（合并所有的临时文件到最终输出流）
     *
     * @param outputStream 输出流
     */
    public abstract void read(OutputStream outputStream);

    /**
     * 逻辑删除文件内容
     *
     * @return 删除结果（为空表示失败）
     */
    public abstract Long logicDelete();

    public String getId() {
        return id;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public long getAccessNum() {
        return accessNum;
    }

}
