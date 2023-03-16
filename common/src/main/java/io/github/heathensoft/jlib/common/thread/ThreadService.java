package io.github.heathensoft.jlib.common.thread;

import io.github.heathensoft.jlib.common.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Frederik Dahl
 * 04/11/2022
 */


public class ThreadService implements Disposable {
    
    private final ExecutorService executor;
    private final List<Future<Worker>> futures;
    
    public ThreadService() {
        this(4,24,3000);
    }
    
    public ThreadService(int core_pool_size, int max_pool_size, int keep_alive_time) {
        futures = new ArrayList<>();
        executor = new ThreadPoolExecutor(
                core_pool_size,
                max_pool_size,
                keep_alive_time,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(512));
    }

    public void update() {
        if (!executor.isShutdown()) {
            for (int i = futures.size() - 1; i >= 0; i--) {
                if (futures.get(i).isDone()) {
                    Worker worker = null;
                    try { futures.remove(i).get().finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void handle(Task task) {
        if (executor.isShutdown()) handleDirect(task);
        try { futures.add(executor.submit(new Worker(task)));
        } catch (RejectedExecutionException e) {
            task.onCompletion(e,-1,0);
        }
    }
    
    public static void handleDirect(Task task) {
        new Worker(task).call().finish();
    }

    public ThreadPoolExecutor executor() {
        return ((ThreadPoolExecutor)executor);
    }

    public void dispose() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30,TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(30,TimeUnit.SECONDS)) {
                    System.err.println("Thread-Pools failed to terminate after 60 seconds");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static final class Worker implements Callable<Worker> {

        private Exception exception;
        private final Task task;
        private int status;
        private long time;

        Worker(Task task) {
            this.time = System.currentTimeMillis();
            this.task = task;
        }

        public Worker call() {
            long wait = System.currentTimeMillis() - time;
            try { status = task.process(wait);
            } catch (Exception e) {
                exception = e;
                status = -1;
            } finally {
                time = System.currentTimeMillis() - time - wait;
            } return this;
        }

        void finish() {
            task.onCompletion(exception, status, time);
        }


    }
}
