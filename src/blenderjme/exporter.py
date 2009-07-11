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
from blenderjme.wrapperclasses import JmeSkinAndBone as _JmeSkinAndBone
from blenderjme.wrapperclasses import JmeNode as _JmeNode
from Blender.Modifier import Type as _bModifierType
from Blender.Modifier import Settings as _bModifierSettings
from Blender.Object import ParentTypes as _bParentTypes
import Blender.Mathutils as _bmath
import blenderjme
import jme.esmath as _esmath

recordTimestamp = "--nostamps" not in blenderjme.blenderArgs

def descendantOf(meNode, ancestor):
    if meNode.parent == None: return False
    if meNode.parent == ancestor: return True
    return descendantOf(meNode.parent, ancestor)

def gen(saveAll, autoRotate, skipObjs=True,
        maxWeightings=4, exportActions=True):
    global recordTimestamp

    reparenteds = {}
    relocateds = {}
    activeActions = {}
    os = []
    candidates = []
    supportedCandidates = []
    zapees = []  # Blender Objs which are supported atomically, but
                 # rejected due to interdependencies with other selected
                 # objects.
    origEditMode = _bEditMode()
    if origEditMode != 0: _bEditMode(0)
    try:
        if saveAll:
            candidates = _bdata.objects
        else:
            candidates = _bdata.scenes.active.objects.selected
        nodeTree = _NodeTree(maxWeightings, exportActions)
        for bo in candidates:
            if _JmeNode.supported(bo, skipObjs):
                supportedCandidates.append(bo)
        for bo in supportedCandidates:
            if bo.type != "Armature": continue
            if  bo.parent != None and bo.parent in supportedCandidates:
                print("Rejecting Armature '" + bo.getName()
                        + "' because we only support root-level Arma Objs")
                zapees.append(bo)
            else:
                activeActions[bo] = bo.getAction()
        for bo in zapees: supportedCandidates.remove(bo)
        for bo in supportedCandidates:
            for mod in bo.modifiers:
                if (mod.type != _bModifierType.ARMATURE
                        or mod[_bModifierSettings.OBJECT] == None): continue
                modObject = mod[_bModifierSettings.OBJECT]
                if (modObject in supportedCandidates
                        and modObject != bo.parent):
                    if bo.parentType != _bParentTypes["OBJECT"]:
                        raise Exception(
                            "Unexpected parent type for Arma-modified Object: "
                            + str(bo.parentType))
                    reparenteds[bo] = bo.parent
                    modObject.makeParentDeform([bo])
            if (bo.parent == None or bo.parentType != _bParentTypes["ARMATURE"]
                    or bo.parent not in supportedCandidates): continue
               # Not a skin node
            if _esmath.isIdentity(bo.matrixLocal * bo.parent.matrixLocal, 6):
                continue
             # Test above just shortcuts useless attempt.
             # The real test whether we will change transform is below.
            # This is a Skin node.
            # We must zero the Skin node Object's transform from the
            # skin node and the Arma obj. to avoid serious problems
            # with bone locations and with model location jumping when
            # animations start up.
            origParentMat = bo.parent.matrixLocal.copy()
            origMat = bo.matrixLocal.copy()
            inversion = (bo.matrixLocal * bo.parent.matrixLocal).invert()
            bo.parent.matrixLocal *= inversion
            bo.matrixLocal *= inversion
            if origMat == bo.matrixLocal:
                print("Matrix was not changed when transformed by:\n"
                        + str(inversion))
            else:
                relocateds[bo] = origMat
            if origParentMat == bo.parent.matrixLocal:
                print ("Parent Matrix was not changed when transformed by:\n"
                        + str(inversion))
            else:
                relocateds[bo.parent] = origParentMat
        # At this point we have converted all exporting skin meshes to be
        # parentType ARMATURE, with corresponding parent, whether or not the
        # skinning is done via modifier.
        for bo in supportedCandidates:
            jmeObj = nodeTree.addSupported(bo, skipObjs)
            if isinstance(jmeObj, _JmeSkinAndBone) and bo in relocateds:
                jmeObj.setMatrix(relocateds[bo])
        # This backs up all objects under skin nodes, since they may get moved

        # Don't know why this is needed here, but exports without skin are
        # totally hosed without the following block (which duplicates what is
        # done in the finally block at the end).
        for armaBo, action in activeActions.iteritems():
            if action == None:
                print "Restoring None action to " + armaBo.getName()
                armaBo.action = None
            else:
                print("Restoring action "
                        + action.getName() + " to " + armaBo.getName())
                action.setActive(armaBo)
        _bdata.scenes.active.update(1)

        for skinBo in relocateds.keys()[:]:
            if skinBo.parent == None: continue  # Armature object
            # skinBo really is a Skin Object now
            for bo in _bdata.scenes.active.objects:
                if bo in relocateds: continue
                if descendantOf(bo, skinBo):
                    relocateds[bo] = bo.matrixLocal.copy()
        root = nodeTree.nest()

        if root == None: raise Exception("Nothing to do...")

        stampText = "Blender export by Blender-to-jME Exporter"
        if recordTimestamp: stampText += (" at " + _datetime.now().isoformat())
        stampText += ("\n     Exporter (not this file!) copyright by\n"
                + "     " + __author__ + "\n     + the jMonkeyEngine Dev Team")
        xmlFile = _XmlFile(nodeTree.getXml(autoRotate))
        xmlFile.addComment(stampText)
        return xmlFile
    finally:
        for bo, mat in relocateds.iteritems():
            #if bo.matrixLocal == mat: continue  # sometimes gives false posit.
            origMat = bo.matrixLocal.copy()
            bo.matrixLocal = mat
            print("Restoring transform matrix of " + bo.type + " '"
                    + bo.getName() + "'")
            if origMat == bo.matrixLocal:
                print("WARNING:  Internal problem:  " + bo.getName()
                        + " Matrix was not changed when restored")
                        #+ " with:\n" + str(mat) + "\nFROM:\n" + str(origMat)
                        #+ "\n==>\n" + str(bo.matrixLocal))
        for bo, par in reparenteds.iteritems():
            print "Restoring parent "+ str(par) + " to " + bo.getName()
            if par == None:
                bo.parent.makeParent([bo]) # This just clears .parentType
                bo.clrParent()
            else:
                par.makeParent([bo])
        for armaBo, action in activeActions.iteritems():
            if action == None:
                print "Restoring None action to " + armaBo.getName()
                armaBo.action = None
            else:
                print("Restoring action "
                        + action.getName() + " to " + armaBo.getName())
                action.setActive(armaBo)
        if origEditMode != 0: _bEditMode(origEditMode)
        _bdata.scenes.active.update(1)
        # This update() prevents Blender from showing the user a disconcerting
        # view of the modified scene before it refreshes with our restorations.
