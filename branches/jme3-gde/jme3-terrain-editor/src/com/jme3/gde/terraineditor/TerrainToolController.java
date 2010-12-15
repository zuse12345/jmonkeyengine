/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.terraineditor;

import com.jme3.asset.AssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.controller.SceneToolController;
import com.jme3.gde.core.sceneexplorer.nodes.JmeNode;
import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.gde.terraineditor.TerrainEditorTopComponent.TerrainEditButton;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.concurrent.Callable;

/**
 * The controller for the terrain modification tools. It will in turn interact
 * with the TerrainEditorController to actually modify the terrain in the scene.
 *
 * Maintains the edit tool state: what tool is activated and what should be done with it.
 * 
 * @author bowens
 */
public class TerrainToolController extends SceneToolController {

    private JmeSpatial jmeRootNode;
    private TerrainEditButton currentEditButtonState = TerrainEditButton.none;
    private Geometry marker;
    private TerrainEditorController editorController;
    private float heightToolRadius;
    private float heightToolHeight;

    public TerrainToolController(Node toolsNode, AssetManager manager, JmeNode rootNode) {
        super(toolsNode, manager);
        this.jmeRootNode = rootNode;
    }

    public void setEditorController(TerrainEditorController editorController) {
        this.editorController = editorController;
    }

    /**
     * divides the incoming value by 100.
     */
    public void setHeightToolHeight(float heightToolHeight) {
        this.heightToolHeight = heightToolHeight/100f;
    }

    public void setHeightToolRadius(float heightToolRadius) {
        this.heightToolRadius = heightToolRadius;
    }



    @Override
    protected void initTools() {
        super.initTools();

        marker = new Geometry("edit marker");
        Mesh m = new Sphere(8, 8, 3);
        marker.setMesh(m);
        Material mat = new Material(manager, "Common/MatDefs/Misc/WireColor.j3md");
        mat.setColor("m_Color", ColorRGBA.Green);
        marker.setMaterial(mat);
        marker.setLocalTranslation(0,0,0);
    }

    protected void setMarkerRadius(float radius) {
        //((Sphere)marker.getMesh()).set;
    }

    public TerrainEditButton getCurrentEditButtonState() {
        return currentEditButtonState;
    }

    public void setTerrainEditButtonState(final TerrainEditButton state) {

        currentEditButtonState = state;
        if (state == TerrainEditButton.none) {
            hideEditTool();
        } else if (state == TerrainEditButton.raiseTerrain || state == TerrainEditButton.lowerTerrain) {
            showEditTool(state);
        }
    }


    public void hideEditTool() {
        SceneApplication.getApplication().enqueue(new Callable<Object>() {
            
            public Object call() throws Exception {
                doHideEditTool();
                return null;
            }
        });
    }

    private void doHideEditTool() {
        marker.removeFromParent();
    }

    public void showEditTool(final TerrainEditButton terrainEditButton) {
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            public Object call() throws Exception {
                doShowEditTool(terrainEditButton);
                return null;
            }
        });
        
    }

    private void doShowEditTool(TerrainEditButton terrainEditButton) {
        //TODO show different tool marker depending on terrainEditButton type
        
        toolsNode.attachChild(marker);

        
    }

    void doMoveEditTool(Vector3f pos) {
        if (marker != null) {
            marker.setLocalTranslation(pos);
            //System.out.println(marker.getLocalTranslation());
        }
    }

    public Vector3f getMarkerLocation() {
        if (marker != null)
            return marker.getLocalTranslation();
        else
            return null;
    }

    public boolean isTerrainEditButtonEnabled() {
        return getCurrentEditButtonState() != TerrainEditButton.none;
    }

    /**
     * raise/lower/paint the terrain
     */
    public void doTerrainEditToolActivated() {

        if (TerrainEditButton.raiseTerrain == getCurrentEditButtonState() ) {
            editorController.doModifyTerrainHeight(getMarkerLocation(), heightToolRadius, heightToolHeight);
        }
        else if (TerrainEditButton.lowerTerrain == getCurrentEditButtonState() ) {
            editorController.doModifyTerrainHeight(getMarkerLocation(), heightToolRadius, -heightToolHeight);
        }
        //TODO add in paint/erase
    }
}
