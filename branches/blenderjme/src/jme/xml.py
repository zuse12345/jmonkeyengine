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

from re import compile as _re_compile
XML_KEYWORD_RE = _re_compile("[a-zA-Z0-9_:][a-zA-Z0-9_:.-]*")
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

    __slots__ = ['name', '__textLinks', 'children',
        '__commentLinks', 'quotedattrs', 'spacesPerIndent', '__attrKeys']
        # The attrKeys is just to preserve the sequence of the attr hash

    def __init__(self, name, attrs=None, attrsPrecision=None):
        """Warning:  Do not use give more than one attr in this cons. if you
        care about the sequence of attrs in the output.
        This is due to Python datatype limitation."""
        object.__init__(self)
        validateXmlKeyword(name)
        self.name = name
        self.__textLinks = []
        self.__commentLinks = []
        self.children = []
        self.quotedattrs = None
        self.__attrKeys = None
        self.spacesPerIndent = 0
        if attrs != None:
            for n, v in attrs.iteritems(): self.addAttr(n, v, attrsPrecision)
            # N.b. we are not just concatenating here.  The attr values are
            # changed by the addAttr method.

    def swap(self, otherTag):
        # "This method can not fix indentation, unfortunately"???? I dunno
        newName = otherTag.name
        newTextLinks = otherTag.__textLinks
        newChildren = otherTag.children
        newCommentLinks = otherTag.__commentLinks
        newQa = otherTag.quotedattrs
        newSpi = otherTag.spacesPerIndent
        newAttrKeys = otherTag.__attrKeys

        otherTag.name = self.name
        otherTag.__textLinks = self.__textLinks
        otherTag.children = self.children
        otherTag.__commentLinks = self.__commentLinks
        otherTag.quotedattrs = self.quotedattrs
        otherTag.spacesPerIndent = self.spacesPerIndent
        otherTag.__attrKeys = self.__attrKeys

        self.name = newName
        self.__textLinks = newTextLinks
        self.children = newChildren
        self.__commentLinks = newCommentLinks
        self.quotedattrs = newQa
        self.spacesPerIndent = newSpi
        self.__attrKeys = newAttrKeys

    def tagsMatching(self, tagName=None, attrName=None, attrVal=None):
        "In Leaf-last order."
        hits = []
        if attrName == None and attrVal != None:
            raise Exception(
                "Must specify an attrName in order to also match an attr value")
        if tagName == None or tagName == self.name:
            if attrName == None:
                hits.append(self)
            elif self.quotedattrs == None or attrName not in self.quotedattrs:
                pass
            elif attrVal == None or self.quotedattrs[attrName][1:-1] == attrVal:
                hits.append(self)
        for child in self.children:
            hits += child.tagsMatching(tagName, attrName, attrVal)
        return hits

    def allNodes(self):
        "Returns flattened list of current node + all descendant nodes."
        nodeList = [self]
        if self.children != None:
            for child in self.children: nodeList += child.allNodes()
        return nodeList

    # Little imperfection in this method.
    # I want to preserve whitespace exactly.  The next tag that gets written
    # is indented according to level.  Need to keep another instance state
    # variable to fix this.
    def addText(self, text):
        self.__textLinks.append(escape(text))

    def delAttr(self, name):
        del self.quotedattrs[name]
        self.__attrKeys.remove(name)

    def addAttr(self, name, val, precision=None, tupleSpacing=None):
        """tupleSpacing inserts an extra spaces to specify tuples.  This is
        only applicable if value is a list type"""
        # N.b., we store the surrounding quotes with each attr value
        validateXmlKeyword(name)
        formatStr = None
        if precision != None:
            #formatStr = "{0:." + str(precision) + "f}"
            # Blender Python doesnt' support string.format() yet.
            formatStr = "%." + str(precision) + "f"
        if self.quotedattrs == None:
            self.quotedattrs = {}
            self.__attrKeys = []
        if isinstance(val, list):
            joinlist = []
            for i in range(len(val)):
                if tupleSpacing and i > 0 and i % tupleSpacing == 0:
                    joinlist.append('')
                if isinstance(val[i], basestring):
                    joinlist.append(val[i])
                else:
                    # Enforce that every element is a str type, as required
                    # by join()
                    if formatStr == None:
                        joinlist.append(str(val[i]))
                    else:
                        #joinlist.append(formatStr.format(val[i]))
                        # See above
                        joinlist.append(formatStr % val[i])
            val = " ".join(joinlist)
        if name in self.__attrKeys: self.__attrKeys.remove(name)
        if formatStr == None or isinstance(val, basestring):
            self.quotedattrs[name] = quoteattr(val)
        else:
            #self.quotedattrs[name] = quoteattr(formatStr.format(val))
            # See above
            self.quotedattrs[name] = quoteattr(formatStr % val)
        self.__attrKeys.append(name)

    def addComment(self, text):
        self.__commentLinks.append(escape(text))

    def addChild(self, child):
        # The child XmlTag will validate itself (escape, validateKeyword, etc.)
        self.children.append(child)

    #def getAttrs():
    # The use case I had for this went away.  May not be necessary.
        #if self.__attrKeys == None: return None
        #map = {}
        #for n in self.__attrKeys:
            #map[n] = dequoteattr(self.quotedattrs[n])
        #return map

    def getAttr(self, key):
        if key not in self.quotedattrs: return None
        quotedAttr = self.quotedattrs[key]
        return quotedAttr[1:-1]

    def __str__(self):
        # Returns this element with no indentaton + children indented 1 level
        bufferLinks = ['<']
        bufferLinks.append(self.name)
        if self.quotedattrs != None:
            for n in self.__attrKeys:
                bufferLinks.append(' ' + n + '=' + self.quotedattrs[n])
        if len(self.__textLinks) > 0 or len(self.children) > 0:
            bufferLinks.append('>')
        else:
            if len(self.__commentLinks) > 0: bufferLinks.append('/>')
        for comment in self.__commentLinks:
            # If this is a 1-line element, then keep the comment(s) lined
            # up with it.  Otherwise, indent alone with other el. contents.
            if len(self.__textLinks) > 0 or len(self.children) > 0:
                bufferLinks.append(('\n<!-- ' + comment)
                  .replace('\n', '\n'+ (' ' * self.spacesPerIndent)) + ' -->')
            else:
                bufferLinks.append(('\n<!-- ' + comment) + ' -->')
        if len(self.__commentLinks) > 0 and len(self.__textLinks) > 0:
            bufferLinks.append('\n')
        bufferLinks += self.__textLinks # Caller must add their own newlines!
        for child in self.children:
            child.spacesPerIndent = self.spacesPerIndent
            bufferLinks.append(('\n' + str(child))
                .replace('\n', '\n' + (' ' * self.spacesPerIndent)))
        if len(self.children) > 0: bufferLinks.append('\n')
        if len(self.__textLinks) > 0 or len(self.children) > 0:
            bufferLinks.append('</' + self.name + '>')
        else:
            if len(self.__commentLinks) < 1: bufferLinks.append('/>')

        return ''.join(bufferLinks)

