package com.jme3.input;

import com.jme3.collision.MotionAllowedListener;
import com.jme3.input.binding.BindingListener;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

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
    private float rotationSpeed = 1f;
    private float moveSpeed = 3f;
    private MotionAllowedListener motionAllowed = null;
    private boolean enabled = true;

    /**
     * Creates a new FlyByCamera to control the given Camera object.
     * @param cam
     */
    public FlyByCamera(Camera cam){
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
    }

    public void setMotionAllowedListener(MotionAllowedListener listener){
        this.motionAllowed = listener;
    }

    /**
     * Sets the move speed. The speed is given in world units per second.
     * @param moveSpeed
     */
    public void setMoveSpeed(float moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    /**
     * Sets the rotation speed.
     * @param rotationSpeed
     */
    public void setRotationSpeed(float rotationSpeed){
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * @param enable If false, the camera will ignore input.
     */
    public void setEnabled(boolean enable){
        enabled = enable;
    }

    /**
     * @return If enabled
     * @see FlyByCamera#setEnabled(boolean)
     */
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * Registers the FlyByCamera to recieve input events from the provided
     * Dispatcher.
     * @param dispacher
     */
    public void registerWithDispatcher(InputManager dispacher){
        dispacher.setCursorVisible(false);

        dispacher.registerJoystickAxisBinding("FLYCAM_Left",  2, JoyInput.AXIS_X, true);
        dispacher.registerJoystickAxisBinding("FLYCAM_Right", 2, JoyInput.AXIS_X, false);
        dispacher.registerJoystickAxisBinding("FLYCAM_Up",    2, JoyInput.AXIS_Y, true);
        dispacher.registerJoystickAxisBinding("FLYCAM_Down",  2, JoyInput.AXIS_Y, false);

        dispacher.registerJoystickAxisBinding("FLYCAM_StrafeLeft",  2, JoyInput.POV_X, true);
        dispacher.registerJoystickAxisBinding("FLYCAM_StrafeRight", 2, JoyInput.POV_X, false);
        dispacher.registerJoystickAxisBinding("FLYCAM_Forward",     2, JoyInput.POV_Y, true);
        dispacher.registerJoystickAxisBinding("FLYCAM_Backward",    2, JoyInput.POV_Y, false);
        
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

        dispacher.addTriggerListener(this);
    }

    private void rotateCamera(float value, Vector3f axis){
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

    private void zoomCamera(float value){
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

    private void riseCamera(float value){
        Vector3f vel = new Vector3f(0, value * moveSpeed, 0);
        Vector3f pos = cam.getLocation().clone();

        if (motionAllowed != null)
            motionAllowed.checkMotionAllowed(pos, vel);
        else
            pos.addLocal(vel);

        cam.setLocation(pos);
    }

    private void moveCamera(float value, boolean sideways){
        Vector3f vel = new Vector3f();
        Vector3f pos = cam.getLocation().clone();

        if (sideways){
            cam.getLeft(vel);
        }else{
            cam.getDirection(vel);
        }
        vel.multLocal(value * moveSpeed);

        if (motionAllowed != null)
            motionAllowed.checkMotionAllowed(pos, vel);
        else
            pos.addLocal(vel);

        cam.setLocation(pos);
    }

    public void onBinding(String binding, float value) {
        if (!enabled)
            return;

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

    
}
