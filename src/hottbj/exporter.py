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
from hottbj.wrapperclasses import NodeTree as _NodeTree
from hottbj.wrapperclasses import JmeSkinAndBone as _JmeSkinAndBone
from hottbj.wrapperclasses import JmeNode as _JmeNode
from Blender.Modifier import Type as _bModifierType
from Blender.Modifier import Settings as _bModifierSettings
from Blender.Object import ParentTypes as _bParentTypes
import Blender.Mathutils as _bmath
import hottbj
import jme.esmath as _esmath

recordTimestamp = "--nostamps" not in hottbj.blenderArgs
skinParents = None
skinParentTypes = None
skinLocals = None
skinMPIs = None

def descendantOf(meNode, ancestor):
    if meNode.parent == None: return False
    if meNode.parent == ancestor: return True
    return descendantOf(meNode.parent, ancestor)

def backup(bo):
    """Back up specified Blender object and its descendants, if it hasn't been
    already."""
    global recordTimestamp, skinParents, skinParentTypes, skinLocals, skinMPIs
    if bo in skinParents: return
    # Since these lists are all parallel (same keys), an object is non or in all
    skinParents[bo] = bo.parent
    skinParentTypes[bo] = bo.parentType
    skinLocals[bo] = bo.matrixLocal.copy()
    #print "backing up loc for " + bo.name + " from\n" + str(bo.matrixLocal)
    #print "\n  ...to " + str(skinLocals[bo])
    skinMPIs[bo] = bo.matrixParentInverse.copy()
    # This is like this for historical reasons.
    # Should be refactored to work completely recursively or completely flat.
    for dob in _bdata.scenes.active.objects:
        if dob in skinParents: continue
        if not descendantOf(dob, bo): continue
        skinParents[dob] = dob.parent
        skinParentTypes[dob] = dob.parentType
        skinLocals[dob] = dob.matrixLocal.copy()
        skinMPIs[dob] = dob.matrixParentInverse.copy()

