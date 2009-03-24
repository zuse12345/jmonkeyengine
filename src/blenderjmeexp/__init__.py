"""
    This package contains the Blender-to-jME exporter scripts and resources.

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

def resFilePresent(path):
    global __moduleDir
    return isfile(join(__moduleDir, path))

def __validateResFile(path):
    if not resFilePresent(path):
        raise Exception("No res file '" + path + "' present under module '" \
                + dirname(__file__) + "'")

def resFileContent(path, encoding='utf-8'):
    """ Examples:
        print thismodule.resFileContent("abc/date.txt")
        print thismodule.resFileContent("extended.txt", "utf-8")
    """
    global __moduleDir
    __validateResFile(path)
    # When Blender starts including the codes module, enable this codes.open()
    # and the following read; and disable the next 2 lines.  Import
    # codes.open too!
    #fileObj = open(join(__moduleDir, path), "r", encoding)
    #retVal = fileObj.read()
    fileObj = open(join(__moduleDir, path), "r")
    retVal = fileObj.read().decode(encoding)
    fileObj.close()
    return retVal
