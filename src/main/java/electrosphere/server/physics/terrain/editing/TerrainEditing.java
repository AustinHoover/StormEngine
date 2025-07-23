package electrosphere.server.physics.terrain.editing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.data.entity.item.Item;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.datacell.interfaces.VoxelCellManager;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Provides utilities for editing terrain (particularly brushes, etc)
 */
public class TerrainEditing {

    /**
     * the minimum value before hard setting to 0
     */
    static final float MINIMUM_FULL_VALUE = 0.01f;

    /**
     * The default magnitude
     */
    public static final float DEFAULT_MAGNITUDE = 1.1f;

    /**
     * The default weight
     */
    public static final float DEFAULT_WEIGHT = 1.0f;
    
    /**
     * Performs a terrain chunk edit. Basically has a sphere around the provided position that it attempts to add value to.
     * @param position The position to perform the edit
     * @param editMagnitude The magnitude of the edit to perform
     * @param type The type of block to make all edited blocks
     * @param weight The weight of the sphere to apply the edit to
     */
    public static void editTerrain(Realm realm, Vector3d position, float editMagnitude, int type, float weight){
        if(position != null && realm != null && realm.getDataCellManager() instanceof VoxelCellManager){
            VoxelCellManager voxelCellManager = (VoxelCellManager) realm.getDataCellManager();
            //calculate kernel size
            int numPlacesToCheck = (int)((editMagnitude * 2 + 1) * (editMagnitude * 2 + 1) * (editMagnitude * 2 + 1));
            //create and fill in kernel of positions to check
            int[] xOffsetSet = new int[numPlacesToCheck];
            int[] yOffsetSet = new int[numPlacesToCheck];
            int[] zOffsetSet = new int[numPlacesToCheck];
            int i = 0;
            for(int x = -(int)editMagnitude; x <= (int)editMagnitude; x++){
                for(int y = -(int)editMagnitude; y <= (int)editMagnitude; y++){
                    for(int z = -(int)editMagnitude; z <= (int)editMagnitude; z++){
                        xOffsetSet[i] = x;
                        yOffsetSet[i] = y;
                        zOffsetSet[i] = z;
                        i++;
                    }
                }
            }
            for(i = 0; i < numPlacesToCheck; i++){
                //calculate position of edit
                Vector3d offsetPos = new Vector3d(position).add(xOffsetSet[i],yOffsetSet[i],zOffsetSet[i]);
                Vector3i chunkPos = ServerWorldData.convertRealToWorldSpace(offsetPos);
                Vector3i voxelPos = ServerWorldData.convertRealToVoxelSpace(offsetPos);
                //get distance from true center point of sphere to current voxel position in world space
                float distance = (float)new Vector3d(Math.floor(offsetPos.x),Math.floor(offsetPos.y),Math.floor(offsetPos.z)).distance(position);
                float currentPositionMagnitude = editMagnitude - distance;

                ServerTerrainChunk data;
                if(
                    voxelPos.x < ServerTerrainChunk.CHUNK_DIMENSION &&
                    voxelPos.y < ServerTerrainChunk.CHUNK_DIMENSION &&
                    voxelPos.z < ServerTerrainChunk.CHUNK_DIMENSION &&
                    voxelPos.x >= 0 &&
                    voxelPos.y >= 0 &&
                    voxelPos.z >= 0 &&
                    chunkPos.x >= 0 &&
                    chunkPos.y >= 0 &&
                    chunkPos.z >= 0 &&
                    currentPositionMagnitude > 0 &&
                    (data = voxelCellManager.getChunkAtPosition(chunkPos)) != null
                ){
                    float current = data.getWeights()[voxelPos.x][voxelPos.y][voxelPos.z];
                    //hard clamp so it doesn't go over 1
                    float finalValue = Math.max(Math.min(current + weight / distance,1),-1);
                    if(finalValue < MINIMUM_FULL_VALUE && current > MINIMUM_FULL_VALUE){
                        finalValue = -1;
                    }
                    voxelCellManager.editChunk(chunkPos, voxelPos, finalValue, type);
                }
            }
        }
    }

