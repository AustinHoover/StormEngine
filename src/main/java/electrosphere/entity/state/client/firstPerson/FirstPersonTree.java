package electrosphere.entity.state.client.firstPerson;

import electrosphere.data.entity.common.treedata.TreeDataAnimation;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.renderer.actor.Actor;

/**
 * Manages the animations for the first person view model
 */
public class FirstPersonTree implements BehaviorTree {


    //the offset from the origin to place the viewmodel
    double heightFromOrigin;

    //the amount to pull below the camera
    double cameraViewDirOffsetY;

    //the amount to pull behind the camera
    double cameraViewDirOffsetZ;

    @Override
    public void simulate(float deltaTime) {
    }

    /**
     * Attaches this tree to the entity.
     * @param entity The entity to attach to
     * @param heightFromOrigin How far from the origin of the creature to place the viewmodel
     * @param cameraViewDirOffset How far to pull the view model behind the camera
     */
    public static FirstPersonTree attachTree(Entity parent, double heightFromOrigin, double cameraViewDirOffsetY, double cameraViewDirOffsetZ){
        FirstPersonTree rVal = new FirstPersonTree();

        rVal.heightFromOrigin = heightFromOrigin;
        rVal.cameraViewDirOffsetY = cameraViewDirOffsetY;
        rVal.cameraViewDirOffsetZ = cameraViewDirOffsetZ;
    
        //!!WARNING!! THIS WAS MANUALLY MODIFIED OH GOD
        parent.putData(EntityDataStrings.FIRST_PERSON_TREE, rVal);
        Globals.clientState.clientScene.registerBehaviorTree(rVal);
        return rVal;
    }

    /**
     * Gets the tree on the entity
     * @param target the entity
     * @return The tree
     */
    public static FirstPersonTree getTree(Entity target){
        return (FirstPersonTree)target.getData(EntityDataStrings.FIRST_PERSON_TREE);
    }

    /**
     * Checks if the provided entity has the tree
     * @param target the provided entity
     * @return true if the entity has a FirstPersonTree, false otherwise
     */
    public static boolean hasTree(Entity target){
        return target.containsKey(EntityDataStrings.FIRST_PERSON_TREE);
    }

    /**
     * the offset from the origin to place the viewmodel
     * @return
     */
    public double getHeightFromOrigin(){
        return heightFromOrigin;
    }

    /**
     * the amount to pull below the camera
     * @return
     */
    public double getCameraViewDirOffsetY(){
        return cameraViewDirOffsetY;
    }

    /**
     * the amount to pull behind the camera
     * @return
     */
    public double getCameraViewDirOffsetZ(){
        return cameraViewDirOffsetZ;
    }

    /**
     * Plays an animation if it exists
     * @param animationName the name of the animation
     */
    public void playAnimation(String animationName, int priority){
        this.playAnimation(animationName, priority, 0.0001);
    }

    /**
     * Plays an animation if it exists
     * @param animationName the name of the animation
     * @param priority The priority to play the animation with
     * @param offset The offset to start the animation at
     */
    public void playAnimation(String animationName, int priority, double offset){
        if(Globals.clientState.firstPersonEntity != null){
            Actor actor = EntityUtils.getActor(Globals.clientState.firstPersonEntity);
            if(
                (!actor.getAnimationData().isPlayingAnimation() || !actor.getAnimationData().isPlayingAnimation(animationName)) &&
                (Globals.assetManager.fetchModel(actor.getBaseModelPath()) != null && Globals.assetManager.fetchModel(actor.getBaseModelPath()).getAnimation(animationName) != null)
            ){
                actor.getAnimationData().playAnimation(animationName,priority);
                actor.getAnimationData().incrementAnimationTime(offset);
            }
        }
    }

    /**
     * Plays an animation if it exists
     * @param animationName the name of the animation
     */
    public void playAnimation(TreeDataAnimation animation){
        this.playAnimation(animation, 0.0001);
    }

