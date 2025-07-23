package electrosphere.renderer.actor.instance;


import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Sphered;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.RenderPipelineState;
import electrosphere.renderer.buffer.ShaderAttribute;
import electrosphere.renderer.model.Model;

/**
 * An instanced actor is a static (not bone animated) actor for an instanced model (eg grass, trees, leaves, rocks, etc)
 */
public class InstancedActor implements Comparable<InstancedActor> {

    /**
     * Path of the model that this instanced actor uses
     */
    String modelPath;

    /**
     * priority used for deciding to draw this actor or not
     */
    int priority;

    /**
     * Index used for deciding where in the data array it is
     */
    int index;

    /**
     * Actually unique information about the instance
     */
    Map<ShaderAttribute,Object> attributes = new HashMap<ShaderAttribute,Object>();

    /**
     * Creates an instanced actor
     * @param modelPath The path of the model this actor uses
     */
    protected InstancedActor(String modelPath){
        this.modelPath = modelPath;
    }
    
    /**
     * Draws the instanced actor. Should be called normally in a loop as if this was a regular actor.
     * @param renderPipelineState The pipeline state of the instanced actor
     * @param position The position used for frustum checking
     */
    public void draw(RenderPipelineState renderPipelineState, Vector3d position){
        Model model = Globals.assetManager.fetchModel(modelPath);
        if(model != null){
            boolean shouldRender = true;
            if(renderPipelineState.shouldFrustumCheck()){
                Sphered boundingSphere = model.getBoundingSphere();
                //frustum check if the model matrix exists (and we therefore can get position)
                boolean frustumCheck = renderPipelineState.getFrustumIntersection().testSphere((float)(position.x + boundingSphere.x), (float)(position.y + boundingSphere.y), (float)(position.z + boundingSphere.z), (float)boundingSphere.r);
                shouldRender = shouldRender && frustumCheck;    
            }
            if(shouldRender){
                Globals.clientInstanceManager.addToQueue(this);
            }
        }
    }

    /**
     * Draws the instanced actor WITHOUT frustum checking. Otherwise identical to the call with frustum checking
     * @param renderPipelineState
     */
    public void draw(RenderPipelineState renderPipelineState){
        Model model = Globals.assetManager.fetchModel(modelPath);
        if(model != null){
            Globals.clientInstanceManager.addToQueue(this);
        }
    }

    /**
     * Gets the path of the model packing this instanced actore
     * @return The path of the model
     */
    protected String getModelPath(){
        return this.modelPath;
    }

    /**
     * Comparable interface requirement to compare two instanced actors to find which one is higher priority
     * @param o The other instanced actor
     * @return The sort order
     */
    @Override
    public int compareTo(InstancedActor o) {
        return this.priority - o.priority;
    }

    /**
     * Sets the value of a given attribute on this instanced actor
     * @param attribute The attribute
     * @param value The value of that attribute for this instance
     */
    public void setAttribute(ShaderAttribute attribute, Object value){
        if(value instanceof Matrix4f){
            if(attributes.containsKey(attribute)){
                ((Matrix4f)attributes.get(attribute)).set((Matrix4f)value);
            } else {
                attributes.put(attribute, new Matrix4f((Matrix4f)value));
            }
        } else if(value instanceof Matrix4d){
            if(attributes.containsKey(attribute)){
                ((Matrix4d)attributes.get(attribute)).set((Matrix4d)value);
            } else {
                attributes.put(attribute, new Matrix4d((Matrix4d)value));
            }
        } else if(
            value instanceof Double ||
            value instanceof Float ||
            value instanceof Integer
        ){
            attributes.put(attribute, value);
        } else if(value instanceof Vector3f){
            attributes.put(attribute, value);
        } else if(value instanceof Vector3d){
            attributes.put(attribute, value);
        } else if(value instanceof Vector4d){
            attributes.put(attribute, value);
        } else if(value instanceof Vector4f){
            attributes.put(attribute, value);
        } else if(value instanceof Quaterniond){
            attributes.put(attribute, value);
        } else if(value instanceof Quaternionf){
            attributes.put(attribute, value);
        } else {
            LoggerInterface.loggerRenderer.ERROR("Unsupported operation " + value, new Exception());
        }
        // attributes.put(attribute, value);
    }

    /**
     * Gets the value of a attribute for this instanced actor
     * @param attribute The attribute index
     * @return The value
     */
    public Object getAttributeValue(ShaderAttribute attribute){
        return attributes.get(attribute);
    }

    /**
     * Gets the instanced actor for a given entity
     * @param entity The entity
     * @return The instanced actor if it exists, null otherwise
     */
    public static InstancedActor getInstancedActor(Entity entity){
        return (InstancedActor)entity.getData(EntityDataStrings.INSTANCED_ACTOR);
    }

    /**
     * Sets the draw priority of the instanced actor
     * @param priority The priority value (lower is higher priority)
     */
    public void setPriority(int priority){
        this.priority = priority;
    }

    /**
     * Gets the priority of this instanced actor
     * @return The priority of the instanced actor
     */
    public int getPriority(){
        return this.priority;
    }


    /**
     * If it exists, gets the model attribute for this instance
     * @param entity The entity to get the attribute from
     * @return The attribute if it exists, or null
     */
    public static ShaderAttribute getInstanceModelAttribute(Entity entity){
        return (ShaderAttribute)entity.getData(EntityDataStrings.INSTANCED_MODEL_ATTRIBUTE);
    }

}
