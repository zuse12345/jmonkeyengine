package com.g3d.input;

import com.g3d.input.binding.BindingListener;
import com.g3d.input.event.JoyAxisEvent;
import com.g3d.math.FastMath;
import com.g3d.math.Matrix3f;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.util.TempVars;
import org.lwjgl.input.Keyboard;

/**
 * A first person view camera controller.
 * After creation, you must register the camera controller with the
 * dispatcher using #registerWithDispatcher().
 *
 * Controls:
 *  - Move the mouse to rotate the camera
 *  - Mouse wheel for zooming in or out
 *  - WASD keys for moving forward/backward and strafing
 *  - QZ keys raise or lower the camera
 */
public class FlyByCamera implements BindingListener {

    private Camera cam;
    private Vector3f initialUpVec;
    private float rotationSpeed = 0.2f;
    private float moveSpeed = 1f;

    /**
     * Creates a new FlyByCamera to control the given Camera object.
     * @param cam
     */
    public FlyByCamera(Camera cam){
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
    }

    /**
     * Sets the move speed. The speed is given in world units per second.
     * @param moveSpeed
     */
    public void setMoveSpeed(float moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    /**
     * Registers the FlyByCamera to recieve input events from the provided
     * Dispatcher.
     * @param dispacher
     */
    public void registerWithDispatcher(Dispatcher dispacher){
        dispacher.setCursorVisible(false);

//        dispacher.registerJoystickAxisBinding("FLYCAM_Left",  2, JoyAxisEvent.AXIS_X, true);
//        dispacher.registerJoystickAxisBinding("FLYCAM_Right", 2, JoyAxisEvent.AXIS_X, false);
//        dispacher.registerJoystickAxisBinding("FLYCAM_Up",    2, JoyAxisEvent.AXIS_Y, true);
//        dispacher.registerJoystickAxisBinding("FLYCAM_Down",  2, JoyAxisEvent.AXIS_Y, false);
//
//        dispacher.registerJoystickAxisBinding("FLYCAM_StrafeLeft",  2, JoyAxisEvent.POV_X, true);
//        dispacher.registerJoystickAxisBinding("FLYCAM_StrafeRight", 2, JoyAxisEvent.POV_X, false);
//        dispacher.registerJoystickAxisBinding("FLYCAM_Forward",     2, JoyAxisEvent.POV_Y, true);
//        dispacher.registerJoystickAxisBinding("FLYCAM_Backward",    2, JoyAxisEvent.POV_Y, false);
        
        dispacher.registerMouseAxisBinding("FLYCAM_Left", 0, true);
        dispacher.registerMouseAxisBinding("FLYCAM_Right", 0, false);
        dispacher.registerMouseAxisBinding("FLYCAM_Up", 1, false);
        dispacher.registerMouseAxisBinding("FLYCAM_Down", 1, true);

        dispacher.registerMouseAxisBinding("FLYCAM_ZoomIn", 2, false);
        dispacher.registerMouseAxisBinding("FLYCAM_ZoomOut", 2, true);

        dispacher.registerKeyBinding("FLYCAM_Left", KeyInput.KEY_LEFT);
        dispacher.registerKeyBinding("FLYCAM_Right", KeyInput.KEY_RIGHT);
        dispacher.registerKeyBinding("FLYCAM_Up", KeyInput.KEY_UP);
        dispacher.registerKeyBinding("FLYCAM_Down", KeyInput.KEY_DOWN);

        dispacher.registerKeyBinding("FLYCAM_StrafeLeft", KeyInput.KEY_A);
        dispacher.registerKeyBinding("FLYCAM_StrafeRight", KeyInput.KEY_D);
        dispacher.registerKeyBinding("FLYCAM_Forward", KeyInput.KEY_W);
        dispacher.registerKeyBinding("FLYCAM_Backward", KeyInput.KEY_S);

        dispacher.registerKeyBinding("FLYCAM_Rise", KeyInput.KEY_Q);
        dispacher.registerKeyBinding("FLYCAM_Lower", KeyInput.KEY_Z);

//        dispacher.registerKeyBinding("FLYCAM_Left", Keyboard.KEY_LEFT);
//        dispacher.registerKeyBinding("FLYCAM_Right", Keyboard.KEY_RIGHT);
//        dispacher.registerKeyBinding("FLYCAM_Up", Keyboard.KEY_UP);
//        dispacher.registerKeyBinding("FLYCAM_Down", Keyboard.KEY_DOWN);
//
//        dispacher.registerKeyBinding("FLYCAM_StrafeLeft", Keyboard.KEY_A);
//        dispacher.registerKeyBinding("FLYCAM_StrafeRight", Keyboard.KEY_D);
//        dispacher.registerKeyBinding("FLYCAM_Forward", Keyboard.KEY_W);
//        dispacher.registerKeyBinding("FLYCAM_Backward", Keyboard.KEY_S);
//
//        dispacher.registerKeyBinding("FLYCAM_Rise", Keyboard.KEY_Q);
//        dispacher.registerKeyBinding("FLYCAM_Lower", Keyboard.KEY_Z);

        dispacher.addTriggerListener(this);
    }

    public void rotateCamera(float value, Vector3f axis){
        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(rotationSpeed * value, axis);

        Vector3f up = cam.getUp();
        Vector3f left = cam.getLeft();
        Vector3f dir = cam.getDirection();

        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);

        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalize();

        cam.setAxes(q);
    }

