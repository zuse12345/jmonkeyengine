/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.welcome;

import atmosphere.PlanetView;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.PreviewRequest;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.SceneListener;
import com.jme3.gde.core.scene.SceneRequest;
import com.jme3.gde.core.sceneexplorer.nodes.NodeUtility;
import com.jme3.scene.Node;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.concurrent.Callable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 *
 * @author normenhansen
 */
public class WelcomeScreen implements ScreenController {

    PlanetView planetView;
    SceneRequest request;

    public void startScreen() {
        final Node rootNode = new Node("Welcome Screen");
        request = new SceneRequest(this, NodeUtility.createNode(rootNode), new ProjectAssetManager(null));
        request.setHelpCtx(new HelpCtx("com.jme3.gde.core.about"));
        request.setWindowTitle("Welcome to jMonkeyEngine");
        SceneApplication.getApplication().addSceneListener(new SceneListener() {

            @Override
            public void sceneRequested(SceneRequest request) {
                if (request.getRequester() == WelcomeScreen.this) {
                    SceneApplication.getApplication().getStateManager().attach(planetView);
                }
            }

            @Override
            public boolean sceneClose(SceneRequest request) {
                SceneApplication.getApplication().getStateManager().detach(planetView);
                SceneApplication.getApplication().removeSceneListener(this);
                return true;
            }

            @Override
            public void previewRequested(PreviewRequest request) {
            }
        });
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                planetView = new PlanetView(rootNode, SceneApplication.getApplication().getViewPort(), SceneApplication.getApplication().getCamera(), WelcomeScreen.this);
                SceneApplication.getApplication().requestScene(request);
                return null;
            }
        });
    }

    public void startTutorial() {
        try {
            planetView.getNifty().gotoScreen("end");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void quit() {
        SceneApplication.getApplication().closeScene(request);
    }

    public void bind(Nifty nifty, Screen screen) {
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
