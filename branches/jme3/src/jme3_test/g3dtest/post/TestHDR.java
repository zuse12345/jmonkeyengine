package g3dtest.post;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.shape.Box;
import com.g3d.post.HDRRenderer;
import com.g3d.texture.Image.Format;
import com.g3d.ui.Picture;

public class TestHDR extends SimpleApplication {

    private HDRRenderer hdrRender;
    private Picture dispQuad;

    public static void main(String[] args){
        TestHDR app = new TestHDR();
        app.start();
    }

    public Geometry createHDRBox(){
        Box boxMesh = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry box = new Geometry("Box", boxMesh);
        box.updateModelBound();
        boxMesh.setStatic();
        Material mat = (Material) manager.loadContent("hdr_pic.j3m");
        box.setMaterial(mat);
        return box;
    }

//    private Material disp;
    
    @Override
    public void simpleInitApp() {
        hdrRender = new HDRRenderer(manager, renderer);
        hdrRender.setSamples(0);
        hdrRender.setMaxIterations(20);
        hdrRender.setExposure(0.87f);
        hdrRender.setThrottle(0.33f);

        viewPort.addProcessor(hdrRender);
        
//        config.setVisible(true);

        rootNode.attachChild(createHDRBox());
    }

    public void simpleUpdate(float tpf){
        if (hdrRender.isInitialized() && dispQuad == null){
            dispQuad = hdrRender.createDisplayQuad();
            dispQuad.setWidth(128);
            dispQuad.setHeight(128);
            dispQuad.setPosition(4, 30);
            guiNode.attachChild(dispQuad);
        }
    }

//    public void displayAvg(Renderer r){
//        r.setFrameBuffer(null);
//        disp = prepare(-1, -1, settings.getWidth(), settings.getHeight(), 3, -1, scene64, disp);
//        r.clearBuffers(true, true, true);
//        r.renderGeometry(pic);
//    }

}
