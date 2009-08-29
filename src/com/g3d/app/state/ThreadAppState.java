package com.g3d.app.state;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The <code>ThreadAppState</code> is used to enqueue tasks to
 * execute in multiple threads. It can also be used
 * to execute a task in the rendering (GL) thread.
 */
public interface ThreadAppState extends AppService {

    /**
     * Execute a task in a thread other than the render thread
     * @param <V> Return value of the task
     * @param call The callable task to be executed.
     * @return The Future object for the task.
     */
    public <V> Future<V> executeTask(Callable<V> call);

    /**
     * Execute a task inside the render thread. This usually
     * happens after all AppStates have been updated.
     * @param <V> Return value of the task
     * @param call The callable task to be executed.
     * @return The Future object for the task.
     */
    public <V> Future<V> executeInGL(Callable<V> call);
}
