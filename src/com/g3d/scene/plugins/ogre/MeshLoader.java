package com.g3d.scene.plugins.ogre;

import com.g3d.animation.BoneAnimation;
import com.g3d.animation.Model;
import com.g3d.asset.AssetInfo;
import com.g3d.asset.AssetKey;
import com.g3d.asset.AssetLoader;
import com.g3d.asset.AssetManager;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.Node;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Format;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.scene.VertexBuffer.Usage;
import com.g3d.util.BufferUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import static com.g3d.util.xml.SAXUtil.*;

/**
 * Loads Ogre3D mesh.xml files.
 */
public class MeshLoader extends DefaultHandler implements AssetLoader {

    private static final Logger logger = Logger.getLogger(MeshLoader.class.getName());

    public static boolean AUTO_INTERLEAVE = true;

    private String meshName;
    private AssetManager assetManager;
    private OgreMaterialList materialList;

    private ShortBuffer sb;
    private IntBuffer ib;
    private FloatBuffer fb;
    private VertexBuffer vb;
    private Mesh mesh;
    private Geometry geom;
    private int geomIdx = 0;
    private int texCoordIdx = 0;
    private static volatile int nodeIdx = 0;
    private String ignoreUntilEnd = null;

    private List<Geometry> geoms = new ArrayList<Geometry>();
    private AnimData animData;

    private ByteBuffer weightsData, indicesData;

    public MeshLoader(){
        super();
    }

    @Override
    public void startDocument() {
        geoms.clear();
    }

    @Override
    public void endDocument() {
    }

    private void pushFace(String v1, String v2, String v3) throws SAXException{
        int i1 = parseInt(v1);

        // TODO: fan/strip support
        int i2 = parseInt(v2);
        int i3 = parseInt(v3);
        if (ib != null){
            ib.put(i1).put(i2).put(i3);
        }else{
            sb.put((short)i1).put((short)i2).put((short)i3);
        }
    }

    private void startFaces(String count) throws SAXException{
        int numFaces = parseInt(count);
        int numIndices;

        if (mesh.getMode() == Mesh.Mode.Triangles){
            mesh.setTriangleCount(numFaces);
            numIndices = numFaces * 3;
        }else{
            throw new SAXException("Triangle strip or fan not supported (yet)!");
            // TODO: Support triangle strip/fan in Ogre3D Loader.
        }

        vb = mesh.getBuffer(Type.Index);
        if (vb.getFormat() == Format.UnsignedShort){
            sb = BufferUtils.createShortBuffer(numIndices);
            vb.updateData(sb);
        }else{
            ib = BufferUtils.createIntBuffer(numIndices);
            vb.updateData(ib);
        }
    }

    private void applyMaterial(Geometry geom, String matName){
//        Material mat = new Material(assetManager, "phong_lighting.j3md");
//        mat.setFloat("m_Shininess", 32f);
//        geom.setMaterial(mat);
//        geom.setMaterial(new Material(assetManager, "vertex_color.j3md"));
        Material mat = null;
        if (materialList != null){
            mat = materialList.get(matName);
        }
        if (mat == null){
            logger.warning("Material "+matName+" not found. Applying default material");
            mat = assetManager.loadMaterial("red_color.j3m");
        }
        
        if (mat.isTransparent())
            geom.setQueueBucket(Bucket.Transparent);
            
        geom.setMaterial(mat);
    }

    private void startMesh(String matName, String usesharedvertices, String use32bitIndices, String opType) throws SAXException{
        mesh = new Mesh();
        if (opType == null || opType.equals("triangle_list")){
            mesh.setMode(Mesh.Mode.Triangles);
        }else if (opType.equals("triangle_strip")){
            mesh.setMode(Mesh.Mode.TriangleStrip);
        }else if (opType.equals("triangle_fan")){
            mesh.setMode(Mesh.Mode.TriangleFan);
        }

        vb = new VertexBuffer(VertexBuffer.Type.Index);
        if (parseBool(use32bitIndices, false)){
            vb.setupData(Usage.Static, 3, Format.UnsignedInt, null);
        }else{
            vb.setupData(Usage.Static, 3, Format.UnsignedShort, null);
        }
        mesh.setBuffer(vb);
        vb = null;

        if (meshName == null)
            geom = new Geometry("OgreSubmesh-"+(++geomIdx), mesh);
        else
            geom = new Geometry(meshName+"-geom-"+(++geomIdx), mesh);

        applyMaterial(geom, matName);
        geoms.add(geom);
    }

