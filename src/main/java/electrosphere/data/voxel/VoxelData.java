package electrosphere.data.voxel;

import java.util.Set;

/**
 * A list of all voxel types in game
 */
public class VoxelData {

    /**
     * The set of all voxel types
     */
    Set<VoxelType> types;

    /**
     * Gets all voxel types
     * @return The set of all voxel types
     */
    public Set<VoxelType> getTypes(){
        return types;
    }

    /**
     * Gets the voxel type by its name, or null if that type does not exist
     * @param name The name of the voxel type
     * @return The voxel type or null
     */
    public VoxelType getTypeFromName(String name){
        for(VoxelType type : types){
            if(type.name.contains(name)){
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the voxel type by its id, or null if that type does not exist
     * @param id The id of the voxel type
     * @return The voxel type or null
     */
    public VoxelType getTypeFromId(int id){
        for(VoxelType type : types){
            if(type.id == id){
                return type;
            }
        }
        return null;
    }
}
