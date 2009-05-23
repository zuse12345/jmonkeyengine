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

from jmetest import resFileContent
from jme.xml import *
import unittest
from unittest import *

class Tests(unittest.TestCase):
    __slots__ = ['expectedMix', 'expectedXml', 'expectedPi']

    def setUp(self):
        self.expectedMix = resFileContent("xmldata/mix.xml")
        apple = XmlTag('apple', {'x':'y'})
        rootTag = XmlTag('orange', {'color':'yellow'})
        apple.addComment("Apples are delicious")
        rootTag.addAttr('mass', 3.4, 3)
        rootTag.addText('Some words')
        rootTag.addComment('One comment')
        rootTag.addComment('Another comment')
        rootTag.addChild(XmlTag('peach', {'skin':'fuffy'}))
        rootTag.addChild(XmlTag('pineapple', {'prickley':'true'}))
        rootTag.addChild(apple)
        apple.addChild(XmlTag('grape', {'tasty':'true'}))
        pi = PITag('processInstr', {'version':'1.0'})
        pi.addAttr('encoding', 'UTF-8')
        # Would like to add multiple attrs in constructore, but due to
        # lack of ordered Hash in Python, that would make the output a real
        # mess to test.
        pi.addComment("A doc comment")
        rootTag.spacesPerIndent = 2
        self.expectedXml = rootTag
        self.expectedPi = pi

    def testTagAssembly(self):
        """This tests manually writes the PI and root elements, sets the
        indentation level, encodes"""

        manualXmlOutput = (str(self.expectedPi) + '\n\n'
                + unicode(str(self.expectedXml), 'utf-8') + '\n')
        #print '{' + self.expectedMix + '}'
        #print '[' + manualXmlOutput + ']'
        self.assertEqual(self.expectedMix, manualXmlOutput)

    def testXmlFile(self):
        "This exercises the XmlFile class"

        #print str(XmlFile(rootTag, pi=pi))
        #XmlFile(rootTag, pi=pi).writeFile("/tmp/auto.xml")
        #autoXmlOutput = str(pi) + '\n\n' + str(rootTag) + '\n'
        self.assertEqual(
                self.expectedMix, XmlFile(
                self.expectedXml, pi=self.expectedPi).decoded() + '\n')

    def testAllNodes(self):
        "Tests the XmlFile.allNodes method"
        self.assertEqual(5, len(self.expectedXml.allNodes()))

    def genUnique(self, color, usedColors):
        "Trivial method to generate a unique value"
        while color in usedColors: color += 'x'
        return color

    def testAttrUniqueness(self):
        "This tests enforcement of attribute value uniqueness"

        apple = XmlTag('apple', {'x':'y'})
        rootTag = XmlTag('orange', {'color':'yellow'})
        apple.addComment("Apples are delicious")
        apple.addAttr("color", 'red')
        rootTag.addAttr('mass', 3.4, 3)
        rootTag.addText('Some words')
        rootTag.addComment('One comment')
        rootTag.addComment('Another comment')
        rootTag.addChild(XmlTag('peach', {'skin':'fuffy'}))
        rootTag.addChild(XmlTag('pineapple', {'prickley':'true'}))
        rootTag.addChild(apple)
        rootTag.addChild(XmlTag("pear", {'color':'yellow'}))
        grapeNode = XmlTag('grape', {'tasty':'true'})
        apple.addChild(grapeNode)
        grapeNode.addAttr("color", "yellow")
        rootTag.spacesPerIndent = 2
        #print rootTag
        yellowColors = rootTag.tagsMatching(attrName="color", attrVal="yellow")
        self.assertEqual(3, len(yellowColors))
        allColorTags = rootTag.tagsMatching(attrName="color")
        allColors = []
        for colorTag in allColorTags:
            allColors.append(colorTag.quotedattrs["color"])
        self.assertEquals(4, len(allColors))
        self.assertEquals(2, len(set(allColors)))
        usedColors = set()
        while len(allColorTags) > 0:
            colorTag = allColorTags.pop(0)
            color = colorTag.quotedattrs["color"][1:-1]
            if color in usedColors:
                color = self.genUnique(color, usedColors)
                colorTag.addAttr("color", color)
                usedColors.add(color)
            else: usedColors.add(color)
        allColorTags = rootTag.tagsMatching(attrName="color")
        allColors = []
        for colorTag in allColorTags:
            allColors.append(colorTag.quotedattrs["color"])
        self.assertEquals(4, len(allColors))
        self.assertEquals(4, len(set(allColors)))
        self.assertEquals(4, len(usedColors))

unittest.TextTestRunner()\
        .run(unittest.TestLoader().loadTestsFromTestCase(Tests))
