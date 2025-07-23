package electrosphere.renderer.ui.elements;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL40;

import electrosphere.client.entity.camera.CameraEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.actor.Actor;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.ui.elementtypes.DraggableElement;
import electrosphere.renderer.ui.elementtypes.ScrollableElement;
import electrosphere.renderer.ui.events.DragEvent;
import electrosphere.renderer.ui.events.DragEvent.DragEventType;
import electrosphere.renderer.ui.events.Event;
import electrosphere.renderer.ui.events.ScrollEvent;

/**
 * Draws a 3d scene inside a panel
 */
public class ActorPanel extends BufferedStandardDrawableContainerElement implements DraggableElement, ScrollableElement {

    /**
     * The default width of an actor panel
     */
    public static final int DEFAULT_WIDTH = 500;

    /**
     * The default height of an actor panel
     */
    public static final int DEFAULT_HEIGHT = 500;

    /**
     * Default distance to move the camera back from the actor
     */
    static final double DEFAULT_STANDOFF_DIST = 1.0;

    /**
     * Default amount to increment the yaw by
     */
    static final double DEFAULT_YAW_INCREMENT = 0.03;

    /**
     * Multiplier applied to drag events to scale how fast the camera rotates
     */
    static final double DRAG_MULTIPLIER = 0.5;

    /**
     * The minimum zoom
     */
    static final double MIN_ZOOM = 0.1;

    /**
     * The maximum zoom
     */
    static final double MAX_ZOOM = 10.0;

    /**
     * Multiplier applied to scroll events to scale how fast the camera zooms
     */
    static final double SCROLL_MULTIPLIER = 0.1;

    /**
     * Color to clear the background with
     */
    static Vector4f color = new Vector4f(1.0f);

    /**
     * Color to clear the panel with
     */
    Vector4d clearColor = new Vector4d(1.0);

    /**
     * The actor to draw
     */
    Actor actor;

    /**
     * The model matrix for the actor panel
     */
    Matrix4d modelMatrix = new Matrix4d();

    /**
     * The current animation to play
     */
    String currentAnim;

    /**
     * The position of the actor
     */
    Vector3f actorPosition = new Vector3f(0,0,0);

    /**
     * The rotation of the actor
     */
    Quaterniond actorRotation = new Quaterniond();

    /**
     * The scale of the actor
     */
    Vector3f actorScale = new Vector3f(1,1,1);

    /**
     * The FOV of the panel
     */
    float FOV = 100.0f;

    /**
     * The aspec ratio of the panel
     */
    float aspectRatio;
    
    /**
     * Used for calculating drawing the panel
     */
    Vector3f texPosition = new Vector3f(0,0,0);

    /**
     * Used for calculating drawing the panel
     */
    Vector3f texScale = new Vector3f(1,1,0);

    /**
     * Tracks whether this actor panel has pulled the camera back based on the bounding sphere of the model or not
     * <p>
     * This must be done during draw-phase instead of creation-phase because the model may not exist when creating
     * </p>
     */
    boolean hasOffsetFromBoundingSphere = false;

    /**
     * The yaw of the camera
     */
    double yaw = 0;

    /**
     * The pitch of the camera
     */
    double pitch = 0;

    /**
     * The radius of the camera
     */
    double cameraRadius = 1.0;

    /**
     * Fires on starting dragging the panel
     */
    DragEventCallback onDragStart;

    /**
     * Fires on dragging the panel
     */
    DragEventCallback onDrag;

    /**
     * Fires on releasing dragging thep anel
     */
    DragEventCallback onDragRelease;

    /**
     * Fires when a scrollable event occurs
     */
    ScrollEventCallback onScroll;

    /**
     * Constructor
     * @param actor The actor
     */
    private ActorPanel(Actor actor){
        super();

        this.actor = actor;
        this.setWidth(DEFAULT_WIDTH);
        this.setHeight(DEFAULT_HEIGHT);
        this.aspectRatio = (float)DEFAULT_WIDTH / (float)DEFAULT_HEIGHT;
        this.recalculateModelMatrix();
    }

    /**
     * Creates an actor panel
     * @param actor The actor to put in the panel
     * @return The actor panel
     */
    public static ActorPanel create(Actor actor){
        ActorPanel rVal = new ActorPanel(actor);
        return rVal;
    }
    
    

