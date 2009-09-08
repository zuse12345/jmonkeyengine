package com.g3d.scene.plugins.ogre;

import com.g3d.asset.AssetInfo;
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

    private String meshName;
    private AssetManager assetManager;
    private OgreMaterialList materialList;

    private ShortBuffer sb;
    private IntBuffer ib;
    private FloatBuffer fb;
    private VertexBuffer vb;
    private Mesh mesh;
    private Geometry geom;
    private Node node;
    private int geomIdx = 0;
    private int texCoordIdx = 0;
    private static volatile int nodeIdx = 0;

    public MeshLoader(){
        super();
    }

    public static void main(String[] args) throws SAXException{
        AssetManager manager = new AssetManager(true);
        manager.loadModel("Cube.meshxml");
    }

    @Override
    public void startDocument() {
        if (meshName == null)
            node = new Node("OgreMesh"+(++nodeIdx));
        else
            node = new Node(meshName+"-ogremesh");
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
        int numFaces = parseInt(count, 0);
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
        node.attachChild(geom);
    }

    private void startGeometry(String vertexcount) throws SAXException{
        int vertCount = parseInt(vertexcount);
        mesh.setVertexCount(vertCount);
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
        try
        {
            FloatBuffer buf = (FloatBuffer) mesh.getBuffer(type).getData();
            buf.put(parseFloat(attribs.getValue("x")))
           .put(parseFloat(attribs.getValue("y")))
           .put(parseFloat(attribs.getValue("z")));
        }
        catch (Exception e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    private void pushColor2(Attributes attribs) throws SAXException{
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

        color = ColorRGBA.randomColor();

        ByteBuffer buf = (ByteBuffer) mesh.getBuffer(Type.Color).getData();
        byte[] rgba = color.asBytesRGBA();
        buf.put(rgba);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException{
        if (qName.equals("submesh")){
            startMesh(attribs.getValue("material"),
                      attribs.getValue("usesharedvertices"),
                      attribs.getValue("use32bitindexes"),
                      attribs.getValue("operationtype"));
        }else if (qName.equals("faces")){
            startFaces(attribs.getValue("count"));
        }else if (qName.equals("face")){
            pushFace(attribs.getValue("v1"),
                     attribs.getValue("v2"),
                     attribs.getValue("v3"));
        }else if (qName.equals("geometry")){
            startGeometry(attribs.getValue("vertexcount"));
        }else if (qName.equals("vertexbuffer")){
            startVertexBuffer(attribs);
        }else if (qName.equals("vertex")){
            startVertex();
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
        }else if (qName.equals("texcoord")){
            pushTexCoord(attribs);
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
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
        }

    }

    @Override
    public void characters(char ch[], int start, int length) {
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
            return node;
        }catch (SAXException ex){
            IOException ioEx = new IOException("Error while parsing Ogre3D mesh.xml");
            ioEx.initCause(ex);
            throw ioEx;
        }

    }

}
