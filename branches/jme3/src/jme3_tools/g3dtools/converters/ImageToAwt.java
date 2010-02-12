package g3dtools.converters;

import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class ImageToAwt {

    private static final HashMap<Format, DecodeParams> params = new HashMap<Format, DecodeParams>();

    private static class DecodeParams {

        final int bpp, am, rm, gm, bm, as, rs, gs, bs, im, is;

        public DecodeParams(int bpp, int am, int rm, int gm, int bm, int as, int rs, int gs, int bs, int im, int is) {
            this.bpp = bpp;
            this.am = am;
            this.rm = rm;
            this.gm = gm;
            this.bm = bm;
            this.as = as;
            this.rs = rs;
            this.gs = gs;
            this.bs = bs;
            this.im = im;
            this.is = is;
        }

        public DecodeParams(int bpp, int rm, int rs, int im, int is, boolean alpha){
            this.bpp = bpp;
            if (alpha){
                this.am = rm;
                this.as = rs;
                this.rm = 0;
                this.rs = 0;
            }else{
                this.rm = rm;
                this.rs = rs;
                this.am = 0;
                this.as = 0;
            }
            
            this.gm = 0;
            this.bm = 0;
            this.gs = 0;
            this.bs = 0;
            this.im = im;
            this.is = is;
        }

        public DecodeParams(int bpp, int rm, int rs, int im, int is){
            this(bpp, rm, rs, im, is, false);
        }
    }

    static {
        final int mx___ = 0xff000000;
        final int m_x__ = 0x00ff0000;
        final int m__x_ = 0x0000ff00;
        final int m___x = 0x000000ff;
        final int sx___ = 24;
        final int s_x__ = 16;
        final int s__x_ = 8;
        final int s___x = 0;
        final int mxxxx = 0xffffffff;
        final int sxxxx = 0;

        final int m4x___ = 0xf000;
        final int m4_x__ = 0x0f00;
        final int m4__x_ = 0x00f0;
        final int m4___x = 0x000f;
        final int s4x___ = 12;
        final int s4_x__ = 8;
        final int s4__x_ = 4;
        final int s4___x = 0;

        final int m5___  = 0xf800;
        final int m_5__  = 0x07c0;
        final int m__5_  = 0x003e;
        final int m___1  = 0x0001;

        final int s5___  = 11;
        final int s_5__  = 6;
        final int s__5_  = 1;
        final int s___1  = 0;

        final int m5__   = 0xf800;
        final int m_6_   = 0x07e0;
        final int m__5   = 0x001f;

        final int s5__   = 11;
        final int s_6_   = 5;
        final int s__5   = 0;

        final int mxx__  = 0xffff0000;
        final int sxx__  = 32;
        final int m__xx  = 0x0000ffff;
        final int s__xx  = 0;

        // note: compressed, depth, or floating point formats not included here..
        
        params.put(Format.ABGR8,    new DecodeParams(4, mx___, m___x, m__x_, m_x__,
                                                        sx___, s___x, s__x_, s_x__,
                                                        mxxxx, sxxxx));
        params.put(Format.ARGB4444, new DecodeParams(4, m4x___, m4_x__, m4__x_, m4___x,
                                                        s4x___, s4_x__, s4__x_, s4___x,
                                                        mxxxx, sxxxx));
        params.put(Format.Alpha16,  new DecodeParams(2, mxxxx, sxxxx, mxxxx, sxxxx, true));
        params.put(Format.Alpha8,   new DecodeParams(1, mxxxx, sxxxx, mxxxx, sxxxx, true));
        params.put(Format.BGR8,     new DecodeParams(3, 0,     m___x, m__x_, m_x__,
                                                        0,     s___x, s__x_, s_x__,
                                                        mxxxx, sxxxx));
        params.put(Format.Luminance16, new DecodeParams(2, mxxxx, sxxxx, mxxxx, sxxxx, false));
        params.put(Format.Luminance8,  new DecodeParams(1, mxxxx, sxxxx, mxxxx, sxxxx, false));
        params.put(Format.Luminance16Alpha16, new DecodeParams(4, m__xx, mxx__, 0, 0,
                                                                  s__xx, sxx__, 0, 0,
                                                                  mxxxx, sxxxx));
        params.put(Format.Luminance16F, new DecodeParams(2, mxxxx, sxxxx, mxxxx, sxxxx, false));
        params.put(Format.Luminance16FAlpha16F, new DecodeParams(4, m__xx, mxx__, 0, 0,
                                                                    s__xx, sxx__, 0, 0,
                                                                    mxxxx, sxxxx));
        params.put(Format.Luminance32F, new DecodeParams(4, mxxxx, sxxxx, mxxxx, sxxxx, false));
        params.put(Format.Luminance8,   new DecodeParams(1, mxxxx, sxxxx, mxxxx, sxxxx, false));
        params.put(Format.RGB5A1,       new DecodeParams(2, m___1, m5___, m_5__, m__5_,
                                                            s___1, s5___, s_5__, s__5_,
                                                            mxxxx, sxxxx));
        params.put(Format.RGB565,       new DecodeParams(2, 0,     m5__ , m_6_ , m__5,
                                                            0,     s5__ , s_6_ , s__5,
                                                            mxxxx, sxxxx));
        params.put(Format.RGB8,         new DecodeParams(3, 0,     m_x__, m__x_, m___x,
                                                            0,     s_x__, s__x_, s___x,
                                                            mxxxx, sxxxx));
        params.put(Format.RGBA8,        new DecodeParams(4, m___x, mx___, m_x__, m__x_,
                                                            s___x, sx___, s_x__, s__x_,
                                                            mxxxx, sxxxx));
    }

    private static int Ix(int x, int y, int w){
        return y * w + x;
    }

    private static int readPixel(ByteBuffer buf, int idx, int bpp){
        buf.position(idx);
        int original = buf.get() & 0xff;
        while ((--bpp) > 0){
            original = (original << 8) | (buf.get() & 0xff);
        }
        return original;
    }

    public static BufferedImage convert(Image image, boolean do16bit, int mipLevel){
        DecodeParams p = params.get(image.getFormat());
        if (p == null)
            throw new UnsupportedOperationException();

        int width = image.getWidth();
        int height = image.getHeight();

        int level = mipLevel;
        while (--level >= 0){
            width  /= 2;
            height /= 2;
        }

        ByteBuffer buf = image.getData(0);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        BufferedImage out;

        boolean alpha = false;
        boolean luminance = false;
        boolean rgb = false;
        if (p.am != 0)
            alpha = true;

        if (p.rm != 0 && p.gm == 0 && p.bm == 0)
            luminance = true;
        else if (p.rm != 0 && p.gm != 0 && p.bm != 0)
            rgb = true;

        // alpha OR luminance but not both
        if ( (alpha && !rgb && !luminance) || (luminance && !alpha && !rgb) ){
            out = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }else if ( (rgb && alpha) || (luminance && alpha) ){
            if (do16bit){
                // RGB5_A1
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int[] nBits = {5, 5, 5, 1};
                int[] bOffs = {0, 1, 2, 3};
                ColorModel colorModel = new ComponentColorModel(cs, nBits, true, false,
                                                                Transparency.BITMASK,
                                                                DataBuffer.TYPE_BYTE);
                WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                                                       width, height,
                                                                       width*2, 2,
                                                                       bOffs, null);
                out = new BufferedImage(colorModel, raster, false, null);
            }else{
                out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
        }else{
            if (do16bit){
                out = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
            }else{
                out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }
        }
        
        int mipPos = 0;
        for (int i = 0; i < mipLevel; i++){
            mipPos += image.getMipMapSizes()[i];
        }
        int inputPixel;
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int i = mipPos + (Ix(x,y,width) * p.bpp);
                inputPixel = (readPixel(buf,i,p.bpp) & p.im) >> p.is;
                int a = (inputPixel & p.am) >> p.as;
                int r = (inputPixel & p.rm) >> p.rs;
                int g = (inputPixel & p.gm) >> p.gs;
                int b = (inputPixel & p.bm) >> p.bs;

                if (luminance)
                    b = g = r;

                if (!alpha)
                    a = 0xff;

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                out.setRGB(x, y, argb);
            }
        }

        return out;
    }

}
