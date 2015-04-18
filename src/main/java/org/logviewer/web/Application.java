package org.logviewer.web;

import org.logviewer.core.LogDirectory;
import org.logviewer.web.util.HttpSessionInitializer;
import org.logviewer.web.util.WebKeys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.servlet.http.HttpSessionListener;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public LogDirectory getLogDirectory() throws IOException, InterruptedException {
        Properties props = PropertiesLoaderUtils.loadProperties(new ClassPathResource("/application.properties"));
        String extensions = props.getProperty(WebKeys.EXTENSIONS_PROPERTY);
        String directoryPath = props.getProperty(WebKeys.LOG_DIR_PROPERTY);
        Integer indexArraySize = Integer.valueOf(props.getProperty(WebKeys.INDEX_ARRAY_SIZE_PROPERTY));
        Integer threadPoolSize = Integer.valueOf(props.getProperty(WebKeys.THREAD_POOL_SIZE_PROPERTY));
        return new LogDirectory(directoryPath,
                indexArraySize,
                threadPoolSize,
                extensions == null ? new String[0] : extensions.split(WebKeys.EXTENSIONS_PROPERTY_SPLIT_PATTERN));
    }

    @Bean
    public HttpSessionListener getSessionInitializer() {
        return new HttpSessionInitializer();
    }
}
