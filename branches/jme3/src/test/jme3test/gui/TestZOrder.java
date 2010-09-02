package jme3test.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.ui.Picture;

public class TestZOrder extends SimpleApplication {

    public static void main(String[] args){
        TestZOrder app = new TestZOrder();
        app.start();
    }

    public void simpleInitApp() {
        Picture p = new Picture("Picture1");
        p.move(0,0,-1);
        p.setPosition(100, 100);
        p.setWidth(100);
        p.setHeight(100);
        p.setImage(assetManager, "Interface/Logo/Monkey.png", false);
        guiNode.attachChild(p);

        Picture p2 = new Picture("Picture2");
        p2.move(0,0,1.001f);
        p2.setPosition(150, 150);
        p2.setWidth(100);
        p2.setHeight(100);
        p2.setImage(assetManager, "Interface/Logo/Monkey.png", false);
        guiNode.attachChild(p2);
    }

}
