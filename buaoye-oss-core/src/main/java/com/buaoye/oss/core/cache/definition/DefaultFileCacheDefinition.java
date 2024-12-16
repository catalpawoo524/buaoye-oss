package com.buaoye.oss.core.cache.definition;

import com.buaoye.oss.common.exception.BuaoyeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * 默认文件缓存定义
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class DefaultFileCacheDefinition extends FileCacheDefinition {

    private static final Logger log = LoggerFactory.getLogger(DefaultFileCacheDefinition.class);

    public DefaultFileCacheDefinition(String id, String filename) {
        super(id, filename);
    }

    /**
     * 判断缓存是否存在
     *
     * @return True：存在，False：不存在
     */
    public boolean cacheExist() {
        return this.content != null && this.content.getFiles() != null && !this.content.getFiles().isEmpty();
    }

    @Override
    public void load(Write write, String eTag) {
        try {
            this.writeLock.lockInterruptibly();
            if (this.cacheExist()) {
                if (eTag == null) {
                    log.info("载入数据中，存在可用的缓存数据：id={}，eTag={}", this.id, this.eTag);
                    return;
                } else if (this.eTag != null && this.eTag.equals(eTag)) {
                    // 数据存在且 ETag 匹配上，直接使用缓存
                    log.info("载入数据中，存在可用的缓存数据，参数：id={}，eTag={}", this.id, eTag);
                    return;
                }
                // 表明线上文件被修改过，清除本地缓存
                this.content.logicDelete();
            }
            this.eTag = eTag;
            write.process(this.content);
        } catch (InterruptedException e) {
            log.error("载入数据失败，锁获取中断，参数：id={}，eTag={}", this.id, eTag);
            throw new BuaoyeException(e);
        } catch (Exception e) {
            this.content.logicDelete();
            throw e;
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void useContent(Write write) {
        write.process(this.content);
    }

    @Override
    public void read(OutputStream outputStream) {
        if (this.content.getFiles() == null || this.content.getFiles().isEmpty()) {
            log.error("读取文件流失败，文件内容为空，参数：id={}", this.id);
            throw new BuaoyeException("读取文件流失败，文件内容为空");
        }
        try (WritableByteChannel outputChannel = Channels.newChannel(outputStream)) {
            for (File file : this.content.getFiles()) {
                try (FileChannel inputChannel = new FileInputStream(file).getChannel()) {
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                }
            }
        } catch (IOException e) {
            log.error("读取文件流失败，合并临时文件执行异常，参数：id={}", this.id);
            throw new BuaoyeException(e);
        }
    }

    @Override
    public Long logicDelete() {
        if (this.writeLock.tryLock() && this.cacheExist()) {
            long filesize = this.content.logicDelete();
            this.writeLock.unlock();
            return filesize;
        }
        return null;
    }

}