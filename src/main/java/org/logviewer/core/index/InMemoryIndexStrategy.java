package org.logviewer.core.index;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryIndexStrategy<T> implements IndexStrategy<T> {

    private final long[] indexArray;

    private final ReentrantReadWriteLock indexArrayRWLock;

    private long indexBlockSize = 1;

    private long length = 0;

    private long position = 0;

    private volatile IndexStrategyState state = IndexStrategyState.NEW;

    public InMemoryIndexStrategy(int indexArraySize) {
        indexArray = new long[indexArraySize];
        indexArrayRWLock = new ReentrantReadWriteLock();
    }

    @Override
    public IndexStrategyState getState() {
        return state;
    }

    @Override
    public void init(Iterator<T> iterator, T splitValue) {
        state = IndexStrategyState.IS_PROCESSED;
        indexArrayRWLock.writeLock().lock();
        int inIndexBlockPosition = 0;
        int indexBlockPosition = 1;
        try {
            while (iterator.hasNext()) {
                if (iterator.next().equals(splitValue)) {
                    length++;
                    inIndexBlockPosition++;
                    if (inIndexBlockPosition == indexBlockSize) {
                        inIndexBlockPosition = 0;
                        if (indexBlockPosition == indexArray.length) {
                            for (int i = 0; i < indexArray.length / 2; i++) {
                                indexArray[i] = indexArray[i * 2];
                            }
                            indexBlockSize *= 2;
                            indexBlockPosition /= 2;
                        }
                        indexArray[indexBlockPosition++] = position + 1;
                    }
                }
                position++;
            }
        }
        finally {
            indexArrayRWLock.writeLock().unlock();
            state = IndexStrategyState.HAS_BEEN_CONSTRUCTED;
        }
    }

    @Override
    public Index getIndex(long n) {
        if (state != IndexStrategyState.HAS_BEEN_CONSTRUCTED) throw new UnsupportedOperationException();
        indexArrayRWLock.readLock().lock();
        try {
            if (n < 0 || n >= length) throw new IndexOutOfBoundsException();
            long indexBlockPosition = n / indexBlockSize;
            long inIndexBlockPosition = n % indexBlockSize;
            if (indexBlockPosition > Integer.MAX_VALUE) throw new IndexOutOfBoundsException();
            return new Index(indexArray[(int) indexBlockPosition],
                    indexBlockSize,
                    inIndexBlockPosition,
                    indexBlockPosition + 1 == indexArray.length ? position : indexArray[(int) (indexBlockPosition + 1)]);
        }
        finally {
            indexArrayRWLock.readLock().unlock();
        }
    }

    @Override
    public long getLength() {
        if (state != IndexStrategyState.HAS_BEEN_CONSTRUCTED) throw new UnsupportedOperationException();
        indexArrayRWLock.readLock().lock();
        try {
            return length;
        }
        finally {
            indexArrayRWLock.readLock().unlock();
        }
    }
}
