package com.g3d.scene.dbg;

import com.g3d.scene.Mesh;
import com.g3d.scene.Mesh.Mode;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Grid extends Mesh {

    public Grid(int xLines, int yLines, float lineDist){
        int lineCount = xLines + yLines;


        FloatBuffer fpb = BufferUtils.createFloatBuffer(6 * lineCount);
        ShortBuffer sib = BufferUtils.createShortBuffer(2 * lineCount);

        float xLineLen = (yLines + 1) * lineDist;
        float yLineLen = (xLines + 1) * lineDist;
        int curIndex = 0;

        // add lines along X
        for (int i = 0; i < xLines; i++){
            float y = (i+1) * lineDist;

            // positions
            fpb.put(0)       .put(y).put(0);
            fpb.put(xLineLen).put(y).put(0);

            // indices
            sib.put( (short) (curIndex++) );
            sib.put( (short) (curIndex++) );
        }

        // add lines along Y
        for (int i = 0; i < yLines; i++){
            float x = (i+1) * lineDist;

            // positions
            fpb.put(x).put(0)       .put(0);
            fpb.put(x).put(yLineLen).put(0);

            // indices
            sib.put( (short) (curIndex++) );
            sib.put( (short) (curIndex++) );
        }

        fpb.flip();
        sib.flip();

        setBuffer(Type.Position, 3, fpb);
        setBuffer(Type.Index, 2, sib);
        
        setMode(Mode.Lines);
    }
}
