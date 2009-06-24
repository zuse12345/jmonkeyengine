package com.g3d.input;

import com.g3d.input.event.JoyAxisEvent;
import com.g3d.input.event.JoyButtonEvent;
import com.g3d.input.event.KeyInputEvent;
import com.g3d.input.event.MouseButtonEvent;
import com.g3d.input.event.MouseMotionEvent;
import com.g3d.input.binding.BindingListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The <code>Dispatcher</code> is responsible for converting input events
 * recieved from the Key, Mouse and Joy Input implementations into an
 * abstract, input device independent representation that user code can use.
 *
 * By default a dispatcher is included with every Application instance for use
 * in user code to query input, unless the Application is created as headless
 * or with input explicitly disabled.
 */
public class Dispatcher implements RawInputListener {

    private MouseInput mouseInput;
    private KeyInput keyInput;
    private JoyInput joyInput;

    private Map<Integer, String> keyBindings = new HashMap<Integer, String>();
    private Map<Integer, String> mouseBtnBindings = new HashMap<Integer, String>();
    private Map<Integer, String> mouseAxisBindings = new HashMap<Integer, String>();
    private Map<Integer, String> joyAxisBindings = new HashMap<Integer, String>();
    private Map<Integer, String> joyButtonBindings = new HashMap<Integer, String>();

    private List<BindingListener> listeners = new ArrayList<BindingListener>();

    private MouseButtonEvent lastButtonEvent = null;
    private KeyInputEvent lastKeyEvent = null;
    private float frameTPF = -1f;

    private boolean[] keyboard;
    private boolean[] mouse;
    private float[][] joyAxes;
    private boolean[][] joyButtons;

    /**
     * Initializes the Dispatcher.
     *
     * @param mouseInput
     * @param keyInput
     * @param joyInput
     * @throws IllegalArgumentException If either mouseInput or keyInput are null.
     */
    public Dispatcher(MouseInput mouseInput, KeyInput keyInput, JoyInput joyInput){
        if (mouseInput == null || keyInput == null)
            throw new IllegalArgumentException("Mouse and key input cannot be null");

        this.keyInput = keyInput;
        keyboard = new boolean[255];
        keyInput.setInputListener(this);

        this.mouseInput = mouseInput;
        mouse = new boolean[mouseInput.getButtonCount()];
        mouseInput.setInputListener(this);
        
        if (joyInput != null){
            this.joyInput = joyInput;
            joyInput.setInputListener(this);
            joyAxes = new float[joyInput.getJoyCount()][];
            joyButtons = new boolean[joyInput.getJoyCount()][];
            for (int i = 0; i < joyAxes.length; i++){
                joyAxes[i] = new float[7]; // because using virtual axes
                joyButtons[i] = new boolean[joyInput.getButtonCount(i)];
            }
        }
    }

