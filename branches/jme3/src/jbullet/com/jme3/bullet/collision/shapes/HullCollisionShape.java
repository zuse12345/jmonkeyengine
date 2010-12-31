package com.jme3.bullet.collision.shapes;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.util.ObjectArrayList;
import com.jme3.bullet.util.Converter;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import java.io.IOException;

public class HullCollisionShape extends CollisionShape {

    private Mesh mesh;

    public HullCollisionShape() {
    }

    public HullCollisionShape(Mesh mesh) {
        this.mesh = mesh;
        createShape();
    }

    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(mesh, "hullMesh", null);
    }

    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule capsule = im.getCapsule(this);
        mesh = (Mesh)capsule.readSavable("hullMesh", null);
        createShape();
    }

    protected void createShape() {
        FloatBuffer vertices = mesh.getFloatBuffer(Type.Position);
        vertices.rewind();
        ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();
        int size = mesh.getVertexCount();
        for (int i = 0; i < size; i++) {
            points.add(new Vector3f(vertices.get(), vertices.get(), vertices.get()));

        }
        cShape = new ConvexHullShape(points);
        cShape.setLocalScaling(Converter.convert(getScale()));
        cShape.setMargin(margin);
    }
}
