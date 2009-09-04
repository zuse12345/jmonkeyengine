package com.g3d.util.xml;

import org.xml.sax.SAXException;

/**
 * Utility methods for parsing XML data using SAX.
 * Applications wishing to use this class
 */
public final class SAXUtil {

    public static int parseInt(String i, int def) throws SAXException{
        if (i == null)
            return def;
        else{
            try {
                return Integer.parseInt(i);
            } catch (NumberFormatException ex){
                throw new SAXException("Expected an integer, got '"+i+"'");
            }
        }
    }

    public static int parseInt(String i) throws SAXException{
        if (i == null)
            throw new SAXException("Expected an integer");
        else{
            try {
                return Integer.parseInt(i);
            } catch (NumberFormatException ex){
                throw new SAXException("Expected an integer, got '"+i+"'");
            }
        }
    }

    public static float parseFloat(String f, float def) throws SAXException{
        if (f == null)
            return def;
        else{
            try {
                return Float.parseFloat(f);
            } catch (NumberFormatException ex){
                throw new SAXException("Expected a decimal, got '"+f+"'");
            }
        }
    }

    public static float parseFloat(String f) throws SAXException{
        if (f == null)
            throw new SAXException("Expected a decimal");
        else{
            try {
                return Float.parseFloat(f);
            } catch (NumberFormatException ex){
                throw new SAXException("Expected a decimal, got '"+f+"'");
            }
        }
    }

    public static boolean parseBool(String bool, boolean def) throws SAXException{
        if (bool == null || bool.equals(""))
            return def;
        else if (bool.equals("false"))
            return false;
        else if (bool.equals("true"))
            return true;
        else
            throw new SAXException("Expected a boolean, got'"+bool+"'");
    }

    public static String parseString(String str, String def){
        if (str == null)
            return def;
        else
            return str;
    }

    public static String parseString(String str) throws SAXException{
        if (str == null)
            throw new SAXException("Expected a string");
        else
            return str;
    }

}
