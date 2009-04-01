package com.g3d.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Input {

//    private static final Input input = new Input();
//
//    protected Map<Trigger, String> bindingMap = new HashMap<Trigger, String>();
//    protected List<InputListener> listeners = new ArrayList<InputListener>();
//
//    public static Input getInput(){
//        return input;
//    }
//
//    public void addListener(InputListener listener) {
//        listeners.add(listener);
//    }
//
//    public void registerBinding(Trigger trigger, String bindingName){
//        bindingMap.put(trigger, bindingName);
//    }
//
//    public void registerKeyboardBinding(int keyCode, String bindingName){
//        registerBinding(new KeyTrigger(keyCode), bindingName);
//    }
//
//    public void registerMouseButtonBinding(int buttonIndex, String bindingName){
//        registerBinding(new MouseButtonTrigger(buttonIndex), bindingName);
//    }
//
//    public void registerMouseAxisBinding(int axis, boolean negative, String bindingName){
//        registerBinding(new MouseAxisTrigger(axis, negative), bindingName);
//    }
//
//    public void clearBindings(){
//        bindingMap.clear();
//    }
//
//    public boolean removeBinding(String bindingName){
//        Trigger key = null;
//        for (Map.Entry<Trigger, String> entry : bindingMap.entrySet()){
//            if (entry.getValue().equals(bindingName)){
//                key = entry.getKey();
//            }
//        }
//        if (key != null){
//            bindingMap.remove(key);
//            return true;
//        }
//        return false;
//    }
//
//    public void enableAllInput(boolean enable){
//        if (!enable){
//            KeyInput.get().removeListeners();
//            MouseInput.get().removeListeners();
//            JoystickInput.get().removeListeners();
//            return;
//        }
//
//        KeyInput.get().addListener(new KeyInputListener() {
//            @Override
//            public void onKey(char character, int keyCode, boolean pressed) {
//                if (!pressed)
//                    return;
//
//                Trigger trigger = new KeyTrigger(keyCode);
//                for (InputListener listener : listeners)
//                    listener.recieveInput(trigger, null, 1f);
//            }
//        });
//        MouseInput.get().addListener(new MouseInputListener() {
//            @Override
//            public void onButton(int button, boolean pressed, int x, int y) {
//                if (!pressed)
//                    return;
//
//                Trigger trigger = new MouseButtonTrigger(button);
//                for (InputListener listener : listeners)
//                    listener.recieveInput(trigger, null, 1f);
//            }
//            @Override
//            public void onWheel(int wheelDelta, int x, int y) {
//                Trigger trigger = new MouseAxisTrigger(2, wheelDelta >= 0f);
//                for (InputListener listener : listeners)
//                    listener.recieveInput(trigger, null, FastMath.abs((float)wheelDelta) / 100f);
//            }
//            @Override
//            public void onMove(int xDelta, int yDelta, int newX, int newY) {
//                if (xDelta != 0){
//                    Trigger trigger = new MouseAxisTrigger(0, xDelta >= 0f);
//                    for (InputListener listener : listeners)
//                        listener.recieveInput(trigger, null, FastMath.abs((float)xDelta) / 100f);
//                }
//                if (yDelta != 0){
//                    Trigger trigger = new MouseAxisTrigger(1, yDelta >= 0f);
//                    for (InputListener listener : listeners)
//                        listener.recieveInput(trigger, null, FastMath.abs((float)yDelta) / 100f);
//                }
//            }
//        });
//        JoystickInput.get().addListener(new JoystickInputListener() {
//            @Override
//            public void onButton(Joystick controller, int button, boolean pressed) {
//                if (!pressed)
//                    return;
//
//                Trigger trigger = new JoystickButtonTrigger(button);
//                for (InputListener listener : listeners)
//                    listener.recieveInput(trigger, null, 1f);
//            }
//            @Override
//            public void onAxis(Joystick controller, int axis, float axisValue) {
//            }
//        });
//    }
//
//    public Vector2f getMouseXY(){
//        return new Vector2f(MouseInput.get().getXAbsolute(),
//                            MouseInput.get().getYAbsolute());
//    }
//
//    public void update(){
//        if (listeners.size() == 0)
//            return;
//
//        for (Map.Entry<Trigger, String> binding : bindingMap.entrySet()){
//            Trigger trigger = binding.getKey();
//            if (trigger.getValue() > 0f){
//                for (InputListener listener : listeners)
//                    listener.recieveInput(trigger, binding.getValue(), trigger.getValue());
//            }
//        }
//    }

}
