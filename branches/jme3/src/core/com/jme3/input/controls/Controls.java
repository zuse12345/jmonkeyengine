package com.jme3.input.controls;

import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;
import java.util.ArrayList;
import java.util.HashMap;

public class Controls implements RawInputListener {

    private final KeyInput keys;
    private final MouseInput mouse;
    private final JoyInput joystick;

    private float frameTPF;
    private long lastUpdateTime = 0;
    private boolean eventsPermitted = false;

    private float axisDeadZone = 0.05f;

    private final IntMap<ArrayList<Mapping>> bindings = new IntMap<ArrayList<Mapping>>();
    private final HashMap<String, Mapping> mappings = new HashMap<String, Mapping>();
    private final IntMap<Long> pressedButtons = new IntMap<Long>();

//    private final ArrayList<Trigger> triggers = new ArrayList<Trigger>();

    private static class Mapping {
        private final String name;
        private final ArrayList<Integer> triggers = new ArrayList<Integer>();
        private final ArrayList<InputListener> listeners = new ArrayList<InputListener>();

        public Mapping(String name){
            this.name = name;
        }
    }

    /**
     * Create a Controls representation for the given inputs.
     * @param keys KeyInput implementation
     * @param mouse MouseInput implementation
     * @param joystick JoyInput implementation, may be null.
     * @throws NullPointerException If mouse or keys is null. 
     */
    public Controls(KeyInput keys, MouseInput mouse, JoyInput joystick) {
        if (keys == null || mouse == null)
            throw new NullPointerException("Mouse or keyboard cannot be null");

        this.keys = keys;
        this.mouse = mouse;
        this.joystick = joystick;

        keys.setInputListener(this);
        mouse.setInputListener(this);
        if (joystick != null) joystick.setInputListener(this);
    }

    static final int joyButtonHash(int joyButton){
        return 1536 | (joyButton & 0xff);
    }

    static final int joyAxisHash(int joyAxis, boolean negative){
        return (negative ? 1280 : 1024) | (joyAxis & 0xff);
    }

    static final int mouseAxisHash(int mouseAxis, boolean negative){
        return (negative ? 768 : 512) | (mouseAxis & 0xff);
    }

    static final int mouseButtonHash(int mouseButton){
        return 256 | (mouseButton & 0xff);
    }

    static final int keyHash(int keyCode){
        return keyCode & 0xff;
    }

    private void invokeActions(int hash, boolean pressed){
        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null)
            return;
        
