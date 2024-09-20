package io.github.heathensoft.jlib.common;

/**
 * @author Frederik Dahl
 * 11/05/2024
 */


@FunctionalInterface
public interface ChainEvent<T> {
    ChainEvent<T> apply(T t);
}
