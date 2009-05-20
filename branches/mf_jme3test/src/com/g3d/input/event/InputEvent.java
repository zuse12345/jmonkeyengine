package com.g3d.input.event;

public class InputEvent {

    protected long time;
    protected long delta;

    public long getTime(){
        return time;
    }

    public void setTime(long time){
        this.time = time;
    }

    public long getTimeDelta(){
        return delta;
    }

    public void setTimeDelta(long delta){
        this.delta = delta;
    }
    
}
