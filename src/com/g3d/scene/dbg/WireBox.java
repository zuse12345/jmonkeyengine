package com.g3d.scene.dbg;

import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer.Type;

public class WireBox extends Mesh {

    public WireBox(float xExt, float yExt, float zExt){
        setBuffer(Type.Position, 3,
                new float[]{
                    -xExt, -yExt,  zExt,
                     xExt, -yExt,  zExt,
                     xExt,  yExt,  zExt,
                    -xExt,  yExt,  zExt,

                    -xExt, -yExt, -zExt,
                     xExt, -yExt, -zExt,
                     xExt,  yExt, -zExt,
                    -xExt,  yExt, -zExt,
                }
        );
        setBuffer(Type.Index, 2,
                new short[]{
                     0, 1,
                     1, 2,
                     2, 3,
                     3, 0,

                     4, 5,
                     5, 6,
                     6, 7,
                     7, 4,

                     0, 4,
                     1, 5,
                     2, 6,
                     3, 7,
                }
        );
        setMode(Mode.Lines);
    }

}