    @Override
    public void draw(
        RenderPipelineState renderPipelineState,
        OpenGLState openGLState,
        Framebuffer framebuffer,
        int framebufferPosX,
        int framebufferPosY
        ) {

        if(this.elementBuffer != null){
            elementBuffer.bind(openGLState);
            openGLState.glViewport(elementBuffer.getWidth(), elementBuffer.getHeight());

            RenderingEngine.setFOV(FOV);
            RenderingEngine.setAspectRatio(aspectRatio);
            Globals.renderingEngine.getStandardUniformManager().update();
            
            openGLState.glDepthTest(true);
            openGLState.glDepthFunc(GL40.GL_LESS);
            GL40.glDepthMask(true);

            GL40.glClearColor((float)clearColor.x, (float)clearColor.y, (float)clearColor.z, (float)clearColor.w);
            GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);

            
            Model actorModel = Globals.assetManager.fetchModel(actor.getBaseModelPath());
            if(currentAnim != null){
                if((!actor.getAnimationData().isPlayingAnimation() || !actor.getAnimationData().isPlayingAnimation(currentAnim)) &&
                actorModel != null &&
                actorModel.getAnimation(currentAnim) != null
                ){
                    actor.getAnimationData().playAnimation(currentAnim,3);
                    actor.getAnimationData().incrementAnimationTime(0.0001);
                }
            }
            if(!hasOffsetFromBoundingSphere && actorModel != null){
                Globals.cameraHandler.updateRadialOffset(new Vector3d(actorPosition));
                double radius = actorModel.getBoundingSphere().r;
                this.cameraRadius = radius + DEFAULT_STANDOFF_DIST;
                CameraEntityUtils.setOrbitalCameraDistance(Globals.clientState.playerCamera, (float)(this.cameraRadius));
                CameraEntityUtils.setCameraCenter(Globals.clientState.playerCamera, new Vector3d(0,(float)(this.cameraRadius - DEFAULT_STANDOFF_DIST),0));
                hasOffsetFromBoundingSphere = true;
            }

            //
            //Setup camera
            //
            yaw = yaw + DEFAULT_YAW_INCREMENT;
            this.clampYaw();
            Globals.cameraHandler.setYaw(yaw);
            Globals.cameraHandler.setPitch(pitch);
            Globals.cameraHandler.updateGlobalCamera();
            this.recalculateModelMatrix();

            //
            // Set rendering engine state
            //
            renderPipelineState.setUseMeshShader(true);
            renderPipelineState.setBufferStandardUniforms(true);
            renderPipelineState.setBufferNonStandardUniforms(true);
            renderPipelineState.setUseMaterial(true);
            renderPipelineState.setUseShadowMap(false);
            renderPipelineState.setUseBones(true);
            renderPipelineState.setUseLight(true);





            
            actor.applySpatialData(modelMatrix,new Vector3d(actorPosition));
            actor.draw(renderPipelineState,openGLState);

            RenderingEngine.setFOV(Globals.gameConfigCurrent.getSettings().getGraphicsFOV());
            RenderingEngine.setAspectRatio(Globals.WINDOW_WIDTH / (float)Globals.WINDOW_HEIGHT);
            Globals.renderingEngine.getStandardUniformManager().update();

            openGLState.glDepthTest(false);

            //this call binds the screen as the "texture" we're rendering to
            //have to call before actually rendering
            framebuffer.bind(openGLState);
            openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());
            
            float ndcX =      (float)this.absoluteToFramebuffer(getAbsoluteX(),framebufferPosX)/framebuffer.getWidth();
            float ndcY =      (float)this.absoluteToFramebuffer(getAbsoluteY(),framebufferPosY)/framebuffer.getHeight();
            float ndcWidth =  (float)getWidth()/framebuffer.getWidth();
            float ndcHeight = (float)getHeight()/framebuffer.getHeight();
            
