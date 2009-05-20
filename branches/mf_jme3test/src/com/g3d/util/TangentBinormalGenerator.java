package com.g3d.util;

import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Type;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class TangentBinormalGenerator {

    public static void generate(Mesh mesh) {
        ShortBuffer indexBuffer = (ShortBuffer) mesh.getBuffer(Type.Index).getData();
        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(Type.Position).getData();
        FloatBuffer textureBuffer = (FloatBuffer) mesh.getBuffer(Type.TexCoord).getData();
        indexBuffer.rewind();

        FloatBuffer tangents = BufferUtils.createFloatBuffer(vertexBuffer.capacity() * 3);
        FloatBuffer binormals = BufferUtils.createFloatBuffer(vertexBuffer.capacity() * 3);

        Vector3f tangent = new Vector3f();
        Vector3f binormal = new Vector3f();
        Vector3f normal = new Vector3f();
        Vector3f verts[] = new Vector3f[3];
        Vector2f texcoords[] = new Vector2f[3];

        for (int i = 0; i < 3; i++) {
            verts[i] = new Vector3f();
            texcoords[i] = new Vector2f();
        }

        for (int t = 0; t < indexBuffer.capacity() / 3; t++) {
            int index[] = new int[3];
            for (int v = 0; v < 3; v++) {
                index[v] = indexBuffer.get();
                verts[v].x = vertexBuffer.get(index[v] * 3);
                verts[v].y = vertexBuffer.get(index[v] * 3 + 1);
                verts[v].z = vertexBuffer.get(index[v] * 3 + 2);

                texcoords[v].x = textureBuffer.get(index[v] * 2);
                texcoords[v].y = textureBuffer.get(index[v] * 2 + 1);
            }

            computeTriangleTangentSpace(tangent, binormal, normal, verts,
                    texcoords);

            for (int v = 0; v < 3; v++) {
                tangents.position(index[v] * 3);
                tangents.put(tangent.x);
                tangents.put(tangent.y);
                tangents.put(tangent.z);

                binormals.position(index[v] * 3);
                binormals.put(binormal.x);
                binormals.put(binormal.y);
                binormals.put(binormal.z);
            }
        }

        mesh.setBuffer(Type.Tangent,  3, tangents);
        mesh.setBuffer(Type.Binormal, 3, binormals);
    }

    private static void computeTriangleTangentSpace(Vector3f tangent,
            Vector3f binormal, Vector3f normal, Vector3f v[], Vector2f t[]) {
        Vector3f edge1 = v[1].subtract(v[0]);
        Vector3f edge2 = v[2].subtract(v[0]);
        Vector2f edge1uv = t[1].subtract(t[0]);
        Vector2f edge2uv = t[2].subtract(t[0]);

        float cp = edge1uv.y * edge2uv.x - edge1uv.x * edge2uv.y;

        if (cp != 0.0f) {
            float mul = 1.0f / cp;
            tangent.set((edge1.mult(-edge2uv.y).add(edge2.mult(edge1uv.y)))
                    .mult(mul));
            binormal.set((edge1.mult(-edge2uv.x).add(edge2.mult(edge1uv.x)))
                    .mult(mul));
            tangent.normalizeLocal();
            binormal.normalizeLocal();
        }
        edge1.cross(edge2, normal);
        normal.normalizeLocal();
    }
}
