package electrosphere.renderer.ui.frame;

import org.joml.Vector3f;
import org.joml.Vector4f;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.framebuffer.Framebuffer;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.texture.Texture;

/**
 * Utilities to draw frames behind elements
 */
public class UIFrameUtils {

    /**
     * The material used for drawing the texture for the frame
     */
    private static final Material customMat = new Material();

    /**
     * Stores the position of the box to draw
     */
    private static final Vector3f boxPosition = new Vector3f();

    /**
     * Stores the dimensions of the box to draw
     */
    private static final Vector3f boxDimensions = new Vector3f();

    /**
     * Stores the position of the texture to draw on the box
     */
    private static final Vector3f texPosition = new Vector3f();

    /**
     * Stores the dimensions of the texture to draw on the box
     */
    private static final Vector3f texScale = new Vector3f();

    /**
     * Draws a frame
     * @param openGLState The opengl state
     * @param frame The texture of the frame to draw
     * @param color The color to draw
     * @param frameCornerDim The dimensions of a corner of the frame texture
     * @param posX The absolute x position to draw the frame
     * @param posY The absolute y position to draw the frame
     * @param width The width of the frame
     * @param height The height of the frame
     * @param framebuffer The framebuffer to draw to
     * @param framebufferPosX The x position of the framebuffer
     * @param framebufferPosY The y position of the framebuffer
     */
    public static void drawFrame(
        OpenGLState openGLState,
        String frame, Vector4f color, int frameTexDim, int frameCornerDim,
        int posX, int posY, int width, int height,
        Framebuffer framebuffer, int framebufferPosX, int framebufferPosY
    ){
        framebuffer.bind(openGLState);
        openGLState.glViewport(framebuffer.getWidth(), framebuffer.getHeight());

        Model planeModel = Globals.assetManager.fetchModel(AssetDataStrings.MODEL_IMAGE_PLANE);
        Texture windowFrame = Globals.assetManager.fetchTexture(frame);
        //render background of window
        if(planeModel != null && windowFrame != null){
            //set materials + uniforms
            customMat.setDiffuse(windowFrame);
            planeModel.getMeshes().get(0).setMaterial(customMat);
            planeModel.pushUniformToMesh(planeModel.getMeshes().get(0).getMeshName(), "color", color);


            //top left corner
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer(posX,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer(posY,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)frameCornerDim/framebuffer.getWidth(),
                (float)frameCornerDim/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                0,
                ((frameTexDim - frameCornerDim) / (float)frameTexDim),
                0
            );
            texScale.set(
                (frameCornerDim / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //top center side
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer(posX + frameCornerDim,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer(posY,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)(width - (frameCornerDim * 2))/framebuffer.getWidth(),
                (float)frameCornerDim/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                (frameCornerDim / (float)frameTexDim),
                ((frameTexDim - frameCornerDim) / (float)frameTexDim),
                0
            );
            texScale.set(
                ((frameTexDim - (frameCornerDim * 2)) / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //top right corner
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer((posX + width) - frameCornerDim,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer(posY,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)frameCornerDim/framebuffer.getWidth(),
                (float)frameCornerDim/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                ((frameTexDim - frameCornerDim) / (float)frameTexDim),
                ((frameTexDim - frameCornerDim) / (float)frameTexDim),
                0
            );
            texScale.set(
                (frameCornerDim / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //center left side
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer(posX,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer(posY + frameCornerDim,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)frameCornerDim/framebuffer.getWidth(),
                (float)(height - (frameCornerDim * 2))/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                0,
                (frameCornerDim / (float)frameTexDim),
                0
            );
            texScale.set(
                (frameCornerDim / (float)frameTexDim),
                ((frameTexDim - (frameCornerDim * 2)) / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //center point
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer(posX + frameCornerDim,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer(posY + frameCornerDim,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)(width - (frameCornerDim * 2))/framebuffer.getWidth(),
                (float)(height - (frameCornerDim * 2))/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                (frameCornerDim / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            texScale.set(
                ((frameTexDim - (frameCornerDim * 2)) / (float)frameTexDim),
                ((frameTexDim - (frameCornerDim * 2)) / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //center right side
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer((posX + width) - frameCornerDim,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer(posY + frameCornerDim,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)frameCornerDim/framebuffer.getWidth(),
                (float)(height - (frameCornerDim * 2))/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                ((frameTexDim - frameCornerDim) / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            texScale.set(
                (frameCornerDim / (float)frameTexDim),
                ((frameTexDim - (frameCornerDim * 2)) / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //bottom left corner
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer(posX,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer((posY + height) - frameCornerDim,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)frameCornerDim/framebuffer.getWidth(),
                (float)frameCornerDim/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                0,
                0,
                0
            );
            texScale.set(
                (frameCornerDim / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //bottom center side
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer(posX + frameCornerDim,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer((posY + height) - frameCornerDim,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)(width - (frameCornerDim * 2))/framebuffer.getWidth(),
                (float)frameCornerDim/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                (frameCornerDim / (float)frameTexDim),
                0,
                0
            );
            texScale.set(
                ((frameTexDim - (frameCornerDim * 2)) / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);

            //bottom right corner
            boxPosition.set(
                (float)UIFrameUtils.absoluteToFramebuffer((posX + width) - frameCornerDim,framebufferPosX)/framebuffer.getWidth(),
                (float)UIFrameUtils.absoluteToFramebuffer((posY + height) - frameCornerDim,framebufferPosY)/framebuffer.getHeight(),
                0
            );
            boxDimensions.set(
                (float)frameCornerDim/framebuffer.getWidth(),
                (float)frameCornerDim/framebuffer.getHeight(),
                0
            );
            texPosition.set(
                ((frameTexDim - frameCornerDim) / (float)frameTexDim),
                0,
                0
            );
            texScale.set(
                (frameCornerDim / (float)frameTexDim),
                (frameCornerDim / (float)frameTexDim),
                0
            );
            UIFrameUtils.drawBox(planeModel);
        }
    }

    /**
     * Draws a box for the frame
     * @param planeModel The model to use
     */
    private static void drawBox(Model planeModel){
        planeModel.pushUniformToMesh("plane", "mPosition", boxPosition);
        planeModel.pushUniformToMesh("plane", "mDimension", boxDimensions);
        planeModel.pushUniformToMesh("plane", "tPosition", texPosition);
        planeModel.pushUniformToMesh("plane", "tDimension", texScale);
        planeModel.drawUI();
    }

    /**
     * Converts an absolute (to the screen) position to a position within a framebuffer
     * @param absolutePos The absolute position
     * @param framebufferPos The position of the framebuffer on the screen
     * @return The position within the framebuffer
     */
    public static int absoluteToFramebuffer(int absolutePos, int framebufferPos){
        return absolutePos - framebufferPos;
    }
    
}
