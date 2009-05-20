package com.g3d.res;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class ThreadingManager {

    protected final ExecutorService executor =
            Executors.newFixedThreadPool(10,
                                         new LoadingThreadFactory());

    protected final ContentManager owner;

    protected int nextThreadId = 0;

    public ThreadingManager(ContentManager owner){
        this.owner = owner;
    }

    protected class LoadingThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "pool" + (nextThreadId++));
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }

    protected class LoadingTask implements Callable<Object> {
        private final String resourceName;
        public LoadingTask(String resourceName){
            this.resourceName = resourceName;
        }
        public Object call() throws Exception {
            return owner.loadContent(resourceName);
        }
    }

    protected class MultiLoadingTask implements Callable<Void> {
        private final String[] resourceNames;
        public MultiLoadingTask(String[] resourceNames){
            this.resourceNames = resourceNames;
        }
        public Void call(){
            owner.loadContents(resourceNames);
            return null;
        }
    }

    public Future<Void> loadContents(String ... names){
        return executor.submit(new MultiLoadingTask(names));
    }

    public Future<Object> loadContent(String name) {
        return executor.submit(new LoadingTask(name));
    }

    public static boolean isLoadingThread() {
        return Thread.currentThread().getName().startsWith("pool");
    }


}
