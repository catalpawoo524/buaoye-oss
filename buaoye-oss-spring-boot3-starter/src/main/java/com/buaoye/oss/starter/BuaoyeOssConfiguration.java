package com.buaoye.oss.starter;

import com.buaoye.oss.core.cache.BayOssCacheTask;
import com.buaoye.oss.core.cache.property.BayOssCacheProperty;
import com.buaoye.oss.core.client.BayOssClientManager;
import com.buaoye.oss.core.client.property.BayOssClientProperty;
import com.buaoye.oss.core.handler.BayOssHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 自动装配配置类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({
        BayOssCacheProperty.class,
        BayOssClientProperty.class
})
public class BuaoyeOssConfiguration {

    @Bean
    public BayOssClientManager bayOssClientManager(BayOssClientProperty bayOssClientProperty) {
        return new BayOssClientManager(bayOssClientProperty);
    }

    @Bean
    public BayOssHandler bayOssHandler() {
        return new BayOssHandler();
    }

    @Bean
    public BayOssCacheTask bayOssCacheTask(BayOssCacheProperty bayOssCacheProperty) {
        return new BayOssCacheTask(bayOssCacheProperty);
    }

}
