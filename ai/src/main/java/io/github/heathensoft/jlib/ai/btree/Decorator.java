package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


public abstract class Decorator extends Behaviour {
    
    protected TreeNode child;
    
    public Decorator() { }
    
    public Decorator(TreeNode child) {
        this.child = child;
    }
    
    public void setChild(TreeNode child) {
        this.child = child;
    }
}
