package com.g3d.control;

import com.g3d.math.Transform;
import com.g3d.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

public class SpatialMotionControl extends AbstractSpatialControl {

    private float time = 0f;
    private boolean repeat = false;
    private float maxTime = 0f;
    private List<KeyFrame> frames = new ArrayList<KeyFrame>();

    private KeyFrame curFrame;
    private int curFrameIndex;

    public SpatialMotionControl(Spatial obj){
        super(obj);
    }

    public void insertKeyFrame(float time, Transform transform){
        int slot = -1;
        for (int i = 0; i < frames.size()-1; i++){
            KeyFrame before = frames.get(i);
            KeyFrame after = frames.get(i+1);
            if (time <= before.getTime()){
                slot = i;
            }else if (before.getTime() <= time && time <= after.getTime()){
                slot = i + 1;
            }
        }
        if (slot == -1){
            // add at the end
            frames.add(new KeyFrame(transform, time));
        }else{
            // insert in desired slot
            frames.add(slot, new KeyFrame(transform, time));
        }
        if (time > maxTime){
            maxTime = time;
        }
    }

    public void setRepeat(boolean repeat){
        this.repeat = repeat;
    }

    private void timeFlow(float delta){
        time += delta;
        if (time > maxTime){
            time %= maxTime;
        }else if (time < 0){
            time = maxTime + time;
        }
        determineFrame();
    }

    private KeyFrame determineFrame(){
        for (int i = 0; i < frames.size(); i++){
            KeyFrame frame = frames.get(i);
            if (time <= frame.getTime()){
                curFrameIndex = i;
                curFrame = frame;
                return frame;
            }
        }
        return null;
    }

    @Override
    public void update(float tpf) {
        if (enabled){
            timeFlow(tpf);
            Transform transform = new Transform();
            if (curFrameIndex == 0){
                transform.set(curFrame.getTransform());
            }else{
                KeyFrame before = frames.get(curFrameIndex-1);
                float timeDiff = curFrame.time - before.time;
                float blend = (time - before.time) / timeDiff;
                transform.interpolateTransforms(before.getTransform(),
                                                curFrame.getTransform(),
                                                blend);
            }
            spatial.setTransform(transform);
        }
    }

}
