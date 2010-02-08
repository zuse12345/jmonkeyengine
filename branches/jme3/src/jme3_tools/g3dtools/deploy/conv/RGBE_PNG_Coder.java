package g3dtools.deploy.conv;

import com.g3d.math.ColorRGBA;
import com.g3d.texture.Image;
import com.g3d.texture.plugins.HDRLoader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class RGBE_PNG_Coder {
    public static final void encode(File hdr, File out) throws IOException{
        HDRLoader loader = new HDRLoader(true);
        FileInputStream fis = new FileInputStream(hdr);
        Image img = loader.load(fis, false);
        fis.close();

        int w = img.getWidth();
        int h = img.getHeight();
        ColorRGBA tmp = new ColorRGBA();
        ByteBuffer bb = img.getData(0);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        for (int y = 0; y < h; y++){
            for (int x = 0; x < w; x++){
                int rgbe = bb.getInt( (y*w+x)*4 );
                tmp.fromIntRGBA(rgbe);
                bi.setRGB(x, y, tmp.asIntARGB());
            }
        }

        ImageIO.write(bi, "png", out);
//        JPEGImageWriter writer = (JPEGImageWriter) ImageIO.getImageWritersBySuffix("jpg").next();
//
//        // Prepare output file
//        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
//        writer.setOutput(ios);
//
//        // Set the compression quality
//        JPEGImageWriteParam iwparam = new JPEGImageWriteParam(null);
//        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
//        iwparam.setCompressionQuality(0.75f);
//        iwparam.setOptimizeHuffmanTables(true);
//        iwparam.setSourceSubsampling(1, 1, 0, 0);
//
//        // Write the image
//        writer.write(null, new IIOImage(bi, null, null), iwparam);
//
//        // Cleanup
//        ios.flush();
//        writer.dispose();
//        ios.close();
    }
    public static void main(String[] args) throws IOException{
        encode(new File("E:/jme3/src/textures/memorial.hdr"),
               new File("C:\\test.jpg"));
    }
}
