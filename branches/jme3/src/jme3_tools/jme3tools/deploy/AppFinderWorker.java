package jme3tools.deploy;

import com.jme3.app.Application;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.SwingWorker;

public class AppFinderWorker extends SwingWorker {

    private List<File> jars = new ArrayList<File>();
    private String[] appList;
    private DeployAppSetting dialog;

    public AppFinderWorker(DeployAppSetting dialog, AppConfig cfg){
        this.dialog = dialog;
        File[] files = cfg.getInputSources();
        for (File file : files){
            if (file.isFile())
                jars.add(file);
        }
    }

    protected void done(){
        dialog.setAppList(appList);
    }

    @Override
    protected Object doInBackground() throws Exception {
        List<String> appNames = new ArrayList<String>();
        for (File jarFile : jars){
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[]{ jarUrl });
            JarFile jf = new JarFile(jarFile);
            Enumeration<? extends JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()){
                JarEntry entry = entries.nextElement();
                if (entry.getName().toLowerCase().endsWith(".class")
                  && !entry.getName().contains("$")){
                    // try to determine class name and load it
                    String name = entry.getName();
                    name = name.substring(0, name.length() - 6);
                    name = name.replaceAll("/", ".");

                    Class<?> clazz = loader.loadClass(name);
                    if (Application.class.isAssignableFrom(clazz)){
                        appNames.add(clazz.getName());
                    }
                }
            }

        }
        appList = appNames.toArray(new String[appNames.size()]);
        return appList;
    }
}
