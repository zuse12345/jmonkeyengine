"""Wrapper classes for Blender native objects to facilitate conversion to
JME-usable format"""

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

from jme.xml import XmlTag as _XmlTag
from jme.xml import ecoformat as _ecoformat
import jme.esmath as _esmath
import Blender.Mathutils as _bmath
from Blender.Material import Modes as _matModes
from Blender.Mesh import Modes as _meshModes
from Blender import Armature as _Armature
from Blender import Window as _Window
from Blender import Set as _bSet
from ActionData import ActionData
from bpy.data import actions as _bActionSeq
from Blender.Object import ParentTypes as _bParentTypes
from Blender.IpoCurve import InterpTypes as _bInterpTypes
from Blender.Modifier import Type as _bModifierType
from Blender.Modifier import Settings as _bModifierSettings

# GENERAL DEVELOPMENT NOTES:
#
# For unsupported Blender features (e.g. a particular Shader), carefully
# consider, on a case-by-case basis, whether it is better to not export the
# containing object (i.e. treat the object as unsupported) or to just not
# export that single features.  For the latter case, it's good practice to
# write a warning to stdout.
#
# When getting the mesh to work with from Blender, make sure to use
# ...getMesh(False, True), or the equivalent ...getMesh(mesh=True).
# This will get a Blender "wrapped" Mesh instance instead of a non-wrapped
# NMesh instance (contrary to the wrapping we are doing here, Blender's
# "wrapping" actually puts you closer to the physical data).
# The parent/child relationships between these wrapper classes reflects
# scene node relationships, not Blender's explicit parenting relationships.
# For example, Blender Objects aggregate Meshes and Mesh references, but do
# not consider them children; but in JME, nodes can have nodes and Meshes
# (or any other Spatials) as children.
#
# Consider limiting vertex transformations (thereby losing useful node
# transformations by checking Object.upAxis (read-only attr.)


class UnsupportedException(Exception):
    def __init__(self, setting, val=None, note=None):
        Exception.__init__(self)
        if val == None:
            self.msg = ("Feature '" + setting + "' is unsupported.")
        else:
            self.msg = ("Value '" + str(val) + "' for setting '" + setting
                    + "' unsupported.")
        if note != None: self.msg += ("  " + note)

    def __str__(self):
        return self.msg

def addRotationEl(parentEl, inQuat):
    """Modifies the parent Element in-place.  Returns nothing.
    It is safe for inQuat to be None, in which case, nothing is done.
    The value attributes are added sequentially so that they will always be
    ordered consistently.
    """
    if inQuat == None: return
    newTag = _XmlTag("localRotation")
    newTag.addAttr("x", inQuat.x, 6)
    newTag.addAttr("y", inQuat.y, 6)
    newTag.addAttr("z", inQuat.z, 6)
    newTag.addAttr("w", inQuat.w, 6)
    parentEl.addChild(newTag)

def addScaleEl(parentEl, inVecList):
    """Modifies the parent Element in-place.  Returns nothing.
    It is safe for inVecList to be None, in which case, nothing is done.
    The value attributes are added sequentially so that they will always be
    ordered consistently.
    """
    if inVecList == None: return
    newTag = _XmlTag("localScale")
    newTag.addAttr("x", inVecList[0], 6)
    newTag.addAttr("y", inVecList[1], 6)
    newTag.addAttr("z", inVecList[2], 6)
    parentEl.addChild(newTag)

def addTranslationEl(parentEl, inVecList):
    """Modifies the parent Element in-place.  Returns nothing.
    It is safe for inVecList to be None, in which case, nothing is done.
    The value attributes are added sequentially so that they will always be
    ordered consistently.
    """
    if inVecList == None: return
    newTag = _XmlTag("localTranslation")
    newTag.addAttr("x", inVecList[0], 6)
    newTag.addAttr("y", inVecList[1], 6)
    newTag.addAttr("z", inVecList[2], 6)
    parentEl.addChild(newTag)

def addVector3fEl(parentEl, tagName, inVecList):
    """
    If specify tagName of None, tag will be named 'com.jme.math.Vector3f',
    otherwise the tag will be named as specified and a 'class' attr will
    be added with value 'com.jme.math.Vector3f'.
    Modifies the parent Element in-place.  Returns nothing.
    It is safe for inVecList to be None, in which case, nothing is done.
    The value attributes are added sequentially so that they will always be
    ordered consistently.
    """
    if inVecList == None: return
    if tagName == None:
        newTag = _XmlTag("com.jme.math.Vector3f")
    else:
        newTag = _XmlTag(tagName)
        newTag.addAttr("class", "com.jme.math.Vector3f")
    newTag.addAttr("x", inVecList[0], 6)
    newTag.addAttr("y", inVecList[1], 6)
    newTag.addAttr("z", inVecList[2], 6)
    parentEl.addChild(newTag)


from Blender.Object import PITypes as _bPITypes
class JmeNode(object):
    __slots__ = ('wrappedObj', 'children', 'jmeMats', 'jmeTextureState',
            'name', 'backoutTransform', 'implClass', 'postFlip', 'join',
            'skinRegion')

    def __init__(self, bObjOrName, nodeTree=None):
        """
        Assumes input Blender Object already validated, like by using the
        supported static method below.
        I.e., is of supported type, and facing method is supported.
        Materials from given bObj and direct data mesh (if any) will be added
        to one of the specified nodeTree's materials maps.
        The instantiator does not take a backoutTransform parameter.
        backoutTransform must be set later, after we know which ancestor
        nodes are being exported.
        """
        object.__init__(self)
        self.children = None
        self.wrappedObj = None
        self.backoutTransform = None
        self.jmeMats = None
        self.jmeTextureState = None
        self.skinRegion = None
        self.implClass = "com.jme.scene.Node"
        self.postFlip = False
        self.join = False  # Means to write one Mesh XML node a.o.t. Node+Mesh

        if isinstance(bObjOrName, basestring):
            self.name = bObjOrName
            return

        if nodeTree == None:
            raise Exception("If instantiating a Blender-Object-wrapping Node, "
                    + "param nodeTree must be set")
        self.wrappedObj = bObjOrName
        self.name = self.wrappedObj.name

        # The join tactic here is the most simple and consistent way.
        # Skins are ALWAYS joined, regardless of whether orphaned.
        # Difficult to non-join orphans, since we don't have the exporting
        # parenting information at instantiation time.
        if self.wrappedObj.parentType == _bParentTypes["ARMATURE"]:
            self.join = True
        else:
            for mod in self.wrappedObj.modifiers:
                if (mod.type == _bModifierType.ARMATURE
                        and mod[_bModifierSettings.OBJECT] != None):
                    self.join = True
                    break

        try:
            icProp = self.wrappedObj.getProperty("jme.implClass")
            if icProp.type == 'STRING':
                self.implClass = icProp.data
                if self.join:
                    raise Exception("An impl class may not be specified for a "
                            + "skin.  This is because it must be written as an "
                            + "appropriate Geometry type");
        except Exception, e:
            pass # gets an exceptions.RuntimeError
        try:
            srProp = self.wrappedObj.getProperty("jme.skinRegion")
            if srProp.type == 'STRING': self.skinRegion = srProp.data
        except Exception, e:
            pass # gets an exceptions.RuntimeError
        bMesh = self.wrappedObj.getData(False, True)
        twoSided = bMesh != None and (bMesh.mode & _meshModes['TWOSIDED'])
        jmeTexs = []
        self.jmeMats = []
        # The local above must be at this scope because for the 'join' case, we
        # will append to and use it again later.  self.jmeMats is not required
        # by scope, but its easier to use it again later knowing it's defined.
        if len(self.wrappedObj.getMaterials()) > 0:
            for bMat in self.wrappedObj.getMaterials():
                if not bMat.mode & _matModes['TEXFACE']:
                    try:
                        self.jmeMats.append(nodeTree.includeMat(bMat, twoSided))
                    except UnsupportedException, ue:
                        print ("Skipping a texture of object " + self.name
                                + " due to: " + str(ue))
                for j in bMat.enabledTextures:
                    try:
                        JmeTexture.supported(bMat.textures[j])
                        jmeTexs.append(nodeTree.includeTex(bMat.textures[j]))
                    except UnsupportedException, ue:
                        print ("Skipping a texture of object " + self.name
                                + " due to: " + str(ue))
            if len(jmeTexs) > 0 and not self.join:
                self.jmeTextureState =  \
                        nodeTree.includeJmeTextureList(jmeTexs[:])
                del(jmeTexs[:])  # clear list
        if bMesh == None or len(bMesh.verts) == 0: return
         # An unused Blender "EmptyMesh" will have 0 verts.
         # This is useful for our Material inheritance feature

        # From here on, we know we have a Blender direct Mesh data member
        objColor = None
        try:
            objColor = self.wrappedObj.color
        except Exception, e:
            print str(e)  # For some reason, sometimes the .color attribute is
                          # available, and sometimes it is not.
        jmeMesh =  JmeMesh(bMesh, objColor)
        self.addChild(jmeMesh)
        #print "Instantiated JmeNode '" + self.getName() + "'"
        # Since Blender has mashed together the Object and Mesh by having
        # object.colbits specify how the Mesh's materials are to be
        # interpreted, we add the Mesh's materials and textures here.
        meshMats = []
        if self.join:
            meshMats += self.jmeMats
            del(self.jmeMats[:])
        for i in range(len(bMesh.materials)):
            #print "Bit " + str(i) + " for " + self.getName() + " = " + str(self.wrappedObj.colbits & (1<<i))
            if 0 != (self.wrappedObj.colbits & (1<<i)): continue
            if bMesh.materials[i] == None: continue
              # This will happen if the mat has been removed
            #print "Adding Mesh Mat " + bMesh.materials[i].name
            if not bMesh.materials[i].mode & _matModes['TEXFACE']:
                try:
                    meshMats.append(
                            nodeTree.includeMat(bMesh.materials[i], twoSided))
                except UnsupportedException, ue:
                    print ("Skipping a mat of mesh " + jmeMesh.getName()
                            + " due to: " + str(ue))
                    continue
            for j in bMesh.materials[i].enabledTextures:
                try:
                    #print "**** Adding Texture for Mat: " + bMesh.materials[i].name
                    JmeTexture.supported(bMesh.materials[i].textures[j])
                    jmeTexs.append(nodeTree.includeTex(
                            bMesh.materials[i].textures[j]))
                except UnsupportedException, ue:
                    print ("Skipping a texture of mesh " + jmeMesh.getName()
                            + " due to: " + str(ue))
        if len(meshMats) > 0: jmeMesh.jmeMats = meshMats
        if len(jmeTexs) > 0:
            jmeMesh.jmeTextureState = nodeTree.includeJmeTextureList(jmeTexs)

    def addChild(self, child):
        if self.children == None: self.children = []
        self.children.append(child)

    def getName(self): return self.name

    def getImplClass(self): return self.implClass

    def getXmlEl(self, autoRotate):
        if self.jmeMats != None and len(self.jmeMats) < 1: self.jmeMats = None
        if (self.wrappedObj != None and self.wrappedObj.parent != None
                and self.wrappedObj.parent.getPose() != None):
            #print "Verifying non-null poses for " + self.getName() + "..."
            for poseBone in self.wrappedObj.parent.getPose().bones.values():
                if not _esmath.isIdentity(poseBone.localMatrix):
                    print("Node " + self.getName()
                        + " has non-zero pose matrix for bone '"
                        + poseBone.name + "'")
                    #print "Mat:\n" + str(poseBone.localMatrix)
                    if len(_bActionSeq) < 1:
                        raise Exception("Use ALT-r and ALT-g and ALT-s to "
                                + "zero all poses before invoking the exporter")
                    raise Exception(
                    "Rest Pose Frame is not active.  Consult the Online Help.")
                    # The reset below DOES NOT WORK.  Why???
                    #raise Exception("Node " + self.getName()
                    ActionData.zeroPose(self.wrappedObj.parent.getPose())
                    break
        if self.join:
            tagName = "Dummy"
        else:
            tagName = self.getImplClass()
        tag = _XmlTag(tagName, {'name':self.getName()})

        if self.jmeMats != None or self.jmeTextureState != None:
            rsTag = _XmlTag('renderStateList')
            if self.jmeMats != None:
                for mat in self.jmeMats: rsTag.addChild(mat.getXmlEl())
            if self.jmeTextureState != None:
                rsTag.addChild(self.jmeTextureState.getXmlEl())
            tag.addChild(rsTag)

        if self.wrappedObj != None:
            udTag = _XmlTag('userData')
            _addPropertiesXml(udTag, self.wrappedObj.game_properties)
            if len(udTag.children) > 0: tag.addChild(udTag)

        if self.children != None:
            childrenTag = _XmlTag('children')
            for child in self.children:
                if isinstance(child, JmeMesh):
                    if self.join:
                        child.populateXml(tag, autoRotate)
                    else:
                        meshTag = _XmlTag('dummy')
                        childrenTag.addChild(meshTag)
                        child.populateXml(meshTag, autoRotate)
                else:
                    if self.join:
                        raise Exception(
                                "Sorry, since skins are jME Geometries, "
                                + "you may not export '" + self.getName()
                                + "' with any children")
                    if self.wrappedObj != None:
                        child.backoutTransform = self.wrappedObj.mat
                    # N.b. DO NOT USE .matrixParentInverse.  That is a static
                    #  value for funky Blender behavior we don't want to retain.
                    childrenTag.addChild(child.getXmlEl(autoRotate))
            if len(childrenTag.children) > 0: tag.addChild(childrenTag)

        if self.wrappedObj == None: return tag

        if self.backoutTransform == None:
            matrix = self.wrappedObj.mat
        else:
            matrix = self.wrappedObj.mat * self.backoutTransform.copy().invert()
        if self.postFlip:
            matrix *= JmeSkinAndBone.BLENDERTOJME_FLIP_MAT4.copy().invert()

        # Set local variables just to reduce typing in this block.
        loc = matrix.translationPart()
        if _esmath.floatsEq(loc, 0.): loc = None
        # N.b. Blender.Mathutils.Quaternines ARE NOT COMPATIBLE WITH jME!
        # Blender Quaternion functions give different results from the
        # algorithms at euclideanspace.com, especially for the X rot
        # element.  However, until I don't want to introduce the risk of
        # several new ESQuaternion methods until I am certain that we can
        # successfully match Ogre exporter behavior with Blender's quats.
        #rQuat = _esmath.ESQuaternion(e, True)
        rQuat = matrix.toQuat()
        if (round(rQuat.x, 6) == 0 and round(rQuat.y, 6) == 0
                and round(rQuat.z, 6) == 0 and round(rQuat.w) == 1):
            rQuat = None
            scaleMat = matrix
        else:
            scaleMat = matrix * rQuat.copy().inverse().toMatrix().resize4x4()
        scale = [scaleMat[0][0], scaleMat[1][1], scaleMat[2][2]]
        if _esmath.floatsEq(scale, 1.): scale = None
        if autoRotate:
            if loc != None:
                hold = loc[1]
                loc[1] = loc[2]
                loc[2] = -hold
            if rQuat != None:
                hold = rQuat.y
                rQuat.y = rQuat.z
                rQuat.z = -hold
            if scale != None:
                hold = scale[1]
                scale[1] = scale[2]
                scale[2] = hold
        addTranslationEl(tag, loc)
        addScaleEl(tag, scale)
        addRotationEl(tag, rQuat)
        return tag

    def getType(self):
        if self.wrappedObj == None: return None
        return self.wrappedObj.type

    def supported(bObj, skipObjs):
        """
        Static method returns 0 if type not supported; non-0 if supported.
        2 if will require face niggling; 3 if faceless.
        An anomaly here is that this method is called for all sorts of
        Blender Objects, since we don't yet know what kind of exporter class
        (if any) corresponds.
        This method doesn't, and cannot, validate dependencies based upon
        other Blender Objects, since we don't necessarily know if the other
        objects are selected for export or not.
        An example of this situation is validation of Armature/Skin linkages.
        """
        # Silently ignore keyframes bMes.key for now.
        # Is face transp mode bMesh.mode for design-time usage?
        # Ignore animation, action, constraints, ip, and keyframing settings.
        # Users should know that we don't support them.

        if bObj.type not in ['Mesh', 'Armature', 'Empty']: return 0
        if bObj.type == 'Armature':
            arma = bObj.getData(False, True)
            # Don't test for arma.vertexGroups or arma.envelopes.
            # We support non-weighted skeletons; and it does us no harm if the
            # user plays with envelopes just on the Blender side.
            # Don't check any bone.options.  These are blender-side items to
            # facilitate laying out bones, constraints, and animations.
            if bObj.parent != None:
                print ("Rejecting Armature '" + bObj.getName()
                        + " because it is not at (object parenting) root level")
                return 0
            return True
        bMesh = bObj.getData(False, True)
        if bMesh != None and bMesh.multires:
            print "multires data"
            return 0
        if skipObjs:
            try:
                if len(bObj.getMaterials()) > 0:
                    for bMat in bObj.getMaterials():
                        if not bMat.mode & _matModes['TEXFACE']:
                            JmeMaterial(bMat, False)
                            # twoSided param doesn't matter
                        for j in bMat.enabledTextures:
                            JmeTexture.supported(bMat.textures[j])
                if bMesh != None:
                    for i in range(len(bMesh.materials)):
                        if 0 != (bObj.colbits & (1<<i)): continue
                        if bMesh.materials[i] == None: continue
                          # This will happen if the mat has been removed
                        if not bMesh.materials[i].mode & _matModes['TEXFACE']:
                            JmeMaterial(bMesh.materials[i], False) # ditto
                        for j in bMesh.materials[i].enabledTextures:
                            JmeTexture.supported(bMesh.materials[i].textures[j])
                # Following test was misplaced, because the nodes with the
                # texcoords may be descendants, not necessarily "this" node.
                #if anyTextures and (not bMesh.vertexUV) and not bMesh.faceUV:
                    #raise UnsupportedException(
                            #"We only support UV for texture mapping, and "
                            #+ "neither sticky nor face texcoords are set")
            except UnsupportedException, ue:
                print ue
                return 0
        if bObj.isSoftBody:
            print "WARNING:  Soft Body settings not supported yet.  Ignoring."
        if bObj.piType != _bPITypes['NONE']:
            print "WARNING:  Particle Interation not supported yet.  Ignoring."
        if bObj.rbMass != 1.0 or bObj.rbRadius != 1.0:
            print "WARNING:  Rigid Body settings not supported yet.  Ignoring."
            # Can't test .rbFlags, since they are on and significant by dflt.
            # Can't test .rbShapeBoundType, since there is a constant with
            # value 0.  Poorly designed constants.
        if bObj.track != None:
            print "WARNING: Object tracking not supported yet.  Ignoring."
        if not bMesh: return 1   # Accept non-Mesh-containing Object
        if bMesh.texMesh != None:
            print "Texture coords by Mesh-ref"
            return 0
        if bMesh.faceUV and bMesh.vertexUV:
            print "Mesh contains both sticky and per-face texture coords"
            return 0
        vpf = JmeMesh.vertsPerFace(bMesh.faces)
        if vpf == None:
            raise Exception(
                    "Internal problem.  vertsPerFace() should no longer return None");
        if len(vpf) == 0:
            print "FYI:  Accepting object '" + bObj.name + "' with no vert faces"
            return 3
        if vpf == set([0]):
            print "FYI:  Accepting object '" + bObj.name + "' with 0 vert faces"
            return 3
        if vpf == set([3,4]) or vpf == set([0,3,4]):
            print ("FYI:  Object '" + bObj.name
                    + "' accepted but will require unification to Trimeshes")
            return 2
        if (vpf == set([3,0]) or vpf == set([0,4])
                or vpf == set([3]) or vpf == set([4])): return 1
        print ("FYI:  Refusing object '" + bObj.name
                + "' because unsupported vertexes-per-face: " + str(vpf))
        return 0

    def __str__(self):
        return '[' + self.getName() + ']'

    def __repr__(self):
        return "<JmeNode> " + self.__str__()

    supported = staticmethod(supported)


