package com.g3d.res.plugins;

import com.g3d.res.ContentLoader;
import com.g3d.res.ContentManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TXTLoader implements ContentLoader {

    public Object load(ContentManager owner, InputStream in, String extension) throws IOException {
        StringBuilder sb = new StringBuilder();

        InputStreamReader r = new InputStreamReader(in);
        char[] buf = new char[128];
        while (r.ready()){
            int read = r.read(buf);
            if (read <= 0)
                break;

            sb.append(buf, 0, read);
        }

        return sb.toString();
    }

}
