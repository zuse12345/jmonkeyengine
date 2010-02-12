/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package g3dtools.converters.palette;

import com.sun.imageio.plugins.common.PaletteBuilder;
import java.awt.image.RenderedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PaletteBuilderWrapper {

    private PaletteBuilder builder;
    private Class<PaletteBuilder> clazz = PaletteBuilder.class;
    private Constructor<PaletteBuilder> ctor;
    private Method buildPalMethod;
    private Method getImageMethod;
    private Field srcField;
    private Field srcRasterField;

    {
        try{
            ctor = clazz.getDeclaredConstructor(RenderedImage.class, int.class);
            ctor.setAccessible(true);

            buildPalMethod = clazz.getDeclaredMethod("buildPalette", (Class<?>[]) null);
            buildPalMethod.setAccessible(true);

            getImageMethod = clazz.getDeclaredMethod("getIndexedImage", (Class<?>[]) null);
            getImageMethod.setAccessible(true);

            srcField = clazz.getDeclaredField("src");
            srcField.setAccessible(true);

            srcRasterField = clazz.getDeclaredField("srcRaster");
            srcRasterField.setAccessible(true);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void initialize(RenderedImage image, int paletteSize) {
        try{
            builder = ctor.newInstance(image, paletteSize);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void buildPalette() {
        try{
            buildPalMethod.invoke(builder, (Object[]) null);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void setSourceImage(RenderedImage image) {
        try{
            srcField.set(builder, image);
            srcRasterField.set(builder, image.getData());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public RenderedImage getIndexedImage() {
        try{
            return (RenderedImage) getImageMethod.invoke(builder, (Object[]) null);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
