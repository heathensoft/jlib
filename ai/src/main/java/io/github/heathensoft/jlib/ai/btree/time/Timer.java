package io.github.heathensoft.jlib.ai.btree.time;

/**
 * @author Frederik Dahl
 * 31/07/2022
 */


public class Timer {
    
    private TimedTask behaviour;
    private final BTime internalTime;
    private final float duration;
    private float accumulator;
    private boolean running;
    private boolean inActive;
    
    public Timer(BTime internalTime, final float duration) {
        this.internalTime = internalTime;
        this.duration = duration;
        this.inActive = true;
        this.running = false;
    }
    
    public void setBehaviour(TimedTask behaviour) {
        this.behaviour = behaviour;
    }
    
    protected void update(float dt) {
        accumulator += dt;
        if (accumulator >= duration) {
            behaviour.onTimerEnd(this);
        }
    }
    
    public void start() {
        running = true;
        if (inActive) {
            internalTime.add(this);
            inActive = false;
        }
    }
    
    public void pause() {
        running = false;
    }
    
    public void stop() {
        accumulator = 0;
        running = false;
    }
    
    public void reset() {
        accumulator = 0;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    protected void setInActive() {
        inActive = true;
    }
    
    
}