class JmeMesh(object):
    __slots__ = ('wrappedMesh', 'jmeMats', 'jmeTextureState',
            '__vpf', 'defaultColor', 'name', 'blenderVertIndexes')
    # defaultColor corresponds to jME's Meshs' defaultColor.
    # In Blender this is a per-Object, not per-Mesh setting.
    # This is why it is a parameter of the constructor below.
    # blenderVertIndexes is a list corresponding to the vertBuf + normBuf
    # lists that we write, with the original Blender indexes of the vert.
    # (This is used for skin weighting).

    def __init__(self, bMesh, color=None):
        """Assumes input Blender Mesh already validated, like by using the
        vertsPerFace static method below.
        I.e., is of supported type, and facing method is supported.
        The color setting should come from the parent Blender Object's
        .color setting."""
        object.__init__(self)
        self.wrappedMesh = bMesh
        self.name = bMesh.name
        self.defaultColor = color
        self.jmeMats = None
        self.jmeTextureState = None
        #print "Instantiated JmeMesh '" + self.getName() + "'"
        self.__vpf = JmeMesh.vertsPerFace(self.wrappedMesh.faces)
        self.blenderVertIndexes = None

    def getName(self): return self.name

    def populateXml(self, tag, autoRotate):
        if self.wrappedMesh.verts == None:
            raise Exception("Mesh '" + self.getName() + "' has None vertexes")
        mesh = self.wrappedMesh.copy()
        # This does do a deep copy! (like we need)
        # Note that the last param here doesn't calculate norms anew, it just
        # transforms them along with vertexes, which is just what we want.
        unify = 3 in self.__vpf and 4 in self.__vpf
        if 4 in self.__vpf and not unify:
            meshType = 'Quad'
        else:
            meshType = 'Tri'
        # TODO:  When iterate through verts to get vert vectors + normals,
        #        do check for None normal and throw if so:
        #   raise Exception("Mesh '"
        #       + self.wrappedMesh.name + "' has a vector with no normal")
        #   This is a Blender convention, not a 3D or JME convention
        #   (requirement for normals).
        tag.name = 'com.jme.scene.' + meshType + 'Mesh'
        if tag.getAttr("name") == None: tag.addAttr("name", self.getName())
        colorTag = None  # so we can tell later on whether we wrote it
        if (self.defaultColor != None and
            (self.defaultColor[0] != 1 or self.defaultColor[1] != 1
                or self.defaultColor[2] != 1 or self.defaultColor[3] != 1)):
            colorTag = _XmlTag("defaultColor")
            tag.addChild(colorTag)
            colorTag.addAttr("r", self.defaultColor[0])
            colorTag.addAttr("g", self.defaultColor[1])
            colorTag.addAttr("b", self.defaultColor[2])
            colorTag.addAttr("a", self.defaultColor[3])

        if self.jmeMats == None and self.jmeTextureState == None:
            # Tough call here.
            # Since default object coloring and vertex coloring are useless
            # with jME lighting enabled, we are disabling lighting if either
            # of these are in use with NO MATERIAL for the Mesh itself.
            # N.b. this may clobber cases where the user depends on material
            # enheritance.  Doing it this way by default because the Blender
            # Gui and renderer do not support material inheritance (though the
            # brand new rendering nodes in v. 2.49 may), so we set the default
            # behavior to what will work for most Blender users.  We should
            # provide an exporter switch for users who want to take advantage
            # of inheritance.
            if colorTag != None or mesh.vertexColors:
                tag.addAttr("lightCombineMode", "Off")
        else:
            rsTag = _XmlTag('renderStateList')
            if self.jmeMats != None:
                for mat in self.jmeMats: rsTag.addChild(mat.getXmlEl())
            tag.addChild(rsTag)
            if self.jmeTextureState != None:
                rsTag.addChild(self.jmeTextureState.getXmlEl())

        # Make a copy so we can easily add verts like a normal Python list
        vertList = []
        vertList += mesh.verts
        vertToColor = None
        vertToUv = None
        faceVertToNewVert = [] # Direct replacement for face[].verts[].index
         # 2-dim array. Only necessary (and only changed) if using vertexColors.
         # Note that though faceVertToNewVert face count and vert count always
         # exactly matches the source mesh.faces.  We just change the
         # destination indexes.
        if mesh.vertexColors:
            vertToColor = {} # Simple modifiedVertIndex -> Color Hash
            #  Temp vars only used in this block:
            origIndColors = {}
            # Nested dict. from OrigIndex -> Color -> NewIndex

            # TODO:  Test for objects with no faces, like curves, points.
            for face in mesh.faces:
                if face.verts == None: continue
                # The face.col reference in the next line WILL THROW
                # if the mesh does not support vert colors
                # (i.e. mesh.vertexColors).
                if len(face.verts) != len(face.col):
                    raise Exception(
                    "Counts of Face vertexes and vertex-colors do not match: "
                        + str(len(face.verts)) + " vs. " + str(len(face.col)))
                if len(face.verts) < 1: continue
                faceVertToNewVert.append([])
                for i in range(len(face.verts)):
                    colKey = (str(face.col[i].r)
                            + "|" + str(face.col[i].g)
                            + "|" + str(face.col[i].b)
                            + "|" + str(face.col[i].a))
                    # Generate key from Color since equality is not implemented
                    # properly for MCols.  I.e. "in" tests fail, etc.
                    if face.verts[i].index in origIndColors:
                        #print ("Checking for " + colKey + " in "
                                #+ str(origIndColors[face.verts[i].index]))
                        if colKey in origIndColors[face.verts[i].index]:
                            #print ("Agreement upon color " + colKey
                                    #+ " for 2 face verts")
                            finalFaceVIndex = \
                              origIndColors[face.verts[i].index][colKey]
                        else:
                            # CREATE NEW VERT COPY!!
                            finalFaceVIndex = len(vertList)
                            vertList.append(UpdatableMVert(face.verts[i],
                                    finalFaceVIndex, mesh))
                            origIndColors[face.verts[i].index][colKey] =\
                                    finalFaceVIndex
                            # Writing the new vert index to the map-map so that
                            # other faces using the same color of the same
                            # original index may re-use it (preceding if block)
                    else:
                        # Only use of vertex (so far), so just save orig index
                        finalFaceVIndex = face.verts[i].index
                        origIndColors[face.verts[i].index] = \
                                { colKey:finalFaceVIndex }
                        # Writing the existing vert index to the map-map so
                        # that other faces using the same color of the same
                        # original index may re-use it.
                    vertToColor[finalFaceVIndex] = face.col[i]
                    faceVertToNewVert[-1].append(finalFaceVIndex)
        else:
            for face in mesh.faces:
                if face.verts == None or len(face.verts) < 1: continue
                faceVertToNewVert.append([])
                for i in range(len(face.verts)):
                    faceVertToNewVert[-1].append(face.verts[i].index)
        # At this point, faceVertToNewVert[][]  contains references to every
        # vertex, both original and new copies.  For shared vertexes, there
        # will be multiple faceVertToNewVert[][] elements pointing to the
        # same vertex, but there will be no vertexes orphaned by faceVert...
        if mesh.faceUV:
            vertToUv = {} # Simple modifiedVertIndex -> [u,v] Vector
            #  Temp vars only used in this block:
            origIndUvs = {}
            # Nested dict. from OrigIndex -> "u|v" -> NewIndex

            # TODO:  Test for objects with no faces, like curves, points.
            firstUvVertexCopy = len(vertList)
            for face in mesh.faces:
                if face.verts == None: continue
                # The face.uv reference in the next line WILL THROW
                # if the mesh does not support uv values
                # (i.e. mesh.faceUV).
                if (len(face.verts) != len(face.uv)):
                    raise Exception(
                    "Counts of Face vertexes and uv-values do not match: "
                        + str(len(face.verts)) + " vs. " + str(len(face.uv)))
                for i in range(len(face.verts)):
                    uvKey = str(face.uv[i].x) + "|" + str(face.uv[i].y)
                    # Compound key to prevent need for nested or 2-dim hash
                    # N.b. we generate the uvKey from the original face data,
                    # since nothing above this level gets changed.
                    vertIndex = faceVertToNewVert[face.index][i]
                    if vertIndex in origIndUvs:
                        if uvKey in origIndUvs[vertIndex]:
                            #print ("Agreement upon uv " + uvKey
                                    #+ " for 2 face verts")
                            finalFaceVIndex = origIndUvs[vertIndex][uvKey]
                        else:
                            # CREATE NEW VERT COPY!!
                            finalFaceVIndex = len(vertList)
                            vertList.append(UpdatableMVert(
                                vertList[vertIndex], finalFaceVIndex, mesh))
                            origIndUvs[vertIndex][uvKey] = finalFaceVIndex
                            # Writing the new vert index to the map-map so that
                            # other faces using the same uv val of the same
                            # original index may re-use it (preceding if block)
                            if vertToColor != None:
                                # Must make a vertToColor mapping for any
                                # new vert.
                                if vertList[-1].origIndex in vertToColor:
                                    vertToColor[finalFaceVIndex] =  \
                                            vertToColor[vertList[-1].origIndex]
                                else:
                                    print ("WARNING:  Original vert "
                                        + str(vertList[-1].origIndex)
                                        + " which was dupped for faceUV, was "
                                        + "assigned no vertex color")
                        faceVertToNewVert[face.index][i] = finalFaceVIndex
                        # Overwriting faceVertToNewVert from vertIndex
                    else:
                        # Only use of vertex (so far), so just save orig index
                        finalFaceVIndex = vertIndex
                        origIndUvs[vertIndex]= { uvKey:finalFaceVIndex }
                        # Writing the existing vert index to the map-map so
                        # that other faces using the same uv val of the same
                        # original index may re-use it.
                    vertToUv[finalFaceVIndex] = face.uv[i]

        if len(mesh.verts) != len(vertList):
            print (str(len(vertList) - len(mesh.verts)) + " verts added:  "
                    + str(len(mesh.verts)) + " -> " + str(len(vertList)))
        coArray = []
        noArray = []
        if vertToColor == None:
            colArray = None
        else:
            colArray = []
        if mesh.faceUV or mesh.vertexUV:
            texArray=[]
        else:
            texArray = None
        nonFacedVertexes = 0
        nonUvVertexes = 0
        self.blenderVertIndexes = []
        for v in vertList:
            if len(self.blenderVertIndexes) < len(mesh.verts):
                # Remove this assertion after confirmed:
                # ARRRG.  Can't determine absolute class name for NMVert!!
                self.blenderVertIndexes.append(v.index)
            else:
                # Remove this assertion after confirmed:
                if not isinstance(v, UpdatableMVert):
                    raise Exception("Vert # "
                            + str(len(self.blenderVertIndexes) - 1)
                            + " not an UpdatableMVert: " + str(type(v)))
                self.blenderVertIndexes.append(v.origIndex)
            coArray.append(v.co.x)
            if autoRotate:
                coArray.append(v.co.z)
                coArray.append(-v.co.y)
            else:
                coArray.append(v.co.y)
                coArray.append(v.co.z)
            noArray.append(v.no.x)
            if autoRotate:
                noArray.append(v.no.z)
                noArray.append(-v.no.y)
            else:
                noArray.append(v.no.y)
                noArray.append(v.no.z)
            # Blender treats +v as up.  That makes sense with gui programming,
            # but the "v" of "uv" goes + down, so we have to correct this.
            # (u,v) = (0,0) = TOP LEFT.
            # This is why both v values are saved as 1 - y.
            if colArray != None:
                if v.index in vertToColor:
                    colArray.append(vertToColor[v.index])
                else:
                    nonFacedVertexes += 1
                    colArray.append(None)  # We signify WHITE by None
            if mesh.vertexUV: 
                # For unknown reason, Blender's Sticky uv vert creation sets
                # values to (-1 to 1) range instead of proper (0 to 1) range.
                texArray.append(v.uvco.x * .5 + .5)
                texArray.append(1. - (v.uvco.y * .5 + .5))
            elif mesh.faceUV: 
                if v.index in vertToUv:
                    uvVert = vertToUv[v.index]
                    texArray.append(uvVert.x)
                    texArray.append(1 - uvVert.y)
                else:
                    nonUvVertexes += 1
                    texArray.append(-1)
                    texArray.append(-1)
        if nonFacedVertexes > 0:
            print ("WARNING: " + str(nonFacedVertexes)
                + " vertexes set to WHITE because no face to derive color from")
        if nonUvVertexes > 0:
            print ("WARNING: " + str(nonUvVertexes)
                + " uv vals set to (-1,-1) because no face to derive uv from")

        vertTag = _XmlTag("vertBuf")
        vertTag.addAttr("data", coArray, 6, 3)
        tag.addChild(vertTag)
        normTag = _XmlTag("normBuf")
        normTag.addAttr("data", noArray, 6, 3)
        tag.addChild(normTag)
        if texArray != None:
            coordsTag = _XmlTag("coords")
            coordsTag.addAttr("data", texArray, 6, 3)
            texCoordsTag = _XmlTag("com.jme.scene.TexCoords", {"perVert":2})
            texCoordsTag.addChild(coordsTag)
            texTag = _XmlTag("texBuf")
            texTag.addChild(texCoordsTag)
            tag.addChild(texTag)
        if colArray != None:
            rgbaArray = []
            for c in colArray:
                if c == None:
                    rgbaArray.append(1)
                    rgbaArray.append(1)
                    rgbaArray.append(1)
                    rgbaArray.append(1)
                else:
                    rgbaArray.append(c.r/255.)
                    rgbaArray.append(c.g/255.)
                    rgbaArray.append(c.b/255.)
                    rgbaArray.append(c.a/255.)
            vertColTag = _XmlTag("colorBuf")
            vertColTag.addAttr("data", rgbaArray, 3, 4)
            tag.addChild(vertColTag)
        if 3 not in self.__vpf and 4 not in self.__vpf: return tag
        faceVertIndexes = []
        for i in range(len(faceVertToNewVert)):
            if unify and len(faceVertToNewVert[i]) == 4:
                faceVertIndexes.append(faceVertToNewVert[i][0])
                faceVertIndexes.append(faceVertToNewVert[i][1])
                faceVertIndexes.append(faceVertToNewVert[i][2])
                faceVertIndexes.append(faceVertToNewVert[i][0])
                faceVertIndexes.append(faceVertToNewVert[i][2])
                faceVertIndexes.append(faceVertToNewVert[i][3])
            else:
                for j in range(len(faceVertToNewVert[i])):
                    faceVertIndexes.append(faceVertToNewVert[i][j])
        indTag = _XmlTag("indexBuffer")
        if len(face.verts) == 4 and not unify: outputVpf = 4
        else: outputVpf = 3
        indTag.addAttr("data", faceVertIndexes, None, outputVpf)
        tag.addChild(indTag)
        return tag

    def vertsPerFace(faces):
        "Static method returns list of unique verts-per-face in the mesh."
        facings = set()
        if faces == None: return facings
        for f in faces:
            if f.verts == None: raise Exception("Face with no vertexes?")
            facings.add(len(f.verts))
        return facings

    vertsPerFace = staticmethod(vertsPerFace)


