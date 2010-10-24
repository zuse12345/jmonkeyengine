package jme3test.input.combomoves;

import java.util.Arrays;
import java.util.HashSet;
import jme3test.input.combomoves.ComboMove.ComboMoveState;

public class ComboMoveExecution {

    private static final float TIME_LIMIT = 0.3f;

    private ComboMove moveDef;
    private int state;
    private float moveTime;
    private boolean finalState = false;

    private String debugString = ""; // for debug only

    public ComboMoveExecution(ComboMove move){
        moveDef = move;
    }

    private boolean isStateSatisfied(HashSet<String> pressedMappings, float time,
                                     ComboMoveState state){

        if (state.getTimeElapsed() != -1f){
            // check if an appropriate amount of time has passed
            // if the state requires it
            if (moveTime + state.getTimeElapsed() >= time){
                return false;
            }
        }
        for (String mapping : state.getPressedMappings()){
            if (!pressedMappings.contains(mapping))
                return false;
        }
        for (String mapping : state.getUnpressedMappings()){
            if (pressedMappings.contains(mapping))
                return false;
        }
        return true;
    }

    public String getDebugString(){
        return debugString;
    }

    public void updateExpiration(float time){
        if (!finalState && moveTime > 0 && moveTime + TIME_LIMIT < time){
            state    = 0;
            moveTime = 0;
            finalState = false;

            // reset debug string.
            debugString = "";
        }
    }

    /**
     * Check if move needs to be executed. 
     * @param pressedMappings Which mappings are currently pressed
     * @param time Current time since start of app
     * @return True if move needs to be executed.
     */
    public boolean updateState(HashSet<String> pressedMappings, float time){
        ComboMoveState currentState = moveDef.getState(state);
        if (isStateSatisfied(pressedMappings, time, currentState)){
            state ++;
            moveTime = time;

            if (state >= moveDef.getNumStates()){
                finalState = false;
                state = 0;
                
                moveTime = time+0.5f; // this is for the reset of the debug string only.
                debugString += ", -CASTING " + moveDef.getMoveName().toUpperCase() + "-";
                return true;
            } 
            
            // the following for debug only.
            if (currentState.getPressedMappings().length > 0){
                if (!debugString.equals(""))
                    debugString += ", ";

                debugString += Arrays.toString(currentState.getPressedMappings()).replace(", ", "+");
            }

            if (state == moveDef.getNumStates() - 1){
                finalState = true;
            }
        }
        return false;
    }

}
