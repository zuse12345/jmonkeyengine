package jme3test.input.combomoves;

import java.util.ArrayList;
import java.util.List;

public class ComboMove {

    public static class ComboMoveState {
        
        private String[] pressedMappings;
        private String[] unpressedMappings;
        private float timeElapsed;

        public ComboMoveState(String[] pressedMappings, String[] unpressedMappings, float timeElapsed) {
            this.pressedMappings = pressedMappings;
            this.unpressedMappings = unpressedMappings;
            this.timeElapsed = timeElapsed;
        }

        public String[] getUnpressedMappings() {
            return unpressedMappings;
        }

        public String[] getPressedMappings() {
            return pressedMappings;
        }

        public float getTimeElapsed() {
            return timeElapsed;
        }
        
    }

    private String moveName;
    private List<ComboMoveState> states = new ArrayList<ComboMoveState>();
    private boolean useFinalState = true;

    private transient String[] pressed, unpressed;
    private transient float timeElapsed;

    public ComboMove(String moveName){
        this.moveName = moveName;
    }

    public boolean useFinalState() {
        return useFinalState;
    }

    public void setUseFinalState(boolean useFinalState) {
        this.useFinalState = useFinalState;
    }
    
    public ComboMove press(String ... pressedMappings){
        this.pressed = pressedMappings;
        return this;
    }

    public ComboMove notPress(String ... unpressedMappings){
        this.unpressed = unpressedMappings;
        return this;
    }

    public ComboMove timeElapsed(float time){
        this.timeElapsed = time;
        return this;
    }

    public void done(){
        if (pressed == null)
            pressed = new String[0];
        
        if (unpressed == null)
            unpressed = new String[0];

        states.add(new ComboMoveState(pressed, unpressed, timeElapsed));
        pressed = null;
        unpressed = null;
        timeElapsed = -1;
    }

    public ComboMoveState getState(int num){
        return states.get(num);
    }

    public int getNumStates(){
        return states.size();
    }

    public String getMoveName() {
        return moveName;
    }
    
}