    private void startGeometry(String vertexcount) throws SAXException{
        int vertCount = parseInt(vertexcount);
        mesh.setVertexCount(vertCount);
    }

        int maxWeightsPerVert = 0;

    /**
     * Normalizes weights if needed and finds largest amount of weights used
     * for all vertices in the buffer.
     */
    private void endBoneAssigns(){
        int vertCount = mesh.getVertexCount();
        weightsData.rewind();
        for (int v = 0; v < vertCount; v++){
            int   w0 = weightsData.get() & 0xFF,
                  w1 = weightsData.get() & 0xFF,
                  w2 = weightsData.get() & 0xFF,
                  w3 = weightsData.get() & 0xFF;

            if (w3 != 0){
                maxWeightsPerVert = Math.max(maxWeightsPerVert, 4);
            }else if (w2 != 0){
                maxWeightsPerVert = Math.max(maxWeightsPerVert, 3);
            }else if (w1 != 0){
                maxWeightsPerVert = Math.max(maxWeightsPerVert, 2);
            }else if (w0 != 0){
                maxWeightsPerVert = Math.max(maxWeightsPerVert, 1);
            }

            float w0f = w0 / 254f,
                  w1f = w1 / 254f,
                  w2f = w2 / 254f,
                  w3f = w3 / 254f;
            float sum = w0f + w1f + w2f + w3f;
            if (sum != 1f){
                weightsData.position(weightsData.position()-4);
                // compute new vals based on sum
                float sumToB = 254f / sum;
                weightsData.put( (byte) Math.round(w0f * sumToB) );
                weightsData.put( (byte) Math.round(w1f * sumToB) );
                weightsData.put( (byte) Math.round(w2f * sumToB) );
                weightsData.put( (byte) Math.round(w3f * sumToB) );
            }
        }
        weightsData.rewind();

        weightsData = null;
        indicesData = null;
    }

    private void startBoneAssigns(){
        // current mesh will have bone assigns
        int vertCount = mesh.getVertexCount();
        // each vertex has
        // - 4 bone weights
        // - 4 bone indices
        weightsData = BufferUtils.createByteBuffer(vertCount * 4);
        indicesData = BufferUtils.createByteBuffer(vertCount * 4);

        VertexBuffer weights = new VertexBuffer(Type.BoneWeight);
        VertexBuffer indices = new VertexBuffer(Type.BoneIndex);

        weights.setupData(Usage.Static, 4, Format.UnsignedByte, weightsData);
        indices.setupData(Usage.Static, 4, Format.UnsignedByte, indicesData);
        
        mesh.setBuffer(weights);
        mesh.setBuffer(indices);
    }

    private void startVertexBuffer(Attributes attribs) throws SAXException{
        if (parseBool(attribs.getValue("positions"), false)){
            vb = new VertexBuffer(Type.Position);
            fb = BufferUtils.createFloatBuffer(mesh.getVertexCount() * 3);
            vb.setupData(Usage.Static, 3, Format.Float, fb);
            mesh.setBuffer(vb);
        }
        if (parseBool(attribs.getValue("normals"), false)){
            vb = new VertexBuffer(Type.Normal);
            fb = BufferUtils.createFloatBuffer(mesh.getVertexCount() * 3);
            vb.setupData(Usage.Static, 3, Format.Float, fb);
            mesh.setBuffer(vb);
        }
        if (parseBool(attribs.getValue("colours_diffuse"), false)){
//            vb = new VertexBuffer(Type.Color);
//            ByteBuffer bb = BufferUtils.createByteBuffer(mesh.getVertexCount() * 4);
//            vb.setupData(Usage.Static, 4, Format.UnsignedByte, bb);
//            mesh.setBuffer(vb);
            vb = new VertexBuffer(Type.Color);
            fb = BufferUtils.createFloatBuffer(mesh.getVertexCount() * 4);
            vb.setupData(Usage.Static, 4, Format.Float, fb);
            mesh.setBuffer(vb);
        }
        if (parseBool(attribs.getValue("tangents"), false)){
            vb = new VertexBuffer(Type.Tangent);
            fb = BufferUtils.createFloatBuffer(mesh.getVertexCount() * 3);
            vb.setupData(Usage.Static, 3, Format.Float, fb);
            mesh.setBuffer(vb);
        }
        if (parseBool(attribs.getValue("binormals"), false)){
            vb = new VertexBuffer(Type.Binormal);
            fb = BufferUtils.createFloatBuffer(mesh.getVertexCount() * 3);
            vb.setupData(Usage.Static, 3, Format.Float, fb);
            mesh.setBuffer(vb);
        }


        int texCoords = parseInt(attribs.getValue("texture_coords"), 0);
        if (texCoords >= 1){
            int dims = parseInt(attribs.getValue("texture_coord_dimensions_0"), 2);
            if (dims < 1 || dims > 4)
                throw new SAXException("Texture coord dimensions must be 1 <= dims <= 4");

            vb = new VertexBuffer(Type.TexCoord);
            fb = BufferUtils.createFloatBuffer(mesh.getVertexCount() * dims);
            vb.setupData(Usage.Static, dims, Format.Float, fb);
            mesh.setBuffer(vb);
        }
    }

