/**
 * InputLocalOffset.java This file was generated by XMLSpy 2006sp2 Enterprise
 * Edition. YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE OVERWRITTEN WHEN
 * YOU RE-RUN CODE GENERATION. Refer to the XMLSpy Documentation for further
 * details. http://www.altova.com/xmlspy
 */

package com.jmex.model.collada.schema;

import com.jmex.model.collada.types.SchemaNMToken;

public class InputLocalOffset extends com.jmex.model.collada.xml.Node {

    private static final long serialVersionUID = 1L;

    public InputLocalOffset(InputLocalOffset node) {
        super(node);
    }

    public InputLocalOffset(org.w3c.dom.Node node) {
        super(node);
    }

    public InputLocalOffset(org.w3c.dom.Document doc) {
        super(doc);
    }

    public InputLocalOffset(com.jmex.model.collada.xml.Document doc,
            String namespaceURI, String prefix, String name) {
        super(doc, namespaceURI, prefix, name);
    }

    public void adjustPrefix() {
        for (org.w3c.dom.Node tmpNode = getDomFirstChild(Attribute, null,
                "offset"); tmpNode != null; tmpNode = getDomNextChild(
                Attribute, null, "offset", tmpNode)) {
            internalAdjustPrefix(tmpNode, false);
        }
        for (org.w3c.dom.Node tmpNode = getDomFirstChild(Attribute, null,
                "semantic"); tmpNode != null; tmpNode = getDomNextChild(
                Attribute, null, "semantic", tmpNode)) {
            internalAdjustPrefix(tmpNode, false);
        }
        for (org.w3c.dom.Node tmpNode = getDomFirstChild(Attribute, null,
                "source"); tmpNode != null; tmpNode = getDomNextChild(
                Attribute, null, "source", tmpNode)) {
            internalAdjustPrefix(tmpNode, false);
        }
        for (org.w3c.dom.Node tmpNode = getDomFirstChild(Attribute, null, "set"); tmpNode != null; tmpNode = getDomNextChild(
                Attribute, null, "set", tmpNode)) {
            internalAdjustPrefix(tmpNode, false);
        }
    }

    public static int getoffsetMinCount() {
        return 1;
    }

    public static int getoffsetMaxCount() {
        return 1;
    }

    public int getoffsetCount() {
        return getDomChildCount(Attribute, null, "offset");
    }

    public boolean hasoffset() {
        return hasDomChild(Attribute, null, "offset");
    }

    public uint newoffset() {
        return new uint();
    }

    public uint getoffsetAt(int index) throws Exception {
        return new uint(getDomNodeValue(dereference(getDomChildAt(Attribute,
                null, "offset", index))));
    }

    public org.w3c.dom.Node getStartingoffsetCursor() throws Exception {
        return getDomFirstChild(Attribute, null, "offset");
    }

    public org.w3c.dom.Node getAdvancedoffsetCursor(org.w3c.dom.Node curNode)
            throws Exception {
        return getDomNextChild(Attribute, null, "offset", curNode);
    }

    public uint getoffsetValueAtCursor(org.w3c.dom.Node curNode)
            throws Exception {
        if (curNode == null)
            throw new com.jmex.model.collada.xml.XmlException("Out of range");
        else
            return new uint(getDomNodeValue(dereference(curNode)));
    }

    public uint getoffset() throws Exception {
        return getoffsetAt(0);
    }

    public void removeoffsetAt(int index) {
        removeDomChildAt(Attribute, null, "offset", index);
    }

    public void removeoffset() {
        while (hasoffset())
            removeoffsetAt(0);
    }

    public void addoffset(uint value) {
        if (value.isNull() == false) {
            appendDomChild(Attribute, null, "offset", value.toString());
        }
    }

    public void addoffset(String value) throws Exception {
        addoffset(new uint(value));
    }

    public void insertoffsetAt(uint value, int index) {
        insertDomChildAt(Attribute, null, "offset", index, value.toString());
    }

    public void insertoffsetAt(String value, int index) throws Exception {
        insertoffsetAt(new uint(value), index);
    }

    public void replaceoffsetAt(uint value, int index) {
        replaceDomChildAt(Attribute, null, "offset", index, value.toString());
    }

    public void replaceoffsetAt(String value, int index) throws Exception {
        replaceoffsetAt(new uint(value), index);
    }

    public static int getsemanticMinCount() {
        return 1;
    }

    public static int getsemanticMaxCount() {
        return 1;
    }

    public int getsemanticCount() {
        return getDomChildCount(Attribute, null, "semantic");
    }

    public boolean hassemantic() {
        return hasDomChild(Attribute, null, "semantic");
    }

    public SchemaNMToken newsemantic() {
        return new SchemaNMToken();
    }

    public SchemaNMToken getsemanticAt(int index) throws Exception {
        return new SchemaNMToken(getDomNodeValue(dereference(getDomChildAt(
                Attribute, null, "semantic", index))));
    }

    public org.w3c.dom.Node getStartingsemanticCursor() throws Exception {
        return getDomFirstChild(Attribute, null, "semantic");
    }

    public org.w3c.dom.Node getAdvancedsemanticCursor(org.w3c.dom.Node curNode)
            throws Exception {
        return getDomNextChild(Attribute, null, "semantic", curNode);
    }

    public SchemaNMToken getsemanticValueAtCursor(org.w3c.dom.Node curNode)
            throws Exception {
        if (curNode == null)
            throw new com.jmex.model.collada.xml.XmlException("Out of range");
        else
            return new SchemaNMToken(getDomNodeValue(dereference(curNode)));
    }

