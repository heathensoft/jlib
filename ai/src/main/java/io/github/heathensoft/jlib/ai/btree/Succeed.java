package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public class Succeed extends TreeNode {
    
    public Succeed() { }
    
    @Override
    protected final Status getStatus() {
        return Status.SUCCESS;
    }
    
    @Override
    protected final Status tick() {
        return Status.SUCCESS;
    }
    
    @Override
    protected final void initialize() {
    
    }
    
    @Override
    protected final Status update() {
        return Status.SUCCESS;
    }
    
    @Override
    protected final void terminate(Status status) {
    
    }
    
    @Override
    protected final void abort() {
    
    }
}
