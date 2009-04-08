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

import Blender
from blendertest import resFileAbsPath
Blender.Load(resFileAbsPath("walkbot.blend"))
# Load() wipes the namespace, so it must be done way up here.

import unittest
from unittest import *
from blenderjme import exporter
from blenderjme.exporter import gen
from blendertest import resFileContent

class Tests(unittest.TestCase):
    def setUp(self):
        exporter.recordTimestamp = False

    def testObjectNesting(self):
        """Model has 2 standalone objects separated by translation, with 3
        levels of Blender object nesting, and asymmetrical rotations (of the
        legs)."""
        expectedXml = resFileContent("walkbot-jme.xml")

        xmlFile = gen(True, True)
        self.assertEquals(expectedXml, unicode(str(xmlFile), 'utf-8') + '\n')

unittest.TextTestRunner()\
        .run(unittest.TestLoader().loadTestsFromTestCase(Tests))
