package com.g3d.post;

import com.g3d.app.Application;
import com.g3d.material.Material;
import com.g3d.math.Vector2f;
import com.g3d.renderer.Renderer;
import com.g3d.asset.AssetManager;
import com.g3d.scene.Spatial;
import com.g3d.texture.FrameBuffer;
import com.g3d.texture.Image;
import com.g3d.texture.Image.Format;
import com.g3d.texture.Texture;
import com.g3d.texture.Texture.MagFilter;
import com.g3d.texture.Texture.MinFilter;
import com.g3d.texture.Texture2D;
import com.g3d.ui.Picture;

public class HDRRenderer {

    private static final int LUMMODE_NONE = 0x1,
                             LUMMODE_ENCODE_LUM = 0x2,
                             LUMMODE_DECODE_LUM = 0x3;

    private FrameBuffer msFB;

    private FrameBuffer mainSceneFB;
    private Texture2D mainScene;
    private FrameBuffer scene64FB;
    private Texture2D scene64;
    private FrameBuffer scene8FB;
    private Texture2D scene8;
    private FrameBuffer scene1FB[] = new FrameBuffer[2];
    private Texture2D scene1[] = new Texture2D[2];

    private Material hdr64;
    private Material hdr8;
    private Material hdr1;
    private Material tone;

    private Picture fsQuad;
    private float time = 0;
    private int curSrc = -1;
    private int oppSrc = -1;
    private float blendFactor = 0;

    private float exposure = 0.18f;
    private float whiteLevel = 100f;
    private float throttle = -1;
    private int maxIterations = -1;
    private Image.Format bufFormat = Format.RGB16F;

    private MinFilter fbMinFilter = MinFilter.BilinearNoMipMaps;
    private MagFilter fbMagFilter = MagFilter.Bilinear;
    private AssetManager manager;

    public HDRRenderer(AssetManager manager){
        this.manager = manager;
    }

    private Material createLumShader(int srcW, int srcH, int bufW, int bufH, int mode,
                                int iters, Texture tex){
        Material mat = new Material(manager, "loglum.j3md");
        
        Vector2f blockSize = new Vector2f(1f / bufW, 1f / bufH);
        Vector2f pixelSize = new Vector2f(1f / srcW, 1f / srcH);
        Vector2f blocks = new Vector2f();
        float numPixels = Float.POSITIVE_INFINITY;
        if (iters != -1){
            do {
                pixelSize.multLocal(2);
                blocks.set(blockSize.x / pixelSize.x,
                           blockSize.y / pixelSize.y);
                numPixels = blocks.x * blocks.y;
            } while (numPixels > iters);
        }else{
            blocks.set(blockSize.x / pixelSize.x,
                       blockSize.y / pixelSize.y);
            numPixels = blocks.x * blocks.y;
        }
        System.out.println(numPixels);

        mat.setBoolean("m_Blocks", true);
        if (mode == LUMMODE_ENCODE_LUM)
            mat.setBoolean("m_EncodeLum", true);
        else if (mode == LUMMODE_DECODE_LUM)
            mat.setBoolean("m_DecodeLum", true);

        mat.setTexture("m_Texture", tex);
        mat.setVector2("m_BlockSize", blockSize);
        mat.setVector2("m_PixelSize", pixelSize);
        mat.setFloat("m_NumPixels", numPixels);

        return mat;
    }

    /**
     * Creates framebuffers that do not depend on display resolution
     */
    public void loadInitial(){
        fsQuad = new Picture("HDR Fullscreen Quad");

        scene64FB = new FrameBuffer(64, 64, 0);
        scene64 = new Texture2D(64, 64, Format.Luminance8);
        scene64FB.setColorTexture(scene64);
        scene64.setMagFilter(fbMagFilter);
        scene64.setMinFilter(fbMinFilter);

        scene8FB = new FrameBuffer(8, 8, 0);
        scene8 = new Texture2D(8, 8, Format.Luminance8);
        scene8FB.setColorTexture(scene8);
        scene8.setMagFilter(fbMagFilter);
        scene8.setMinFilter(fbMinFilter);

        scene1FB[0] = new FrameBuffer(1, 1, 0);
        scene1[0] = new Texture2D(1, 1, Format.Luminance8);
        scene1FB[0].setColorTexture(scene1[0]);

        scene1FB[1] = new FrameBuffer(1, 1, 0);
        scene1[1] = new Texture2D(1, 1, Format.Luminance8);
        scene1FB[1].setColorTexture(scene1[1]);
    }

    private void createLumShaders(){
        int w = mainSceneFB.getWidth();
        int h = mainSceneFB.getHeight();
        hdr64 = createLumShader(w,  h,  64, 64, LUMMODE_ENCODE_LUM, maxIterations, mainScene);
        hdr8  = createLumShader(64, 64, 8,  8,  LUMMODE_NONE,       maxIterations, scene64);
        hdr1  = createLumShader(8,  8,  1,  1,  LUMMODE_NONE,       maxIterations, scene8);
    }

