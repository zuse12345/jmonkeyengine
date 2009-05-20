package com.g3d.res;

import java.io.IOException;
import java.io.InputStream;

public interface ContentLoader {
    public void setOwner(ContentManager owner);
    public Object load(InputStream stream, String extension) throws IOException;
}
