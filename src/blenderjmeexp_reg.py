#!BPY
"""Blender ==> jME XML format Exporter
Name: 'JMonkeyEngine (*-jme.xml)'
Blender: 248
Group: 'Export'
Tip: 'Export to jMonkeyEngine XML file'"""
# TODO:  install archived version of Blender and set the Blender version
# above to the older version that this product works with.

__version__ = '${product.version}'
__date__ = '${build.time}'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'
# The Blender Help browser system parses this file for the __url__ value.
# That system is designed terribly and completely chokes if __url__ is
# derived in any way, so we can't generate the __url__ value.

__bpydoc__ = "Run the exporter and hit the Help button in the Gui."

# Copyright (c) 2009, Blaine Simpson and the jMonkeyEngine Dev Team.
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

from blenderjme import gui
from Blender.Draw import Register

# As there is little point to unit testing this script, it always assumes
# the gui and menu system are present.

Register(gui.drawer, gui.inputHandler, gui.btnHandler)
