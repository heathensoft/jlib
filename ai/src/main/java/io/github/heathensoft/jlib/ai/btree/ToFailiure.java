package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 06/08/2022
 */


public class ToFailiure extends Decorator {
    
    public ToFailiure() { }
    
    public ToFailiure(TreeNode child) {
        super(child);
    }
    
    @Override
    protected void initialize() { }
    
    @Override
    protected Status update() {
        switch (child.tick()) {
            case RUNNING:
            case SUCCESS: return Status.RUNNING;
            case FAILIURE: return Status.SUCCESS;
        } throw new IllegalStateException("INVALID");
    }
    
    @Override
    protected void terminate(Status status) {
        if (status == Status.RUNNING) child.abort();
    }
}
