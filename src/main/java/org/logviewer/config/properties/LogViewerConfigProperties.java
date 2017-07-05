package org.logviewer.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ConfigurationProperties("logviewer")
public class LogViewerConfigProperties {

    @NotNull
    private String[] extensions;

    @NotNull
    private String baseDir;

    @Min(1)
    @NotNull
    private Integer indexArraySize;

    @Min(1)
    @NotNull
    private Integer threadPoolSize;

    public String[] getExtensions() {
        return extensions;
    }

    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public Integer getIndexArraySize() {
        return indexArraySize;
    }

    public void setIndexArraySize(Integer indexArraySize) {
        this.indexArraySize = indexArraySize;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(Integer threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
}
