#!/usr/bin/python

"""The jme Python module root directory must be in PYTHON path (or sys.path)
for this script to work.  Here are the two normal idioms to accomplish this:
To execute this script directly from the "src" directory with the command-line:
    PYTHONPATH=. relative/path/to/this/script.py
    # For Windows, use SET to set the PYTHONPATH env variable.
Or run an interactive Python shell or a script that resides right in "src", and
To execute from another python script (or interactively at Python prompt):
    import absolute.module.spec  # For this exact module file
A nice shortcut for the 2nd option is to use the python -c switch, like this:
    cd .../src; python -c 'import absolute.module.spec'
"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

from jmetest import resFileContent
from jme.xml import XmlTag, PITag
import unittest
from unittest import *

class Tests(unittest.TestCase):
    __slots__ = ['expectedMix']

    def setUp(self):
        self.expectedMix = resFileContent("xmldata/mix.xml", "utf-8")

    def testTagAssembly(self):
        """This tests manually writes the PI and root elements, and sets the
        indentation level"""

        apple = XmlTag('apple', {'x':'y'})
        tag = XmlTag('orange', {'color':'yellow'})
        apple.addComment("Apples are delicious")
        tag.addAttr('mass3', 3.4, 3)
        tag.addText('Some words')
        tag.addComment('One comment')
        tag.addComment('Another comment')
        tag.addChild(XmlTag('peach', {'skin':'fuffy'}))
        tag.addChild(XmlTag('pineapple', {'prickley':'true'}))
        tag.addChild(apple)
        apple.addChild(XmlTag('grape', {'tasty':'true'}))
        pi = PITag('processInstr', {'version':'1.0', 'encoding':'UTF-8'})
        pi.addComment("A doc comment")
        tag.spacesPerIndent = 2
        manualXmlOutput = str(pi) + '\n\n' + str(tag) + '\n'
        #print '{' + self.expectedMix + '}'
        #print '[' + manualXmlOutput + ']'
        self.assertEqual(self.expectedMix, manualXmlOutput)

unittest.TextTestRunner()\
        .run(unittest.TestLoader().loadTestsFromTestCase(Tests))
