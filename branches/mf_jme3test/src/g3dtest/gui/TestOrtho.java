package g3dtest.gui;

import com.g3d.app.SimpleApplication;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial.CullHint;
import com.g3d.ui.Picture;

public class TestOrtho extends SimpleApplication {

    private Node orthoNode = new Node("Ortho Node");

    public static void main(String[] args){
        TestOrtho app = new TestOrtho();
        app.start();
    }

    public void simpleInitApp() {
        orthoNode.setCullHint(CullHint.Never);

        Picture p = new Picture("Picture");
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(manager, "Monkey.png", false);

        // attach geometry to orthoNode
        orthoNode.attachChild(p);
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
