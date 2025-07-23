package electrosphere.entity.scene;

/**
 * Descriptor of an entity in a scene
 */
public class EntityDescriptor {

    /**
     * The different types of entities that can be defined in an entity descriptor
     * Principally used in SceneLoader.java's serverInstantiateSceneFile function
     */
    public static final String TYPE_CREATURE = "creature";
    public static final String TYPE_ITEM = "item";
    public static final String TYPE_OBJECT = "object";
    
    //the type of entity (creature, item, etc)
    String type;
    //the subtype (eg human, katana, etc)
    String subtype;
    //position of the entity in the scene
    double posX;
    double posY;
    double posZ;
    //rotation of the entity in the scene
    double rotX;
    double rotY;
    double rotZ;
    double rotW;

    public String getType(){
        return type;
    }

    public String getSubtype(){
        return subtype;
    }

    public double getPosX(){
        return posX;
    }

    public double getPosY(){
        return posY;
    }

    public double getPosZ(){
        return posZ;
    }

    public double getRotX(){
        return rotX;
    }

    public double getRotY(){
        return rotY;
    }

    public double getRotZ(){
        return rotZ;
    }

    public double getRotW(){
        return rotW;
    }
    

}
