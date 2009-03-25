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

class JmeObject(object):
    __slots__ = ('wrappedObj', 'children', '__vpf')
    __QUAT_IDENTITY = Quaternion()  # Do not modify value!

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
        if not self.children: self.children = []
        self.children.append(child)

    def getName(self):
        return self.wrappedObj.name

    def getXmlEl(self):
        tag = XmlTag('com.jme.scene.Node', {'name':self.getName()})
        # TODO:  This is where all of the attributes and children should be
        # added to the XML.
        mesh = self.wrappedObj.getData(False, True)
        if mesh:
            childrenTag = XmlTag("children", {"size":1})
            childrenTag.addChild(JmeObject.__genMeshEl(mesh, self.__vpf))
            tag.addChild(childrenTag)

        if self.children:
            for child in self.children: tag.addChild(child.getXmlEl())
        # Use either loc + rot + size OR matrixLocal
        # Set local variables just to reduce typing in this block.
        loc = self.wrappedObj.loc
        rQuat = self.wrappedObj.matrixLocal.toQuat()
        # DOES NOT WORK TO DO rQuat = self.wrappedObj.rot.toQuat() !!!!!!!!!
        if rQuat == JmeObject.__QUAT_IDENTITY: rQuat = None
        size = self.wrappedObj.size
        if loc and (loc[0] != 0. or loc[1] != 0. or loc[2] != 0.):
            tag.addChild(XmlTag("localTranslation", \
                    {"x":loc[0], "y":loc[1], "z":loc[2]}, 7))
        if rQuat and \
            (rQuat.x != 0. or rQuat.y != 0. or rQuat.z != 0. or rQuat.w != 1.):
            tag.addChild(XmlTag("localRotation", \
                    {"x":rQuat.x, "y":rQuat.y, "z":rQuat.z, "w":rQuat.w}, 7))
        if size and (size[0] != 1. or size[1] != 1. or size[2] != 1.):
            tag.addChild(XmlTag("localScale", \
                    {"x":size[0], "y":size[1], "z":size[2]}, 7))
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

    def __genMeshEl(meshObj, vpf):
        if not meshObj.verts:
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
        coArray = []
        noArray = []
        for v in meshObj.verts:
            coArray.append(v.co.x)
            coArray.append(v.co.y)
            coArray.append(v.co.z)
            noArray.append(v.no.x)
            noArray.append(v.no.y)
            noArray.append(v.no.z)
        vertTag = XmlTag("vertBuf", {"data":coArray}, 7)
        vertTag.addAttr("size", len(coArray))
        tag.addChild(vertTag)
        normTag = XmlTag("normBuf", {"data":noArray}, 7)
        normTag.addAttr("size", len(noArray))
        tag.addChild(normTag)
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
        if self.children:
            childrenTag = XmlTag('children', {'size':len(self.children)})
            tag.addChild(childrenTag)
            for child in self.children: childrenTag.addChild(child.getXmlEl())
        return tag
