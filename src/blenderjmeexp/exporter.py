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

BLENDER_TO_JME_ROTATION = RotationMatrix(-90, 4, 'x')

def gen(saveAll, autoRotate):
    origEditMode = Blender.Window.EditMode()
    if origEditMode != 0: Blender.Window.EditMode(0)
    try:
        os = []
        candidates = []
        changedMats = {}  # Matrixes we have to change to effect axis rotation
        if saveAll:
            candidates = data.objects
        else:
            candidates = data.scenes.active.objects.selected
        for o in candidates:
            if JmeObject.supported(o): os.append(JmeObject(o))
        root = None
        if len(os) > 1:
            root = JmeNode("BlenderObjects")
            for o in os:
                if autoRotate:
                    changedMats[o.wrappedObj] = o.wrappedObj.matrixLocal.copy()
                    o.wrappedObj.matrixLocal = \
                            o.wrappedObj.matrixLocal * BLENDER_TO_JME_ROTATION
                root.addChild(o)
        else:
            if autoRotate:
                changedMats[os[0].wrappedObj] = \
                        os[0].wrappedObj.matrixLocal.copy()
                os[0].wrappedObj.matrixLocal = \
                        os[0].wrappedObj.matrixLocal * BLENDER_TO_JME_ROTATION
            root = os[0]

        pi = PITag('xml', {'version':'1.0', 'encoding':'UTF-8'})
        pi.addComment("Blender export by Blender/JME Exporter at " \
                + datetime.now().isoformat())
        xmlFile = XmlFile(root.getXmlEl(), pi=pi)
        for n, v in changedMats.iteritems(): n.matrixLocal = v
        # This restores the original matrixes for top-level objects
        data.scenes.active.update(1)
        return xmlFile
    finally:
        if origEditMode != 0: Blender.Window.EditMode(origEditMode)
