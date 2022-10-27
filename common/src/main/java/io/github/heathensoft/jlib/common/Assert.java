package io.github.heathensoft.jlib.common;

import java.util.Objects;

/**
 * @author Frederik Dahl
 * 18/10/2022
 */


public class Assert {
    
    private final static String not_null = "argument cannot be null";
    
    
    public static void notNull(Object object) {
        Objects.requireNonNull(object,not_null);
    }
    
    public static void notNull(String message, Object object) {
        message = message == null ? not_null : message;
        Objects.requireNonNull(object,message);
    }
    
    public static void notNull(Object... objects) {
        notNull(not_null,objects);
    }
    
    public static void notNull(String message, Object... objects) {
        message = message == null ? not_null : message;
        notNull(message,(Object) objects);
        for (Object object : objects) {
            notNull(message,object);
        }
    }
}
