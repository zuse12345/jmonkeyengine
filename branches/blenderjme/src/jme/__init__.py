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
from codecs import open

__moduleDir = abspath(dirname(__file__))

def resFilePresent(path):
    global __moduleDir
    return isfile(join(__moduleDir, path))

def __validateResFile(path):
    if not resFilePresent(path):
        raise Exception("No res file '" + path + "' present under module '" \
                + dirname(__file__) + "'")

def resFileContent(path, encoding=None):
    """ Examples:
        print thismodule.resFileContent("abc/date.txt")
        print thismodule.resFileContent("extended.txt", "utf-8")
    """
    global __moduleDir
    __validateResFile(path)
    fileObj = open(join(__moduleDir, path), "r", encoding)
    retVal = fileObj.read()
    fileObj.close()
    return retVal
