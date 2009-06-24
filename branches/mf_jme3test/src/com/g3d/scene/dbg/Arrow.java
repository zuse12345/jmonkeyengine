package com.g3d.scene.dbg;

import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.util.TempVars;

public class Arrow extends Mesh {

    private static final float[] positions = new float[]{
                    0,     0,      0,
                    0,     0,      1, // tip
                    0.05f,  0,   0.9f, // tip right
                   -0.05f,  0,   0.9f, // tip left
                    0,   0.05f,  0.9f, // tip top
                    0,  -0.05f,  0.9f, // tip buttom
    };

    public Arrow(Vector3f extent){
        float len = extent.length();
        Vector3f dir = extent.normalize();
        
        Quaternion tempQuat = TempVars.get().quat1;
        tempQuat.lookAt(dir, Vector3f.UNIT_Y);
        tempQuat.normalize();
        Vector3f tempVec = TempVars.get().vect1;

        float[] newPositions = new float[positions.length];
        for (int i = 0; i < positions.length; i+=3){
            Vector3f vec = tempVec.set(positions[i],
                                       positions[i+1],
                                       positions[i+2]);
            vec.multLocal(len);
            tempQuat.mult(vec, vec);

            newPositions[i]   = vec.getX();
            newPositions[i+1] = vec.getY();
            newPositions[i+2] = vec.getZ();
        }
        setBuffer(Type.Position, 3, newPositions);
        setBuffer(Type.Index, 2,
                new int[]{
                    0, 1,
                    1, 2,
                    1, 3,
                    1, 4,
                    1, 5,
                }
        );
        setMode(Mode.Lines);
    }
}
