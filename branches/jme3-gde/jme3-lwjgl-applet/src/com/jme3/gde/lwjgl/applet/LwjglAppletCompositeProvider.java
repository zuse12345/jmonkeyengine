/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.lwjgl.applet;

import com.jme3.gde.core.j2seproject.ProjectExtensionManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 * @author Tomas Zezula
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-java-j2seproject", category = "Application", position = 300)
public class LwjglAppletCompositeProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CAT_LWJGL_APPLET = "LwjglApplet"; // NOI18N
    private static LwjglAppletProperties jwsProps = null;

    public LwjglAppletCompositeProvider() {
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CAT_LWJGL_APPLET,
                NbBundle.getMessage(LwjglAppletCompositeProvider.class, "LBL_Category_LWJGL_Applet"), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        // use OkListener to create new configuration first
        jwsProps = new LwjglAppletProperties(context.lookup(Project.class));
        category.setStoreListener(new SavePropsListener(jwsProps, context.lookup(Project.class)));
//        category.setOkButtonListener(new OKPropsListener(jwsProps, context.lookup(Project.class)));
        return new LwjglAppletCustomizerPanel(jwsProps);
    }

//    private class OKPropsListener implements ActionListener {
//
//        private LwjglAppletProperties jwsProps;
//        private Project j2seProject;
//
//        public OKPropsListener(LwjglAppletProperties props, Project proj) {
//            this.j2seProject = proj;
//            this.jwsProps = props;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//        }
//    }

    private class SavePropsListener implements ActionListener {

        private String extensionName = "lwjglapplet";
        private String extensionVersion = "v0.7";
        private String extensionTargets =
                  "    <target name=\"-lwjgl-applet\" depends=\"-test-lwjgl-applet-enabled\" if=\"is.lwjgl.applet.enabled\">\n"
                + "        <echo>LWJGL Applet Creation</echo>\n"
                + "    </target>\n"
                + "    <target name=\"-test-lwjgl-applet-enabled\">\n"
                + "        <condition property=\"is.lwjgl.applet.enabled\">\n"
                + "            <istrue value=\"${lwjgl.applet.enabled}\"/>\n"
                + "        </condition>\n"
                + "    </target>\n";
        private String[] extensionDependencies = new String[]{"jar", "-lwjgl-applet"};
        private ProjectExtensionManager manager = new ProjectExtensionManager(extensionName, extensionVersion, extensionTargets, extensionDependencies);
        private LwjglAppletProperties properties;
        private Project project;

        public SavePropsListener(LwjglAppletProperties props, Project project) {
            this.properties = props;
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            if ("true".equals(properties.getProperties().getProperty("lwjgl.applet.enabled"))) {
                manager.checkExtension(project);
            } else {
                manager.removeExtension(project);
            }
            try {
                properties.store();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
//        private <C extends ProjectConfiguration> void setActiveConfig(final ProjectConfigurationProvider<C> provider, String displayName) throws IOException {
//            Collection<C> configs = provider.getConfigurations();
//            for (final C c : configs) {
//                if (displayName.equals(c.getDisplayName())) {
//                    try {
//                        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
//
//                            public Void run() throws Exception {
//                                provider.setActiveConfiguration(c);
//                                return null;
//                            }
//                        });
//                    } catch (MutexException mex) {
//                        throw (IOException) mex.getException();
//                    }
//                }
//            }
//        }
    }
}
