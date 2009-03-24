__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

import Blender
from Blender import Draw
from Blender import BGL
from jme.xml import XmlTag, PITag, XmlFile

def gen(x):
    origEditMode = Blender.Window.EditMode()
    if origEditMode != 0: Blender.Window.EditMode(0)
    try:
        apple = XmlTag('apple', {'x':'y'})
        rootTag = XmlTag('orange', {'color':'yellow'})
        apple.addComment("Apples are delicious")
        rootTag.addAttr('mass', 3.4, 3)
        rootTag.addText('Some words')
        rootTag.addComment('One comment')
        rootTag.addComment('Another comment')
        rootTag.addChild(XmlTag('peach', {'skin':'fuffy'}))
        rootTag.addChild(XmlTag('pineapple', {'prickley':'true'}))
        rootTag.addChild(apple)
        apple.addChild(XmlTag('grape', {'tasty':'true'}))
        pi = PITag('processInstr', {'version':'1.0', 'encoding':'UTF-8'})
        pi.addComment("A doc comment")
    finally:
        if origEditMode != 0: Blender.Window.EditMode(origEditMode)
    return XmlFile(rootTag, pi=pi)