    public void load(Renderer r, int w, int h, int samples){
        if (samples > 1){
            msFB = new FrameBuffer(w, h, samples);
            msFB.setDepthBuffer(Format.Depth);
            msFB.setColorBuffer(bufFormat);
        }

        mainSceneFB = new FrameBuffer(w, h, 0);
        mainScene = new Texture2D(w, h, bufFormat);
        mainSceneFB.setDepthBuffer(Format.Depth);
        mainSceneFB.setColorTexture(mainScene);
        mainScene.setMagFilter(fbMagFilter);
        mainScene.setMinFilter(fbMinFilter);

        // prepare tonemap shader
        tone = new Material(manager, "tonemap.j3md");
        tone.setTexture("m_Texture", mainScene);
        tone.setFloat("m_A", 0.18f);
        tone.setFloat("m_White", 100);

        createLumShaders();
    }

    private int opposite(int i){
        return i == 1 ? 0 : 1;
    }

    private void renderProcessing(Renderer r, FrameBuffer dst, Material mat){
        if (dst == null){
            fsQuad.setWidth(mainSceneFB.getWidth());
            fsQuad.setHeight(mainSceneFB.getHeight());
        }else{
            fsQuad.setWidth(dst.getWidth());
            fsQuad.setHeight(dst.getHeight());
        }
        fsQuad.setMaterial(mat);
        fsQuad.updateGeometricState(0, true);

        r.setFrameBuffer(dst);
        r.clearBuffers(true, true, true);
        r.renderGeometry(fsQuad);
    }

    private void renderToneMap(Renderer r){
        tone.setFloat("m_A", exposure);
        tone.setFloat("m_White", whiteLevel);
        tone.setTexture("m_Lum", scene1[oppSrc]);
        tone.setTexture("m_Lum2", scene1[curSrc]);
        tone.setFloat("m_BlendFactor", blendFactor);
        renderProcessing(r, null, tone);
    }

    public void setBufferFormat(Format fmt){
        bufFormat = fmt;
    }

    public void setExposure(float exp){
        this.exposure = exp;
    }

    public void setWhiteLevel(float whiteLevel){
        this.whiteLevel = whiteLevel;
    }

    public void setMaxIterations(int maxIterations){
        this.maxIterations = maxIterations;

        // regenerate shaders if needed
        if (hdr64 != null)
            createLumShaders();
    }

    public void setThrottle(float throttle){
        this.throttle = throttle;
    }

    public void setUseFastFilter(boolean fastFilter){
        if (fastFilter){
            fbMagFilter = MagFilter.Nearest;
            fbMinFilter = MinFilter.NearestNoMipMaps;
        }else{
            fbMagFilter = MagFilter.Bilinear;
            fbMinFilter = MinFilter.BilinearNoMipMaps;
        }
    }

    private void updateAverageLuminance(Renderer r){
        renderProcessing(r, scene64FB, hdr64);
        renderProcessing(r, scene8FB, hdr8);
        renderProcessing(r, scene1FB[curSrc], hdr1);

//        r.setFrameBuffer(scene64FB);
//        hdr64 = prepareLum(w, h, 64, 64, 1, iters, mainScene, hdr64);
//        r.clearBuffers(true, true, true);
//        r.renderGeometry(fsQuad);
//
//        r.setFrameBuffer(scene8FB);
//        hdr8 = prepareLum(64, 64, 8, 8, 0, iters, scene64, hdr8);
//        r.clearBuffers(true, true, true);
//        r.renderGeometry(fsQuad);
//
//        r.setFrameBuffer(scene1FB[curSrc]);
//        hdr1 = prepareLum(8, 8, 1, 1, 2, iters, scene8, hdr1);
//        r.clearBuffers(true, true, true);
//        r.renderGeometry(fsQuad);
    }

    public void update(float tpf, Application a, Spatial obj){
        time += tpf;
        blendFactor = (time / throttle);
        
        Renderer r = a.getRenderer();
        if (msFB != null){
            // first render to multisampled FB
            r.setFrameBuffer(msFB);
            r.clearBuffers(true,true,true);
            a.render(obj, r);
            r.renderQueue();

            // render back to non-multisampled FB
            r.copyFrameBuffer(msFB, mainSceneFB);
        }else{
            r.setFrameBuffer(mainSceneFB);
            r.clearBuffers(true,true,false);
            a.render(obj, r);
            r.renderQueue();
        }
        
        // should we update avg lum?
        if (throttle == -1){
            // update every frame
            curSrc = 0;
            oppSrc = 0;
            blendFactor = 0;
            time = 0;
            updateAverageLuminance(r);
        }else{
            if (curSrc == -1){
                curSrc = 0;
                oppSrc = 0;

                // initial update
                updateAverageLuminance(r);

                blendFactor = 0;
                time = 0;
            }else if (time > throttle){

                // time to switch
                oppSrc = curSrc;
                curSrc = opposite(curSrc);

                updateAverageLuminance(r);

                blendFactor = 0;
                time = 0;
            }
        }
        renderToneMap(r);
    }

}
