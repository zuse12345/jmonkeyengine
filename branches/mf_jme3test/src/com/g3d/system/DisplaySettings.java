package com.g3d.system;

import java.util.HashMap;

public class DisplaySettings extends HashMap<String, Object> {

    private static final DisplaySettings defaults = new DisplaySettings();

    public static final String LWJGL_OPENGL2 = "LWJGL-OpenGL2",
                               LWJGL_OPENGL3 = "LWJGL-OpenGL3";

    static {
        defaults.put("Width", 640);
        defaults.put("Height", 480);
        defaults.put("BitsPerPixel", 32);
        defaults.put("Frequency", 60);
        defaults.put("DepthBits", 8);
        defaults.put("StencilBits", 0);
        defaults.put("Samples", 0);
        defaults.put("VSync", false);
        defaults.put("Fullscreen", false);
        defaults.put("Title", G3DSystem.getFullName());
        defaults.put("Renderer", LWJGL_OPENGL2);
        defaults.put("DisableJoysticks", true);
    }

    private Template template;

    public DisplaySettings(){
        template = Template.None;
    }

    public static enum Template {
        None,
        DesktopFullscreen,
        Default640x480,
        Default800x600,
        Default1024x768,
        Default1280x720
    }

    public DisplaySettings(Template template){
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

    public void copyFrom(DisplaySettings other){
        this.putAll(other);
        this.template = other.template;
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
