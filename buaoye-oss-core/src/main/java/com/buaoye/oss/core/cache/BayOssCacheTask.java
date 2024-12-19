package com.buaoye.oss.core.cache;

import com.buaoye.oss.core.cache.definition.FileCacheDefinition;
import com.buaoye.oss.core.cache.property.BayOssCacheProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 缓存定时任务
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class BayOssCacheTask {

    private static final Logger log = LoggerFactory.getLogger(BayOssCacheTask.class);

    private final BayOssCacheProperty bayOssCacheProperty;

    public BayOssCacheTask(BayOssCacheProperty bayOssCacheProperty) {
        this.bayOssCacheProperty = bayOssCacheProperty;
    }

    /**
     * 逻辑清理定时任务
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public synchronized void logicClearTask() {
        LocalDateTime latestTime = LocalDateTime.now().minusMinutes(bayOssCacheProperty.getCacheTime());
        List<FileCacheDefinition> clearList = new ArrayList<>();
        AtomicLong totalSize = new AtomicLong();
        List<FileCacheDefinition> fileCacheList = BayOssCacheManager.getFileCacheDefinitions().values().stream().filter(item -> {
            if (item.getAccessTime().isBefore(latestTime)) {
                // 清理长时间未访问的文件
                clearList.add(item);
                return false;
            } else {
                totalSize.addAndGet(item.getSize());
                return true;
            }
        }).sorted(Comparator.comparing(FileCacheDefinition::getAccessTime).thenComparing(FileCacheDefinition::getAccessNum)).collect(Collectors.toList());
        // 如果大小超出限制，则按访问时间清理，直至满足条件
        for (FileCacheDefinition fileCacheDefinition : fileCacheList) {
            if (totalSize.get() > bayOssCacheProperty.getMaxCacheSize()) {
                clearList.add(fileCacheDefinition);
                totalSize.addAndGet(-fileCacheDefinition.getSize());
            }
        }
        long clearSize = 0;
        for (FileCacheDefinition fileCacheDefinition : clearList) {
            clearSize += fileCacheDefinition.delete();
            BayOssCacheManager.getFileCacheDefinitions().remove(fileCacheDefinition.getId());
        }
        log.info("Buaoye Oss - 文件缓存清理定时任务执行成功，当前使用缓存{}字节，限制缓存{}字节，本次清理缓存{}字节", totalSize.get(), bayOssCacheProperty.getMaxCacheSize(), clearSize);
    }

}
