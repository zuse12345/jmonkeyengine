"""
    This package contains <ENTER_DESCRIPTION_HERE>.
"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = '<Your Name>, <your@email.addr>'
__url__ = 'http://www.jmonkeyengine.com'

# Copyright (c) 2009, the jMonkeyEngine team
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
# THIS SOFTWARE IS PROVIDED BY the jMonkeyEngine team
# ''AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL the jMonkeyEngine team
# BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from os.path import abspath as _abspath
from os.path import dirname as _dirname
from os.path import isfile as _isfile
from os.path import join as _join
# from codecs import open as _codecs_open
#   except that codecs module not available in Blender

_moduleDir = _abspath(_dirname(__file__))

def _resFileAbsPath(path):
    global _moduleDir
    if path.startswith('/') or path.startswith('\\'):
        raise Exception("Resource file paths should not be absolute: " + path)
    return _join(_moduleDir, path)

def resFileAbsPath(path):
    absPath = _resFileAbsPath(path)
    if not _isfile(absPath):
        raise Exception("No res file '" + path + "' present in package '"
                + __name__ + "'")
    return absPath

#def resFileContent(path, encoding='utf-8'):
# Forcing UTF for now.  Enable encoding param once Blender supports encodings
def resFileContent(path):
    """ Examples:
        print thismodule.resFileContent("abc/date.txt")
        print thismodule.resFileContent("extended.txt", "utf-8")
    """
    global _moduleDir
    absPath = resFileAbsPath(path)

    # When Blender starts including the codes module, add encoding param above,
    # and enable _codes_open above and use that instead of plain open().
    #fileObj = _codecs_open(_join(_moduleDir, path), "r", encoding)
    #retVal = fileObj.read()
    # This alternative would work, but the codecs reader is better.
    #retVal = fileObj.read().decode(encoding)

    fileObj = open(_join(_moduleDir, absPath), "r")
    retVal = unicode(fileObj.read())
    fileObj.close()
    return retVal
