package electrosphere.client.ui.menu.debug.entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.entity.debug.DebugVisualizerUtils;
import electrosphere.client.interact.ClientInteractionEngine;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityActorTab;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityDebugActions;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityFoliageTab;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityFurnitureTab;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityHitboxTab;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityInteractionTab;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityInventoryTab;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityPhysicsTab;
import electrosphere.client.ui.menu.debug.entity.tabs.ImGuiEntityServerTab;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.AnimationPriorities;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.entity.state.equip.ClientToolbarState;
import electrosphere.entity.state.foliage.AmbientFoliage;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.server.ServerPlayerViewDirTree;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.entity.types.terrain.TerrainChunk;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.actor.instance.InstancedActor;
import electrosphere.renderer.actor.mask.ActorAnimationMaskEntry;
import electrosphere.renderer.anim.AnimChannel;
import electrosphere.renderer.anim.Animation;
import electrosphere.renderer.model.Bone;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.ui.imgui.ImGuiWindow;
import electrosphere.renderer.ui.imgui.ImGuiWindow.ImGuiWindowCallback;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.entity.poseactor.PoseActor;
import electrosphere.server.entity.poseactor.PoseModel;
import imgui.ImGui;

/**
 * Macros for creating imgui windows relating to entity debugging
 */
public class ImGuiEntityMacros {
    
    //window for selecting entities to view
    public static ImGuiWindow clientEntityListWindow;
    private static boolean filterBasic = true; //filters out entities we probably wouldn't want to see (particles, terrain meshes, foliage cells, etc)
    private static boolean filterToCreatures = false; //filters the entity list to just creatures
    private static boolean filterToFoliage = false; //filters the entity list to just foliage
    private static boolean filterToTerrain = false; //filters the entity list to just terrain
    private static boolean filterHasCollidable = false; //filters the entity list to just entities that have collidables

    //window for viewing details about an entity
    protected static ImGuiWindow clientEntityDetailWindow;
    private static Entity detailViewEntity = null;

    //tree node values
    private static boolean showDataTab = false; //show all data names stored in the entity
    private static boolean showActorTab = false; //show the actor tab
    private static boolean showServerTab = false; //show server data
    private static boolean showHitboxTab = false; //show the hitbox tab
    private static boolean showInstancedActorTab = false; //show the instanced actor tab
    private static boolean showPoseActorTab = false; //show the pose actor tab
    private static boolean showEquipStateTab = false; //actor details
    private static boolean showFirstPersonTab = false; //first person tab
    private static boolean showLinkedEntitiesTab = false;//show linked entities
    private static boolean showServerViewDirTab = false; //show server view dir
    private static boolean showPhysicsTab = false; //show physics values
    private static boolean showFoliageTab = false; //show foliage data
    private static boolean showToolbarTab = false; //show toolbar data
    private static boolean showInventoryTab = false; //show inventory data
    private static boolean showInteractionTab = false; //show inventory data
    private static boolean showFurnitureTab = false; //show the furniture data
    private static boolean showDebugActionsTab = false; //show debug actions

    /**
     * Creates the windows in this file
     */
    public static void createClientEntityWindows(){
        ImGuiEntityMacros.createClientEntityDetailWindow();
        ImGuiEntityMacros.createClientEntitySelectionWindow();
    }

