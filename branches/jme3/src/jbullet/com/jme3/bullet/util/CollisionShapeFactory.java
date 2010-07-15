/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.bullet.util;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author normenhansen, tim8dev
 */
public class CollisionShapeFactory {

    private static CompoundCollisionShape createCompoundShape(
            Node rootNode, CompoundCollisionShape shape, boolean meshAccurate, boolean dynamic) {
        for (Spatial spatial : rootNode.getChildren()) {
            if (spatial instanceof Node) {
                createCompoundShape((Node) spatial, shape, meshAccurate, dynamic);
            } else if (spatial instanceof Geometry) {
                if (meshAccurate) {
                    CollisionShape childShape = dynamic
                            ? createSingleDynamicMeshShape((Geometry) spatial)
                            : createSingleMeshShape((Geometry) spatial);
                    if (childShape != null) {
                        shape.addChildShape(childShape,
                                spatial.getWorldTranslation(),
                                spatial.getWorldRotation().toRotationMatrix());
                    }
                } else {
                    shape.addChildShape(createSingleBoxShape(spatial),
                            spatial.getWorldTranslation(),
                            spatial.getWorldRotation().toRotationMatrix());
                }
            }
        }
        return shape;
    }

    public static CompoundCollisionShape createCompoundShape(
            Node rootNode, CompoundCollisionShape shape, boolean meshAccurate) {
        rootNode.updateGeometricState();
        return createCompoundShape(rootNode, shape, meshAccurate, false);
    }

    public static CompoundCollisionShape createMeshCompoundShape(Node rootNode) {
        rootNode.updateGeometricState();
        return createCompoundShape(rootNode, new CompoundCollisionShape(), true);
    }

    public static CompoundCollisionShape createBoxCompoundShape(Node rootNode) {
        rootNode.updateGeometricState();
        return createCompoundShape(rootNode, new CompoundCollisionShape(), false);
    }

    public static CollisionShape createMeshShape(Spatial spatial) {
        if (spatial instanceof Geometry) {
            return createSingleMeshShape((Geometry) spatial);
        } else if (spatial instanceof Node) {
            return createMeshCompoundShape((Node) spatial);
        } else {
            throw new IllegalArgumentException("Supplied spatial must either be Node or Geometry!");
        }
    }

    /**
     * Note that mesh-sccurate dynamic shapes are very expensive and you should consider
     * using a compound shape of standard shapes instead.
     */
    public static CollisionShape createDynamicMeshShape(Spatial spatial) {
        if (spatial instanceof Geometry) {
            return createSingleDynamicMeshShape((Geometry) spatial);
        } else if (spatial instanceof Node) {
            spatial.updateGeometricState();
            return createCompoundShape((Node) spatial, new CompoundCollisionShape(), true, true);
        } else {
            throw new IllegalArgumentException("Supplied spatial must either be Node or Geometry!");
        }

    }

    public static CollisionShape createBoxShape(Spatial spatial) {
        if (spatial instanceof Geometry) {
            return createSingleBoxShape((Geometry) spatial);
        } else if (spatial instanceof Node) {
            return createBoxCompoundShape((Node) spatial);
        } else {
            throw new IllegalArgumentException("Supplied spatial must either be Node or Geometry!");
        }
    }

    public static MeshCollisionShape createSingleMeshShape(Geometry geom) {
        Mesh mesh = geom.getMesh();
        if (mesh != null) {
            MeshCollisionShape mColl = new MeshCollisionShape(mesh);
            mColl.setScale(geom.getWorldScale());
            return mColl;
        } else {
            return null;
        }
    }

    public static BoxCollisionShape createSingleBoxShape(Spatial spatial) {
        spatial.setModelBound(new BoundingBox());
        spatial.updateGeometricState();
        spatial.updateModelBound();
        BoxCollisionShape shape = new BoxCollisionShape(
                ((BoundingBox) spatial.getWorldBound()).getExtent(new Vector3f()));
        return shape;
    }

    /**
     * Note that mesh-sccurate dynamic shapes are very expensive and you should consider
     * using a compound shape of standard shapes instead.
     */
    public static GImpactCollisionShape createSingleDynamicMeshShape(Geometry geom) {
        Mesh mesh = geom.getMesh();
        if (mesh != null) {
            GImpactCollisionShape dynamicShape = new GImpactCollisionShape(mesh);
            dynamicShape.setScale(geom.getWorldScale());
            return dynamicShape;
        } else {
            return null;
        }
    }
}
