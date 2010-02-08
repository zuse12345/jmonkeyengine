package com.g3d.physics;

/**
 * <code>EllipseCollider</code> provides an implementation of a DynamicCollider using an Ellipsoid shape.
 * 
 * @author Lucas Goraieb
 */
//public class EllipseCollider extends DynamicCollider {

//    protected float intersectEllipse(Vector3f center, Vector3f velocity,
//            Vector3f ellipseLocation, Vector3f ellipseDimension,
//            Vector3f intersectionPoint, Vector3f intersectionNormal) {
//
//        TempVars vars = TempVars.get();
//        assert vars.lock();
//        Vector3f ieNA = vars.vect1,
//                iePosA = vars.vect2,
//                ieRA = vars.vect3,
//                ieNB = vars.vect4;
//
//
//        boolean ieEmbedded = false;
//        float ieDist = center.distance(ellipseLocation) - 1;
//        if (velocity.length() > ieDist){
//            ieNA.set(velocity).normalizeLocal().multLocal(ieDist);
//            iePosA.set(center).addLocal(ieNA);
//            ieEmbedded = true;
//        }else{
//            iePosA.set(center).addLocal(velocity);
//            ieEmbedded = false;
//        }
//
//        ieNA.set(ellipseLocation).subtractLocal(iePosA).divideLocal(ellipseDimension).normalizeLocal();
//        ieRA.set(iePosA).addLocal(ieNA);
//        ieNB.set(ieRA).subtractLocal(ellipseLocation).divideLocal(ellipseDimension).normalizeLocal();
//        intersectionPoint.set(ellipseLocation).addLocal(ieNB.mult(ellipseDimension));
//        intersectionNormal.set(iePosA).subtractLocal(ellipseLocation).divideLocal(ellipseDimension).normalizeLocal();
//        ieDist = iePosA.distance(intersectionPoint) - 1;
//        if (!ieEmbedded){
//            if (ieDist < 0){
//                ieDist = 0;
//            }
//        }
//        assert vars.unlock();
//        return ieDist;
//    }

//}