    /**
     * Client scene entity view
     */
    protected static void createClientEntitySelectionWindow(){
        clientEntityListWindow = new ImGuiWindow("Client Entities");
        clientEntityListWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                //audio engine details
                ImGui.text("Client Entities");
                if(ImGui.checkbox("Filter Basic", filterBasic)){
                    filterBasic = !filterBasic;
                }
                if(ImGui.checkbox("Filter to Creatures", filterToCreatures)){
                    filterToCreatures = !filterToCreatures;
                }
                if(ImGui.checkbox("Filter to Foliage", filterToFoliage)){
                    filterToFoliage = !filterToFoliage;
                }
                if(ImGui.checkbox("Filter to Terrain", filterToTerrain)){
                    filterToTerrain = !filterToTerrain;
                }
                if(ImGui.checkbox("Filter has Collidable", filterHasCollidable)){
                    filterHasCollidable = !filterHasCollidable;
                }
                for(Entity entity : Globals.clientState.clientSceneWrapper.getScene().getEntityList()){
                    //filters
                    if(filterToCreatures && !CreatureUtils.isCreature(entity)){
                        continue;
                    }
                    if(filterToFoliage && !FoliageUtils.isFoliage(entity)){
                        continue;
                    }
                    if(filterToTerrain && !TerrainChunk.isTerrainEntity(entity)){
                        continue;
                    }
                    if(filterHasCollidable && PhysicsEntityUtils.getCollidable(entity) == null){
                        continue;
                    }
                    if(filterBasic &&
                    (
                        AmbientFoliage.getAmbientFoliageTree(entity) != null ||
                        entity.containsKey(EntityDataStrings.TERRAIN_IS_TERRAIN) ||
                        entity.containsKey(EntityDataStrings.FLUID_IS_FLUID)
                    )
                    ){
                        continue;
                    }
                    ImGui.beginGroup();
                    ImGui.pushID(entity.getId());
                    ImGui.text("Id: " + entity.getId() + " (" + getEntityName(entity) + ")");
                    if(ImGui.button("Details")){
                        showEntity(entity);
                    }
                    ImGui.popID();
                    ImGui.endGroup();
                }
            }
        });
        clientEntityListWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(clientEntityListWindow);
    }

    /**
     * View details about a client entity
     */
    protected static void createClientEntityDetailWindow(){
        clientEntityDetailWindow = new ImGuiWindow("Entity Data");

        clientEntityDetailWindow.setCallback(new ImGuiWindowCallback() {
            @Override
            public void exec() {
                ImGui.text("Current ID: " + detailViewEntity.getId());
                if(ImGui.treeNode("Views")){
                    if(ImGui.checkbox("Data View", showDataTab)){
                        showDataTab = !showDataTab;
                    }
                    if(EntityLookupUtils.isServerEntity(detailViewEntity) && ImGui.checkbox("Server Details", showServerTab)){
                        showServerTab = !showServerTab;
                    }
                    if(EntityUtils.getActor(detailViewEntity) != null && ImGui.checkbox("Actor Details", showActorTab)){
                        showActorTab = !showActorTab;
                    }
                    if(InstancedActor.getInstancedActor(detailViewEntity) != null && ImGui.checkbox("Instanced Actor Details", showInstancedActorTab)){
                        showInstancedActorTab = !showInstancedActorTab;
                    }
                    if(EntityUtils.getPoseActor(detailViewEntity) != null && ImGui.checkbox("Pose Actor Details", showPoseActorTab)){
                        showPoseActorTab = !showPoseActorTab;
                    }
                    if(ClientEquipState.hasEquipState(detailViewEntity) && ImGui.checkbox("Equip State", showEquipStateTab)){
                        showEquipStateTab = !showEquipStateTab;
                    }
                    if(FirstPersonTree.hasTree(detailViewEntity) && ImGui.checkbox("First Person", showFirstPersonTab)){
                        showFirstPersonTab = !showFirstPersonTab;
                    }
                    if(
                        (
                            AttachUtils.hasChildren(detailViewEntity) ||
                            AttachUtils.getParent(detailViewEntity) != null ||
                            detailViewEntity == Globals.clientState.firstPersonEntity ||
                            detailViewEntity == Globals.clientState.playerEntity ||
                            Globals.clientState.clientSceneWrapper.clientToServerMapContainsId(detailViewEntity.getId())
                        ) &&
                        ImGui.checkbox("Linked entities`", showLinkedEntitiesTab)
                    ){
                        showLinkedEntitiesTab = !showLinkedEntitiesTab;
                    }
                    if(ServerPlayerViewDirTree.hasTree(detailViewEntity) && ImGui.checkbox("Server View Dir", showServerViewDirTab)){
                        showServerViewDirTab = !showServerViewDirTab;
                    }
                    if((PhysicsEntityUtils.getDBody(detailViewEntity) != null || PhysicsEntityUtils.getDGeom(detailViewEntity) != null) && ImGui.checkbox("Physics", showPhysicsTab)){
                        showPhysicsTab = !showPhysicsTab;
                    }
                    if(HitboxCollectionState.hasHitboxState(detailViewEntity) && ImGui.checkbox("Hitbox State", showHitboxTab)){
                        showHitboxTab = !showHitboxTab;
                    }
                    if(CommonEntityUtils.getCommonData(detailViewEntity) instanceof FoliageType && ImGui.checkbox("Foliage Data", showFoliageTab)){
                        showFoliageTab = !showFoliageTab;
                    }
                    if(ClientToolbarState.getClientToolbarState(detailViewEntity) != null && ImGui.checkbox("Toolbar Data", showToolbarTab)){
                        showToolbarTab = !showToolbarTab;
                    }
                    if(ClientInteractionEngine.hasInteractionBody(detailViewEntity) && ImGui.checkbox("Interaction Data", showInteractionTab)){
                        showInteractionTab = !showInteractionTab;
                    }
                    if(CommonEntityUtils.getCommonData(detailViewEntity) != null && CommonEntityUtils.getCommonData(detailViewEntity).getGridAlignedData() != null && ImGui.checkbox("Furniture Data", showFurnitureTab)){
                        showFurnitureTab = !showFurnitureTab;
                    }
                    if(
                        (InventoryUtils.hasNaturalInventory(detailViewEntity) || InventoryUtils.hasEquipInventory(detailViewEntity) || InventoryUtils.hasToolbarInventory(detailViewEntity)) && 
                        ImGui.checkbox("Inventory Data", showInventoryTab)
                    ){
                        showInventoryTab = !showInventoryTab;
                    }
                    if(ImGui.checkbox("Debug Actions", showDebugActionsTab)){
                        showDebugActionsTab = !showDebugActionsTab;
                    }
                    ImGui.treePop();
                }
                ImGui.nextColumn();
                ImGuiEntityServerTab.drawServerView(showServerTab, detailViewEntity);
                ImGuiEntityActorTab.drawActorView(showActorTab,detailViewEntity);
                ImGuiEntityActorTab.drawInstancedActorView(showInstancedActorTab, detailViewEntity);
                ImGuiEntityHitboxTab.drawHitboxTab(showHitboxTab,detailViewEntity);
                ImGuiEntityMacros.drawPoseActor();
                ImGuiEntityMacros.drawEquipState();
                ImGuiEntityMacros.drawFirstPersonView();
                ImGuiEntityMacros.drawLinkedEntities();
                ImGuiEntityMacros.drawServerViewDir();
                ImGuiEntityPhysicsTab.drawPhysicsView(showPhysicsTab, detailViewEntity);
                ImGuiEntityFoliageTab.drawFoliageView(showFoliageTab, detailViewEntity);
                ImGuiEntityInventoryTab.drawToolbarTab(showToolbarTab, detailViewEntity);
                ImGuiEntityInventoryTab.drawInventoryTab(showInventoryTab, detailViewEntity);
                ImGuiEntityInteractionTab.drawInteractionTab(showInteractionTab, detailViewEntity);
                ImGuiEntityFurnitureTab.drawFurnitureTab(showFurnitureTab, detailViewEntity);
                ImGuiEntityDebugActions.drawDebugActions(showDebugActionsTab, detailViewEntity);
                ImGuiEntityMacros.drawDataView();
            }
        });
        clientEntityDetailWindow.setOpen(false);
        Globals.renderingEngine.getImGuiPipeline().addImGuiWindow(clientEntityDetailWindow);
    }

    /**
     * Shows the entity window for a specific entity
     * @param entity The entity
     */
    public static void showEntity(Entity entity){
        detailViewEntity = entity;
        clientEntityDetailWindow.setOpen(true);
    }

    /**
     * Draws the data view
     */
    protected static void drawDataView(){
        if(showDataTab && ImGui.collapsingHeader("Data View")){
            if(detailViewEntity != null){
                for(String key : detailViewEntity.getKeys()){
                    ImGui.text(key);
                }
            }
        }
    }

    /**
     * Draws pose actor 
     */
    protected static void drawPoseActor(){
        if(showPoseActorTab && ImGui.collapsingHeader("Pose Actor Details")){
            ImGui.indent();
            if(detailViewEntity != null && EntityUtils.getPoseActor(detailViewEntity) != null){
                PoseActor poseActor = EntityUtils.getPoseActor(detailViewEntity);

                //animation queue
                if(ImGui.collapsingHeader("Animation Queue")){
                    Set<ActorAnimationMaskEntry> animationQueue = poseActor.getAnimationQueue();
                    for(ActorAnimationMaskEntry mask : animationQueue){
                        ImGui.text(mask.getAnimationName() + " -  " + mask.getPriority());
                        ImGui.text(mask.getDuration() + " " + mask.getTime());
                    }
                }

                //bone values
                if(ImGui.collapsingHeader("Bone Values")){
                    for(Bone bone : poseActor.getBoneValues()){
                        ImGui.text(bone.boneID);
                        ImGui.text("Position: " + poseActor.getBonePosition(bone.boneID));
                        ImGui.text("Rotation: " + poseActor.getBoneRotation(bone.boneID));
                        ImGui.text(bone.getFinalTransform() + "");
                    }
                }

                //Draws all the bones in the world
                if(ImGui.button("Draw Bones")){
                    Globals.renderingEngine.getDebugContentPipeline().getDebugBonesPipeline().setEntity(detailViewEntity);
                }
    
                //Browsable list of all animations with their data
                if(ImGui.collapsingHeader("Animation Channel Data")){
                    Model model = Globals.assetManager.fetchModel(poseActor.getModelPath());
                    ImGui.indent();
                    for(Animation animation : model.getAnimations()){
                        if(ImGui.collapsingHeader(animation.name)){
                            if(ImGui.button("Play")){
                                poseActor.playAnimation(animation.name, AnimationPriorities.getValue(AnimationPriorities.MODIFIER_MAX));
                            }
                            for(AnimChannel channel : animation.channels){
                                ImGui.pushID(channel.getNodeID());
                                if(ImGui.button("Fully describe")){
                                    channel.fullDescribeChannel();
                                }
                                ImGui.text("=" + channel.getNodeID() + "=");
                                ImGui.text("" + channel.getCurrentPosition());
                                ImGui.text("" + channel.getCurrentRotation());
                                ImGui.text("" + channel.getCurrentScale());
                                ImGui.popID();
                            }
                        }
                    }
                    ImGui.unindent();
                }

                //print data macros
                if(ImGui.collapsingHeader("Print Data")){
                    //print bone values
                    if(ImGui.button("Print current bone values")){
                        for(Bone bone : poseActor.getBoneValues()){
                            LoggerInterface.loggerRenderer.DEBUG(bone.boneID);
                            LoggerInterface.loggerRenderer.DEBUG("" + bone.getFinalTransform());
                        }
                    }
        
                    //print animation keys
                    if(ImGui.button("Print animation keys")){
                        PoseModel model = Globals.assetManager.fetchPoseModel(poseActor.getModelPath());
                        model.describeAllAnimations();
                    }
                }
            }
            ImGui.unindent();
        }
    }

    /**
     * First person data
     */
    protected static void drawFirstPersonView(){
        if(showFirstPersonTab && ImGui.collapsingHeader("First Person Tree")){
            ImGui.indent();
            // FirstPersonTree firstPersonTree = FirstPersonTree.getTree(detailViewEntity);
            if(ImGui.button("Visualize facing vectors")){
                DebugVisualizerUtils.clientSpawnVectorVisualizer((Entity vector) -> {
                    EntityUtils.getPosition(vector).set(EntityUtils.getPosition(Globals.clientState.firstPersonEntity));
                    EntityUtils.getRotation(vector).set(EntityUtils.getRotation(Globals.clientState.firstPersonEntity));
                });
                DebugVisualizerUtils.clientSpawnVectorVisualizer((Entity vector) -> {
                    EntityUtils.getPosition(vector).set(EntityUtils.getPosition(Globals.clientState.playerEntity));
                    EntityUtils.getRotation(vector).set(EntityUtils.getRotation(Globals.clientState.playerEntity));
                });
                DebugVisualizerUtils.clientSpawnVectorVisualizer((Entity vector) -> {
                    int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId());
                    Entity serverPlayerEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
                    EntityUtils.getPosition(vector).set(EntityUtils.getPosition(serverPlayerEntity));
                    EntityUtils.getRotation(vector).set(EntityUtils.getRotation(serverPlayerEntity));
                });
            }
            ImGui.unindent();
        }
    }


    //stores the edited rotation values
    private static float[] rotationValuesFirstPerson = new float[]{
        0,0,0
    };
    private static float[] rotationValuesThirdPerson = new float[]{
        0,0,0
    };
    private static float[] vectorValuesFirstPerson = new float[]{
        0,0,0
    };
    private static float[] vectorValuesThirdPerson = new float[]{
        0,0,0
    };

    //constraints for vector offset
    private static final float MAXIMUM_OFFSET = 1;
    private static final float MINIMUM_OFFSET = -MAXIMUM_OFFSET;

    /**
     * Client scene equip state view
     */
    protected static void drawEquipState(){
        if(showEquipStateTab && ImGui.collapsingHeader("Equip State Details")){
            ImGui.indent();
            if(detailViewEntity != null && ClientEquipState.getClientEquipState(detailViewEntity) != null){
                ClientEquipState clientEquipState = ClientEquipState.getClientEquipState(detailViewEntity);
                if(ImGui.collapsingHeader("All Equip Points")){
                    for(EquipPoint point : clientEquipState.getAllEquipPoints()){
                        if(ImGui.collapsingHeader(point.getEquipPointId())){
                            ImGui.text("Has item equipped: " + (clientEquipState.getEquippedItemAtPoint(point.getEquipPointId()) != null));
                            ImGui.text("Bone (Third Person): " + point.getBone());
                            ImGui.text("Bone (First Person): " + point.getFirstPersonBone());

                            //offsets
                            ImGui.text("[Third Person] Offset: " + AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()));
                            if(ImGui.sliderFloat3("[Third Person] Offset", vectorValuesThirdPerson, MINIMUM_OFFSET, MAXIMUM_OFFSET)){
                                Vector3d offset = new Vector3d(vectorValuesThirdPerson[0],vectorValuesThirdPerson[1],vectorValuesThirdPerson[2]);
                                List<Float> newValues = new LinkedList<Float>();
                                newValues.add((float)offset.x);
                                newValues.add((float)offset.y);
                                newValues.add((float)offset.z);
                                point.setOffsetVectorThirdPerson(newValues);
                                Entity equippedEntity = clientEquipState.getEquippedItemAtPoint(point.getEquipPointId());
                                if(equippedEntity != null){
                                    AttachUtils.setVectorOffset(equippedEntity, AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()));
                                }
                            }
                            ImGui.text("[First Person] Offset: " + AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorFirstPerson()));
                            if(ImGui.sliderFloat3("[First Person] Offset", vectorValuesFirstPerson, MINIMUM_OFFSET, MAXIMUM_OFFSET)){
                                Vector3d offset = new Vector3d(vectorValuesFirstPerson[0],vectorValuesFirstPerson[1],vectorValuesFirstPerson[2]);
                                List<Float> newValues = new LinkedList<Float>();
                                newValues.add((float)offset.x);
                                newValues.add((float)offset.y);
                                newValues.add((float)offset.z);
                                point.setOffsetVectorFirstPerson(newValues);
                                Entity equippedEntity = clientEquipState.getEquippedItemAtPoint(point.getEquipPointId());
                                if(equippedEntity != null){
                                    AttachUtils.setVectorOffset(equippedEntity, AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorFirstPerson()));
                                }
                            }

                            //rotations
                            ImGui.text("[Third Person] Rotation: " + AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson()));
                            if(ImGui.sliderFloat3("[Third Person] Rotation (In Euler along x,y,z)", rotationValuesThirdPerson, 0, (float)(Math.PI * 2))){
                                Quaterniond rotation = new Quaterniond().rotateXYZ(rotationValuesThirdPerson[0], rotationValuesThirdPerson[1], rotationValuesThirdPerson[2]);
                                List<Float> newValues = new LinkedList<Float>();
                                newValues.add((float)rotation.x);
                                newValues.add((float)rotation.y);
                                newValues.add((float)rotation.z);
                                newValues.add((float)rotation.w);
                                point.setOffsetRotationThirdPerson(newValues);
                                Entity equippedEntity = clientEquipState.getEquippedItemAtPoint(point.getEquipPointId());
                                if(equippedEntity != null){
                                    AttachUtils.setRotationOffset(equippedEntity, AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson()));
                                }
                            }
                            ImGui.text("[First Person] Rotation: " + AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationFirstPerson()));
                            if(ImGui.sliderFloat3("[First Person] Rotation (In Euler along x,y,z)", rotationValuesFirstPerson, 0, (float)(Math.PI * 2))){
                                Quaterniond rotation = new Quaterniond().rotateXYZ(rotationValuesFirstPerson[0], rotationValuesFirstPerson[1], rotationValuesFirstPerson[2]);
                                List<Float> newValues = new LinkedList<Float>();
                                newValues.add((float)rotation.x);
                                newValues.add((float)rotation.y);
                                newValues.add((float)rotation.z);
                                newValues.add((float)rotation.w);
                                point.setOffsetRotationFirstPerson(newValues);
                                Entity equippedEntity = clientEquipState.getEquippedItemAtPoint(point.getEquipPointId());
                                if(equippedEntity != null){
                                    AttachUtils.setRotationOffset(equippedEntity, AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationFirstPerson()));
                                }
                            }
                            if(ImGui.button("Print transforms")){
                                LoggerInterface.loggerEngine.WARNING("Third person Offset: " + AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorThirdPerson()));
                                LoggerInterface.loggerEngine.WARNING("First person Offset: " + AttachUtils.getEquipPointVectorOffset(point.getOffsetVectorFirstPerson()));
                                LoggerInterface.loggerEngine.WARNING("Third person Rotation: " + AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationThirdPerson()));
                                LoggerInterface.loggerEngine.WARNING("First person Rotation: " + AttachUtils.getEquipPointRotationOffset(point.getOffsetRotationFirstPerson()));
                            }
                        }
                    }
                }
                
            }
            ImGui.unindent();
        }
    }

    /**
     * Client scene equip state view
     */
    protected static void drawLinkedEntities(){
        if(showLinkedEntitiesTab && ImGui.collapsingHeader("Linked entities")){
            ImGui.indent();
            if(detailViewEntity == Globals.clientState.playerEntity && ImGui.button("View Model")){
                ImGuiEntityMacros.showEntity(Globals.clientState.firstPersonEntity);
            }
            if(detailViewEntity == Globals.clientState.firstPersonEntity && ImGui.button("3rd Person Model")){
                ImGuiEntityMacros.showEntity(Globals.clientState.playerEntity);
            }
            if(AttachUtils.getParent(detailViewEntity) != null && ImGui.button("Parent")){
                ImGuiEntityMacros.showEntity(AttachUtils.getParent(detailViewEntity));
            }
            if(AttachUtils.hasChildren(detailViewEntity) && ImGui.collapsingHeader("Children")){
                for(Entity child : AttachUtils.getChildrenList(detailViewEntity)){
                    if(ImGui.button("Child " + child.getId())){
                        ImGuiEntityMacros.showEntity(child);
                    }
                }
            }
            if(ClientEquipState.hasEquipState(detailViewEntity) && ImGui.collapsingHeader("Equipped")){
                ClientEquipState clientEquipState = ClientEquipState.getClientEquipState(detailViewEntity);
                for(String equippedPoint : clientEquipState.getEquippedPoints()){
                    Entity entity = clientEquipState.getEquippedItemAtPoint(equippedPoint);
                    if(ImGui.button("Slot: " + equippedPoint)){
                        ImGuiEntityMacros.showEntity(entity);
                    }
                }
            }
            if(Globals.clientState.clientSceneWrapper.clientToServerMapContainsId(detailViewEntity.getId())){
                //detailViewEntity is a client entity
                //get server entity
                int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(detailViewEntity.getId());
                Entity serverEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
                if(serverEntity != null && ImGui.button("Server Entity")){
                    ImGuiEntityMacros.showEntity(serverEntity);
                }
            } else if(Globals.clientState.clientSceneWrapper.containsServerId(detailViewEntity.getId())){
                //detailViewEntity is a server entity
                //get client entity
                int clientId = Globals.clientState.clientSceneWrapper.mapServerToClientId(detailViewEntity.getId());
                Entity clientEntity = Globals.clientState.clientSceneWrapper.getScene().getEntityFromId(clientId);
                if(clientEntity != null && ImGui.button("Client Entity")){
                    ImGuiEntityMacros.showEntity(clientEntity);
                }
            }
            ImGui.unindent();
        }
    }

    /**
     * Server view dir
     */
    protected static void drawServerViewDir(){
        if(showServerViewDirTab && ImGui.collapsingHeader("Server View Dir")){
            ImGui.indent();
            if(ServerPlayerViewDirTree.hasTree(detailViewEntity)){
                ServerPlayerViewDirTree viewDirTree = ServerPlayerViewDirTree.getTree(detailViewEntity);
                ImGui.text("Yaw: " + viewDirTree.getYaw());
                ImGui.text("Pitch: " + viewDirTree.getPitch());
            }
            ImGui.unindent();
        }
    }

    /**
     * Gets the displayed name of an entity (ie creature type, foliage type, terrain, etc)
     * @param entity
     * @return
     */
    private static String getEntityName(Entity entity){
        if(CreatureUtils.isCreature(entity)){
            return CreatureUtils.getType(entity);
        }
        if(ItemUtils.isItem(entity)){
            return ItemUtils.getType(entity);
        }
        if(FoliageUtils.isFoliage(entity)){
            return FoliageUtils.getFoliageType(entity).getId();
        }
        return "Entity";
    }

}
