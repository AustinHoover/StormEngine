package electrosphere.renderer.pipelines;

import java.util.HashSet;
import java.util.Set;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Sphered;
import org.joml.Vector3d;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.actor.instance.TextureInstancedActor;

/**
 * Pipeline for rendering foliage
 */
public class FoliagePipeline implements RenderPipeline {

    /**
     * Template bounding shere used for checking frustum for this cell
     */
    static Sphered boundingSphere = new Sphered(0.5,0.5,0.5,8);

    /**
     * Set for storing entities of a specific tag
     */
    private HashSet<Entity> entityTagSet = new HashSet<Entity>();

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Set<Entity> foliageEntities = Globals.clientState.clientScene.getEntitiesWithTag(EntityTags.DRAW_FOLIAGE_PASS, entityTagSet);
        if(foliageEntities != null){
            for(Entity foliageEntity : foliageEntities){
                Matrix4d modelMatrix = new Matrix4d();
                Vector3d cameraCenter = CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera);
                Vector3d realPosition = EntityUtils.getPosition(foliageEntity);

                Vector3d cameraModifiedPosition = new Vector3d(realPosition).sub(cameraCenter);
                //frustum check entire cell
                boolean shouldRender = renderPipelineState.getFrustumIntersection().testSphere((float)(cameraModifiedPosition.x + boundingSphere.x), (float)(cameraModifiedPosition.y + boundingSphere.y), (float)(cameraModifiedPosition.z + boundingSphere.z), (float)(boundingSphere.r));
                if(shouldRender){
                    //disable frustum check and instead perform at cell level
                    boolean currentFrustumCheckState = renderPipelineState.shouldFrustumCheck();
                    renderPipelineState.setFrustumCheck(false);
                    Vector3d grassPosition = EntityUtils.getPosition(foliageEntity);
                    Quaterniond grassRotation = EntityUtils.getRotation(foliageEntity);
                    TextureInstancedActor actor = TextureInstancedActor.getTextureInstancedActor(foliageEntity);

                    
                    modelMatrix = modelMatrix.identity();
                    modelMatrix.translate(cameraModifiedPosition);
                    modelMatrix.rotate(new Quaterniond(grassRotation));
                    modelMatrix.scale(new Vector3d(EntityUtils.getScale(foliageEntity)));
                    actor.applySpatialData(modelMatrix,grassPosition);


                    //draw
                    actor.draw(renderPipelineState, openGLState);
                    renderPipelineState.setFrustumCheck(currentFrustumCheckState);
                }
            }
        }
    }
    
}
