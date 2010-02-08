
import com.g3d.texture.plugins.DDSLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kirill
 */
public class Kaka {

    public static void main(String[] args) throws IOException{
        File f = new File("E:\\jME3\\src\\textures\\pond1_rgtc.DDS");
        DDSLoader dds = new DDSLoader();
        FileInputStream fis = new FileInputStream(f);
        dds.load(fis);
        fis.close();
    }

}
