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

import Blender
from Blender import Draw
from Blender import BGL
from jme.xml import XmlTag, PITag, XmlFile
from datetime import datetime
from blenderjmeexp.wrapperclasses import *
from bpy import data
from Blender.Mathutils import RotationMatrix

BLENDER_TO_JME_ROTATION = RotationMatrix(-90, 4, 'x')
recordTimestamp = True

def gen(saveAll, autoRotate):
    global recordTimestamp

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
        if len(os) < 1:
            raise Exception("Nothing to do...")
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

        #if autoRotate: data.scenes.active.update()     No effect
        stampText = "Blender export by Blender/JME Exporter"
        if recordTimestamp: stampText += (" at " + datetime.now().isoformat())
        xmlFile = XmlFile(root.getXmlEl())
        xmlFile.addComment(stampText)
        # Though the final XML text has not been generated, the buffer of
        # string pieces have all been generated at this point, and the model
        # data will no longer be referenced.  Therefore, it's safe to return
        # a XmlFile even though we will now revert some transforms (which
        # will then not match what is in the XmlFile instance).
        for n, v in changedMats.iteritems(): n.matrixLocal = v
        # This restores the original matrixes for top-level objects
        if len(changedMats) > 0: data.scenes.active.update(1)
        return xmlFile
    finally:
        if origEditMode != 0: Blender.Window.EditMode(origEditMode)
