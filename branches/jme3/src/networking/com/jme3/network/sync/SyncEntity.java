package com.jme3.network.sync;

public interface SyncEntity {
    public InitState generateInitState();
    public SyncState generateSyncState();
    public void      applySyncState(SyncState state);
}
