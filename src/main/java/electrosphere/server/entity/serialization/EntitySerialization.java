package electrosphere.server.entity.serialization;

import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * A serializaed entity
 */
public class EntitySerialization {

    /**
     * The type of the entity
     */
    int type;
    
    /**
     * The subtype of the entity
     */
    String subtype;

    /**
     * The (optional) template of the entity
     */
    String template;

    /**
     * The position of the entity
     */
    Vector3d position;

    /**
     * The rotation of the entity
     */
    Quaterniond rotation;

    /**
     * Gets the type of the entity
     * @return The type of the entity
     */
    public int getType(){
        return type;
    }

    /**
     * Sets the type of the entity
     * @param type The type
     */
    public void setType(int type){
        this.type = type;
    }

    /**
     * Gets the subtype of the entity
     * @return The subtype
     */
    public String getSubtype(){
        return subtype;
    }

    /**
     * Sets the subtype of the entity
     * @param subtype The subtype
     */
    public void setSubtype(String subtype){
        this.subtype = subtype;
    }

    /**
     * Gets the (optional) template of the entity
     * @return The template
     */
    public String getTemplate(){
        return template;
    }

    /**
     * Sets the template of the entity
     * @param template The template
     */
    public void setTemplate(String template){
        this.template = template;
    }

    /**
     * Gets the position of the entity
     * @return The position
     */
    public Vector3d getPosition(){
        return position;
    }

    /**
     * Sets the position of the entity
     * @param position The position
     */
    public void setPosition(Vector3d position){
        this.position = position;
    }

    /**
     * Gets the rotation of the entity
     * @return The rotation
     */
    public Quaterniond getRotation(){
        return rotation;
    }

    /**
     * Sets the rotation of the entity
     * @param rotation The rotation
     */
    public void setRotation(Quaterniond rotation){
        this.rotation = rotation;
    }


}