    public void onBinding(String binding, float value) {
        if (binding.equals("FLYCAM_Left")){
            rotateCamera(value, initialUpVec);
        }else if (binding.equals("FLYCAM_Right")){
            rotateCamera(-value, initialUpVec);
        }else if (binding.equals("FLYCAM_Up")){
            rotateCamera(-value, cam.getLeft());
        }else if (binding.equals("FLYCAM_Down")){
            rotateCamera(value, cam.getLeft());
        }else if (binding.equals("FLYCAM_Forward")){
            moveCamera(value, false);
        }else if (binding.equals("FLYCAM_Backward")){
            moveCamera(-value, false);
        }else if (binding.equals("FLYCAM_StrafeLeft")){
            moveCamera(value, true);
        }else if (binding.equals("FLYCAM_StrafeRight")){
            moveCamera(-value, true);
        }else if (binding.equals("FLYCAM_Rise")){
            riseCamera(value);
        }else if (binding.equals("FLYCAM_Lower")){
            riseCamera(-value);
        }else if (binding.equals("FLYCAM_ZoomIn")){
            zoomCamera(value);
        }else if (binding.equals("FLYCAM_ZoomOut")){
            zoomCamera(-value);
        }
    }

    public void zoomCamera(float value){
        // derive fovY value
        float h = cam.getFrustumTop();
        float w = cam.getFrustumRight();
        float aspect = w / h;

        float near = cam.getFrustumNear();

        float fovY = FastMath.atan(h / near)
                  / (FastMath.DEG_TO_RAD * .5f);
        fovY += value * 0.1f;

        h = FastMath.tan( fovY * FastMath.DEG_TO_RAD * .5f) * near;
        w = h * aspect;

        cam.setFrustumTop(h);
        cam.setFrustumBottom(-h);
        cam.setFrustumLeft(-w);
        cam.setFrustumRight(w);
    }

    public void riseCamera(float value){
        Vector3f pos = TempVars.get().vect1.set(cam.getLocation());
        pos.addLocal(0, value * moveSpeed, 0);
        cam.setLocation(pos);
    }

    public void moveCamera(float value, boolean sideways){
        TempVars vars = TempVars.get();

        Vector3f vel = vars.vect1;
        if (sideways){
            cam.getLeft(vel);
        }else{
            cam.getDirection(vel);
        }
        vel.multLocal(value * moveSpeed);
        vel.addLocal(cam.getLocation());
        cam.setLocation(vel);
    }
}
