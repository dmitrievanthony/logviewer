package org.logviewer.core;

import java.util.List;

public interface LogFile extends Iterable<LogRecord> {

    static final char SPLIT_CHAR = '\n';

    String getName();

    long length();

    long lastModified();

    boolean isAvailable();

    LogRecord get(long n);

    List<LogRecord> get(long first, int count);
}
