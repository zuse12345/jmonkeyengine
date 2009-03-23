__version__ = '$Revision$'
__date__ = '$Date$'
__author__ = 'Blaine Simpson, blaine (dot) simpson (at) admc (dot) com'
__url__ = 'http://www.jmonkeyengine.com'

import Blender
from Blender import Draw
from Blender import BGL

btnvalClear = False
BTNID_CLEAR = 1
BTNID_SAVE = 2
BTNID_CANCEL = 3

def btnHandler(btnId):
    global btnvalClear
    if btnId == BTNID_CLEAR:
        btnvalClear = not btnvalClear
        print "Toggled to " + str(btnvalClear)
        return
    if btnId == BTNID_SAVE:
        Blender.Window.FileSelector(saveFile, "A Label")
        return
    if btnId == BTNID_CANCEL:
        print "cancelled"
        guiBox.free()
        Draw.Exit()
        return

def inputHandler(eventNum, press): # press is set for mouse movements. ?
    if not press: return
    if eventNum == Draw.ESCKEY:
        print "Got ESC"
        guiBox.free()
        Draw.Exit()

class GuiBox(object):
    __slots__ = ['x', 'y', 'w', 'h', 'imgs']

    def __init__(self, w, h, imgpaths):
        object.__init__(self)
        self.w = w
        self.h = h
        availableW, availableH = Blender.Window.GetAreaSize()
        if (w > availableW) or (h > availableH):
            raise Exception("Current Window not large enough for our Gui")
        self.x = (availableW - self.w) / 2
        self.y = (availableH - self.h) / 2
        self.imgs = []
        for path in imgpaths: self.imgs.append(Blender.Image.Load(path))
        for img in self.imgs: img.glLoad()

    def free(self):
        for img in self.imgs: img.glFree()

    def drawBg(self):
        BGL.glColor3f(.95,.54,.24)
        BGL.glRectf(self.x, self.y, self.x + self.w, self.y + self.h)
        BGL.glColor3f(1, 1, 1)
        BGL.glRectf(self.x + 5, self.y + 5, \
                self.x + self.w - 5, self.y + self.h - 5)

        imgDim = self.imgs[0].getSize()[0]; # Unfortunately [0] == [1]
        imgY = self.y + self.h - imgDim   # Img starts at top of gui box
        imgX = self.x + (self.w - len(self.imgs) * imgDim) / 2
          # Centered horizontally in gui box
        for img in self.imgs:
            BGL.glEnable(BGL.GL_TEXTURE_2D)
            BGL.glBindTexture(BGL.GL_TEXTURE_2D, img.getBindCode())
            BGL.glBegin(BGL.GL_POLYGON)
            BGL.glTexCoord2f(0.0,0.0) 
            BGL.glColor3f(1.0,1.0,1.0)
            BGL.glVertex3f(float(imgX),float(imgY),0.0)
            BGL.glTexCoord2f(1.0,0.0)
            BGL.glColor3f(1.0,1.0,1.0)
            BGL.glVertex3f(float(imgX + imgDim),float(imgY),0.0)
            BGL.glTexCoord2f(1.0,1.0)
            BGL.glColor3f(1.0,1.0,1.0)
            BGL.glVertex3f(float(imgX+imgDim),float(imgY+imgDim),0.0)
            BGL.glTexCoord2f(0.0,1.0)	
            BGL.glColor3f(1.0,1.0,1.0)
            BGL.glVertex3f(float(imgX),float(imgY+imgDim),0.0 )
            imgX += imgDim
            BGL.glEnd() 
            BGL.glDisable(BGL.GL_TEXTURE_2D)

        # Anything done after the image writing writes with BLACK
        #BGL.glColor3f(1., 0., 0.)
        #BGL.glRectf(self.x, y, self.x + self.w, self.y + self.h -  self.imgH)


guiBox = GuiBox(330, 300, ['/home/blaine/.blender/scripts/bj1.png', \
    '/home/blaine/.blender/scripts/bj2.png', \
    '/home/blaine/.blender/scripts/bj3.png', \
    '/home/blaine/.blender/scripts/bj4.png', \
    '/home/blaine/.blender/scripts/bj5.png'])

def saveFile(filepath):
    global btnvalClear
    # Can only get here when our Gui is present, but completely overwritten
    # by the FileSelector window.
    origEditMode = Blender.Window.EditMode()
    if origEditMode != 0: Blender.Window.EditMode(0)
    try:
        print "Attempting to save file '" + filepath + "'"
        if btnvalClear: raise Exception("Fake exception")
        print "Saved file '" + filepath + "'"
        guiBox.free()
        Draw.Exit()
    except Exception as e:
        if 1 == Draw.PupMenu(str(e) + "%t|Abort|Try other settings"):
            guiBox.free()
            Draw.Exit()
            print "Aborted"
        print "Will retry"
    finally:
        print "Finally..."
        if origEditMode != 0: Blender.Window.EditMode(origEditMode)

def drawer():
    global btnvalClear, BTNID_CLEAR, BTNID_SAVE, BTNID_CANCEL, guiBox

    BGL.glClear(BGL.GL_COLOR_BUFFER_BIT)
    guiBox.drawBg()
    Draw.Toggle("Clear function",
            BTNID_CLEAR, guiBox.x + 10, guiBox.y + 200,100,20, btnvalClear)
    Draw.Button("Do something", BTNID_SAVE, \
            guiBox.x + 10, guiBox.y + 50, 100, 20, "Tooltip")
    Draw.Button("Cancel", BTNID_CANCEL, guiBox.x + 10, guiBox.y + 10, 100, 20)


Draw.Register(drawer, inputHandler, btnHandler)

print "Script executed"
