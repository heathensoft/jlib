package io.github.heathensoft.jlib.ai.btree;


import io.github.heathensoft.jlib.ai.btree.time.BTime;
import io.github.heathensoft.jlib.ai.btree.time.TimedTask;
import io.github.heathensoft.jlib.ai.btree.time.Timer;

/**
 * @author Frederik Dahl
 * 06/08/2022
 */


public class Wait extends Action<Timer> implements TimedTask {
    
    
    public Wait(BTime time, final float duration) {
        super(new Timer(time,duration));
        getContext().setBehaviour(this);
    }
    
    public Wait(final float duration) {
        super(new Timer(internalTime,duration));
        getContext().setBehaviour(this);
    }
    
    @Override
    protected Status tick() {
        if (!context.isRunning()) {
            if (status != Status.RUNNING) {
                status = Status.RUNNING;
                context.start();
            } else {
                status = Status.SUCCESS;
                context.stop();
            }
        } return status;
    }
    
    @Override
    protected void initialize() { }
    
    @Override
    protected Status update() {
        return null;
    }
    
    @Override
    protected void terminate(Status status) {
        context.stop();
    }
    
    @Override
    public void onTimerEnd(Timer timer) {
        context.stop();
    }
    
}