    /**
     * Plays an animation if it exists
     * @param animationName the name of the animation
     * @param offset The offset to start the animation at
     */
    public void playAnimation(TreeDataAnimation animation, double offset){
        if(Globals.clientState.firstPersonEntity != null){
            Actor actor = EntityUtils.getActor(Globals.clientState.firstPersonEntity);
            if(
                (!actor.getAnimationData().isPlayingAnimation() || !actor.getAnimationData().isPlayingAnimation(animation.getNameFirstPerson())) &&
                (Globals.assetManager.fetchModel(actor.getBaseModelPath()) != null && Globals.assetManager.fetchModel(actor.getBaseModelPath()).getAnimation(animation.getNameFirstPerson()) != null)
            ){
                actor.getAnimationData().playAnimation(animation, false);
                actor.getAnimationData().incrementAnimationTime(offset);
            }
        }
    }

    /**
     * Plays an animation if it exists
     * @param animationName the name of the animation
     */
    public void interruptAnimation(TreeDataAnimation animation){
        if(Globals.clientState.firstPersonEntity != null){
            Actor actor = EntityUtils.getActor(Globals.clientState.firstPersonEntity);
            if(
                (actor.getAnimationData().isPlayingAnimation() || actor.getAnimationData().isPlayingAnimation(animation.getNameFirstPerson())) &&
                (Globals.assetManager.fetchModel(actor.getBaseModelPath()) != null && Globals.assetManager.fetchModel(actor.getBaseModelPath()).getAnimation(animation.getNameFirstPerson()) != null)
            ){
                actor.getAnimationData().interruptAnimation(animation, false);
            }
        }
    }

    /**
     * If the entity has a first person tree, plays the provided animation
     * @param entity The entity
     * @param animationName the name of the animation
     * @param priority The priority of the animation
     */
    public static void conditionallyPlayAnimation(Entity entity, String animationName, int priority, double offset){
        if(entity != null && entity == Globals.clientState.playerEntity && Globals.clientState.firstPersonEntity != null && FirstPersonTree.hasTree(Globals.clientState.firstPersonEntity)){
            FirstPersonTree.getTree(Globals.clientState.firstPersonEntity).playAnimation(animationName, priority);
        }
    }

    /**
     * If the entity has a first person tree, plays the provided animation
     * @param entity The entity
     * @param animationName the name of the animation
     * @param priority The priority of the animation
     */
    public static void conditionallyPlayAnimation(Entity entity, String animationName, int priority){
        FirstPersonTree.conditionallyPlayAnimation(entity, animationName, priority, 0.0001);
    }

    /**
     * If the entity has a first person tree, plays the provided animation
     * @param entity The entity
     * @param animationName the name of the animation
     */
    public static void conditionallyPlayAnimation(Entity entity, TreeDataAnimation animation){
        if(entity != null && entity == Globals.clientState.playerEntity && Globals.clientState.firstPersonEntity != null && FirstPersonTree.hasTree(Globals.clientState.firstPersonEntity)){
            FirstPersonTree.getTree(Globals.clientState.firstPersonEntity).playAnimation(animation);
        }
    }

    /**
     * If the entity has a first person tree, plays the provided animation
     * @param entity The entity
     * @param animationName the name of the animation
     * @param offset The offset to start the animation at
     */
    public static void conditionallyPlayAnimation(Entity entity, TreeDataAnimation animation, double offset){
        if(entity != null && entity == Globals.clientState.playerEntity && FirstPersonTree.hasTree(Globals.clientState.firstPersonEntity)){
            FirstPersonTree.getTree(Globals.clientState.firstPersonEntity).playAnimation(animation,offset);
        }
    }
    
    /**
     * If the entity has a first person tree, interrupts the provided animation
     * @param entity The entity
     * @param animation The animation
     */
    public static void conditionallyInterruptAnimation(Entity entity, TreeDataAnimation animation){
        if(entity != null && entity == Globals.clientState.playerEntity && FirstPersonTree.hasTree(Globals.clientState.firstPersonEntity)){
            FirstPersonTree.getTree(Globals.clientState.firstPersonEntity).interruptAnimation(animation);
        }
    }

}
