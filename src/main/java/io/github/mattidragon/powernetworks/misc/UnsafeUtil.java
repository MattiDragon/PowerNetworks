package io.github.mattidragon.powernetworks.misc;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

public class UnsafeUtil {
    private UnsafeUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T createUnsafe(Class<T> tClass) {
        try {
            return (T) UnsafeAccess.UNSAFE.allocateInstance(tClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to unsafely create object", e);
        }
    }
}
