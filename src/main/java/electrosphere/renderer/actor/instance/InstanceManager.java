package electrosphere.renderer.actor.instance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;
import electrosphere.renderer.buffer.ShaderAttribute;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.VisualShader;

/**
 * Manages all instanced actors. This is what actually does the draw call in opengl.
 * The instancedactor class is effectively a convenient interface for working with this manager.
 */
public class InstanceManager {
    
    //A list of all models currently being drawn each loop with this instance manager
    List<String> modelsToDraw = new LinkedList<String>();
    //The map of model path to instance data. Instance data contains all the important information for drawing all instanced of a given model.
    Map<String,InstanceData> pathToInstanceData = new HashMap<String,InstanceData>();

    /**
     * Adds an instanced actor to the list of actors to be priority sorted into drawn and not drawn
     * @param actor The instanced actor
     */
    protected void addToQueue(InstancedActor actor){
        InstanceData data = pathToInstanceData.get(actor.getModelPath());
        data.addInstance(actor);
    }

    /**
     * Creates an instanced actor of a model at a given model path
     * @param modelPath The path for the model
     * @return The instanced actor
     */
    public InstancedActor createInstancedActor(
            String modelPath,
            String vertexShaderPath,
            String fragmentShaderPath,
            Map<ShaderAttribute,HomogenousBufferTypes> attributeTypes,
            int capacity
        ){
        InstancedActor rVal = new InstancedActor(modelPath);
        if(!pathToInstanceData.containsKey(modelPath)){
            //create instance data
            HomogenousInstanceData instanceData = new HomogenousInstanceData(capacity,vertexShaderPath,fragmentShaderPath);
            //queue shader
            Globals.assetManager.addShaderToQueue(vertexShaderPath, fragmentShaderPath);
            //if asset manager doesn't have model, queue model
            if(Globals.assetManager.fetchModel(modelPath) == null){
                Globals.assetManager.addModelPathToQueue(modelPath);
            }
            //set attributes
            for(ShaderAttribute attribute : attributeTypes.keySet()){
                instanceData.addDataType(attribute, attributeTypes.get(attribute));
            }
            //register to internal-to-manager datastructures
            pathToInstanceData.put(modelPath,instanceData);
            modelsToDraw.add(modelPath);
        }
        return rVal;
    }

    /**
     * Creates an instanced actor of a model at a given model path
     * @param modelPath The path for the model
     * @return The instanced actor
     */
    public InstancedActor createInstancedActor(
            String modelPath,
            String vertexShaderPath,
            String fragmentShaderPath,
            StridedInstanceData instanceData,
            int capacity
        ){
        InstancedActor rVal = new InstancedActor(modelPath);
        if(!pathToInstanceData.containsKey(modelPath)){
            //queue shader
            Globals.assetManager.addShaderToQueue(vertexShaderPath, fragmentShaderPath);
            //if asset manager doesn't have model, queue model
            if(Globals.assetManager.fetchModel(modelPath) == null){
                Globals.assetManager.addModelPathToQueue(modelPath);
            }
            //register to internal-to-manager datastructures
            pathToInstanceData.put(modelPath,instanceData);
            modelsToDraw.add(modelPath);
        }
        return rVal;
    }

    /**
     * Draws all models that are queued in this instance manager
     */
    public void draw(RenderPipelineState renderPipelineState, OpenGLState openGLState){
        boolean instanced = renderPipelineState.getInstanced();
        boolean useMeshShader = renderPipelineState.getUseMeshShader();
        renderPipelineState.setInstanced(true);
        renderPipelineState.setUseMeshShader(false);
        for(String modelPath : modelsToDraw){
            //update render pipeline
            InstanceData data = pathToInstanceData.get(modelPath);
            renderPipelineState.setInstanceData(data);

            //fill buffers
            data.fillBuffers();

            //fetch model/shader and draw if both available
            VisualShader shader = Globals.assetManager.fetchShader(data.getVertexShader(), data.getFragmentShader());
            Model model = Globals.assetManager.fetchModel(modelPath);
            if(model != null && shader != null){
                openGLState.setActiveShader(renderPipelineState, shader);
                model.draw(renderPipelineState,openGLState);
            }

            data.flip();

            //clear queue
            data.clearDrawQueue();
        }
        renderPipelineState.setInstanced(instanced);
        renderPipelineState.setUseMeshShader(useMeshShader);
    }

}
