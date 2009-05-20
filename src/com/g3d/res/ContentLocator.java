package com.g3d.res;

import java.io.InputStream;

public interface ContentLocator {
    public void setRootPath(String rootPath);
    public InputStream locate(String name);
}
