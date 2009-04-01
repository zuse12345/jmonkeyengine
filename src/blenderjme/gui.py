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

from Blender import Window as _bWindow
from Blender import Draw as _bDraw
from Blender import BGL as _bBGL
from Blender.Image import Load as _bLoad
from bpy import data as _bdata
import exporter as _exporter
from os.path import abspath as _abspath
from os.path import isfile as _isfile
import blenderjme
from blenderjme.wrapperclasses import JmeNode as _JmeNode
from traceback import tb_lineno as _tb_lineno
from sys import exc_info as _exc_info

defaultFilePath = _abspath("default-jme.xml")
saveAll = False
xmlFile = None
axisFlip = True
BTNID_SAVEALL = 1
BTNID_SAVE = 2
BTNID_CANCEL = 3
BTNID_OVERWRITE = 4
BTNID_FLIP = 5
selCount = None
allCount = None
fileOverwrite = False

def exitModule():
    global guiBox, selCount, allCount
    selCount = None
    allCount = None
    _bDraw.Exit()
    if guiBox != None:
        guiBox.free()
        guiBox = None
    print "Exiting exporter"

def updateExportableCounts():
    """Returns counts of all exportable objects, and all selected exportable
    objects"""

    global selCount, allCount
    selCount = 0
    allCount = 0
    for o in _bdata.objects:
        if _JmeNode.supported(o): allCount = allCount + 1
    for o in _bdata.scenes.active.objects.selected:
        if _JmeNode.supported(o): selCount = selCount + 1

def btnHandler(btnId):
    global saveAll, xmlFile, defaultFilePath, fileOverwrite, axisFlip
    if btnId == BTNID_SAVEALL:
        saveAll = not saveAll
        return
    if btnId == BTNID_OVERWRITE:
        fileOverwrite = not fileOverwrite
        return
    if btnId == BTNID_FLIP:
        axisFlip = not axisFlip
        return
    if btnId == BTNID_SAVE:
        try:
            xmlFile = _exporter.gen(saveAll, axisFlip)
        except Exception, e:
            # Python 2.5 does not support "except X as y:" syntax
            ei = _exc_info()[2]
            while ei:
                print ("  " + ei.tb_frame.f_code.co_filename + ':'
                    + str(_tb_lineno(ei)))
                ei = ei.tb_next
            print e
            if 1 == _bDraw.PupMenu(str(e) + "%t|Abort|Try other settings"):
                exitModule()
            return
        _bWindow.FileSelector(saveFile, "Write XML file", defaultFilePath)
        # TODO:  Upon successful save, store file path to Blender registry
        # so that we can use it as default the next time.
        return
    if btnId == BTNID_CANCEL:
        exitModule()
        return

def inputHandler(eventNum, press): # press is set for mouse movements. ?
    if not press: return
    if eventNum == _bDraw.ESCKEY:
        print "Got ESC"
        exitModule()

class GuiBox(object):
    __slots__ = ['x', 'y', 'w', 'h', '__imgs', '__imgpaths', 'screenTooSmall']

    def __init__(self, w, h, imgpaths):
        object.__init__(self)
        self.w = w
        self.h = h
        availableW, availableH = _bWindow.GetAreaSize()
        self.__imgs = None
        if w > availableW or h > availableH:
            self.screenTooSmall = True
            return
        self.screenTooSmall = False
        self.x = (availableW - self.w) / 2
        self.y = (availableH - self.h) / 2
        self.__imgpaths = imgpaths
        self.__loadImages()

    def __loadImages(self):
        self.__imgs = []
        for path in self.__imgpaths:
            self.__imgs.append(_bLoad(
                blenderjme.resFileAbsPath(path)))
        for img in self.__imgs: img.glLoad()

    def free(self):
        if self.__imgs != None:
            for img in self.__imgs: img.glFree()
        self.__imgs = None

    def drawBg(self):
        if self.__imgs == None: self.__loadImages()
        _bBGL.glColor3f(.95,.54,.24)
        _bBGL.glRectf(self.x, self.y, self.x + self.w, self.y + self.h)
        _bBGL.glColor3f(1, 1, 1)
        _bBGL.glRectf(self.x + 5, self.y + 5,
                self.x + self.w - 5, self.y + self.h - 5)

        imgDim = self.__imgs[0].getSize()[0]; # Unfortunately [0] == [1]
        imgY = self.y + self.h - imgDim   # Img starts at top of gui box
        imgX = self.x + (self.w - len(self.__imgs) * imgDim) / 2
          # Centered horizontally in gui box
        for img in self.__imgs:
            _bBGL.glEnable(_bBGL.GL_TEXTURE_2D)
            _bBGL.glBindTexture(_bBGL.GL_TEXTURE_2D, img.getBindCode())
            _bBGL.glBegin(_bBGL.GL_POLYGON)
            _bBGL.glTexCoord2f(0.0,0.0) 
            _bBGL.glColor3f(1.0,1.0,1.0)
            _bBGL.glVertex3f(float(imgX),float(imgY),0.0)
            _bBGL.glTexCoord2f(1.0,0.0)
            _bBGL.glColor3f(1.0,1.0,1.0)
            _bBGL.glVertex3f(float(imgX + imgDim),float(imgY),0.0)
            _bBGL.glTexCoord2f(1.0,1.0)
            _bBGL.glColor3f(1.0,1.0,1.0)
            _bBGL.glVertex3f(float(imgX+imgDim),float(imgY+imgDim),0.0)
            _bBGL.glTexCoord2f(0.0,1.0)	
            _bBGL.glColor3f(1.0,1.0,1.0)
            _bBGL.glVertex3f(float(imgX),float(imgY+imgDim),0.0 )
            imgX += imgDim
            _bBGL.glEnd() 
            _bBGL.glDisable(_bBGL.GL_TEXTURE_2D)

        # Anything done after the image writing writes with BLACK
        #_bBGL.glColor3f(1., 0., 0.)
        #_bBGL.glRectf(self.x, y, self.x + self.w, self.y + self.h -  self.imgH)


