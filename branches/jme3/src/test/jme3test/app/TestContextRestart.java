package jme3test.app;

import com.jme3.app.Application;
import com.jme3.system.AppSettings;

public class TestContextRestart {

    public static void main(String[] args) throws InterruptedException{
        AppSettings settings = new AppSettings(true);

        final Application app = new Application();
        app.setSettings(settings);
        app.start();

        Thread.sleep(3000);

        settings.setFullscreen(true);
        settings.setResolution(-1, -1);
        app.setSettings(settings);
        app.restart();

        Thread.sleep(3000);

        app.stop();
    }

}
