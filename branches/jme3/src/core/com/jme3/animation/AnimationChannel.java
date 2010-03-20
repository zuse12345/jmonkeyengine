package com.jme3.animation;

public class AnimationChannel {

    private AnimationControl control;

    private BoneAnimation animation;
    private BoneAnimation blendFrom;
    private float time;
    private float speed;
    private float timeBlendFrom;
    private float speedBlendFrom;

    private LoopMode loopMode, loopModeBlendFrom;
    private float defaultBlendTime = 0.15f;

    private float blendAmount = 1f;
    private float blendRate   = 0;

    private static float clampWrapTime(float t, float max, LoopMode loopMode){
        if (t < 0f){
            switch (loopMode){
                case DontLoop:
                    return 0;
                case Cycle:
                    return 0;
                case Loop:
                    return max - t;
            }
        }else if (t > max){
            switch (loopMode){
                case DontLoop:
                    return max;
                case Cycle:
                    return max;
                case Loop:
                    return t - max;
            }
        }

        return t;
    }

    AnimationChannel(AnimationControl control){
        this.control = control;
    }

    public String getAnimationName() {
        return animation != null ? animation.getName() : null;
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(LoopMode loopMode) {
        this.loopMode = loopMode;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public void play(String name, float blendTime){
        BoneAnimation anim = control.animationMap.get(name);
        if (anim == null)
            throw new IllegalArgumentException("Cannot find animation named: '"+name+"'");

        if (animation != null){
            // activate blending
            blendFrom = animation;
            timeBlendFrom = time;
            speedBlendFrom = speed;
            loopModeBlendFrom = loopMode;
            blendAmount = 0f;
            blendRate   = 1f / blendTime;
        }

        animation = anim;
        time = 0;
        speed = 1f;
        loopMode = LoopMode.Loop;
    }

    public void play(String name){
        play(name, defaultBlendTime);
    }

    void reset(){
        animation = null;
        blendFrom = null;
    }

    void update(float tpf) {
        if (animation == null)
            return;

        time = clampWrapTime(time, animation.getLength(), loopMode);
        animation.setTime(time, control.skeleton, blendAmount);
        time += tpf * speed;

        if (blendFrom != null){
            timeBlendFrom = clampWrapTime(timeBlendFrom,
                                          blendFrom.getLength(),
                                          loopModeBlendFrom);
            blendFrom.setTime(timeBlendFrom, control.skeleton, 1f - blendAmount);
            timeBlendFrom += tpf * speedBlendFrom;

            blendAmount += tpf * blendRate;
            if (blendAmount > 1f){
                blendFrom = null;
            }
        }
    }

}
