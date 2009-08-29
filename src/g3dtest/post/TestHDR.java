package g3dtest.post;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.shape.Box;
import com.g3d.post.HDRRenderer;
import com.g3d.texture.Image.Format;

public class TestHDR extends SimpleApplication {

    private Node fbNode = new Node("Framebuffer Node");
    private HDRRenderer hdrRender;
    private float lastTpf;
    
    public static void main(String[] args){
        TestHDR app = new TestHDR();
//        AppSettings settings = new AppSettings(AppSettings.Template.Default640x480);
//        settings.setSamples(8);
//        app.setSettings(settings);
        app.start();
    }

    public Geometry createHDRBox(){
        Box boxMesh = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry box = new Geometry("Box", boxMesh);
        box.updateModelBound();
        boxMesh.setStatic();
        Material mat = manager.loadMaterial("hdr_pic.j3m");
        box.setMaterial(mat);
        return box;
    }

//    private Material disp;
    
    @Override
    public void simpleInitApp() {
        hdrRender = new HDRRenderer(manager);

        hdrRender.setBufferFormat(Format.RGB111110F);
        hdrRender.setMaxIterations(20);
        hdrRender.setExposure(0.87f);
        hdrRender.setThrottle(0.33f);

        hdrRender.loadInitial();
        hdrRender.load(renderer, settings.getWidth(), settings.getHeight(), 8);
        

//        config.setVisible(true);

        fbNode.attachChild(createHDRBox());
    }

//    public void displayAvg(Renderer r){
//        r.setFrameBuffer(null);
//        disp = prepare(-1, -1, settings.getWidth(), settings.getHeight(), 3, -1, scene64, disp);
//        r.clearBuffers(true, true, true);
//        r.renderGeometry(pic);
//    }

    @Override
    public void simpleUpdate(float tpf){
        fbNode.updateGeometricState(tpf, true);
        lastTpf = tpf;
    }

    @Override
    public void simpleRender(Renderer r){
        hdrRender.update(lastTpf, this, fbNode);
    }

}
