package io.github.heathensoft.jlib.common;

/**
 * @author Frederik Dahl
 * 01/04/2024
 */

@FunctionalInterface
public interface Executor {

    Executor voidExecutor = () -> {};
    void execute();

}
