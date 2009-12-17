package com.g3d.scene.plugins.ogre;

import com.g3d.animation.BoneAnimation;
import com.g3d.animation.Skeleton;
import java.util.List;

public class AnimData {

    public final Skeleton skeleton;
    public final List<BoneAnimation> anims;

    public AnimData(Skeleton skeleton, List<BoneAnimation> anims) {
        this.skeleton = skeleton;
        this.anims = anims;
    }
}
