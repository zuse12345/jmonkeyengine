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

from math import *

class ESQuaternion(object):
    """Quaternions based on the excellent algorithms at
    http://www.euclideanspace.com/"""

    __slots__ = \
        ['x', 'y', 'z', 'w']

    def __init__(self, vectorOrEulerAngles, wOrDegreeUnits=False):
        """
        To specify rotation axis + w, call like:
            ESQuaternion(float[3], w)

        To specify Euler rotations, use one of these invocation patterns:
            ESQuaternion(float[3])        # for angles in radians*
            ESQuaternion(float[3], True)  # for anges in degrees
        Specify Euler rotation angles in Blender's (and www.eclideanspace.com)
        sequence convention of Yaw/Heading, Pitch/Attitude, Roll/Bank.
        The * constructor above is the one to use for blenderObject.rot, since
        (contrary to Blender API docs), the Blender.Object.Object 'rot'
        attribute holds radian Euler angles (in the sequence required here).
        The Euler-to-Quaternion code is a Python port of the first (excellent)
        algorithm at
        http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm
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
        heading = eulers[0]
        pitch = eulers[1]
        bank = eulers[2]
        if wOrDegreeUnits:
            # We work in Radians, so convert input to Radians
            heading = radians(heading)
            pitch = radians(pitch)
            bank = radians(bank)
        c1 = cos(heading/2.)
        s1 = sin(heading/2.)
        c2 = cos(pitch/2.)
        s2 = sin(pitch/2.)
        c3 = cos(bank/2.)
        s3 = sin(bank/2.)
        c1c2 = c1 * c2
        s1s2 = s1 * s2
        self.w =c1c2 * c3 - s1s2 * s3
        self.x =c1c2 * s3 + s1s2 * c3
        self.y =s1 * c2 * c3 + c1 * s2 * s3
        self.z =c1 * s2 * c3 - s1 * c2 * s3

    def asFloatArray(self):
        """Note that this sequence favors jME like x, y, z, w;
        NOT Blender like w, x, y, z."""
        return [self.x, self.y, self.x, self.w]

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
        return "(" + str(self.x) + ', ' + str(self.y) + ', ' + str(self.z) \
                + ") " + str(self.w)

    def __repr__(self):
        return "Rot axis: (" + str(self.x) + ', ' + str(self.y) + ', ' \
                + str(self.z) + "), w: " + str(self.w)
