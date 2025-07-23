package electrosphere.renderer.pipelines;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL40;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.RenderPipelineState.SelectedShaderEnum;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.actor.instance.InstancedActor;
import electrosphere.renderer.buffer.ShaderAttribute;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.target.DrawTargetAccumulator;
import electrosphere.renderer.target.DrawTargetAccumulator.ModelAccumulatorData;

/**
 * The main render pipeline with OIT pass
 */
public class MainContentPipeline implements RenderPipeline {

    //First person drawing routine
    FirstPersonItemsPipeline firstPersonSubPipeline;

    /**
     * The draw target accumulator
     */
    private DrawTargetAccumulator drawTargetAccumulator = new DrawTargetAccumulator();

    /**
     * The queue for non-static entities to draw
     */
    private List<Entity> standardDrawCall = new LinkedList<Entity>();

    /**
     * Number of terrain chunks rendered
     */
    private int terrainChunks = 0;

    /**
     * Set for storing entities of a specific tag
     */
    private HashSet<Entity> entityTagSet = new HashSet<Entity>();

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("MainContentPipeline.render");
        this.clearTrackingData();
        
        Matrix4d modelTransformMatrix = new Matrix4d();
        
        //bind screen fbo
        RenderingEngine.screenFramebuffer.bind(openGLState);
        openGLState.glDepthTest(true);
        openGLState.glDepthFunc(GL40.GL_LESS);
        GL40.glDepthMask(true);
        openGLState.glViewport(Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
        
        ///
        ///     R E N D E R I N G      S T U F F
        ///
        //Sets the background color.
        GL40.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);


        //
        // Set render pipeline state
        //
        renderPipelineState.setSelectedShader(SelectedShaderEnum.PRIMARY);
        renderPipelineState.setUseMeshShader(true);
        renderPipelineState.setBufferStandardUniforms(true);
        renderPipelineState.setBufferNonStandardUniforms(true); //true so that pre-programmed (in data) uniforms are pushed to gpu when rendering each mesh
        renderPipelineState.setUseMaterial(true);
        renderPipelineState.setUseShadowMap(true);
        renderPipelineState.setUseBones(true);
        renderPipelineState.setUseLight(true);


        //
        //     Pass One: Solids
        //

