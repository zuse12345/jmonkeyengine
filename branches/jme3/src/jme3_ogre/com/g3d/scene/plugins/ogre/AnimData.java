package com.g3d.scene.plugins.ogre;

import com.g3d.animation.BoneAnimation;
import com.g3d.animation.Skeleton;
import java.util.ArrayList;

public class AnimData {

    public final Skeleton skeleton;
    public final ArrayList<BoneAnimation> anims;

    public AnimData(Skeleton skeleton, ArrayList<BoneAnimation> anims) {
        this.skeleton = skeleton;
        this.anims = anims;
    }
}
