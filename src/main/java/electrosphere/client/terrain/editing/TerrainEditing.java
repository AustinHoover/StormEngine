package electrosphere.client.terrain.editing;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.net.parser.net.message.TerrainMessage;

/**
 * Utilities for editing terrain from client side of things
 */
public class TerrainEditing {

    /**
     * Performs a terrain chunk edit. Basically has a sphere around the provided position that it attempts to add value to.
     * @param position The position to perform the edit
     * @param editMagnitude The magnitude of the edit to perform
     * @param type The type of block to make all edited blocks
     * @param weight The weight of the sphere to apply the edit to
     */
    public static void editTerrain(Vector3d position, float editMagnitude, int type, float weight){
        if(position != null){
            Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestUseTerrainPaletteMessage(position.x, position.y, position.z, editMagnitude, weight, type));
            //calculate kernel size
            // int numPlacesToCheck = (int)((editMagnitude * 2 + 1) * (editMagnitude * 2 + 1) * (editMagnitude * 2 + 1));
            // //create and fill in kernel of positions to check
            // int[] xOffsetSet = new int[numPlacesToCheck];
            // int[] yOffsetSet = new int[numPlacesToCheck];
            // int[] zOffsetSet = new int[numPlacesToCheck];
            // int i = 0;
            // for(int x = -(int)editMagnitude; x <= (int)editMagnitude; x++){
            //     for(int y = -(int)editMagnitude; y <= (int)editMagnitude; y++){
            //         for(int z = -(int)editMagnitude; z <= (int)editMagnitude; z++){
            //             xOffsetSet[i] = x;
            //             yOffsetSet[i] = y;
            //             zOffsetSet[i] = z;
            //             i++;
            //         }
            //     }
            // }
            // for(i = 0; i < numPlacesToCheck; i++){
            //     //calculate position of edit
            //     Vector3d offsetPos = new Vector3d(position).add(xOffsetSet[i],yOffsetSet[i],zOffsetSet[i]);
            //     Vector3i chunkPos = Globals.clientWorldData.convertRealToChunkSpace(offsetPos);
            //     Vector3i voxelPos = Globals.clientWorldData.convertRealToVoxelSpace(offsetPos);
            //     //get distance from true center point of sphere to current voxel position in world space
            //     float distance = (float)new Vector3d(Math.floor(offsetPos.x),Math.floor(offsetPos.y),Math.floor(offsetPos.z)).distance(position);
            //     float currentPositionMagnitude = editMagnitude - distance;
            //     ChunkData data = Globals.clientTerrainManager.getChunkDataAtWorldPoint(chunkPos.x, chunkPos.y, chunkPos.z);
            //     if(
            //         voxelPos.x < ChunkData.CHUNK_SIZE &&
            //         voxelPos.y < ChunkData.CHUNK_SIZE &&
            //         voxelPos.z < ChunkData.CHUNK_SIZE &&
            //         currentPositionMagnitude > 0 &&
            //         data != null
            //     ){
            //         float current = data.getVoxelWeight()[voxelPos.x][voxelPos.y][voxelPos.z];
            //         //hard clamp so it doesn't go over 1
            //         float finalValue = Math.max(Math.min(current + weight,1),-1);
            //         Globals.clientTerrainManager.updateChunk(
            //             chunkPos.x, chunkPos.y, chunkPos.z,
            //             voxelPos.x, voxelPos.y, voxelPos.z,
            //             finalValue, 1
            //         );
            //     }
            // }
        }
    }

    /**
     * Tries to remove terrain with proper game logic checks applied
     * @param position The position to perform the edit
     * @param editMagnitude The magnitude of the edit to perform
     * @param weight The weight of the sphere to apply the edit to
     */
    public static void removeTerrainGated(Vector3d position, float editMagnitude, float weight){
        if(position != null){
            Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestDestroyTerrainMessage(position.x, position.y, position.z, editMagnitude, weight));
        }
    }
    
}