        int size = maps.size();
        for (int i = size - 1; i >= 0; i--){
            Mapping mapping = maps.get(i);
            ArrayList<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; i >= 0; i--){
                InputListener listener = listeners.get(j);
                if (listener instanceof ActionListener){
                    ((ActionListener)listener).onAction(mapping.name, pressed, frameTPF);
                }
            }
        }
    }

    private void invokeTimedActions(int hash, long time, boolean pressed){
        if (!bindings.containsKey(hash))
            return;

        if (pressed)
            pressedButtons.put(hash, time);
        else{
            long pressTime   = pressedButtons.get(hash);
            long lastUpdate  = lastUpdateTime;
            long releaseTime = time;
            float timeDelta = releaseTime - Math.max(pressTime, lastUpdate);
            float frameDelta = frameTPF * 1000f;
            invokeAnalogs(hash, timeDelta / frameDelta);
        }
    }

    private void invokeUpdateActions(long currentTime){
        for (Entry<Long> pressedButton : pressedButtons){
            int hash = pressedButton.getKey();
            long pressTime   = pressedButton.getValue();
            float timeDelta = currentTime - pressTime;
            float frameDelta = frameTPF * 1000f;
            if (timeDelta > 0)
                invokeAnalogs(hash, timeDelta / frameDelta);
        }
    }

    private void invokeAnalogs(int hash, float value){
        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null)
            return;

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--){
            Mapping mapping = maps.get(i);
            ArrayList<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; i >= 0; i--){
                InputListener listener = listeners.get(j);
                if (listener instanceof AnalogListener){
                    ((AnalogListener)listener).onAnalog(mapping.name, value, frameTPF);
                }
            }
        }
    }

    private void invokeAnalogsAndActions(int hash, float value){
        if (value < axisDeadZone){
            invokeAnalogs(hash, value);
            return;
        }

        ArrayList<Mapping> maps = bindings.get(hash);
        if (maps == null)
            return;

        int size = maps.size();
        for (int i = size - 1; i >= 0; i--){
            Mapping mapping = maps.get(i);
            ArrayList<InputListener> listeners = mapping.listeners;
            int listenerSize = listeners.size();
            for (int j = listenerSize - 1; i >= 0; i--){
                InputListener listener = listeners.get(j);
                if (listener instanceof ActionListener){
                    ((ActionListener)listener).onAction(mapping.name, true, frameTPF);
                }else if (listener instanceof AnalogListener){
                    ((AnalogListener)listener).onAnalog(mapping.name, value, frameTPF);
                }
            }
        }
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("JoyInput has raised an event at an illegal time.");

        int axis    = evt.getAxisIndex();
        float value = evt.getValue();
        if (value < 0){
            invokeAnalogsAndActions(joyAxisHash(axis, true), -value);
        }else{
            invokeAnalogsAndActions(joyAxisHash(axis, false), value);
        }
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("JoyInput has raised an event at an illegal time.");

        int hash = joyButtonHash(evt.getButtonIndex());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("MouseInput has raised an event at an illegal time.");

        if (evt.getDX() != 0){
            float val = Math.abs(evt.getDX()) / 1024f;
            invokeAnalogsAndActions(mouseAxisHash(MouseInput.AXIS_X, evt.getDX() < 0), val);
        }
        if (evt.getDY() != 0){
            float val = Math.abs(evt.getDY()) / 1024f;
            invokeAnalogsAndActions(mouseAxisHash(MouseInput.AXIS_Y, evt.getDY() < 0), val);
        }
        if (evt.getDeltaWheel() != 0){
            float val = Math.abs(evt.getDeltaWheel()) / 100f;
            invokeAnalogsAndActions(mouseAxisHash(MouseInput.AXIS_WHEEL, evt.getDeltaWheel() < 0), val);
        }
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (!eventsPermitted)
            throw new UnsupportedOperationException("MouseInput has raised an event at an illegal time.");

        int hash = mouseButtonHash(evt.getButtonIndex());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    public void onKeyEvent(KeyInputEvent evt){
        if (!eventsPermitted)
            throw new UnsupportedOperationException("KeyInput has raised an event at an illegal time.");

        int hash = keyHash(evt.getKeyCode());
        invokeActions(hash, evt.isPressed());
        invokeTimedActions(hash, evt.getTime(), evt.isPressed());
    }

    public void setAxisDeadZone(float deadZone){
        this.axisDeadZone = deadZone;
    }

    public void addListener(InputListener listener, String ... mappingNames){
        for (String mappingName : mappingNames){
            Mapping mapping = mappings.get(mappingName);
            if (mapping == null){
                mapping = new Mapping(mappingName);
                mappings.put(mappingName, mapping);
            }
            mapping.listeners.add(listener);
        }
    }

    public void removeListener(InputListener listener){
        for (Mapping mapping : mappings.values()){
            mapping.listeners.remove(listener);
        }
    }

    public void addMapping(String mappingName, Trigger ... triggers){
        Mapping mapping = mappings.get(mappingName);
        if (mapping == null){
            mapping = new Mapping(mappingName);
            mappings.put(mappingName, mapping);
        }

        for (Trigger trigger : triggers){
            int hash = trigger.hashCode();
            ArrayList<Mapping> names = bindings.get(hash);
            if (names == null){
                names = new ArrayList<Mapping>();
                bindings.put(hash, names);
            }
            names.add(mapping);
            mapping.triggers.add(hash);
        }
    }

    public void deleteMapping(String mappingName){
        Mapping mapping = mappings.remove(mappingName);
        if (mapping == null)
            throw new IllegalArgumentException("Cannot find mapping: "+mappingName);

        ArrayList<Integer> triggers = mapping.triggers;
        for (int i = triggers.size() - 1; i >= 0; i--){
            int hash = triggers.get(i);
            ArrayList<Mapping> maps = bindings.get(hash);
            maps.remove(mapping);
        }
    }

    public void update(float tpf){
        frameTPF = tpf;

        eventsPermitted = true;

        keys.update();
        mouse.update();
        if (joystick != null) joystick.update();
        
        eventsPermitted = false;

        long currentTime = System.currentTimeMillis();
        invokeUpdateActions(currentTime);

        lastUpdateTime = currentTime;
    }

}
