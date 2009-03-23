#!/usr/bin/python

"""The jme Python module root directory must be in PYTHON path (or sys.path)
for this script to work.  Here are the two normal idioms to accomplis this:
To execute this script directly from the command-line:
    PYTHONPATH=. relative/path/to/this/script.py
    # For Windows, use SET to set the PYTHONPATH env variable.
To execute from another python script (or interactively at Python prompt):
    import absolute.module.spec  # For this exact module file
"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

from jmetest import resFileContent
from jme.xml import XmlTag, PITag

print '[' + resFileContent("xmldata/mix.xml", "utf-8") + ']'

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
print '[' + str(pi) + '\n\n' + str(tag) + ']'
