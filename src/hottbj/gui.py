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

# We purposefully do not handle the case where the user changes or merges the
# Window containing our Gui box.  This is a limitation of the crappy Blender
# window manager.  No user expecting success would do this anyways.

from Blender import Window as _bWindow
from Blender import Draw as _bDraw
from Blender import BGL as _bBGL
from Blender.Image import Load as _bLoad
from bpy import data as _bdata
import exporter as _exporter
from os.path import abspath as _abspath
from os.path import isfile as _isfile
import hottbj
from hottbj.wrapperclasses import JmeNode as _JmeNode
from traceback import tb_lineno as _tb_lineno
from sys import exc_info as _exc_info
import webbrowser as _webbrowser

defaultFilePath = _abspath("default-jme.xml")
helpUrl = "file://" + hottbj.resFileAbsPath("exporter.html")
saveAll = False
xmlFile = None
axisFlip = True
skipObjs = True  # Unsupported Mat Objs
exportActions = True
maxWeightings = 4
BTNID_SAVEALL = 1
BTNID_SAVE = 2
BTNID_CANCEL = 3
BTNID_OVERWRITE = 4
BTNID_FLIP = 5
BTNID_SKIPOBJS = 6
BTNID_HELP = 7
BTNID_MAXWEIGHTINGS = 8
BTNID_EXPORTACTIONS = 9
selCount = None
allCount = None      # Does double-duty.  (allCount != None) means Gui is up.
fileOverwrite = False
maxWeightingsNumber = None

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
    """Generates counts of all exportable objects, and all selected exportable
    objects"""

    global selCount, allCount, skipObjs
    selCount = 0
    allCount = 0
    for o in _bdata.scenes.active.objects:
        if not _JmeNode.supported(o, skipObjs): continue
        allCount = allCount + 1
        if o.sel: selCount = selCount + 1

