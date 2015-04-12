package org.logviewer.core.index;

import java.io.IOException;
import java.util.Iterator;

/**
 * Strategy pattern implementation is used for indexation of log files.
 * @param <T>
 */
public interface IndexStrategy<T> {

    void init(Iterator<T> iterator, T splitValue) throws IOException;

    IndexStrategyState getState();

    Index getIndex(long n);

    long getLength();
}
