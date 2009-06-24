package com.g3d.font;

import java.util.ArrayList;
import java.util.List;

public class QuadList {

    private List<FontQuad> quads = new ArrayList<FontQuad>();

    public void addQuad(FontQuad quad){
        quads.add(quad);
    }

    public FontQuad getQuad(int index){
        return quads.get(index);
    }

    public int getQuantity(){
        return quads.size();
    }

    public void ensureSize(int size){
        if (quads.size() < size){
            int quadSize = quads.size();
            for (int i = 0; i < size - quadSize; i++){
                quads.add(new FontQuad());
            }
        }
        for (int i = 0; i < size; i++){
            quads.get(i).setSize(0, 0);
        }
    }

}