def btnHandler(btnId):
    global saveAll, xmlFile, defaultFilePath, fileOverwrite, axisFlip, \
            skipObjs, helpUrl, maxWeightings, exportActions
    if btnId == BTNID_SKIPOBJS:
        skipObjs = not skipObjs
        updateExportableCounts()
        _bDraw.Redraw()
        return
    if btnId == BTNID_SAVEALL:
        saveAll = not saveAll
        _bDraw.Redraw()
        return
    if btnId == BTNID_OVERWRITE:
        fileOverwrite = not fileOverwrite
        _bDraw.Redraw()
        return
    if btnId == BTNID_FLIP:
        axisFlip = not axisFlip
        _bDraw.Redraw()
        return
    if btnId == BTNID_SAVE:
        try:
            xmlFile = _exporter.gen(
                    saveAll, axisFlip, skipObjs, maxWeightings, exportActions)
        except Exception, e:
            # Python 2.5 does not support "except X as y:" syntax
            ei = _exc_info()[2]
            while ei:
                print ("  " + ei.tb_frame.f_code.co_filename + ':'
                    + str(_tb_lineno(ei)))
                ei = ei.tb_next
            print e
            if 1 == (_bDraw.PupMenu(str(e)
                    + "%t|Abort export|Try other settings")): exitModule()
            return
        _bWindow.FileSelector(saveFile, "Write XML file", defaultFilePath)
        return
    if btnId == BTNID_HELP:
        _webbrowser.open(helpUrl)
        return
    if btnId == BTNID_CANCEL:
        exitModule()
        return
    if btnId == BTNID_MAXWEIGHTINGS:
        maxWeightings = maxWeightingsNumber.val
        return
    if btnId == BTNID_EXPORTACTIONS:
        exportActions = not exportActions
        _bDraw.Redraw()
        return
    raise Exception("Unexpected button ID: " + btnId)

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
            self.__imgs.append(_bLoad(hottbj.resFileAbsPath(path)))
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
    guiBox = GuiBox(330, 300, ['hottbj-1.png', 'hottbj-2.png',
        'hottbj-3.png', 'hottbj-4.png', 'hottbj-5.png'])

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
    global saveAll, guiBox, selCount, allCount, fileOverwrite, maxWeightings
    global maxWeightingsNumber

    if guiBox == None: mkGuiBox()
    if guiBox.screenTooSmall:
        _bDraw.PupMenu("Window too small.  "
                + "(TIP: Close or enlarge script Window if open)")
        exitModule()
        return
    _bBGL.glClear(_bBGL.GL_COLOR_BUFFER_BIT)
    _bDraw.Label("v. ${product.version}",
            guiBox.x + 10, guiBox.y + 32,157,10)
    guiBox.drawBg()
    _bDraw.PushButton("Cancel", BTNID_CANCEL,
            guiBox.x + 10, guiBox.y + 10, 50, 17, "Exit this exporter")
    _bDraw.PushButton("Help", BTNID_HELP,
            guiBox.x + 280, guiBox.y + 10, 40, 17,
            "Open Help document in web browser")
    _bDraw.Label(" (c) 2009 Blaine Simpson",
            guiBox.x + 170, guiBox.y + 46,145,10)
    _bDraw.Label("+ the jMonkeyEngine team",
            guiBox.x + 160, guiBox.y + 32,157,10)
    if allCount == None:
        # First, regardless of modes, check if any export can possibly succeed.
        canSucceed = False
        for o in _bdata.scenes.active.objects:
            if _JmeNode.supported(o, False):
                canSucceed = True
                break
        if not canSucceed:
            _bDraw.Label("Your scenes contain no",
                    guiBox.x + 15, guiBox.y + 200,200,17)
            _bDraw.Label("export-supported objects",
                    guiBox.x + 15, guiBox.y + 170,200,17)
            return
        updateExportableCounts()
    if saveAll: toggleText = str(allCount) + " in Scene"
    else: toggleText = str(selCount) + " Selected"
    if skipObjs: skipText = "Skip Objs"
    else: skipText = "Skip Mats"
    if fileOverwrite: overwriteText = "Silently"
    else: overwriteText = "Confirm"
    if axisFlip: flipText = "Rotate X"
    else: flipText = "No"
    if exportActions: actionsText = "Include"
    else: actionsText = "Exclude"
    _bDraw.Toggle(toggleText,
            BTNID_SAVEALL, guiBox.x + 15, guiBox.y + 200, 75, 17, saveAll,
            "Choose to export supported SELECTED objects or ALL objects",
            redrawDummy)
            # Would prefer to make a 2-line button, but _bDraw does not
            # support that... or basically anything other than vanilla.
    _bDraw.Toggle(overwriteText, BTNID_OVERWRITE,
            guiBox.x + 15, guiBox.y + 175, 47, 17, fileOverwrite,
            "Whether to confirm before overwriting existing export files")
    _bDraw.Toggle(flipText, BTNID_FLIP,
            guiBox.x + 15, guiBox.y + 150, 55, 17, axisFlip,
            "Rotate X axis -90 degress in export so -Y axis becomes +Z")
    _bDraw.Toggle(skipText, BTNID_SKIPOBJS,
            guiBox.x + 15, guiBox.y + 125, 60, 17, skipObjs,
            "Choose to skip just the Mats or the containing Objs", redrawDummy)
    maxWeightingsNumber = _bDraw.Number("", BTNID_MAXWEIGHTINGS,
            guiBox.x + 15, guiBox.y + 100, 50, 17, maxWeightings, 1, 10000,
            "Max number of the highest weighted bones which can influence an "
            + "animated vertex")
    _bDraw.Toggle(actionsText, BTNID_EXPORTACTIONS,
            guiBox.x + 15, guiBox.y + 75, 50, 17, exportActions,
            "Whether to export all Actions for selected Armatures")
    _bDraw.PushButton("Export", BTNID_SAVE,
            guiBox.x + 150, guiBox.y + 10, 50, 17,
            "Proceed to select file to save to")
    _bDraw.Label("Object(s) to export", guiBox.x + 100, guiBox.y + 200,200,17)
    _bDraw.Label("Export file overwrites",
            guiBox.x + 100, guiBox.y + 175,200,17)
    _bDraw.Label("Make axes jME-conformant",
            guiBox.x + 100, guiBox.y + 150,200,17)
    _bDraw.Label("Unsupported Material-handling",
            guiBox.x + 100, guiBox.y + 125,200,17)
    _bDraw.Label("Max bone weightings/vert.",
            guiBox.x + 100, guiBox.y + 100,200,17)
    _bDraw.Label("Blender animations",
            guiBox.x + 100, guiBox.y + 75,200,17)
