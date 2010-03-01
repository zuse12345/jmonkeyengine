package g3dtest.android;

import com.g3d.app.SimpleApplication;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial;
import com.g3d.scene.Spatial.CullHint;

public class TestSceneLoading extends SimpleApplication {

    private void setState(Spatial s){
        s.setCullHint(CullHint.Never);
        if (s instanceof Node){
            Node n = (Node) s;
            for (int i = 0; i < n.getQuantity(); i++){
                Spatial s2 = n.getChild(i);
                setState(s2);
            }
        }
    }

    public void simpleInitApp() {
        Spatial scene = manager.loadModel("FINAL_LEVEL2.j3o");
//        setState(scene);
        rootNode.attachChild(scene);

        cam.setLocation(new Vector3f(-18.059685f, 34.64228f, 4.5048084f));
        cam.setRotation(new Quaternion(0.22396432f, 0.5235024f, -0.1448922f, 0.8091919f));
        cam.update();
    }

}