class JmeBone(object):
    """We make a top level Bone for the entire Armature, for which Blender has
    no corresponding bone.  This is necessary since Blender can have
    "unparented" bones, but JME requires all but the root bone to be
    parented."""

    # IMPORTANT!  It could well be that the rotations of the bones should all
    # be baked into the vertex positions, so that "poses" are all rotations
    # from the zero rotation.  Probably not difficult to do, but can't tell
    # until we get to animating.

    # IMPORTANT!  Since the Blender Python depends on globally unique bone
    # names, we are making use of that to simplify our references.
    # Until this gets validated by NodeTree.uniquify(), this is a danger.
    # This class uses bone names as XML IDs.

    __slots__ = ('matrix', 'children', 'name', 'armaObj',
            'parentBone', 'inverseTotalTrans', 'actions')
    # The childYOffset is a translation added to the translation of all
    # direct child bones.  Since Blender is calculating the vertex weights
    # of the skins, this is the only remaining significance of a bone's
    # length.
    # Our new rootBone gets its transform from the owning Blender Object
    # (not the Armature, because Armatures do not have a transform).
    # Bones corresponding to Blender Bones generate transform from the Bone
    # and its parentBone's attributes.
    # I'd like to name "children" more precisely, like "boneChildren", but
    # need to name it "children" so it can be used for our XML tree recursion.
    # "inverseTotalTrans" name and purpose is copied from the Ogre exporter.

    # For now, we support only a single animation controller. All Actions get
    # added as Animations of that Controller.

    def __init__(self, objOrBone, parentBone=None):
        object.__init__(self)
        blenderChildren = []
        self.parentBone = parentBone
        self.actions = None
        if parentBone == None:
            if isinstance(objOrBone, _Armature.Bone):
                raise Exception("parentBone not specified for internal Bone")
            self.armaObj = objOrBone
            # We need to store the arma Object because it is needed for posing
            arma = self.armaObj.getData(False, True)
            if (not isinstance(arma, _Armature.Armature)):
                raise Exception(
                        "Data object for top-level bone not an Armature: "
                        + str(type(arma)))
            # N.b. this matrix IS NOT READY TO USE until rootBone updates it
            # in getXmlEl()
            self.name = arma.name
            self.matrix = _bmath.Matrix(self.armaObj.mat)
            for childBlenderBone in arma.bones.values():
                # This loop makes all Blender top-level bones into direct
                # children of our new, single topLevel root bone.
                if childBlenderBone.parent == None:
                    blenderChildren.append(childBlenderBone)
        else:
            if not isinstance(objOrBone, _Armature.Bone):
                raise Exception("Internal bone object is not a Blender Bone: "
                        + str(type(objOrBone)))
            self.armaObj = None
            self.name = objOrBone.name
            self.matrix = objOrBone.matrix['ARMATURESPACE']
            blenderChildren = objOrBone.children

        if len(blenderChildren) < 1:
            self.children = None
            return
        self.children = []
        for blenderBone in blenderChildren:
            # Recursive instantiation:
            self.children.append(JmeBone(blenderBone, self))

    def getChildren(self):
        return self.children

    def getName(self):
        return self.name

    def getXmlEl(self, autoRotate, addlTransform, nonDefoBackout):
        """addlTransform is the same for all bones and animation channels of
        an Armature."""
        tag = _XmlTag('com.jme.animation.Bone', {'name':self.getName()})
        tag.addAttr("id", self.getName())
        # The id is not strictly needed if a bone is not skinned
        # and not the target of a bone influence.
        # Since nearly all bones are skinned, easier to id them all.
        boutTrans = self.matrix.copy()
        if nonDefoBackout != None: boutTrans *= nonDefoBackout
        # boutTrans  is the arma space transform without intermediate bones
        # backed out.
        if self.parentBone == None:
            invParentMat = None
        else:
            invParentMat = self.parentBone.inverseTotalTrans
        if (self.parentBone != None and addlTransform != None
                and not _esmath.isIdentity(addlTransform)):
            #print "Applying addl to " + self.getName() + ": " + str(addlTransform)
            self.matrix *= addlTransform
        if not _esmath.isIdentity(self.matrix):
            bindTag = _XmlTag("bindMatrix", {'class':'com.jme.math.Matrix4f'})
            # This makes unconventional sequence 00 10 20 instead of 00 01 02...
            # Can fix that by just swapping the next 2 lines.  Keeping this way
            # for now because that's how XMLExporter writes and it's easier to
            # compare regression runs with the same ordering.
            for y in range(4):
                for x in range(4):
                    if ((x == y and round(self.matrix[x][y], 6) != 1.)
                            or (x != y and round(self.matrix[x][y], 6) != 0.)):
                        bindTag.addAttr(("m" + str(y) + str(x)),
                                self.matrix[x][y], 6)
            tag.addChild(bindTag)
        self.inverseTotalTrans = self.matrix.copy().invert()
            # N.b. .matrix is in an intermediate state here
        if invParentMat != None: self.matrix *= invParentMat
        #if invParentMat != None:
            #if not JmeBone.beenHere:
                #print "SKIPPING invParent transformation..."
                #JmeBone.beenHere = True
            #else:
                #print "FROM " + str(self.matrix)
                #self.matrix *= invParentMat
                #print "TO " + str(self.matrix)

        # Must have calculated self.inverseTotalTrans before recursing,
        # since children use it.
        if self.children != None:
            childrenTag = _XmlTag('children')
            tag.addChild(childrenTag)
            for child in self.children:
                if isinstance(child, JmeBone):
                    childrenTag.addChild(child.getXmlEl(
                        autoRotate, addlTransform, nonDefoBackout))
                elif isinstance(child, JmeNode):
                    child.backoutTransform = boutTrans
                    if autoRotate: child.postFlip = True
                    childrenTag.addChild(child.getXmlEl(autoRotate))
                else:
                    raise Exception("Unexpected child of bone: "
                            + child.getName() + " of type " + str(type(child)))

        autoRotate = False
        # Transforms for skel or animation just will not work right with a
        # proper axis flip.  Until we fix this, bones will always have +Y
        # forward.

        #if self.parentBone != None:
        # Real Blender bones, which have only translation + rotation.
        # Take care of this simpler case first.
        loc = self.matrix.translationPart()
        if not _esmath.floatsEq(loc, 0.):
            if autoRotate:
                hold = loc[1]
                loc[1] = loc[2]
                loc[2] = -hold
            addTranslationEl(tag, loc)
        quatRot = self.matrix.toQuat()
        if (round(quatRot.x, 6) != 0 or round(quatRot.y, 6) != 0
                or round(quatRot.z, 6) != 0 or round(quatRot.w) != 1):
            if autoRotate:
                hold = quatRot.y
                quatRot.y = quatRot.z
                quatRot.z = -hold
            addRotationEl(tag, quatRot)
        # N.b. bones have no scale
        scale = self.matrix.scalePart()
        if not _esmath.floatsEq(scale, 1.):
            if autoRotate:
                hold = scale[1]
                scale[1] = scale[2]
                scale[2] = hold
            print "Setting scale of " + self.getName() + " to " + str(scale)
            # TODO:  Test with symmetrical scaling (sx = sy = sz), and
            # asymmetrical scaling.  Preliminary testing indicates that the
            # latter does not work.
            addScaleEl(tag, scale)
        if self.actions == None: return tag

        gConTag = _XmlTag("geometricalControllers")
        conTag = _XmlTag("com.jme.animation.AnimationController",
                {'repeatType': 1, "id": "AC_" + self.getName()})
        animsTag = _XmlTag("animationSets")

        for anim in self.actions:
            animsTag.addChild(anim.getXmlEl(autoRotate, addlTransform))

        conTag.addChild(animsTag)
        conTag.addChild(_XmlTag('skeleton', {
            'class': "com.jme.animation.Bone", 'ref': self.getName()
        }))
        gConTag.addChild(conTag)
        tag.addChild(gConTag)
        tag.addChild(_XmlTag("animationController", {
            "class":"com.jme.animation.AnimationController",
            "ref": "AC_" + self.getName()
        }))
        return tag

    def addAction(self, actionData):
        if self.actions == None: self.actions = []
        self.actions.append(JmeAnimation(actionData))

    def addChild(self, newChild):
        if not isinstance(newChild, JmeNode):
            raise Exception("Attempted to add unexpected child type to bone: "
                    + newChild.getName() + " of type " + str(type(newChild)))
        if self.children == None: self.children = []
        self.children.append(newChild)


