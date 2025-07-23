package electrosphere.data.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A list of all block types in game
 */
public class BlockData {

    /**
     * The set of all voxel types
     */
    Set<BlockType> types;

    /**
     * The map of block id -> boolean that stores whether the block should cutout transparent or blended transparent
     */
    Map<Integer,Boolean> solidsMap = new HashMap<Integer,Boolean>();

    /**
     * Gets all block types
     * @return The set of all block types
     */
    public Set<BlockType> getTypes(){
        return types;
    }

    /**
     * Gets the block type by its name, or null if that type does not exist
     * @param name The name of the block type
     * @return The block type or null
     */
    public BlockType getTypeFromName(String name){
        for(BlockType type : types){
            if(type.name.contains(name)){
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the block type by its id, or null if that type does not exist
     * @param id The id of the block type
     * @return The block type or null
     */
    public BlockType getTypeFromId(int id){
        for(BlockType type : types){
            if(type.id == id){
                return type;
            }
        }
        return null;
    }

    /**
     * Constructs the solids map
     */
    public void constructSolidsMap(){
        for(BlockType type : this.types){
            if(type.transparent != null && type.isTransparent()){
                solidsMap.put(type.getId(), false);
            } else {
                solidsMap.put(type.getId(), true);
            }
        }
    }

    /**
     * Gets the solids map
     * @return The solids map
     */
    public Map<Integer,Boolean> getSolidsMap(){
        return this.solidsMap;
    }


}