            Vector3f boxPosition = new Vector3f(ndcX,ndcY,0);
            Vector3f boxDimensions = new Vector3f(ndcWidth,ndcHeight,0);





            
            //
            // Set rendering engine state
            //
            renderPipelineState.setUseMeshShader(true);
            renderPipelineState.setBufferStandardUniforms(false);
            renderPipelineState.setBufferNonStandardUniforms(true);
            renderPipelineState.setUseMaterial(true);
            renderPipelineState.setUseShadowMap(false);
            renderPipelineState.setUseBones(false);
            renderPipelineState.setUseLight(false);



            

            Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
            if(planeModel != null){
                planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
                planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
                planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
                planeModel.pushUniformToMesh("plane", "tDimension", texScale);
                planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", color);
                planeModel.getMeshes().get(0).setMaterial(elementMat);
                planeModel.draw(renderPipelineState,Globals.renderingEngine.getOpenGLState());
            } else {
                LoggerInterface.loggerRenderer.ERROR("Actor Panel unable to find plane model!!", new Exception());
            }
        }
    }

    
    /**
     * Sets the animation of the actor panel's actor
     * @param animation The animation
     */
    public void setAnimation(String animation){
        currentAnim = animation;
    }

    /**
     * Sets the position of the actor panel's actor
     * @param position The position
     */
    public void setPosition(Vector3f position){
        this.actorPosition.set(position);
        recalculateModelMatrix();
    }

    /**
     * Sets the rotation of the actor panel's actor
     * @param rotation The rotation
     */
    public void setRotation(Quaterniond rotation){
        this.actorRotation.set(rotation);
        recalculateModelMatrix();
    }

    /**
     * Sets the scale of the actor panel's actor
     * @param scale The scale
     */
    public void setScale(Vector3f scale){
        this.actorScale.set(scale);
        recalculateModelMatrix();
    }

    /**
     * Recalculates the model matrix
     */
    private void recalculateModelMatrix(){
        this.modelMatrix.identity();
        this.modelMatrix.translate(new Vector3d(actorPosition).sub(CameraEntityUtils.getCameraCenter(Globals.clientState.playerCamera)));
        this.modelMatrix.rotate(actorRotation);
        this.modelMatrix.scale(new Vector3d(actorScale));
        actor.applySpatialData(this.modelMatrix,new Vector3d(actorPosition));
    }

    /**
     * Sets the clear color of this panel
     * @param color The clear color
     */
    public void setClearColor(Vector4d color){
        this.clearColor.set(color);
    }

    /**
     * Clamps the yaw value
     */
    private void clampYaw(){
        while(yaw > 360){
            yaw = yaw - 360;
        }
        while(yaw < 0){
            yaw = yaw + 360;
        }
    }

    /**
     * Clamps the pitch value
     */
    private void clampPitch(){
        if(pitch >= 90){
            pitch = 89.99;
        }
        if(pitch <= -90){
            pitch = -89.99;
        }
    }

    /**
     * Handles any events applied to this actor panel
     */
    public boolean handleEvent(Event event){
        boolean propagate = true;
        if(event instanceof DragEvent){
            DragEvent dragEvent = (DragEvent)event;
            if(dragEvent.getType() == DragEventType.START){
                if(onDragStart != null){
                    if(!onDragStart.execute(dragEvent)){
                        propagate = false;
                    }
                } else {

                }
            }
            if(dragEvent.getType() == DragEventType.DRAG){
                if(onDrag != null){
                    if(!onDrag.execute(dragEvent)){
                        propagate = false;
                    }
                } else {
                    yaw = yaw + dragEvent.getDeltaX() * DRAG_MULTIPLIER;
                    this.clampYaw();
                    pitch = pitch + dragEvent.getDeltaY() * DRAG_MULTIPLIER;
                    this.clampPitch();
                    propagate = false;
                }
            }
            if(dragEvent.getType() == DragEventType.RELEASE){
                if(onDragRelease != null){
                    if(!onDragRelease.execute(dragEvent)){
                        propagate = false;
                    }
                }
            }
        }
        if(event instanceof ScrollEvent){
            ScrollEvent scrollEvent = (ScrollEvent)event;
            if(onScroll != null){
                if(!onScroll.execute(scrollEvent)){
                    propagate = false;
                }
            } else {
                this.cameraRadius = this.cameraRadius - scrollEvent.getScrollAmount() * SCROLL_MULTIPLIER;
                if(this.cameraRadius > MAX_ZOOM){
                    this.cameraRadius = MAX_ZOOM;
                }
                if(this.cameraRadius < MIN_ZOOM){
                    this.cameraRadius = MIN_ZOOM;
                }
                CameraEntityUtils.setOrbitalCameraDistance(Globals.clientState.playerCamera, (float)cameraRadius);
                propagate = false;
            }
        }
        return propagate;
    }

    @Override
    public void setOnDragStart(DragEventCallback callback) {
        onDragStart = callback;
    }

    @Override
    public void setOnDrag(DragEventCallback callback) {
        onDrag = callback;
    }

    @Override
    public void setOnDragRelease(DragEventCallback callback) {
        onDragRelease = callback;
    }

    @Override
    public void setOnScrollCallback(ScrollEventCallback callback) {
        onScroll = callback;
    }
    


}
