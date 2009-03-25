"""This module provides tandalone (i.e. no third party modules required) XML 
abilities.
Right now only handles WRITING, not reading XML files.
Does not handle DTD at this time, only XML schemas.
XmlFile nly handles one Processing Instruction per document for now, but you
can use XmlTag and PITag to write a document with multiple PIs."""

__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

def escape(text):
    """Emulate xml.sax.saxutils.escape() so we can use this function in
    Blender without requiring external Python modules
    """
    if text.count('&') > 0: text = text.replace('&', '&amp;')
    if text.count('<') > 0: text = text.replace('<', '&lt;')
    if text.count('>') > 0: text = text.replace('>', '&gt;')
    return text

def quoteattr(text):
    """Emulate xml.sax.saxutils.quoteattr() so we can use this function in
    Blender without requiring external Python modules"""
    if not isinstance(text, basestring): text = str(text)
    if text.count('"') > 0 and text.count("'") > 0:
        raise Exception("Attr value contains both types of quoetes: " + text)
        ## TODO:  Escape one of the types of quotes and use that as delim
    if text.count('"') > 0:
        delim = ';'
    else:
        delim = '"'
    return delim + escape(text) + delim

import re
# TODO:  Verify that the re module is included in the Python base.
XML_KEYWORD_RE = re.compile("[a-zA-Z0-9_:][a-zA-Z0-9_:.-]*")
def validateXmlKeyword(word):
    """Throws if the specified word is invalid as an XML element name or
    attribute name"""
    if not XML_KEYWORD_RE.match(word):
        raise Exception("Invalid XML keyword: " + word)

class XmlTag(object):
    """Add attributes, children, text, and comments, then fetch the
    completed tag text.
    Does not support intermixing text and tags.
    Can have text and tags within a single element, but the sequence of
    them are not preserved.
    """

    __slots__ = \
        ['name', '__textLinks', '__children', \
        '__commentLinks', '__curAttrs', 'spacesPerIndent']

    def __init__(self, name, attrs=None):
        object.__init__(self)
        validateXmlKeyword(name)
        self.name = name
        self.__textLinks = []
        self.__commentLinks = []
        self.__children = []
        self.__curAttrs = None
        self.spacesPerIndent = 0
        if attrs: 
            for n, v in attrs.iteritems(): self.addAttr(n, v)

    # Little imperfection in this method.
    # I want to preserve whitespace exactly.  The next tag that gets written
    # is indented according to level.  Need to keep another instance state
    # variable to fix this.
    def addText(self, text):
        self.__textLinks.append(escape(text))

    def addAttr(self, name, val, precision=None):
        # N.b., we store the surrounging quotes with each attr value
        validateXmlKeyword(name)
        if not self.__curAttrs: self.__curAttrs = {}
        if precision:
            self.__curAttrs[name] = quoteattr(str(round(val, precision)))
        else:
            self.__curAttrs[name] = quoteattr(val)

    def addComment(self, text):
        self.__commentLinks.append(escape(text))

    def addChild(self, child):
        # The child XmlTag will validate itself (escape, validateKeyword, etc.)
        self.__children.append(child)

    def __str__(self):
        # Returns this element with no indentaton + children indented 1 level
        bufferLinks = ['<']
        bufferLinks.append(self.name)
        if self.__curAttrs:
            for n, v in self.__curAttrs.iteritems():
                bufferLinks.append(' ' + n + '=' + v)
        if len(self.__textLinks) > 0 or len(self.__children) > 0:
            bufferLinks.append('>')
        else:
            if len(self.__commentLinks) > 0: bufferLinks.append('/>')
        for comment in self.__commentLinks:
            # If this is a 1-line element, then keep the comment(s) lined
            # up with it.  Otherwise, indent alone with other el. contents.
            if len(self.__textLinks) > 0 or len(self.__children) > 0:
                bufferLinks.append(('\n<!-- ' + comment)  \
                  .replace('\n', '\n'+ (' ' * self.spacesPerIndent)) + ' -->')
            else:
                bufferLinks.append(('\n<!-- ' + comment) + ' -->')
        if len(self.__commentLinks) > 0 and len(self.__textLinks) > 0:
            bufferLinks.append('\n')
        for text in self.__textLinks:
            bufferLinks.append(text) # Caller must add their own newlines!
        for child in self.__children:
            child.spacesPerIndent = self.spacesPerIndent
            bufferLinks.append(('\n' + str(child))  \
                .replace('\n', '\n' + (' ' * self.spacesPerIndent)))
        if len(self.__children) > 0: bufferLinks.append('\n')
        if len(self.__textLinks) > 0 or len(self.__children) > 0:
            bufferLinks.append('</' + self.name + '>')
        else:
            if len(self.__commentLinks) < 1: bufferLinks.append('/>')

        return ''.join(bufferLinks)

