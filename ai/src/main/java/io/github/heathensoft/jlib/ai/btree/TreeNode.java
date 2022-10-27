package io.github.heathensoft.jlib.ai.btree;


import io.github.heathensoft.jlib.ai.btree.time.BTime;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public abstract class TreeNode {
    
    protected static final BTime internalTime = new BTime(16);
    
    protected TreeNode() { }
    
    protected abstract Status getStatus();
    
    protected abstract Status tick();
    
    /**
     * Called at the start of a tick if the node is not already RUNNING
     */
    protected abstract void initialize();
    
    protected abstract Status update();
    
    /**
     * Terminate is called on exiting an update with status != RUNNING,
     * or when abort() is called on a currently running TreeNode.
     * @param status the status of the node at the momement of termination.
     */
    protected abstract void terminate(Status status);
    
    /**
     * Terminates the node if its currently running
     */
    protected abstract void abort();
    
    
}