class JmeAnimation(object):
    """We have distinct ActionData and JmeAnimation objects because ActionData
    are just tentative JME animations."""
    __slots__ = ('__data')

    def __init__(self, actionData):
        object.__init__(self)
        self.__data = actionData;

    def getXmlEl(self, autoRotate, addlTransform):
        tag = _XmlTag('com.jme.animation.BoneAnimation', {
                'name': self.__data.getName(),
                'startFrame': self.__data.startFrame,
                'endFrame': self.__data.endFrame
        })
        # endFrame is the 0-based INDEX of the last frame that will play.

        keyframeTimeTag = _XmlTag('keyframeTime')
        keyframeTimeTag.addAttr("data", self.__data.keyframeTimes, 6)
        # keyframe times are relative to Animation start time of 0
        # Normally you should have 0 for first frame.  Otherwise, if
        # interpolation is on, it will interpolate movement backwards to 0.
        # If repeatType is 0, at the final time motion will freeze with the
        # last transform.  If repeatType is 1, motion flips immediately from
        # the final transform to the 0 position.
        if self.__data.interpType == _bInterpTypes.CONST:
            interpolationTypeTag = None
        elif (self.__data.interpType == _bInterpTypes.LINEAR
                or self.__data.interpType == _bInterpTypes.BEZIER):
            interpolationTypeTag = _XmlTag('interpolationType')
            # Use no interpolationType tag for No interpolation, or set a value
            # for each frame: 0 for Linear, 1 for Bezier.
            # SWITCH TO BEZIER ONCE com.jme.animation BEZIER IS FIXED!
            interpTypes = []
            for i in range(len(self.__data.keyframeTimes)): interpTypes.append(0)
            interpolationTypeTag.addAttr("data", interpTypes)
            if self.__data.interpType == _bInterpTypes.BEZIER:
                print("WARNING:  Interp type for action '"
                        + self.__data.getName()
                        + "' stored as LINEAR instead of BEZIER, due to "
                        + "com.jme.animation limitation")
        else:
            raise Exception("Unsupported IPO Interpolation type for '"
                    + self.__data.getName() + "': "
                    + str(self.__data.interpType))

        channelNames = self.__data.getChannelNames()

        transformsTag = _XmlTag('boneTransforms')
        for boneName in channelNames:
            # com.jme.BoneAnimation requires rotations and translations
            # elements, even if the size is 0.  The code below can be
            # streamlined once this is fixed.
            translationsTag = _XmlTag("translations")
            if boneName in self.__data.locs:
                # Sanity check:
                if (len(self.__data.keyframeTimes)
                        ) != len(self.__data.locs[boneName]):
                    raise Exception("Bone " + boneName + " has "
                            + str(len(self.__data.locs[boneName]))
                            + " locs, but there are "
                            + str(len(self.__data.keyframeTimes))
                            + " key frames")
                for loc in self.__data.locs[boneName]:
                    if autoRotate:
                        hold = loc[1]
                        loc[1] = loc[2]
                        loc[2] = -hold
                    addVector3fEl(translationsTag, None, loc)

            rotationsTag = _XmlTag("rotations")
            if boneName in self.__data.rots.keys():
                # Sanity check:
                if (len(self.__data.keyframeTimes)
                        != len(self.__data.rots[boneName])):
                    raise Exception("Bone " + boneName + " has " +
                            + str(len(self._data.rots[boneName]))
                            + " quat rots, but there are "
                            + str(len(self._data.keyframeTimes))
                            + " key frames")
                for quat in self.__data.rots[boneName]:
                    if autoRotate:
                        hold = quat.y
                        quat.y = quat.z
                        quat.z = -hold
                    quatTag = _XmlTag('com.jme.math.Quaternion')
                    quatTag.addAttr("x", quat.x, 6)
                    quatTag.addAttr("y", quat.y, 6)
                    quatTag.addAttr("z", quat.z, 6)
                    quatTag.addAttr("w", quat.w, 6)
                    rotationsTag.addChild(quatTag)

            transformTag = _XmlTag('com.jme.animation.BoneTransform')
            transformTag.addChild(translationsTag)
            transformTag.addChild(rotationsTag)
            transformTag.addChild(_XmlTag('bone', {
                    'class': "com.jme.animation.Bone", 'ref':boneName
            }))
            transformsTag.addChild(transformTag)

        tag.addChild(keyframeTimeTag)
        if interpolationTypeTag != None: tag.addChild(interpolationTypeTag)
        tag.addChild(transformsTag)
        return tag


