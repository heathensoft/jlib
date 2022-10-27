package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public class Sequence extends Serial {
    
    public Sequence() { }
    
    public Sequence(TreeNode... children) {
        super(children);
    }
    
    public Sequence(boolean reactive) {
        super(reactive);
    }
    
    public Sequence(boolean reactive, boolean random) {
        super(reactive, random);
    }
    
    public Sequence(boolean reactive, TreeNode... children) {
        super(reactive, children);
    }
    
    public Sequence(boolean reactive, boolean random, TreeNode... children) {
        super(reactive, random, children);
    }
    
    @Override
    protected Status update() {
        if (status == Status.RUNNING) {
            if (reactive) children.reset();
            else switch (children.getPrevious().tick()) {
                case RUNNING: return Status.RUNNING;
                case FAILIURE: return Status.FAILIURE; }
        } while (children.notEmpty()) {
            switch ((random ? children.popRandom() : children.pop()).tick()) {
                case RUNNING: return Status.RUNNING;
                case FAILIURE: return Status.FAILIURE;
            }
        } return Status.SUCCESS;
    }
}
