package electrosphere.client.sim;

import java.util.HashSet;

import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.entity.crosshair.Crosshair;
import electrosphere.client.entity.instance.InstanceUpdater;
import electrosphere.client.fluid.manager.ClientFluidManager;
import electrosphere.client.interact.ClientInteractionEngine;
import electrosphere.client.terrain.manager.ClientTerrainManager;
import electrosphere.client.ui.menu.ingame.ToolbarPreviewWindow;
import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.collidable.ClientCollidableTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.renderer.actor.Actor;

public class ClientSimulation {

    //if true, is ready to simulate
    boolean isReady = false;

    //if true, should load terrain
    boolean loadTerrain = false;

    //used for tracking different in player position between frames (principally for draw cell manager)
    Vector3d newPlayerCharacterPosition = new Vector3d();

    /**
     * Set for storing entities of a specific tag
     */
    private HashSet<Entity> entityTagSet = new HashSet<Entity>();
    
    /**
     * Constructor
     */
    public ClientSimulation(){
        isReady = false;
    }
    
    /**
     * Main simulation function
     */
    public void simulate(){

        //
        //check for dependencies
        if(Globals.clientState.clientSceneWrapper == null){
            return;
        }

        Globals.profiler.beginCpuSample("simulate");

        //
        //load terrain
        if(isLoadingTerrain()){
            loadTerrain();
        }

        //process all server synchronization messages
        Globals.profiler.beginCpuSample("clientSynchronizationManager.processMessages");
        Globals.clientState.clientSynchronizationManager.processMessages();
        Globals.profiler.endCpuSample();
        //
        //simulate bullet physics engine step
        if(EngineState.EngineFlags.RUN_PHYSICS){
            Globals.clientState.clientSceneWrapper.getCollisionEngine().simulatePhysics();
            Globals.clientState.clientSceneWrapper.getCollisionEngine().updateDynamicObjectTransforms();
        }
        
        //update actor animations
        Globals.profiler.beginCpuSample("update actor animations");
        Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.DRAWABLE, entityTagSet);
        for(Entity currentEntity : entityTagSet){
            Actor currentActor = EntityUtils.getActor(currentEntity);
            if(currentActor.getLodLevel() == Actor.LOD_LEVEL_STATIC){
                continue;
            }
            if(currentActor.getAnimationData().isPlayingAnimation()){
                currentActor.getAnimationData().incrementAnimationTime((float)Globals.engineState.timekeeper.getSimFrameTime());
            }
        }
        Globals.profiler.endCpuSample();
        //
        //make items play idle animation
        Globals.profiler.beginCpuSample("item animations");
        Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM, entityTagSet);
        for(Entity item : entityTagSet){
            ItemUtils.updateItemActorAnimation(item);
        }
        Globals.profiler.endCpuSample();
        //
        //update attached entity positions
        AttachUtils.clientUpdateAttachedEntityPositions();
        Globals.particleService.handleAllSignals();
        //
        //Hitbox stuff
        Globals.profiler.beginCpuSample("update hitboxes");
        Globals.clientState.clientSceneWrapper.getHitboxManager().simulate();
        Globals.profiler.endCpuSample();
        //
        //update foliage
        if(Globals.clientState.foliageCellManager != null){
            Globals.clientState.foliageCellManager.update();
        }
        //
        //targeting crosshair
        Globals.profiler.beginCpuSample("crosshair update");
        Crosshair.checkTargetable();
        Crosshair.updateTargetCrosshairPosition();
        Globals.profiler.endCpuSample();
        //
        //simulate behavior trees
        Globals.clientState.clientSceneWrapper.getScene().simulateBehaviorTrees((float)Globals.engineState.timekeeper.getSimFrameTime());
        //
        //sum collidable impulses
        Globals.profiler.beginCpuSample("collidable logic");
        Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.COLLIDABLE, entityTagSet);
        for(Entity collidable : entityTagSet){
            if(ClientCollidableTree.hasClientCollidableTree(collidable)){
                ClientCollidableTree.getClientCollidableTree(collidable).simulate((float)Globals.engineState.timekeeper.getSimFrameTime());
            }
        }
        //
        //clear collidable impulse lists
        Globals.clientState.clientSceneWrapper.getCollisionEngine().clearCollidableImpulseLists();
        Globals.clientState.clientSceneWrapper.getChemistryEngine().clearCollidableImpulseLists();
        Globals.profiler.endCpuSample();

        //
        //Rebase world origin
        Globals.clientState.clientSceneWrapper.getCollisionEngine().rebaseWorldOrigin();

        //
        //wrap up functions
        this.runClientFunctions();

        Globals.profiler.endCpuSample();
    }

    /**
     * Client functions
     */
    public void runClientFunctions(){
        Globals.profiler.beginCpuSample("client functions");
        ClientTerrainManager.generateTerrainChunkGeometry();
        ClientFluidManager.generateFluidChunkGeometry();
        this.updateSkyboxPos();
        Globals.clientState.clientSceneWrapper.destroyEntitiesOutsideSimRange();
        InstanceUpdater.updateInstancedActorPriority();
        Globals.cameraHandler.updateGlobalCamera();
        this.updateFirstPersonAttachments();
        //bones have potenitally moved, so need to update where attached entities actually are before drawing
        AttachUtils.clientUpdateAttachedEntityPositions();
        ClientInteractionEngine.updateInteractionTargetLabel();
        ToolbarPreviewWindow.checkVisibility();
        this.runServices();
        // updateCellManager();
        Globals.profiler.endCpuSample();
    }

    /**
     * Runs the client services
     */
    private void runServices(){
        Globals.profiler.beginCpuSample("ClientSimulation.runServices");
        Globals.clientState.clientTemporalService.simulate();
        Globals.profiler.endCpuSample();
    }


    /**
     * Updates the skybox position to center on the player
     */
    private void updateSkyboxPos(){
        Globals.profiler.beginCpuSample("updateSkyboxPos");
        if(Globals.clientState.skybox != null && Globals.clientState.playerEntity != null){
            EntityUtils.setPosition(Globals.clientState.skybox, EntityUtils.getPosition(Globals.clientState.playerEntity));
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * If in first person, update the spatial transforms of things attached to the player viewmodel
     */
    private void updateFirstPersonAttachments(){
        Globals.profiler.beginCpuSample("updateFirstPersonAttachments");
        //update the facing vector when camera moves in first person
        if(!Globals.controlHandler.cameraIsThirdPerson() && Globals.clientState.playerCamera != null && Globals.clientState.playerEntity != null){
            CreatureUtils.setFacingVector(Globals.clientState.playerEntity, CameraEntityUtils.getFacingVec(Globals.clientState.playerCamera));
            //flush equipped item state
            if(ClientEquipState.hasEquipState(Globals.clientState.playerEntity)){
                ClientEquipState equipState = ClientEquipState.getClientEquipState(Globals.clientState.playerEntity);
                equipState.evaluatePlayerAttachments();
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Loads terrain that is in queue
     */
    public void loadTerrain(){
        Globals.profiler.beginCpuSample("ClientSimulation.loadTerrain");
        if(Globals.clientState.clientTerrainManager != null){
            Globals.clientState.clientTerrainManager.handleMessages();
            this.updateTerrainCellManager();
        }
        if(Globals.clientState.clientFluidManager != null && EngineState.EngineFlags.RUN_FLUIDS){
            Globals.clientState.clientFluidManager.handleMessages();
            this.updateFluidCellManager();
        }
        if(Globals.clientState.clientBlockManager != null){
            Globals.clientState.clientBlockManager.handleMessages();
            this.updateBlockCellManager();
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Updates the terrain cell manager (specifically position handling)
     */
    private void updateTerrainCellManager(){
        ///
        ///      C L I E N T    C E L L    M A N A G E R
        ///
        if(Globals.clientState.clientDrawCellManager != null && Globals.clientState.clientWorldData != null){
            //Cell manager do your things
            Globals.clientState.clientDrawCellManager.update();
        }
    }

    /**
     * Updates the fluid cell manager (specifically position handling)
     */
    private void updateFluidCellManager(){
        //fluid work
        if(Globals.clientState.fluidCellManager != null && Globals.clientState.clientWorldData != null && EngineState.EngineFlags.RUN_FLUIDS){
            Globals.clientState.fluidCellManager.update();
        }
    }

    /**
     * Updates the block cell manager
     */
    private void updateBlockCellManager(){
        if(Globals.clientState.clientBlockCellManager != null && Globals.clientState.clientWorldData != null){
            Globals.clientState.clientBlockCellManager.update();
        }
    }
    
    /**
     * Gets whether the client simulation is ready to execute
     * @return True if ready to execute, false otherwise
     */
    public boolean isReady(){
        return isReady;
    }
    
    /**
     * Sets the ready status of the client simulation
     * @param ready True if ready to simulate, false otherwise
     */
    public void setReady(boolean ready){
        isReady = ready;
    }
    
    /**
     * Freezes simulation (sets ready to false)
     */
    public void freeze(){
        isReady = false;
    }

    /**
     * Unfreezes simulation (sets ready to true)
     */
    public void unfreeze(){
        isReady = true;
    }

    /**
     * Gets whether the client simulation is loading terrain
     * @return True if loading terrain, false otherwise
     */
    public boolean isLoadingTerrain(){
        return this.loadTerrain;
    }

    /**
     * Sets whether the client should load terrain or not
     * @param loadTerrain True if should load terrain, false otherwise
     */
    public void setLoadingTerrain(boolean loadTerrain){
        this.loadTerrain = loadTerrain;
    }

}
