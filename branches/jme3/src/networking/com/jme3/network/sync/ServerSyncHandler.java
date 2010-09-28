package com.jme3.network.sync;

import com.jme3.network.connection.Client;

public interface ServerSyncHandler {
    public boolean validateInitState(Client sender, InitState state);
    public boolean validateSyncState(Client sender, SyncState state);
}
