package org.logviewer.config;

import org.logviewer.web.util.HttpSessionInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSessionListener;

@Configuration
public class WebConfig {

    @Bean
    public HttpSessionListener getSessionInitializer() {
        return new HttpSessionInitializer();
    }
}
