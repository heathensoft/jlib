package io.github.heathensoft.jlib.ai.btree.time;


/**
 *
 * No need to remove timers manually. They remove themselves when they end.
 * Timers are removed dynamically in the update loop.
 *
 * @author Frederik Dahl
 * 06/08/2022
 */


public class BTime {
 
    private int iCount;
    private Timer[] timers;
    
    public BTime() {
        timers = new Timer[1];
    }
    
    public BTime(int initialCapacity) {
        timers = new Timer[Math.max(1,initialCapacity)];
    }
    
    public void add(Timer timer) {
        if (iCount == timers.length) {
            Timer[] tmp = timers;
            timers = new Timer[iCount * 2];
            System.arraycopy(tmp,0,timers,0,iCount);
        } timers[iCount++] = timer;
    }
    
    public void update(float dt) {
        for (int i = iCount - 1; i >= 0; --i) {
            Timer timer = timers[i];
            if (timer.isRunning())
                timer.update(dt);
            else { timer.setInActive();
                timers[i] = timers[--iCount];
                timers[iCount] = null;
            }
        }
    }
    
    
    
    public int activeTimers() {
        return iCount;
    }
}
