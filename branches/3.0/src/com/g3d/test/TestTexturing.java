package com.g3d.test;

import com.g3d.math.Quaternion;
import com.g3d.math.Transform;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.scene.material.TextureMaterial;
import com.g3d.app.SimpleApplication;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture.WrapMode;
import com.g3d.util.TextureLoader;
import java.io.IOException;

public class TestTexturing extends SimpleApplication {

    private float time = 0;
    private Geometry plane;

    public static void main(String[] args){
        TestTexturing app = new TestTexturing();
        app.start();
    }

    public void simpleInitApp() {
        // create a simple plane/quad
        Mesh planeMesh = new Mesh();
        planeMesh.setBuffer(Type.Position, 3, new float[]{ -1f,  1f, 0f,
                                                            1f,  1f, 0f,
                                                            1f, -1f, 0f,
                                                           -1f, -1f, 0f});
        planeMesh.setBuffer(Type.TexCoord, 2, new float[]{ 0f, 0f,
                                                           2f, 0f,
                                                           2f, 2f,
                                                           0f, 2f });
        planeMesh.setBuffer(Type.Index, 3, new short[]{ 0, 1, 2,
                                                        2, 3, 0 });

        plane = new Geometry("Textured Plane", planeMesh);
        try {
            // test texture loading
            Texture tex = TextureLoader.loadTexture(TestTexturing.class.getResource("/com/g3d/test/data/Monkey.png"));

            // test mipmapping
            tex.setMinFilter(Texture.MinFilter.Trilinear);

            // test wrapping modes
            tex.setWrap(WrapMode.MirroredRepeat);

            // basic textured material
            plane.setMaterial(new TextureMaterial(tex));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        rootNode.attachChild(plane);

        // set up the camera
        Camera camera = getCamera();
        camera.setLocation(new Vector3f(0, 0, 7));
        camera.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    public void simpleUpdate(float tpf) {
        // does some rotation and moving in/out
        time += tpf * 5;
        Transform t = new Transform(new Vector3f(0f, 0f, (time % 15f) - 10f));
        Quaternion q = new Quaternion();
        q.fromAngleAxis(time / 10f, Vector3f.UNIT_Y);
        t.setRotationQuaternion(q);
        plane.setTransform(t);
    }

}
