"""
    Bender-environment-specific tests.

    The functions defined here are not automatically loaded info namespaces of
    module files.  Those files must "from... import..." if they want local
    names for these functions.
"""

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

from os.path import abspath, dirname, isfile, join
#from codecs import open            codecs module note available in Blender

__moduleDir = abspath(dirname(__file__))

def __resFileAbsPath(path):
    global __moduleDir
    if path.startswith('/') or path.startswith('\\'):
        raise Exception("Resource file paths should not be absolute: " + path)
    return join(__moduleDir, path)

def resFileAbsPath(path):
    absPath = __resFileAbsPath(path)
    if not isfile(absPath):
        raise Exception("No res file '" + path + "' present in package '" \
                + __name__ + "'")
    return absPath

#def resFileContent(path, encoding='utf-8'):
# Force to UTF until Blender supports encodings
def resFileContent(path):
    """ Examples:
        print thismodule.resFileContent("abc/date.txt")
        print thismodule.resFileContent("extended.txt", "utf-8")
    """
    global __moduleDir
    absPath = resFileAbsPath(path)
    # When Blender starts including the codes module, enable this codes.open()
    # and the following read; and disable the next 2 lines.  Import
    # codes.open too!
    #fileObj = open(join(__moduleDir, path), "r", encoding)
    #retVal = fileObj.read()
    fileObj = open(join(__moduleDir, absPath), "r")
    #retVal = fileObj.read().decode(encoding)
    retVal = unicode(fileObj.read())
    fileObj.close()
    return retVal
