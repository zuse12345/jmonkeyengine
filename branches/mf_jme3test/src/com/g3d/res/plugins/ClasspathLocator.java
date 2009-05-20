package com.g3d.res.plugins;

import com.g3d.res.*;
import java.io.InputStream;

public class ClasspathLocator implements ContentLocator {

    private String root = "/";

    public ClasspathLocator(){
    }

    public void setRootPath(String rootPath) {
        this.root = rootPath;
    }
    
    public InputStream locate(String name) {
        String path = null;
        if (name.startsWith(root)){
            path = name;
        }else{
            path = root + name;
        }
        return ClasspathLocator.class.getResourceAsStream(path);
    }


}
