"""Wrapper classes for Blender native objects to facilitate conversion to
JME-usable format"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

from jme.xml import XmlTag, PITag, XmlFile

class JmeObject(object):
    __slots__ = ('wrappedObj', 'name', 'children')

    def __init__(self, bObj):
        object.__init__(self)
        self.wrappedObj = bObj
        self.children = None
        print "Instantiated jmeObject '" + self.getName() + "'"

    def addChild(child):
        if not self.children: self.children = []
        self.children.append(child)

    def getName(self):
        return self.wrappedObj.name

    def getXmlEl(self):
        tag = XmlTag('com.jme.scene.Node')
        if self.children:
            for child in self.children: tag.addChild(child)
        return str(tag)

    def getType(self):
        return self.wrappedObj.type

    def supported(bObj):
        return bObj.type in ['Mesh']

    supported = staticmethod(supported)
