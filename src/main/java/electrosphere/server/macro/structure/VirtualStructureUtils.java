package electrosphere.server.macro.structure;

import org.joml.AABBd;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.client.block.BlockChunkData;
import electrosphere.controls.cursor.CursorState;

/**
 * Utility functions for dealing with structures on the server
 */
public class VirtualStructureUtils {
    
    
//     public static Structure placeStructureAtPoint(float posX, float posY, float posZ, String type){
//         Realm realm = Globals.serverState.realmManager.getRealms().iterator().next();
//         int worldX = realm.getServerWorldData().convertRealToChunkSpace(posX);
//         int worldY = realm.getServerWorldData().convertRealToChunkSpace(posY);
//         Structure rVal = new Structure(worldX,worldY,posX,posY,type);
//         Globals.macroData.addStructure(rVal);
        
// //         double centerHeight = Globals.serverTerrainManager.getHeightAtPosition(posX, posY, posZ);
// //         StructureType currentTypeObject = Globals.gameConfigCurrent.getStructureTypeMap().getType(type);
// //         float radius = currentTypeObject.getRadius();
// //         for(int x = -(int)radius; x < radius; x++){
// //             for(int y = -(int)radius; y < radius; y++){
// //                 int newWorldX = Globals.serverWorldData.convertRealToChunkSpace(posX + x);
// //                 int newWorldY = Globals.serverWorldData.convertRealToChunkSpace(posY + y);
// //                 double newLocationX = Globals.serverWorldData.getRelativeLocation(posX + x, newWorldX);
// //                 double newLocationY = Globals.serverWorldData.getRelativeLocation(posY + y, newWorldY);
// // //                System.out.println("Set height: " + centerHeight);
// // //                System.out.println("Deform in chunk: " + newWorldX + "," + newWorldY);
// //                 Globals.serverTerrainManager.deformTerrainAtLocationToValue(newWorldX, newWorldY, (int)(newLocationX), (int)(newLocationY), (float)centerHeight);
// //             }
// //         }
//         // StructureUtils.serverSpawnBasicStructure(type, realm, new Vector3d(posX,(float)centerHeight + 2.4f,posY), new Quaternionf());
//         return rVal;
//     }
    
    public static boolean validStructurePlacementPosition(float posX, float posY, String type){
        // StructureType toPlaceType = Globals.gameConfigCurrent.getStructureTypeMap().getType(type);
        // Vector2f toPlacePos = new Vector2f(posX, posY);
        // for(Structure virtualStruct : Globals.macroData.getStructures()){
        //     StructureType existantType = Globals.gameConfigCurrent.getStructureTypeMap().getType(virtualStruct.getType());
        //     Vector2f existantPos = new Vector2f(virtualStruct.getLocationX(),virtualStruct.getLocationY());
        //     if(existantPos.distance(toPlacePos) < toPlaceType.getRadius() + existantType.getRadius()){
        //         return false;
        //     }
        // }
        return true;
    }

    /**
     * Samples a virtual structure's fab
     * @param struct The structure
     * @param currRealPos The real position to sample from
     * @return The type if it is in the structure's bounds, 0 otherwise
     */
    public static short getSample(VirtualStructure struct, Vector3d currRealPos){
        AABBd aabb = struct.getAABB();
        Vector3d localBlockPos = new Vector3d(
            Math.round((currRealPos.x - aabb.minX) / BlockChunkData.BLOCK_SIZE_MULTIPLIER),
            Math.round((currRealPos.y - aabb.minY) / BlockChunkData.BLOCK_SIZE_MULTIPLIER),
            Math.round((currRealPos.z - aabb.minZ) / BlockChunkData.BLOCK_SIZE_MULTIPLIER)
        );

        Quaterniond rotationQuat = CursorState.getBlockRotation(struct.getRotation());
        rotationQuat.transform(localBlockPos);
        Vector3d dimVec = new Vector3d(struct.getFab().getDimensions());
        rotationQuat.transform(dimVec);
        dimVec.absolute();
        if(localBlockPos.x < 0){
            localBlockPos.x = dimVec.x + localBlockPos.x;
        }
        if(localBlockPos.y < 0){
            localBlockPos.y = dimVec.y + localBlockPos.y;
        }
        if(localBlockPos.z < 0){
            localBlockPos.z = dimVec.z + localBlockPos.z;
        }
        int finalX = Math.round((float)localBlockPos.x);
        int finalY = Math.round((float)localBlockPos.y);
        int finalZ = Math.round((float)localBlockPos.z);
        if(
            finalX >= 0 &&
            finalY >= 0 &&
            finalZ >= 0 &&
            finalX < struct.getFab().getDimensions().x &&
            finalY < struct.getFab().getDimensions().y &&
            finalZ < struct.getFab().getDimensions().z
        ){
            return struct.getFab().getType(finalX,finalY,finalZ);
        }
        return 0;
    }

}
