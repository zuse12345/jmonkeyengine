/**
 * polylistType.java
 *
 * This file was generated by XMLSpy 2007sp2 Enterprise Edition.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the XMLSpy Documentation for further details.
 * http://www.altova.com/xmlspy
 */


package com.jmex.model.collada.schema;

import com.jmex.xml.types.SchemaNCName;

public class polylistType extends com.jmex.xml.xml.Node {

	public polylistType(polylistType node) {
		super(node);
	}

	public polylistType(org.w3c.dom.Node node) {
		super(node);
	}

	public polylistType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public polylistType(com.jmex.xml.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	
	public void adjustPrefix() {
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "name" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "name", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "count" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "count", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "material" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "material", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "input" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "input", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new InputLocalOffset(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "p" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "p", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "extra" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "extra", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new extraType(tmpNode).adjustPrefix();
		}
	}
	public void setXsiType() {
 		org.w3c.dom.Element el = (org.w3c.dom.Element) domNode;
		el.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "polylist");
	}

	public static int getnameMinCount() {
		return 0;
	}

	public static int getnameMaxCount() {
		return 1;
	}

	public int getnameCount() {
		return getDomChildCount(Attribute, null, "name");
	}

	public boolean hasname() {
		return hasDomChild(Attribute, null, "name");
	}

	public SchemaNCName newname() {
		return new SchemaNCName();
	}

	public SchemaNCName getnameAt(int index) throws Exception {
		return new SchemaNCName(getDomNodeValue(getDomChildAt(Attribute, null, "name", index)));
	}

