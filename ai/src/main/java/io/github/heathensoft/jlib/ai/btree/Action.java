package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public abstract class Action<C> extends Behaviour {
    
    protected final C context;
    
    public Action(C context) {
        this.context = context;
    }
    
    public C getContext() {
        return context;
    }
    
}
