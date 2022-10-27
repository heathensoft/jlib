package io.github.heathensoft.jlib.ai.btree;

/**
 * @author Frederik Dahl
 * 06/08/2022
 */


public abstract class BTree {
 
    protected Behaviour root;
    
    public Status tick() {
        return root.tick();
    }
    
    public void reset() {
        if (root != null) root.abort();
    }
    
    public void setRoot(Behaviour root) {
        reset();
        this.root = root;
    }
    
    public static void updateInternalTime(float dt) {
        TreeNode.internalTime.update(dt);
    }
    
    public static Selector passiveSelector(TreeNode... children) {
        return new Selector(false,false,children);
    }
    
    public static Selector reactiveSelector(TreeNode... children) {
        return new Selector(true,false,children);
    }
    
    public static Selector passiveSelectorRND(TreeNode... children) {
        return new Selector(false,true,children);
    }
    
    public static Selector reactiveSelectorRND(TreeNode... children) {
        return new Selector(true,true,children);
    }
    
    public static Sequence passiveSequence(TreeNode... children) {
        return new Sequence(false,false,children);
    }
    
    public static Sequence reactiveSequence(TreeNode... children) {
        return new Sequence(true,false,children);
    }
    
    public static Sequence passiveSequenceRND(TreeNode... children) {
        return new Sequence(false,true,children);
    }
    
    public static Sequence reactiveSequenceRND(TreeNode... children) {
        return new Sequence(true,true,children);
    }
    
    public static Fail fail() {
        return new Fail();
    }
    
    public static Succeed succeed() {
        return new Succeed();
    }
    
    public static Invert invert(TreeNode child) {
        return new Invert(child);
    }
    
    public static Wait wait(final float duration) {
        return new Wait(duration);
    }
    
    public static Parallel parallelRequireONE(TreeNode... children) {
        return new Parallel(Parallel.Policy.REQUIRE_ONE,children);
    }
    
    public static Parallel parallelRequireALL(TreeNode... children) {
        return new Parallel(Parallel.Policy.REQUIRE_ALL,children);
    }
    
    public static Remember rememberSuccess(TreeNode child, final float duration) {
        return new Remember(child,TreeNode.internalTime, Remember.Policy.SUCCESS,duration);
    }
    
    public static Remember rememberFailiure(TreeNode child, final float duration) {
        return new Remember(child,TreeNode.internalTime, Remember.Policy.FAILIURE,duration);
    }
    
    public static Remember rememberLast(TreeNode child, final float duration) {
        return new Remember(child,TreeNode.internalTime, Remember.Policy.LAST_STATE,duration);
    }
    
    public static Repeat repeat(TreeNode child, final int times) {
        return new Repeat(child, times);
    }
    
    public static ToFailiure toFailiure(TreeNode child) {
        return new ToFailiure(child);
    }
}