class PITag(object):
    """XML Process Instruction tag.
    Very similar to the XmlTag class in this module."""

    __slots__ = \
        ['name', '__commentLinks', '__curAttrs']

    def __init__(self, name, attrs=None):
        object.__init__(self)
        validateXmlKeyword(name)
        self.name = name
        self.__commentLinks = []
        self.__curAttrs = None
        if attrs: 
            for n, v in attrs.iteritems(): self.addAttr(n, v)

    def addAttr(self, name, val, precision=None):
        # N.b., we store the surrounging quotes with each attr value
        validateXmlKeyword(name)
        if not self.__curAttrs: self.__curAttrs = {}
        if precision != None:
            self.__curAttrs[name] = quoteattr(str(round(val, precision)))
        else:
            self.__curAttrs[name] = quoteattr(val)

    def addComment(self, text):
        self.__commentLinks.append(escape(text))

    def __str__(self):
        # Returns this element with no indentaton
        bufferLinks = ['<?']
        bufferLinks.append(self.name)
        if self.__curAttrs:
            for n, v in self.__curAttrs.iteritems():
                bufferLinks.append(' ' + n + '=' + v)
        bufferLinks.append('?>')
        for comment in self.__commentLinks:
            bufferLinks.append(('\n<!-- ' + comment) + ' -->')

        return ''.join(bufferLinks)

#from codecs import open      codecs module not available in Blender
class XmlFile(object):
    __slots__ = ['root', 'spacesPerIndent', 'encoding', 'pi', \
            '__commentLinks']

    def __init__(self, root, spacesPerIndent=2, pi=None):
    #def __init__(self, root, spacesPerIndent=2, encoding='utf-8', pi=None):
    # Encoding forced to utf-8 until Blender gives some encoding support
        object.__init__(self)
        self.root = root
        self.spacesPerIndent = spacesPerIndent
        self.encoding = 'utf-8'
        self.__commentLinks = []
        self.pi = pi

    def decoded(self):
        return unicode(self.__str__(), self.encoding)

    def __str__(self):
        pieces = []
        if not self.pi:
            self.pi = PITag("xml", {'version':'1.0', 'encoding':self.encoding})
            # This is the default document Processing Instruction
        for docComment in self.__commentLinks: self.pi.addComment(docComment)
        self.root.spacesPerIndent = self.spacesPerIndent
        return str(self.pi) + '\n\n' + str(self.root)

    def addComment(self, text):
        self.__commentLinks.append(escape(text))
        # Consider whether it is worthwhile to insert these doc-comments
        # above PI comments.

    def setPI(self, pi):
        if self.pi: raise Exception("We only support one PI for now.  " \
                + "Consider wiriting your PIs and Tags directly.")
        self.pi = pi

    def writeFile(self, filePath):
        # When Blender starts including the codecs module, enable this
        # codecs.open() and the writes, and disable the next 3 lines.
        #fileObj = open(filePath, "w", self.encoding).  And import codecs.open.
        #fileObj.write(self.__str__())
        #fileObj.write('\n')
        fileObj = open(filePath, "w")

        #fileObj.write(self.__str__().encode(self.encoding))
        #fileObj.write('\n'.encode(self.encoding))
        # TODO: SERIOUS PROBLEM HERE.  string.encode() is not availabe in
        # Blender, and without codecs, there is no alternative.
        # I want to leave our encoding code in place, for when Blender
        # improves, but it is misleading.  What to do... ?
        fileObj.write(self.__str__())
        fileObj.write('\n')

        fileObj.close()