	public org.w3c.dom.Node getStartingnameCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "name" );
	}

	public org.w3c.dom.Node getAdvancednameCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "name", curNode );
	}

	public SchemaNCName getnameValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new SchemaNCName(getDomNodeValue(curNode));
	}

	public SchemaNCName getname() throws Exception 
 {
		return getnameAt(0);
	}

	public void removenameAt(int index) {
		removeDomChildAt(Attribute, null, "name", index);
	}

	public void removename() {
		removenameAt(0);
	}

	public org.w3c.dom.Node addname(SchemaNCName value) {
		if( value.isNull() )
			return null;

		return  appendDomChild(Attribute, null, "name", value.toString());
	}

	public org.w3c.dom.Node addname(String value) throws Exception {
		return addname(new SchemaNCName(value));
	}

	public void insertnameAt(SchemaNCName value, int index) {
		insertDomChildAt(Attribute, null, "name", index, value.toString());
	}

	public void insertnameAt(String value, int index) throws Exception {
		insertnameAt(new SchemaNCName(value), index);
	}

	public void replacenameAt(SchemaNCName value, int index) {
		replaceDomChildAt(Attribute, null, "name", index, value.toString());
	}

	public void replacenameAt(String value, int index) throws Exception {
		replacenameAt(new SchemaNCName(value), index);
	}

	public static int getcountMinCount() {
		return 1;
	}

	public static int getcountMaxCount() {
		return 1;
	}

	public int getcountCount() {
		return getDomChildCount(Attribute, null, "count");
	}

	public boolean hascount() {
		return hasDomChild(Attribute, null, "count");
	}

	public uint newcount() {
		return new uint();
	}

	public uint getcountAt(int index) throws Exception {
		return new uint(getDomNodeValue(getDomChildAt(Attribute, null, "count", index)));
	}

	public org.w3c.dom.Node getStartingcountCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "count" );
	}

	public org.w3c.dom.Node getAdvancedcountCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "count", curNode );
	}

	public uint getcountValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new uint(getDomNodeValue(curNode));
	}

	public uint getcount() throws Exception 
 {
		return getcountAt(0);
	}

	public void removecountAt(int index) {
		removeDomChildAt(Attribute, null, "count", index);
	}

	public void removecount() {
		removecountAt(0);
	}

	public org.w3c.dom.Node addcount(uint value) {
		if( value.isNull() )
			return null;

		return  appendDomChild(Attribute, null, "count", value.toString());
	}

	public org.w3c.dom.Node addcount(String value) throws Exception {
		return addcount(new uint(value));
	}

	public void insertcountAt(uint value, int index) {
		insertDomChildAt(Attribute, null, "count", index, value.toString());
	}

	public void insertcountAt(String value, int index) throws Exception {
		insertcountAt(new uint(value), index);
	}

	public void replacecountAt(uint value, int index) {
		replaceDomChildAt(Attribute, null, "count", index, value.toString());
	}

	public void replacecountAt(String value, int index) throws Exception {
		replacecountAt(new uint(value), index);
	}

	public static int getmaterialMinCount() {
		return 0;
	}

	public static int getmaterialMaxCount() {
		return 1;
	}

	public int getmaterialCount() {
		return getDomChildCount(Attribute, null, "material");
	}

	public boolean hasmaterial() {
		return hasDomChild(Attribute, null, "material");
	}

	public SchemaNCName newmaterial() {
		return new SchemaNCName();
	}

	public SchemaNCName getmaterialAt(int index) throws Exception {
		return new SchemaNCName(getDomNodeValue(getDomChildAt(Attribute, null, "material", index)));
	}

	public org.w3c.dom.Node getStartingmaterialCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "material" );
	}

	public org.w3c.dom.Node getAdvancedmaterialCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "material", curNode );
	}

	public SchemaNCName getmaterialValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new SchemaNCName(getDomNodeValue(curNode));
	}

	public SchemaNCName getmaterial() throws Exception 
 {
		return getmaterialAt(0);
	}

	public void removematerialAt(int index) {
		removeDomChildAt(Attribute, null, "material", index);
	}

	public void removematerial() {
		removematerialAt(0);
	}

	public org.w3c.dom.Node addmaterial(SchemaNCName value) {
		if( value.isNull() )
			return null;

		return  appendDomChild(Attribute, null, "material", value.toString());
	}

	public org.w3c.dom.Node addmaterial(String value) throws Exception {
		return addmaterial(new SchemaNCName(value));
	}

	public void insertmaterialAt(SchemaNCName value, int index) {
		insertDomChildAt(Attribute, null, "material", index, value.toString());
	}

	public void insertmaterialAt(String value, int index) throws Exception {
		insertmaterialAt(new SchemaNCName(value), index);
	}

	public void replacematerialAt(SchemaNCName value, int index) {
		replaceDomChildAt(Attribute, null, "material", index, value.toString());
	}

	public void replacematerialAt(String value, int index) throws Exception {
		replacematerialAt(new SchemaNCName(value), index);
	}

	public static int getinputMinCount() {
		return 0;
	}

	public static int getinputMaxCount() {
		return Integer.MAX_VALUE;
	}

	public int getinputCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "input");
	}

	public boolean hasinput() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "input");
	}

	public InputLocalOffset newinput() {
		return new InputLocalOffset(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "input"));
	}

	public InputLocalOffset getinputAt(int index) throws Exception {
		return new InputLocalOffset(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "input", index));
	}

	public org.w3c.dom.Node getStartinginputCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "input" );
	}

	public org.w3c.dom.Node getAdvancedinputCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "input", curNode );
	}

	public InputLocalOffset getinputValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new InputLocalOffset(curNode);
	}

	public InputLocalOffset getinput() throws Exception 
 {
		return getinputAt(0);
	}

	public void removeinputAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "input", index);
	}

	public void removeinput() {
		while (hasinput())
			removeinputAt(0);
	}

	public org.w3c.dom.Node addinput(InputLocalOffset value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "input", value);
	}

	public void insertinputAt(InputLocalOffset value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "input", index, value);
	}

	public void replaceinputAt(InputLocalOffset value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "input", index, value);
	}

	public static int getvcountMinCount() {
		return 0;
	}

	public static int getvcountMaxCount() {
		return 1;
	}

	public int getvcountCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount");
	}

	public boolean hasvcount() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount");
	}

	public ListOfUInts newvcount() {
		return new ListOfUInts();
	}

	public ListOfUInts getvcountAt(int index) throws Exception {
		return new ListOfUInts(getDomNodeValue(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount", index)));
	}

	public org.w3c.dom.Node getStartingvcountCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount" );
	}

	public org.w3c.dom.Node getAdvancedvcountCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount", curNode );
	}

	public ListOfUInts getvcountValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new ListOfUInts(getDomNodeValue(curNode));
	}

	public ListOfUInts getvcount() throws Exception 
 {
		return getvcountAt(0);
	}

	public void removevcountAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount", index);
	}

	public void removevcount() {
		removevcountAt(0);
	}

	public org.w3c.dom.Node addvcount(ListOfUInts value) {
		if( value.isNull() )
			return null;

		return  appendDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount", value.toString());
	}

	public org.w3c.dom.Node addvcount(String value) throws Exception {
		return addvcount(new ListOfUInts(value));
	}

	public void insertvcountAt(ListOfUInts value, int index) {
		insertDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount", index, value.toString());
	}

	public void insertvcountAt(String value, int index) throws Exception {
		insertvcountAt(new ListOfUInts(value), index);
	}

	public void replacevcountAt(ListOfUInts value, int index) {
		replaceDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "vcount", index, value.toString());
	}

	public void replacevcountAt(String value, int index) throws Exception {
		replacevcountAt(new ListOfUInts(value), index);
	}

	public static int getpMinCount() {
		return 0;
	}

	public static int getpMaxCount() {
		return 1;
	}

	public int getpCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "p");
	}

	public boolean hasp() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "p");
	}

	public ListOfUInts newp() {
		return new ListOfUInts();
	}

	public ListOfUInts getpAt(int index) throws Exception {
		return new ListOfUInts(getDomNodeValue(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "p", index)));
	}

	public org.w3c.dom.Node getStartingpCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "p" );
	}

	public org.w3c.dom.Node getAdvancedpCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "p", curNode );
	}

	public ListOfUInts getpValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new ListOfUInts(getDomNodeValue(curNode));
	}

	public ListOfUInts getp() throws Exception 
 {
		return getpAt(0);
	}

	public void removepAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "p", index);
	}

	public void removep() {
		removepAt(0);
	}

	public org.w3c.dom.Node addp(ListOfUInts value) {
		if( value.isNull() )
			return null;

		return  appendDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "p", value.toString());
	}

	public org.w3c.dom.Node addp(String value) throws Exception {
		return addp(new ListOfUInts(value));
	}

	public void insertpAt(ListOfUInts value, int index) {
		insertDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "p", index, value.toString());
	}

	public void insertpAt(String value, int index) throws Exception {
		insertpAt(new ListOfUInts(value), index);
	}

	public void replacepAt(ListOfUInts value, int index) {
		replaceDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "p", index, value.toString());
	}

	public void replacepAt(String value, int index) throws Exception {
		replacepAt(new ListOfUInts(value), index);
	}

	public static int getextraMinCount() {
		return 0;
	}

	public static int getextraMaxCount() {
		return Integer.MAX_VALUE;
	}

	public int getextraCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "extra");
	}

	public boolean hasextra() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "extra");
	}

	public extraType newextra() {
		return new extraType(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "extra"));
	}

	public extraType getextraAt(int index) throws Exception {
		return new extraType(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "extra", index));
	}

	public org.w3c.dom.Node getStartingextraCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "extra" );
	}

	public org.w3c.dom.Node getAdvancedextraCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "extra", curNode );
	}

	public extraType getextraValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new extraType(curNode);
	}

	public extraType getextra() throws Exception 
 {
		return getextraAt(0);
	}

	public void removeextraAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "extra", index);
	}

	public void removeextra() {
		while (hasextra())
			removeextraAt(0);
	}

	public org.w3c.dom.Node addextra(extraType value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "extra", value);
	}

	public void insertextraAt(extraType value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "extra", index, value);
	}

	public void replaceextraAt(extraType value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "extra", index, value);
	}

}
