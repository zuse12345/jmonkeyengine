/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.filetypes.actions;

import com.jme3.gde.core.sceneviever.app.SceneViewerApplication;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public final class OpenModel implements ActionListener {

    private final DataObject context;

    public OpenModel(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        Set<FileObject> files = context.files();
        for (Iterator<FileObject> it = files.iterator(); it.hasNext();) {
            FileObject fileObject = it.next();
            final String name=fileObject.getNameExt();
            SceneViewerApplication.getApplication().enqueue(new Callable<Object>() {
                public Object call() throws Exception {
                    SceneViewerApplication.getApplication().showModel(name);
                    return null;
                }
            });
        }
    }
}
