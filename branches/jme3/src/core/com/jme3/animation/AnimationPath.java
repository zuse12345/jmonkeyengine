/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.animation;

import com.jme3.asset.AssetManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Nehon
 */
public class AnimationPath implements Control {

    private Spatial target;
    private boolean enabled = true;
    private boolean playing = false;
    private int currentWayPoint;
    private float currentValue;
    private List<Vector3f> wayPoints = new ArrayList<Vector3f>();
    private Node debugNode;
    AssetManager assetManager;

    public enum PathInterpolation {

        Linear,
        CatmullRom
    }
    private PathInterpolation pathInterpolation = PathInterpolation.CatmullRom;

    public AnimationPath(Spatial target) {
        super();
        this.target=target;
        target.addControl(this);
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSpatial(Spatial spatial) {
        target = spatial;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void update(float tpf) {       
        if(playing){
            Vector3f temp=new Vector3f();
            switch (pathInterpolation) {
                case CatmullRom:           
                      List<Vector3f> CRcontrolPoints = getCatmullRomWayPoints(wayPoints);
                      temp = FastMath.interpolateCatmullRom(currentValue, CRcontrolPoints.get(currentWayPoint),
                            CRcontrolPoints.get(currentWayPoint + 1), CRcontrolPoints.get(currentWayPoint + 2), CRcontrolPoints.get(currentWayPoint + 3));
                    break;
                case Linear:
                      temp = FastMath.interpolateLinear(currentValue,wayPoints.get(currentWayPoint),wayPoints.get(currentWayPoint+1));
                    break;
                default:

                    break;
            }
            target.setLocalTranslation(temp);
              currentValue=Math.min(currentValue+tpf, 1.0f);
              if(currentValue==1.0f){
                  currentValue=0;
                  currentWayPoint++;
              }
              if(currentWayPoint==wayPoints.size()-1){
                  stop();
              }


        }
    }

    private void attachDebugNode(Node root) {
        if (debugNode == null) {
            debugNode = new Node("AnimationPathFor" + target.getName());
            Material m = assetManager.loadMaterial("Common/Materials/RedColor.j3m");
            for (Iterator<Vector3f> it = wayPoints.iterator(); it.hasNext();) {
                Vector3f cp = it.next();
                Geometry geo = new Geometry("box", new Box(cp, 0.3f, 0.3f, 0.3f));
                geo.setMaterial(m);
                debugNode.attachChild(geo);

            }

            switch (pathInterpolation) {
                case CatmullRom:
                    debugNode.attachChild(CreateCatmullRomPath());
                    break;
                case Linear:
                    debugNode.attachChild(CreateLinearPath());
                    break;
                default:
                    debugNode.attachChild(CreateLinearPath());
                    break;
            }

            root.attachChild(debugNode);
        }
    }

    private Geometry CreateLinearPath() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Blue);

        float[] array = new float[wayPoints.size() * 3];
        short[] indices = new short[(wayPoints.size() - 1) * 2];
        int i = 0;
        int cpt = 0;
        int k = 0;
        int j = 0;
        for (Iterator<Vector3f> it = wayPoints.iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            array[i] = vector3f.x;
            i++;
            array[i] = vector3f.y;
            i++;
            array[i] = vector3f.z;
            i++;
            if (it.hasNext()) {
                k = j;
                indices[cpt] = (short) k;
                cpt++;
                k++;
                indices[cpt] = (short) k;
                cpt++;
                j++;
            }
        }

        Mesh lineMesh = new Mesh();
        lineMesh.setMode(Mesh.Mode.Lines);
        lineMesh.setBuffer(VertexBuffer.Type.Position, 3, array);
        lineMesh.setBuffer(VertexBuffer.Type.Index, (wayPoints.size() - 1) * 2, indices);
        lineMesh.updateBound();
        lineMesh.updateCounts();

        Geometry lineGeometry = new Geometry("line", lineMesh);
        lineGeometry.setMaterial(mat);
        return lineGeometry;
    }

