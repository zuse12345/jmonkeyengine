package g3dtest.export;

import com.g3d.animation.Model;
import com.g3d.app.SimpleApplication;
import com.g3d.export.binary.BinaryExporter;
import com.g3d.export.binary.BinaryImporter;
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
        Model ogreModel = (Model) manager.loadOgreModel("OTO.meshxml", "OTO.material");
        ogreModel.setLocalScale(0.1f);

        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            BinaryExporter exp = new BinaryExporter();
//            exp.save(ogreModel, baos);
//
//            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//            BinaryImporter imp = new BinaryImporter(manager);
//            Model ogreModelReloaded = (Model) imp.load(bais, null, null);
//            ogreModelReloaded.setAnimation("push");

            FileOutputStream fos = new FileOutputStream("C:\\mymodel.j3o");
            BinaryExporter exp = new BinaryExporter();
            exp.save(ogreModel, fos);
            fos.close();

            FileInputStream fis = new FileInputStream("C:\\mymodel.j3o");
            BinaryImporter imp = new BinaryImporter(manager);
            Model ogreModelReloaded = (Model) imp.load(fis, null, null);
            ogreModelReloaded.setAnimation("push");
            fis.close();

            rootNode.attachChild(ogreModelReloaded);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
