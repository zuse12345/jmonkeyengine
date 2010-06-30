package com.jme3.input;

import com.jme3.input.binding.BindingListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.Controls;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import java.util.ArrayList;

/**
 * The <code>InputManager</code> is responsible for converting input events
 * recieved from the Key, Mouse and Joy Input implementations into an
 * abstract, input device independent representation that user code can use.
 *
 * By default a dispatcher is included with every Application instance for use
 * in user code to query input, unless the Application is created as headless
 * or with input explicitly disabled.
 */
public class InputManager extends Controls {

    private ArrayList<BindingListener> listeners = new ArrayList<BindingListener>();

    private final BindingTranslator translator = new BindingTranslator();

    /**
     * Listener responsible for converting axis analog events
     * to bindings. Will not multiply by TPF for absolute delta.
     */
    private class BindingTranslator implements AnalogListener {
        public void onAnalog(String name, float value, float tpf) {
            if (name == null)
                return;

            for (int i = 0; i < listeners.size(); i++){
                listeners.get(i).onBinding(name, value);
            }
        }
    }

    /**
     * Initializes the InputManager.
     *
     * @param mouseInput
     * @param keyInput
     * @param joyInput
     * @throws IllegalArgumentException If either mouseInput or keyInput are null.
     */
    public InputManager(MouseInput mouseInput, KeyInput keyInput, JoyInput joyInput){
        super(keyInput, mouseInput, joyInput);
    }

    /**
     * Updates the Dispatcher. This will query current input devices and send
     * appropriate events to registered listeners.
     *
     * @param tpf Time per frame value.
     */
    @Override
    public void update(float tpf){
        for (int i = 0; i < listeners.size(); i++)
            listeners.get(i).onPreUpdate(tpf);

        super.update(tpf);

        for (int i = 0; i < listeners.size(); i++)
            listeners.get(i).onPostUpdate(tpf);
    }

    /**
     * Registers a keyboard key binding.
     * @param name
     * @param keyCode
     *
     * @deprecated Use the new addMapping method to register key bindings
     */
    @Deprecated
    public void registerKeyBinding(String name, int keyCode){
        addMapping(name, new KeyTrigger(keyCode));
        addListener(translator, name);
    }

    /**
     * Registers a mouse button binding.
     * @param name
     * @param btnIndex
     *
     * @deprecated Use the new addMapping method to register mouse bindings
     */
    @Deprecated
    public void registerMouseButtonBinding(String name, int btnIndex){
        addMapping(name, new MouseButtonTrigger(btnIndex));
        addListener(translator, name);
    }

    /**
     * Registers a mouse axis binding
     * @param name
     * @param mouseAxis 0 is X axis, 1 is Y axis, 2 is wheel
     * @param negative If true, the event will be invoked when the axis is in
     * the negative direction, otherwise it will be invoked on positive direction.
     *
     * @deprecated Use the new addMapping method to register mouse bindings
     */
    @Deprecated
    public void registerMouseAxisBinding(String name, int mouseAxis, boolean negative){
        addMapping(name, new MouseAxisTrigger(mouseAxis, negative));
        addListener(translator, name);
    }

    @Deprecated
    public void registerJoystickAxisBinding(String name, int joyIndex, int axisIndex, boolean negative){
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a trigger listener. Listeners will be invoked when the binding
     * value is greater than zero.
     *
     * @param listener
     *
     * @deprecated Use the new addListener method to register listeners for bindings
     */
    @Deprecated
    public void addBindingListener(BindingListener listener){
        listeners.add(listener);
    }

    /**
     * Remove a previously added trigger listener.
     * @param listener
     *
     * @deprecated Use the new addListener method to register listeners for bindings
     */
    @Deprecated
    public void removeBindingListener(BindingListener listener){
        listeners.remove(listener);
    }

    /**
     * Removes all trigger listeners from the list of registered listnerers.
     *
     * @deprecated Use the new addListener method to register listeners for bindings
     */
    @Deprecated
    public void clearBindingListeners(){
        listeners.clear();
    }

}
