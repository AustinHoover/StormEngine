package electrosphere.renderer.pipelines.debug;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.joml.AABBd;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL45;
import org.ode4j.ode.DAABBC;
import org.ode4j.ode.DCapsule;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DSphere;

import electrosphere.client.block.BlockChunkData;
import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.client.interact.select.AreaSelection;
import electrosphere.collision.CollisionEngine;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.PhysicsUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.block.fab.FurnitureSlotMetadata;
import electrosphere.data.block.fab.RoomMetadata;
import electrosphere.data.block.fab.StructureMetadata;
import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.grident.GridAlignedData;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.DrawableUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.hitbox.HitboxCollectionState;
import electrosphere.entity.state.hitbox.HitboxCollectionState.HitboxState;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.meshgen.GeometryMeshGen;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.pipelines.RenderPipeline;
import electrosphere.renderer.texture.Texture;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.utils.EntityLookupUtils;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.civilization.road.Road;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.path.MacroPathNode;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.util.math.SpatialMathUtils;
import electrosphere.util.math.region.RegionPrism;

/**
 * Pipeline for rendering content to assist debugging
 */
public class DebugContentPipeline implements RenderPipeline {

    //The bone debugging pipeline
    DebugBonesPipeline debugBonesPipeline = new DebugBonesPipeline();

    /**
     * The farm plot macro data entities to draw
     */
    private List<Entity> farmPlotEntities = new LinkedList<Entity>();

    /**
     * Renderables for macro nav graph entities
     */
    private List<Entity> macroNavEntities = new LinkedList<Entity>();

    /**
     * The set of points to visualize a path along
     */
    private List<Vector3d> pathPoints = null;

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("DebugContentPipeline.render");

