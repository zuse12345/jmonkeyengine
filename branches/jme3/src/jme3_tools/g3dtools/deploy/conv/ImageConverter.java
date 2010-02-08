package g3dtools.deploy.conv;

import com.g3d.math.ColorRGBA;
import com.g3d.math.Vector3f;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGQTable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class ImageConverter {

    private static final JPEGQTable badTable;
    private static final JPEGQTable perfectTable;

    static {
        int[] table = new int[64];
        Arrays.fill(table, 999999999);
        badTable = new JPEGQTable(table.clone());

        Arrays.fill(table, 1);
        perfectTable = new JPEGQTable(table.clone());
    }

    static double minV = 100,
           maxV = -100,
           minU = 100,
           maxU = -100;

    private static final void normalToUV(Vector3f normal, ColorRGBA out){
        //double x_sqr = normal.x * normal.x;
        //double y_sqr = normal.y * normal.y;
        //double z_p1_sqr = normal.z + 1.0;
        //z_p1_sqr *= z_p1_sqr;
        
        //double m = 2.0 * Math.sqrt(x_sqr + y_sqr + z_p1_sqr);
        //double u = (normal.x / m) + 0.5;
        //double v = (normal.y / m) + 0.5;
//        minV = Math.min(minV, v);
//        maxV = Math.max(maxV, v);
//        minU = Math.min(minU, u);
//        maxU = Math.max(maxU, u);

//        store[0] = ((int) Math.round(u * 255.0)) & 0xFF;
//        store[1] = ((int) Math.round(v * 255.0)) & 0xFF;

        out.r = normal.x;
        out.g = normal.y;
        out.b = 1.0f;
    }

    private static final void uvToNormal(ColorRGBA in, Vector3f store){
        store.x = in.r;
        store.y = in.g;
        store.z = (float) Math.sqrt(1.0f - store.x * store.x - store.y * store.y);
    }

    private static final BufferedImage normalizeImage(BufferedImage inputImage){
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(),
                                                      inputImage.getHeight(),
                                                      BufferedImage.TYPE_INT_RGB);
        Vector3f vector = new Vector3f();
        ColorRGBA color = new ColorRGBA();
        for (int y = 0; y < inputImage.getHeight(); y++){
            for (int x = 0; x < inputImage.getWidth(); x++){
                int argb = inputImage.getRGB(x, y);
                color.fromIntARGB(argb);
                vector.set(color.r, color.g, color.b);
                vector.normalizeLocal();
                color.set(vector.x, vector.y, vector.z, 1f);
                argb = color.asIntARGB();
                outputImage.setRGB(x, y, argb);
            }
        }
        return outputImage;
    }

    private static final void encodeSpecial(BufferedImage inputImage, File outFile, float q) throws IOException{
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(),
                                                      inputImage.getHeight(),
                                                      BufferedImage.TYPE_INT_RGB);

        ColorRGBA color = new ColorRGBA();
        Vector3f vector = new Vector3f();
        for (int y = 0; y < inputImage.getHeight(); y++){
            for (int x = 0; x < inputImage.getWidth(); x++){
                int argb = inputImage.getRGB(x, y);

                color.fromIntARGB(argb);
                vector.set(color.r, color.g, color.b);
                vector.normalizeLocal();
                normalToUV(vector, color);

                argb = color.asIntARGB();
                outputImage.setRGB(x, y, argb);
            }
        }

        FileOutputStream stream = new FileOutputStream(outFile);
        JPEGEncodeParam param = JPEGCodec.getDefaultJPEGEncodeParam(
                                    inputImage.getRaster(),
                                    JPEGEncodeParam.COLOR_ID_RGB);

        param.setHorizontalSubsampling(0, 1);
        param.setHorizontalSubsampling(1, 1);
        param.setHorizontalSubsampling(2, 1);

        param.setVerticalSubsampling(0, 1);
        param.setVerticalSubsampling(1, 1);
        param.setVerticalSubsampling(2, 1);

