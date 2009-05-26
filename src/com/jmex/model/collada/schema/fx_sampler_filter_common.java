/**
 * fx_sampler_filter_common.java
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

import com.jmex.xml.types.SchemaNMToken;

public class fx_sampler_filter_common extends SchemaNMToken {
	public static final int ENONE = 0; /* NONE */
	public static final int ENEAREST = 1; /* NEAREST */
	public static final int ELINEAR = 2; /* LINEAR */
	public static final int ENEAREST_MIPMAP_NEAREST = 3; /* NEAREST_MIPMAP_NEAREST */
	public static final int ELINEAR_MIPMAP_NEAREST = 4; /* LINEAR_MIPMAP_NEAREST */
	public static final int ENEAREST_MIPMAP_LINEAR = 5; /* NEAREST_MIPMAP_LINEAR */
	public static final int ELINEAR_MIPMAP_LINEAR = 6; /* LINEAR_MIPMAP_LINEAR */

	public static String[] sEnumValues = {
		"NONE",
		"NEAREST",
		"LINEAR",
		"NEAREST_MIPMAP_NEAREST",
		"LINEAR_MIPMAP_NEAREST",
		"NEAREST_MIPMAP_LINEAR",
		"LINEAR_MIPMAP_LINEAR",
	};

	public fx_sampler_filter_common() {
		super();
	}

	public fx_sampler_filter_common(String newValue) {
		super(newValue);
		validate();
	}

	public fx_sampler_filter_common(SchemaNMToken newValue) {
		super(newValue);
		validate();
	}

	public static int getEnumerationCount() {
		return sEnumValues.length;
	}

	public static String getEnumerationValue(int index) {
		return sEnumValues[index];
	}

	public static boolean isValidEnumerationValue(String val) {
		for (int i = 0; i < sEnumValues.length; i++) {
			if (val.equals(sEnumValues[i]))
				return true;
		}
		return false;
	}

	public void validate() {

		if (!isValidEnumerationValue(toString()))
			throw new com.jmex.xml.xml.XmlException("Value of fx_sampler_filter_common is invalid.");
	}
}
