package com.g3d.input.event;

/**
 * An abstract input event.
 */
public abstract class InputEvent {

    /**
     * Time in ticks when the event occured.
     */
    protected long time;

    /**
     * Delta value since this event last happened in ticks.
     */
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
