package org.logviewer.core;

import org.logviewer.core.cache.CacheStrategy;
import org.logviewer.core.index.Index;
import org.logviewer.core.index.IndexStrategy;
import org.logviewer.core.index.IndexStrategyState;
import org.logviewer.util.FileChannelImplUtil;
import org.logviewer.util.UnsafeUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * LogFile implementation is used to access file content by the mmap function with
 * delegation of loading data from disk into main memory. Because the standart Java
 * API don't provide the method for mmap of file size larger than 2Gb and so
 * reflection call of mmap method of FileChannelImpl and Unsafe interface is used instead.
 */
public class MemoryMappedLogFile implements LogFile, Closeable {

    private final File file;

    private final RandomAccessFile randomAccessFile;

    private final long baseMmapAddress;

    private volatile CacheStrategy<Long, LogRecord> cacheStrategy;

    private volatile IndexStrategy<Byte> indexStrategy;

    public MemoryMappedLogFile(File file, CacheStrategy<Long, LogRecord> cacheStrategy, IndexStrategy<Byte> indexStrategy) throws IOException {
        if (file == null || cacheStrategy == null || indexStrategy == null) {
            throw new NullPointerException();
        }
        this.file = file;
        this.cacheStrategy = cacheStrategy;
        this.indexStrategy = indexStrategy;
        this.randomAccessFile = new RandomAccessFile(file, "r");
        this.baseMmapAddress = FileChannelImplUtil.mmap(randomAccessFile.getChannel(), 0, 0L, randomAccessFile.length());
        long time = System.currentTimeMillis();
        this.indexStrategy.init(new MemoryMappedFileByteIterator(), (byte) SPLIT_CHAR);
        System.out.println("Indexing of [" + file.getName() + "] done (" + (System.currentTimeMillis() - time) + "ms)");
    }

