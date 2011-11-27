/*
 * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this 
 * list of conditions and the following disclaimer. 
 * 
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution. 
 * 
 * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the 
 * names of its contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package jme.geometry.model;

/**
 * A Mesh is a group of related triangles in MS3D.
 *
 * @author naj
 * @version 0.1
 */
public class Mesh {

    /**
     * The name of the mesh in MS3D.
     */
    public String name;

    /**
     * The flags in MS3D.
     */
    public int flags;

    /**
     * The index into the array of materials in the model.  -1 indicates that
     * the mesh does not have a material assigned to it.
     */
    public int materialIndex;

    /**
     * The number of vertices in the mesh.
     */
    public int numberVertices;

    /**
     * The number of normal vectors in the mesh.
     */
    public int numberNormals;

    /**
     * The number of triangles in the mesh.
     */
    public int numberTriangles;

    /**
     * The vertices in the mesh.
     */
    public Vertex[] vertices;

    /**
     * The normals in the mesh.  Stores as an array of (x,y,z) arrays.
     */
    public float[][] normals;

    /**
     * The traingles in the mesh.
     */
    public Triangle[] triangles;

}