#!BPY
"""Blender-to-jME XML format Exporter
Name: 'JMonkeyEngine (*-jme.xml)'
Blender: 241
Group: 'Export'"""
# TODO:  install archived version of Blender and set the Blender version
# above to the older version that this product works with.
# TODO:  Change the Group to 'Export' before publish.  It's more convenient
# while doing development to have it by itself in a menu (File/Export is
# rather cluttered).

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

from blenderjmeexp.gui import *

Draw.Register(drawer, inputHandler, btnHandler)
