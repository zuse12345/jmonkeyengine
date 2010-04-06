package com.jme3.niftygui;

import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import de.lessvoid.nifty.input.mouse.MouseInputEvent;
import de.lessvoid.nifty.spi.input.InputSystem;
import java.util.ArrayList;
import java.util.List;

public class InputSystemJme implements InputSystem, RawInputListener {

    private ArrayList<MouseInputEvent> events = new ArrayList<MouseInputEvent>();
    private boolean pressed = false;
    private int x, y;

    public List<MouseInputEvent> getMouseEvents() {
        events.clear();
        events.add(new MouseInputEvent(x, y, pressed));
        return events;
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
        
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {

    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        x = evt.getX();
        y = evt.getY();
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (evt.getButtonIndex() == 0){
            pressed = evt.isDown();
        }
    }

    public void onKeyEvent(KeyInputEvent evt) {

    }
}
