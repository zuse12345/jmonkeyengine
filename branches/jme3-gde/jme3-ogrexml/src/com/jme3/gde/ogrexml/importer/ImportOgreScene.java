/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.ogrexml.importer;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.ogrexml.importer.panels.ImportOgreXMLDialog;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.MeshLoader;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class ImportOgreScene implements ActionListener {

    private final Project context;

    public ImportOgreScene(Project context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        //TODO: use separate AssetManager
        final ProjectAssetManager manager=context.getLookup().lookup(ProjectAssetManager.class);
        if(manager==null){
            StatusDisplayer.getDefault().setStatusText("Project has no AssetManager!");
            return;
        }

        final ImportOgreXMLDialog dialog=new ImportOgreXMLDialog(new JFrame(), true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if(dialog.getModelPath()!=null&&dialog.getMaterialPath()!=null){
            Runnable run=new Runnable() {
                public void run() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Importing OgreScene");
                    progressHandle.start();
                    manager.getManager().registerLocator(dialog.getModelPath().getParent().getPath(),
                                            "com.jme3.asset.plugins.FileLocator",
                                            "scene", "meshxml",
                                            "material", "jpg", "png");

                    Spatial model=MeshLoader.loadModel(manager.getManager(), dialog.getModelPath().getNameExt(), dialog.getMaterialPath().getNameExt());

                    String name=dialog.getModelPath().getName()+".j3o";
                    File outputDir=FileUtil.toFile(context.getProjectDirectory().getFileObject("assets/models"));
                    BinaryExporter exp = BinaryExporter.getInstance();
                    try {
                        exp.save(model, new File(outputDir.getCanonicalPath()+File.separator+name));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    progressHandle.finish();
                    //try make NetBeans update the tree.. :/
                    context.getProjectDirectory().getFileObject("assets/models").refresh();
                }
            };
            new Thread(run).start();
        }

        StatusDisplayer.getDefault().setStatusText("Import with project AssetManager: "+manager);
    }
}
