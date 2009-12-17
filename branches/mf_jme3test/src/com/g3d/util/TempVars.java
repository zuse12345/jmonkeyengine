package com.g3d.util;

import com.g3d.math.Eigen3f;
import com.g3d.math.Matrix4f;
import com.g3d.math.Matrix3f;
import com.g3d.math.Plane;
import com.g3d.math.Quaternion;
import com.g3d.math.Triangle;
import com.g3d.math.Vector2f;
import com.g3d.math.Vector3f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Temporary variables assigned to each thread. Engine classes may access
 * these temp variables with TempVars.get(). A method using temp vars with this
 * class is not allowed to make calls to other methods using the class otherwise
 * memory corruption will occur. A locking mechanism may be implemented
 * in the future to prevent the occurance of such situation.
 */
public class TempVars {

    private static final ThreadLocal<TempVars> varsLocal
            = new ThreadLocal<TempVars>(){
        @Override
        public TempVars initialValue(){
            return new TempVars();
        }
    };

    public static TempVars get(){
        return varsLocal.get();
    }

    private TempVars(){
    }

    private boolean locked = false;
    private StackTraceElement[] lockerStack;

    public final boolean lock(){
        if (locked){
           System.err.println("INTERNAL ERROR");
           System.err.println("Offending trace: ");

           StackTraceElement[] stack = new Throwable().getStackTrace();
           for (int i = 1; i < stack.length; i++){
               System.err.println("\tat "+stack[i].toString());
           }

           System.err.println("Attempted to aquire TempVars lock owned by");
           for (int i = 1; i < lockerStack.length; i++){
               System.err.println("\tat "+lockerStack[i].toString());
           }
           System.exit(1);
           return false;
        }

        lockerStack = new Throwable().getStackTrace();
        locked = true;
        return true;
    }

    public final boolean unlock(){
        if (!locked){
            System.err.println("INTERNAL ERROR");
            System.err.println("Attempted to release non-existent lock: ");

            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (int i = 1; i < stack.length; i++){
                System.err.println("\tat "+stack[i].toString());
            }

            System.exit(1);
            return false;
        }

        lockerStack = null;
        locked = false;
        return true;
    }

    /**
     * For interfacing with OpenGL in Renderer.
     */
    public final IntBuffer intBuffer1 = BufferUtils.createIntBuffer(1);
    public final IntBuffer intBuffer16 = BufferUtils.createIntBuffer(16);
    public final FloatBuffer floatBuffer16 = BufferUtils.createFloatBuffer(16);

    /**
     * Fetching triangle from mesh
     */
    public final Triangle triangle = new Triangle();

    /**
     * General vectors.
     */
    public final Vector3f vect1 = new Vector3f();
    public final Vector3f vect2 = new Vector3f();
    public final Vector3f vect3 = new Vector3f();
    public final Vector3f vect4 = new Vector3f();
    public final Vector3f vect5 = new Vector3f();

    public final Vector3f[] tri = { new Vector3f(),
                                    new Vector3f(),
                                    new Vector3f() };

    /**
     * 2D vector
     */
    public final Vector2f vect2d = new Vector2f();

    /**
     * General matrices.
     */
    public final Matrix3f tempMat3 = new Matrix3f();
    public final Matrix4f tempMat4 = new Matrix4f();

    /**
     * General quaternions.
     */
    public final Quaternion quat1 = new Quaternion();

    /**
     * Eigen
     */
    public final Eigen3f eigen = new Eigen3f();

    /**
     * Plane
     */
     public final Plane plane = new Plane();

    /**
     * BoundingBox ray collision
     */
    public final float[] fWdU = new float[3];
    public final float[] fAWdU = new float[3];
    public final float[] fDdU = new float[3];
    public final float[] fADdU = new float[3];
    public final float[] fAWxDdU = new float[3];

    /**
     * BIHTree
     */
    public final float[] bihSwapTmp = new float[9];

}
