package jme3test.niftygui;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;

public class TestNiftyGui extends SimpleApplication {

    private Nifty nifty;

    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setAudioRenderer("LWJGL");
        TestNiftyGui app = new TestNiftyGui();
        app.setSettings(settings);
        app.start();
    }

    public void simpleInitApp() {
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(manager,
                                                          inputManager,
                                                          audioRenderer,
                                                          guiViewPort);
        nifty = niftyDisplay.getNifty();

        // WARNING: Hack alert. This is needed so the fonts' textures can be found
        manager.registerLocator("/tutorial/fonts/", ClasspathLocator.class.getName(), "png");

        // load helloworld.xml
        //nifty.fromXml("jme3test/niftygui/helloworld.xml", "start");
        nifty.fromXml("tutorial/tutorial.xml"/*"all/intro.xml"*/, "start");

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);

        // disable the fly cam
        flyCam.setEnabled(false);

        // allow us to interact with gui components
        inputManager.setCursorVisible(true);
    }

}
