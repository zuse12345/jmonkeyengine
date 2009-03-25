"""Wrapper classes for Blender native objects to facilitate conversion to
JME-usable format"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

from jme.xml import XmlTag, PITag, XmlFile
from Blender.Mathutils import RotationMatrix

class JmeObject(object):
    __slots__ = ('wrappedObj', 'children', 'vertsPerFace')

    def __init__(self, bObj):
        """Assumes input Blender Object already validated.
        I.e., is of supported type, and facing method is supported."""
        object.__init__(self)
        self.wrappedObj = bObj
        self.children = None
        print "Instantiated JmeObject '" + self.getName() + "'"
        faces = bObj.getData(False, True).faces
        self.vertsPerFace = None
        if not faces or len(faces) < 1: return
        for f in faces:
            if f.verts and len(f.verts) > 0:
                self.vertsPerFace = len(f.verts)
                return
        self.vertsPerFace = 0
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
            childrenTag.addChild(JmeObject.__genMeshEl(mesh, self.vertsPerFace))
            tag.addChild(childrenTag)

        if self.children:
            for child in self.children: tag.addChild(child.getXmlEl())
        # Use either loc + rot + size OR matrixLocal
        # Set local variables just to reduce typing in this block.
        loc = self.wrappedObj.loc
        rQuat = None
        if self.wrappedObj.rot: rQuat = self.wrappedObj.rot.toQuat()
        size = self.wrappedObj.size
        if loc and (loc[0] != 0. or loc[1] != 0. or loc[2] != 0.):
            tag.addChild(XmlTag("localTranslation", \
                    {"x":loc[0], "y":loc[1], "z":loc[2]}, 7))
        if rQuat and \
            (rQuat.x != 0. or rQuat.y != 0. or rQuat.z != 0. or rQuat.w != 1.):
            tag.addChild(XmlTag("localRotation", \
                    {"x":rQuat.x, "y":rQuat.x, "z":rQuat.x, "w":rQuat.w}, 7))
        if size and (size[0] != 1. or size[1] != 1. or size[2] != 1.):
            tag.addChild(XmlTag("localScale", \
                    {"x":size[0], "y":size[1], "z":size[2]}, 7))
        return tag

    def getType(self):
        return self.wrappedObj.type

    def supported(bObj):
        """Reject non-Mesh-type Blender Objects, and any Mesh-type Objects
        which has unsupported face vertexing"""
        if bObj.type not in ['Mesh']: return False
        if not bObj.data: raise Exception("Mesh Object has no data member?")
        if not bObj.data.faces:
            print "FYI:  Accepting object '" + bObj.name + "' with no faces"
            return True
        vertexesPerFace = None
        for f in bObj.getData(False, True).faces:
            if f.verts == None: raise Exception("Face with no vertexes?")
            if vertexesPerFace == None:
                vertexesPerFace = len(f.verts)
                continue
            if vertexesPerFace != len(f.verts):
                print "FYI:  Refusing object '" + bObj.name \
                        + "' because contains 2 different vertexes-per-face: " \
                        + str(vertexesPerFace) + " and " + str(len(f.verts))
                return False
        if vertexesPerFace == None:
            print "FYI:  Accepting object '" + bObj.name + "' with 0 faces"
            return True
        #print "VPF = " + str(vertexesPerFace)
        if vertexesPerFace == 3: return True
        if vertexesPerFace == 4: return True
        print "FYI:  Refusing object '" + bObj.name \
                + "' because unsupported vertexes-per-face: " \
                + str(vertexesPerFace)
        return False

    def __str__(self):
        return '[' + self.getName() + ']'

    def __repr__(self):
        return "<JmeObject> " + self.__str__()

    def __genMeshEl(meshObj, vpf):
        if not meshObj.verts:
            raise Exception("Mesh '" + meshObj.name + "' has no vertexes")
        if vpf == 4:
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
        if (not meshObj.faces) or len(meshObj.faces) < 1: return tag
        faceVertIndexes = []
        for face in meshObj.faces:
            for v in face.verts: faceVertIndexes.append(v.index)
        indTag = XmlTag("indexBuffer", {"data":faceVertIndexes})
        indTag.addAttr("size", len(faceVertIndexes))
        tag.addChild(indTag)
        return tag

    supported = staticmethod(supported)
    __genMeshEl = staticmethod(__genMeshEl)

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
