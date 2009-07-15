"""This module provides critical math functionality which is either broken or
missing in Blender."""

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

import math as _math

class ESQuaternion(object):
    """Quaternions based on the excellent algorithms at
    http://www.euclideanspace.com/"""

    __slots__ = ['x', 'y', 'z', 'w']

    def __init__(self, vectorOrEulerAngles, wOrDegreeUnits=False):
        """
        To specify rotation axis + w, call like:
            ESQuaternion(float[3], w)
        That's all there is to that format.  The remainder of this description
        is about the other format.

        To specify Euler rotations, use one of these invocation patterns:
            ESQuaternion(float[3])        # for angles in radians*
            ESQuaternion(float[3], True)  # for anges in degrees
        Specify Euler rotation angles in Blender's (and www.eclideanspace.com)
        sequence convention of Yaw/Heading, Pitch/Attitude, Roll/Bank.
        Note that this is based on the traditional but misplaced (IMO) example
        of an airplane flying to the right (I.e., along X axis).
        I say it is misplaced because nobody thinks of the default local
        forward direction of objects in blender being to the right or left.
        Forward is either towards the viewer or away from the viewer
        (this is how jME interprets Yaw/Heading/Pitch/Attitude/Roll/Bank).

        I recommend that you forget the distraction of these words and just
        consider the axes relative to the object being oriented.
        From the perspective of the object being oriented, Z is forwards and
        backwards (3rd float), X is left and right, and Y is up and down:
        Due to Blender's insufferable quirk to orient scenes to +Y, top-level
        objects in a Blender scene typicall have a rotation transform to point
        them to +Y (so they face the camera, etc.).  Our exporter removes
        this idiocy and points top-level scene objects back to +Z (which would
        be up in a Blender scene, but towards the viewer in reasonable 3D apps).

        The * constructor above is the one to use for blenderObject.rot, since
        (contrary to Blender API docs), the Blender.Object.Object 'rot'
        attribute holds radian Euler angles (in the sequence required here).
        The Euler-to-Quaternion code is a Python port of the first (excellent)
        algorithm at
        http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm

        IMPEMENTATON NOTE:
        It seems that Blender applies the Euler angles in an unusual order,
        not just x axis then y axis then z axis like JME does, and which is
        handled by the http://www.euclideanspace.com algorithms.
        For this reason, the variables eulerX/Y/Z are Blender's X, Y, Z,
        and not the X, Y, Z on the euclideanspace site.  The mapping is
        Blender X -> ES Y; Blender Y -> ES Z; Blender Z -> ES X...
        or maybe it's the inverse of that... I forget now, and it's difficult
        to work it out since euclideanspace calls the heading/altitude/bank
        instead of by the axis names.  :(
        """
        object.__init__(self)
        if not isinstance(wOrDegreeUnits, bool):
            vector = vectorOrEulerAngles
            self.x = vector[0]
            self.y = vector[1]
            self.z = vector[2]
            self.w = wOrDegreeUnits
            return

        eulers = vectorOrEulerAngles

        eulerX = eulers[0]
        eulerY = eulers[1]
        eulerZ = eulers[2]

        if wOrDegreeUnits:
            # We work in Radians, so convert input to Radians
            eulerY = _math.radians(eulerY)
            eulerZ = _math.radians(eulerZ)
            eulerX = _math.radians(eulerX)
        c1 = _math.cos(eulerY/2.)
        s1 = _math.sin(eulerY/2.)
        c2 = _math.cos(eulerZ/2.)
        s2 = _math.sin(eulerZ/2.)
        c3 = _math.cos(eulerX/2.)
        s3 = _math.sin(eulerX/2.)
        c1c2 = c1 * c2
        s1s2 = s1 * s2
        self.w = c1c2 * c3 - s1s2 * s3
        self.x = c1c2 * s3 + s1s2 * c3
        self.y = s1 * c2 * c3 + c1 * s2 * s3
        self.z = c1 * s2 * c3 - s1 * c2 * s3

    def asFloatArray(self):
        """Note that this sequence favors jME like x, y, z, w;
        NOT Blender like w, x, y, z."""
        return [self.x, self.y, self.x, self.w]

    def norm(self):
        return (self.w * self.w + self.x * self.x
                + self.y * self.y + self.z * self.z)

    def inverse(self):
        norm = self.norm()
        if norm <= 0.0: raise Exception("Can't invert Quat w/ norm of " + norm)
        invNorm = 1.0 / norm
        return ESQuaternion([self.x * -invNorm, self.y * -invNorm,
                self.z * -invNorm], self.w * invNorm)

    def equals(self, otherQuat, precision=7):
        """Compare 'this' Quaternion to the specified one, within the specified
        precision."""
        if round(self.x, precision) != round(otherQuat.x, precision):
            return False
        if round(self.y, precision) != round(otherQuat.y, precision):
            return False
        if round(self.z, precision) != round(otherQuat.z, precision):
            return False
        if round(self.w, precision) != round(otherQuat.w, precision):
            return False
        return True

    def __str__(self):
        return ("(" + str(self.x) + ', ' + str(self.y) + ', ' + str(self.z)
                + ") " + str(self.w))

    def __repr__(self):
        return ("Rot axis: (" + str(self.x) + ', ' + str(self.y) + ', '
                + str(self.z) + "), w: " + str(self.w))

def floatsEq(alist, val, precision=6):
    """Returns true if all elements of the specified list are equal to the
    specified value, within the specified precision.
    Input 'precision' is not the number of significant digits, but the number
    of significant digits after the decimal point.
    """
    roundedVal = round(val, precision)
    for i in range(len(alist)):
        if round(alist[i], precision) != roundedVal: return False
    return True

def floats2dEq(a, b, precision=5):
    """Compares two two-dimensional float arrays, to the specified precision,
    and returns True or False.
    Input 'precision' is not the number of significant digits, but the number
    of significant digits after the decimal point.
    """
    if a == None and b == None: return True
    if a == None or b == None: return False
    if len(a) != len(b): return False
    for i in range(len(a)):
        if a[i] == None and b[i] == None: continue
        if a[i] == None or b[i] == None: return False
        for j in range(len(a[i])):
            if a[i][j] == None and b[i][j] == None: continue
            if a[i][j] == None or b[i][j] == None: return False
            if round(a[i][j], precision) != round(b[i][j], precision):
                return False
    return True

def isIdentity(m, precision=5):
    """Returns true if the specified 2-dimensional float matrix is equal to the
    identity matrix of the same size, within the specified precision."""
    for row in range(len(m)):
        for col in range(len(m[row])):
            if row == col:
                if round(m[row][col], precision) != 1.0: return False
            else:
                if round(m[row][col], precision) != 0.0: return False
    return True
