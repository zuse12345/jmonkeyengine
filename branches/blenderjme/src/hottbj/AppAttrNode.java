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


import com.jme.scene.Node;
import com.jme.util.export.StringStringMap;
import com.jme.util.export.StringBoolMap;
import com.jme.util.export.StringIntMap;
import com.jme.util.export.StringFloatMap;

/**
 * Sample Node subclass that will echo all *SpatialAppAttrs in toString().
 *
 * In your own custom classes, you will want to use the values of the Attrs
 * for app-specific purposes, perhaps deriving instance field values or adding
 * wrapper getters and setters for specific map elements.
 * <P>
 * This class requires the presence of the com.jme.util.export.*Map* classes
 * in your compilation-time and runtime classpath.
 * We expect these classes to be merged into jME head, in which case this
 * requirement will be alleviated.
 * </P>
 */
public class AppAttrNode extends Node {
    /**
     * If 'this' instance is instantiated by a jME Importer, this constructor
     * will be used.
     */
    public AppAttrNode() {
        System.err.println("No-param " + AppAttrNode.class.getName()
                + " instantiated");
    }

    /**
     * Use this constructor to populate an instance with test attr values.
     */
    public AppAttrNode(String s) {
        super(s);
        System.err.println(AppAttrNode.class.getName() + " (" + getName()
                + ") instantiated.  Writing several sample attrs");
        StringStringMap strAttrMap = new StringStringMap();
        strAttrMap.put("myName", s);
        strAttrMap.put("digit two", Integer.toString(2));
        setUserData("stringSpatialAppAttrs", strAttrMap);
        StringFloatMap floAttrMap = new StringFloatMap();
        floAttrMap.put("myFloat", 0.8f);
        floAttrMap.put("float two", 23.456f);
        setUserData("floatSpatialAppAttrs", floAttrMap);
        StringBoolMap booAttrMap = new StringBoolMap();
        booAttrMap.put("myBool", true);
        booAttrMap.put("bool two", false);
        setUserData("boolSpatialAppAttrs", booAttrMap);
        StringIntMap intAttrMap = new StringIntMap();
        intAttrMap.put("myInt", 82);
        intAttrMap.put("int two", 9214);
        setUserData("intSpatialAppAttrs", intAttrMap);
    }

    public String toString() {
        return super.toString() + "\nString PROPS: "
            + getUserData("stringSpatialAppAttrs")
            + "\nFloat PROPS: "
            + getUserData("floatSpatialAppAttrs")
            + "\nInt PROPS: "
            + getUserData("intSpatialAppAttrs")
            + "\nBool PROPS: "
            + getUserData("boolSpatialAppAttrs");
    }
}