    public SchemaNMToken getsemantic() throws Exception {
        return getsemanticAt(0);
    }

    public void removesemanticAt(int index) {
        removeDomChildAt(Attribute, null, "semantic", index);
    }

    public void removesemantic() {
        while (hassemantic())
            removesemanticAt(0);
    }

    public void addsemantic(SchemaNMToken value) {
        if (value.isNull() == false) {
            appendDomChild(Attribute, null, "semantic", value.toString());
        }
    }

    public void addsemantic(String value) throws Exception {
        addsemantic(new SchemaNMToken(value));
    }

    public void insertsemanticAt(SchemaNMToken value, int index) {
        insertDomChildAt(Attribute, null, "semantic", index, value.toString());
    }

    public void insertsemanticAt(String value, int index) throws Exception {
        insertsemanticAt(new SchemaNMToken(value), index);
    }

    public void replacesemanticAt(SchemaNMToken value, int index) {
        replaceDomChildAt(Attribute, null, "semantic", index, value.toString());
    }

    public void replacesemanticAt(String value, int index) throws Exception {
        replacesemanticAt(new SchemaNMToken(value), index);
    }

    public static int getsourceMinCount() {
        return 1;
    }

    public static int getsourceMaxCount() {
        return 1;
    }

    public int getsourceCount() {
        return getDomChildCount(Attribute, null, "source");
    }

    public boolean hassource() {
        return hasDomChild(Attribute, null, "source");
    }

    public URIFragmentType newsource() {
        return new URIFragmentType();
    }

    public URIFragmentType getsourceAt(int index) throws Exception {
        return new URIFragmentType(getDomNodeValue(dereference(getDomChildAt(
                Attribute, null, "source", index))));
    }

    public org.w3c.dom.Node getStartingsourceCursor() throws Exception {
        return getDomFirstChild(Attribute, null, "source");
    }

    public org.w3c.dom.Node getAdvancedsourceCursor(org.w3c.dom.Node curNode)
            throws Exception {
        return getDomNextChild(Attribute, null, "source", curNode);
    }

    public URIFragmentType getsourceValueAtCursor(org.w3c.dom.Node curNode)
            throws Exception {
        if (curNode == null)
            throw new com.jmex.model.collada.xml.XmlException("Out of range");
        else
            return new URIFragmentType(getDomNodeValue(dereference(curNode)));
    }

    public URIFragmentType getsource() throws Exception {
        return getsourceAt(0);
    }

    public void removesourceAt(int index) {
        removeDomChildAt(Attribute, null, "source", index);
    }

    public void removesource() {
        while (hassource())
            removesourceAt(0);
    }

    public void addsource(URIFragmentType value) {
        if (value.isNull() == false) {
            appendDomChild(Attribute, null, "source", value.toString());
        }
    }

    public void addsource(String value) throws Exception {
        addsource(new URIFragmentType(value));
    }

    public void insertsourceAt(URIFragmentType value, int index) {
        insertDomChildAt(Attribute, null, "source", index, value.toString());
    }

    public void insertsourceAt(String value, int index) throws Exception {
        insertsourceAt(new URIFragmentType(value), index);
    }

    public void replacesourceAt(URIFragmentType value, int index) {
        replaceDomChildAt(Attribute, null, "source", index, value.toString());
    }

    public void replacesourceAt(String value, int index) throws Exception {
        replacesourceAt(new URIFragmentType(value), index);
    }

    public static int getsetMinCount() {
        return 0;
    }

    public static int getsetMaxCount() {
        return 1;
    }

    public int getsetCount() {
        return getDomChildCount(Attribute, null, "set");
    }

    public boolean hasset() {
        return hasDomChild(Attribute, null, "set");
    }

    public uint newset() {
        return new uint();
    }

    public uint getsetAt(int index) throws Exception {
        return new uint(getDomNodeValue(dereference(getDomChildAt(Attribute,
                null, "set", index))));
    }

    public org.w3c.dom.Node getStartingsetCursor() throws Exception {
        return getDomFirstChild(Attribute, null, "set");
    }

    public org.w3c.dom.Node getAdvancedsetCursor(org.w3c.dom.Node curNode)
            throws Exception {
        return getDomNextChild(Attribute, null, "set", curNode);
    }

    public uint getsetValueAtCursor(org.w3c.dom.Node curNode) throws Exception {
        if (curNode == null)
            throw new com.jmex.model.collada.xml.XmlException("Out of range");
        else
            return new uint(getDomNodeValue(dereference(curNode)));
    }

    public uint getset() throws Exception {
        return getsetAt(0);
    }

    public void removesetAt(int index) {
        removeDomChildAt(Attribute, null, "set", index);
    }

    public void removeset() {
        while (hasset())
            removesetAt(0);
    }

    public void addset(uint value) {
        if (value.isNull() == false) {
            appendDomChild(Attribute, null, "set", value.toString());
        }
    }

    public void addset(String value) throws Exception {
        addset(new uint(value));
    }

    public void insertsetAt(uint value, int index) {
        insertDomChildAt(Attribute, null, "set", index, value.toString());
    }

    public void insertsetAt(String value, int index) throws Exception {
        insertsetAt(new uint(value), index);
    }

    public void replacesetAt(uint value, int index) {
        replaceDomChildAt(Attribute, null, "set", index, value.toString());
    }

    public void replacesetAt(String value, int index) throws Exception {
        replacesetAt(new uint(value), index);
    }

    private org.w3c.dom.Node dereference(org.w3c.dom.Node node) {
        return node;
    }
}