package g3dtest.gui;

import com.g3d.app.SimpleApplication;
import com.g3d.font.BitmapFont;
import com.g3d.font.BitmapText;
import com.g3d.font.Rectangle;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial.CullHint;

public class TestBitmapFont extends SimpleApplication {

    private String txtB =
    "ABCDEFGHIKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()-=_+[]\\;',./{}|:<>?";

    public static void main(String[] args){
        TestBitmapFont app = new TestBitmapFont();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        BitmapFont fnt = manager.loadFont("angelFont.fnt");
//        BitmapText txt = new BitmapText(fnt, false);
//        txt.setBox(new Rectangle(0, 0, settings.getWidth(), settings.getHeight()));
//        txt.setSize(64);
//        txt.setText(txtB);
//        txt.assemble();

        BitmapText txt4 = new BitmapText(fnt, false);
        txt4.setSize(32);
        txt4.setText("Text without restriction. Text without restriction. Text without restriction. Text without restriction");
        txt4.setLocalTranslation(40, txt4.getLineHeight() * 2, 0);

        guiNode.attachChild(txt4);
    }

}
