package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public class Fail extends TreeNode {
    
    public Fail() { }
    
    @Override
    protected final Status getStatus() {
        return Status.FAILIURE;
    }
    
    @Override
    protected final Status tick() {
        return Status.FAILIURE;
    }
    
    @Override
    protected final void initialize() {
    
    }
    
    @Override
    protected final Status update() {
        return Status.FAILIURE;
    }
    
    @Override
    protected final void terminate(Status status) {
    
    }
    
    @Override
    protected final void abort() {
    
    }
    
}