class PITag(object):
    """XML Process Instruction tag.
    Very similar to the XmlTag class in this module."""

    __slots__ = ['name', '__commentLinks', 'quotedattrs', '__attrKeys']
        # The attrKeys is just to preserve the sequence of the attr hash

    def __init__(self, name, attrs=None, attrsPrecision=None):
        """Warning:  Do not use give more than one attr in this cons. if you
        care about the sequence of attrs in the output.
        This is due to Python datatype limitation."""
        object.__init__(self)
        validateXmlKeyword(name)
        self.name = name
        self.__commentLinks = []
        self.quotedattrs = None
        self.__attrKeys = None
        if attrs != None:
            for n, v in attrs.iteritems(): self.addAttr(n, v, attrsPrecision)
            # N.b. we are not just concatenating here.  The attr values are
            # changed by the addAttr method.

    def addAttr(self, name, val, precision=None, tupleSpacing=None):
        """tupleSpacing inserts an extra spaces to specify tuples.  This is
        only applicable if value is a list type"""
        # N.b., we store the surrounding quotes with each attr value
        validateXmlKeyword(name)
        formatStr = None
        if precision != None:
            #formatStr = "{0:." + str(precision) + "f}"
            # Blender Python doesnt' support string.format() yet.
            formatStr = "%." + str(precision) + "f"
        if self.quotedattrs == None:
            self.quotedattrs = {}
            self.__attrKeys = []
        if isinstance(val, list):
            joinlist = []
            for i in range(len(val)):
                if i > 0 and tupleSpacing: joinlist.append('')
                if isinstance(val[i], basestring):
                    joinlist.append(val[i])
                else:
                    # Enforce that every element is a str type, as required
                    # by join()
                    if formatStr == None:
                        joinlist.append(str(val[i]))
                    else:
                        #joinlist.append(formatStr.format(val[i]))
                        # See above
                        joinlist.append(formatStr % val[i])
            val = " ".join(joinlist)
        if name in self.__attrKeys: self.__attrKeys.remove(name)
        if formatStr == None or isinstance(val, basestring):
            self.quotedattrs[name] = quoteattr(val)
        else:
            #self.quotedattrs[name] = quoteattr(formatStr.format(val))
            # See above
            self.quotedattrs[name] = quoteattr(formatStr % val)
        self.__attrKeys.append(name)

    def addComment(self, text):
        self.__commentLinks.append(escape(text))

    def __str__(self):
        # Returns this element with no indentaton
        bufferLinks = ['<?']
        bufferLinks.append(self.name)
        if self.quotedattrs != None:
            for n in self.__attrKeys:
                bufferLinks.append(' ' + n + '=' + self.quotedattrs[n])
        bufferLinks.append('?>')
        for comment in self.__commentLinks:
            bufferLinks.append(('\n<!-- ' + comment) + ' -->')

        return ''.join(bufferLinks)

#from codecs import open      codecs module not available in Blender
class XmlFile(object):
    __slots__ = ['root', 'spacesPerIndent', 'encoding', 'pi', '__commentLinks']

    def __init__(self, root, spacesPerIndent=2, pi=None):
    #def __init__(self, root, spacesPerIndent=2, encoding='utf-8', pi=None):
    # Encoding forced to utf-8 until Blender gives some encoding support
        object.__init__(self)
        if root == None: raise Exception(
                "XML documents require a root element, but none specified")
        self.root = root
        self.spacesPerIndent = spacesPerIndent
        self.encoding = 'utf-8'
        self.__commentLinks = []
        self.pi = pi

    def decoded(self):
        return unicode(self.__str__(), self.encoding)

    def __str__(self):
        pieces = []
        if self.pi == None:
            self.pi = PITag("xml", {'version':'1.0'})
            self.pi.addAttr('encoding', self.encoding.upper())
            # This is the default document Processing Instruction
        for docComment in self.__commentLinks: self.pi.addComment(docComment)
        self.__commentLinks = []
        self.root.spacesPerIndent = self.spacesPerIndent
        return str(self.pi) + '\n\n' + str(self.root)

    def addComment(self, text):
        self.__commentLinks.append(escape(text))
        # Consider whether it is worthwhile to insert these doc-comments
        # above PI comments.

    def setPI(self, pi):
        if self.pi != None:
            raise Exception("We only support one PI for now.  "
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
