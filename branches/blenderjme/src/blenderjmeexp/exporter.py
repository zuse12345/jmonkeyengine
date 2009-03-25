__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

import Blender
from Blender import Draw
from Blender import BGL
from jme.xml import XmlTag, PITag, XmlFile
from datetime import datetime
from blenderjmeexp.wrapperclasses import *
from bpy import data

def gen(saveAll):
    origEditMode = Blender.Window.EditMode()
    if origEditMode != 0: Blender.Window.EditMode(0)
    try:
        os = []
        candidates = []
        if saveAll:
            candidates = data.objects
        else:
            candidates = data.scenes.active.objects.selected
        for o in candidates:
            if JmeObject.supported(o): os.append(JmeObject(o))
        root = None
        if len(os) > 1:
            root = JmeNode("BlenderObjects")
            for o in os: root.addChild(o)
        else:
            root = os[0]

        pi = PITag('xml', {'version':'1.0', 'encoding':'UTF-8'})
        pi.addComment("Blender export by Blender/JME Exporter at " \
                + datetime.now().isoformat())
        return XmlFile(root.getXmlEl(), pi=pi)
    finally:
        if origEditMode != 0: Blender.Window.EditMode(origEditMode)
