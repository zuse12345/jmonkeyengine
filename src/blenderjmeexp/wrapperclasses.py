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
from Blender.Mathutils import Quaternion
from jme.esmath import *

class JmeObject(object):
    __slots__ = ('wrappedObj', 'children', '__vpf')
    __QUAT_IDENTITY = ESQuaternion([0,0,0], 1)  # Do not modify value!

    def __init__(self, bObj):
        """Assumes input Blender Object already validated.
        I.e., is of supported type, and facing method is supported."""
        object.__init__(self)
        self.wrappedObj = bObj
        self.children = None
        print "Instantiated JmeObject '" + self.getName() + "'"
        self.__vpf = JmeObject.__vertsPerFace(bObj.getData(False, True).faces)
        # TODO:  Generate vertex color array by looking up each vertex's
        # color in the face's face.col[vertIndex].
        # What to do if some vertexes have color (by associated with a face
        # vertex color) but other vertexes in the same mesh do not?...
        # I'm thinking to save them as white and at least print a warning
        # to stdout.  Looks like existing JME XML parser requires color
        # coordinates for all vertexes or none.

    def addChild(self, child):
        if self.children == None: self.children = []
        self.children.append(child)

    def getName(self):
        return self.wrappedObj.name

    def getXmlEl(self):
        tag = XmlTag('com.jme.scene.Node', {'name':self.getName()})
        # TODO:  This is where all of the attributes and children should be
        # added to the XML.
        mesh = self.wrappedObj.getData(False, True)
        if mesh != None:
            childrenTag = XmlTag("children", {"size":1})
            childrenTag.addChild(JmeObject.__genMeshEl( \
                    mesh, self.__vpf, self.wrappedObj.color))
            tag.addChild(childrenTag)

        if self.children != None:
            for child in self.children: tag.addChild(child.getXmlEl())
        # Use either loc + rot + size OR matrixLocal
        # Set local variables just to reduce typing in this block.
        loc = self.wrappedObj.loc
        # N.b. Blender.Mathutils.Quaternines ARE NOT COMPATIBLE WITH jME!
        rQuat = ESQuaternion(self.wrappedObj.rot)
        if rQuat == JmeObject.__QUAT_IDENTITY: rQuat = None
        size = self.wrappedObj.size
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
        if size != None and (size[0] != 1. or size[1] != 1. or size[2] != 1.):
            locTag = XmlTag("localScale")
            locTag.addAttr("x", size[0], 7)
            locTag.addAttr("y", size[1], 7)
            locTag.addAttr("z", size[2], 7)
            tag.addChild(locTag)
        return tag

    def getType(self):
        return self.wrappedObj.type

    def __vertsPerFace(faces):
        """Returns 0 if type not supported; non-0 if supported.
           2 if will require face niggling; 3 if faceless"""
        if faces == None: return None
        facings = set()
        for f in faces:
            if f.verts == None: raise Exception("Face with no vertexes?")
            facings.add(len(f.verts))
        return facings

    def supported(bObj):
        """Returns 0 if type not supported; non-0 if supported.
           2 if will require face niggling; 3 if faceless"""
        if bObj.type not in ['Mesh']: return 0
        if not bObj.getData(False, True):
            raise Exception("Mesh Object has no data member?")
        vpf = JmeObject.__vertsPerFace(bObj.getData(False, True).faces)
        if vpf == None:
            print "FYI:  Accepting object '" + bObj.name + "' with no faces"
            return 3
        if vpf == set([0]):
            print "FYI:  Accepting object '" + bObj.name + "' with 0 vert faces"
            return 3
        if vpf == set([3,4]) or vpf == set([0,3,4]):
            print "FYI:  Object '" + bObj.name \
                    + "' accepted but will require unification to Trimeshes"
            return 2
        if vpf == set([3,0]) or vpf == set([0,4]) \
                or vpf == set([3]) or vpf == set([4]): return 1
        print "FYI:  Refusing object '" + bObj.name \
                + "' because unsupported vertexes-per-face: " + str(vpf)
        return 0

    def __str__(self):
        return '[' + self.getName() + ']'

    def __repr__(self):
        return "<JmeObject> " + self.__str__()

    def __genMeshEl(meshObj, vpf, color):
        if meshObj.verts == None:
            raise Exception("Mesh '" + meshObj.name + "' has no vertexes")
        unify = 3 in vpf and 4 in vpf
        if 4 in vpf and not unify:
            meshType = 'Quad'
        else:
            meshType = 'Tri'
        # TODO:  When iterate through verts to get vert vectors + normals,
        #        do check for null normal and throw if so:
        #   raise Exception("Mesh '" \
        #       + meshObj.name + "' has a vector with no normal")
        #   This is a Blender convention, not a 3D or JME convention
        #   (requirement for normals).
        tag = XmlTag('com.jme.scene.' + meshType + 'Mesh', {'name':meshObj.name})
        if color != None and \
            (color[0] != 1 or color[1] != 1 or color[2] != 1 or color[3] != 1):
            colorTag = XmlTag("defaultColor", \
                    {"class":"com.jme.renderer.ColorRGBA"})
            tag.addChild(colorTag)
            colorTag.addAttr("r", color[0])
            colorTag.addAttr("g", color[1])
            colorTag.addAttr("b", color[2])
            colorTag.addAttr("a", color[3])

        vcMap = None   # maps vertex INDEX to MCol
        colArray = None
        if meshObj.vertexColors != None:
            multiColorMapped = False
            vcMap = {}
            # TODO:  Test for objects with no faces, like curves, points.
            for face in meshObj.faces:
                if face.verts == None: continue
                if len(face.verts) != len(face.col):
                    raise Exception( \
                    "Counts of Face vertexes and vertex-colors do not match: " \
                        + str(len(face.verts)) + " vs. " + str(len(face.col)))
                for i in range(len(face.verts)):
                    if face.verts[i].index in vcMap: multiColorMapped = True
                    else: vcMap[face.verts[i].index] = face.col[i]
            if multiColorMapped:
                print "WARNING: Ignored some multi-mapped vertex coloring(s). "\
                        "Should average these"
            colArray = []

        coArray = []
        noArray = []
        nonFacedVertexes = 0
        for v in meshObj.verts:
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
            print "WARNING: " + str(nonFacedVertexes) \
                + " vertexes set to WHITE because no face to derive color from"
        vertTag = XmlTag("vertBuf", {"data":coArray}, 7)
        vertTag.addAttr("size", len(coArray))
        tag.addChild(vertTag)
        normTag = XmlTag("normBuf", {"data":noArray}, 7)
        normTag.addAttr("size", len(noArray))
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
            vertColTag = XmlTag("colorBuf", {"data":rgbaArray}, 3)
            vertColTag.addAttr("size", len(rgbaArray))
            tag.addChild(vertColTag)
        if 3 not in vpf and 4 not in vpf: return tag
        faceVertIndexes = []
        for face in meshObj.faces:
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
        indTag = XmlTag("indexBuffer", {"data":faceVertIndexes})
        indTag.addAttr("size", len(faceVertIndexes))
        tag.addChild(indTag)
        return tag

    supported = staticmethod(supported)
    __genMeshEl = staticmethod(__genMeshEl)
    __vertsPerFace = staticmethod(__vertsPerFace)

class JmeNode(object):
    __slots__ = ('name', 'children')

    def __init__(self, name):
        object.__init__(self)
        self.name = name
        self.children = None
        print "Instantiated JmeNode '" + self.getName() + "'"

    def addChild(self, child):
        if not self.children: self.children = []
        self.children.append(child)

    def getName(self):
        return self.name

    def getType(self):
        return self.wrappedObj.type

    def __str__(self):
        return '(' + self.getName() + ')'

    def __repr__(self):
        return "<JmeNode> " + self.__str__()

    def getXmlEl(self):
        tag = XmlTag('com.jme.scene.Node', {'name':self.getName()})
        # TODO:  This is where all of the attributes and children should be
        # added to the XML.
        if self.children != None:
            childrenTag = XmlTag('children', {'size':len(self.children)})
            tag.addChild(childrenTag)
            for child in self.children: childrenTag.addChild(child.getXmlEl())
        return tag
