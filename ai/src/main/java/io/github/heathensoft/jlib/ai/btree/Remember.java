package io.github.heathensoft.jlib.ai.btree;

import io.github.heathensoft.jlib.ai.btree.time.BTime;
import io.github.heathensoft.jlib.ai.btree.time.TimedTask;
import io.github.heathensoft.jlib.ai.btree.time.Timer;

/**
 * @author Frederik Dahl
 * 06/08/2022
 */


public class Remember extends Decorator implements TimedTask {
    
    private final Timer timer;
    private final Policy policy;
    
    public Remember(TreeNode child, BTime time, Policy policy, final float duration) {
        super(child);
        this.timer = new Timer(time,duration);
        this.timer.setBehaviour(this);
        this.policy = policy;
    }
    
    public Remember(TreeNode child, Policy policy, final float duration) {
        super(child);
        this.timer = new Timer(internalTime,duration);
        this.timer.setBehaviour(this);
        this.policy = policy;
    }
    
    public Remember(TreeNode child, BTime time, final float duration) {
        this(child,time,Policy.LAST_STATE,duration);
    }
    
    public Remember(TreeNode child, final float duration) {
        this(child,internalTime,Policy.LAST_STATE,duration);
    }
    
    public Remember(BTime time, Policy policy, final float duration) {
        this.timer = new Timer(time,duration);
        this.timer.setBehaviour(this);
        this.policy = policy;
    }
    
    public Remember(Policy policy, final float duration) {
        this.timer = new Timer(internalTime,duration);
        this.timer.setBehaviour(this);
        this.policy = policy;
    }
    
    public Remember(BTime time, final float duration) {
        this(time,Policy.LAST_STATE,duration);
    }
    
    public Remember(final float duration) {
        this(internalTime,Policy.LAST_STATE,duration);
    }
    
    public enum Policy {
        LAST_STATE,
        FAILIURE,
        SUCCESS
    }
    
    @Override
    protected void initialize() {
    
    }
    
    @Override
    protected Status update() {
        if (timer.isRunning())
            return status;
        Status status = child.tick();
        switch (status) {
            case RUNNING: break;
            case INVALID: throw new IllegalStateException();
            default: switch (policy) {
                    case LAST_STATE:
                        timer.start();
                        break;
                    case FAILIURE:
                        if (status == Status.FAILIURE) {
                            timer.start();
                        } break;
                    case SUCCESS:
                        if (status == Status.SUCCESS) {
                            timer.start();
                        } break;
                }
        } return status;
    }
    
    @Override
    protected void terminate(Status status) {
        // if this was aborted the timer will stop.
        // terminates child if the child is running.
        if (status == Status.RUNNING) {
            timer.stop();
            child.abort();
        }
        
    }
    
    @Override
    public void onTimerEnd(Timer timer) {
        timer.stop();
    }
    
}
