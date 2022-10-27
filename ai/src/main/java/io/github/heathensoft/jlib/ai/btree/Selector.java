package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public class Selector extends Serial {
    
    public Selector() { }
    
    public Selector(TreeNode... children) {
        super(children);
    }
    
    public Selector(boolean reactive) {
        super(reactive);
    }
    
    public Selector(boolean reactive, boolean random) {
        super(reactive, random);
    }
    
    public Selector(boolean reactive, TreeNode... children) {
        super(reactive, children);
    }
    
    public Selector(boolean reactive, boolean random, TreeNode... children) {
        super(reactive, random, children);
    }
    
    @Override
    protected Status update() {
        if (status == Status.RUNNING) {
            if (reactive) children.reset();
            else switch (children.getPrevious().tick()) {
                    case RUNNING: return Status.RUNNING;
                    case SUCCESS: return Status.SUCCESS; }
        } while (children.notEmpty()) {
            switch ((random ? children.popRandom() : children.pop()).tick()) {
                case RUNNING: return Status.RUNNING;
                case SUCCESS: return Status.SUCCESS;
            }
        } return Status.FAILIURE;
    }
}
