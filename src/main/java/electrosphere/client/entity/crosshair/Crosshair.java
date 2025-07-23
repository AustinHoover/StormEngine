package electrosphere.client.entity.crosshair;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;

import org.joml.Vector3d;

/**
 * Crosshair logic
 */
public class Crosshair {
    
    static Entity crossHairEntity;
    static float rotationTheta = 0;
    static float bobTheta = 0;
    static float bobMagnitude = 0.1f;
    static float offsetVertical = 1;
    static Entity currentTarget = null;
    static boolean crosshairActive = false;
    
    public static void initCrossHairEntity(){
        crossHairEntity = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(crossHairEntity, "/Models/engine/lockoncrosshair1.fbx");
        // EntityUtils.setVisible(crossHairEntity, false);
    }
    
    
    static final float TARGET_MAX_DIST = 1;
    public static void checkTargetable(){
        if(crossHairEntity != null && Globals.clientState.playerEntity != null && Globals.clientState.playerCamera != null){
            Vector3d parentPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
//            if(currentTarget == null){
            if(!crosshairActive){
                Entity target = null;
                double dist = 100;
                for(Entity entity : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.TARGETABLE)){
                    Vector3d entityPos = EntityUtils.getPosition(entity);
                    double currentDist = parentPos.distance(entityPos);
                    double currentAngleDiff = new Vector3d(entityPos).sub(parentPos).normalize().dot(new Vector3d(CameraEntityUtils.getCameraEye(Globals.clientState.playerCamera)));
                    if(currentDist + currentAngleDiff < dist && currentDist <= TARGET_MAX_DIST && entity != Globals.clientState.playerEntity){
                        target = entity;
                        dist = currentDist + currentAngleDiff;
                    }
                }
                if(target != null){
//                    System.out.println("Found target!");
                    currentTarget = target;
                    // EntityUtils.setVisible(crossHairEntity, true);
                } else {
                    currentTarget = null;
                    // EntityUtils.setVisible(crossHairEntity, false);
                }
            }
//            } else {
//                if(parentPos.distance(EntityUtils.getPosition(currentTarget)) > TARGET_MAX_DIST){
//                    currentTarget = null;
//                    EntityUtils.setVisible(crossHairEntity, false);
//                }
//            }
        }
    }
    
    public static void updateTargetCrosshairPosition(){
        if(crossHairEntity != null && currentTarget != null){
            rotationTheta += 0.01;
            if(rotationTheta > Math.PI * 2){
                rotationTheta = 0;
            }
            bobTheta += 0.01;
            if(bobTheta > Math.PI * 2){
                bobTheta = 0;
            }
            Vector3d crosshairPos = EntityUtils.getPosition(crossHairEntity);
            crosshairPos.set(new Vector3d(EntityUtils.getPosition(currentTarget)).add(0,offsetVertical + Math.sin(bobTheta) * bobMagnitude,0));
//            EntityUtils.getRotation(crossHairEntity).rotationZ(rotationTheta);
            EntityUtils.getRotation(crossHairEntity).rotationXYZ((float)-Math.PI/2.0f, 0, rotationTheta);
        }
    }
    
    public static void setCrosshairActive(boolean active){
        crosshairActive = active;
    }
    
    public static boolean getCrosshairActive(){
        return crosshairActive;
    }
    
    public static boolean hasTarget(){
        return currentTarget != null;
    }
    
    public static Vector3d getTargetPosition(){
        return EntityUtils.getPosition(currentTarget);
    }
    
    public static Entity getTarget(){
        return currentTarget;
    }
    
}
