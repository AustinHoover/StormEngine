package electrosphere.server.simulation;

import java.util.Set;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.collidable.ServerCollidableTree;
import electrosphere.entity.state.lod.ServerLODComponent;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.entity.poseactor.PoseActor;

/**
 * Server-side micro-scale simulation
 */
public class MicroSimulation {
    
    /**
     * Tracks whether the micro simulation is ready or not
     */
    boolean isReady = false;
    
    /**
     * Constructor
     */
    public MicroSimulation(){
        isReady = false;
    }
    
    /**
     * Simulates a provided data cell
     * @param dataCell The data cell
     */
    public void simulate(ServerDataCell dataCell){
        Globals.profiler.beginCpuSample("MicroSimulation.simulate");
        if(dataCell.isReady()){
            //update actor animations
            Set<Entity> poseableEntities = dataCell.getScene().getEntitiesWithTag(EntityTags.POSEABLE);
            if(poseableEntities != null){
                for(Entity currentEntity : poseableEntities){
                    if(ServerLODComponent.hasServerLODComponent(currentEntity)){
                        if(ServerLODComponent.getServerLODComponent(currentEntity).getLodLevel() == ServerLODComponent.LOW_RES){
                            continue;
                        }
                    }
                    //fetch actor
                    PoseActor currentPoseActor = EntityUtils.getPoseActor(currentEntity);
                    //increment animations
                    if(currentPoseActor.isPlayingAnimation()){
                        currentPoseActor.incrementAnimationTime(Globals.engineState.timekeeper.getSimFrameTime());
                    }
                }
            }

            //
            //make items play idle animation
            for(Entity itemEnt : dataCell.getScene().getEntitiesWithTag(EntityTags.ITEM)){
                if(ServerLODComponent.hasServerLODComponent(itemEnt)){
                    if(ServerLODComponent.getServerLODComponent(itemEnt).getLodLevel() == ServerLODComponent.LOW_RES){
                        continue;
                    }
                }
                ItemUtils.updateItemPoseActorAnimation(itemEnt);
            }

            //
            //simulate behavior trees
            if(dataCell.getScene().getNumBehaviorTrees() > 0){
                dataCell.getScene().simulateBehaviorTrees((float)Globals.engineState.timekeeper.getSimFrameTime());
            }

            //update attached entity positions
            //!!This must come after simulating behavior trees!!
            //if it does not come after queueing animations, the attach positions might not represent the animation of the parent
            Globals.profiler.beginCpuSample("MicroSimulation - attached entity positions");
            AttachUtils.serverUpdateAttachedEntityPositions(dataCell);
            Globals.profiler.endCpuSample();
            

            //
            //sum collidable impulses
            Globals.profiler.beginCpuSample("MicroSimulation - collidables");
            Set<Entity> collidables = dataCell.getScene().getEntitiesWithTag(EntityTags.COLLIDABLE);
            for(Entity collidable : collidables){
                if(ServerCollidableTree.hasServerCollidableTree(collidable)){
                    ServerCollidableTree.getServerCollidableTree(collidable).simulate((float)Globals.engineState.timekeeper.getSimFrameTime());
                }
            }
            Globals.profiler.endCpuSample();

            //
            //update actor transform caches
            Globals.profiler.beginCpuSample("MicroSimulation - poseables");
            poseableEntities = dataCell.getScene().getEntitiesWithTag(EntityTags.POSEABLE);
            if(poseableEntities != null){
                for(Entity currentEntity : dataCell.getScene().getEntitiesWithTag(EntityTags.POSEABLE)){
                    if(ServerLODComponent.hasServerLODComponent(currentEntity)){
                        if(ServerLODComponent.getServerLODComponent(currentEntity).getLodLevel() == ServerLODComponent.LOW_RES){
                            continue;
                        }
                    }
                    //fetch actor
                    PoseActor currentPoseActor = EntityUtils.getPoseActor(currentEntity);
                    currentPoseActor.updateTransformCache();
                }
            }
            Globals.profiler.endCpuSample();
        }
        Globals.profiler.endCpuSample();
    }
    
    /**
     * Checks whether the micro simulation is ready or not
     * @return true if it is ready, false otherwise
     */
    public boolean isReady(){
        return isReady;
    }
    
    /**
     * Sets whether the micro simulation is ready or not
     * @param ready true if it is ready, false otherwise
     */
    public void setReady(boolean ready){
        isReady = ready;
    }
    
    /**
     * Freezes the micro simulation
     */
    public void freeze(){
        isReady = false;
    }
    
    /**
     * Unfreezes the micro simulation
     */
    public void unfreeze(){
        isReady = true;
    }
    
}
