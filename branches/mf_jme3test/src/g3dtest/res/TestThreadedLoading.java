/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package g3dtest.res;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Quad;
import com.g3d.system.AppSettings;
import com.g3d.texture.Texture;
import java.util.concurrent.ExecutionException;

public class TestThreadedLoading extends SimpleApplication {

    public static void main(String[] args){
        TestThreadedLoading app = new TestThreadedLoading();
        app.setSettings(new AppSettings(AppSettings.Template.Default1024x768));

        try {
            long time = System.nanoTime();
            app.getContentManager().loadContents("debug_normals.j3md",
                                                 "plain_texture.j3md",
                                                 "red_color.j3m",
                                                 "solid_color.j3md",
                                                 "teapot.obj",
                                                 "phong.vert",
                                                 "Monkey.DDS",
                                                 "Monkey.jpg",
                                                 "Monkey.png",
                                                 "heightmap.png",
                                                 "nave.hdr");
            System.out.println( (System.nanoTime() - time) );
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        app.start();
    }

    @Override
    public void simpleInitApp() {
        Quad quadMesh = new Quad(1, 1);
        quadMesh.updateGeometry(1, 1, true);

        Geometry quad = new Geometry("Textured Quad", quadMesh);
        quad.updateModelBound();

        Texture tex = manager.loadTexture("nave.hdr");
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        tex.setAnisotropicFilter(32);
        
        Material mat = new Material(manager, "plain_texture.j3md");
        mat.setTexture("m_ColorMap", tex);
        quad.setMaterial(mat);

        float aspect = tex.getImage().getWidth() / (float) tex.getImage().getHeight();
        quad.setLocalScale(new Vector3f(aspect, 1, 1));

        rootNode.attachChild(quad);
    }

}
