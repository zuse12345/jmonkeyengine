/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.lwjgl.applet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author normenhansen
 */
public class LwjglAppletProperties {

    private String[] keyList = new String[]{
        "lwjgl.applet.enabled",
        "lwjgl.applet.mainclass"
    };
    private Project project;
    private EditableProperties properties;

    public LwjglAppletProperties(Project project) {
        this.project = project;
        properties = new EditableProperties(true);
        if (project instanceof J2SEProject) {
            load();
        }
    }

    public EditableProperties getProperties() {
        return properties;
    }

    public void load() {
        properties.clear();
        J2SEPropertyEvaluator eval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (eval == null) {
            return;
        }
        for (int i = 0; i < keyList.length; i++) {
            String string = keyList[i];
            String value = eval.evaluator().getProperty(string);
            if (value != null) {
                properties.setProperty(string, value);
            }
            //TODO: create defaults!
        }
    }

    public void store() throws IOException {
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (projPropsFO == null) {
            return;
        }
        final EditableProperties ep = new EditableProperties(true);

        try {
            final InputStream is = projPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {

                @Override
                public Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    putProperties(properties, ep);
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }

    }

    private void putProperties(EditableProperties from, EditableProperties to) {
        for (int i = 0; i < keyList.length; i++) {
            String string = keyList[i];
            String value = from.getProperty(string);
            if (value == null || "".equals(value)) {
                to.remove(string);
            } else {
                to.put(string, value);
            }
        }
    }
}
