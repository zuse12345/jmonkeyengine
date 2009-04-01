#!/usr/bin/python

"""This is a test, but can't use unittest, since our goal is to test the
Blender-provided Python environment, yet that environment does not have
the unittest module.
This tests tests dependencies on modules that we do not deliver, which
includes both Blender-provided system modules and the Blender implementation
modules (like Blender.*)."""

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

print "Watch console for errors..."
try:
    import Blender as dummy01
    import Blender.Mathutils as dummy02
    from bpy import data as dummy03
    from datetime import datetime as dummy04
    import math as dummy05
    import os as dummy06
    import os.path as dummy07
    import sys as dummy08
    from sys import exc_info as dummy09
    from traceback import tb_lineno as dummy10
    import re as dummy11

except Exception, e:
    print "External module not available in execution environment."
    raise
