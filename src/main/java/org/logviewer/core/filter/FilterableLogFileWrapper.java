package org.logviewer.core.filter;

import org.logviewer.core.LogFile;
import org.logviewer.core.LogRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wrapper for filtering data in log file, wrapper pattern implementation.
 */
public class FilterableLogFileWrapper implements LogFile {

    private final LogFile logFile;

    private final List<Long> matchingRows = new ArrayList<>();

    public FilterableLogFileWrapper(LogFile logFile, Predicate<LogRecord> predicate) {
        this.logFile = logFile;
        for (LogRecord logRecord : logFile) {
            if (predicate.test(logRecord)) matchingRows.add(logRecord.getId());
        }
    }

    public FilterableLogFileWrapper(LogFile logFile, String regex) {
        this(logFile, logRecord -> logRecord.getMessage().matches(regex));
    }

    @Override
    public Iterator<LogRecord> iterator() {
        return new Iterator<LogRecord>() {

            private Iterator<Long> iterator = matchingRows.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public LogRecord next() {
                return logFile.get(iterator.next());
            }
        };
    }

    @Override
    public long length() {
        return matchingRows.size();
    }

    @Override
    public LogRecord get(long n) {
        if (n > Integer.MAX_VALUE) throw new IndexOutOfBoundsException();
        return logFile.get(matchingRows.get((int) n));
    }

    @Override
    public List<LogRecord> get(long first, int count) {
        if (count < 0 || first + count > Integer.MAX_VALUE) throw new IndexOutOfBoundsException();
        return matchingRows.subList((int) first, (int) first + count)
                .stream()
                .map(id -> logFile.get(id))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return logFile.getName();
    }

    @Override
    public long lastModified() {
        return logFile.lastModified();
    }

    @Override
    public boolean isAvailable() {
        return logFile.isAvailable();
    }
}