    @Override
    public LogRecord get(long n) {
        if (n < 0 || n >= indexStrategy.getLength()) throw new IndexOutOfBoundsException();
        // looking up in cache
        LogRecord logRecord = cacheStrategy.get(n);
        if (logRecord != null) return logRecord;
        // looking up in file
        StringBuilder builder = new StringBuilder();
        long rowOffset = getRowOffset(n);
        try {
            long fileLength = randomAccessFile.length();
            for (long i = rowOffset; i < fileLength; i++) {
                char ch = (char) UnsafeUtil.getUnsafe().getByte(baseMmapAddress + i);
                if (ch == SPLIT_CHAR || i == fileLength - 1) {
                    logRecord = new LogRecord(n, builder.toString());
                    cacheStrategy.setIfAbsent(n, logRecord);
                    return logRecord;
                }
                builder.append(ch);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        throw new ArithmeticException(); // write to log, unreachable state
    }

    @Override
    public List<LogRecord> get(long first, int count) {
        if (first < 0 || first >= indexStrategy.getLength() || count < 0) throw new IndexOutOfBoundsException();
        if (first + count >= indexStrategy.getLength()) {
            if (indexStrategy.getLength() - first > Integer.MAX_VALUE) throw new IndexOutOfBoundsException();
            count = (int) (indexStrategy.getLength() - first);
        }
        List<LogRecord> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) result.add(null);
        // looking up in cache
        int firstOutOfCacheElement = -1;
        for (int i = 0; i < count; i++) {
            LogRecord record = cacheStrategy.get(first + i);
            if (record != null){
                result.set(i, record);
            }
            else if (firstOutOfCacheElement < 0){
                firstOutOfCacheElement = i;
            }
        }
        if (firstOutOfCacheElement < 0) return result; // all log records has been extracted from cache
        StringBuilder builder = new StringBuilder();
        // looking up in file (from first not found row)
        long rowOffset = getRowOffset(first + firstOutOfCacheElement);
        try {
            long fileLength = randomAccessFile.length();
            for (long i = rowOffset; i < fileLength; i++) {
                char ch = (char) UnsafeUtil.getUnsafe().getByte(baseMmapAddress + i);
                if (ch == SPLIT_CHAR || i == fileLength - 1) {
                    if (result.get(firstOutOfCacheElement) == null) {
                        LogRecord logRecord = new LogRecord(first + firstOutOfCacheElement, builder.toString());
                        cacheStrategy.setIfAbsent(first + firstOutOfCacheElement, logRecord);
                        result.set(firstOutOfCacheElement, logRecord);
                        builder.setLength(0);
                    }
                    // else log record already has been extracted from cache
                    firstOutOfCacheElement++;
                    if (firstOutOfCacheElement == count) {
                        return result;
                    }
                }
                else {
                    if (result.get(firstOutOfCacheElement) == null) {
                        builder.append(ch);
                    }
                    // else log record already has been extracted from cache
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        throw new ArithmeticException(); // write to log, unreachable state
    }

    /*
    public void iterate(long from, long to, Consumer<LogRecord> consumer) {
        if (from < 0 || to > length() || from >= to) throw new IndexOutOfBoundsException();
        long firstRowPosition = getRowOffset(from);
        StringBuilder builder = new StringBuilder();
        try {
            long fileLength = randomAccessFile.length();
            for (long i = firstRowPosition; i < fileLength; i++) {
                char ch = (char) UnsafeUtil.getUnsafe().getByte(baseMmapAddress + i);
                if (ch == SPLIT_CHAR || i == fileLength - 1) {
                    LogRecord logRecord = new LogRecord(from++, builder.toString());
                    cacheStrategy.setIfAbsent(logRecord.getId(), logRecord);
                    consumer.accept(logRecord);
                    builder.setLength(0);
                } else builder.append(ch);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }*/

    private long getRowOffset(long n) {
        Index index = indexStrategy.getIndex(n);
        long inIndexBlockPosition = index.getPositionInBlock();
        if (inIndexBlockPosition == 0) return index.getBlockOffset();
        for (long i = 0; i < index.getBlockLength(); i++) {
            char ch = (char) UnsafeUtil.getUnsafe().getByte(baseMmapAddress + index.getBlockOffset() + i);
            if (ch == SPLIT_CHAR) {
                inIndexBlockPosition = inIndexBlockPosition - 1;
                if (inIndexBlockPosition == 0) {
                    return index.getBlockOffset() + i + 1;
                }
            }
        }
        throw new ArithmeticException(); // write to log, unreachable state
    }

    @Override
    public boolean isAvailable() {
        return indexStrategy.getState().equals(IndexStrategyState.HAS_BEEN_CONSTRUCTED);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long length() {
        return indexStrategy.getLength();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public void close() throws IOException {
        FileChannelImplUtil.unmmap(baseMmapAddress, randomAccessFile.length());
        randomAccessFile.close();
    }

    @Override
    public Iterator<LogRecord> iterator() {
        Iterator<LogRecord> iterator = null;
        try {
            iterator = new MemoryMappedFileRowIterator();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return iterator;
    }

    private class MemoryMappedFileRowIterator implements Iterator<LogRecord> {

        private final Iterator<Byte> byteIterator;

        private final StringBuilder builder = new StringBuilder();

        private long index = 0;

        public MemoryMappedFileRowIterator() throws IOException {
            this.byteIterator = new MemoryMappedFileByteIterator();
        }

        @Override
        public boolean hasNext() {
            return byteIterator.hasNext();
        }

        @Override
        public LogRecord next() {
            while (hasNext()) {
                byte next = byteIterator.next();
                if (next == SPLIT_CHAR) {
                    LogRecord logRecord = new LogRecord(index++, builder.toString());
                    builder.setLength(0);
                    return logRecord;
                }
                builder.append((char) next);
            }
            return new LogRecord(index++, builder.toString());
        }
    }

    private class MemoryMappedFileByteIterator implements Iterator<Byte> {

        private long position = 0;

        private long size;

        public MemoryMappedFileByteIterator() throws IOException {
            this.size = randomAccessFile.length();
        }

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public Byte next() {
            return UnsafeUtil.getUnsafe().getByte(baseMmapAddress + position++);
        }
    }
}