    private Geometry CreateCatmullRomPath() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Blue);
        int nbSubSegments = 10;

        List<Vector3f> CRcontrolPoints = getCatmullRomWayPoints(wayPoints);

        float[] array = new float[(((wayPoints.size() - 1) * nbSubSegments) + 1) * 3];
        short[] indices = new short[((wayPoints.size() - 1) * nbSubSegments) * 2];
        int i = 0;
        int cptCP = 0;
        for (Iterator<Vector3f> it = wayPoints.iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            array[i] = vector3f.x;
            i++;
            array[i] = vector3f.y;
            i++;
            array[i] = vector3f.z;
            i++;
            if (it.hasNext()) {
                for (int j = 1; j < nbSubSegments; j++) {
                    Vector3f temp = FastMath.interpolateCatmullRom((float) j / nbSubSegments, CRcontrolPoints.get(cptCP),
                            CRcontrolPoints.get(cptCP + 1), CRcontrolPoints.get(cptCP + 2), CRcontrolPoints.get(cptCP + 3));
                    array[i] = temp.x;
                    i++;
                    array[i] = temp.y;
                    i++;
                    array[i] = temp.z;
                    i++;
                }
            }
            cptCP++;
        }

        i = 0;
        int k = 0;
        for (int j = 0; j < ((wayPoints.size() - 1) * nbSubSegments); j++) {
            k = j;
            indices[i] = (short) k;
            i++;
            k++;
            indices[i] = (short) k;
            i++;
        }



        Mesh lineMesh = new Mesh();
        lineMesh.setMode(Mesh.Mode.Lines);
        lineMesh.setBuffer(VertexBuffer.Type.Position, 3, array);
        lineMesh.setBuffer(VertexBuffer.Type.Index,((wayPoints.size() - 1) * nbSubSegments) * 2, indices);
        lineMesh.updateBound();
        lineMesh.updateCounts();

        Geometry lineGeometry = new Geometry("line", lineMesh);
        lineGeometry.setMaterial(mat);
        return lineGeometry;
    }

    private List<Vector3f> getCatmullRomWayPoints(List<Vector3f> list) {
        List<Vector3f> cr = new ArrayList<Vector3f>();
        int nb = list.size() - 1;
        cr.add(list.get(0).subtract(list.get(1).subtract(list.get(0))));
        for (Iterator<Vector3f> it = list.iterator(); it.hasNext();) {
            Vector3f vector3f = it.next();
            cr.add(vector3f);
        }
        cr.add(list.get(nb).add(list.get(nb).subtract(list.get(nb - 1))));
        return cr;
    }

    public void render(RenderManager rm, ViewPort vp) {
    }

    public void write(JmeExporter ex) throws IOException {
    }

    public void read(JmeImporter im) throws IOException {
    }

    public void play() {
        playing = true;
        currentWayPoint=0;
    }

    public void stop() {
        playing = false;
    }

    public void addWayPoint(Vector3f wayPoint) {
        wayPoints.add(wayPoint);
    }

    public Vector3f getWayPoint(int i) {
        return wayPoints.get(i);
    }

    public void removeWayPoint(Vector3f wayPoint) {
        wayPoints.remove(wayPoint);
    }

    public Iterator<Vector3f> iterator() {
        return wayPoints.iterator();
    }

    public PathInterpolation getPathInterpolation() {
        return pathInterpolation;
    }

    public void setPathInterpolation(PathInterpolation pathInterpolation) {
        this.pathInterpolation = pathInterpolation;
    }

    public void disableDebugShape() {
        debugNode.removeFromParent();
        debugNode.detachAllChildren();
        debugNode = null;
        assetManager = null;
    }

    public void enableDebugShape(AssetManager manager, Node rootNode) {
        assetManager = manager;
        attachDebugNode(rootNode);
    }
}
