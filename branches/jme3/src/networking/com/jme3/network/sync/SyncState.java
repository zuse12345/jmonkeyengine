package com.jme3.network.sync;

import com.jme3.network.serializing.Serializable;

/**
 * An interface for synchronization state.
 * Sent across network to update clients.
 *
 * @author Kirusha
 * @param <T>
 */
@Serializable
public interface SyncState <T extends SyncState> {
    
    /**
     * Interpolate from this state toward the <code>end</code> state by
     * <code>amount</code> scale (from 0.0 to 1.0), and store the result
     * in store.
     *
     * @param end
     * @param amount
     * @param store
     */
    public void interpolate(T end, float amount, T store);

    /**
     * Update the state based on the currently known information.
     * @param tpf
     */
    public void update(float tpf);

    /**
     * @return Determines the rate in seconds at which
     * sync update packets are sent.
     */
    public float getSyncRate();
}
