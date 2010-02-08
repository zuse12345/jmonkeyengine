package com.g3d.font;

import com.g3d.renderer.queue.RenderQueue.Bucket;
import com.g3d.scene.Geometry;
import com.g3d.scene.Mesh;
import com.g3d.scene.VertexBuffer;
import com.g3d.scene.VertexBuffer.Type;
import com.g3d.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BitmapText extends Geometry {

    private BitmapFont font;
    private StringBlock block;
    private QuadList quadList = new QuadList();
    private float lineWidth = 0f;
    private boolean rightToLeft = false;
    private boolean needRefresh = true;

    public BitmapText(BitmapFont font, boolean rightToLeft){
        super("BitmapFont", new Mesh());

        setQueueBucket(Bucket.Gui);
        setCullHint(CullHint.Never);

        this.rightToLeft = rightToLeft;
        this.font = font;
        this.block = new StringBlock();
        setMaterial(font.getPage(0));

        // initialize buffers
        Mesh m = getMesh();
        m.setBuffer(Type.Position, 3, new float[0]);
        m.setBuffer(Type.TexCoord, 2, new float[0]);
        m.setBuffer(Type.Index, 3, new short[0]);
    }

    public void setSize(float size) {
        block.setSize(size);
        needRefresh = true;
    }

    public void setText(String text){
        block.setText(text);
        needRefresh = true;
    }

    public void setBox(Rectangle rect){
        block.setTextBox(rect);
        needRefresh = true;
    }

    public float getLineHeight(){
        return font.getLineHeight(block);
    }

    public float getLineWidth(){
        return lineWidth;
    }

    public void updateLogicalState(float tpf){
        super.updateLogicalState(tpf);
        if (needRefresh)
            assemble();
    }

    private void assemble(){
        // first generate quadlist
        if (block.getTextBox() == null){
            lineWidth = font.updateText(block, quadList, rightToLeft);
        }else{
            font.updateTextRect(block, quadList);
        }

        Mesh m = getMesh();
        m.setVertexCount(quadList.getQuantity() * 4);
        m.setTriangleCount(quadList.getQuantity() * 2);

        VertexBuffer pb = m.getBuffer(Type.Position);
        VertexBuffer tb = m.getBuffer(Type.TexCoord);
        VertexBuffer ib = m.getBuffer(Type.Index);

        FloatBuffer fpb = (FloatBuffer) pb.getData();
        FloatBuffer ftb = (FloatBuffer) tb.getData();
        ShortBuffer sib = (ShortBuffer) ib.getData();

        // increase capacity of buffers as needed
        fpb.rewind();
        fpb = BufferUtils.ensureLargeEnough(fpb, m.getVertexCount() * 3);
        pb.updateData(fpb);

        ftb.rewind();
        ftb = BufferUtils.ensureLargeEnough(ftb, m.getVertexCount() * 2);
        tb.updateData(ftb);

        sib.rewind();
        sib = BufferUtils.ensureLargeEnough(sib, m.getTriangleCount() * 3);
        ib.updateData(sib);

        // go for each quad and append it to the buffers
        for (int i = 0; i < quadList.getQuantity(); i++){
            FontQuad fq = quadList.getQuad(i);
            fq.appendPositions(fpb);
            fq.appendTexCoords(ftb);
            fq.appendIndices(sib, i);
        }
        
        fpb.rewind();
        ftb.rewind();
        sib.rewind();
    }

}
