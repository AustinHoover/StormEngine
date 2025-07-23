package electrosphere.entity.state.light;

import org.joml.Vector3d;
import org.joml.Vector3f;

import electrosphere.data.entity.common.light.PointLightDescription;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.equip.ClientEquipState;
import electrosphere.renderer.light.PointLight;

/**
 * Keeps the point light attached to the entity
 */
public class ClientPointLightComponent implements BehaviorTree {


    /**
     * The parent entity
     */
    Entity parent;

    /**
     * The description of the point light
     */
    PointLightDescription description;

    /**
     * The point light itself
     */
    PointLight light;

    @Override
    public void simulate(float deltaTime) {
        Vector3d entityPos = EntityUtils.getPosition(parent);
        Vector3d offset = null;
        if(description.getOffset() != null){
            offset = new Vector3d(description.getOffset());
        } else {
            offset = new Vector3d();
        }
        light.setPosition(new Vector3f((float)entityPos.x,(float)entityPos.y,(float)entityPos.z).add((float)offset.x,(float)offset.y,(float)offset.z));
    }

    /**
     * Private constructor
     * @param parent
     * @param params
     */
    private ClientPointLightComponent(Entity parent, Object ... params){
        this.parent = parent;
        description = (PointLightDescription)params[0];
        light = Globals.renderingEngine.getLightManager().createPointLight(parent, description);
    }
    

    /**
     * <p>
     * Attaches this tree to the entity.
     * </p>
     * @param entity The entity to attach to
     * @param tree The behavior tree to attach
     * @param params Optional parameters that will be provided to the constructor
     */
    public static ClientPointLightComponent attachTree(Entity parent, Object ... params){
        ClientPointLightComponent rVal = new ClientPointLightComponent(parent,params);
        //!!WARNING!! from here below should not be touched
        //This was generated automatically to properly alert various systems that the btree exists and should be tracked
        parent.putData(EntityDataStrings.TREE_CLIENTLIGHTSTATE, rVal);
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(rVal);
        return rVal;
    }
    /**
     * <p>
     * Detatches this tree from the entity.
     * </p>
     * @param entity The entity to detach to
     * @param tree The behavior tree to detach
     */
    public static void detachTree(Entity entity, BehaviorTree tree){
    }
    /**
     * <p>
     * Gets the ClientEquipState of the entity
     * </p>
     * @param entity the entity
     * @return The ClientEquipState
     */
    public static ClientEquipState getClientEquipState(Entity entity){
        return (ClientEquipState)entity.getData(EntityDataStrings.TREE_CLIENTLIGHTSTATE);
    }

}
