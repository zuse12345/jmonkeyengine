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

from jme.xml import XmlTag as _XmlTag
import jme.esmath as _esmath
import Blender.Mathutils as _bmath

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


class JmeNode(object):
    __slots__ = ('wrappedObj', 'children',
            'name', 'autoRotate', 'retainTransform')
    IDENTITY_4x4 = _bmath.Matrix().resize4x4()
    BLENDER_TO_JME_ROTATION = _bmath.RotationMatrix(-90, 4, 'x')

    def __init__(self, bObjOrName):
        """Assumes input Blender Object already validated, like by using the
        supported() static method below.
        I.e., is of supported type, and facing method is supported."""
        object.__init__(self)
        self.children = None
        self.wrappedObj = None
        self.autoRotate = False
        self.retainTransform = None

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
        tag = _XmlTag('com.jme.scene.Node', {'name':self.getName()})

        if self.wrappedObj != None:
            matrix = _bmath.Matrix(self.wrappedObj.matrixLocal)
        # Do all work with a copy.  We don't midify user data.
        if self.autoRotate:
            if matrix == None:
                raise Exception("Internal error.  Only our grouper node "
                        + "should have no wrapped Blender Object")
            # Need to transform the entire matrixLocal.
            matrix = matrix * JmeNode.BLENDER_TO_JME_ROTATION

        meshBakeTransform = None
        if self.wrappedObj != None:
            if matrix != JmeNode.IDENTITY_4x4:
                # BAKE IN ANY NODE ROTATION TRANSFORMATION
                # Make 4x4 matrix out of JUST rotation portion of matrixLocal
                meshBakeTransform = matrix.rotationPart().resize4x4()
                # Wipe the rotation portion of matrixLocal.
                # Unfortunately, this competely hoses the translation row,
                # so save off the properly rotated translation vector:
                translationRow = matrix[3]
                # This is because the inversion hoses the translation part
                derot = matrix * _bmath.Matrix(meshBakeTransform).invert()
                matrix = _bmath.Matrix(
                        derot[0], derot[1], derot[2], translationRow)

        if self.children != None:
            childrenTag = _XmlTag('children', {'size':len(self.children)})
            tag.addChild(childrenTag)
            for child in self.children:
                if isinstance(child, JmeMesh):
                    child.bakeTransform = meshBakeTransform
                else:
                    child.retainTransform = meshBakeTransform
                childrenTag.addChild(child.getXmlEl())

        if self.wrappedObj == None: return tag

        if self.retainTransform != None: matrix = matrix * self.retainTransform

        # Set local variables just to reduce typing in this block.
        loc = matrix.translationPart()
        # N.b. Blender.Mathutils.Quaternines ARE NOT COMPATIBLE WITH jME!
        e = matrix.toEuler()
        if round(e.x, 6) == 0 and round(e.y, 6) == 0 and round(e.z, 6) == 0:
            rQuat = None
        else:
            rQuat = _esmath.ESQuaternion(matrix.toEuler(), True)
        #else: rQuat = self.wrappedObj.rot.toQuat()
        scale = matrix.scalePart()
        # Need to add the attrs sequentially in order to preserve sequence
        # of the attrs in the output.
        if loc != None and (round(loc[0], 6) != 0. or round(loc[1], 6) != 0.
                or round(loc[2], 6) != 0.):
            locTag = _XmlTag("localTranslation")
            locTag.addAttr("x", loc[0], 6)
            locTag.addAttr("y", loc[1], 6)
            locTag.addAttr("z", loc[2], 6)
            tag.addChild(locTag)
        if rQuat != None:
            locTag = _XmlTag("localRotation")
            locTag.addAttr("x", rQuat.x, 6)
            locTag.addAttr("y", rQuat.y, 6)
            locTag.addAttr("z", rQuat.z, 6)
            locTag.addAttr("w", rQuat.w, 6)
            tag.addChild(locTag)
        if scale != None and (round(scale[0], 6) != 1.
                or round(scale[1], 6) != 1. or round(scale[2], 6) != 1.):
            locTag = _XmlTag("localScale")
            locTag.addAttr("x", scale[0], 6)
            locTag.addAttr("y", scale[1], 6)
            locTag.addAttr("z", scale[2], 6)
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
    __slots__ = ('wrappedMesh',
            '__vpf', 'defaultColor', 'name', 'bakeTransform')
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
        self.bakeTransform = None
        #print "Instantiated JmeMesh '" + self.getName() + "'"
        self.__vpf = JmeMesh.vertsPerFace(self.wrappedMesh.faces)

    def getName(self): return self.name

    def getXmlEl(self):
        if self.wrappedMesh.verts == None:
            raise Exception("Mesh '" + self.getName() + "' has no vertexes")
        mesh = self.wrappedMesh.copy()
        # This does do a deep copy! (like we need)
        if self.bakeTransform != None: mesh.transform(self.bakeTransform, True)
        # Note that the last param here doesn't calculate norms anew, it just
        # transforms them along with vertexes, which is just what we want.
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
        tag = _XmlTag('com.jme.scene.' + meshType + 'Mesh',
                {'name':self.getName()})
        if (self.defaultColor != None and
            (self.defaultColor[0] != 1 or self.defaultColor[1] != 1
                or self.defaultColor[2] != 1 or self.defaultColor[3] != 1)):
            colorTag = _XmlTag("defaultColor",
                    {"class":"com.jme.renderer.ColorRGBA"})
            tag.addChild(colorTag)
            colorTag.addAttr("r", self.defaultColor[0])
            colorTag.addAttr("g", self.defaultColor[1])
            colorTag.addAttr("b", self.defaultColor[2])
            colorTag.addAttr("a", self.defaultColor[3])

        vcMap = None   # maps vertex INDEX to MCol
        colArray = None
        if mesh.vertexColors:
            multiColorMapped = False
            vcMap = {}
            # TODO:  Test for objects with no faces, like curves, points.
            for face in mesh.faces:
                if face.verts == None: continue
                # The face.col reference in the next line WILL THROW
                # if the mesh does not support vert colors
                # (i.e. mesh.vertexColors).
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
        for v in mesh.verts:
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
        vertTag = _XmlTag("vertBuf", {"size":len(coArray)})
        vertTag.addAttr("data", coArray, 6, 3)
        tag.addChild(vertTag)
        normTag = _XmlTag("normBuf", {"size":len(noArray)})
        normTag.addAttr("data", noArray, 6, 3)
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
            vertColTag = _XmlTag("colorBuf", {"size":len(rgbaArray)})
            vertColTag.addAttr("data", rgbaArray, 3, 4)
            tag.addChild(vertColTag)
        if 3 not in self.__vpf and 4 not in self.__vpf: return tag
        faceVertIndexes = []
        for face in mesh.faces:
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
        indTag = _XmlTag("indexBuffer", {"size":len(faceVertIndexes)})
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

    __slots__ = ('__memberMap', '__memberKeys')
    # N.b. __memberMap does not have a member for each node, but a member
    #      for each saved Blender object.
    # __memberKey is just because Python has no ordered maps/dictionaries
    # We want nodes persisted in a well-defined sequence.

    def __init__(self):
        self.__memberMap = {}
        self.__memberKeys = []

    def addIfSupported(self, blenderObj):
        """Creates a JmeNode for the given Blender Object, if the Object is
        supported."""
        if JmeNode.supported(blenderObj):
            self.__memberMap[blenderObj] = JmeNode(blenderObj)
            self.__memberKeys.append(blenderObj)

    def __uniquifyNames(node, parentName, nameSet):
        # Would like to rename nodes ealier, to that messages (error and
        # otherwise) could reflect the new names, but we can't nest properly
        # until all nodes are added to the tree, and we want tree organization
        # to determine naming precedence.
        if node.name == None:
            raiseException("Node in tree without name!")
        #print "Checking name " + node.name + "..."
        if node.name in nameSet:
            #print "Renaming 2nd node with name " + node.name + "..."
            # TODO:  Make the checks for existing substrings case-insensitive
            if (isinstance(node, JmeMesh) and parentName != None
                    and (parentName + "Mesh") not in nameSet):
                node.name = (parentName + "Mesh")
            elif (isinstance(node, JmeMesh) and node.name.count("Mesh") < 1
                    and (node.name + "Mesh") not in nameSet):
                node.name += "Mesh"
            elif (isinstance(node, JmeNode) and node.name.count("Node") < 1
                    and (node.name + "Node") not in nameSet):
                node.name += "Node"
            else:
                node.name += ".uniqd"
                while node.name in nameSet: node.name += ".uniqd"
        nameSet.add(node.name)
        if (not isinstance(node, JmeNode)) or node.children == None: return
        for child in node.children:
            NodeTree.__uniquifyNames(child, node.name, nameSet)

    def nest(self):
        """addChild()s wherever the wrappedObj's parent is present; adds all
        remaining nodes to the top level; and enforces the tree has a single
        root by adding a top grouping node if necessary.
        Returns the root node."""

        if len(self.__memberKeys) < 1: return None
        for bo in self.__memberKeys:
            if bo.parent != None and bo.parent in self.__memberMap:
                self.__memberMap[bo.parent].addChild(self.__memberMap[bo])
                del self.__memberMap[bo]
        for key in self.__memberKeys[:]:
            if key not in self.__memberMap: self.__memberKeys.remove(key)
        if len(self.__memberKeys) < 1:
            raise Exception("Internal problem.  Tree ate itself.")
        if len(self.__memberKeys) < 2:
            root = self.__memberMap.popitem()[1]
            del self.__memberKeys[0]
        else:
            root = JmeNode("BlenderObjects")
            for key in self.__memberKeys: root.addChild(self.__memberMap[key])
        NodeTree.__uniquifyNames(root, None, set())
        return root

    __uniquifyNames = staticmethod(__uniquifyNames)
