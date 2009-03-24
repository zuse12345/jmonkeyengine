"""
    This package contains eneric JMonkeyEngine functions and classes which may
    be useful to multiple different products.

    The functions defined here are not automatically loaded info namespaces of
    module files.  Those files must "from... import..." if they want local
    names for these functions.
"""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

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
