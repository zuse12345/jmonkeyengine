package com.g3d.system;

import java.io.IOException;
import java.util.HashMap;

public class AppSettings extends HashMap<String, Object> {

    private static final AppSettings defaults = new AppSettings();

    public static final String LWJGL_OPENGL2 = "LWJGL-OpenGL2",
                               LWJGL_OPENGL3 = "LWJGL-OpenGL3",
                               JOGL          = "JOGL";

    static {
        defaults.put("Width", 640);
        defaults.put("Height", 480);
        defaults.put("BitsPerPixel", 24);
        defaults.put("Frequency", 60);
        defaults.put("DepthBits", 24);
        defaults.put("StencilBits", 0);
        defaults.put("Samples", 0);
        defaults.put("Fullscreen", false);
        defaults.put("Title", G3DSystem.getFullName());
        defaults.put("Renderer", LWJGL_OPENGL2);
        defaults.put("DisableJoysticks", true);
        defaults.put("UseInput", true);

        defaults.put("VSync", false);
        defaults.put("FrameRate", -1);

        // disable these settings to benchmark speed
//        defaults.put("VSync", true);
        defaults.put("FrameRate", 60);
    }

    private Template template;

    public AppSettings(){
        template = Template.None;
    }

    public static enum Template {
        None,
        DesktopFullscreen,
        Default320x240,
        Default640x480,
        Default800x600,
        Default1024x768,
        Default1280x720
    }

    public AppSettings(Template template){
        this.template = template;
        if (template == Template.None){
            // ?
            return;
        }else if (template == Template.DesktopFullscreen){
            // .. handled by context
            putAll(defaults);
            setFullscreen(true);
            setVSync(true);
            return;
        }

        putAll(defaults);
        if (template == Template.Default640x480){
            // nothing
        }else if (template == Template.Default320x240){
            setResolution(320, 240);
        }else if (template == Template.Default800x600){
            setResolution(800, 600);
        }else if (template == Template.Default1024x768){
            setResolution(1024, 768);
        }else if (template == Template.Default1280x720){
            setResolution(1280, 720);
        }else{
            throw new RuntimeException("Unsupported settings template");
        }
    }

    public Template getTemplate() {
        return template;
    }

    public void copyFrom(AppSettings other){
        this.putAll(other);
        this.template = other.template;
    }

    public void save() throws IOException{
    }

    public int getInteger(String key){
        Integer i = (Integer) get(key);
        if (i == null)
            return 0;

        return i.intValue();
    }

    public boolean getBoolean(String key){
        Boolean b = (Boolean) get(key);
        if (b == null)
            return false;

        return b.booleanValue();
    }

    public String getString(String key){
        String s = (String) get(key);
        if (s == null)
            return null;

        return s;
    }

    public void putInteger(String key, int value){
        put(key, new Integer(value));
    }

    public void putBoolean(String key, boolean value){
        put(key, new Boolean(value));
    }

    public void putString(String key, String value){
        put(key, value);
    }

    public void setFrameRate(int frameRate){
        putInteger("FrameRate", frameRate);
    }

    public void setUseInput(boolean use){
        putBoolean("UseInput", use);
    }

    public void setRenderer(String renderer){
        putString("Renderer", renderer);
    }

    public void setWidth(int value){
        putInteger("Width", value);
    }

    public void setHeight(int value){
        putInteger("Height", value);
    }

    public void setResolution(int width, int height){
        setWidth(width);
        setHeight(height);
    }

    public void setFrequency(int value){
        putInteger("Frequency", value);
    }

    public void setBitsPerPixel(int value){
        putInteger("BitsPerPixel", value);
    }

    public void setSamples(int value){
        putInteger("Samples", value);
    }

    public void setTitle(String title){
        putString("Title", title);
    }

    public void setFullscreen(boolean value){
        putBoolean("Fullscreen", value);
    }

    public void setVSync(boolean value){
        putBoolean("VSync", value);
    }

    public int getFrameRate(){
        return getInteger("FrameRate");
    }

    public boolean useInput(){
        return getBoolean("UseInput");
    }

    public String getRenderer(){
        return getString("Renderer");
    }

    public int getWidth(){
        return getInteger("Width");
    }

    public int getHeight(){
        return getInteger("Height");
    }

    public int getBitsPerPixel(){
        return getInteger("BitsPerPixel");
    }

    public int getFrequency(){
        return getInteger("Frequency");
    }

    public int getDepthBits(){
        return getInteger("DepthBits");
    }

    public int getStencilBits(){
        return getInteger("StencilBits");
    }

    public int getSamples(){
        return getInteger("Samples");
    }

    public String getTitle() {
        return getString("Title");
    }

    public boolean isVSync(){
        return getBoolean("VSync");
    }

    public boolean isFullscreen(){
        return getBoolean("Fullscreen");
    }
}
