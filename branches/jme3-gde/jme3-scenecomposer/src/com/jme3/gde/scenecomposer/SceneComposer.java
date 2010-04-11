/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme3.gde.scenecomposer;

import com.jme3.gde.core.scene.SceneListener;
import com.jme3.gde.core.scene.nodes.JmeNode;
import com.jme3.gde.core.scene.nodes.JmeSpatial;
import java.awt.image.BufferedImage;

/**
 *
 * @author normenhansen
 */
public class SceneComposer implements SceneListener{
    private JmeNode rootNode;

    public void rootNodeChanged(JmeSpatial spatial) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void nodeSelected(JmeSpatial spatial) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void previewChanged(BufferedImage preview, Object source) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

}