    private void startVertex(){
        texCoordIdx = 0;
    }

    private void pushAttrib(Type type, Attributes attribs) throws SAXException{
        try {
            FloatBuffer buf = (FloatBuffer) mesh.getBuffer(type).getData();
            buf.put(parseFloat(attribs.getValue("x")))
           .put(parseFloat(attribs.getValue("y")))
           .put(parseFloat(attribs.getValue("z")));
        } catch (Exception ex){
           throw new SAXException("Failed to push attrib", ex);
        }
    }

    private void pushTexCoord(Attributes attribs) throws SAXException{
        if (texCoordIdx >= 1)
            return; // TODO: Support multi-texcoords
        
        VertexBuffer tcvb = mesh.getBuffer(Type.TexCoord);
        FloatBuffer buf = (FloatBuffer) tcvb.getData();

        buf.put(parseFloat(attribs.getValue("u")));
        if (tcvb.getNumComponents() >= 2){
            buf.put(parseFloat(attribs.getValue("v")));
            if (tcvb.getNumComponents() >= 3){
                buf.put(parseFloat(attribs.getValue("w")));
                if (tcvb.getNumComponents() == 4){
                    buf.put(parseFloat(attribs.getValue("x")));
                }
            }
        }

        texCoordIdx++;
    }

    private void pushColor(Attributes attribs) throws SAXException{
        FloatBuffer buf = (FloatBuffer) mesh.getBuffer(Type.Color).getData();
        String value = parseString(attribs.getValue("value"));
        String[] vals = value.split(" ");
        if (vals.length != 3 && vals.length != 4)
            throw new SAXException("Color value must contain 3 or 4 components");

        ColorRGBA color = new ColorRGBA();
        color.r = parseFloat(vals[0]);
        color.g = parseFloat(vals[1]);
        color.b = parseFloat(vals[2]);
        if (vals.length == 3)
            color.a = 1f;
        else
            color.a = parseFloat(vals[3]);
        
        buf.put(color.r).put(color.g).put(color.b).put(color.a);
    }

    private void pushBoneAssign(String vertIndex, String boneIndex, String weight) throws SAXException{
        int vert = parseInt(vertIndex);
        float w = parseFloat(weight);
        byte bone = (byte) parseInt(boneIndex);

        assert bone >= 0;
        assert vert >= 0 && vert < mesh.getVertexCount();

        int i;
        // see which weights are unused for a given bone
        for (i = vert * 4; i < vert * 4 + 4; i++){
            int v = weightsData.get(i) & 0xFF;
            if (v == 0)
                break;
        }

        // convert fp weight to byte
        byte weightByte = (byte) Math.round(w * 254f);
        assert weightByte != 0;

        weightsData.put(i, weightByte);
        indicesData.put(i, bone);
    }

