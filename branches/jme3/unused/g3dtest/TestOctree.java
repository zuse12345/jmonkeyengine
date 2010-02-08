package g3dtest;

import com.g3d.app.SimpleApplication;
import com.g3d.bounding.DebugOctree;
import com.g3d.bounding.Octree;
import com.g3d.control.SpatialMotionControl;
import com.g3d.math.ColorRGBA;
import com.g3d.math.FastMath;
import com.g3d.math.Quaternion;
import com.g3d.math.Transform;
import com.g3d.math.Vector3f;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Node;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.scene.shape.PQTorus;
import com.g3d.scene.shape.Sphere;
import com.g3d.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestOctree extends SimpleApplication {

	ColorRGBA[] colorSpread = { ColorRGBA.White, ColorRGBA.Green,
			ColorRGBA.Gray };

	ColorRGBA[] colorSpread2 = { ColorRGBA.Blue, ColorRGBA.Red,
			ColorRGBA.Yellow };

	Geometry s, r;

	Node n, m, t;

	int count = 0;

	List<Octree> octreeList = new ArrayList<Octree>();

	Map<Octree, Set<Integer>> storedTriangles = new HashMap<Octree, Set<Integer>>();

	public static void main(String[] args){
        TestOctree app = new TestOctree();
        app.start();
    }

    @Override
    public void simpleInitApp() {
		OctreeDebugger.initialize(manager, rootNode);

		Sphere sphere = new Sphere(10, 10, 1);
        s = new Geometry("sphere", sphere);
		s.updateModelBound();

		n = new Node("sphere node");

		Mesh torus = new PQTorus(5, 4, 2f, .5f, 128, 16);
        r = new Geometry("tort", torus);
		r.setLocalTranslation(new Vector3f(1, 0, 0));
		r.setLocalRotation(new Quaternion().fromAngleNormalAxis(FastMath.PI*0.25f, new Vector3f(0,1f,0)));
		r.setLocalScale(0.9f);
		r.updateModelBound();

		m = new Node("tort node");

        SpatialMotionControl control = new SpatialMotionControl(m);
        control.setRepeat(true);
        control.insertKeyFrame(0, new Transform(new Vector3f(10, 10, 0)));
		control.insertKeyFrame(4, new Transform(new Vector3f(-10, -10, 0)));
//		r.addController(st);

        r.getMesh().setBuffer(Type.Color, 4, new float[0]);
        FloatBuffer color1 = (FloatBuffer) r.getMesh().getBuffer(Type.Color).getData();
        color1.clear();
        for (int i = 0, bLength = color1.capacity(); i < bLength; i+=4) {
            ColorRGBA c = colorSpread[i % 3];
            color1.put(c.r).put(c.g).put(c.b).put(c.a);
        }
        color1.flip();
        //Octree octree = new Octree(r, j,100); //Uncomment if you want to see without the debugger helpers
        Octree octree = new DebugOctree(r, 100);
        octree.build();
        octreeList.add(octree);


        FloatBuffer color2 = (FloatBuffer) s.getMesh().getBuffer(Type.Color).getData();
        color2.clear();
        for (int i = 0, bLength = color2.capacity(); i < bLength; i+=4) {
            ColorRGBA c = colorSpread[i % 3];
            color2.put(c.r).put(c.g).put(c.b).put(c.a);
        }
        color2.flip();
		

		n.attachChild(r);
		m.attachChild(s);

		rootNode.attachChild(n);
		rootNode.attachChild(m);

		t = new Node();
		rootNode.attachChild(t);
	}

    @Override
	public void simpleUpdate(float tpf) {
		count++;
		if (count < 5)
			return;
		count = 0;

		int[] indexBuffer = new int[3];

		int triCount = 0;

		for (Octree octree : octreeList) {
			Set<Integer> oldData = storedTriangles.get(octree);
			if (oldData != null) {
				for (Integer triIndex : oldData) {
					octree.getMesh().getTriangle(triIndex, indexBuffer);
	                FloatBuffer color2 = (FloatBuffer) octree.getMesh().getBuffer(Type.Color).getData();
					BufferUtils.setInBuffer(colorSpread[indexBuffer[0] % 3], color2, indexBuffer[0]);
					BufferUtils.setInBuffer(colorSpread[indexBuffer[1] % 3], color2, indexBuffer[1]);
					BufferUtils.setInBuffer(colorSpread[indexBuffer[2] % 3], color2, indexBuffer[2]);
				}
			}

			storedTriangles.clear();
			t.detachAllChildren();

			Set<Integer> triList = octree.intersect(s.getWorldBound());
			if (triList.size() > 0) {
				triCount += triList.size();
				storedTriangles.put(octree, triList);
				for (Integer triIndex : triList) {
					octree.getMesh().getTriangle(triIndex, indexBuffer);
	                FloatBuffer color2 = (FloatBuffer) octree.getMesh().getBuffer(Type.Color).getData();
	                BufferUtils.setInBuffer(ColorRGBA.Blue, color2, indexBuffer[0]);
	                BufferUtils.setInBuffer(ColorRGBA.Blue, color2, indexBuffer[1]);
	                BufferUtils.setInBuffer(ColorRGBA.Blue, color2, indexBuffer[2]);
				}
			}
		}

        context.setTitle("Triangles: "+triCount);
	}
}