guiBox = None

def mkGuiBox():
    global guiBox
    if guiBox != None:
        raise Exception("Attempted to create 2nd GuiBox.  Ignoring.")
    guiBox = GuiBox(330, 300,
            ['bje1.png', 'bje2.png', 'bje3.png', 'bje4.png', 'bje5.png'])

def redrawDummy(x, y): _bDraw.Redraw()

def saveFile(filepath):
    # Can only get here when our Gui is present, but completely overwritten
    # by the FileSelector window.
    global defaultFilePath, fileOverwrite

    try:
        if filepath.endswith(".blend"):
            raise Exception(
            "You should only save Blender native files with extension '.blend'")
        if _isfile(filepath) and (not fileOverwrite) and (1 != _bDraw.PupMenu(
                "Overwrite '" + filepath + "'?%t|Yes|No")): return
        print "Attempting to save file '" + filepath + "'"
        xmlFile.writeFile(filepath)
        print "Saved file '" + filepath + "'"
        defaultFilePath = filepath
        exitModule()
    except Exception, e:
        # Python 2.5 does not support "except X as y:" syntax
        ei = _exc_info()[2]
        while ei:
            print ("  " + ei.tb_frame.f_code.co_filename + ':'
                + str(_tb_lineno(ei)))
            ei = ei.tb_next
        print e
        if 1 == _bDraw.PupMenu(str(e) + "%t|Abort|Try other settings"):
            exitModule()
            return
        print "Will retry"

def drawer():
    global saveAll, guiBox, selCount, allCount, fileOverwrite

    if guiBox == None: mkGuiBox()
    if guiBox.screenTooSmall:
        _bDraw.PupMenu("Window too small.  (Close script Window if open).")
        exitModule()
        return
    _bBGL.glClear(_bBGL.GL_COLOR_BUFFER_BIT)
    guiBox.drawBg()
    _bDraw.PushButton("Cancel", BTNID_CANCEL,
            guiBox.x + 10, guiBox.y + 10, 50, 20, "Abort export")
    _bDraw.Label(" (c) 2009 Blaine Simpson",
            guiBox.x + 170, guiBox.y + 23,145,10)
    _bDraw.Label("+ the jMonkeyEngine team",
            guiBox.x + 160, guiBox.y + 9,157,10)
    if not allCount: updateExportableCounts()
    if allCount < 1:
        _bDraw.Label("Your scenes contain no",
                guiBox.x + 10, guiBox.y + 200,200,20)
        _bDraw.Label("export-supported objects",
                guiBox.x + 10, guiBox.y + 170,200,20)
        return
    if saveAll:
        toggleText = str(allCount) + " Scene Object(s)"
    else:
        toggleText = str(selCount) + " Selected Object(s)"
    _bDraw.Toggle(toggleText,
            BTNID_SAVEALL, guiBox.x + 10, guiBox.y + 200, 130, 20, saveAll,
            "Choose to export supported SELECTED objects or ALL objects",
            redrawDummy)
            # Would prefer to make a 2-line button, but _bDraw does not
            # support that... or basically anything other than vanilla.
    _bDraw.Toggle("Overwrite", BTNID_OVERWRITE,
            guiBox.x + 10, guiBox.y + 175, 60, 20, fileOverwrite,
            "Silently overwrite export file if it exists beforehand")
    _bDraw.Toggle("Rotate X", BTNID_FLIP,
            guiBox.x + 10, guiBox.y + 150, 55, 20, axisFlip,
            "Rotate X axis -90 degress in export so -Y axis becomes +Z")
    _bDraw.PushButton("Export", BTNID_SAVE,
            guiBox.x + 10, guiBox.y + 50, 50, 20, "Select file to save to")
    _bDraw.Label("Reserved space", guiBox.x + 180, guiBox.y + 150,200,20)
    _bDraw.Label("More space", guiBox.x + 180, guiBox.y + 100,200,20)
