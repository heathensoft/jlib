package io.github.heathensoft.jlib.ai.btree.time;

/**
 * @author Frederik Dahl
 * 31/07/2022
 */


public interface TimedTask {
    
    /** Called when the time interval has reached zero. Here the timed task
     * is responsible for how to respond. reset timer / stop timer etc.  */
    void onTimerEnd(Timer timer);
    
    
    
}
