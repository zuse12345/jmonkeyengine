package com.jme3.network.sync;

import com.jme3.network.serializing.Serializable;

@Serializable
public interface InitState {
    public SyncEntity createEntity();
}
