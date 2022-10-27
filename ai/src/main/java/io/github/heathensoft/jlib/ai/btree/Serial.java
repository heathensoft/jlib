package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


abstract class Serial extends Composite {
    
    protected boolean reactive;
    protected boolean random;
    
    protected Serial() { }
    
    protected Serial(TreeNode... children) {
        super(children);
    }
    
    protected Serial(boolean reactive) {
        this.reactive = reactive;
    }
    
    protected Serial(boolean reactive, boolean random) {
        this.reactive = reactive;
        this.random = random;
    }
    
    protected Serial(boolean reactive, TreeNode... children) {
        super(children);
        this.reactive = reactive;
    }
    
    protected Serial(boolean reactive, boolean random, TreeNode... children) {
        super(children);
        this.reactive = reactive;
        this.random = random;
    }
    
    @Override
    protected final void initialize() {
        children.reset();
    }
    
    @Override
    protected final void terminate(Status status) {
        int childCount = children.capacity();
        for (int i = 0; i < childCount; i++) {
            children.get(i).abort();
        }
    }
    
}
