package jme3test.export;

import com.jme3.animation.Model;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.plugins.ogre.OgreMaterialList;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestOgreConvert extends SimpleApplication {

    public static void main(String[] args){
        TestOgreConvert app = new TestOgreConvert();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        OgreMaterialList materials = (OgreMaterialList) manager.loadContent(new AssetKey("OTO.material"));
        Model ogreModel = (Model) manager.loadContent(new OgreMeshKey("OTO.meshxml", materials));
        ogreModel.setLocalScale(0.1f);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(-1,-1,-1).normalizeLocal());
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
            ogreModel.setAnimation("push");
//            fis.close();

            rootNode.attachChild(ogreModel);
//        } catch (IOException ex){
//            ex.printStackTrace();
//        }
    }
}
