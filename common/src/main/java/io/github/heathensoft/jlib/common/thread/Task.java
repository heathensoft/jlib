package io.github.heathensoft.jlib.common.thread;

/**
 * Task executed by runnable Worker
 *
 * note: total time = wait + runtime
 *
 * @author Frederik Dahl
 * 04/11/2022
 */


public interface Task {
    
    /**
     * run() called in worker
     * @param thread task running thread
     * @param waited_ms time spent in queue milliseconds
     * @throws Exception You can safely throw exceptions in method
     *         throwing an exception exits with the exception and status: -1
     */
    void start(long thread, long waited_ms) throws Exception;
    
    /**
     * Equivalent to runnable run()
     * @return status of process. Could be whatever you want.
     * @throws Exception You can safely throw exceptions in method
     *         throwing an exception exits with the exception and status: -1
     */
    int process() throws Exception;
    
    /**
     *
     * @param e exception thrown by start() and process()
     * @param status status returned by process
     * @param runtime_ms time spent processing milliseconds
     */
    void exit(Exception e, int status, long runtime_ms);
}