    /**
     * Performs a terrain chunk edit. Basically has a sphere around the provided position that it attempts to remove value from.
     * @param realm The realm to destroy terrain within
     * @param sourceEntity The entity performing the destruction
     * @param position The position to perform the edit
     * @param editMagnitude The magnitude of the edit to perform
     * @param weight The weight of the sphere to apply the edit to
     */
    public static void destroyTerrain(Realm realm, Entity sourceEntity, Vector3d position, float editMagnitude, float weight){
        if(position != null && realm != null && realm.getDataCellManager() instanceof VoxelCellManager){
            VoxelCellManager voxelCellManager = (VoxelCellManager) realm.getDataCellManager();
            //calculate kernel size
            int numPlacesToCheck = (int)((editMagnitude * 2 + 1) * (editMagnitude * 2 + 1) * (editMagnitude * 2 + 1));
            //create and fill in kernel of positions to check
            int[] xOffsetSet = new int[numPlacesToCheck];
            int[] yOffsetSet = new int[numPlacesToCheck];
            int[] zOffsetSet = new int[numPlacesToCheck];
            int i = 0;
            for(int x = -(int)editMagnitude; x <= (int)editMagnitude; x++){
                for(int y = -(int)editMagnitude; y <= (int)editMagnitude; y++){
                    for(int z = -(int)editMagnitude; z <= (int)editMagnitude; z++){
                        xOffsetSet[i] = x;
                        yOffsetSet[i] = y;
                        zOffsetSet[i] = z;
                        i++;
                    }
                }
            }
            Map<Integer,Float> typeAmountMap = new HashMap<Integer,Float>();
            for(i = 0; i < numPlacesToCheck; i++){
                //calculate position of edit
                Vector3d offsetPos = new Vector3d(position).add(xOffsetSet[i],yOffsetSet[i],zOffsetSet[i]);
                Vector3i chunkPos = ServerWorldData.convertRealToWorldSpace(offsetPos);
                Vector3i voxelPos = ServerWorldData.convertRealToVoxelSpace(offsetPos);
                //get distance from true center point of sphere to current voxel position in world space
                float distance = (float)new Vector3d(Math.floor(offsetPos.x),Math.floor(offsetPos.y),Math.floor(offsetPos.z)).distance(position);
                float currentPositionMagnitude = editMagnitude - distance;

                ServerTerrainChunk data;
                if(
                    voxelPos.x < ServerTerrainChunk.CHUNK_DIMENSION &&
                    voxelPos.y < ServerTerrainChunk.CHUNK_DIMENSION &&
                    voxelPos.z < ServerTerrainChunk.CHUNK_DIMENSION &&
                    voxelPos.x >= 0 &&
                    voxelPos.y >= 0 &&
                    voxelPos.z >= 0 &&
                    chunkPos.x >= 0 &&
                    chunkPos.y >= 0 &&
                    chunkPos.z >= 0 &&
                    currentPositionMagnitude > 0 &&
                    (data = voxelCellManager.getChunkAtPosition(chunkPos)) != null
                ){
                    int originalTypeAtPos = voxelCellManager.getVoxelTypeAtLocalPosition(chunkPos, voxelPos);
                    float current = data.getWeights()[voxelPos.x][voxelPos.y][voxelPos.z];
                    //hard clamp so it doesn't go over 1
                    float finalValue = Math.max(Math.min(current + weight / distance,1),-1);
                    int finalType = originalTypeAtPos;
                    if(finalValue < MINIMUM_FULL_VALUE && current >= MINIMUM_FULL_VALUE){
                        finalValue = -1;
                        finalType = ServerTerrainChunk.VOXEL_TYPE_AIR;
                    }
                    voxelCellManager.editChunk(chunkPos, voxelPos, finalValue, finalType);

                    //keep track of how much we're editing each chunk so we can add items to inventories
                    if(typeAmountMap.containsKey(originalTypeAtPos)){
                        typeAmountMap.put(originalTypeAtPos,typeAmountMap.get(originalTypeAtPos) + (finalValue - current));
                    } else {
                        typeAmountMap.put(originalTypeAtPos,(finalValue - current));
                    }
                }
            }

            if(sourceEntity == null){
                throw new Error("Does not support destroying terrain without a source entity");
            }

            for(Entry<Integer,Float> typeEntry : typeAmountMap.entrySet()){
                VoxelType voxelType = Globals.gameConfigCurrent.getVoxelData().getTypeFromId(typeEntry.getKey());
                Item voxelItem = Globals.gameConfigCurrent.getItemMap().getItem(Item.getVoxelTypeId(voxelType));
                int count = -(int)(float)typeEntry.getValue();
                ServerInventoryState.serverCreateInventoryItem(sourceEntity, voxelItem.getId(), count);
            }

        }
    }

}
