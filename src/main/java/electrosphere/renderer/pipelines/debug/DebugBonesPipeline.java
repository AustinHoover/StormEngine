package electrosphere.renderer.pipelines.debug;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL40;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.model.Bone;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.pipelines.RenderPipeline;
import electrosphere.util.math.MathBones;

/**
 * Renders the bones for a given mesh
 */
public class DebugBonesPipeline implements RenderPipeline {

    //The scale vector
    static final Vector3d scale = new Vector3d(1);

    /**
     * The entity to render bones for
     */
    Entity targetEntity;

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("DebugBonesPipeline.render");

        if(targetEntity != null){
            //bind screen fbo
            RenderingEngine.screenFramebuffer.bind(openGLState);
            openGLState.glDepthTest(true);
            openGLState.glDepthFunc(GL40.GL_LESS);
            GL40.glDepthMask(true);
            openGLState.glBlend(false);
            openGLState.glViewport(Globals.gameConfigCurrent.getSettings().getRenderResolutionX(), Globals.gameConfigCurrent.getSettings().getRenderResolutionY());
            
            ///
            ///     R E N D E R I N G      S T U F F
            ///
            //Sets the background color.


            //
            // Set render pipeline state
            //
            renderPipelineState.setUseMeshShader(false);
            renderPipelineState.setBufferStandardUniforms(true);
            renderPipelineState.setBufferNonStandardUniforms(false);
            renderPipelineState.setUseMaterial(true);
            renderPipelineState.setUseShadowMap(true);
            renderPipelineState.setUseBones(true);
            renderPipelineState.setUseLight(true);

            Matrix4d modelTransformMatrix = new Matrix4d();

            //
            //Get target data
            //
            Actor targetActor = EntityUtils.getActor(targetEntity);
            Model boneModel = Globals.assetManager.fetchModel(AssetDataStrings.UNITCYLINDER);
            boneModel.getMaterials().get(0).setDiffuse(Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_DEFAULT));
            for(Bone bone : targetActor.getAnimationData().getBoneValues()){
                Vector3d bonePos = MathBones.getBoneWorldPosition(targetEntity, bone.boneID);
                Quaterniond boneRot = MathBones.getBoneWorldRotation(targetEntity, bone.boneID);

                //put pos + rot into model
                Vector3d cameraModifiedPosition = new Vector3d(bonePos).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(boneRot);
                modelTransformMatrix.scale(scale);
                boneModel.setModelMatrix(modelTransformMatrix);

                //draw
                boneModel.draw(renderPipelineState,openGLState);

            }
        }

        Globals.profiler.endCpuSample();
    }

    /**
     * Sets the entity that should be drawn in this pipeline
     * @param entity The entity to draw bones for
     */
    public void setEntity(Entity entity){
        this.targetEntity = entity;
    }
    
}
