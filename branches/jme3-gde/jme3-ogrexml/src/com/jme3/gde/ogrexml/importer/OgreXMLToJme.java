/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.ogrexml.importer;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.scene.Spatial;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

public final class OgreXMLToJme implements ActionListener {

    private final DataObject context;

    public OgreXMLToJme(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        final ProjectAssetManager manager = context.getLookup().lookup(ProjectAssetManager.class);
        if (manager == null) {
            StatusDisplayer.getDefault().setStatusText("Project has no AssetManager!");
            return;
        }

        if (context != null) {
            Callable run = new Callable() {

                public Void call() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Importing OgreXML");
                    progressHandle.start();

                    FileObject file = context.getPrimaryFile();
                    Spatial model = manager.getManager().loadModel(manager.getRelativeAssetPath(file.getPath()));

                    String outputPath = file.getParent().getPath() + File.separator + file.getName() + ".j3o";
                    BinaryExporter exp = BinaryExporter.getInstance();
                    try {
                        exp.save(model, new File(outputPath));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    progressHandle.finish();
                    //try make NetBeans update the tree.. :/
                    context.getPrimaryFile().getParent().refresh();
                    return null;
                }
            };
            SceneApplication.getApplication().enqueue(run);
        }

        StatusDisplayer.getDefault().setStatusText("Import with project AssetManager: " + manager);
    }
}
