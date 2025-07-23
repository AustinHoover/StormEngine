package electrosphere.renderer.pipelines;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL40;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.client.firstPerson.FirstPersonTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.actor.Actor;

/**
 * Renders content that should only be rendered in first person (ie the view model/hands/whatever)
 */
public class FirstPersonItemsPipeline implements RenderPipeline {

    //internal model matrix
    Matrix4d modelTransformMatrix = new Matrix4d();

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {

        if(Globals.clientState.firstPersonEntity != null && !Globals.controlHandler.cameraIsThirdPerson()){
            //update logic
            if(Globals.cameraHandler.getTrackPlayerEntity()){
                updateFirstPersonModelPosition(Globals.clientState.firstPersonEntity);
            }

            //setup opengl state
            renderPipelineState.setUseBones(true);
            renderPipelineState.setUseLight(true);
            renderPipelineState.setUseMaterial(true);
            renderPipelineState.setUseMeshShader(true);
            renderPipelineState.setUseShadowMap(false);
            renderPipelineState.setBufferStandardUniforms(true);
            renderPipelineState.setFrustumCheck(false);

            openGLState.glDepthTest(true);
            openGLState.glDepthFunc(GL40.GL_LESS);
            GL40.glDepthMask(true);

            //render

            //
            //Draw viewmodel
            {
                Vector3d position = EntityUtils.getPosition(Globals.clientState.firstPersonEntity);
                Actor actor = EntityUtils.getActor(Globals.clientState.firstPersonEntity);
                //calculate camera-modified vector3d
                Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                //calculate and apply model transform
                modelTransformMatrix.identity();
                modelTransformMatrix.translate(cameraModifiedPosition);
                modelTransformMatrix.rotate(EntityUtils.getRotation(Globals.clientState.firstPersonEntity));
                modelTransformMatrix.scale(new Vector3d(EntityUtils.getScale(Globals.clientState.firstPersonEntity)));
                actor.applySpatialData(modelTransformMatrix,position);
                //draw
                actor.draw(renderPipelineState, openGLState);
            }

            //draw children of viewmodel
            if(AttachUtils.hasChildren(Globals.clientState.firstPersonEntity)){
                for(Entity child : AttachUtils.getChildrenList(Globals.clientState.firstPersonEntity)){
                    Vector3d position = EntityUtils.getPosition(child);
                    Actor actor = EntityUtils.getActor(child);
                    //calculate camera-modified vector3d
                    Vector3d cameraModifiedPosition = new Vector3d(position).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera));
                    //calculate and apply model transform
                    modelTransformMatrix.identity();
                    modelTransformMatrix.translate(cameraModifiedPosition);
                    modelTransformMatrix.rotate(new Quaterniond(EntityUtils.getRotation(child)));
                    modelTransformMatrix.scale(new Vector3d(EntityUtils.getScale(child)));
                    actor.applySpatialData(modelTransformMatrix,position);
                    //draw
                    actor.draw(renderPipelineState, openGLState);
                }
            }


        }

    }

    /**
     * Updates the position and rotation of the first person model
     */
    private void updateFirstPersonModelPosition(Entity target){

        FirstPersonTree tree = FirstPersonTree.getTree(target);

        Matrix4d rotationMat = CameraEntityUtils.getRotationMat(Globals.clientState.playerCamera);
        EntityUtils.getRotation(Globals.clientState.firstPersonEntity).set(CameraEntityUtils.getRotationQuat(Globals.clientState.playerCamera));

        Vector3d playerPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
        Vector4d behindCameraOffsetRaw = rotationMat.transform(new Vector4d(0,tree.getCameraViewDirOffsetY(),tree.getCameraViewDirOffsetZ(),1)); //pushes the model behind the camera
        Vector3d behindCameraOffset = new Vector3d(behindCameraOffsetRaw.x,behindCameraOffsetRaw.y,behindCameraOffsetRaw.z);
        EntityUtils.setPosition(Globals.clientState.firstPersonEntity, new Vector3d(playerPos).add(0.0f,tree.getHeightFromOrigin(),0.0f).add(behindCameraOffset));

        if(ClientEquipState.hasEquipState(Globals.clientState.playerEntity)){
            ClientEquipState clientEquipState = ClientEquipState.getClientEquipState(Globals.clientState.playerEntity);
            clientEquipState.evaluatePlayerAttachments();
        }
    }
    
}
