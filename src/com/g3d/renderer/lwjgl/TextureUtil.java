package com.g3d.renderer.lwjgl;

import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.ARBHalfFloatPixel;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.EXTPackedFloat;
import static org.lwjgl.opengl.ARBTextureCompression.*;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;

public class TextureUtil {

    public static int convertTextureFormat(Format fmt){
        switch (fmt){
            case Alpha16:
                return GL_ALPHA16;
            case Alpha8:
                return GL_ALPHA8;
            case DXT1:
                return GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
            case DXT1A:
                return GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
            case DXT3:
                return GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
            case DXT5:
                return GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
            case Depth:
                return GL_DEPTH_COMPONENT;
            case Depth16:
                return GL_DEPTH_COMPONENT16;
            case Depth24:
                return GL_DEPTH_COMPONENT24;
            case Depth32:
                return GL_DEPTH_COMPONENT32;
            case Intensity8:
                return GL_INTENSITY8;
            case Intensity16:
                return GL_INTENSITY16;
            case Luminance8:
                return GL_LUMINANCE8;
            case Luminance16:
                return GL_LUMINANCE16;
            case RGB10:
                return GL_RGB10;
            case RGB16:
                return GL_RGB16;
            case RGB111110F:
                return EXTPackedFloat.GL_R11F_G11F_B10F_EXT;
            case RGB16F:
                return ARBTextureFloat.GL_RGB16F_ARB;
            case RGB32F:
                return ARBTextureFloat.GL_RGB32F_ARB;
            case RGB5A1:
                return GL_RGB5_A1;
            case RGB8:
                return GL_RGB8;
            case RGBA16:
                return GL_RGBA16;
            case RGBA8:
                return GL_RGBA8;
            default:
                throw new UnsupportedOperationException("Unrecognized format: "+fmt);
        }
    }

    public static void uploadTexture(Image img,
                                     int target, 
                                     int border){

        Image.Format fmt = img.getFormat();
        ByteBuffer data;
        if (img.getData() != null && img.getData().size() > 0){
            data = img.getData(0);
        }else{
            data = null;
        }

        int width = img.getWidth();
        int height = img.getHeight();
        int depth = img.getDepth();

        boolean compress = false;
        int internalFormat = -1;
        int format = -1;
        int dataType = -1;

        switch (fmt){
            case Alpha16:
                internalFormat = GL_ALPHA16;
                format = GL_ALPHA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case Alpha8:
                internalFormat = GL_ALPHA8;
                format = GL_ALPHA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case DXT1:
                compress = true;
                internalFormat = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
                format = GL_RGB;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case DXT1A:
                compress = true;
                internalFormat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
                format = GL_RGBA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case DXT3:
                compress = true;
                internalFormat = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
                format = GL_RGBA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case DXT5:
                compress = true;
                internalFormat = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
                format = GL_RGBA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case Depth:
                internalFormat = GL_DEPTH_COMPONENT;
                format = GL_DEPTH_COMPONENT;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case Intensity8:
                internalFormat = GL_INTENSITY8;
                format = GL_INTENSITY;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case Intensity16:
                internalFormat = GL_INTENSITY16;
                format = GL_INTENSITY;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case Luminance16:
                internalFormat = GL_LUMINANCE16;
                format = GL_LUMINANCE;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case Luminance8:
                internalFormat = GL_LUMINANCE8;
                format = GL_LUMINANCE;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case RGB10:
                internalFormat = GL_RGB10;
                format = GL_RGB;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case RGB16:
                internalFormat = GL_RGB16;
                format = GL_RGB;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case RGB111110F:
                internalFormat = EXTPackedFloat.GL_R11F_G11F_B10F_EXT;
                format = GL_RGB;
                dataType = EXTPackedFloat.GL_UNSIGNED_INT_10F_11F_11F_REV_EXT;
                break;
            case RGB16F:
                internalFormat = ARBTextureFloat.GL_RGB16F_ARB;
                format = GL_RGB;
                dataType = ARBHalfFloatPixel.GL_HALF_FLOAT_ARB;
                break;
            case RGB32F:
                internalFormat = ARBTextureFloat.GL_RGB32F_ARB;
                format = GL_RGB;
                dataType = GL_FLOAT;
                break;
            case RGB5A1:
                internalFormat = GL_RGB5_A1;
                format = GL_RGBA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case RGB8:
                internalFormat = GL_RGB8;
                format = GL_RGB;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case RGBA16:
                internalFormat = GL_RGBA16;
                format = GL_RGBA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            case RGBA8:
                internalFormat = GL_RGBA8;
                format = GL_RGBA;
                dataType = GL_UNSIGNED_BYTE;
                break;
            default:
                throw new UnsupportedOperationException("Unrecognized format: "+fmt);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        int[] mipSizes = img.getMipMapSizes();
        int pos = 0;
        if (mipSizes == null){
            if (data != null)
                mipSizes = new int[]{ data.capacity() };
            else
                mipSizes = new int[]{ width * height * fmt.getBitsPerPixel() / 8 };
        }

        for (int i = 0; i < mipSizes.length; i++){
            int mipWidth =  Math.max(1, width  >> i);
            int mipHeight = Math.max(1, height >> i);
            int mipDepth =  Math.max(1, depth  >> i);

            if (data != null){
                data.position(pos);
                data.limit(pos + mipSizes[i]);
            }
            
            if (compress && data != null){
                if (target == GL_TEXTURE_1D){
                    glCompressedTexImage1D(target,
                                           i,
                                           internalFormat,
                                           mipWidth,
                                           border,
                                           data);
                }else if (target == GL_TEXTURE_2D){
                    glCompressedTexImage2D(target,
                                           i,
                                           internalFormat,
                                           mipWidth,
                                           mipHeight,
                                           border,
                                           data);
                }else if (target == GL_TEXTURE_3D){
                    glCompressedTexImage3D(target,
                                           i,
                                           internalFormat,
                                           mipWidth,
                                           mipHeight,
                                           mipDepth,
                                           border,
                                           data);
                }
            }else{
                if (target == GL_TEXTURE_1D){
                    glTexImage1D(target,
                                 i,
                                 internalFormat,
                                 mipWidth,
                                 border,
                                 format,
                                 dataType,
                                 data);
                }else if (target == GL_TEXTURE_2D){
                    glTexImage2D(target,
                                 i,
                                 internalFormat,
                                 mipWidth,
                                 mipHeight,
                                 border,
                                 format,
                                 dataType,
                                 data);
                }else if (target == GL_TEXTURE_3D){
                    glTexImage3D(target,
                                 i,
                                 internalFormat,
                                 mipWidth,
                                 mipHeight,
                                 mipDepth,
                                 border,
                                 format,
                                 dataType,
                                 data);
                }
            }
            
            pos += mipSizes[i];
        }
    }

}
