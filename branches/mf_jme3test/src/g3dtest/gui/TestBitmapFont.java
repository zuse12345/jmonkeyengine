package g3dtest.gui;

import com.g3d.app.SimpleApplication;
import com.g3d.font.BitmapFont;
import com.g3d.font.BitmapText;
import com.g3d.font.Rectangle;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial.CullHint;

public class TestBitmapFont extends SimpleApplication {

    private Node orthoNode = new Node("Ortho Node");

    private String txtA = "אני מאוד ממליץ לך להשתמש DXT/S3TC על מרקם ואדיו זיכרון.";
    private String txtB =
    "This extension provides a mechanism to specify vertex attrib and "+
    "element array locations using GPU addresses. "+
    "Binding vertex buffers is one of the most frequent and expensive "+
    "operations in many GL applications, due to the cost of chasing "+
    "pointers and binding objects described in the Overview of "+
    "NV_shader_buffer_load. The intent of this extension is to enable a "+
    "way for the application to specify vertex attrib state that alleviates "+
    "the overhead of object binds and driver memory management.";

    public static void main(String[] args){
        TestBitmapFont app = new TestBitmapFont();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        manager.setProperty("FlipImages", "true");

        BitmapFont fnt = manager.loadFont("cooper.fnt");
        BitmapText txt = new BitmapText(fnt, false);
        txt.setBox(new Rectangle(0, 0, settings.getWidth(), settings.getHeight()));
        txt.setSize(32);
        txt.setText(txtB);
        txt.assemble();

        orthoNode.setLocalTranslation(0, settings.getHeight(), 0);
        orthoNode.setCullHint(CullHint.Never);
        orthoNode.attachChild(txt);
    }

    @Override
    public void simpleUpdate(float tpf){
        orthoNode.updateGeometricState(tpf, true);
    }

    @Override
    public void simpleRender(Renderer r){
        render(orthoNode, r);
        r.renderQueue();
    }

}
