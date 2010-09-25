package com.jme3.scene.control;

import java.io.IOException;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;

public class BillboardControl extends AbstractControl {

    private Matrix3f orient;
    private Vector3f look;
    private Vector3f left;
    private Alignment alignment;

    /**
     * Determines how the billboard is aligned to the screen/camera.
     */
    public enum Alignment {
        /**
         * Aligns this Billboard to the screen.
         */
        Screen,

        /**
         * Aligns this Billboard to the camera position.
         */
        Camera,

        /**
          * Aligns this Billboard to the screen, but keeps the Y axis fixed.
          */
        AxialY,

        /**
         * Aligns this Billboard to the screen, but keeps the Z axis fixed.
         */
        AxialZ;
    }
    
    public BillboardControl() {
        super();
        orient = new Matrix3f();
        look = new Vector3f();
        left = new Vector3f();
        alignment = Alignment.Screen;
    }

    public Control cloneForSpatial(Spatial spatial) {
        BillboardControl control = new BillboardControl();
        control.alignment = this.alignment;
        control.setSpatial(spatial);
        return control;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        Camera cam = vp.getCamera();
        rotateBillboard(cam);
    }

    /**
     * rotate the billboard based on the type set
     *
     * @param cam
     *            Camera
     */
    private void rotateBillboard(Camera cam) {
        switch (alignment) {
            case AxialY:
                rotateAxial(cam, Vector3f.UNIT_Y);
                break;
            case AxialZ:
                rotateAxial(cam, Vector3f.UNIT_Z);
                break;
            case Screen:
                rotateScreenAligned(cam);
                break;
            case Camera:
                rotateCameraAligned(cam);
                break;
        }
    }

    /**
     * Aligns this Billboard so that it points to the camera position.
     *
     * @param camera
     *            Camera
     */
    private void rotateCameraAligned(Camera camera) {
        look.set(camera.getLocation()).subtractLocal(
                spatial.getWorldTranslation());
        // coopt left for our own purposes.
        Vector3f xzp = left;
        // The xzp vector is the projection of the look vector on the xz plane
        xzp.set(look.x, 0, look.z);

        // check for undefined rotation...
        if (xzp.equals(Vector3f.ZERO)) {
            return;
        }

        look.normalizeLocal();
        xzp.normalizeLocal();
        float cosp = look.dot(xzp);

        // compute the local orientation matrix for the billboard
        orient.set(0, 0, xzp.z);
        orient.set(0, 1, xzp.x * -look.y);
        orient.set(0, 2, xzp.x * cosp);
        orient.set(1, 0, 0);
        orient.set(1, 1, cosp);
        orient.set(1, 2, look.y);
        orient.set(2, 0, -xzp.x);
        orient.set(2, 1, xzp.z * -look.y);
        orient.set(2, 2, xzp.z * cosp);

        // The billboard must be oriented to face the camera before it is
        // transformed into the world.
        spatial.setLocalRotation(orient);
        spatial.updateGeometricState();
    }

    /**
     * Rotate the billboard so it points directly opposite the direction the
     * camera's facing
     *
     * @param camera
     *            Camera
     */
    private void rotateScreenAligned(Camera camera) {
        // coopt diff for our in direction:
        look.set(camera.getDirection()).negateLocal();
        // coopt loc for our left direction:
        left.set(camera.getLeft()).negateLocal();
        orient.fromAxes(left, camera.getUp(), look);
        spatial.setLocalRotation(orient);
        spatial.updateGeometricState();
    }

    /**
     * Rotate the billboard towards the camera, but keeping a given axis fixed.
     *
     * @param camera
     *            Camera
     */
    private void rotateAxial(Camera camera, Vector3f axis) {
        // Compute the additional rotation required for the billboard to face
        // the camera. To do this, the camera must be inverse-transformed into
        // the model space of the billboard.
        look.set(camera.getLocation()).subtractLocal(
                spatial.getWorldTranslation());
        spatial.getWorldRotation().mult(look, left); // coopt left for our own
        // purposes.
        left.x *= 1.0f / spatial.getWorldScale().x;
        left.y *= 1.0f / spatial.getWorldScale().y;
        left.z *= 1.0f / spatial.getWorldScale().z;

        // squared length of the camera projection in the xz-plane
        float lengthSquared = left.x * left.x + left.z * left.z;
        if (lengthSquared < FastMath.FLT_EPSILON) {
            // camera on the billboard axis, rotation not defined
            return;
        }

        // unitize the projection
        float invLength = FastMath.invSqrt(lengthSquared);
        if (axis.y == 1) {
            left.x *= invLength;
            left.y = 0.0f;
            left.z *= invLength;

            // compute the local orientation matrix for the billboard
            orient.set(0, 0, left.z);
            orient.set(0, 1, 0);
            orient.set(0, 2, left.x);
            orient.set(1, 0, 0);
            orient.set(1, 1, 1);
            orient.set(1, 2, 0);
            orient.set(2, 0, -left.x);
            orient.set(2, 1, 0);
            orient.set(2, 2, left.z);
        } else if (axis.z == 1) {
            left.x *= invLength;
            left.y *= invLength;
            left.z = 0.0f;

            // compute the local orientation matrix for the billboard
            orient.set(0, 0, left.y);
            orient.set(0, 1, left.x);
            orient.set(0, 2, 0);
            orient.set(1, 0, -left.y);
            orient.set(1, 1, left.x);
            orient.set(1, 2, 0);
            orient.set(2, 0, 0);
            orient.set(2, 1, 0);
            orient.set(2, 2, 1);
        }

        // The billboard must be oriented to face the camera before it is
        // transformed into the world.
        spatial.setLocalRotation(orient);
        spatial.updateGeometricState();
    }

    /**
     * Returns the alignment this Billboard is set too.
     *
     * @return The alignment of rotation, AxialY, AxialZ, Camera or Screen.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Sets the type of rotation this Billboard will have. The alignment can
     * be Camera, Screen, AxialY, or AxialZ. Invalid alignments will
     * assume no billboard rotation.
     */
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    @Override
    public void write(JmeExporter e) throws IOException {
        super.write(e);
        OutputCapsule capsule = e.getCapsule(this);
        capsule.write(orient, "orient", null);
        capsule.write(look, "look", null);
        capsule.write(left, "left", null);
        capsule.write(alignment, "alignment", Alignment.Screen);
    }

    @Override
    public void read(JmeImporter e) throws IOException {
        super.read(e);
        InputCapsule capsule = e.getCapsule(this);
        orient = (Matrix3f) capsule.readSavable("orient", null);
        look = (Vector3f) capsule.readSavable("look", null);
        left = (Vector3f) capsule.readSavable("left", null);
        alignment = capsule.readEnum("alignment", Alignment.class, Alignment.Screen);
    }
}
