package com.jme3.niftygui;

import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.input.mouse.MouseInputEvent;
import de.lessvoid.nifty.spi.input.InputSystem;
import java.util.ArrayList;
import java.util.List;

public class InputSystemJme implements InputSystem, RawInputListener {

    private Nifty nifty;
    private ArrayList<MouseInputEvent> events = new ArrayList<MouseInputEvent>();
    private ArrayList<MouseInputEvent> eventsCopy = new ArrayList<MouseInputEvent>();
    private boolean pressed = false;
    private int x, y;

    public List<MouseInputEvent> getMouseEvents() {
        eventsCopy.clear();
        eventsCopy.addAll(events);
        events.clear();
        return eventsCopy;
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        x = evt.getX();
        y = evt.getY();
        events.add(new MouseInputEvent(x, y, pressed));
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (evt.getButtonIndex() == 0){
            pressed = evt.isDown();
            events.add(new MouseInputEvent(x, y, pressed));
        }
    }

    public void onKeyEvent(KeyInputEvent evt) {
//        nifty.keyEvent(y, eventCharacter, pressed);
    }
}
