"""Wrapper classes for Blender native objects to facilitate conversion to
JME-usable format"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

# Copyright (c) 2009, Blaine Simpson and the jMonkeyEngine team
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of the <organization> nor the
#       names of its contributors may be used to endorse or promote products
#       derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY Blaine Simpson and the jMonkeyEngine team
# ''AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL Blaine Simpson or the jMonkeyEngine team
# BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from jme.xml import *
from jme.esmath import *
from Blender.Mathutils import *

# GENERAL DEVELOPMENT NOTES:
#
# When getting the mesh to work with from Blender, make sure to use
# ...getMesh(False, True), or the equivalent ...getMesh(mesh=True).
# This will get a Blender "wrapped" Mesh instance instead of a non-wrapped
# NMesh instance (contrary to the wrapping we are doing here, Blender's
# "wrapping" actually puts you closer to the physical data).
# The parent/child relationships between these wrapper classes reflects
# scene node relationships, not Blender's explicit parenting relationships.
# For example, Blender Objects aggregate Meshes and Mesh references, but do
# not consider them children; but in JME, nodes can have nodes and Meshes
# (or any other Spatials) as children.

BLENDER_TO_JME_ROTATION = RotationMatrix(-90, 4, 'x')


class JmeNode(object):
    __slots__ = ('wrappedObj', 'children', 'name', 'autoRotate')
    __QUAT_IDENTITY = ESQuaternion([0,0,0], 1)  # Do not modify value!

    def __init__(self, bObjOrName):
        """Assumes input Blender Object already validated, like by using the
        supported() static method below.
        I.e., is of supported type, and facing method is supported."""
        object.__init__(self)
        self.children = None
        self.wrappedObj = None
        self.autoRotate = False

        if isinstance(bObjOrName, basestring):
            self.name = bObjOrName
        else:
            self.wrappedObj = bObjOrName
            self.name = self.wrappedObj.name
            bMesh = self.wrappedObj.getData(False, True)
            if bMesh != None:
                self.addChild(JmeMesh(bMesh, self.wrappedObj.color))
        #print "Instantiated JmeNode '" + self.getName() + "'"

    def addChild(self, child):
        if self.children == None: self.children = []
        self.children.append(child)

    def getName(self): return self.name

    def getXmlEl(self):
        global BLENDER_TO_JME_ROTATION
         
        tag = XmlTag('com.jme.scene.Node', {'name':self.getName()})

        if self.children != None:
            childrenTag = XmlTag('children', {'size':len(self.children)})
            tag.addChild(childrenTag)
            for child in self.children: childrenTag.addChild(child.getXmlEl())
        if self.wrappedObj == None: return tag

        if self.autoRotate:
            # Need to transform the entire matrixLocal.
            matrix = self.wrappedObj.matrixLocal * BLENDER_TO_JME_ROTATION
        else:
            matrix = Matrix(self.wrappedObj.matrixLocal)
        # Use either loc + rot + size OR matrixLocal
        # Set local variables just to reduce typing in this block.
        loc = matrix.translationPart()
        # N.b. Blender.Mathutils.Quaternines ARE NOT COMPATIBLE WITH jME!
        e = matrix.toEuler()
        if e.x == 0 and e.y == 0 and e.z == 0: rQuat = None
        else: rQuat = ESQuaternion(matrix.toEuler(), True)
        scale = matrix.scalePart()
        # Need to add the attrs sequentially in order to preserve sequence
        # of the attrs in the output.
        if loc != None and (loc[0] != 0. or loc[1] != 0. or loc[2] != 0.):
            locTag = XmlTag("localTranslation")
            locTag.addAttr("x", loc[0], 7)
            locTag.addAttr("y", loc[1], 7)
            locTag.addAttr("z", loc[2], 7)
            tag.addChild(locTag)
        if rQuat != None:
            locTag = XmlTag("localRotation")
            locTag.addAttr("x", rQuat.x, 7)
            locTag.addAttr("y", rQuat.y, 7)
            locTag.addAttr("z", rQuat.z, 7)
            locTag.addAttr("w", rQuat.w, 7)
            tag.addChild(locTag)
        if scale != None and (scale[0] != 1.
                or scale[1] != 1. or scale[2] != 1.):
            locTag = XmlTag("localScale")
            locTag.addAttr("x", scale[0], 7)
            locTag.addAttr("y", scale[1], 7)
            locTag.addAttr("z", scale[2], 7)
            tag.addChild(locTag)
        return tag

    def getType(self):
        return self.wrappedObj.type

    def supported(bObj):
        """Returns 0 if type not supported; non-0 if supported.
           2 if will require face niggling; 3 if faceless"""
        if bObj.type not in ['Mesh']: return 0
        if not bObj.getData(False, True):
            raise Exception("Mesh Object has no data member?")
        vpf = JmeMesh.vertsPerFace(bObj.getData(False, True).faces)
        if vpf == None:
            print "FYI:  Accepting object '" + bObj.name + "' with no faces"
            return 3
        if vpf == set([0]):
            print "FYI:  Accepting object '" + bObj.name + "' with 0 vert faces"
            return 3
        if vpf == set([3,4]) or vpf == set([0,3,4]):
            print ("FYI:  Object '" + bObj.name
                    + "' accepted but will require unification to Trimeshes")
            return 2
        if (vpf == set([3,0]) or vpf == set([0,4])
                or vpf == set([3]) or vpf == set([4])): return 1
        print ("FYI:  Refusing object '" + bObj.name
                + "' because unsupported vertexes-per-face: " + str(vpf))
        return 0

    def __str__(self):
        return '[' + self.getName() + ']'

    def __repr__(self):
        return "<JmeNode> " + self.__str__()

    supported = staticmethod(supported)


class JmeMesh(object):
    __slots__ = ('wrappedMesh', '__vpf', 'defaultColor', 'name')
    # defaultColor corresponds to jME's Meshs' defaultColor.
    # In Blender this is a per-Object, not per-Mesh setting.
    # This is why it is a parameter of the constructor below.

    def __init__(self, bMesh, color=None):
        """Assumes input Blender Mesh already validated, like by using the
        vertsPerFace static method below.
        I.e., is of supported type, and facing method is supported.
        The color setting should come from the parent Blender Object's
        .color setting."""
        object.__init__(self)
        self.wrappedMesh = bMesh
        self.name = bMesh.name
        self.defaultColor = color
        #print "Instantiated JmeMesh '" + self.getName() + "'"
        self.__vpf = JmeMesh.vertsPerFace(self.wrappedMesh.faces)

    def getName(self): return self.name

    def getXmlEl(self):
        if self.wrappedMesh.verts == None:
            raise Exception("Mesh '" + self.getName() + "' has no vertexes")
        unify = 3 in self.__vpf and 4 in self.__vpf
        if 4 in self.__vpf and not unify:
            meshType = 'Quad'
        else:
            meshType = 'Tri'
        # TODO:  When iterate through verts to get vert vectors + normals,
        #        do check for None normal and throw if so:
        #   raise Exception("Mesh '"
        #       + self.wrappedMesh.name + "' has a vector with no normal")
        #   This is a Blender convention, not a 3D or JME convention
        #   (requirement for normals).
        tag = XmlTag('com.jme.scene.' + meshType + 'Mesh',
                {'name':self.getName()})
        if (self.defaultColor != None and
            (self.defaultColor[0] != 1 or self.defaultColor[1] != 1
                or self.defaultColor[2] != 1 or self.defaultColor[3] != 1)):
            colorTag = XmlTag("defaultColor",
                    {"class":"com.jme.renderer.ColorRGBA"})
            tag.addChild(colorTag)
            colorTag.addAttr("r", self.defaultColor[0])
            colorTag.addAttr("g", self.defaultColor[1])
            colorTag.addAttr("b", self.defaultColor[2])
            colorTag.addAttr("a", self.defaultColor[3])

        vcMap = None   # maps vertex INDEX to MCol
        colArray = None
        if self.wrappedMesh.vertexColors:
            multiColorMapped = False
            vcMap = {}
            # TODO:  Test for objects with no faces, like curves, points.
            for face in self.wrappedMesh.faces:
                if face.verts == None: continue
                # The face.col reference in the next line WILL THROW
                # if the mesh does not support vert colors
                # (i.e. self.wrappedMesh.vertexColors).
                if len(face.verts) != len(face.col):
                    raise Exception(
                    "Counts of Face vertexes and vertex-colors do not match: "
                        + str(len(face.verts)) + " vs. " + str(len(face.col)))
                for i in range(len(face.verts)):
                    if face.verts[i].index in vcMap: multiColorMapped = True
                    else: vcMap[face.verts[i].index] = face.col[i]
            if multiColorMapped:
                print ("WARNING: Ignored some multi-mapped vertex "
                    + "coloring(s). Should average these.")
            colArray = []

        coArray = []
        noArray = []
        nonFacedVertexes = 0
        for v in self.wrappedMesh.verts:
            coArray.append(v.co.x)
            coArray.append(v.co.y)
            coArray.append(v.co.z)
            noArray.append(v.no.x)
            noArray.append(v.no.y)
            noArray.append(v.no.z)
            if colArray != None:
                if v.index in vcMap:
                    colArray.append(vcMap[v.index])
                else:
                    nonFacedVertexes += 1
                    colArray.append(None)  # We signify WHITE by None
        if nonFacedVertexes > 0:
            print ("WARNING: " + str(nonFacedVertexes)
                + " vertexes set to WHITE because no face to derive color from")
        vertTag = XmlTag("vertBuf", {"size":len(coArray)})
        vertTag.addAttr("data", coArray, 7, 3)
        tag.addChild(vertTag)
        normTag = XmlTag("normBuf", {"size":len(noArray)})
        normTag.addAttr("data", noArray, 7, 3)
        tag.addChild(normTag)
        if colArray != None:
            rgbaArray = []
            for c in colArray:
                if c == None:
                    rgbaArray.append(1)
                    rgbaArray.append(1)
                    rgbaArray.append(1)
                    rgbaArray.append(1)
                else:
                    rgbaArray.append(c.r/255.)
                    rgbaArray.append(c.g/255.)
                    rgbaArray.append(c.b/255.)
                    rgbaArray.append(c.a/255.)
            vertColTag = XmlTag("colorBuf", {"size":len(rgbaArray)})
            vertColTag.addAttr("data", rgbaArray, 3, 4)
            tag.addChild(vertColTag)
        if 3 not in self.__vpf and 4 not in self.__vpf: return tag
        faceVertIndexes = []
        for face in self.wrappedMesh.faces:
            if face.verts == None or len(face.verts) < 1: continue
            if unify and len(face.verts) == 4:
                faceVertIndexes.append(face.verts[0].index)
                faceVertIndexes.append(face.verts[1].index)
                faceVertIndexes.append(face.verts[2].index)
                faceVertIndexes.append(face.verts[0].index)
                faceVertIndexes.append(face.verts[2].index)
                faceVertIndexes.append(face.verts[3].index)
            else:
                for v in face.verts: faceVertIndexes.append(v.index)
        indTag = XmlTag("indexBuffer", {"size":len(faceVertIndexes)})
        if len(face.verts) == 4 and not unify: outputVpf = 4
        else: outputVpf = 3
        indTag.addAttr("data", faceVertIndexes, None, outputVpf)
        tag.addChild(indTag)
        return tag

    def vertsPerFace(faces):
        """Returns 0 if type not supported; non-0 if supported.
           2 if will require face niggling; 3 if faceless"""
        if faces == None: return None
        facings = set()
        for f in faces:
            if f.verts == None: raise Exception("Face with no vertexes?")
            facings.add(len(f.verts))
        return facings

    vertsPerFace = staticmethod(vertsPerFace)


class NodeTree(object):
    """Trivial tree dedicated for JmeNode members.
    Add all members to the tree, then call nest().
    See method descriptions for details."""

    __slots__ = ('__memberMap')

    def __init__(self):
        self.__memberMap = {}

    def addIfSupported(self, blenderObj):
        """Creates a JmeNode for the given Blender Object, if the Object is
        supported."""
        if JmeNode.supported(blenderObj):
            self.__memberMap[blenderObj] = JmeNode(blenderObj)

    def nest(self):
        """addChild()s wherever the wrappedObj's parent is present; adds all
        remaining nodes to the top level; and enforces the tree has a single
        root by adding a top grouping node if necessary.
        Returns the root node."""

        if len(self.__memberMap) < 1: return None
        parented = []
        for bo, node in self.__memberMap.iteritems():
            if bo.parent != None and bo.parent in self.__memberMap:
                self.__memberMap[bo.parent].addChild(node)
                parented.append(bo)
        for bo in parented: del self.__memberMap[bo]
        if len(self.__memberMap) < 1:
            raise Exception("Internal problem.  Tree ate itself.")
        if len(self.__memberMap) < 2: return self.__memberMap.popitem()[1]
        root = JmeNode("BlenderObjects")
        for node in self.__memberMap.itervalues(): root.addChild(node)
        return root
