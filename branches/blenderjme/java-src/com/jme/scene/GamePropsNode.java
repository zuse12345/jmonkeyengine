/*
 * @(#)$Id$
 *
 * Copyright (c) 2009, Blaine Simpson and the jMonkeyEngine Dev Team.
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


package com.jme.scene;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;

public class GamePropsNode extends Node {
    protected Map<String, String> gameProperties = null;

    public Map<String, String> getGameProperties() { return gameProperties; }
    public void setGameProperties(Map<String, String> gameProperties) {
        this.gameProperties =  gameProperties;
    }

    public String getGameProperty(String key) {
        if (gameProperties == null) return null;
        return gameProperties.get(key);
    }

    public void addGameProperty(String key, String val) {
        if (gameProperties == null)
            gameProperties = new HashMap<String, String>();
        gameProperties.put(key, val);
    }

    public GamePropsNode() {
        super();
        System.err.println("No-param " + GamePropsNode.class.getName()
                + " instantiated");
    }
    public GamePropsNode(String s) { super(s);
        // REMOVE THIS BLOCK. JUST FOR TESTING:
        addGameProperty("Nombre", s.toUpperCase());
        addGameProperty("Secundo", "2nd");

        System.err.println(GamePropsNode.class.getName() + " (" + getName()
                + ") instantiated");
    }

    public void write(JMEExporter e) throws IOException {
        super.write(e);
        if (gameProperties != null) {
            // TODO:  Verify that parallel sequence is guaranteed between
            // HashMap.keySet() and HashMap.values().
            List<String> propStrings =
                    new ArrayList<String>(2 * gameProperties.size());
            /*
             * This is much faster from Java, but not as good for
             * Blender/Python, and, more importantly, makes it much harder to
             * read or manually edit settings in the *-jme.xml files.
            propStrings.addAll(gameProperties.keySet());
            propStrings.addAll(gameProperties.values());
            */
            for (Map.Entry<String, String> me : gameProperties.entrySet()) {
                propStrings.add(me.getKey());
                propStrings.add(me.getValue());
            }
            e.getCapsule(this) .write(propStrings.toArray(new String[0]),
                    "gameProperties", null);
        }
    }

     public void read(JMEImporter e) throws IOException {
        super.read(e);
        String[] propStrings = e.getCapsule(this).readStringArray(
                "gameProperties", null);
        if (propStrings != null) {
            if (propStrings.length != (propStrings.length/2) * 2)
                throw new IOException(
                        "'gameProperties' String array has uneven size");
            gameProperties = new HashMap<String, String>(propStrings.length/2);
            for (int i = 0; i < propStrings.length; i += 2)
                gameProperties.put(propStrings[i], propStrings[i + 1]);
        }
     }

     /**
      * Just for debugging.  REMOVEME.
      */
     public String toString() {
         return super.toString() + "\nPROPS: " + gameProperties;
     }
}