def gen(saveAll, autoRotate, skipObjs=True,
        maxWeightings=4, exportActions=True, skinTransfers=True):
    # There is unfortunately WAY TOO MUCH CRAZY behavior in this method.
    # This is because of Blender wackiness.  Changes to matrixLocal can't
    # be seen right away.  Hidden effects of magical .matrixParentInverse.
    # Different behavior from .setMatrix() and .matrixLocal =...

    # An unfortunate consequence of object interdependencies is that though we
    # have pre-validated as much as possible, some objects may be rejected due
    # relationships with other "selected" items.  In that case, we must just
    # abort, since our UI doesn't have any other way to notify the user.
    # Otherwise it would look as if everything selected exported successfully.
    global recordTimestamp, skinParents, skinParentTypes, skinLocals, skinMPIs

    skinParents = {}
    skinParentTypes = {}
    skinLocals = {}
    skinMPIs = {}
    armaLocals = {}
    activeActions = {}
    os = []
    candidates = []
    supportedCandidates = []
    if skinTransfers:
        skinTransferNodes = []
    else:
        skinTransferNodes = None
        # None means do not create SkinTransferNodes.
    origEditMode = _bEditMode()
    if origEditMode != 0: _bEditMode(0)
    activeScene = _bdata.scenes.active
    try:
        if saveAll:
            candidates = _bdata.objects
        else:
            candidates = activeScene.objects.selected
        nodeTree = _NodeTree(maxWeightings, exportActions)
        for bo in candidates:
            layerIsActive = False
            for layer in bo.layers:
                if layer in activeScene.layers:
                    layerIsActive = True
                    break
            if not layerIsActive: continue
            if _JmeNode.supported(bo, skipObjs):
                supportedCandidates.append(bo)

        for bo in supportedCandidates:
            if bo.type == "Armature": activeActions[bo] = bo.getAction()
            for mod in bo.modifiers:
                if (mod.type != _bModifierType.ARMATURE
                        or mod[_bModifierSettings.OBJECT] == None): continue
                modObject = mod[_bModifierSettings.OBJECT]
                if ((modObject in supportedCandidates
                        or skinTransferNodes != None)
                        and modObject != bo.parent):
                    if bo.parentType != _bParentTypes["OBJECT"]:
                        raise Exception(
                            "Unexpected parent type for Arma-modified Object '"
                            + bo.getName() + "': " + str(bo.parentType))
                    backup(bo)
                    #print ("\n\nPRE for " + bo.name + "\n"
                    #+ str(bo.matrixLocal) + "\n" + str(bo.matrixParentInverse)
                    modObject.makeParentDeform([bo])
                    #print "\n\nPOST\n" + str(bo.matrixLocal)
                    #+ "\n" + str(bo.matrixParentInverse)
            if (bo.parent == None or bo.parentType != _bParentTypes["ARMATURE"]
                    or (bo.parent not in supportedCandidates
                    and skinTransferNodes == None)): continue
            # From this point, we know we will store 'bo' as a skin.

            if (bo.parent not in supportedCandidates
                    and bo.parent not in skinTransferNodes):
                skinTransferNodes.append(bo.parent)

            # No-ops if backed up before .makeParentDeform() above:
            backup(bo)
            if bo.parent in armaLocals:
                origParentMat = armaLocals[bo.parent]
                adjustedParent = True
            else:
                origParentMat = bo.parent.matrixLocal.copy()
                armaLocals[bo.parent] = origParentMat
                adjustedParent = False
            #if not _esmath.floats2dEq(bo.mat, bo.parent.mat):
                #raise Exception("Set skin " + bo.getName()
                  #+ "'s transform to match that of its Armature")
            if _esmath.isIdentity(bo.matrixLocal * origParentMat):
                continue
            # Test above just shortcuts useless attempt.
            # The real test whether we will change transform is below.
            # Doesn't seem right with multiple skins.
            # Move the arma according to one skin but maybe not for others?

            # We must zero the Skin node Object's transform from the
            # skin node and the Arma obj. to avoid serious problems
            # with bone locations and with model location jumping when
            # animations start up.
            origMat = bo.matrixLocal.copy()
            inversion = (bo.matrixLocal * origParentMat).invert()
            if not adjustedParent: bo.parent.matrixLocal *= inversion
            #bo.matrixLocal *= inversion
            bo.setMatrix(_bmath.Matrix()) # .identity sometimes does not work
            if origMat == bo.matrixLocal:
                print("Matrix was not changed when identitied:\n"
                        + str(bo.matrixLocal))
            else:
                print "Relocated " + bo.name + " to\n" + str(bo.matrixLocal)
            if not _esmath.isIdentity(bo.matrixLocal):
                print bo.name + " NOT zeroed:\n" + str(bo.matrixLocal)
            if adjustedParent: continue
            if origParentMat == bo.parent.matrixLocal:
                print ("Parent Matrix was not changed when transformed by:\n"
                        + str(inversion))
        # At this point we have converted all exporting skin meshes to be
        # parentType ARMATURE, with corresponding parent, whether or not the
        # skinning is done via modifier.
        for bo in supportedCandidates:
            jmeObj = nodeTree.addSupported(bo, skipObjs)
            if (isinstance(jmeObj, _JmeSkinAndBone)
                    and not _esmath.isIdentity(armaLocals[bo])):
                jmeObj.setMatrix(armaLocals[bo])
        if skinTransferNodes != None:
            for bo in skinTransferNodes:
                jmeObj = nodeTree.addSupported(bo, skipObjs, True)
                if not isinstance(jmeObj, _JmeSkinAndBone):
                    raise Exception("Assertion failed:  Wrapped class for "
                            + bo.name + " not a JmeSkinAndBone: "
                            + str(type(jmeObj)))
                if not _esmath.isIdentity(armaLocals[bo]):
                    jmeObj.setMatrix(armaLocals[bo])

        # Don't know why this is needed here, but exports invoked with
        # non-rest pose get totally hosed without the following block (which is
        # a duplicate of some code in the finally block).
        for armaBo, action in activeActions.iteritems():
            if action != None: action.setActive(armaBo)
        activeScene.update(1)

        root = nodeTree.nest()

        if root == None: raise Exception("Nothing to do...")

        stampText = "Blender export by HottBJ Exporter"
        if recordTimestamp: stampText += (" at " + _datetime.now().isoformat())
        stampText += ("\n     Exporter (not this file!) copyright by\n"
                + "     " + __author__ + "\n     + the jMonkeyEngine Dev Team")
        xmlFile = _XmlFile(nodeTree.getXml(autoRotate))
        xmlFile.addComment(stampText)
        return xmlFile
    finally:
        # Restoration sequence is significant, since parenting can magically
        # change transforms.  I don't know about about the 2 transforms, but
        # it seems more likely that matrixParentInverse would effect local
        # than vice versa.
        # Armas are always are root level and are never reparented
        for bo, localMat in armaLocals.iteritems():
            bo.setMatrix(localMat)
        for bo, par in skinParents.iteritems():
            if par == bo.parent:
                print "Restoring only transforms of " + bo.name
                # This is to prevent unnecessary reparenting, since this would
                # fail if parenting other than normal makeParent is needed
                # (like makeParentDeform et. al.).
            elif par == None:
                print ("Restoring " + bo.name + " to root, with Transforms")
                bo.parent.makeParent([bo]) # This just clears .parentType
                bo.clrParent()
            else:
                print ("Restoring " + bo.name
                        + " to parent " + par.name + ", with Transforms")
                par.makeParent([bo])
        for bo, mpi in skinMPIs.iteritems():
            _esmath.setFloats2d(bo.matrixParentInverse, mpi)
        for bo, localMat in skinLocals.iteritems():
            bo.setMatrix(localMat)
        for armaBo, action in activeActions.iteritems():
            if action == None:
                print "Restoring None action to " + armaBo.name
                armaBo.action = None
            else:
                print("Restoring action " + action.name + " to " + armaBo.name)
                action.setActive(armaBo)
        if origEditMode != 0: _bEditMode(origEditMode)
        activeScene.update(1)
        # This update() prevents Blender from showing the user a disconcerting
        # view of the modified scene before it refreshes with our restorations.

        # VALIDATION
        for bo, localMat in armaLocals.iteritems():
            if not _esmath.floats2dEq(bo.matrixLocal, localMat):
                print ("! Restoration of local arma matrix of " + bo.name
                        + " failed.  It remains:\n" + str(bo.matrixLocal))
        for bo, par in skinParents.iteritems():
            if bo.parent != par:
                print ("! Restoration of parent of " + bo.name
                + " failed (it remains '" + str(bo.parent) + "')")
        for bo, parType in skinParentTypes.iteritems():
            if bo.parentType != parType:
                print ("! Restoration of parent type of " + bo.name
                + " failed (it remains '" + str(bo.parentType) + "')")
        for bo, mpi in skinMPIs.iteritems():
            if not _esmath.floats2dEq(bo.matrixParentInverse, mpi):
                print ("! Restoration of MPI of " + bo.name
                + " failed.  It remains\n" + str(bo.matrixParentInverse))
        for bo, localMat in skinLocals.iteritems():
            if not _esmath.floats2dEq(bo.matrixLocal, localMat):
                print ("! Restoration of local skin matrix of " + bo.name
                        + " failed.  It remains:\n" + str(bo.matrixLocal))