class JmeSkinAndBone(object):
    """
    This class basically generates a Node parent of a SkinNode + Bone; and
    corresonds to the Blender Armature's parent Object.
    We make a top level Bone for the entire Armature, for which Blender has
    no corresponding bone.  This is necessary since Blender can have
    "unparented" bones, but JME requires all but the root bone to be
    parented.
    Very much want to support multiple Bone and SkinNode child, but for now: No

    This node itself is only for grouping and will never have a transform.
    Any transform for the Blender Armature-parent Object will be assigned to
    our new root bone.  Consequently, the 'backoutTransform' attribute here
    is passed directly to the root bone.

    Mesh objects which are "parented to bone" in Blender are _really_ parented
    to the Bone object here.  If we were to emulate Blender, these meshes
    would actually be parented to this JmeSkinAndBone with just a reference to
    a bone name.

    addlTransform is a shared transform pointer which will EVENTUALLY
    contain a matrix that needs to be applied to all bones.

    'children' is maintained only because it is needed for recursive functions
    of NodeTree.  This is a derived value which is always set to:
        boneTree (element 0) + skin (element 1 if present) + plainChildren
    where plainChildren are normal, non mesh-deforming Objects.
    If and when we support multiple skins, 'skin' here will be changed to list
    'skins'.

    'matrix' does not hold the Armature parent object matrix, but a form of the
    skin Object's matrix.
    """
    __slots__ = ('wrappedObj', 'backoutTransform', 'matrix', 'boneMap', 'skins',
            'plainChildren', 'implClass', 'skinTransferNode',
            'boneTree', 'name', 'children', 'actionDataList', 'maxWeightings')
    WEIGHT_THRESHOLD = .001
    BLENDERTOJME_FLIP_MAT4 = _bmath.RotationMatrix(-90, 4, 'x')
    # boneMap is a flat map of bone names to JmeBones, not blender bones.

    def __init__(self, bObj, maxWeightings, animate, skinTransferNode=False):
        """
        N.b. the bObj param is not the data object (like we take for Mesh
        Objects), but a Blender Object.
        Just as for JmeNodes, we can't set backoutTransform until we know what
        objects are to be exported.
        """
        object.__init__(self)
        self.maxWeightings = maxWeightings
        self.wrappedObj = bObj
        if skinTransferNode:
            self.name = "XFER" + bObj.name
        else:
            self.name = bObj.name
        self.implClass = "com.jme.scene.Node"
        self.skinTransferNode = skinTransferNode
        try:
            icProp = self.wrappedObj.getProperty("jme.implClass")
            if icProp.type == 'STRING': self.implClass = icProp.data
        except Exception, e:
            pass # gets an exceptions.RuntimeError
        self.skins = []
        self.plainChildren = None
        self.actionDataList = None
        if animate and not skinTransferNode: self.__runPoses()
        # Need to run this before instantiating bones, I think
        self.matrix = None

        self.boneTree = JmeBone(bObj)
        if self.boneTree.name == self.name: self.boneTree.name += "SuperBone"
        self.__regenChildren()
        if self.actionDataList != None:
            for data in self.actionDataList: self.boneTree.addAction(data)
        self.backoutTransform = None
        self.boneMap = {}
        # N.b., we populate boneMap BEFORE any mesh children are added, so that
        # the bone map will contain only bones.
        self.__recursiveAddToBoneMap(self.boneTree)
        #for n, b in self.boneMap.iteritems():
            #print "    " + n + " ==> " + b.getName()

    def __regenChildren(self):
        self.children = [self.boneTree]
        self.children += self.skins
        if self.plainChildren != None: self.children += self.plainChildren

    def __recursiveAddToBoneMap(self, jmeBone):
        self.boneMap[jmeBone.getName()] = jmeBone
        if jmeBone.children != None:
            for childBone in jmeBone.children:
                self.__recursiveAddToBoneMap(childBone)

    def setMatrix(self, matrix):
        self.matrix = matrix.copy()

    def __runPoses(self):
        """Execute poses to gather data.
        We do this very early because we need optimized pose data in order to
        structure our wrapper objects as required by com.jme.animation."""

        self.actionDataList = []
        for bAction in _bActionSeq:
            if len(set(bAction.getChannelNames()) - set(
                    self.wrappedObj.getData(False, True).bones.keys())) > 0:
                continue

            bAction.setActive(self.wrappedObj)
            actionData = ActionData(bAction,
                    self.wrappedObj.getData(False, True).bones)
            try:
                if actionData.blenderFrames == None:
                    print "No-op Action '" + actionData.getName() + " skipped"
                    continue
                for frameNum in actionData.blenderFrames:
                    print("Posing " + self.name + "/" + actionData.getName()
                            + " frame #" + str(int(frameNum-1.)) + " @"
                            + str(ActionData.frameRate) + " fps")
                    _bSet('curframe', int(frameNum))
                    _Window.Redraw()
                    self.wrappedObj.evaluatePose(int(frameNum))
                    actionData.addPose(self.wrappedObj.getPose().bones)
                #self.wrappedObj.evaluatePose(ORIG_FRAME_NUM)  Seems unnecessary
                print("Posed Action '" + actionData.getName()
                        + " of Arma " + self.getName() + " at frame times: "
                        + str(actionData.keyframeTimes))
                actionData.cull()
                if actionData.blenderFrames == None:
                    print "Action '" + actionData.getName() + " culled"
            finally:
                actionData.reset(self.wrappedObj.getPose())
                if actionData.blenderFrames != None:
                    self.actionDataList.append(actionData)
                # blenderFrames may == None if no channels are significant to
                # this Armature
                #self.wrappedObj.action = None
                #_bScenes.active.update(1)

    def getName(self): return self.name

    def getImplClass(self): return self.implClass

    def addChild(self, newChild):
        if (newChild.wrappedObj.type != "Mesh"
                and newChild.wrappedObj.type != "Empty"):
            raise Exception(
                    "Don't know how to parent the type of " + newChild.getName()
                    + " to an Armature: " + newChild.wrappedObj.type)
        if newChild.wrappedObj.parentType == _bParentTypes["OBJECT"]:
            if self.skinTransferNode:
                raise Exception("Can't add non-skin child '"
                        + newChild.name + "' to Transfer Node " + getName())
            if self.plainChildren == None: self.plainChildren = []
            self.plainChildren.append(newChild)
        elif (newChild.wrappedObj.parentType == _bParentTypes["ARMATURE"]):
            self.skins.append(newChild)
            if not newChild.join:
                raise Exception("Internal error.  Skin node not joined.")
        elif (newChild.wrappedObj.parentType == _bParentTypes["BONE"]):
            if self.skinTransferNode:
                raise Exception("Can't add non-deforming bone-child '"
                        + newChild.name + "' to Transfer Node "
                        + self.getName())
            if newChild.wrappedObj.parentbonename == None:
                raise Exception("Attempted to add " + newChild.getName()
                        + " to a bone, but no specific bone is named")
            if newChild.wrappedObj.parentbonename not in self.boneMap:
                raise Exception("Parent bone specified by "
                        + newChild.getName() + " is not present: "
                        + newChild.wrappedObj.parentbonename)
            self.boneMap[newChild.wrappedObj.parentbonename].addChild(newChild)
        else:
            raise Exception(
                    "Unexpected parentType for " + newChild.getName() + ": "
                    + newChild.wrappedObj.parentType)
        self.__regenChildren()

    def getXmlEl(self, autoRotate):
        # children = boneTree + [skins] + plainChildren

        addlTransform = self.wrappedObj.mat.copy()
        # We couldn't set backoutTransform until now, because we didn't know
        # if the Skin object or parent Blender object were being exported.
        print "addlTransform WAS " + str(addlTransform)
        # Critical to update addlTransform before any bones are written
        if len(self.skins) > 0:
            print "Adjusting bone transforms to Skin Objects"
            for extraskin in self.skins[1:]:
                if not _esmath.floats2dEq(self.skins[0].wrappedObj.mat,
                        extraskin.wrappedObj.mat):
                    print ("Skin " + self.skins[0].wrappedObj.name + "\n"
                            + str(self.skins[0].wrappedObj.mat) + "\nSkin "
                            + extraskin.wrappedObj.name + "\n"
                            + str(extraskin.wrappedObj.mat))
                    raise Exception(
                        "Skins for same Armature have differing transforms: "
                        + self.skins[0].name + " vs. " + extraskin.name)
            addlTransform *= self.skins[0].wrappedObj.mat.copy().invert()
            # Bones will be relative to the Skin object
        elif self.backoutTransform != None:
            print "Adjusting transforms to some other parent Object"
            addlTransform *= self.backoutTransform.copy().invert()
        print "addlTransform NOW " + str(addlTransform)

        # It is quite a hack, but the best I can do:
        # The armature skin  and bones are not properly axis-flipped.  They
        # just depend on this object to rotate and make them look right.
        # This necessitates other hacks to back out this flip from
        # non-bone/non-skin children (both Armature Object Object children,
        # and non-deforming bone Object children).
        if autoRotate:
            if self.matrix == None:
                self.matrix = JmeSkinAndBone.BLENDERTOJME_FLIP_MAT4
            else:
                self.matrix *= JmeSkinAndBone.BLENDERTOJME_FLIP_MAT4
        if (len(self.skins) < 1
                and self.plainChildren == None and self.matrix == None):
            if self.skinTransferNode:
                raise Exception("Assertion failed.  No skins for "
                        + "skinTransferNode '" + self.getName() + "'")
            return self.boneTree.getXmlEl(
                    autoRotate, addlTransform, self.wrappedObj.mat.copy())
            # Makes for economical, simple XML if only exporting the bones

        tag = _XmlTag(self.getImplClass(), {'name':self.getName()})

        if self.wrappedObj != None:
            udTag = _XmlTag('userData')
            _addPropertiesXml(udTag, self.wrappedObj.game_properties)
            if len(udTag.children) > 0: tag.addChild(udTag)

        if self.matrix != None and not self.skinTransferNode:
            # This block for writing local transforms is copied directly from
            # JmeNode above.  See that section for commentary.
            loc = self.matrix.translationPart()
            if _esmath.floatsEq(loc, 0.): loc = None
            rQuat = self.matrix.toQuat()
            if (round(rQuat.x, 6) == 0 and round(rQuat.y, 6) == 0
                    and round(rQuat.z, 6) == 0 and round(rQuat.w) == 1):
                rQuat = None
                scaleMat = self.matrix
            else:
                scaleMat = self.matrix * (
                        rQuat.copy().inverse().toMatrix().resize4x4())
            scale = [scaleMat[0][0], scaleMat[1][1], scaleMat[2][2]]
            if _esmath.floatsEq(scale, 1.): scale = None
            addTranslationEl(tag, loc)
            addScaleEl(tag, scale)
            addRotationEl(tag, rQuat)

        childrenTag = _XmlTag('children')
        tag.addChild(childrenTag)
        childrenTag.addChild(self.boneTree.getXmlEl(
                autoRotate, addlTransform, self.wrappedObj.mat.copy()))
        
        if self.plainChildren != None:
            for child in self.plainChildren:
                #child.backoutTransform = self.matrix
                if autoRotate: child.postFlip = True
                childrenTag.addChild(child.getXmlEl(autoRotate))
                # Not quite perfect here.
                # With no auto-rotate the chilren Objects have Z forward
                # instead of Y forward like it is in Blender.
                # Other cases work great.
        if len(self.skins) < 1:
            if self.skinTransferNode:
                raise Exception("Assertion failed.  No skins for "
                        + "skinTransferNode '" + self.getName() + "'")
            return tag

        skinNodeTag = _XmlTag('com.jme.animation.SkinNode', {
                'name':self.getName() + "SkinNode" })
        cacheTag = _XmlTag('cache')
        skinNodeTag.addChild(cacheTag)
        skinChildrenTag = _XmlTag('children')
         # Useless but required.  Will only ever have just 1 child.
        skinNodeTag.addChild(skinChildrenTag)
        skinsNodeTag = _XmlTag("com.jme.scene.Node", {
                # Notice the subtle "s" diff:  skinNodeTag vs. skinsNodeTag
                'name':self.getName() + "Skins",
                'id': self.getName() + "Skins"
        })
        skinChildrenTag.addChild(skinsNodeTag) # the only purpose for skinsC...
        skinsChildrenTag = _XmlTag('children')
        skinsNodeTag.addChild(skinsChildrenTag)
        transferNodeRegions = []
        for i in range(len(self.skins)):
            transferNodeRegions.append(self.skins[i].skinRegion)
            self.populateSkinXml(self.skins[i], i, skinsChildrenTag, cacheTag)
        if self.skinTransferNode:
            if len(set(transferNodeRegions)) != 1:
                raise Exception("All skins of a non-exported armature must "
                        + "have the same skin region, but '" + self.getName()
                        + "' has " + str(transferNodeRegions))
            if transferNodeRegions[0] != None:
                skinNodeTag.addAttr("region", transferNodeRegions[0])
        else:
            if (len(set(transferNodeRegions)) > 1
                    or transferNodeRegions[0] != None):
                regionsMap = ""
                for i in range(len(self.skins)):
                    if transferNodeRegions[i] != None:
                        if len(regionsMap) > 0: regionsMap += ","
                        regionsMap += self.skins[i].getName()  \
                                + ":" + transferNodeRegions[i]
                skinNodeTag.addAttr("geometryRegions", regionsMap)
        skinNodeTag.addChild(_XmlTag('skins', {
                "class":"com.jme.scene.Node",
                'ref': self.getName() + "Skins"
        }))
        # Adding this tag last to eliminate possible consequences of using the
        # "reference" too early.  Well... not last any more.
        if self.skinTransferNode:
            skinNodeTag.name = "com.jme.animation.SkinTransferNode"
            return skinNodeTag

        childrenTag.addChild(skinNodeTag)
        skinNodeTag.addChild(_XmlTag('skeleton', {
                "class":"com.jme.animation.Bone",
                "ref":self.boneTree.getName(),
                "name":self.boneTree.getName()
        }))
        return tag

    def populateSkinXml(self, skin, skinIndex, skinsChildrenTag, cacheTag):
        meshChild = None
        for grandChild in skin.children:
            if isinstance(grandChild, JmeMesh):
                meshChild = grandChild
                break
            # Blender doesn't allow > 1 mesh child, so don't need to check that
        if meshChild == None:
            raise Exception(
                    "No child of Skin object is a mesh: " + skin.getName())
        skinChildTag = skin.getXmlEl(False)
        skinsChildrenTag.addChild(skinChildTag)
        mesh = meshChild.wrappedMesh
        vGroups = mesh.getVertGroupNames()
        if vGroups == None or len(vGroups) < 1: return
        vertexWeights = {}
        # jvi is the jME vertBuf/noBuf we write; bvi is the Blender vert index
        for jvi in range(len(meshChild.blenderVertIndexes)):
            bvi = meshChild.blenderVertIndexes[jvi]
            vWeightMap = {}   # Weight for this jvi
            for g in vGroups:
                try:
                    weight = mesh.getVertsFromGroup(g, 1, [bvi])[0][1]
                except Exception, e:
                    continue
                if weight >= JmeSkinAndBone.WEIGHT_THRESHOLD:
                    vWeightMap[g] = weight
            if len(vWeightMap) > 0: vertexWeights[jvi] = vWeightMap
        print (str(len(vertexWeights)) + " verts out of "
                + str(len(meshChild.blenderVertIndexes))
                + " weighted for skin '" + skin.getName() + "'")
        if len(vertexWeights) < 1: return

        salaTag = _XmlTag('SavableArrayListArray_' + str(skinIndex))
        for vi, vWeightMap in vertexWeights.iteritems():
            if len(vWeightMap.values()) > self.maxWeightings:
                allWeights = list(vWeightMap.values())
                allWeights.sort()
                limit = allWeights[-4]
                weightCount = self.maxWeightings
            else:
                limit = 0;
                weightCount = len(vWeightMap)
            salTag = _XmlTag('SavableArrayList_' + str(vi))
            salaTag.addChild(salTag)
            weightSum = 0
            for weight in vWeightMap.itervalues():
                if weight < 0:
                    raise Exception("Negative weight.  Should we support this?")
                if weight >= limit: weightSum += weight
            influenceCount = 0
            for group, weight in vWeightMap.iteritems():
                if weight < limit: continue
                influenceCount += 1
                influenceTag = _XmlTag("com.jme.animation.BoneInfluence", {
                        "boneId":group })
                influenceTag.addAttr("weight", weight/weightSum, 6)
                salTag.addChild(influenceTag)
                influenceTag.addChild(_XmlTag("bone", {
                    "class":"com.jme.animation.Bone", "ref":group
                }))
            # ASSERTION.  For performance reasons, may want to remove this
            # after the Assertion is known to always be true:
            if limit != 0 and influenceCount != self.maxWeightings:
                raise Exception("ASSERTION FAILED.  maxWeightings set to "
                        + str(self.maxWeightings) + ", yet we wrote "
                        + str(influenceCount) + " weight influences for skin '"
                        + skin.getName() + "'")
        cacheTag.addChild(salaTag)


