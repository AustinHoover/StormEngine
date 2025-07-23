package electrosphere.entity.scene;

/**
 * Description of the realm a scene is created within
 */
public class RealmDescriptor {

    /**
     * A gridded realm
     */
    public static final String REALM_DESCRIPTOR_GRIDDED = "gridded";
    public static final String REALM_DESCRIPTOR_PROCEDURAL = "procedural";
    public static final String REALM_DESCRIPTOR_GENERATION_TESTING = "generationTesting";

    /**
     * Types of procedural world terrain
     */
    public static final String PROCEDURAL_TYPE_DEFAULT = "proceduralTypeDefault";
    public static final String PROCEDURAL_TYPE_HOMOGENOUS = "proceduralTypeHomogenous";

    /**
     * The dirt voxel type's id
     */
    public static final int VOXEL_DIRT_ID = 1;
    
    /**
     * The type of realm
     */
    String type = REALM_DESCRIPTOR_GRIDDED;

    /**
     * If this is a gridded realm, what is the size of the realm
     */
    int griddedRealmSize;

    /**
     * The base voxel type to generate with
     */
    Integer baseVoxel = VOXEL_DIRT_ID;

    /**
     * The type of world
     */
    String worldType = PROCEDURAL_TYPE_DEFAULT;

    /**
     * The type of biome for a homogenous world type
     */
    String biomeType = "";


    /**
     * Gets the type of realm
     * @return The type
     */
    public String getType(){
        return type;
    }

    /**
     * Sets the type of realm
     * @param realmType The realm type
     */
    public void setType(String realmType){
        this.type = realmType;
    }

    /**
     * Gets the size of the gridded realm
     * @return The size
     */
    public int getGriddedRealmSize(){
        return griddedRealmSize;
    }

    /**
     * Sets the size of the gridded realm
     * @param size The size
     */
    public void setGriddedRealmSize(int size){
        this.griddedRealmSize = size;
    }

    /**
     * Gets the id of the base voxel type
     * @return the id of the base voxel type
     */
    public Integer getBaseVoxel(){
        return this.baseVoxel;
    }

    /**
     * Sets the base voxel type
     * @param voxelId The voxel type's id
     */
    public void setBaseVoxel(int voxelId){
        this.baseVoxel = voxelId;
    }

    /**
     * Gets the type of the world
     * @return The type of the world
     */
    public String getWorldType() {
        return worldType;
    }

    /**
     * Sets the type of the world
     * @param worldType The type of the world
     */
    public void setWorldType(String worldType) {
        this.worldType = worldType;
    }

    /**
     * Gets the type of biome for a homogenous world
     * @return The biome
     */
    public String getBiomeType() {
        return biomeType;
    }

    /**
     * Sets the biome type for a homogenous world
     * @param biomeType The biome type
     */
    public void setBiomeType(String biomeType) {
        this.biomeType = biomeType;
    }

    

}
