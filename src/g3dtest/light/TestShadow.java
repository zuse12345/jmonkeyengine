package g3dtest.light;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.renderer.RenderManager;
import com.g3d.renderer.Renderer;
import com.g3d.renderer.queue.RenderQueue.ShadowMode;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.dbg.WireFrustum;
import com.g3d.scene.shape.Box;
import com.g3d.shadow.BasicShadowRenderer;
import com.g3d.shadow.ShadowUtil;

public class TestShadow extends SimpleApplication {

    float angle;
    Spatial lightMdl;
    Spatial teapot;
    WireFrustum frustum;

    private BasicShadowRenderer bsr;

    public static void main(String[] args){
        TestShadow app = new TestShadow();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // put the camera in a bad position
        cam.setLocation(new Vector3f(0.7804813f, 1.7502685f, -2.1556435f));
        cam.setRotation(new Quaternion(0.1961598f, -0.7213164f, 0.2266092f, 0.6243975f));
        cam.setFrustumFar(50);

        Material mat = manager.loadMaterial("white_color.j3m");
        rootNode.setShadowMode(ShadowMode.CastAndRecieve);
        Box floor = new Box(Vector3f.ZERO, 3, 0.1f, 3);
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setMaterial(mat);
        floorGeom.setLocalTranslation(0,-0.2f,0);
        floorGeom.updateModelBound();
        rootNode.attachChild(floorGeom);

        teapot = manager.loadModel("teapot.obj");
        teapot.setLocalScale(2f);
        teapot.setMaterial(mat);
        rootNode.attachChild(teapot);
//        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
//        lightMdl.setMaterial(mat);
//        // disable shadowing for light representation
//        lightMdl.setShadowMode(ShadowMode.Off);
//        rootNode.attachChild(lightMdl);

        rootNode.updateGeometricState();
        bsr = new BasicShadowRenderer(manager, 1024);

        frustum = new WireFrustum(bsr.getPoints());
        Geometry g = new Geometry("f", frustum);
        g.setCullHint(Spatial.CullHint.Never);
        g.setShadowMode(ShadowMode.Off);
        g.setMaterial(new Material(manager, "wire_color.j3md"));
        g.getMaterial().setColor("m_Color", ColorRGBA.Red);
        rootNode.attachChild(g);
    }

    @Override
    public void simpleUpdate(float tpf){
        // rotate teapot around Y axis
        teapot.rotate(0, tpf * 0.25f, 0);
    }

    @Override
    public void simpleRender(RenderManager r){
//        bsr.postQueue(r);
//
//        Vector3f[] points = new Vector3f[8];
//        for (int i = 0; i < points.length; i++)
//            points[i] = new Vector3f();
//
//        ShadowUtil.updateFrustumPoints(bsr.getShadowCamera(), 1, 20, 1f, points);
//        frustum.update(points);
//        //renderer.clearBuffers(true,true,true);
//        bsr.postRender(r);
    }

}
