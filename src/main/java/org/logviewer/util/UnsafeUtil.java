package org.logviewer.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {

    private static Unsafe unsafe;

    static {
        try {
            Field theUnsafeInnerField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeInnerField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeInnerField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(System.out);
        }
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }
}
