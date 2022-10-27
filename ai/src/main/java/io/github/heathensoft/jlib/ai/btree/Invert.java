package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public class Invert extends Decorator {
    
    public Invert() { }
    
    public Invert(TreeNode child) {
        super(child);
    }
    
    @Override
    protected void initialize() { }
    
    @Override
    protected Status update() {
        switch (child.tick()) {
            case RUNNING: return Status.RUNNING;
            case SUCCESS: return Status.FAILIURE;
            case FAILIURE: return Status.SUCCESS;
            default: throw new IllegalStateException();
        }
    }
    
    @Override
    protected void terminate(Status status) {
        if (status == Status.RUNNING) child.abort();
    }
}
