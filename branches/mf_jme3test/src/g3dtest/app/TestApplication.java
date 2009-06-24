package g3dtest.app;

import com.g3d.app.Application;
import com.g3d.system.AppSettings;

/**
 * Test Application functionality, such as create, restart, destroy, etc.
 * @author Kirill
 */
public class TestApplication {

    public static void main(String[] args) throws InterruptedException{
        System.out.println("Creating application..");
        Application app = new Application();
        System.out.println("Starting application in LWJGL mode..");
        app.start();
        System.out.println("Waiting 5 seconds");
        Thread.sleep(5000);
        System.out.println("Closing application..");
        app.stop();

        Thread.sleep(2000);
        System.out.println("Starting in fullscreen mode");
        app = new Application();
        app.setSettings(new AppSettings(AppSettings.Template.DesktopFullscreen));
        app.start();
        Thread.sleep(5000);
        app.stop();

        System.out.println("Creating JOGL application..");
        AppSettings settings = new AppSettings(AppSettings.Template.Default640x480);
        settings.setRenderer(AppSettings.JOGL);
        app = new Application();
        app.setSettings(settings);
        app.start();
        Thread.sleep(5000);
        app.stop();
    }

}
