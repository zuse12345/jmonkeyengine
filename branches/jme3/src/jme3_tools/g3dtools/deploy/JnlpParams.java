package g3dtools.deploy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JnlpParams {

    private URL codebase;
    private float version;

    private String title;
    private String vendor;
    private URL homepage;
    private URL icon;

    private String description;
    private String shortDesc;

    private boolean desktopShortcut;
    private String submenuShortcut;

    private float javaVersion;
    private int initHeapSize, maxHeapSize;

    private boolean isApplet;
    private int width, height;
    private String mainClass;
    private URL mainJar;
    private List<URL> jars;

    private boolean allowPermissions;

    public static final JnlpParams createSome(){
        try{
            JnlpParams p = new JnlpParams();
            p.codebase = new URL("http://mfkarpg.110mb.com/applet/");
            p.version = 0.1f;
            p.title = "Jnlp Gen Test";
            p.vendor = "Gibbon Entertainment, Inc.";
            p.homepage = new URL("http://www.jmonkeyengine.com/");
            p.icon = new URL("http://mfkarpg.110mb.com/applet/icon.png");
            p.description = "This is an application that really doesn't do much.";
            p.shortDesc = "Stuff";
            p.desktopShortcut = true;
            p.submenuShortcut = "Gibbon Software";
            p.javaVersion = 1.5f;
            p.initHeapSize = 128;
            p.maxHeapSize = 256;
            p.isApplet = false;
            p.mainClass = "g3dgame.cubefield.CubeField";
            p.mainJar = new URL("http://mfkarpg.110mb.com/applet/main.jar");
            p.allowPermissions = false;
            return p;
        }catch (MalformedURLException ex){
            Logger.getLogger(JnlpParams.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean isAllowPermissions() {
        return allowPermissions;
    }

    public void setAllowPermissions(boolean allowPermissions) {
        this.allowPermissions = allowPermissions;
    }

    public URL getCodebase() {
        return codebase;
    }

    public void setCodebase(URL codebase) {
        this.codebase = codebase;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDesktopShortcut() {
        return desktopShortcut;
    }

    public void setDesktopShortcut(boolean desktopShortcut) {
        this.desktopShortcut = desktopShortcut;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public URL getHomepage() {
        return homepage;
    }

    public void setHomepage(URL homepage) {
        this.homepage = homepage;
    }

    public URL getIcon() {
        return icon;
    }

    public void setIcon(URL icon) {
        this.icon = icon;
    }

    public int getInitHeapSize() {
        return initHeapSize;
    }

    public void setInitHeapSize(int initHeapSize) {
        this.initHeapSize = initHeapSize;
    }

    public boolean isApplet() {
        return isApplet;
    }

    public void setApplet(boolean isApplet) {
        this.isApplet = isApplet;
    }

    public List<URL> getJars() {
        return jars;
    }

    public void setJars(List<URL> jars) {
        this.jars = jars;
    }

    public float getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(float javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public URL getMainJar() {
        return mainJar;
    }

    public void setMainJar(URL mainJar) {
        this.mainJar = mainJar;
    }

    public int getMaxHeapSize() {
        return maxHeapSize;
    }

    public void setMaxHeapSize(int maxHeapSize) {
        this.maxHeapSize = maxHeapSize;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getSubmenuShortcut() {
        return submenuShortcut;
    }

    public void setSubmenuShortcut(String submenuShortcut) {
        this.submenuShortcut = submenuShortcut;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