    private void startSkeleton(String name){
        animData = (AnimData) assetManager.loadContent(new AssetKey(name+"xml"));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException{
        if (ignoreUntilEnd != null)
            return;

        if (qName.equals("texcoord")){
            pushTexCoord(attribs);
        }else if (qName.equals("vertexboneassignment")){
            pushBoneAssign(attribs.getValue("vertexindex"),
                           attribs.getValue("boneindex"),
                           attribs.getValue("weight"));
        }else if (qName.equals("face")){
            pushFace(attribs.getValue("v1"),
                     attribs.getValue("v2"),
                     attribs.getValue("v3"));
        }else if (qName.equals("position")){
            pushAttrib(Type.Position, attribs);
        }else if (qName.equals("normal")){
            pushAttrib(Type.Normal, attribs);
        }else if (qName.equals("tangent")){
            pushAttrib(Type.Tangent, attribs);
        }else if (qName.equals("binormal")){
            pushAttrib(Type.Binormal, attribs);
        }else if (qName.equals("colour_diffuse")){
            pushColor(attribs);
        }else if (qName.equals("vertex")){
            startVertex(); 
        }else if (qName.equals("faces")){
            startFaces(attribs.getValue("count"));
        }else if (qName.equals("geometry")){
            startGeometry(attribs.getValue("vertexcount"));
        }else if (qName.equals("vertexbuffer")){
            startVertexBuffer(attribs);
        }else if (qName.equals("boneassignments")){
            startBoneAssigns();
        }else if (qName.equals("submesh")){
            startMesh(attribs.getValue("material"),
                      attribs.getValue("usesharedvertices"),
                      attribs.getValue("use32bitindexes"),
                      attribs.getValue("operationtype"));
        }else if (qName.equals("submeshes")){
            // ok
        }else if (qName.equals("skeletonlink")){
            startSkeleton(attribs.getValue("name"));
        }else if (qName.equals("mesh")){
            // ok
        }else{
            logger.warning("Unknown tag: "+qName+". Ignoring.");
            ignoreUntilEnd = qName;
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        if (ignoreUntilEnd != null){
            if (ignoreUntilEnd.equals(qName))
                ignoreUntilEnd = null;

            return;
        }

        if (qName.equals("submesh")){
            geom = null;
            mesh = null;
        }else if (qName.equals("faces")){
            if (ib != null)
                ib.flip();
            else
                sb.flip();
            
            vb = null;
            ib = null;
            sb = null;
        }else if (qName.equals("geometry")){
            // finish writing to buffers
            for (VertexBuffer vBuf : mesh.getBuffers()){
                Buffer data = vBuf.getData();
                if (data.position() != 0)
                    data.flip();
            }
            // update bounds
            mesh.updateBound();
            
            // XXX: Only needed for non-animated models!
//            mesh.setStatic();
//            if (AUTO_INTERLEAVE)
//                mesh.setInterleaved();
        }else if (qName.equals("boneassignments")){
            endBoneAssigns();
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
    }

    private Node compileModel(){
        String nodeName;
        if (meshName == null)
            nodeName = "OgreMesh"+(++nodeIdx);
        else
            nodeName = meshName+"-ogremesh";

        Node model;
        if (animData != null){
            Mesh[] meshes = new Mesh[geoms.size()];
            for (int i = 0; i < geoms.size(); i++){
                Mesh m = geoms.get(i).getMesh();
                // create bind pose

                
                VertexBuffer bindPos = new VertexBuffer(Type.BindPosePosition);
                bindPos.setupData(Usage.Static, 3, Format.Float, BufferUtils.clone(m)

                meshes[i] = m;
            }

            Map<String, BoneAnimation> anims = new HashMap<String, BoneAnimation>();
            List<BoneAnimation> animList = animData.anims;
            for (int i = 0; i < animList.size(); i++){
                BoneAnimation anim = animList.get(i);
                anims.put(anim.getName(), anim);
            }

            model = new Model(nodeName, meshes, animData.skeleton, anims);
        }else{
            model = new Node(nodeName);
        }

        for (int i = 0; i < geoms.size(); i++)
            model.attachChild(geoms.get(i));

        return model;
    }

    public Object load(AssetInfo info) throws IOException {
        try{
            assetManager = info.getManager();
            meshName = info.getKey().getName();
            String ext = info.getKey().getExtension();
            meshName = meshName.substring(0, meshName.length() - ext.length() - 1);
            materialList = ((OgreMeshKey)info.getKey()).getMaterialList();

            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
            InputStreamReader r = new InputStreamReader(info.openStream());
            xr.parse(new InputSource(r));
            r.close();

            return compileModel();
        }catch (SAXException ex){
            IOException ioEx = new IOException("Error while parsing Ogre3D mesh.xml");
            ioEx.initCause(ex);
            throw ioEx;
        }

    }

}
