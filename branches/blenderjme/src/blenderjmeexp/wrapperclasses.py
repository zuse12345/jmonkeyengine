"""Wrapper classes for Blender native objects to facilitate conversion to
JME-usable format"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

from jme.xml import XmlTag, PITag, XmlFile

class JmeObject(object):
    __slots__ = ('wrappedObj', 'children')

    def __init__(self, bObj):
        object.__init__(self)
        self.wrappedObj = bObj
        self.children = None
        print "Instantiated JmeObject '" + self.getName() + "'"
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
            childrenTag.addChild(JmeObject.__genMeshEl(mesh))
            tag.addChild(childrenTag)

        if self.children:
            for child in self.children: tag.addChild(child.getXmlEl())
        # Use either loc + rot + size OR matrixLocal
        if self.wrappedObj.rot:
            tag.addChild(XmlTag("localRotation", {"x":0}))
        if self.wrappedObj.loc:
            tag.addChild(XmlTag("localTranslation", {"x":0}))
        if self.wrappedObj.size:
            tag.addChild(XmlTag("localScale", {"x":0}))
        return tag

    def getType(self):
        return self.wrappedObj.type

    def supported(bObj):
        return bObj.type in ['Mesh']

    def __str__(self):
        return '[' + self.getName() + ']'

    def __repr__(self):
        return "<JmeObject> " + self.__str__()

    def __genMeshEl(meshObj):
        if not meshObj.verts:
            raise Exception("Mesh '" + meshObj.name + "' has no vertexes")
        # TODO:  When iterate through verts to get vert vectors + normals,
        #        do check for null normal and throw if so:
        #   raise Exception("Mesh '" \
        #       + meshObj.name + "' has a vector with no normal")
        #   This is a Blender convention, not a 3D or JME convention
        #   (requirement for normals).
        tag = XmlTag('com.jme.scene.TriMesh', {'name':meshObj.name})
        tag.addChild(XmlTag("vertBuf", {"data":0, "size":len(meshObj.verts)}))
        tag.addChild(XmlTag("normBuf", {"data":0, "size":len(meshObj.verts)}))
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