        //
        //     D R A W     A L L     E N T I T I E S
        //
        Globals.profiler.beginCpuSample("MainContentPipeline.render - Solids non-instanced");
        Vector3d positionVec = new Vector3d();
        Vector3d scaleVec = new Vector3d();
        Vector3d cameraCenterVec = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);

        //
        //Draw terrain (guarantees partial transparencies have terrain behind them at least)
        renderPipelineState.setUseBones(false);
        for(Entity currentEntity : this.drawTargetAccumulator.getTerrainEntities()){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(
                currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)!=null
            ){
                //fetch actor
                Actor currentActor = EntityUtils.getActor(currentEntity);
                //calculate camera-modified vector3d
                Vector3d cameraCenter = scaleVec.set(cameraCenterVec);
                Vector3d cameraModifiedPosition = positionVec.set(position).sub(cameraCenter);
                //calculate and apply model transform
                modelTransformMatrix = modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                modelTransformMatrix.scale(scaleVec.set(EntityUtils.getScale(currentEntity)));
                currentActor.applySpatialData(modelTransformMatrix,position);
                //draw
                currentActor.draw(renderPipelineState,openGLState);

                //tracking
                this.terrainChunks++;
            }
        }
        for(Entity currentEntity : this.drawTargetAccumulator.getBlockEntities()){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(
                currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)!=null
            ){
                //fetch actor
                Actor currentActor = EntityUtils.getActor(currentEntity);
                //calculate camera-modified vector3d
                Vector3d cameraCenter = scaleVec.set(cameraCenterVec);
                Vector3d cameraModifiedPosition = positionVec.set(position).sub(cameraCenter);
                //calculate and apply model transform
                modelTransformMatrix = modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                modelTransformMatrix.scale(scaleVec.set(EntityUtils.getScale(currentEntity)));
                currentActor.applySpatialData(modelTransformMatrix,position);
                //draw
                currentActor.draw(renderPipelineState,openGLState);

                //tracking
                this.terrainChunks++;
            }
        }
        renderPipelineState.setUseBones(true);

        //
        //draw main models
        for(Entity currentEntity : this.standardDrawCall){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(
                currentEntity.getData(EntityDataStrings.DATA_STRING_DRAW)!=null
            ){
                //fetch actor
                Actor currentActor = EntityUtils.getActor(currentEntity);
                //calculate camera-modified vector3d
                Vector3d cameraCenter = scaleVec.set(cameraCenterVec);
                Vector3d cameraModifiedPosition = positionVec.set(position).sub(cameraCenter);
                //calculate and apply model transform
                modelTransformMatrix = modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                modelTransformMatrix.scale(scaleVec.set(EntityUtils.getScale(currentEntity)));
                currentActor.applySpatialData(modelTransformMatrix,position);
                //draw
                currentActor.draw(renderPipelineState,openGLState);

                //tracking
                if(currentEntity.containsKey(EntityDataStrings.TERRAIN_IS_TERRAIN)){
                    this.terrainChunks++;
                }
            }
        }

        //
        //draw low-LOD non-terrain models
        renderPipelineState.setUseBones(false);
        for(ModelAccumulatorData accumulator : this.drawTargetAccumulator.getCalls()){
            Model model = Globals.assetManager.fetchModel(accumulator.getModelPath());
            if(model != null){
                int count = accumulator.getCount();
                List<Matrix4d> transforms = accumulator.getTransforms();
                List<Vector3d> positions = accumulator.getPositions();
                model.setMeshMask(null);
                for(int meshIndex = 0; meshIndex < model.getMeshCount(); meshIndex++){
                    for(int i = 0; i < count; i++){
                        Vector3d position = positions.get(i);
                        Matrix4d transform = transforms.get(i);
                        model.setWorldPos(position);
                        model.setModelMatrix(transform);
                        model.drawMesh(renderPipelineState, openGLState, meshIndex);
                    }
                }
            }
        }
        renderPipelineState.setUseBones(true);
        Globals.profiler.endCpuSample();
        Globals.profiler.beginCpuSample("MainContentPipeline.render - Solids Foliage");
        Globals.renderingEngine.getFoliagePipeline().render(openGLState, renderPipelineState);
        Globals.profiler.endCpuSample();
        Globals.profiler.beginCpuSample("MainContentPipeline.render - Solids instanced");
        Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_INSTANCED, entityTagSet);
        for(Entity currentEntity : entityTagSet){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(MainContentPipeline.shouldDrawSolidPass(currentEntity)){
                //fetch actor
                InstancedActor currentActor = InstancedActor.getInstancedActor(currentEntity);
                //if the shader attribute for model matrix exists, calculate the model matrix and apply
                if(InstancedActor.getInstanceModelAttribute(currentEntity) != null){
                    ShaderAttribute modelAttribute = InstancedActor.getInstanceModelAttribute(currentEntity);
                    //calculate model matrix
                    Vector3d cameraModifiedPosition = positionVec.set(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    Quaterniond rotation = EntityUtils.getRotation(currentEntity);
                    // modelTransformMatrix.identity();
                    modelTransformMatrix.identity().translationRotateScale(
                        cameraModifiedPosition,
                        rotation,
                        scaleVec.set(EntityUtils.getScale(currentEntity))
                    );
                    //set actor value
                    currentActor.setAttribute(modelAttribute, new Matrix4d(modelTransformMatrix));
                    //draw
                    currentActor.draw(renderPipelineState, new Vector3d(cameraModifiedPosition));
                } else {
                    currentActor.draw(renderPipelineState);
                }
            }
        }
        //draw all instanced models
        Globals.clientInstanceManager.draw(renderPipelineState, openGLState);
        Globals.profiler.endCpuSample();
        this.firstPersonSubPipeline.render(openGLState, renderPipelineState);

        //
        //      Pass Two: Transparency Accumulator + Revealage
        //
        // glDisable(GL_DEPTH_TEST);
        GL40.glDepthMask(false);
        openGLState.glBlend(true);
        openGLState.glBlendFunci(0, GL40.GL_ONE, GL40.GL_ONE);
        openGLState.glBlendFunci(1, GL40.GL_ZERO, GL40.GL_ONE_MINUS_SRC_COLOR);
        GL40.glBlendEquation(GL40.GL_FUNC_ADD);

        RenderingEngine.transparencyBuffer.bind(openGLState);
        GL40.glClearBufferfv(GL40.GL_COLOR,0,RenderingEngine.transparencyAccumulatorClear);
        GL40.glClearBufferfv(GL40.GL_COLOR,1,RenderingEngine.transparencyRevealageClear);

        //
        // Set render pipeline state
        //
        renderPipelineState.setUseMeshShader(true);
        renderPipelineState.setSelectedShader(SelectedShaderEnum.OIT);
        openGLState.glDepthFunc(GL40.GL_LEQUAL);

        //
        //!!!WARNING!!!
        //Comments on function:
        //If you're going "gee wilikers I don't know why the back planes of my transparent-labeled aren't showing through the transparency", this is for you
        //The transparent pass receives the depth buffer of the opaque pass and IS DEPTH MASK CULLED
        //This means if you draw the transparent object in the depth pass, it will not draw in the transparent pass as it is culled
        //
        //!!!WARNING!!!
        //TLDR OF ABOVE: DO NOT DRAW TRANSPARENT OBJECTS IN OPAQUE PASS
        //

        Globals.profiler.beginCpuSample("MainContentPipeline.render - Transparents non-instanced");
        Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAWABLE, entityTagSet);
        for(Entity currentEntity : entityTagSet){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(MainContentPipeline.shouldDrawTransparentPass(currentEntity)){
                //fetch actor
                Actor currentActor = EntityUtils.getActor(currentEntity);
                //calculate camera-modified vector3d
                Vector3d cameraModifiedPosition = positionVec.set(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                //calculate and apply model transform
                modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(EntityUtils.getRotation(currentEntity));
                modelTransformMatrix.scale(scaleVec.set(EntityUtils.getScale(currentEntity)));
                currentActor.applySpatialData(modelTransformMatrix,position);
                //draw
                currentActor.draw(renderPipelineState,openGLState);
            }
        }
        Globals.profiler.endCpuSample();
        Globals.profiler.beginCpuSample("MainContentPipeline.render - Transparents instanced");
        Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_INSTANCED, entityTagSet);
        for(Entity currentEntity : entityTagSet){
            Vector3d position = EntityUtils.getPosition(currentEntity);
            if(MainContentPipeline.shouldDrawTransparentPass(currentEntity)){
                //fetch actor
                InstancedActor currentActor = InstancedActor.getInstancedActor(currentEntity);
                //if the shader attribute for model matrix exists, calculate the model matrix and apply
                if(InstancedActor.getInstanceModelAttribute(currentEntity) != null){
                    ShaderAttribute modelAttribute = InstancedActor.getInstanceModelAttribute(currentEntity);
                    //calculate model matrix
                    Vector3d cameraModifiedPosition = positionVec.set(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    Quaterniond rotation = EntityUtils.getRotation(currentEntity);
                    // modelTransformMatrix.identity();
                    modelTransformMatrix.identity().translationRotateScale(
                        cameraModifiedPosition,
                        rotation,
                        scaleVec.set(EntityUtils.getScale(currentEntity))
                    );
                    //set actor value
                    currentActor.setAttribute(modelAttribute, new Matrix4d(modelTransformMatrix));
                    //draw
                    currentActor.draw(renderPipelineState, new Vector3d(cameraModifiedPosition));
                } else {
                    currentActor.draw(renderPipelineState);
                }
            }
        }
        //draw all instanced models
        Globals.clientInstanceManager.draw(renderPipelineState,openGLState);
        Globals.profiler.endCpuSample();


        //
        // Set render pipeline state
        //
        renderPipelineState.setSelectedShader(SelectedShaderEnum.PRIMARY);


        //
        // Reset State
        //
        Globals.renderingEngine.defaultFramebuffer.bind(openGLState);

        
        
//        openGLState.glBindVertexArray(0);

        Globals.profiler.endCpuSample();
    }

    /**
     * Checks if the entity should be drawn
     * @param entity The entity
     * @return true if should draw, false otherwise
     */
    public static boolean shouldDrawSolidPass(Entity entity){
        return
        (
            (boolean)entity.getData(EntityDataStrings.DATA_STRING_DRAW) && 
            entity.containsKey(EntityDataStrings.DRAW_SOLID_PASS)
        ) &&
        (
            !MainContentPipeline.entityBlacklist(entity)
        )
        ;
    }

    /**
     * Checks if the entity should be drawn
     * @param entity The entity
     * @return true if should draw, false otherwise
     */
    static boolean shouldDrawTransparentPass(Entity entity){
        return
        (
            (boolean)entity.getData(EntityDataStrings.DATA_STRING_DRAW) && 
            entity.getData(EntityDataStrings.DRAW_TRANSPARENT_PASS) != null
        ) &&
        (
            !MainContentPipeline.entityBlacklist(entity)
        )
        ;
    }

    /**
     * Checks whether the entity is on the blacklist for drawing in main pipeline or not
     * @param entity The entity
     * @return True if in blacklist, false otherwise
     */
    static boolean entityBlacklist(Entity entity){
        return 
        //don't draw first person view in this pipeline ever
        entity == Globals.clientState.firstPersonEntity ||

        //don't draw third person view if camera is first person
        (
            entity == Globals.clientState.playerEntity &&
            !Globals.controlHandler.cameraIsThirdPerson()
        ) ||
        (
            !Globals.controlHandler.cameraIsThirdPerson() &&
            AttachUtils.getParent(entity) != null &&
            AttachUtils.getParent(entity) == Globals.clientState.playerEntity
        ) ||

        //don't draw items if they're attached to viewmodel
        (
            Globals.clientState.firstPersonEntity != null &&
            !Globals.controlHandler.cameraIsThirdPerson() &&
            AttachUtils.hasParent(entity) &&
            AttachUtils.getParent(entity) == Globals.clientState.firstPersonEntity
        )
        ;
    }

    /**
     * Clears the tracking data
     */
    private void clearTrackingData(){
        this.terrainChunks = 0;
    }

    /**
     * Get the first person pipeline
     * @param firstPersonItemsPipeline the first person pipeline
     */
    public void setFirstPersonPipeline(FirstPersonItemsPipeline firstPersonItemsPipeline){
        this.firstPersonSubPipeline = firstPersonItemsPipeline;
    }

    /**
     * Gets the draw target accumulator
     * @return The draw target accumulator
     */
    public DrawTargetAccumulator getDrawTargetAccumulator(){
        return drawTargetAccumulator;
    }

    /**
     * Gets the queue of standard entities to draw
     * @return The queue of standard entites
     */
    public List<Entity> getStandardEntityQueue(){
        return standardDrawCall;
    }

    /**
     * Gets the tracking info
     * @return The tracking info
     */
    public String getTrackingInfo(){
        String message = "" +
        "Terrain entities:" + this.terrainChunks + "\n" +
        "";
        for(DrawTargetAccumulator.ModelAccumulatorData data : this.drawTargetAccumulator.getCalls()){
            message = message + data.getModelPath() + ": " + data.getCount() + "\n";
        }
        return message;
    }
    
}
