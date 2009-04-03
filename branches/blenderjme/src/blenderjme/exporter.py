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

from Blender.Window import EditMode as _bEditMode
from jme.xml import XmlFile as _XmlFile
from datetime import datetime as _datetime
from bpy import data as _bdata
from blenderjme.wrapperclasses import NodeTree as _NodeTree
import blenderjme

recordTimestamp = "--nostamps" not in blenderjme.blenderArgs

def gen(saveAll, autoRotate):
    global recordTimestamp

    origEditMode = _bEditMode()
    if origEditMode != 0: _bEditMode(0)
    try:
        os = []
        candidates = []
        changedMats = {}  # Matrixes we have to change to effect axis rotation
        if saveAll:
            candidates = _bdata.objects
        else:
            candidates = _bdata.scenes.active.objects.selected
        nodeTree = _NodeTree()
        for bo in candidates: nodeTree.addIfSupported(bo)
        root = nodeTree.nest()

        if root == None: raise Exception("Nothing to do...")
        if autoRotate:
            if root.wrappedObj == None:
                for child in root.children: child.autoRotate = True
            else:
                root.autoRotate = True

        stampText = "Blender export by Blender/JME Exporter"
        if recordTimestamp: stampText += (" at " + _datetime.now().isoformat())
        xmlFile = _XmlFile(nodeTree.getXml())
        xmlFile.addComment(stampText)
        return xmlFile
    finally:
        if origEditMode != 0: _bEditMode(origEditMode)
