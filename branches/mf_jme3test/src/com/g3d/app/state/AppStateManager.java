package com.g3d.app.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppStateManager {

    private Map<String, AppState> stateMap;
    private Map<Class<? extends AppService>, AppService> serviceMap;
    private ArrayList<AppState> stateList;

    public AppStateManager(){
        stateList  = new ArrayList<AppState>();
        serviceMap = new HashMap<Class<? extends AppService>, AppService>();
        stateMap   = new HashMap<String, AppState>();
    }

    public void attachState(AppState state){
        if (state instanceof AppService)
            serviceMap.put( (Class<? extends AppService>) state.getClass(), (AppService) state);

        stateMap.put(state.getName(), state);
        stateList.add(state);
        state.notifyAttach(this);
    }

    public AppState detachState(AppState state){
        if (state instanceof AppService)
            serviceMap.remove( (Class<? extends AppService>) state.getClass());

        stateMap.remove(state.getName());
        stateList.remove(state);
        state.notifyDetach(this);
        return state;
    }
    
    public AppState getState(String name){
        return stateMap.get(name);
    }

    public <T extends AppService> T getService(Class<T> serviceClz){
        return (T) serviceMap.get(serviceClz);
    }

    public AppState detachState(String name){
        AppState state = getState(name);
        if (state == null)
            return null;

        return detachState(state);
    }

    public void update(float tpf){
        for (AppState state : stateList){
            state.update(this, tpf);
        }
    }

}
