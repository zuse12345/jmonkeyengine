
package com.jme3.gde.core.codeless;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;

public class CodelessProjectFactory implements ProjectFactory {

    public static final String PROJECT_DIR = "nbproject";

    //Specifies when a project is a project, i.e.,
    //if the project directory "texts" is present:
    @Override
    public boolean isProject(FileObject projectDirectory) {
        FileObject configDir=projectDirectory.getFileObject(PROJECT_DIR);
        if(configDir != null){
            if(configDir.getFileObject("assets.properties")!=null){
                return true;
            }
        }
        return false;
    }

    //Specifies when the project will be opened, i.e.,
    //if the project exists:
    @Override
    public Project loadProject(FileObject dir, ProjectState state) throws IOException {
        return isProject(dir) ? new CodelessProject(dir, state) : null;
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        FileObject projectRoot = project.getProjectDirectory();
        if (projectRoot.getFileObject(PROJECT_DIR) == null) {
            throw new IOException("Project dir " + projectRoot.getPath() +
                    " deleted," +
                    " cannot save project");
        }
        //Force creation of the texts dir if it was deleted:
        ((CodelessProject) project).getConfigFolder(true);
    }
    
}
