package com.g3d.asset.pack;

public interface ProgressListener {
    public void onMaxProgress(int maxProgress);
    public void onProgress(int addProgress);
    public void onText(String text);
    public void onError(String text, Throwable err);
}
