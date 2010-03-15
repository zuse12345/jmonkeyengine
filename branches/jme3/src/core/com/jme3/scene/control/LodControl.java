package com.jme3.scene.control;

import com.jme3.bounding.BoundingVolume;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;

public class LodControl extends AbstractControl {

    private float trisPerPixel = 1f;
    private float distTolerance = 1f;
    private float lastDistance = 0f;
    private int lastLevel = 0;
    private int numLevels;
    private int[] numTris;

    public LodControl(Geometry geom){
        super(geom);
        Mesh mesh = geom.getMesh();
        numLevels = mesh.getNumLodLevels();
        numTris = new int[numLevels];
        for (int i = numLevels - 1; i >= 0; i--)
            numTris[i] = mesh.getTriangleCount(i);

    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    protected void controlRender(RenderManager rm, ViewPort vp){
        BoundingVolume bv = spatial.getWorldBound();
        float newDistance = bv.distanceTo(vp.getCamera().getLocation());
        int level;

        if (Math.abs(newDistance - lastDistance) <= distTolerance)
            level = lastLevel; // we haven't moved relative to the model, send the old measurement back.
        else if (lastDistance > newDistance && lastLevel == 0)
            level = lastLevel; // we're already at the lowest setting and we just got closer to the model, no need to keep trying.
        else if (lastDistance < newDistance && lastLevel == numLevels - 1)
            level = lastLevel; // we're already at the highest setting and we just got further from the model, no need to keep trying.
        else{
            lastDistance = newDistance;

            // estimate area of polygon via bounding volume
            float area = AreaUtils.calcScreenArea(bv, lastDistance, vp.getCamera().getWidth());
            float trisToDraw = area * trisPerPixel;
            level = numLevels - 1;
            for (int i = numLevels; --i >= 0;){
                if (trisToDraw - numTris[i] < 0){
                    break;
                }
                level = i;
            }
            lastLevel = level;
        }

        spatial.setLodLevel(level);
    }

    public ControlType getType() {
        return ControlType.LevelOfDetail;
    }
}
