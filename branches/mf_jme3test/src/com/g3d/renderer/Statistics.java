package com.g3d.renderer;

public class Statistics {

    protected int vertCount;
    protected int objCount;
    protected int memoryMeshes;
    protected int memoryTextures;
    protected int memoryShaders;

    public int getMemoryMeshes() {
        return memoryMeshes;
    }

    public void addMemoryMeshes(int memoryMeshes) {
        this.memoryMeshes += memoryMeshes;
    }

    public int getMemoryShaders() {
        return memoryShaders;
    }

    public void addMemoryShaders(int memoryShaders) {
        this.memoryShaders += memoryShaders;
    }

    public int getMemoryTextures() {
        return memoryTextures;
    }

    public void addMemoryTextures(int memoryTextures) {
        this.memoryTextures += memoryTextures;
    }

    public int getObjCount() {
        return objCount;
    }

    public void addObjCount(int objCount) {
        this.objCount += objCount;
    }

    public int getVertCount() {
        return vertCount;
    }

    public void addVertCount(int vertCount) {
        this.vertCount += vertCount;
    }

    public void reset() {
        vertCount = 0;
        objCount = 0;
        memoryMeshes = 0;
        memoryTextures = 0;
        memoryShaders = 0;
    }

}
