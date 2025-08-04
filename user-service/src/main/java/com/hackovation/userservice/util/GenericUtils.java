package com.hackovation.userservice.util;

import java.lang.reflect.Field;

public class GenericUtils {

    public static void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null)
            return;

        Class<?> clazz = source.getClass();
        while (clazz != null && !clazz.equals(Object.class)) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(source);

                    if (value != null) {
                        Field targetField = target.getClass().getDeclaredField(field.getName());
                        targetField.setAccessible(true);
                        targetField.set(target, value);
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                    // You can log this if needed
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
