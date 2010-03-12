/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.importer;

import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.importer.panels.ImportOgreXMLDialog;
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

public final class ImportOgreXML implements ActionListener {

    private final Project context;

    public ImportOgreXML(Project context) {
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
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Importing OgreXML");
                    progressHandle.start();
                    manager.getManager().registerLocator(dialog.getModelPath().getParent().getPath(),
                                                              "com.jme3.asset.plugins.FileSystemLocator",
                                                              "*");
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
                    context.getProjectDirectory().getFileObject("assets/models").getChildren();
                }
            };
            new Thread(run).start();
        }

        StatusDisplayer.getDefault().setStatusText("Import with project AssetManager: "+manager);
    }

    private void doImport(){
    }

}
