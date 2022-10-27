package io.github.heathensoft.jlib.ai.btree;

import org.joml.Random;

/**
 * @author Frederik Dahl
 * 05/08/2022
 */


abstract class Composite extends Behaviour {
    
    protected NodeList children;
    
    protected Composite() { }
    
    protected Composite(TreeNode... children) {
        this.children = new NodeList(children);
    }
    
    public void setChildren(TreeNode... children) {
        this.children = new NodeList(children);
    }
    
    protected final static class NodeList {
        
        private static final Random rnd = new Random(System.currentTimeMillis());
        
        private final TreeNode[] nodes;
        private int remaining;
        
        NodeList(TreeNode... nodes) {
            if (nodes == null) throw new IllegalStateException("Null argument for ...nodes");
            this.nodes = nodes;
            this.remaining = capacity();
        }
        
        TreeNode getPrevious() {
            return nodes[remaining];
        }
        
        TreeNode get(int index) {
            return nodes[index];
        }
        
        TreeNode pop() {
            return nodes[--remaining];
        }
        
        TreeNode popRandom() {
            int index = rnd.nextInt(remaining--);
            if (index == remaining) return nodes[remaining];
            TreeNode node = nodes[index];
            nodes[index] = nodes[remaining];
            nodes[remaining] = node;
            return node;
        }
        
        void reset() {
            remaining = capacity();
        }
        
        boolean isReset() {
            return remaining == capacity();
        }
        
        boolean notReset() {
            return remaining < capacity();
        }
        
        int remaining() {
            return remaining;
        }
        
        int capacity() {
            return nodes.length;
        }
        
        boolean isEmpty() {
            return remaining == 0;
        }
        
        boolean notEmpty() {
            return remaining > 0;
        }
    }
    
}
