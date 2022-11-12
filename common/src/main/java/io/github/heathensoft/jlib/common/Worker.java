package io.github.heathensoft.jlib.common;


import io.github.heathensoft.jlib.common.storage.generic.Poolable;

/**
 * @author Frederik Dahl
 * 04/11/2022
 */


final class Worker implements Runnable, Poolable, Comparable<Worker>{
    
    private ThreadService service;
    private Task task;
    private int priority;
    private long time;
    
    Worker() { }
    
    
    void set(ThreadService service, Task task, int priority) {
        this.service = service;
        this.priority = priority;
        this.task = task;
    }
    
    void onEnqueue() {
        time = System.currentTimeMillis();
    }
    
    @Override
    public void run() {
        long wait = System.currentTimeMillis() - time;
        long thread = Thread.currentThread().getId();
        Exception exception = null;
        int status = 0;
        try { task.start(thread, wait);
            status = task.process();
        } catch (Exception e) {
            exception = e;
            status = -1;
        } finally {
            time = System.currentTimeMillis() - wait;
            task.exit(exception, status, time);
            if (service != null) service.free(this);
        }
    }
    
    @Override
    public void reset() {
        service = null;
        task = null;
    }
    
    
    @Override
    public int compareTo(Worker o) {
        return Integer.compare(o.priority,priority);
    }
}
