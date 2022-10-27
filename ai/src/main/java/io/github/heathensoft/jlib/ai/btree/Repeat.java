package io.github.heathensoft.jlib.ai.btree;

/**
 * Repeats a child until it succeeds n-times or fails during.
 *
 * @author Frederik Dahl
 * 06/08/2022
 */


public class Repeat extends Decorator {
    
    protected final int times;
    protected int count;
    
    public Repeat(int times) {
        if (times <= 0) throw new IllegalStateException("repeat <= 0");
        this.times = times;
    }
    
    public Repeat(TreeNode child, int times) {
        super(child);
        if (times <= 0) throw new IllegalStateException("repeat <= 0");
        this.times = times;
    }
    
    @Override
    protected void initialize() { }
    
    @Override
    protected Status update() {
        while (true) {
            switch (child.tick()) {
                case RUNNING: return Status.RUNNING;
                case FAILIURE: return Status.FAILIURE;
                case SUCCESS: if (++count == times) return Status.SUCCESS; break;
                case INVALID: throw new IllegalStateException();
            }
        }
    }
    
    @Override
    protected void terminate(Status status) {
        if (status == Status.RUNNING) child.abort();
        count = 0;
        
    }
    
}
