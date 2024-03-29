package io.github.heathensoft.jlib.common;

/**
 * Disposable is meant for resources allocated outside the Java heap.
 * Native memory or GPU storage.
 *
 * @author Frederik Dahl
 * 07/01/2022
 */


public interface Disposable {
    
    static void dispose(Disposable disposable) {
        if (disposable != null) disposable.dispose();
    }
    
    static void dispose(Disposable ...disposables) {
        if (disposables != null)
            for (Disposable disposable : disposables)
                dispose(disposable);
    }
    
    void dispose();
    
}
