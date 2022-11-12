package io.github.heathensoft.jlib.common;

import io.github.heathensoft.jlib.common.storage.generic.Pool;

import java.util.concurrent.*;

/**
 * @author Frederik Dahl
 * 04/11/2022
 */


public class ThreadService implements Disposable {
    
    private final ExecutorService executor;
    private final Pool<Worker> workerPool;
    private final Object lock = new Object();
    
    public ThreadService() {
        this(4,16,3000);
    }
    
    public ThreadService(int core_pool_size, int max_pool_size, int keep_alive_time) {
        executor = new ThreadPoolExecutor(
                core_pool_size,
                max_pool_size,
                keep_alive_time,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<>()
        );
        workerPool = new Pool<>(max_pool_size) {
            @Override
            protected Worker newObject() {
                return new Worker();
            }
        }; workerPool.fill(max_pool_size);
    }
    
    public void handle(Task task) {
        handle(task,0);
    }
    
    public void handle(Task task, int priority) {
        Worker worker;
        synchronized (lock) {
            worker = workerPool.obtain();
        } worker.set(this, task, priority);
        worker.onEnqueue();
        executor.submit(worker);
    }
    
    public static void handleDirect(Task task) {
        Worker worker = new Worker();
        worker.set(null, task, 0);
        worker.onEnqueue();
        worker.run();
    }
    
    final void free(Worker worker) {
        synchronized (lock) {
            workerPool.free(worker);
        }
    }
    
    @Override
    public void dispose() {
        executor.shutdown();
    }
}
