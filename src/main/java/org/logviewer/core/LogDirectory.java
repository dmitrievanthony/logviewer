package org.logviewer.core;

import org.logviewer.core.cache.NoCacheStrategy;
import org.logviewer.core.index.InMemoryIndexStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * LogDirectory is used for providing api of log files list, observation and
 * loading its.
 */
public class LogDirectory {

    private static final Logger log = LoggerFactory.getLogger(LogDirectory.class);

    private final File directory;

    private final int defaultIndexArraySize;

    private final Map<String, LogFile> logFileMap = new ConcurrentHashMap<>();

    private final FileConsumer fileConsumer;

    private final ExecutorService executors;

    public LogDirectory(String directoryPath, int defaultIndexArraySize, int threadPoolSize, String... extensions) throws IOException {
        this.defaultIndexArraySize = defaultIndexArraySize;
        this.directory = new File(directoryPath);
        this.executors = Executors.newFixedThreadPool(threadPoolSize);
        // creating log filter
        Predicate<File> logFileFilter = file -> {
            if (!file.isDirectory()) {
                for (String extension : extensions) {
                    if (file.getName().endsWith(extension)) return true;
                }
            }
            return false;
        };
        // creating new file loader
        this.fileConsumer = new FileConsumer();
        // loading all files
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.stream(files)
                    .filter(logFileFilter)
                    .forEach(file -> executors.submit(() -> fileConsumer.accept(file, StandardWatchEventKinds.ENTRY_CREATE)));
        }
        // creating and starting new file observer
        Thread observer = new Thread(new LogDirectoryObserver(fileConsumer));
        observer.start();
    }

    public LogFile getLogFile(String fileName) {
        return logFileMap.get(fileName);
    }

    public List<LogFile> getLogFiles() {
        return logFileMap.values().stream()
                .sorted((o1, o2) -> Long.valueOf(o1.lastModified()).compareTo(o2.lastModified()))
                .collect(Collectors.toList());
    }

    private class FileConsumer implements BiConsumer<File, WatchEvent.Kind> {

        @Override
        public void accept(File file, WatchEvent.Kind kind) {
            try {
                if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                    LogFile logFile = new MemoryMappedLogFile(file,
                            new NoCacheStrategy<>(),
                            new InMemoryIndexStrategy<>(defaultIndexArraySize));
                    logFileMap.put(file.getName(), logFile);
                }
            } catch (IOException e) {
                log.error("Cannot create memory mapped log file", e);
            }
        }

    }

    private class LogDirectoryObserver implements Runnable {

        private final BiConsumer<File, WatchEvent.Kind> fileConsumer;

        public LogDirectoryObserver(BiConsumer<File, WatchEvent.Kind> fileConsumer) {
            this.fileConsumer = fileConsumer;
        }

        @Override
        public void run() {
            FileSystem fileSystem = FileSystems.getDefault();
            try {
                WatchService watchService = fileSystem.newWatchService();
                directory.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    try {
                        WatchKey watchKey = watchService.take();
                        watchKey.pollEvents().stream().forEach(we -> {
                            File file = new File(directory.getPath(), we.context().toString());
                            executors.submit(() -> fileConsumer.accept(file, we.kind()));
                        });
                        if(!watchKey.reset()) break;
                        if(Thread.currentThread().isInterrupted()) break;
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                log.error("Log directory observer stopped", e);
            }
        }
    }
}
