package com.g3d.app.state;

public interface AppState {
    public String getName();
    public void notifyAttach(AppStateManager manager);
    public void notifyDetach(AppStateManager manager);
    public void update(AppStateManager manager, float tpf);
}
