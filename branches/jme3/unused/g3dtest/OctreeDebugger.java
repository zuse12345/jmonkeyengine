package g3dtest;

import com.g3d.bounding.BoundingBox;
import com.g3d.bounding.DebugOctree;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.res.ContentManager;
import com.g3d.scene.Geometry;
import com.g3d.scene.Node;
import com.g3d.scene.Spatial;
import com.g3d.scene.Spatial.CullHint;
import com.g3d.scene.shape.Box;

/**
 * <code>OctreeDebugger</code> provides an singleton to make Debug Helpers.
 *
 * @author Lucas Goraieb
 * @version $Id: Octree.java,v 0.5 2007/04/27 20:33:02 nca Exp $
 */
public class OctreeDebugger {

	private static OctreeDebugger instance;

	public static OctreeDebugger getInstance() {
		if (instance == null) {
			instance = new OctreeDebugger();
		}
		return instance;
	}

	public static void initialize(ContentManager manager, Node rootNode) {
		OctreeDebugger o = getInstance();
		o.manager = manager;
		o.rootNode = rootNode;
		o.debugNode = new Node();
		o.rootNode.attachChild(o.debugNode);
	}

    private ContentManager manager;

	private Node rootNode;

	private Node debugNode;

	public Geometry addBox(BoundingBox bb) {
		Box box = new Box(bb.getCenter(),
                          bb.getXExtent(),
                          bb.getYExtent(),
                          bb.getZExtent());

        // set wireframe on box
        Geometry boxGeom = new Geometry("Box", box);
        Material mat = new Material(manager, "wire_color.j3md");
        mat.setColor("m_Color", ColorRGBA.White);
        debugNode.attachChild(boxGeom);

        return boxGeom;
	}

	public void hit(DebugOctree node) {
		if (node.getTriangleData() == null) {
            Geometry g = node.getDebugBox();
            Material mat = g.getMaterial();
            mat.setColor("m_Color", ColorRGBA.White);
		} else {
			Geometry g = node.getDebugBox();
            Material mat = g.getMaterial();
            mat.setColor("m_Color", ColorRGBA.Red);
		}
		node.getDebugBox().setCullHint(CullHint.Never);
	}

	public void reset() {
		for (Spatial c : debugNode.getChildren()) {
			if (c instanceof Geometry) {
				((Geometry) c).setCullHint(CullHint.Always);
			}
		}
	}

}
