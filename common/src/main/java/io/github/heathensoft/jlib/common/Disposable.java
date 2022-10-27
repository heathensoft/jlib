package io.github.heathensoft.jlib.common;

/**
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
