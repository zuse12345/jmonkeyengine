package com.g3d.util;

import com.g3d.math.FastMath;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer.Type;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.g3d.util.BufferUtils.*;

/*
 * TODO:
 *  +add simplified tangent and complete tangent + binormal
 *  +add support for triangle strips and fans,
 *  +add support for rebuilding problematic meshes.
 */
public class TangentBinormalGenerator {

    private static final Logger log = Logger.getLogger(
			TangentBinormalGenerator.class.getName());

    private static float toleranceAngle;
    private static float toleranceDot;

    static {
        setToleranceAngle(45);
    }

    private static interface IndexWrapper {
        public int get(int i);
        public int size();
    }
    
    private static IndexWrapper getIndexWrapper(final Buffer buff) {
        if (buff instanceof ShortBuffer) {
            return new IndexWrapper() {
                private ShortBuffer buf = (ShortBuffer) buff;
                @Override public int get(int i) {
                    return ((int) buf.get(i))&(0x0000FFFF);
                }
                @Override public int size() {
                    return buf.capacity();
                }
            };
        }
        else if (buff instanceof IntBuffer) {
            return new IndexWrapper() {
                private IntBuffer buf = (IntBuffer) buff;
                @Override public int get(int i) {
                    return buf.get(i);
                }
                @Override public int size() {
                    return buf.capacity();
                }
            };
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static class VertexData {
        public final Vector3f tangent = new Vector3f();
        public final Vector3f binormal = new Vector3f();
        public final List<TriangleData> triangles =
                    new ArrayList<TriangleData>();

        public VertexData() {
            
        }
    }

    private static class TriangleData {
        public final Vector3f tangent;
        public final Vector3f binormal;
        public final Vector3f normal;
        public int index0;
        public int index1;
        public int index2;

        public TriangleData(Vector3f tangent, Vector3f binormal,
                        Vector3f normal,
                        int index0, int index1, int index2)
        {
            this.tangent = tangent;
            this.binormal = binormal;
            this.normal = normal;
            
            this.index0 = index0;
            this.index1 = index1;
            this.index2 = index2;
        }
    }

    private static VertexData[] initVertexData(int size) {
        VertexData[] vertices = new VertexData[size];
        for (int i = 0; i < size; i++) {
            vertices[i] = new VertexData();
        }
        return vertices;
    }

    public static void generate(Mesh mesh) {
        System.out.println("generating");
        int[] index = new int[3];
        Vector3f[] v = new Vector3f[3];
        Vector2f[] t = new Vector2f[3];
        for (int i = 0; i < 3; i++) {
            v[i] = new Vector3f();
            t[i] = new Vector2f();
        }

        VertexData[] vertices;
        switch (mesh.getMode()) {
            case Triangles:
                vertices = processTriangles(mesh, index, v, t); break;
            case TriangleStrip:
                vertices = processTriangleStrip(mesh, index, v, t); break;
            case TriangleFan:
                vertices = processTriangleFan(mesh, index, v, t); break;
            default: throw new UnsupportedOperationException(
                    mesh.getMode() + " is not supported.");
        }

        //compute average tangents

    }

    private static VertexData[] processTriangles(Mesh mesh,
            int[] index, Vector3f[] v, Vector2f[] t)
    {
        IndexWrapper indexBuffer =  getIndexWrapper(mesh.getBuffer(Type.Index).getData());
        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(Type.Position).getData();
        FloatBuffer textureBuffer = (FloatBuffer) mesh.getBuffer(Type.TexCoord).getData();

        VertexData[] vertices = initVertexData(vertexBuffer.capacity() / 3);

        for (int i = 0; i < indexBuffer.size() / 3; i++) {
            for (int j = 0; j < 3; j++) {
                index[j] = indexBuffer.get(i*3 + j);
                populateFromBuffer(v[j], vertexBuffer, index[j]);
                populateFromBuffer(t[j], textureBuffer, index[j]);
            }

            TriangleData triData = processTriangle(index, v, t);
            if (triData != null) {
                vertices[index[0]].triangles.add(triData);
                vertices[index[1]].triangles.add(triData);
                vertices[index[2]].triangles.add(triData);
            }
        }
        
        return vertices;
    }
    private static VertexData[] processTriangleStrip(Mesh mesh,
            int[] index, Vector3f[] v, Vector2f[] t)
    {
        return null;
    }
    private static VertexData[] processTriangleFan(Mesh mesh,
            int[] index, Vector3f[] v, Vector2f[] t)
    {
        return null;
    }

    private static TriangleData processTriangle(int[] index,
            Vector3f[] v, Vector2f[] t)
    {
        Vector3f edge1 = new Vector3f();
        Vector3f edge2 = new Vector3f();
        Vector2f edge1uv = new Vector2f();
        Vector2f edge2uv = new Vector2f();

        Vector3f tangent = new Vector3f();
        Vector3f binormal = new Vector3f();
        Vector3f normal = new Vector3f();

        t[1].subtract(t[0], edge1uv);
        t[2].subtract(t[0], edge2uv);
        float det = edge1uv.x*edge2uv.y - edge1uv.y*edge2uv.x;

        if (Math.abs(det) < FastMath.ZERO_TOLERANCE) {
            log.log(Level.WARNING, "Linearly dependent texture coordinates " +
                    "for triangle [{0}, {1}, {2}].",
                    new Object[]{ index[0], index[1], index[2] });
            return null;
        }

        v[1].subtract(v[0], edge1);
        v[2].subtract(v[0], edge2);

        tangent.set(edge1);
        tangent.normalizeLocal();
        binormal.set(edge2);
        binormal.normalizeLocal();

        if (Math.abs(Math.abs(tangent.dot(binormal)) - 1)
                        < FastMath.ZERO_TOLERANCE)
        {
            log.log(Level.WARNING, "Vertecies are on the same line " +
                    "for triangle [{0}, {1}, {2}].",
                    new Object[]{ index[0], index[1], index[2] });
            return null;
        }

        float factor = 1/det;
        tangent.x = (edge2uv.y*edge1.x - edge1uv.y*edge2.x)*factor;
        tangent.y = (edge2uv.y*edge1.y - edge1uv.y*edge2.y)*factor;
        tangent.z = (edge2uv.y*edge1.z - edge1uv.y*edge2.z)*factor;
        tangent.normalizeLocal();

        binormal.x = (edge1uv.x*edge2.x - edge2uv.x*edge1.x)*factor;
        binormal.y = (edge1uv.x*edge2.y - edge2uv.x*edge1.y)*factor;
        binormal.z = (edge1uv.x*edge2.z - edge2uv.x*edge1.z)*factor;
        binormal.normalizeLocal();

        tangent.cross(binormal, normal);

        return new TriangleData(
                        tangent,
                        binormal,
                        normal,
                        index[0], index[1], index[2]
                    );
    }

    public static void setToleranceAngle(float angle) {
        if (angle < 0 || angle > 179) {
            throw new IllegalArgumentException(
                        "The angle must be between 0 and 179 degrees.");
        }
        toleranceDot = FastMath.cos(angle*FastMath.DEG_TO_RAD);
        toleranceAngle = angle;
    }

    private void processTriangleData(Mesh mesh, VertexData[] vertices) {
        FloatBuffer normalBuffer = (FloatBuffer) mesh.getBuffer(Type.Normal).getData();

        FloatBuffer tangents = BufferUtils.createFloatBuffer(vertices.length * 3);
        FloatBuffer binormals = BufferUtils.createFloatBuffer(vertices.length * 3);

        Vector3f tangent = new Vector3f();
        Vector3f binormal = new Vector3f();
        Vector3f normal = new Vector3f();
        Vector3f givenNormal = new Vector3f();

        processVertex:
        for (int i = 0; i < vertices.length; i++) {

            populateFromBuffer(givenNormal, normalBuffer, i);
            VertexData currentVertex = vertices[i];
            List<TriangleData> triangles = currentVertex.triangles;

            if (triangles.size() == 0) {
                log.log(Level.WARNING, "No tangents found for vertex {0}.", i);
                continue;
            }

            // check if a vertex should be separated
            tangent.set(triangles.get(0).tangent);
            binormal.set(triangles.get(0).binormal);
            for (int j = 1; j < triangles.size(); j++) {
                TriangleData triangleData = triangles.get(j);

                if (tangent.dot(triangleData.tangent) < toleranceDot) {
                    log.log(Level.WARNING,
                            "Angle between tangents exceeds tolerance.");
                    break;
                }
                if (binormal.dot(triangleData.binormal) < toleranceDot) {
                    log.log(Level.WARNING,
                            "Angle between binormals exceeds tolerance.");
                    break;
                }
            }

            // find average tangent
            tangent.set(0, 0, 0);
            binormal.set(0, 0, 0);

            for (int j = 0; j < triangles.size(); j++) {
                TriangleData triangleData = triangles.get(j);
                tangent.addLocal(triangleData.tangent);
                binormal.addLocal(triangleData.binormal);

                if (givenNormal.dot(triangleData.normal) < 0) {
                    log.log(Level.WARNING,
                        "Generated normal is flipped for vertex {0}.", i);
                    continue processVertex;
                }
            }

            float tangentLength = tangent.length();
            if (tangentLength < FastMath.ZERO_TOLERANCE) {
                log.log(Level.WARNING,
                        "Shared tangent is zero for vertex {0}.", i);
                continue;
            }
            tangent.divideLocal(tangentLength);

            if (Math.abs(Math.abs(tangent.dot(givenNormal)) - 1)
                        < FastMath.ZERO_TOLERANCE)
            {
                log.log(Level.WARNING,
                        "Normal and tangent are parallel for vertex {0}.", i);
                continue;
            }

            float binormalLength = binormal.length();
            if (binormalLength < FastMath.ZERO_TOLERANCE) {
                log.log(Level.WARNING,
                        "Shared binormal is zero for vertex {0}.", i);
                continue;
            }
            binormal.divideLocal(binormalLength);

            if (Math.abs(Math.abs(binormal.dot(givenNormal)) - 1)
                        < FastMath.ZERO_TOLERANCE)
            {
                log.log(Level.WARNING,
                        "Normal and binormal are parallel for vertex {0}.", i);
                continue;
            }

            // FIXME: add simplified tangent and complete tangent + binormal
            givenNormal.cross(tangent, binormal);
            binormal.cross(givenNormal, tangent);
            tangent.normalizeLocal();

            // store the computed values
            setInBuffer(tangent, tangents, i);
            setInBuffer(binormal, binormals, i);
        }

        mesh.setBuffer(Type.Tangent,  3, tangents);
        mesh.setBuffer(Type.Binormal, 3, binormals);
    }
    
}
