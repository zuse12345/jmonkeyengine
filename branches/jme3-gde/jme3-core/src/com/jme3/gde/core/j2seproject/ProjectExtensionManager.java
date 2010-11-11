/*
 *  Copyright (c) 2009-2010 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.j2seproject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class ProjectExtensionManager {

    private String extensionName;
    private String extensionVersion;
    private String extensionTargets;
    private String[] extensionDependencies;

    public ProjectExtensionManager(String extensionName, String extensionVersion, String extensionTargets, String[] extensionDependencies) {
        this.extensionName = extensionName;
        this.extensionVersion = extensionVersion;
        this.extensionTargets = extensionTargets;
        this.extensionDependencies = extensionDependencies;
    }

    public void checkExtension(Project proj) {
        if (!(proj instanceof J2SEProject)) {
            Logger.getLogger(ProjectExtensionManager.class.getName()).log(Level.WARNING, "Trying to load Assets Properties from non-asset project");
            return;
        }

        FileObject projDir = proj.getProjectDirectory();
        final FileObject buildXmlFO = J2SEProjectUtil.getBuildXml((J2SEProject) proj);
        if (buildXmlFO == null) {
            Logger.getLogger(ProjectExtensionManager.class.getName()).log(Level.WARNING, "The project build script does not exist, the project cannot be extended by jMP.");
            return;
        }
        FileObject assetsBuildFile = getImpl(projDir);
        AntBuildExtender extender = proj.getLookup().lookup(AntBuildExtender.class);
        if (extender != null) {
            assert assetsBuildFile != null;
            if (extender.getExtension(extensionName) == null) { // NOI18N
                AntBuildExtender.Extension ext = extender.addExtension(extensionName, assetsBuildFile); // NOI18N
                for (int i = 0; i < extensionDependencies.length; i += 2) {
                    String target = extensionDependencies[i];
                    String extension = extensionDependencies[i + 1];
                    ext.addDependency(target, extension); // NOI18N
                }
                try {
                    ProjectManager.getDefault().saveProject(proj);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            Logger.getLogger(ProjectExtensionManager.class.getName()).log(Level.WARNING, "Trying to include assets build snippet in project type that doesn't support AntBuildExtender API contract.");
        }

    }

    private FileObject getImpl(FileObject projDir) {
        FileObject assetsImpl = projDir.getFileObject("nbproject/" + extensionName + "-impl.xml");
        if (assetsImpl == null) {
            Logger.getLogger(ProjectExtensionManager.class.getName()).log(Level.INFO, "Creating {0}-impl.xml", extensionName);
            assetsImpl = createImpl(projDir);
        } else {
            try {
                if (!assetsImpl.asLines().get(1).startsWith("<!--" + extensionName + "-impl.xml v1.0-->")) {
                    assetsImpl.delete();
                    assetsImpl = createImpl(projDir);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return assetsImpl;
    }

    private FileObject createImpl(FileObject projDir) {
        FileLock lock = null;
        FileObject file = null;
        try {
            file = projDir.getFileObject("nbproject").createData(extensionName + "-impl.xml");
            lock = file.lock();
            OutputStreamWriter out = new OutputStreamWriter(file.getOutputStream(lock));
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<!--" + extensionName + "-impl.xml " + extensionVersion + "-->\n");
            out.write("<project name=\"" + extensionName + "-impl\" basedir=\"..\">\n");
            out.write(extensionTargets);
            out.write("</project>\n");
            out.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
        return file;
    }

    public void removeExtension(Project proj) {
        if (!(proj instanceof J2SEProject)) {
            Logger.getLogger(ProjectExtensionManager.class.getName()).log(Level.WARNING, "Trying to load Assets Properties from non-asset project");
            return;
        }

        FileObject projDir = proj.getProjectDirectory();
        final FileObject buildXmlFO = J2SEProjectUtil.getBuildXml((J2SEProject) proj);
        if (buildXmlFO == null) {
            Logger.getLogger(ProjectExtensionManager.class.getName()).log(Level.WARNING, "The project build script does not exist, the project cannot be extended by jMP.");
            return;
        }
        FileObject assetsBuildFile = getImpl(projDir);
        AntBuildExtender extender = proj.getLookup().lookup(AntBuildExtender.class);
        if (extender != null) {
            assert assetsBuildFile != null;
            if (extender.getExtension(extensionName) != null) { // NOI18N
                extender.removeExtension(extensionName); // NOI18N
                try {
                    assetsBuildFile.delete();
                    ProjectManager.getDefault().saveProject(proj);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            Logger.getLogger(ProjectExtensionManager.class.getName()).log(Level.WARNING, "Trying to include assets build snippet in project type that doesn't support AntBuildExtender API contract.");
        }
    }
}
