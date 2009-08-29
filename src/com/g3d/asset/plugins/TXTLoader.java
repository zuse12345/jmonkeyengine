package com.g3d.asset.plugins;

import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetLoader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TXTLoader implements AssetLoader {

    public Object load(AssetInfo info) throws IOException {
        StringBuilder sb = new StringBuilder();

        InputStreamReader r = new InputStreamReader(info.openStream());
        char[] buf = new char[128];
        while (r.ready()){
            int read = r.read(buf);
            if (read <= 0)
                break;

            sb.append(buf, 0, read);
        }
        r.close();

        return sb.toString();
    }

}