        //bind screen fbo
        RenderingEngine.screenFramebuffer.bind(openGLState);
        openGLState.glDepthTest(true);
        openGLState.glDepthFunc(GL45.GL_LESS);
        GL45.glDepthMask(true);
        openGLState.glViewport(Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
        
        ///
        ///     R E N D E R I N G      S T U F F
        ///
        //Sets the background color.


        //
        // Set render pipeline state
        //
        renderPipelineState.setUseMeshShader(true);
        renderPipelineState.setBufferStandardUniforms(true);
        renderPipelineState.setBufferNonStandardUniforms(false);
        renderPipelineState.setUseMaterial(false);
        renderPipelineState.setUseShadowMap(true);
        renderPipelineState.setUseBones(true);
        renderPipelineState.setUseLight(true);

        Matrix4d modelTransformMatrix = new Matrix4d();

        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawCollisionSpheresClient()){
            for(HitboxCollectionState hitboxState : Globals.clientState.clientSceneWrapper.getHitboxManager().getAllHitboxes()){
                DebugContentPipeline.renderHitboxes(openGLState, renderPipelineState, modelTransformMatrix, hitboxState);
            }
        }
        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawCollisionSpheresServer()){
            int serverIdForClientEntity = Globals.clientState.clientSceneWrapper.mapClientToServerId(Globals.clientState.playerEntity.getId());
            Entity serverPlayerEntity = EntityLookupUtils.getEntityById(serverIdForClientEntity);
            Realm playerRealm = Globals.serverState.realmManager.getEntityRealm(serverPlayerEntity);
            List<HitboxCollectionState> hitboxStates = new LinkedList<HitboxCollectionState>(playerRealm.getHitboxManager().getAllHitboxes());
            for(HitboxCollectionState hitboxState : hitboxStates){
                DebugContentPipeline.renderHitboxes(openGLState, renderPipelineState, modelTransformMatrix, hitboxState);
            }
        }

        //render client physics objects
        if(Globals.gameConfigCurrent.getSettings().graphicsDebugDrawPhysicsObjectsClient()){
            CollisionEngine engine = Globals.clientState.clientSceneWrapper.getCollisionEngine();
            for(Collidable collidable : engine.getCollidables()){
                Entity physicsEntity = collidable.getParent();
                if((boolean)physicsEntity.getData(EntityDataStrings.DATA_STRING_DRAW) && physicsEntity.getData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE) != null){
                    CollidableTemplate template = (CollidableTemplate)physicsEntity.getData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE);
                    DebugContentPipeline.renderCollidable(openGLState, renderPipelineState, modelTransformMatrix, physicsEntity, template);
                }
            }
        }

        //render server physics objects
        if(Globals.gameConfigCurrent.getSettings().graphicsDebugDrawPhysicsObjectsServer()){
            CollisionEngine engine = Globals.serverState.realmManager.first().getCollisionEngine();
            LinkedList<Collidable> collidables = new LinkedList<Collidable>(engine.getCollidables());
            for(Collidable collidable : collidables){
                Entity physicsEntity = collidable.getParent();
                if(physicsEntity.getData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE) != null){
                    CollidableTemplate template = (CollidableTemplate)physicsEntity.getData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE);
                    DebugContentPipeline.renderCollidable(openGLState, renderPipelineState, modelTransformMatrix, physicsEntity, template);
                }
            }
        }

        //render interaction engine collidables
        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawInteractionCollidables()){
            CollisionEngine engine = Globals.clientState.clientSceneWrapper.getInteractionEngine();
            for(Collidable collidable : engine.getCollidables()){
                Entity physicsEntity = collidable.getParent();
                if((boolean)physicsEntity.getData(EntityDataStrings.DATA_STRING_DRAW) && physicsEntity.getData(EntityDataStrings.INTERACTION_TEMPLATE) != null){
                    CollidableTemplate template = (CollidableTemplate)physicsEntity.getData(EntityDataStrings.INTERACTION_TEMPLATE);
                    DebugContentPipeline.renderCollidable(openGLState, renderPipelineState, modelTransformMatrix, physicsEntity, template);
                }
            }
        }

        //render current structure data
        if(
            Globals.clientState.clientLevelEditorData.getCurrentFab() != null &&
            Globals.clientState.clientLevelEditorData.getCurrentFab().getFabMetadata() != null &&
            Globals.clientState.clientLevelEditorData.getCurrentFab().getFabMetadata().getStructureData() != null
        ){
            StructureMetadata structureData = Globals.clientState.clientLevelEditorData.getCurrentFab().getFabMetadata().getStructureData();
            if(structureData.getBoundingArea() != null){
                DebugContentPipeline.renderAreaSelection(
                    openGLState, renderPipelineState, modelTransformMatrix, 
                    structureData.getBoundingArea(), AssetDataStrings.TEXTURE_RED_TRANSPARENT
                );
                if(structureData.getRooms() != null){
                    for(RoomMetadata roomArea : structureData.getRooms()){
                        DebugContentPipeline.renderAreaSelection(
                            openGLState, renderPipelineState, modelTransformMatrix, 
                            roomArea.getArea(), AssetDataStrings.TEXTURE_TEAL_TRANSPARENT
                        );
                        //render entry points
                        for(Vector3d entrypoint : roomArea.getEntryPoints()){
                            DebugContentPipeline.renderPoint(openGLState, renderPipelineState, modelTransformMatrix, entrypoint, 0.5f, AssetDataStrings.TEXTURE_RED_TRANSPARENT);
                        }
                        //render furniture slots
                        for(FurnitureSlotMetadata furnitureSlot : roomArea.getFurnitureSlots()){
                            DebugContentPipeline.renderAreaSelection(openGLState, renderPipelineState, modelTransformMatrix, furnitureSlot.getArea(), AssetDataStrings.TEXTURE_RED_TRANSPARENT);
                        }
                    }
                }
            }
        }

        //
        //Draw grid alignment data
        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawGridAlignment()){
            Model physicsGraphicsModel = Globals.assetManager.fetchModel(AssetDataStrings.UNITCUBE);
            for(Entity entity : Globals.clientState.clientSceneWrapper.getScene().getEntityList()){
                CommonEntityType data = CommonEntityUtils.getCommonData(entity);
                if(data == null){
                    continue;
                }
                if(data.getGridAlignedData() != null){
                    GridAlignedData gridAlignedData = data.getGridAlignedData();
                    Texture texture = Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_BLUE_TRANSPARENT);
                    if(texture != null){
                        texture.bind(openGLState);
                    }
                    Vector3d position = EntityUtils.getPosition(entity);
                    //calculate camera-modified vector3d
                    Vector3d cameraModifiedPosition = new Vector3d(position).add(0,gridAlignedData.getHeight() * BlockChunkData.BLOCK_SIZE_MULTIPLIER / 2.0f,0).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    modelTransformMatrix.identity();
                    modelTransformMatrix.translate(cameraModifiedPosition);
                    modelTransformMatrix.rotate(EntityUtils.getRotation(entity));
                    modelTransformMatrix.scale(
                        gridAlignedData.getWidth() * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        gridAlignedData.getHeight() * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
                        gridAlignedData.getLength() * BlockChunkData.BLOCK_SIZE_MULTIPLIER
                    );
                    physicsGraphicsModel.setModelMatrix(modelTransformMatrix);
                    physicsGraphicsModel.draw(renderPipelineState,openGLState);
                }
            }
        }

        //
        //Draw town data
        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawMacroColliders()){
            for(VirtualStructure struct : Globals.serverState.realmManager.first().getMacroData().getStructures()){
                DebugContentPipeline.renderAABB(openGLState, renderPipelineState, modelTransformMatrix, struct.getAABB(), AssetDataStrings.TEXTURE_BLUE_TRANSPARENT);
            }
            for(Road road : Globals.serverState.realmManager.first().getMacroData().getRoads()){
                DebugContentPipeline.renderTube(openGLState, renderPipelineState, modelTransformMatrix, road.getPoint1(), road.getPoint2(), road.getRadius(), AssetDataStrings.TEXTURE_BLUE_TRANSPARENT);
            }
            if(this.farmPlotEntities.isEmpty()){
                MacroData macroData = Globals.serverState.realmManager.first().getMacroData();
                List<MacroRegion> farmPlots = macroData.getTown(0).getFarmPlots(macroData);
                if(farmPlots.size() > 0){
                    for(MacroRegion region : farmPlots){
                        if(region.getRegion() instanceof RegionPrism prism){
                            Entity plotDebugEnt = EntityCreationUtils.createClientSpatialEntity();
                            DrawableUtils.makeEntityDrawable(plotDebugEnt, () -> {
                                Vector3d[] finalPoints = new Vector3d[prism.getPoints().length];
                                for(int i = 0; i < finalPoints.length; i++){
                                    finalPoints[i] = new Vector3d(prism.getPoints()[i]).sub(prism.getAABB().minX,prism.getAABB().minY,prism.getAABB().minZ);
                                }
                                return GeometryMeshGen.genPrism(finalPoints, prism.getHeight());
                            });
                            EntityUtils.getPosition(plotDebugEnt).set(prism.getAABB().minX,prism.getAABB().minY,prism.getAABB().minZ);
                            this.farmPlotEntities.add(plotDebugEnt);
                        }
                    }
                }
            }
            if(this.macroNavEntities.isEmpty()){
                MacroData macroData = Globals.serverState.realmManager.first().getMacroData();
                List<MacroPathNode> pathNodes = macroData.getPathCache().getNodes();
                if(pathNodes.size() > 0){
                    for(MacroPathNode pathNode : pathNodes){
                        Entity pathDebugEnt = EntityCreationUtils.createClientSpatialEntity();
                        DrawableUtils.makeEntityDrawable(pathDebugEnt, AssetDataStrings.MODEL_WAYPOINT);
                        EntityUtils.getPosition(pathDebugEnt).set(pathNode.getPosition());
                        EntityUtils.getScale(pathDebugEnt).set(1);
                        this.macroNavEntities.add(pathDebugEnt);
                        //draw paths between nodes
                        List<MacroPathNode> neighbors = pathNode.getNeighborNodes(macroData.getPathCache());
                        for(MacroPathNode neighbor : neighbors){
                            if(neighbor.getId() < pathNode.getId()){

                            }
                        }
                    }
                }
            } else {
                //render connections between points
                MacroData macroData = Globals.serverState.realmManager.first().getMacroData();
                List<MacroPathNode> pathNodes = macroData.getPathCache().getNodes();
                if(pathNodes.size() > 0){
                    for(MacroPathNode pathNode : pathNodes){
                        //draw paths between nodes
                        List<MacroPathNode> neighbors = pathNode.getNeighborNodes(macroData.getPathCache());
                        for(MacroPathNode neighbor : neighbors){
                            if(neighbor.getId() < pathNode.getId()){
                                DebugContentPipeline.renderTube(openGLState, renderPipelineState, modelTransformMatrix, pathNode.getPosition(), neighbor.getPosition(), 1, AssetDataStrings.TEXTURE_YELLOW_TRANSPARENT);
                            }
                        }
                    }
                }
            }
        } else {
            if(!this.farmPlotEntities.isEmpty()){
                for(Entity entity : this.farmPlotEntities){
                    ClientEntityUtils.destroyEntity(entity);
                }
                this.farmPlotEntities.clear();
            }
            if(!this.macroNavEntities.isEmpty()){
                for(Entity entity : this.macroNavEntities){
                    ClientEntityUtils.destroyEntity(entity);
                }
                this.macroNavEntities.clear();
            }
        }

        //
        //Path points rendering
        if(this.pathPoints != null && pathPoints.size() > 1){
            for(int i = 0; i < pathPoints.size() - 1; i++){
                DebugContentPipeline.renderTube(openGLState, renderPipelineState, modelTransformMatrix, pathPoints.get(i), pathPoints.get(i+1), 1, AssetDataStrings.TEXTURE_RED_TRANSPARENT);
            }
        }

        //
        //Draw cell colliders data
        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawClientCellColliders()){
            for(Entity ent : Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.TERRAIN)){
                if(PhysicsEntityUtils.getDGeom(ent) != null){
                    Vector3d entPos = EntityUtils.getPosition(ent);
                    DebugContentPipeline.renderAABB(openGLState, renderPipelineState, modelTransformMatrix, new Vector3d(entPos).add(4,4,4), new Vector3d(entPos).add(12,12,12), AssetDataStrings.TEXTURE_RED_TRANSPARENT);
                }
            }
        }
        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawServerCellColliders()){
            Vector3d floatingOrigin = Globals.serverState.realmManager.first().getCollisionEngine().getFloatingOrigin();
            Collection<Entity> entities = Collections.unmodifiableCollection(EntityLookupUtils.getAllEntities());
            for(Entity ent : entities){
                if(PhysicsEntityUtils.getDGeom(ent) != null && ent.containsKey(EntityDataStrings.TERRAIN_IS_TERRAIN)){
                    DGeom geom = PhysicsEntityUtils.getDGeom(ent);
                    DAABBC aabb = geom.getAABB();
                    Vector3d min = new Vector3d(aabb.getMin0(),aabb.getMin1(),aabb.getMin2()).add(floatingOrigin);
                    Vector3d max = new Vector3d(aabb.getMax0(),aabb.getMax1(),aabb.getMax2()).add(floatingOrigin);
                    DebugContentPipeline.renderAABB(openGLState, renderPipelineState, modelTransformMatrix, min, max, AssetDataStrings.TEXTURE_RED_TRANSPARENT);
                }
            }
        }

        //
        //Draw facing vectors
        if(Globals.gameConfigCurrent.getSettings().getGraphicsDebugDrawServerFacingVectors()){
            Realm realm = Globals.serverState.realmManager.first();
            Collection<Entity> entities = realm.getDataCellManager().entityLookup(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera), 100);
            for(Entity ent : entities){
                if(CreatureUtils.getFacingVector(ent) != null){
                    DebugContentPipeline.renderTube(openGLState, renderPipelineState, modelTransformMatrix, EntityUtils.getPosition(ent), new Vector3d(EntityUtils.getPosition(ent)).add(CreatureUtils.getFacingVector(ent)), 0.3, AssetDataStrings.TEXTURE_BLUE_TRANSPARENT);
                }
            }
        }

        //update pipeline state to use mats again
        renderPipelineState.setUseMaterial(true);
        
        if(Globals.gameConfigCurrent.getSettings().graphicsDebugDrawNavmesh()){
            throw new Error("Not yet implemented!");
        }

        debugBonesPipeline.render(openGLState, renderPipelineState);

        Globals.profiler.endCpuSample();
    }

    /**
     * Gets the color texture to use to draw a hitbox
     * @param shapeStatus The hitbox status
     * @param data The hitbox data
     * @return The texture path to use
     */
    private static String getHitboxColor(HitboxState shapeStatus, HitboxData data){
        switch(data.getType()){
            case HitboxData.HITBOX_TYPE_BLOCK_CONNECTED: {
                if(shapeStatus.getHadCollision()){
                    return AssetDataStrings.TEXTURE_YELLOW_TRANSPARENT;
                }
                if(shapeStatus.isActive()){
                    return AssetDataStrings.TEXTURE_BLUE_TRANSPARENT;
                }
                return AssetDataStrings.TEXTURE_GREY_TRANSPARENT;
            }
            case HitboxData.HITBOX_TYPE_HIT:
            case HitboxData.HITBOX_TYPE_HIT_CONNECTED: {
                if(shapeStatus.getHadCollision()){
                    return AssetDataStrings.TEXTURE_YELLOW_TRANSPARENT;
                }
                if(shapeStatus.isActive()){
                    if(shapeStatus.isBlockOverride()){
                        return AssetDataStrings.TEXTURE_BLUE_TRANSPARENT;
                    }
                    return AssetDataStrings.TEXTURE_RED_TRANSPARENT;
                }
                return AssetDataStrings.TEXTURE_GREY_TRANSPARENT;
            }
            case HitboxData.HITBOX_TYPE_HURT:
            case HitboxData.HITBOX_TYPE_HURT_CONNECTED: {
                if(shapeStatus.getHadCollision()){
                    return AssetDataStrings.TEXTURE_YELLOW_TRANSPARENT;
                }
                return AssetDataStrings.TEXTURE_GREY_TRANSPARENT;
            }
        }
        return AssetDataStrings.TEXTURE_GREY_TRANSPARENT;
    }

    /**
     * Renders a collidable
     * @param openGLState The opengl state
     * @param renderPipelineState The render pipeline state
     * @param modelTransformMatrix The model transform matrix
     * @param physicsEntity The entity
     * @param template The template
     */
    private static void renderCollidable(OpenGLState openGLState, RenderPipelineState renderPipelineState, Matrix4d modelTransformMatrix, Entity physicsEntity, CollidableTemplate template){
        Model physicsGraphicsModel;
        if((boolean)physicsEntity.getData(EntityDataStrings.DATA_STRING_DRAW)){
            switch(template.getType()){
                case CollidableTemplate.COLLIDABLE_TYPE_CYLINDER: {
                    if((physicsGraphicsModel = Globals.assetManager.fetchModel(AssetDataStrings.UNITCYLINDER)) != null){
                        //set color based on collision status, type, etc
                        Texture texture = Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_BLUE_TRANSPARENT);
                        if(texture != null){
                            texture.bind(openGLState);
                        }
                        Vector3d position = EntityUtils.getPosition(physicsEntity);
                        //calculate camera-modified vector3d
                        Vector3d cameraModifiedPosition = new Vector3d(position).add(template.getOffsetX(),template.getOffsetY(),template.getOffsetZ()).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                        modelTransformMatrix.identity();
                        modelTransformMatrix.translate(cameraModifiedPosition);
                        modelTransformMatrix.rotate(EntityUtils.getRotation(physicsEntity));
                        modelTransformMatrix.scale(template.getDimension1(),template.getDimension2() * 0.5,template.getDimension3());
                        physicsGraphicsModel.setModelMatrix(modelTransformMatrix);
                        physicsGraphicsModel.draw(renderPipelineState,openGLState);
                    }
                } break;
                case CollidableTemplate.COLLIDABLE_TYPE_CUBE: {
                    if((physicsGraphicsModel = Globals.assetManager.fetchModel(AssetDataStrings.UNITCUBE)) != null){
                        //set color based on collision status, type, etc
                        Texture texture = Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_BLUE_TRANSPARENT);
                        if(texture != null){
                            texture.bind(openGLState);
                        }
                        Vector3d position = EntityUtils.getPosition(physicsEntity);
                        Quaterniond rotation = EntityUtils.getRotation(physicsEntity);
                        //calculate camera-modified vector3d
                        Vector3d cameraModifiedPosition = new Vector3d(position).add(template.getOffsetX(),template.getOffsetY(),template.getOffsetZ()).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                        modelTransformMatrix.identity();
                        modelTransformMatrix.translate(cameraModifiedPosition);
                        modelTransformMatrix.rotate(rotation);
                        modelTransformMatrix.scale(template.getDimension1(),template.getDimension2(),template.getDimension3());
                        physicsGraphicsModel.setModelMatrix(modelTransformMatrix);
                        physicsGraphicsModel.draw(renderPipelineState,openGLState);
                    }
                } break;
                case CollidableTemplate.COLLIDABLE_TYPE_CAPSULE: {
                    if((physicsGraphicsModel = Globals.assetManager.fetchModel(AssetDataStrings.UNITCYLINDER)) != null){
                        //set color based on collision status, type, etc
                        Texture texture = Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_BLUE_TRANSPARENT);
                        if(texture != null){
                            texture.bind(openGLState);
                        }
                        Vector3d position = EntityUtils.getPosition(physicsEntity);
                        Quaterniond rotation = EntityUtils.getRotation(physicsEntity);
                        //calculate camera-modified vector3d
                        Vector3d cameraModifiedPosition = new Vector3d(position).add(template.getOffsetX(),template.getDimension1() + template.getOffsetY(),template.getOffsetZ()).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                        modelTransformMatrix.identity();
                        modelTransformMatrix.translate(cameraModifiedPosition);
                        modelTransformMatrix.rotate(rotation);
                        modelTransformMatrix.scale(template.getDimension1(),template.getDimension2() * 0.5 + template.getDimension1() + template.getDimension1(),template.getDimension3());
                        physicsGraphicsModel.setModelMatrix(modelTransformMatrix);
                        physicsGraphicsModel.draw(renderPipelineState,openGLState);
                    }
                } break;
                default: {
                    throw new Error("Unsupported shape type!");
                }
            }
        }
    }

    /**
     * Renders a hitbox collection state
     * @param openGLState The opengl state
     * @param renderPipelineState The render pipeline state
     * @param modelTransformMatrix The model transform matrix
     * @param hitboxState The hitbox collection state
     */
    private static void renderHitboxes(OpenGLState openGLState, RenderPipelineState renderPipelineState, Matrix4d modelTransformMatrix, HitboxCollectionState hitboxState){
        Model hitboxModel;
        for(DGeom geom : hitboxState.getGeometries()){
            if(geom instanceof DSphere){
                DSphere sphereView = (DSphere)geom;
                HitboxState shapeStatus = hitboxState.getShapeStatus(geom);
                if((hitboxModel = Globals.assetManager.fetchModel(AssetDataStrings.UNITSPHERE)) != null){
                    //set color based on collision status, type, etc
                    Texture texture = Globals.assetManager.fetchTexture(getHitboxColor(shapeStatus,shapeStatus.getHitboxData()));
                    if(texture != null){
                        texture.bind(openGLState);
                    }
                    Vector3d position = PhysicsUtils.odeVecToJomlVec(sphereView.getPosition());
                    //calculate camera-modified vector3d
                    Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    modelTransformMatrix.identity();
                    modelTransformMatrix.translate(cameraModifiedPosition);
                    modelTransformMatrix.scale(sphereView.getRadius() * 2);
                    hitboxModel.setModelMatrix(modelTransformMatrix);
                    hitboxModel.draw(renderPipelineState,openGLState);
                }
            }
            if(geom instanceof DCapsule){
                DCapsule capsuleView = (DCapsule)geom;
                HitboxState shapeStatus = hitboxState.getShapeStatus(geom);
                if((hitboxModel = Globals.assetManager.fetchModel(AssetDataStrings.UNITCYLINDER)) != null){
                    //set color based on collision status, type, etc
                    Texture texture = Globals.assetManager.fetchTexture(getHitboxColor(shapeStatus,shapeStatus.getHitboxData()));
                    if(texture != null){
                        texture.bind(openGLState);
                    }
                    Vector3d position = PhysicsUtils.odeVecToJomlVec(capsuleView.getPosition());
                    //calculate camera-modified vector3d
                    Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    modelTransformMatrix.identity();
                    modelTransformMatrix.translate(cameraModifiedPosition);
                    //since you're directly accessing the quat from the body, need to adjust it to be in the correct orientation
                    modelTransformMatrix.rotate(PhysicsUtils.odeQuatToJomlQuat(capsuleView.getQuaternion()).mul(new Quaterniond(0.707,0,0,0.707)));
                    //the ode4j capsule's end caps are always at least radius length, the length only controls the distance between the two caps.
                    //unfortunately that won't be easy to replicate with rendering tech currently; instead, run logic below
                    double radius = capsuleView.getRadius();
                    double length = capsuleView.getLength();
                    if(length < radius) length = radius;
                    modelTransformMatrix.scale(radius,length,radius);
                    hitboxModel.setModelMatrix(modelTransformMatrix);
                    hitboxModel.draw(renderPipelineState,openGLState);
                }
            }
        }
    }

    /**
     * Renders an area select
     * @param openGLState The opengl state
     * @param renderPipelineState The render pipeline state
     * @param modelTransformMatrix The model transform matrix
     * @param areaSelection The area selection
     */
    private static void renderAreaSelection(OpenGLState openGLState, RenderPipelineState renderPipelineState, Matrix4d modelTransformMatrix, AreaSelection areaSelection, String texturePath){
        Model model = Globals.assetManager.fetchModel(AssetDataStrings.UNITCUBE);
        if(model != null){
            Texture texture = Globals.assetManager.fetchTexture(texturePath);
            if(texture != null){
                texture.bind(openGLState);
            }
            Vector3d dims = new Vector3d(areaSelection.getRectEnd()).sub(areaSelection.getRectStart());
            Vector3d position = new Vector3d(areaSelection.getRectStart()).add(dims.x/2.0,dims.y/2.0,dims.z/2.0);
            //calculate camera-modified vector3d
            Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
            modelTransformMatrix.identity();
            modelTransformMatrix.translate(cameraModifiedPosition);
            modelTransformMatrix.scale(dims);
            model.setModelMatrix(modelTransformMatrix);
            model.draw(renderPipelineState,openGLState);
        }
    }

    /**
     * Renders an area select
     * @param openGLState The opengl state
     * @param renderPipelineState The render pipeline state
     * @param modelTransformMatrix The model transform matrix
     * @param areaSelection The area selection
     */
    private static void renderPoint(OpenGLState openGLState, RenderPipelineState renderPipelineState, Matrix4d modelTransformMatrix, Vector3d point, double size, String texturePath){
        Model model = Globals.assetManager.fetchModel(AssetDataStrings.UNITSPHERE);
        if(model != null){
            Texture texture = Globals.assetManager.fetchTexture(texturePath);
            if(texture != null){
                texture.bind(openGLState);
            }
            //calculate camera-modified vector3d
            Vector3d cameraModifiedPosition = new Vector3d(point).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
            modelTransformMatrix.identity();
            modelTransformMatrix.translate(cameraModifiedPosition);
            modelTransformMatrix.scale(new Vector3d(size));
            model.setModelMatrix(modelTransformMatrix);
            model.draw(renderPipelineState,openGLState);
        }
    }

    /**
     * Renders an area select
     * @param openGLState The opengl state
     * @param renderPipelineState The render pipeline state
     * @param modelTransformMatrix The model transform matrix
     * @param areaSelection The area selection
     */
    private static void renderAABB(OpenGLState openGLState, RenderPipelineState renderPipelineState, Matrix4d modelTransformMatrix, AABBd aabb, String texturePath){
        DebugContentPipeline.renderAABB(openGLState, renderPipelineState, modelTransformMatrix, new Vector3d(aabb.minX,aabb.minY,aabb.minZ), new Vector3d(aabb.maxX,aabb.maxY,aabb.maxZ), texturePath);
    }

    /**
     * Renders an area select
     * @param openGLState The opengl state
     * @param renderPipelineState The render pipeline state
     * @param modelTransformMatrix The model transform matrix
     * @param areaSelection The area selection
     */
    private static void renderAABB(OpenGLState openGLState, RenderPipelineState renderPipelineState, Matrix4d modelTransformMatrix, Vector3d start, Vector3d end, String texturePath){
        Model model = Globals.assetManager.fetchModel(AssetDataStrings.UNITCUBE);
        if(model != null){
            Texture texture = Globals.assetManager.fetchTexture(texturePath);
            if(texture != null){
                texture.bind(openGLState);
            }
            //calculate camera-modified vector3d
            Vector3d cameraModifiedPosition = new Vector3d(start.x,start.y,start.z).lerp(new Vector3d(end.x,end.y,end.z),0.5).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
            modelTransformMatrix.identity();
            modelTransformMatrix.translate(cameraModifiedPosition);
            modelTransformMatrix.scale(new Vector3d(end.x - start.x,end.y - start.y,end.z - start.z));
            model.setModelMatrix(modelTransformMatrix);
            model.draw(renderPipelineState,openGLState);
        }
    }

    /**
     * Renders an area select
     * @param openGLState The opengl state
     * @param renderPipelineState The render pipeline state
     * @param modelTransformMatrix The model transform matrix
     * @param areaSelection The area selection
     */
    private static void renderTube(OpenGLState openGLState, RenderPipelineState renderPipelineState, Matrix4d modelTransformMatrix, Vector3d start, Vector3d end, double radius, String texturePath){
        Model model = Globals.assetManager.fetchModel(AssetDataStrings.UNITCYLINDER);
        if(model != null){
            Texture texture = Globals.assetManager.fetchTexture(texturePath);
            if(texture != null){
                texture.bind(openGLState);
            }
            //calculate camera-modified vector3d
            Vector3d cameraModifiedPosition = new Vector3d(end).lerp(new Vector3d(start),0.5).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
            modelTransformMatrix.identity();
            modelTransformMatrix.translate(cameraModifiedPosition);
            modelTransformMatrix.rotate(SpatialMathUtils.calculateRotationFromPointToPoint(start, end).mul(new Quaterniond().rotateZ(Math.PI / 2.0)));
            modelTransformMatrix.scale(new Vector3d(radius,end.distance(start) / 2.0,radius));
            model.setModelMatrix(modelTransformMatrix);
            model.draw(renderPipelineState,openGLState);
        }
    }

    /**
     * Sets the path points to render
     * @param pathPoints The set of points to render a path along
     */
    public void setPathPoints(List<Vector3d> pathPoints){
        this.pathPoints = pathPoints;
    }

    /**
     * Gets the bone debugging pipeline
     * @return The bone debugging pipeline
     */
    public DebugBonesPipeline getDebugBonesPipeline(){
        return this.debugBonesPipeline;
    }
    
}
