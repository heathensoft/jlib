package io.github.heathensoft.jlib.ai.btree;

/**
 * I stripped down Condition to the essentials. No initialize, terminate or abort.
 * Only a query to return true/false for SUCCESS/FAILIURE.
 * It does not store its status. getStatus() returns INVALID.
 * A condition node should ideally be state checks and lighter calculations anyways.
 * This is a good reason to keep it that way.
 *
 * @author Frederik Dahl
 * 05/08/2022
 */


public abstract class Condition<C> extends TreeNode {
    
    protected C context;
    
    public Condition(C context) {
        this.context = context;
    }
    
    @Override
    protected final Status getStatus() {
        return Status.INVALID;
    }
    
    @Override
    protected final Status tick() {
        return query() ? Status.SUCCESS : Status.FAILIURE;
    }
    
    protected abstract boolean query();
    
    @Override
    protected final void initialize() { }
    
    @Override
    protected final Status update() {
        return Status.INVALID;
    }
    
    @Override
    protected final void terminate(Status status) { }
    
    @Override
    protected final void abort() { }
}
