#!/usr/bin/python

"See the file 'doc/testing.txt' for how to execute"

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

from jmetest import resFileContent
from jme.xml import *
import unittest
from unittest import *

class Tests(unittest.TestCase):
    __slots__ = ['expectedMix']

    def setUp(self):
        self.expectedMix = resFileContent("xmldata/mix.xml")

    def testTagAssembly(self):
        """This tests manually writes the PI and root elements, sets the
        indentation level, encodes"""

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
        pi = PITag('processInstr', {'version':'1.0', 'encoding':'UTF-8'})
        pi.addComment("A doc comment")
        rootTag.spacesPerIndent = 2
        manualXmlOutput = str(pi) + '\n\n' \
                + unicode(str(rootTag), 'utf-8') + '\n'
        #print '{' + self.expectedMix + '}'
        #print '[' + manualXmlOutput + ']'
        self.assertEqual(self.expectedMix, manualXmlOutput)

    def testXmlFile(self):
        "This exercises the XmlFile class"

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
        pi = PITag('processInstr', {'version':'1.0', 'encoding':'UTF-8'})
        pi.addComment("A doc comment")
        #print str(XmlFile(rootTag, pi=pi))
        #XmlFile(rootTag, pi=pi).writeFile("/tmp/auto.xml")
        #autoXmlOutput = str(pi) + '\n\n' + str(rootTag) + '\n'
        self.assertEqual( \
                self.expectedMix, XmlFile(rootTag, pi=pi).decoded() + '\n')

unittest.TextTestRunner()\
        .run(unittest.TestLoader().loadTestsFromTestCase(Tests))

