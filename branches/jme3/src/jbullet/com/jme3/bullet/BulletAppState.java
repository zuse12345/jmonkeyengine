/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.bullet;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsSpace.BroadphaseType;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author normenhansen
 */
public class BulletAppState implements AppState, PhysicsTickListener {

    protected boolean initialized = false;
    protected Application app;
    protected AppStateManager stateManager;
    protected ScheduledThreadPoolExecutor executor;
    protected PhysicsSpace pSpace;
    protected ThreadingType threadingType = ThreadingType.SEQUENTIAL;
    protected BroadphaseType broadphaseType = BroadphaseType.DBVT;
    protected Vector3f worldMin = new Vector3f(-10000f, -10000f, -10000f);
    protected Vector3f worldMax = new Vector3f(10000f, 10000f, 10000f);
    private float speed = 1;
    protected float tpf;
    protected Future physicsFuture;

    public BulletAppState() {
    }

    public BulletAppState(Vector3f worldMin, Vector3f worldMax) {
        this(worldMin, worldMax, BroadphaseType.DBVT);
    }

    public BulletAppState(Vector3f worldMin, Vector3f worldMax, BroadphaseType broadphaseType) {
        this.worldMin.set(worldMin);
        this.worldMax.set(worldMax);
        this.broadphaseType = broadphaseType;
    }

    private boolean startPhysicsOnExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
        executor = new ScheduledThreadPoolExecutor(1);
        final BulletAppState app = this;
        Callable<Boolean> call = new Callable<Boolean>() {

            public Boolean call() throws Exception {
                detachedPhysicsLastUpdate = System.currentTimeMillis();
                pSpace = new PhysicsSpace(worldMin, worldMax, broadphaseType);
                pSpace.addTickListener(app);
                return true;
            }
        };
        try {
            return executor.submit(call).get();
        } catch (InterruptedException ex) {
            Logger.getLogger(BulletAppState.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ExecutionException ex) {
            Logger.getLogger(BulletAppState.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    private Callable<Boolean> parallelPhysicsUpdate = new Callable<Boolean>() {

        public Boolean call() throws Exception {
            pSpace.update(tpf * getSpeed());
            return true;
        }
    };
    long detachedPhysicsLastUpdate = 0;
    private Callable<Boolean> detachedPhysicsUpdate = new Callable<Boolean>() {

        public Boolean call() throws Exception {
            pSpace.update(getPhysicsSpace().getAccuracy() * getSpeed());
            long update = System.currentTimeMillis() - detachedPhysicsLastUpdate;
            detachedPhysicsLastUpdate = System.currentTimeMillis();
            executor.schedule(detachedPhysicsUpdate, Math.round(getPhysicsSpace().getAccuracy() * 1000000.0f) - (update * 1000), TimeUnit.MICROSECONDS);
            return true;
        }
    };

    public PhysicsSpace getPhysicsSpace() {
        return pSpace;
    }

    public void startPhysics() {
        //start physics thread(pool)
        if (threadingType == ThreadingType.PARALLEL) {
            startPhysicsOnExecutor();
        } else if (threadingType == ThreadingType.DETACHED) {
            startPhysicsOnExecutor();
            executor.submit(detachedPhysicsUpdate);
        } else {
            pSpace = new PhysicsSpace(worldMin, worldMax, broadphaseType);
        }
        pSpace.addTickListener(this);
        initialized = true;
    }

    public void initialize(AppStateManager stateManager, Application app) {
        if (!initialized) {
            startPhysics();
        }
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void stateAttached(AppStateManager stateManager) {
        if (!initialized) {
            startPhysics();
        }
        if (threadingType == ThreadingType.PARALLEL) {
            PhysicsSpace.setLocalThreadPhysicsSpace(pSpace);
        }
    }

    public void stateDetached(AppStateManager stateManager) {
    }

    public void update(float tpf) {
        this.tpf = tpf;
        //TODO: move to postRender()
        if (physicsFuture != null) {
            try {
                physicsFuture.get();
                physicsFuture = null;
            } catch (InterruptedException ex) {
                Logger.getLogger(BulletAppState.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(BulletAppState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void render(RenderManager rm) {
        if (threadingType == ThreadingType.PARALLEL) {
            physicsFuture = executor.submit(parallelPhysicsUpdate);
        } else if (threadingType == ThreadingType.SEQUENTIAL) {
            pSpace.update(tpf * speed);
        } else {
        }
    }

    public void cleanup() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        pSpace.removeTickListener(this);
        pSpace.destroy();
    }

    /**
     * @return the threadingType
     */
    public ThreadingType getThreadingType() {
        return threadingType;
    }

    /**
     * @param threadingType the threadingType to set
     */
    public void setThreadingType(ThreadingType threadingType) {
        this.threadingType = threadingType;
    }

    public void setBroadphaseType(BroadphaseType broadphaseType) {
        this.broadphaseType = broadphaseType;
    }

    public void setWorldMin(Vector3f worldMin) {
        this.worldMin = worldMin;
    }

    public void setWorldMax(Vector3f worldMax) {
        this.worldMax = worldMax;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void physicsTick(PhysicsSpace space, float f) {
    }

    public enum ThreadingType {
        /** Default mode; user update, physics update and rendering happen sequentially (single threaded) */

        SEQUENTIAL,
        /** Parallel threaded mode; only physics update and rendering are executed in parallel, update order is kept.*/
        PARALLEL,
        /** Detached threaded mode; physics executes independently on other thread, only location and rotation is transferred thread safe,
        <b>all</b> other physics operations including adding and removing of objects to the physics space
        have to be done from the physics thread. (Creation of objects is safe on any thread except for vehicle)*/
        DETACHED
    }
}
