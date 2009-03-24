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

    def addChild(self, child):
        if not self.children: self.children = []
        self.children.append(child)

    def getName(self):
        return self.wrappedObj.name

    def getXmlEl(self):
        tag = XmlTag('com.jme.scene.Node', {'name':self.getName()})
        # TODO:  This is where all of the attributes and children should be
        # added to the XML.
        if self.children:
            for child in self.children: tag.addChild(child.getXmlEl())
        return tag

    def getType(self):
        return self.wrappedObj.type

    def supported(bObj):
        return bObj.type in ['Mesh']

    def __str__(self):
        return '[' + self.getName() + ']'

    def __repr__(self):
        return "<JmeObject> " + self.__str__()


    supported = staticmethod(supported)

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
            for child in self.children: tag.addChild(child.getXmlEl())
        return tag
