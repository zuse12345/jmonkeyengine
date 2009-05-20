package com.g3d.input;

import com.g3d.input.event.JoyAxisEvent;
import com.g3d.input.event.JoyButtonEvent;
import com.g3d.input.event.KeyInputEvent;
import com.g3d.input.event.MouseButtonEvent;
import com.g3d.input.event.MouseMotionEvent;

public interface RawInputListener {

    public void onJoyAxisEvent(JoyAxisEvent evt);
    public void onJoyButtonEvent(JoyButtonEvent evt);
    public void onMouseMotionEvent(MouseMotionEvent evt);
    public void onMouseButtonEvent(MouseButtonEvent evt);
    public void onKeyEvent(KeyInputEvent evt);
}
