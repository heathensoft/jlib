package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


abstract class Behaviour extends TreeNode {
 
    protected Status status = Status.INVALID;
    
    public final Status getStatus() {
        return status;
    }
    
    protected Status tick() {
        if (status != Status.RUNNING)
            initialize();
        status = update();
        if (status != Status.RUNNING)
            terminate(status);
        return status;
    }
    
    protected void abort() {
        if (status == Status.RUNNING) {
            terminate(status);
            status = Status.INVALID;
        }
    }
    
}