class NodeTree(object):
    """Trivial tree dedicated for JmeNode and JmeSkinAndBone members.
    Add all members to the tree, then call and nest().
    See method descriptions for details."""

    __slots__ = ('__memberMap', '__memberKeys', '__maxWeightings',
            '__matMap1side', '__matMap2side', 'root',
            '__textureHash', '__textureStates', '__exportActions')
    # N.b. __memberMap does not have a member for each node, but a member
    #      for each saved Blender object.
    # __memberKeys is just because Python has no ordered maps/dictionaries
    # We want nodes persisted in a well-defined sequence.
    # __textureHash is named *Hash instead of *Map only to avoid confusion
    # with "texture maps", which these are not.

    def __init__(self, maxWeightings, exportActions):
        object.__init__(self)
        self.__memberMap = {}
        self.__memberKeys = []
        self.__matMap1side = {}
        self.__matMap2side = {}
        self.__textureHash = {}
        self.__textureStates = set()
        self.__maxWeightings = None
        self.root = None
        ActionData.updateFrameRate()
        if not isinstance(maxWeightings, int):
            raise Exception("Illegal form at for weightings value: "
                    + str(maxWeightings))
        if maxWeightings < 1:
            raise Exception("maxWeightings must be > 0")
        self.__maxWeightings = maxWeightings
        self.__exportActions = exportActions

    def includeTex(self, mtex):
        """include* instead of add*, because we don't necessarily 'add'.  We
        will just increment the refCount if it's already in our list.
        Returns new or used JmeTexture."""
        newJmeTexId = JmeTexture.idFor(mtex)
        if newJmeTexId in self.__textureHash:
            jmeTex = self.__textureHash[newJmeTexId]
            jmeTex.refCount += 1
            return jmeTex
        jmeTex = JmeTexture(mtex, newJmeTexId)
        self.__textureHash[newJmeTexId] = jmeTex
        return jmeTex

    def includeJmeTextureList(self, textureList):
        """include* instead of add*, because we don't necessarily 'add'.  We
        will just increment the refCount if it's already in our list.
        Returns new or used JmeTextureState."""
        for texState in self.__textureStates:
            if texState.cf(textureList):
                texState.refCount += 1
                return texState
        texState = JmeTextureState(textureList)
        self.__textureStates.add(texState)
        return texState

    def includeMat(self, bMat, twoSided):
        """include* instead of add*, because we don't necessarily 'add'.  We
        will just increment the refCount if it's already in our list.
        Returns new or used JmeMaterial."""
        if twoSided:
            matMap = self.__matMap2side
        else:
            matMap = self.__matMap1side
        if bMat in matMap:
            jmeMat = matMap[bMat]
            jmeMat.refCount += 1
            return jmeMat
        jmeMat = JmeMaterial(bMat, twoSided)
        matMap[bMat] = jmeMat
        return jmeMat

    def addSupported(self, blenderObj, skipObjs, skipTransferNode=False):
        """Creates a JmeNode or JmeSkinAndBone for the given Blender Object,
        The specified object MUST BE PRE-VALIDATED AS BEING SUPPORTED!"""
        if not JmeNode.supported(blenderObj, skipObjs):
            raise Exception(
                "Internal error:  Script said item supported when it is not: "
                + blenderObj.getName())
        if blenderObj.type == "Armature":
            if self.__maxWeightings == None:
                raise Exception("Assertion failed:  maxWeightings not set")
            self.__memberMap[blenderObj] = JmeSkinAndBone(blenderObj,
                self.__maxWeightings, self.__exportActions, skipTransferNode)
        else:
            if skipTransferNode:
                raise Exception(
                    "Assertion failed.  skipTransferNode set for non-Armature "
                    + blenderObj.name + ":  " + blenderObj.type)
            self.__memberMap[blenderObj] = JmeNode(blenderObj, self)
        self.__memberKeys.append(blenderObj)
        return self.__memberMap[blenderObj]

    # IMPORTANT TODO:  Bones are Spatials too in jME.
    #  Ensure that Bone names are unique.  This is actually required for
    #  animation export consistency, since Blender, amazingly, provides no way
    #  to map action channels to bones other than by the bone name.
    def __uniquifyNames(node, parentName, nameSet):
        """Static private method"""
        # Would like to rename nodes ealier, to that messages (error and
        # otherwise) could reflect the new names, but we can't nest properly
        # until all nodes are added to the tree, and we want tree organization
        # to determine naming precedence.
        if node.name == None:
            raiseException("Node in tree without name!")
        #print "Checking name " + node.name + "..."
        if node.name in nameSet:
            #print "Renaming 2nd node with name " + node.name + "..."
            # TODO:  Make the checks for existing substrings case-insens.
            if (isinstance(node, JmeMesh) and parentName != None
                    and (parentName + "Mesh") not in nameSet):
                node.name = (parentName + "Mesh")
            elif (isinstance(node, JmeMesh) and node.name.count("Mesh") < 1
                    and (node.name + "Mesh") not in nameSet):
                node.name += "Mesh"
            elif (isinstance(node, JmeNode) and node.name.count("Node") < 1
                    and (node.name + "Node") not in nameSet):
                node.name += "Node"
            else:
                node.name += ".uniqd"
                while node.name in nameSet: node.name += ".uniqd"
        nameSet.add(node.name)
        if (isinstance(node, JmeMesh)) or node.children == None: return
        nameToPass = node.name
        for child in node.children:
            NodeTree.__uniquifyNames(child, nameToPass, nameSet)

    def nest(self):
        """
        This method should be renamed.  It does general post-selection work,
        not just nesting.

        addChild()s wherever the wrappedObj's parent is present; adds all
        remaining nodes to the top level; and enforces the tree has a single
        root by adding a top grouping node if necessary.

        Also does some animation work.

        Returns the root node.
        """

        if len(self.__memberKeys) < 1:
            self.root = None
            return self.root

        # Supporting arbitrary nesting of skeleton/anination-related elements
        # would be best, but that is quite impossible with com.jme.animation.
        # Since we can't achieve that goal, I'm restricting things farther for
        # now:  Just to get things working, I'm mandating a Node to
        # aggregrate the Bone + SkinNode.  This prevents general sharing, but
        # is necessary for now, due to the several interdependencies between
        # many objects nested within both Bone and SkinNode.

        parentedBos = []
        for bo in self.__memberKeys:
            if bo.parent != None and bo.parent in self.__memberKeys:
                #print ("Nesting " + bo.getType() + "/" + bo.getName()
                        #+ " to  " + bo.parent.getType() + "/"
                        #+ bo.parent.getName())
                self.__memberMap[bo.parent].addChild(self.__memberMap[bo])
                parentedBos.append(bo)
        for bo in parentedBos:
            del self.__memberMap[bo]
            self.__memberKeys.remove(bo)
        if len(self.__memberKeys) < 1:
            raise Exception("Internal problem.  Tree ate itself.")
        if len(self.__memberKeys) < 2:
            self.root = self.__memberMap.popitem()[1]
            del self.__memberKeys[0]
        else:
            self.root = JmeNode("BlenderObjects")
            for key in self.__memberKeys:
                self.root.addChild(self.__memberMap[key])
        return self.root

    def __inlineBones(self, xml):
        """This accommodates very abnormal and frustrating requirements of
        XMLImporter that where the Controller elements reference bones, only
        the topmost bone may be a reference, the rest must be inline."""
        # 1:  Find all Bones
        # 2:  Narrow to those with geometricalControllers children
        # 3:  For each gC element, store refs to all com.jme.animation.Bone
        #     elements underneath ../children (storing the name).
        # 4:  For every bone element under the gC, swap the
        #     gC/.../bone for the children/.../com.jme.animation.Bone of
        #     same name  Set id and ref attrs.

        print "Inlining bone definitions to satisfy jME XML parser..."
        allBones = xml.tagsMatching("com.jme.animation.Bone", "name")
        geoParentingBones = []
        for bone in allBones:
            for child in bone.children:
                if child.name == "geometricalControllers":
                    geoParentingBones.append(bone)
                    break
        print "Found " + str(len(geoParentingBones)) + " geo-parenting bones"
        for geoBone in geoParentingBones: NodeTree.__inlineGeoBones(geoBone)

    def __inlineGeoBones(geoBone):
        childrenTag = None
        animationSets = None
        for child in geoBone.children:
            if child.name == "children":
                childrenTag = child
            if child.name == "geometricalControllers":
                for grandChild in child.children:
                    if grandChild.name == "com.jme.animation.AnimationController":
                        for greatGrandChild in grandChild.children:
                            if greatGrandChild.name == "animationSets":
                                animationSets = greatGrandChild
        if childrenTag == None:
            raise Exception(
                    "No children element peer of 'geometricalControllers'")
        if animationSets == None:
            raise Exception(
                    "No animationSets for 'geometricalControllers'")
        infiniteLoopCheck = 0
        while True:
            infiniteLoopCheck += 1
            if infiniteLoopCheck > 10000:
                raise Exception("Internal loop problem in __inlineGeoBones")
            boneDefs = {}
            for boneDef in childrenTag.tagsMatching(
                    "com.jme.animation.Bone", "name"):
                boneDefs[boneDef.getAttr("name")] = boneDef
            print("Found " + str(len(boneDefs)) + " SuperBone children "
                    + "bone defs for controller: " + str(boneDefs.keys()))
                # We don't want problems to lock up Blender permanently
            fixed = False
            for boneRef in animationSets.tagsMatchingAny(
                    ["bone", "com.jme.animation.Bone"], "ref"):
                if boneRef.getAttr("ref") in boneDefs:
                    fixed = True
                    boneName = boneRef.getAttr("ref")
                    print ("Moving " + boneName
                            + " def from SuperBone children to a BoneAnimation")
                    boneDef = boneDefs.pop(boneName)
                    boneRef.name = "com.jme.animation.Bone"
                    boneRef.delAttr("class")
                    boneDef.name = "bone"
                    boneDef.addAttr("class", "com.jme.animation.Bone")

                    boneDef.swap(boneRef)
                    # We can only fix one element each time through the main
                    # loop, because the element we exchange may well contain
                    # other boneRefs.
                    break

            # We keep running until a loop has no effect
            if not fixed: return

    def getXml(self, autoRotate):
        if self.root == None and self.nest() == None: return None
        for m in self.__matMap1side.itervalues(): m.written = False
        for m in self.__matMap2side.itervalues(): m.written = False
        for t in self.__textureHash.itervalues(): t.written = False
        for ts in self.__textureStates: ts.written = False
        xml = self.root.getXmlEl(autoRotate)
        self.__inlineBones(xml)
        NodeTree.__uniquifyNames(self.root, None, set())
        return xml

    __uniquifyNames = staticmethod(__uniquifyNames)
    __inlineGeoBones = staticmethod(__inlineGeoBones)


