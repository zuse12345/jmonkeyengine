package g3dtools.converters.model;

import com.g3d.scene.IndexBuffer;
import com.g3d.scene.Mesh;
import com.g3d.scene.Mesh.Mode;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Format;
import com.g3d.scene.VertexBuffer.Type;
import g3dtools.converters.model.strip.PrimitiveGroup;
import g3dtools.converters.model.strip.TriStrip;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class ModelConverter {

    private static final class PrimComparator
            implements Comparator<PrimitiveGroup> {

        public int compare(PrimitiveGroup g1, PrimitiveGroup g2) {
            if (g1.type < g2.type)
                return -1;
            else if (g1.type > g2.type)
                return 1;
            else
                return 0;
        }
    }

    private static final PrimComparator primComp = new PrimComparator();

    public static void generateStrips(Mesh mesh, boolean stitch, boolean listOnly, int cacheSize, int minStripSize){
        TriStrip ts = new TriStrip();
        ts.setStitchStrips(stitch);
        ts.setCacheSize(cacheSize);
        ts.setListsOnly(listOnly);
        ts.setMinStripSize(minStripSize);

        IndexBuffer ib = mesh.getIndexBuffer();
        int[] indices = new int[ib.size()];
        for (int i = 0; i < indices.length; i++)
            indices[i] = ib.get(i);

        PrimitiveGroup[] groups = ts.generateStrips(indices);
        Arrays.sort(groups, primComp);

        int numElements = 0;
        for (PrimitiveGroup group : groups)
            numElements += group.numIndices;

        VertexBuffer original = mesh.getBuffer(Type.Index);
        Buffer buf = VertexBuffer.createBuffer(original.getFormat(),
                                               original.getNumComponents(),
                                               numElements);
        original.updateData(buf);
        ib = mesh.getIndexBuffer();

        mesh.setMode(Mode.Hybrid);

        int curIndex = 0;
        int[] modeStart = new int[]{ -1, -1, -1 };
        int[] elementLengths = new int[groups.length];
        for (int i = 0; i < groups.length; i++){
            PrimitiveGroup group = groups[i];
            elementLengths[i] = group.numIndices;

            if (modeStart[group.type] == -1){
                modeStart[group.type] = i;
            }

            int[] trimmedIndices = group.getTrimmedIndices();
            for (int j = 0; j < trimmedIndices.length; j++){
                ib.put(curIndex + j, trimmedIndices[j]);
            }

            curIndex += group.numIndices;
        }

        mesh.setElementLengths(elementLengths);
        mesh.setModeStart(modeStart);
    }

    public static void compressIndexBuffer(Mesh mesh){
        int vertCount = mesh.getVertexCount();
        VertexBuffer vb = mesh.getBuffer(Type.Index);
        Format targetFmt = vb.getFormat();
        if (vb.getFormat() == Format.UnsignedInt && vertCount <= 0xffff){
            if (vertCount <= 256)
                targetFmt = Format.UnsignedByte;
            else
                targetFmt = Format.UnsignedShort;
        }else if (vb.getFormat() == Format.UnsignedShort && vertCount <= 0xff){
            targetFmt = Format.UnsignedByte;
        }

        if (vb.getFormat() == targetFmt)
            return;

        IndexBuffer src = mesh.getIndexBuffer();
        Buffer newBuf = VertexBuffer.createBuffer(targetFmt, vb.getNumComponents(), src.size());

        VertexBuffer newVb = new VertexBuffer(Type.Index);
        newVb.setupData(vb.getUsage(), vb.getNumComponents(), targetFmt, newBuf);
        mesh.clearBuffer(Type.Index);
        mesh.setBuffer(newVb);

        IndexBuffer dst = mesh.getIndexBuffer();
        for (int i = 0; i < src.size(); i++){
            dst.put(i, src.get(i));
        }
    }

    private static boolean getBoolean(Map<String, String> params, String param){
        String val = params.get(param);
        return val != null && val.equals("true");
    }

    private static int getInt(Map<String, String> params, String param){
        String val = params.get(param);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException ex){
            return -1;
        }
    }

    public static void convertMeshForAndroid(Mesh mesh){
        compressIndexBuffer(mesh);
        generateStrips(mesh, false, false, 24, 4);
//        FloatToFixed

    }

    public static void convertMesh(Mesh mesh, Map<String, String> params){
        Format tcFmt  = Format.valueOf(params.get("buffer.texcoord.format"));
        Format posFmt = Format.valueOf(params.get("buffer.position.format"));
        Format clrFmt = Format.valueOf(params.get("buffer.color.format"));
        Format nmFmt = Format.valueOf(params.get("buffer.normal.format"));
        boolean strip = getBoolean(params, "tristrip.enabled");
        boolean compIdx = getBoolean(params, "buffer.index.compress");

        if (strip){
            int cacheSize = getInt(params, "tristrip.cachesize");
            boolean listOnly = getBoolean(params, "tristrip.listonly");
            int minStripSize = getInt(params, "tristrip.minstripsize");
            
        }
    }

}
