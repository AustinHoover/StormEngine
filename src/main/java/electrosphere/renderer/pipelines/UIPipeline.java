package electrosphere.renderer.pipelines;

import org.lwjgl.opengl.GL40;

import electrosphere.client.ui.menu.WindowStrings;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.RenderingEngine;
import electrosphere.renderer.debug.DebugRendering;
import electrosphere.renderer.texture.Texture;
import electrosphere.renderer.ui.UIUtils;
import electrosphere.renderer.ui.elements.Window;
import electrosphere.renderer.ui.elementtypes.DrawableElement;
import electrosphere.renderer.ui.elementtypes.Element;

/**
 * Main ui rendering pipeline
 */
public class UIPipeline implements RenderPipeline {

    @Override
    public void render(OpenGLState openGLState, RenderPipelineState renderPipelineState) {
        Globals.profiler.beginCpuSample("UIPipeline.render");

        //
        //Black background
        //
        if(Globals.renderingEngine.RENDER_FLAG_RENDER_BLACK_BACKGROUND){
            openGLState.setActiveShader(renderPipelineState, RenderingEngine.screenTextureShaders);
            openGLState.glDepthTest(false);
            openGLState.glBindVertexArray(RenderingEngine.screenTextureVAO);
            Texture blackTexture = Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_BLACK);
            if(blackTexture != null){
                blackTexture.bind(openGLState);
            }
            GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, 6);
            openGLState.glBindVertexArray(0);
        }


        //
        //White background
        //
        if(Globals.renderingEngine.RENDER_FLAG_RENDER_WHITE_BACKGROUND){
            openGLState.setActiveShader(renderPipelineState, RenderingEngine.screenTextureShaders);
            openGLState.glDepthTest(false);
            openGLState.glBindVertexArray(RenderingEngine.screenTextureVAO);
            Texture blackTexture = Globals.assetManager.fetchTexture(AssetDataStrings.TEXTURE_OFF_WHITE);
            if(blackTexture != null){
                blackTexture.bind(openGLState);
            }
            GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, 6);
            openGLState.glBindVertexArray(0);
        }


        //
        // Set render pipeline state
        //
        if(Globals.renderingEngine.RENDER_FLAG_RENDER_UI){
            renderPipelineState.setUseMeshShader(true);
            renderPipelineState.setBufferStandardUniforms(false);
            renderPipelineState.setBufferNonStandardUniforms(true);
            renderPipelineState.setUseMaterial(true);
            renderPipelineState.setUseShadowMap(false);
            renderPipelineState.setUseBones(false);
            renderPipelineState.setUseLight(false);

            //set opengl state
            openGLState.glDepthTest(false);
            openGLState.glBlend(true);

            for(Element currentElement : Globals.elementService.getWindowList()){
                if(currentElement instanceof DrawableElement){
                    DrawableElement drawable = (DrawableElement) currentElement;
                    if(drawable.getVisible()){
                        drawable.draw(renderPipelineState, openGLState, Globals.renderingEngine.defaultFramebuffer, 0, 0);
                    }
                }
            }

            if(Globals.elementService.getWindow(WindowStrings.TOOLTIP_WINDOW) != null){
                Window tooltipWindow = (Window)Globals.elementService.getWindow(WindowStrings.TOOLTIP_WINDOW);
                tooltipWindow.draw(renderPipelineState, openGLState, Globals.renderingEngine.defaultFramebuffer, 0, 0);
            }
        }

        if(DebugRendering.RENDER_DEBUG_UI_TREE){
            UIUtils.renderOutlineTree(openGLState, renderPipelineState, Globals.elementService.getWindowList().get(0));
        }
        
        Globals.profiler.endCpuSample();
    }
    
}