from Blender.Material import Shaders as _bShaders
from bpy.data import scenes as _bScenes
class JmeMaterial(object):
    # May or may not need to redesign with subclasses to handle textures and
    # other material states that are not simply the 4 colors + 2 states + shini.
    "A material definition corresponding to a jME MaterialState."

    __slots__ = ('colorMaterial', 'ambient', 'emissive', 'diffuse', 'written',
            'specular', 'shininess', 'materialFace', 'refCount', 'blenderName')
    AMBIENT_OBJDIF_WEIGHTING = .7
    # This has no effect if Blender World ambient lighting is active.
    # If inactive, this specifies the portion of the generated material ambient
    # color from the material's diffuse color.  The remainder
    # (1.0 - ...WEIGHTING) is white.  This is before the total effect is
    # diminished according to the Blender Material's .amb value.

    def __init__(self, bMat, twoSided):
        "Throws a descriptive UnsupportedException for the obvious reason"

        object.__init__(self)
        self.written = False   # May write refs after written is True
        self.refCount = 0
        self.blenderName = bMat.name

        # Supportability validation
        #if len(bMat.colorband) > 0:  Attribute missing.
        # Would like to check validate the ObColor setting, but can't find it
        # in the API.
        if len(bMat.colorbandDiffuse) > 0:
            raise UnsupportedException("colorbandDiffuse", "any",
                "colorbandDiffuse length: " + str(len(bMat.colorbandDiffuse)))
        if bMat.diffuseShader != _bShaders['DIFFUSE_LAMBERT']:
            raise UnsupportedException("diffuse shader", bMat.diffuseShader,
                    "Only Lambert diffuse supported")
        if (bMat.specShader != _bShaders['SPEC_COOKTORR']
                and bMat.specShader != _bShaders['SPEC_PHONG']):
            # These two make "normal" specular effect, similar to the goal of
            # jME shading.  (Whether that goal is achieved is another question).
            raise UnsupportedException("specular shader", bMat.specShader,
                    "Only Cook-Torr and Phong specular supported")
        if bMat.enableSSS:
            raise UnsupportedException("Subsurface Scattering (sss)")
        # Users should know that we don't support Mirroring, Halo, IPO,
        # lightGroups, etc., so we ignore all of these
        # Mirroring settings include Raytrace, Fresnel, Transp.
        # bMat.glossTra?
        if bMat.rbFriction != 0.5 or bMat.rbRestitution != 0:
            print "WARNING:  Rigid Body settings not supported yet.  Ignoring."
        if bMat.shadAlpha != 1.0:
            raise UnsupportedException("shadow alpha", bMat.shadAlpha)
        if bMat.specTransp != 1.0:
            raise UnsupportedException("specular transp.", bMat.specTransp)

        if twoSided: self.materialFace = "FrontAndBack"
        else: self.materialFace = None

        #softnessPercentage = 1. - (bMat.hard - 1.) / 510.
        # Softness increases specular spread.
        # Unfortunately, it's far from linear, and the Python math functions
        # available are inadequate to normalize this to a ratio we can use.
        # Therefore, for now we are ignoring the harness setting.
        if (abs(bMat.hard - 50)) > 1:
            print ("WARNING: Hardness setting ignored.  " +
                    "Adjust spec setting to compensate")
        self.shininess = 128 - bMat.spec * .5 * 128
        # jME "shininess" is actually UNshininess!

        if bMat.mode & _matModes['VCOL_PAINT']:
            # TODO:  API says replaces "basic colors".  Need to verify that
            # that means diffuse, a.o.t. diffuse + specular + mirror.
            self.colorMaterial = "Diffuse"
        elif bMat.mode & _matModes['VCOL_LIGHT']:
            # TODO:  API says "add... as extra light".  ?  Test.
            # If by this they mean light-source-independent-light, then we
            # want to set this to "Ambient".
            self.colorMaterial = "AmbientAndDiffuse"
        elif bMat.mode & _matModes['TEXFACE']:
            raise Exception("Internal problem.  "
                    + "Should not add Mat node for a Texture Material")
        else:
            self.colorMaterial = None
        self.diffuse = bMat.rgbCol
        self.diffuse.append(bMat.alpha)
        self.specular = bMat.specCol
        self.specular.append(bMat.alpha)
        if bMat.emit == 0.:
            self.emissive = None
        else:
            self.emissive = [bMat.rgbCol[0] * bMat.emit,
                    bMat.rgbCol[1] * bMat.emit,
                    bMat.rgbCol[2] * bMat.emit, bMat.alpha]

        # Ambient setting needs special attention.
        # Blender's ambient value applies the "World Ambient Light" color",
        # not any material color.  So, with a typical R=G=B color in Blender or
        # jME, this will "wash out" the material's explicitly set colors.
        # If the user has set the World ambient light color, this is probably
        # exactly what they want.  If the World ambient light is "off" (by
        # leaving the ambient light color at its default value of 0/0/0), then
        # the user probably has no idea that their explicit mat colors will
        # be weakened according to the mat's amb setting-- to the point
        # that if there is no light falling on the object, it will retain no
        # mat coloring.
        # Therefore, if World ambient light is off, we will make this washing
        # out effect more subtle, by combining diffuse + world lighting.
        world = _bScenes.active.world
        if world == None or _esmath.floatsEq(world.amb, 0., 3):
            # Add some material coloring into gray ambient coloring.  Details
            # above.  Weighted average of mat diffuse coloring + white (the
            # white portion will take the color of ambient-generating lights).
            # Local vars just for conciseness.
            diffuseFactor = JmeMaterial.AMBIENT_OBJDIF_WEIGHTING
            whiteFactor = 1.0 - diffuseFactor
            self.ambient = [
                (diffuseFactor * self.diffuse[0] + whiteFactor) * bMat.amb,
                (diffuseFactor * self.diffuse[1] + whiteFactor) * bMat.amb,
                (diffuseFactor * self.diffuse[2] + whiteFactor) * bMat.amb,
                bMat.alpha]
        else:
            self.ambient = [world.amb[0], world.amb[1], world.amb[2], 1]

    def getXmlEl(self):
        tag = _XmlTag('com.jme.scene.state.MaterialState',
                {'class':"com.jme.scene.state.lwjgl.LWJGLMaterialState"})
        if self.written:
            tag.addAttr('ref', self.blenderName)
            return tag
        self.written = True
        #if self.refCount > 0: tag.addAttr("id", self.blenderName)
        tag.addAttr("id", self.blenderName)
        # Blender users have the ability to use these names, and to pull them
        # in from shared libraries, so it can be useful to propagate the names
        # even if not needed for our own refs.

        tag.addAttr("shininess", self.shininess, 2)
        if self.materialFace != None:
            tag.addAttr("materialFace", self.materialFace)
        if self.colorMaterial != None:
            tag.addAttr("colorMaterial", self.colorMaterial)
        diffuseTag = _XmlTag("diffuse")
        diffuseTag.addAttr("r", self.diffuse[0], 3)
        diffuseTag.addAttr("g", self.diffuse[1], 3)
        diffuseTag.addAttr("b", self.diffuse[2], 3)
        diffuseTag.addAttr("a", self.diffuse[3], 3)
        tag.addChild(diffuseTag)
        ambientTag = _XmlTag("ambient")
        ambientTag.addAttr("r", self.ambient[0], 3)
        ambientTag.addAttr("g", self.ambient[1], 3)
        ambientTag.addAttr("b", self.ambient[2], 3)
        ambientTag.addAttr("a", self.ambient[3], 3)
        tag.addChild(ambientTag)
        if self.emissive != None:
            emissiveTag = _XmlTag("emissive")
            emissiveTag.addAttr("r", self.emissive[0], 3)
            emissiveTag.addAttr("g", self.emissive[1], 3)
            emissiveTag.addAttr("b", self.emissive[2], 3)
            emissiveTag.addAttr("a", self.emissive[3], 3)
            tag.addChild(emissiveTag)
        # TODO:  Consider if it is safe to skip specular tag if it is equal
        # to (0, 0, 0, *).  I think so.
        specularTag = _XmlTag("specular")
        specularTag.addAttr("r", self.specular[0], 3)
        specularTag.addAttr("g", self.specular[1], 3)
        specularTag.addAttr("b", self.specular[2], 3)
        specularTag.addAttr("a", self.specular[3], 3)
        tag.addChild(specularTag)
        return tag


