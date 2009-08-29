package com.g3d.scene.plugins;

import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetLoader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Loads Ogre3D mesh.xml files.
 */
public class MESHLoader extends DefaultHandler implements AssetLoader {

    public static void main(String[] args) throws SAXException{

    }

    public Object load(AssetInfo info) throws IOException {
        try{
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
            InputStreamReader r = new InputStreamReader(info.openStream());
            xr.parse(new InputSource(r));
            return null;
        }catch (SAXException ex){
            throw new IOException("Error while parsing XML", ex);
        }

    }

}
