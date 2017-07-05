package org.logviewer.config;

import org.logviewer.config.properties.LogViewerConfigProperties;
import org.logviewer.core.LogDirectory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(LogViewerConfigProperties.class)
public class CoreConfig {

    @Bean
    public LogDirectory getLogDirectory(LogViewerConfigProperties config) throws IOException, InterruptedException {
        return new LogDirectory(config.getBaseDir(), config.getIndexArraySize(), config.getThreadPoolSize(), config.getExtensions());
    }
}
