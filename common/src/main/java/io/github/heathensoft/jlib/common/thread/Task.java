package io.github.heathensoft.jlib.common.thread;

/**
 *
 * NO CALLS TO OpenGL!
 *
 * Task executed by Callable Worker
 *
 * note: total time = wait + runtime
 *
 * @author Frederik Dahl
 * 04/11/2022
 */


public interface Task {

    /**
     * Worker Thread
     * @return status of process. Could be whatever you want.
     * @param queue_time_ms spent in queue milliseconds
     * @throws Exception You can safely throw exceptions in method
     *         throwing an exception here completes with this exception and status: -1
     */
    int process(long queue_time_ms) throws Exception;
    
    /**
     * Super Thread
     * @param e any exception thrown by process() or RejectedExecutionException
     *          if the task got rejected (Thread pool full)
     * @param status completion status. -1 if the process threw an exception
     *               or if the task got rejected (Thread pool full)
     * @param runtime_ms time spent processing milliseconds
     */
    void onCompletion(Exception e, int status, long runtime_ms);



}
