package org.logviewer.core.search;

import org.logviewer.core.LogFile;
import org.logviewer.core.LogRecord;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Implementation of the Wrapper pattern is needed to provide search data in log files.
 */
public class SearchableLogFileWrapper implements LogFile {

    private final LogFile logFile;

    private final Predicate<LogRecord> predicate;

    public SearchableLogFileWrapper(LogFile logFile, Predicate<LogRecord> predicate) {
        this.logFile = logFile;
        this.predicate = predicate;
    }

    public SearchableLogFileWrapper(LogFile logFile, String regex) {
        this(logFile, logRecord -> logRecord.getMessage().matches(regex));
    }

    @Override
    public Iterator<LogRecord> iterator() {
        return new Iterator<LogRecord>() {

            private Iterator<LogRecord> iterator = logFile.iterator();

            private LogRecord next = null;

            @Override
            public boolean hasNext() {
                while (next == null && iterator.hasNext()) {
                    LogRecord logRecord = iterator.next();
                    if (predicate.test(logRecord)) {
                        next = logRecord;
                    }
                }
                return next != null;
            }

            @Override
            public LogRecord next() {
                if (hasNext()) {
                    LogRecord logRecord = next;
                    next = null;
                    return logRecord;
                }
                throw new IndexOutOfBoundsException();
            }
        };
    }

    public LogFile getLogFile() {
        return logFile;
    }

    @Override
    public String getName() {
        return logFile.getName();
    }

    @Override
    public long length() {
        return logFile.length();
    }

    @Override
    public long lastModified() {
        return logFile.lastModified();
    }

    @Override
    public boolean isAvailable() {
        return logFile.isAvailable();
    }

    @Override
    public LogRecord get(long n) {
        return logFile.get(n);
    }

    @Override
    public List<LogRecord> get(long first, int count) {
        return logFile.get(first, count);
    }
}
