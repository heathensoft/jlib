package io.github.heathensoft.jlib.ai.btree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Frederik Dahl
 * 07/08/2022
 */


public class Parallel extends Composite {
    
    private final Queue<TreeNode> running;
    private final Policy policy;
    private int successCount;
    
    public enum Policy {
        REQUIRE_ONE,
        REQUIRE_ALL
    }
    
    public Parallel() {
        this.running = new LinkedList<>();
        this.policy = Policy.REQUIRE_ALL;
    }
    
    public Parallel(Policy policy) {
        this.running = new LinkedList<>();
        this.policy = policy;
    }
    
    public Parallel(TreeNode... children) {
        super(children);
        this.running = new LinkedList<>();
        this.policy = Policy.REQUIRE_ALL;
    }
    
    public Parallel(Policy policy, TreeNode... children) {
        super(children);
        this.running = new LinkedList<>();
        this.policy = policy;
    }
    
    @Override
    protected void initialize() {
    
    }
    
    @Override
    protected Status update() {
        Status childStatus;
        TreeNode child;
        int childCount;
        if (running.isEmpty()) {
            childCount = children.capacity();
            for (int i = 0; i < childCount; i++) {
                child = children.get(i);
                childStatus = child.tick();
                switch (childStatus) {
                    case RUNNING:
                        running.add(child);
                        break;
                    case SUCCESS:
                        if (policy == Policy.REQUIRE_ONE) {
                            return Status.SUCCESS;
                        } successCount++;
                        break;
                    case FAILIURE:
                        if (policy == Policy.REQUIRE_ALL) {
                            return Status.FAILIURE;
                        } break;
                    default: throw new IllegalStateException();
                }
            }
        } else { childCount = running.size();
            for (int i = 0; i < childCount; i++) {
                child = running.remove();
                childStatus = child.tick();
                switch (childStatus) {
                    case RUNNING:
                        running.add(child);
                        break;
                    case SUCCESS:
                        if (policy == Policy.REQUIRE_ONE) {
                            return Status.SUCCESS;
                        } successCount++;
                        break;
                    case FAILIURE:
                        if (policy == Policy.REQUIRE_ALL) {
                            return Status.FAILIURE;
                        } break;
                    default: throw new IllegalStateException();
                }
            }
            
        } if (running.isEmpty()) {
            if (policy == Policy.REQUIRE_ALL) {
                if (successCount == children.capacity()) {
                    return Status.SUCCESS;
                } else throw new IllegalStateException();
            } else /* REQUIRE_ONE */ {
                if (successCount == 0) {
                    return Status.FAILIURE;
                } else throw new IllegalStateException();
            }
        } return Status.RUNNING;
    }
    
    @Override
    protected void terminate(Status status) {
        while (!running.isEmpty()) {
            running.poll().abort();
        } successCount = 0;
    }
}
