package g3dtest.export;

import com.g3d.export.Savable;
import com.g3d.export.binary.BinaryExporter;
import com.g3d.export.binary.BinaryImporter;
import com.g3d.export.xml.XMLExporter;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImpExp {

//    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public static final Savable reload(Savable savable){
        try{
            BinaryExporter exp = new BinaryExporter();
            BinaryImporter imp = new BinaryImporter(null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exp.save(savable, baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return imp.load(bais, null, null);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static final void export(Savable savable){
        FileOutputStream fos = null;
        try{
            File f = new File("C:\\model.j3o");
            fos = new FileOutputStream(f);
            BinaryExporter exp = new BinaryExporter();
            exp.save(savable, fos);
            fos.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }finally{
            try{
                fos.close();
            }catch (IOException ex){
                Logger.getLogger(ImpExp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static final void showXML(Savable savable){
        try{
            XMLExporter exp = new XMLExporter();
            File f = File.createTempFile("jme_savable", ".xml");
            FileOutputStream fos = new FileOutputStream(f);
            exp.save(savable, fos);
            Desktop.getDesktop().browse(f.toURI());
            f.deleteOnExit();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
