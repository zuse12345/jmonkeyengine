/**
 * sourceType.java
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

import com.jmex.xml.types.SchemaID;
import com.jmex.xml.types.SchemaNCName;

public class sourceType extends com.jmex.xml.xml.Node {

	public sourceType(sourceType node) {
		super(node);
	}

	public sourceType(org.w3c.dom.Node node) {
		super(node);
	}

	public sourceType(org.w3c.dom.Document doc) {
		super(doc);
	}

	public sourceType(com.jmex.xml.xml.Document doc, String namespaceURI, String prefix, String name) {
		super(doc, namespaceURI, prefix, name);
	}
	
	public void adjustPrefix() {
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "id" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "id", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Attribute, null, "name" );
				tmpNode != null;
				tmpNode = getDomNextChild( Attribute, null, "name", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, false);
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "asset" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "asset", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new assetType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new IDREF_arrayType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new Name_arrayType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new bool_arrayType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new float_arrayType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new int_arrayType(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new technique_commonType9(tmpNode).adjustPrefix();
		}
		for (	org.w3c.dom.Node tmpNode = getDomFirstChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "technique" );
				tmpNode != null;
				tmpNode = getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "technique", tmpNode )
			) {
			internalAdjustPrefix(tmpNode, true);
			new techniqueType5(tmpNode).adjustPrefix();
		}
	}
	public void setXsiType() {
 		org.w3c.dom.Element el = (org.w3c.dom.Element) domNode;
		el.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "source");
	}

	public static int getidMinCount() {
		return 1;
	}

	public static int getidMaxCount() {
		return 1;
	}

	public int getidCount() {
		return getDomChildCount(Attribute, null, "id");
	}

	public boolean hasid() {
		return hasDomChild(Attribute, null, "id");
	}

	public SchemaID newid() {
		return new SchemaID();
	}

	public SchemaID getidAt(int index) throws Exception {
		return new SchemaID(getDomNodeValue(getDomChildAt(Attribute, null, "id", index)));
	}

	public org.w3c.dom.Node getStartingidCursor() throws Exception {
		return getDomFirstChild(Attribute, null, "id" );
	}

	public org.w3c.dom.Node getAdvancedidCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Attribute, null, "id", curNode );
	}

	public SchemaID getidValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new SchemaID(getDomNodeValue(curNode));
	}

	public SchemaID getid() throws Exception 
 {
		return getidAt(0);
	}

	public void removeidAt(int index) {
		removeDomChildAt(Attribute, null, "id", index);
	}

	public void removeid() {
		removeidAt(0);
	}

	public org.w3c.dom.Node addid(SchemaID value) {
		if( value.isNull() )
			return null;

		return  appendDomChild(Attribute, null, "id", value.toString());
	}

	public org.w3c.dom.Node addid(String value) throws Exception {
		return addid(new SchemaID(value));
	}

	public void insertidAt(SchemaID value, int index) {
		insertDomChildAt(Attribute, null, "id", index, value.toString());
	}

	public void insertidAt(String value, int index) throws Exception {
		insertidAt(new SchemaID(value), index);
	}

	public void replaceidAt(SchemaID value, int index) {
		replaceDomChildAt(Attribute, null, "id", index, value.toString());
	}

	public void replaceidAt(String value, int index) throws Exception {
		replaceidAt(new SchemaID(value), index);
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

	public static int getassetMinCount() {
		return 0;
	}

	public static int getassetMaxCount() {
		return 1;
	}

	public int getassetCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "asset");
	}

	public boolean hasasset() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "asset");
	}

	public assetType newasset() {
		return new assetType(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "asset"));
	}

	public assetType getassetAt(int index) throws Exception {
		return new assetType(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "asset", index));
	}

	public org.w3c.dom.Node getStartingassetCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "asset" );
	}

	public org.w3c.dom.Node getAdvancedassetCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "asset", curNode );
	}

	public assetType getassetValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new assetType(curNode);
	}

	public assetType getasset() throws Exception 
 {
		return getassetAt(0);
	}

	public void removeassetAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "asset", index);
	}

	public void removeasset() {
		removeassetAt(0);
	}

	public org.w3c.dom.Node addasset(assetType value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "asset", value);
	}

	public void insertassetAt(assetType value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "asset", index, value);
	}

	public void replaceassetAt(assetType value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "asset", index, value);
	}

	public static int getIDREF_arrayMinCount() {
		return 1;
	}

	public static int getIDREF_arrayMaxCount() {
		return 1;
	}

	public int getIDREF_arrayCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array");
	}

	public boolean hasIDREF_array() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array");
	}

	public IDREF_arrayType newIDREF_array() {
		return new IDREF_arrayType(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "IDREF_array"));
	}

	public IDREF_arrayType getIDREF_arrayAt(int index) throws Exception {
		return new IDREF_arrayType(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array", index));
	}

	public org.w3c.dom.Node getStartingIDREF_arrayCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array" );
	}

	public org.w3c.dom.Node getAdvancedIDREF_arrayCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array", curNode );
	}

	public IDREF_arrayType getIDREF_arrayValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new IDREF_arrayType(curNode);
	}

	public IDREF_arrayType getIDREF_array() throws Exception 
 {
		return getIDREF_arrayAt(0);
	}

	public void removeIDREF_arrayAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "IDREF_array", index);
	}

	public void removeIDREF_array() {
		removeIDREF_arrayAt(0);
	}

	public org.w3c.dom.Node addIDREF_array(IDREF_arrayType value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "IDREF_array", value);
	}

	public void insertIDREF_arrayAt(IDREF_arrayType value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "IDREF_array", index, value);
	}

	public void replaceIDREF_arrayAt(IDREF_arrayType value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "IDREF_array", index, value);
	}

	public static int getName_arrayMinCount() {
		return 1;
	}

	public static int getName_arrayMaxCount() {
		return 1;
	}

	public int getName_arrayCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array");
	}

	public boolean hasName_array() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array");
	}

	public Name_arrayType newName_array() {
		return new Name_arrayType(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "Name_array"));
	}

	public Name_arrayType getName_arrayAt(int index) throws Exception {
		return new Name_arrayType(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array", index));
	}

	public org.w3c.dom.Node getStartingName_arrayCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array" );
	}

	public org.w3c.dom.Node getAdvancedName_arrayCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array", curNode );
	}

	public Name_arrayType getName_arrayValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new Name_arrayType(curNode);
	}

	public Name_arrayType getName_array() throws Exception 
 {
		return getName_arrayAt(0);
	}

	public void removeName_arrayAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "Name_array", index);
	}

	public void removeName_array() {
		removeName_arrayAt(0);
	}

	public org.w3c.dom.Node addName_array(Name_arrayType value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "Name_array", value);
	}

	public void insertName_arrayAt(Name_arrayType value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "Name_array", index, value);
	}

	public void replaceName_arrayAt(Name_arrayType value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "Name_array", index, value);
	}

	public static int getbool_arrayMinCount() {
		return 1;
	}

	public static int getbool_arrayMaxCount() {
		return 1;
	}

	public int getbool_arrayCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array");
	}

	public boolean hasbool_array() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array");
	}

	public bool_arrayType newbool_array() {
		return new bool_arrayType(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "bool_array"));
	}

	public bool_arrayType getbool_arrayAt(int index) throws Exception {
		return new bool_arrayType(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array", index));
	}

	public org.w3c.dom.Node getStartingbool_arrayCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array" );
	}

	public org.w3c.dom.Node getAdvancedbool_arrayCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array", curNode );
	}

	public bool_arrayType getbool_arrayValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new bool_arrayType(curNode);
	}

	public bool_arrayType getbool_array() throws Exception 
 {
		return getbool_arrayAt(0);
	}

	public void removebool_arrayAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "bool_array", index);
	}

	public void removebool_array() {
		removebool_arrayAt(0);
	}

	public org.w3c.dom.Node addbool_array(bool_arrayType value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "bool_array", value);
	}

	public void insertbool_arrayAt(bool_arrayType value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "bool_array", index, value);
	}

	public void replacebool_arrayAt(bool_arrayType value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "bool_array", index, value);
	}

	public static int getfloat_arrayMinCount() {
		return 1;
	}

	public static int getfloat_arrayMaxCount() {
		return 1;
	}

	public int getfloat_arrayCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array");
	}

	public boolean hasfloat_array() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array");
	}

	public float_arrayType newfloat_array() {
		return new float_arrayType(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "float_array"));
	}

	public float_arrayType getfloat_arrayAt(int index) throws Exception {
		return new float_arrayType(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array", index));
	}

	public org.w3c.dom.Node getStartingfloat_arrayCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array" );
	}

	public org.w3c.dom.Node getAdvancedfloat_arrayCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array", curNode );
	}

	public float_arrayType getfloat_arrayValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new float_arrayType(curNode);
	}

	public float_arrayType getfloat_array() throws Exception 
 {
		return getfloat_arrayAt(0);
	}

	public void removefloat_arrayAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "float_array", index);
	}

	public void removefloat_array() {
		removefloat_arrayAt(0);
	}

	public org.w3c.dom.Node addfloat_array(float_arrayType value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "float_array", value);
	}

	public void insertfloat_arrayAt(float_arrayType value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "float_array", index, value);
	}

	public void replacefloat_arrayAt(float_arrayType value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "float_array", index, value);
	}

	public static int getint_arrayMinCount() {
		return 1;
	}

	public static int getint_arrayMaxCount() {
		return 1;
	}

	public int getint_arrayCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array");
	}

	public boolean hasint_array() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array");
	}

	public int_arrayType newint_array() {
		return new int_arrayType(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "int_array"));
	}

	public int_arrayType getint_arrayAt(int index) throws Exception {
		return new int_arrayType(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array", index));
	}

	public org.w3c.dom.Node getStartingint_arrayCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array" );
	}

	public org.w3c.dom.Node getAdvancedint_arrayCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array", curNode );
	}

	public int_arrayType getint_arrayValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new int_arrayType(curNode);
	}

	public int_arrayType getint_array() throws Exception 
 {
		return getint_arrayAt(0);
	}

	public void removeint_arrayAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "int_array", index);
	}

	public void removeint_array() {
		removeint_arrayAt(0);
	}

	public org.w3c.dom.Node addint_array(int_arrayType value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "int_array", value);
	}

	public void insertint_arrayAt(int_arrayType value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "int_array", index, value);
	}

	public void replaceint_arrayAt(int_arrayType value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "int_array", index, value);
	}

	public static int gettechnique_commonMinCount() {
		return 0;
	}

	public static int gettechnique_commonMaxCount() {
		return 1;
	}

	public int gettechnique_commonCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common");
	}

	public boolean hastechnique_common() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common");
	}

	public technique_commonType9 newtechnique_common() {
		return new technique_commonType9(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "technique_common"));
	}

	public technique_commonType9 gettechnique_commonAt(int index) throws Exception {
		return new technique_commonType9(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common", index));
	}

	public org.w3c.dom.Node getStartingtechnique_commonCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common" );
	}

	public org.w3c.dom.Node getAdvancedtechnique_commonCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common", curNode );
	}

	public technique_commonType9 gettechnique_commonValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new technique_commonType9(curNode);
	}

	public technique_commonType9 gettechnique_common() throws Exception 
 {
		return gettechnique_commonAt(0);
	}

	public void removetechnique_commonAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique_common", index);
	}

	public void removetechnique_common() {
		removetechnique_commonAt(0);
	}

	public org.w3c.dom.Node addtechnique_common(technique_commonType9 value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "technique_common", value);
	}

	public void inserttechnique_commonAt(technique_commonType9 value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "technique_common", index, value);
	}

	public void replacetechnique_commonAt(technique_commonType9 value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "technique_common", index, value);
	}

	public static int gettechniqueMinCount() {
		return 0;
	}

	public static int gettechniqueMaxCount() {
		return Integer.MAX_VALUE;
	}

	public int gettechniqueCount() {
		return getDomChildCount(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique");
	}

	public boolean hastechnique() {
		return hasDomChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique");
	}

	public techniqueType5 newtechnique() {
		return new techniqueType5(domNode.getOwnerDocument().createElementNS("http://www.collada.org/2005/11/COLLADASchema", "technique"));
	}

	public techniqueType5 gettechniqueAt(int index) throws Exception {
		return new techniqueType5(getDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique", index));
	}

	public org.w3c.dom.Node getStartingtechniqueCursor() throws Exception {
		return getDomFirstChild(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique" );
	}

	public org.w3c.dom.Node getAdvancedtechniqueCursor( org.w3c.dom.Node curNode ) throws Exception {
		return getDomNextChild( Element, "http://www.collada.org/2005/11/COLLADASchema", "technique", curNode );
	}

	public techniqueType5 gettechniqueValueAtCursor( org.w3c.dom.Node curNode ) throws Exception {
		if( curNode == null )
			throw new com.jmex.xml.xml.XmlException("Out of range");
		else
			return new techniqueType5(curNode);
	}

	public techniqueType5 gettechnique() throws Exception 
 {
		return gettechniqueAt(0);
	}

	public void removetechniqueAt(int index) {
		removeDomChildAt(Element, "http://www.collada.org/2005/11/COLLADASchema", "technique", index);
	}

	public void removetechnique() {
		while (hastechnique())
			removetechniqueAt(0);
	}

	public org.w3c.dom.Node addtechnique(techniqueType5 value) {
		return appendDomElement("http://www.collada.org/2005/11/COLLADASchema", "technique", value);
	}

	public void inserttechniqueAt(techniqueType5 value, int index) {
		insertDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "technique", index, value);
	}

	public void replacetechniqueAt(techniqueType5 value, int index) {
		replaceDomElementAt("http://www.collada.org/2005/11/COLLADASchema", "technique", index, value);
	}

}
