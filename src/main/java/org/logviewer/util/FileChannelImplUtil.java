package org.logviewer.util;

import sun.nio.ch.FileChannelImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

public class FileChannelImplUtil {

    public static long mmap(FileChannel channel, int p, long position, long length) throws IOException {
        try {
            Method map = FileChannelImpl.class.getDeclaredMethod("map0", int.class, long.class, long.class);
            map.setAccessible(true);
            return (Long) map.invoke(channel, p, position, length);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UnsupportedClassVersionError();
        } catch (InvocationTargetException e) {
            throw new IOException();
        }
    }

    public static void unmmap(long address, long length) throws IOException {
        try {
            Method unmmap = FileChannelImpl.class.getDeclaredMethod("unmap0", long.class, long.class);
            unmmap.setAccessible(true);
            unmmap.invoke(null, address, length);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UnsupportedClassVersionError();
        } catch (InvocationTargetException e) {
            throw new IOException();
        }
    }
}
