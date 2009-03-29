#!/usr/bin/python

"See the file 'doc/testing.txt' for how to execute"

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

from jme.esmath import *
import unittest
from unittest import *

class Tests(unittest.TestCase):
    def testSingleAxisRotations(self):
        "Test quaternions for rotations around a single axis"
        self.assertTrue(ESQuaternion(
                [0, 0.25881904510252074, 0], 0.9659258262890683).equals(
                ESQuaternion([0, 30, 0], True)))
        # Negavite tests:
        self.assertFalse(ESQuaternion(
                [0, 0.25881804510252074, 0], 0.9659258262890683).equals(
                ESQuaternion([0, 30, 0], True)))
        self.assertFalse(ESQuaternion(
                [0, 0.25881904510252074, 0], 0.9659248262890683).equals(
                ESQuaternion([0, 30, 0], True)))
        self.assertFalse(ESQuaternion(
                [0, 0.25881904510252074, 0], 0.9659258262890683).equals(
                ESQuaternion([0, 30.001, 0], True)))

    def testMixedRotation(self):
        "Test quaternions representing complex rotations"
        self.assertTrue(ESQuaternion(
                [0.36758011983238364, 0.24479231586341083,
                0.18214796572990116], 0.8785122060499201).equals(
                ESQuaternion([40, 20, 30], True)))
        # Negative tests.  Switch each parameter, in turn, by 1/1,000,000th
        self.assertFalse(ESQuaternion(
                [0.36758111983238364, 0.24479231586341083,
                0.18214796572990116], 0.8785122060499201).equals(
                ESQuaternion([40, 20, 30], True)))
        self.assertFalse(ESQuaternion(
                [0.36758111983238364, 0.24479331586341083,
                0.18214796572990116], 0.8785122060499201).equals(
                ESQuaternion([40, 20, 30], True)))
        self.assertFalse(ESQuaternion(
                [0.36758011983238364, 0.24479231586341083,
                0.18214896572990116], 0.8785122060499201).equals(
                ESQuaternion([40, 20, 30], True)))
        self.assertFalse(ESQuaternion(
                [0.36758011983238364, 0.24479231586341083,
                0.18214796572990116], 0.8785132060499201).equals(
                ESQuaternion([40, 20, 30], True)))


    def testSuperRotation(self):
        "Test rotations over 2 pi / 360 degrees"
        # +/ multiples of 4 pi, 720 degrees have no effect
        self.assertTrue(ESQuaternion(
                [0.36758011983238364, 0.24479231586341083,
                0.18214796572990116], 0.8785122060499201).equals(
                ESQuaternion([760, 740, 750], True)))
        # Not sure of all combinations, but it looks like each +/ 360 degrees
        # changes the sign of every resultant component
        self.assertTrue(ESQuaternion(
                [-0.36758011983238364, -0.24479231586341083,
                -0.18214796572990116], -0.8785122060499201).equals(
                ESQuaternion([400, 380, 390], True)))
        self.assertTrue(ESQuaternion(
                [-0.36758011983238364, -0.24479231586341083,
                -0.18214796572990116], -0.8785122060499201).equals(
                ESQuaternion([40, 380, 30], True)))

    def testNegativeRotation(self):
        "Test rotations in the negative direction"
        self.assertTrue(ESQuaternion(
                [-0.36758011983238364, -0.24479231586341083,
                -0.18214796572990116], -0.8785122060499201).equals(
                ESQuaternion([-320, -340, -330], True)))

unittest.TextTestRunner()\
        .run(unittest.TestLoader().loadTestsFromTestCase(Tests))
