package jme3test.export;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMaterialList;
import com.jme3.scene.plugins.ogre.OgreMeshKey;

public class TestOgreConvert extends SimpleApplication {

    public static void main(String[] args){
        TestOgreConvert app = new TestOgreConvert();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        OgreMaterialList materials = (OgreMaterialList) manager.loadAsset(new AssetKey("OTO.material"));
        Spatial ogreModel = (Spatial) manager.loadAsset(new OgreMeshKey("OTO.meshxml", materials));
//        ogreModel.setLocalScale(0.1f);

        ogreModel = ogreModel.clone();

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(0,-1,-1).normalizeLocal());
        System.out.println(dl.getDirection());
        rootNode.addLight(dl);

//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            BinaryExporter exp = new BinaryExporter();
//            exp.save(ogreModel, baos);
//
//            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//            BinaryImporter imp = new BinaryImporter(manager);
//            Model ogreModelReloaded = (Model) imp.load(bais, null, null);
//            ogreModelReloaded.setAnimation("push");

//            FileOutputStream fos = new FileOutputStream("C:\\mymodel.j3o");
//            BinaryExporter exp = new BinaryExporter();
//            exp.save(ogreModel, fos);
//            fos.close();
//
//            FileInputStream fis = new FileInputStream("C:\\mymodel.j3o");
//            BinaryImporter imp = new BinaryImporter();
//            imp.setAssetManager(manager);
//            Model ogreModelReloaded = (Model) imp.load(fis, null, null);

        AnimControl control = ogreModel.getControl(AnimControl.class);
        AnimChannel chan = control.createChannel();
        chan.setAnim("push");
//            fis.close();

            rootNode.attachChild(ogreModel);
//        } catch (IOException ex){
//            ex.printStackTrace();
//        }
    }
}
