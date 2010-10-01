package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.renderer.Caps;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Texture;

public class TestCartoonEdge extends SimpleApplication {

    private FilterPostProcessor fpp;

    public static void main(String[] args){
        TestCartoonEdge app = new TestCartoonEdge();
        app.start();
    }

    public void setupFilters(){
        if (renderer.getCaps().contains(Caps.GLSL100)){
            fpp=new FilterPostProcessor(assetManager);
            fpp.addFilter(new CartoonEdgeFilter());
            viewPort.addProcessor(fpp);
        }
    }

    public void makeToonish(Spatial spatial){
        if (spatial instanceof Node){
            Node n = (Node) spatial;
            for (Spatial child : n.getChildren())
                makeToonish(child);
        }else if (spatial instanceof Geometry){
            Geometry g = (Geometry) spatial;
            Material m = g.getMaterial();
            if (m.getMaterialDef().getName().equals("Phong Lighting")){
                Texture t = assetManager.loadTexture("Textures/ColorRamp/toon.png");
//                t.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
//                t.setMagFilter(Texture.MagFilter.Nearest);
                m.setTexture("m_ColorRamp", t);
                m.setBoolean("m_UseMaterialColors", true);
                m.setColor("m_Specular", ColorRGBA.Black);
                m.setColor("m_Diffuse", ColorRGBA.White);
                m.setBoolean("m_VertexLighting", true);
            }
        }
    }

    public void setupLighting(){
   
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-1, -1, 1).normalizeLocal());
        dl.setColor(new ColorRGBA(2,2,2,1));

        rootNode.addLight(dl);
    }

    public void setupModel(){
        Spatial model = assetManager.loadModel("Models/MonkeyHead/MonkeyHead.mesh.xml");
        makeToonish(model);
        model.rotate(0, FastMath.PI, 0);
//        signpost.setLocalTranslation(12, 3.5f, 30);
//        model.scale(0.10f);
//        signpost.setShadowMode(ShadowMode.CastAndReceive);
        rootNode.attachChild(model);
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.Gray);

        cam.setLocation(new Vector3f(-5.6310086f, 5.0892987f, -13.000479f));
        cam.setRotation(new Quaternion(0.1779095f, 0.20036356f, -0.03702727f, 0.96272093f));
        cam.update();

        cam.setFrustumFar(300);
        flyCam.setMoveSpeed(30);

        rootNode.setCullHint(CullHint.Never);

        setupLighting();
        setupModel();
        setupFilters();
    }

}
