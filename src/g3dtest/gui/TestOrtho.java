package g3dtest.gui;

import com.g3d.app.SimpleApplication;
import com.g3d.ui.Picture;

public class TestOrtho extends SimpleApplication {

    public static void main(String[] args){
        TestOrtho app = new TestOrtho();
        app.start();
    }

    public void simpleInitApp() {
        Picture p = new Picture("Picture");
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(manager, "Monkey.png", false);

        // attach geometry to orthoNode
        guiNode.attachChild(p);
    }

}