from Blender.Texture import Types as _bTexTypes
from Blender.Texture import ExtendModes as _bTexExtModes
from Blender.Texture import ImageFlags as _bImgFlags
from Blender.Texture import Flags as _bTexFlags
from Blender.Texture import TexCo as _bTexCo
from Blender.Texture import BlendModes as _bBlendModes
from Blender.Texture import Mappings as _bTexMappings
from Blender.Texture import MapTo as _bTexMapTo
class JmeTexture(object):
    """A Texture corresponding to a single jME Texture.
    This encompasses a unique aggregation of a Blender texture-mapper ("MTex")
    + texture ("Texture")."""
    # If you add support for any additional direct MTex attributes, you must
    # add a hash value for it to the idFor() method.
    # (By "direct", I mean excluding .tex, which is uniquized by its name).

    REQUIRED_IMGFLAGS = \
        _bImgFlags['MIPMAP'] | _bImgFlags['USEALPHA'] | _bImgFlags['INTERPOL']

    def supported(mtex):
        """Static method that validates the specified Blender MTex.
        MTex is Blender's Material-specific Texture-holder.
        DOES NOT RETURN A MEANINGFUL VALUE.
        Will return upon validation success.
        Otherwise will raise an UnsupportedException with a detail message."""
        
        tex = mtex.tex
        if tex == None:
            raise UnsupportedExcpeption("texture", tex,
                    "MTex has no target Texture")

        # Would like to support those prohibited in this code block.
        if mtex.colfac != 1.0:
            raise UnsupportedException("MTex colfac", mtex.colfac)
        if mtex.size[0] == None or mtex.size[1] == None or mtex.size[2] == None:
            raise UnsupportedException("A null mtex (scale) size dimension")
        # This is a much more general solution than setting "flip".
        if mtex.ofs[0] == None or mtex.ofs[1] == None or mtex.ofs[2] == None:
            raise UnsupportedException("A null mtex offset dimension")
        if JmeTexture.REQUIRED_IMGFLAGS != tex.imageFlags:
            raise UnsupportedException("Tex Image flags", tex.imageFlags,
            "not USEALPHA + MIPMAP + INTERPOL")

        if tex.image == None:
            raise UnsupportedException(
                    "texture image", tex.image, "no image")
        if tex.image.filename == None:
            raise UnsupportedException("texture image file",
                    tex.image.filename, "no image file name")
        if tex.type != _bTexTypes['IMAGE']:
            raise UnsupportedException("Texture Type", tex.type)
        #print "TEX anti: " + str(tex.anti)  Attr missing???
        if tex.crop != (0.0, 0.0, 1.0, 1.0):
            raise UnsupportedException("Tex cropping extents",
                    str(tex.crop))
        # As implement ExtendModes, remove tests from here:
        if (tex.extend != _bTexExtModes['REPEAT']
                and tex.extend != _bTexExtModes['CLIP']
                and tex.extend != _bTexExtModes['EXTEND']):
            raise UnsupportedException("Tex extend mode", tex.extend)
        if tex.brightness != 1.0:
            raise UnsupportedException("Tex brightness", tex.brightness)
        if tex.calcAlpha != 0:
            raise UnsupportedException( "Tex calcAlpha", tex.calcAlpha)
        if len(tex.colorband) != 0:
            raise UnsupportedException("Tex colorband.len",
                    len(tex.colorband))
        if tex.contrast != 1.0:
            raise UnsupportedException("Tex contrast", tex.contrast)
        if tex.filterSize != 1.0:
            raise UnsupportedException("Tex filterSize" +tex.filterSize)
        # Allow PREVIEW_ALPHA, since Blender dev environment setting.
        if tex.flags & _bTexFlags['FLIPBLEND']:
            raise UnsupportedException("Tex flags", 'FLIPBLEND')
        if tex.flags & _bTexFlags['NEGALPHA']:
            raise UnsupportedException("Tex flags", 'NEGALPHA')
        #if tex.flags & _bTexFlags['CHECKER_ODD']:
            #raise UnsupportedException("Tex flags", 'CHECKER_ODD')
            # This is on by default, but the Blender Gui doesn't show
            # checkering on.  ?
        if tex.flags & _bTexFlags['CHECKER_EVEN']:
            raise UnsupportedException("Tex flags", 'CHECKER_EVEN')
        if tex.flags & _bTexFlags['COLORBAND']:
            raise UnsupportedException("Tex flags", 'COLORBAND')
        if tex.flags & _bTexFlags['REPEAT_XMIR']:
            raise UnsupportedException("Tex flags", 'REPEAT_XMIR')
        if tex.flags & _bTexFlags['REPEAT_YMIR']:
            raise UnsupportedException("Tex flags", 'REPEAT_YMIR')
        if tex.repeat[0] != 1:
            raise UnsupportedException("Tex X-Repeat", tex.repeat[0])
        if tex.repeat[1] != 1:
            raise UnsupportedException("Tex Y-Repeat", tex.repeat[1])
        # Map Input
        if mtex.texco != _bTexCo["STICK"] and mtex.texco != _bTexCo["UV"]:
            raise UnsupportedException("Coordinate Mapping type",
                    mtex.texco)
        # Just ignore uvlayer.  We don't care what layer names the user has
        # defined, nor which they set to active.
        if (mtex.blendmode != _bBlendModes['MIX'] # == jME ApplyMode.Decal
                and mtex.blendmode != _bBlendModes['MULTIPLY'] # Modulate
                and mtex.blendmode != _bBlendModes['ADD']): # Add
            raise UnsupportedException("MTex blendmode", mtex.blendmode)
        if mtex.mapping != _bTexMappings['FLAT']:
            raise UnsupportedException("MTex mapping shape", mtex.mapping)
        if mtex.mapto != _bTexMapTo['COL']:
            raise UnsupportedException("MTex mapTo", mtex.mapto)
        if mtex.correctNor:
            raise UnsupportedException("MTex correctNor", mtex.correctNor)
        if round(mtex.dispfac, 3) != .2:
            raise UnsupportedException("MTex surface disp. fac.",
                    mtex.dispfac)
        if mtex.fromOrig:
            raise UnsupportedException("MTex fromOrig", mtex.fromOrig)
        if mtex.neg: raise UnsupportedException("MTex negate", mtex.neg)
        if mtex.norfac != .5:
            raise UnsupportedException("MTex normal affec factor",
                    mtex.norfac)
        if mtex.stencil:
            raise UnsupportedException("MTex stencil", mtex.stencil)
        if mtex.warpfac != 0.0:
            raise UnsupportedException("MTex warpfac", mtex.warpfac)
        if mtex.xproj != 1:
            raise UnsupportedException("MTex xproj", mtex.xproj)
        if mtex.yproj != 2:
            raise UnsupportedException("MTex yproj" + mtex.yproj)
        if mtex.zproj != 3:
            raise UnsupportedException("MTex zproj" + mtex.zproj)

    def idFor(mtex):
        """Static method that returns a unique JmeTexture id.
        Input MTex must already have been validated."""

        return (hash(mtex.tex.name) +  97 * mtex.size[0] + 89 * mtex.size[1]
                + 83 * mtex.size[2] + 79 * mtex.ofs[0] + 73 * mtex.ofs[1]
                + 71 * mtex.ofs[2] + 67 * mtex.texco + mtex.blendmode * 61)
        # Remember that the mtex.tex.name takes care of all uniqueness for the
        # .tex.  We must be vigilant to add something unique for each direct
        # mtex attribute THAT MAY VARY.  We constrain most mtex values so that
        # they may not vary.

    __slots__ = (
            'written', 'refCount', 'applyMode', 'filepath', 'wrapMode',
            'refid', 'scale', 'translation')
    idFor = staticmethod(idFor)
    supported = staticmethod(supported)

    def __init__(self, mtex, newId):
        "Throws a descriptive UnsupportedException for the obvious reason"
        # TODO:  Look into whether Blender persistence supports saving
        #        image references, so we can share (possibly large) Image
        #        objects instead of loading separate copies from FS for each
        #        JmeTexture.

        object.__init__(self)
        self.written = False   # May write refs after written is True
        self.refCount = 0
        if mtex.blendmode == _bBlendModes['MIX']:
            self.applyMode = "Decal"
        elif mtex.blendmode == _bBlendModes['MULTIPLY']:
            self.applyMode = "Modulate"  # This is the jME default
        elif mtex.blendmode == _bBlendModes['ADD']:
            self.applyMode = "Add"
        else:
            raise Exception("Unexpected blendmode even though pre-validated: "
                    + mtex.blendmode)
        if mtex.tex.extend == _bTexExtModes['REPEAT']:
            self.wrapMode = "Repeat"
        elif mtex.tex.extend == _bTexExtModes['EXTEND']:
            self.wrapMode = "Clamp"
        elif mtex.tex.extend == _bTexExtModes['CLIP']:
            self.wrapMode = "BorderClamp"
            # I haven't got this to reveal the underlaying mat yet, but it's
            # the closes wrap mode we have.  May require the right applyMode.
        else:
            raise Exception("Unexpected extend mode even though pre-validated: "
                    + mtex.tex.extend)
        if (len(mtex.tex.image.filename) > 2
                and mtex.tex.image.filename[0:2] == "//"):
            self.filepath = mtex.tex.image.filename[2:]
        else:
            self.filepath = mtex.tex.image.filename
        if _esmath.floatsEq(mtex.size, 1.):
            self.scale = None
        else:
            self.scale = mtex.size
        if _esmath.floatsEq(mtex.ofs, 0.) and self.scale == None:
            self.translation = None
        else:
            translBase = [mtex.ofs[0], -mtex.ofs[1], mtex.ofs[2]]
            if self.scale == None:
                scalingOffset = [0., 0., 0.]
            else:
                scalingOffset = [.5 - .5 * self.scale[0],
                        .5 + -.5 * self.scale[1],
                        .5 - .5 * self.scale[2]]
            self.translation = [translBase[0] + scalingOffset[0],
                    translBase[1] + scalingOffset[1],
                    translBase[2] + scalingOffset[2]]
        self.refid = newId

    def getXmlEl(self):
        tag = _XmlTag('com.jme.image.Texture2D')
        if self.written:
            tag.addAttr('ref', self.refid)
            return tag
        self.written = True
        if self.refCount > 0: tag.addAttr("id", self.refid)

        tag.addAttr("apply", self.applyMode)
        tag.addAttr("wrapS", self.wrapMode)
        tag.addAttr("wrapT", self.wrapMode)
        addVector3fEl(tag, "translation", self.translation)
        addVector3fEl(tag, "scale", self.scale)
        textureKeyTag = _XmlTag(
                "textureKey", {"class":"com.jme.util.TextureKey"})
        tag.addChild(textureKeyTag)
        textureKeyTag.addAttr("file", self.filepath)
        textureKeyTag.addAttr("protocol", "file")
        return tag


class JmeTextureState(object):
    "A unique list of JmeTextures"

    __slots__ = ('__jmeTextures', 'written', 'refCount')

    def __init__(self, jmeTextures):
        object.__init__(self)
        self.written = False   # May write refs after written is True
        self.__jmeTextures = jmeTextures
        self.refCount = 0

    def cf(self, jmeTextureList):
        if len(self.__jmeTextures) != len(jmeTextureList): return False
        return set(self.__jmeTextures) == set(jmeTextureList)

    def getXmlEl(self):
        tag = _XmlTag('com.jme.scene.state.TextureState',
                {'class':"com.jme.scene.state.lwjgl.LWJGLTextureState"})
        if self.written:
            tag.addAttr('ref', id(self))
            return tag
        self.written = True
        if self.refCount > 0: tag.addAttr("id", id(self))

        textureGroupingTag = _XmlTag("texture")
        tag.addChild(textureGroupingTag)
        for texture in self.__jmeTextures:
            textureGroupingTag.addChild(texture.getXmlEl())
        return tag

class UpdatableMVert:
    #__slots__ = ('co', 'hide', 'index', 'no', 'sel', 'uvco')
    __slots__ = ('co', 'index', 'no', 'uvco', 'origIndex')
    # For efficiency, cut out the attrs that we will never use.
    # Very important to retain the original vertex's uvco, since these are
    # used when writing sticky uv values for dupped verts.
    # Need to keep track of the original index so that loops after the one
    # which added the duplicate can (1) loop through just the original
    # vertexes, then (2) loop through added vertexes, copying results just
    # derived for the original source vertex.

    def __init__(self, mvert, newIndex, mesh):
        object.__init__(self)
        self.co = mvert.co
        #self.hide = mvert.hide
        self.origIndex = mvert.index
        self.index = newIndex
        self.no = mvert.no
        #self.sel = mvert.sel
        if mesh.vertexUV: self.uvco = mvert.uvco


def _addPropertiesXml(parentTag, gameProps):
    """Takes a parentTag so that caller may write jME User Data independently
    of these game properties.
    Automatically filters out all properties with names beginning with 'jme.'.
    """
    stringPropKeys = []
    stringPropVals = []
    intPropKeys = []
    intPropVals = []
    floatPropKeys = []
    floatPropVals = []
    boolPropKeys = []
    boolPropVals = []
    for prop in gameProps:
        if prop.name[:4] == "jme.": continue
        if prop.type == "INT":
            intPropKeys.append(prop.name)
            intPropVals.append(prop.data)
        elif prop.type == "BOOL":
            boolPropKeys.append(prop.name)
            if(prop.data):
                boolPropVals.append("true")
            else:
                boolPropVals.append("false")
        elif prop.type == "FLOAT":
            floatPropKeys.append(prop.name)
            floatPropVals.append(prop.data)
        elif prop.type == "STRING":
            stringPropKeys.append(prop.name)
            stringPropVals.append(prop.data)
        else:
            stringPropKeys.append(prop.name)
            stringPropVals.append(str(prop.data))
    if len(stringPropKeys) > 0:
        savableTag = _XmlTag("Savable",
                {'class':'com.jme.util.export.StringStringMap'})
        entryTag = _XmlTag("MapEntry",
                {'key':'stringSpatialAppAttrs'})
        keysTag = _XmlTag("keys")
        valsTag = _XmlTag("vals")
        parentTag.addChild(entryTag)
        entryTag.addChild(savableTag)
        savableTag.addChild(keysTag)
        savableTag.addChild(valsTag)
        for i in range(len(stringPropKeys)):
            keysTag.addChild(_XmlTag("String_" + str(i),
                    {'value': stringPropKeys[i]}))
            valsTag.addChild(_XmlTag("String_" + str(i),
                    {'value': stringPropVals[i]}))
    if len(intPropKeys) > 0:
        savableTag = _XmlTag("Savable",
                {'class':'com.jme.util.export.StringIntMap'})
        entryTag = _XmlTag("MapEntry",
                {'key':'intSpatialAppAttrs'})
        keysTag = _XmlTag("keys")
        valsTag = _XmlTag("vals", {'data':intPropVals})
        parentTag.addChild(entryTag)
        entryTag.addChild(savableTag)
        savableTag.addChild(keysTag)
        savableTag.addChild(valsTag)
        for i in range(len(intPropKeys)):
            keysTag.addChild(_XmlTag("String_" + str(i),
                    {'value': intPropKeys[i]}))
    if len(boolPropKeys) > 0:
        savableTag = _XmlTag("Savable",
                {'class':'com.jme.util.export.StringBoolMap'})
        entryTag = _XmlTag("MapEntry",
                {'key':'boolSpatialAppAttrs'})
        keysTag = _XmlTag("keys")
        valsTag = _XmlTag("vals", {'data':boolPropVals})
        parentTag.addChild(entryTag)
        entryTag.addChild(savableTag)
        savableTag.addChild(keysTag)
        savableTag.addChild(valsTag)
        for i in range(len(boolPropKeys)):
            keysTag.addChild(_XmlTag("String_" + str(i),
                    {'value': boolPropKeys[i]}))
    if len(floatPropKeys) > 0:
        savableTag = _XmlTag("Savable",
                {'class':'com.jme.util.export.StringFloatMap'})
        entryTag = _XmlTag("MapEntry",
                {'key':'floatSpatialAppAttrs'})
        keysTag = _XmlTag("keys")
        valsTag = _XmlTag("vals", {'data':floatPropVals}, 6)
        parentTag.addChild(entryTag)
        entryTag.addChild(savableTag)
        savableTag.addChild(keysTag)
        savableTag.addChild(valsTag)
        for i in range(len(floatPropKeys)):
            keysTag.addChild(_XmlTag("String_" + str(i),
                    {'value': floatPropKeys[i]}))