    private void notifyListeners(String name, float value){
        if (name == null)
            return;

        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onBinding(name, value);
        }
    }

    /**
     * @param visible whether the mouse cursor should be visible or not.
     */
    public void setCursorVisible(boolean visible){
        mouseInput.setCursorVisible(visible);
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
        if (evt.getDX() > 0){
            // positive X axis
            String name = mouseAxisBindings.get(1);
            float value = evt.getDX() / 1000f;
            notifyListeners(name, value);
        }else if (evt.getDX() < 0){
            String name = mouseAxisBindings.get(-1);
            float value = evt.getDX() / -1000f;
            notifyListeners(name, value);
        }
        if (evt.getDY() > 0){
            String name = mouseAxisBindings.get(2);
            float value = evt.getDY() / 1000f;
            notifyListeners(name, value);
        }else if (evt.getDY() < 0){
            String name = mouseAxisBindings.get(-2);
            float value = evt.getDY() / -1000f;
            notifyListeners(name, value);
        }
        if (evt.getDeltaWheel() > 0){
            String name = mouseAxisBindings.get(3);
            float value = evt.getDeltaWheel() / 100f;
            notifyListeners(name, value);
        }else if (evt.getDeltaWheel() < 0){
            String name = mouseAxisBindings.get(-3);
            float value = evt.getDeltaWheel() / -100f;
            notifyListeners(name, value);
        }
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        mouse[evt.getButtonIndex()] = evt.isPressed();
        
        String name = mouseBtnBindings.get(evt.getButtonIndex());
        if (name == null)
            return;

        if (lastButtonEvent != null
         && lastButtonEvent.getButtonIndex() == evt.getButtonIndex()
         && lastButtonEvent.isPressed() && evt.isReleased()){
            long delta = evt.getTime() - lastButtonEvent.getTime();
            float seconds = delta / (1000000000f); // convert nanoseconds to seconds

            // for how long was the key pressed relative to the current
            // frame time? deltaSeconds / frameSeconds
            notifyListeners(name, seconds);
        }
        lastButtonEvent = evt;
    }

    public void onKeyEvent(KeyInputEvent evt) {
        if (evt.isDown())
            return; // repeat events not used for bindings

        // update keyboard state
        keyboard[evt.getKeyCode()] = evt.isPressed();
        
        // check if binding set for key
        String name = keyBindings.get(evt.getKeyCode());
        if (name == null)
            return;

        // this part is used for really short key presses (less than
        // one frame long).
        if (lastKeyEvent != null
         && lastKeyEvent.getKeyCode() == evt.getKeyCode()
         && lastKeyEvent.isPressed() && evt.isReleased()){
            long delta = evt.getTime() - lastKeyEvent.getTime();
            float seconds = delta / (1000000000f); // convert nanoseconds to seconds

            // for how long was the key pressed relative to the current
            // frame time? deltaSeconds / frameSeconds
            notifyListeners(name, seconds);
        }
        lastKeyEvent = evt;
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
        joyAxes[evt.getJoyIndex()][evt.getAxisIndex()] = evt.getValue();
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }

    /**
     * Updates the Dispatcher. This will query current input devices and send
     * appropriate events to registered listeners.
     *
     * @param tpf Time per frame value.
     */
    public void update(float tpf){
        lastButtonEvent = null;
        lastKeyEvent = null;
        frameTPF = tpf;
        
        // query current keyboard state for all bindings
        for (Map.Entry<Integer, String> entry : keyBindings.entrySet()){
            if (entry.getKey() >= keyboard.length)
                continue;

            if (keyboard[entry.getKey()]){
                // key is currently down
                notifyListeners(entry.getValue(), tpf);
            }
        }

        if (joyInput != null){
            for (Map.Entry<Integer, String> entry : joyAxisBindings.entrySet()){
                int axisId = entry.getKey();
                boolean negative = false;
                if (axisId < 0){
                    axisId = -axisId;
                    negative = true;
                }
                int joyIndex = (axisId & 0xFFFF) - 1;
                int axisIndex = ((axisId & 0xFFFF0000) >> 16) - 1;
                if (joyIndex >= joyAxes.length)
                    continue;

                float value = joyAxes[joyIndex][axisIndex];
                if (value > 0 && !negative){
                    // key is currently down
                    notifyListeners(entry.getValue(), tpf * value);
                }else if (value < 0 && negative){
                    notifyListeners(entry.getValue(), -tpf * value);
                }
            }
        }
    }

    /**
     * Registers a keyboard key binding.
     * @param name
     * @param keyCode
     */
    public void registerKeyBinding(String name, int keyCode){
        keyBindings.put(keyCode, name);
    }

    /**
     * Registers a mouse button binding.
     * @param name
     * @param btnIndex
     */
    public void registerMouseButtonBinding(String name, int btnIndex){
        mouseBtnBindings.put(btnIndex, name);
    }

    /**
     * Registers a mouse axis binding
     * @param name
     * @param mouseAxis 0 is X axis, 1 is Y axis, 2 is wheel
     * @param negative If true, the event will be invoked when the axis is in
     * the negative direction, otherwise it will be invoked on positive direction.
     */
    public void registerMouseAxisBinding(String name, int mouseAxis, boolean negative){
        mouseAxis ++; // makes axis 0 (X) become 1 & axis 1 (Y) become 2
        if (negative)
            mouseAxis = -mouseAxis;

        mouseAxisBindings.put(mouseAxis, name);
    }

    public void registerJoystickAxisBinding(String name, int joyIndex, int axisIndex, boolean negative){
        if (joyInput == null)
            return;

        axisIndex ++;
        joyIndex ++;
        int axisId = joyIndex | (axisIndex << 16);
        if (negative)
            axisId = -axisId;
        
        joyAxisBindings.put(axisId, name);
    }

    /**
     * Adds a trigger listener. Listeners will be invoked when the binding
     * value is greater than zero.
     *
     * @param listener
     */
    public void addTriggerListener(BindingListener listener){
        listeners.add(listener);
    }

    /**
     * Remove a previously added trigger listener.
     * @param listener
     */
    public void removeTriggerListener(BindingListener listener){
        listeners.remove(listener);
    }

    /**
     * Removes all trigger listeners from the list of registered listnerers.
     */
    public void clearTriggerListeners(){
        listeners.clear();
    }

}