//        q *= 100f;
//        float m = q < 50f ? 5000f / q : 200f - q * 2f;
//        m /= 100f;

        param.setQuality(q, false);
//        JPEGQTable newTable = JPEGQTable.StdLuminance.getScaledInstance(m, true);
//        param.setQTable(0, newTable);
//        param.setQTable(1, newTable);
        param.setQTable(2, badTable);

//        param.setImageInfoValid(true);
//        param.setTableInfoValid(true);
//        param.setMarkerData(JPEGEncodeParam.APP0_MARKER, null);

        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(stream, param);
        encoder.encode(inputImage.getRaster());
        stream.close();
    }

    private static final BufferedImage decodeSpecial(File inFile) throws IOException{
        BufferedImage img = ImageIO.read(inFile);
        BufferedImage outImage = new BufferedImage(img.getWidth(), img.getHeight(),
                                                   BufferedImage.TYPE_INT_RGB);

        ColorRGBA color = new ColorRGBA();
        Vector3f vector = new Vector3f();
        for (int y = 0; y < img.getHeight(); y++){
            for (int x = 0; x < img.getWidth(); x++){
                int argb = img.getRGB(x, y);

                color.fromIntARGB(argb);
                uvToNormal(color, vector);
                color.set(vector.x, vector.y, vector.z, 1.0f);
                outImage.setRGB(x, y, color.asIntARGB());
            }
        }

        return outImage;
    }

    private static final void encodeRegular(BufferedImage inputImage, File outFile, float q) throws IOException{
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(outFile);
        writer.setOutput(ios);

        // Set the compression quality
        JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.US);
        param.setSourceSubsampling(1, 1, 0, 0);
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
        param.setCompressionQuality(q);
        param.setOptimizeHuffmanTables(true);

        // Write the image
        writer.write(null, new IIOImage(inputImage, null, null), param);

        // Cleanup
        ios.flush();
        writer.dispose();
        ios.close();
    }

    private static final BufferedImage decodeRegular(File inFile) throws IOException{
        return ImageIO.read(inFile);
    }

    private static final double computeDifference(BufferedImage a, BufferedImage b){
        long totalErr = 0;

        for (int y = 0; y < a.getHeight(); y++){
            for (int x = 0; x < a.getWidth(); x++){
                int argb1 = a.getRGB(x, y);
                int argb2 = b.getRGB(x, y);
                int dr = ((argb1 >> 16) & 0xFF) - ((argb2 >> 16) & 0xFF);
                int dg = ((argb1 >> 8) & 0xFF) - ((argb2 >> 8) & 0xFF);
                int db = ((argb1 >> 0) & 0xFF) - ((argb2 >> 0) & 0xFF);
                int total = dr + dg + db;
                totalErr += total;
            }
        }

        int numPixels = a.getWidth() * a.getHeight();
        return (double) totalErr / (double) numPixels;
    }

    public static void main(String[] args) throws IOException{
        // load a normal map
        File input = new File("E:\\jME3\\src\\textures\\pond1DOT3.png");
        File outs = new File("E:\\SPECIAL.jpg");
        File outr = new File("E:\\REGULAR.jpg");

        BufferedImage o = ImageIO.read(input);
        o = normalizeImage(o);
        ImageIO.write(o, "png", new File("E:\\original.png"));

        outr.delete();
        outs.delete();
        encodeRegular(o, outr, 0.34f);
        encodeSpecial(o, outs, 0.07f);
        
        // original, regular, and special
        BufferedImage r = decodeRegular(outr);
        BufferedImage s = decodeSpecial(outs);

        ImageIO.write(r, "png", new File("E:\\regular.png"));
        ImageIO.write(s, "png", new File("E:\\special.png"));

        // special avg error
        double errSpecial = computeDifference(o, s);

        // regular avg error
        double errRegular = computeDifference(o, r);

        // baseline
        double errOriginal = computeDifference(o, o);

        System.out.println("s" + errSpecial);
        System.out.println("r" + errRegular);
        System.out.println("o" + errOriginal);
    }

}
