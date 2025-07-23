package electrosphere.server.datacell.physics;

import electrosphere.engine.Globals;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.server.datacell.Realm;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author satellite
 */
public class DataCellPhysicsManager {
    
    
    //the dimensions of the world that this cell manager can handles
    int cellWidth;
    
    //the width of a minicell in this manager
    int miniCellWidth;
    
    //all currently displaying mini cells
    Set<PhysicsDataCell> cells;
    Map<String,PhysicsDataCell> keyCellMap = new HashMap<String,PhysicsDataCell>();
    Set<String> invalid;
    Set<String> updateable;
    Set<String> hasRequested;
    
    
    VisualShader program;
    
    
    
    int drawRadius = 5;
    int drawStepdownInterval = 3;
    int drawStepdownValue = 25;
    
    int physicsRadius = 3;
    
    int worldBoundDiscreteMin = 0;
    int worldBoundDiscreteMax = 0;
        
    //client terrain manager
    // ClientTerrainManager clientTerrainManager;
    
    
    //ready to start updating?
    boolean update = false;

    //controls whether we try to generate the drawable entities
    //we want this to be false when in server-only mode
    boolean generateDrawables = false;

    Realm realm;
    
    
    /**
     * DrawCellManager constructor
     * @param commonWorldData The common world data
     * @param clientTerrainManager The client terrain manager
     * @param discreteX The initial discrete position X coordinate
     * @param discreteY The initial discrete position Y coordinate
     */
    public DataCellPhysicsManager(Realm realm, int discreteX, int discreteY, int discreteZ){
        worldBoundDiscreteMax = (int)(Globals.clientState.clientWorldData.getWorldBoundMin().x / ServerTerrainChunk.CHUNK_DIMENSION * 1.0f);
        cells = new HashSet<PhysicsDataCell>();
        invalid = new HashSet<String>();
        updateable = new HashSet<String>();
        hasRequested = new HashSet<String>();
        
        this.realm = realm;
        
        program = Globals.terrainShaderProgram;
        
        drawRadius = Globals.gameConfigCurrent.getSettings().getGraphicsPerformanceLODChunkRadius();
        drawStepdownInterval = Globals.gameConfigCurrent.getSettings().getGameplayPhysicsCellRadius();
        physicsRadius = Globals.gameConfigCurrent.getSettings().getGameplayPhysicsCellRadius();
        
        update = true;
    }
    
    DataCellPhysicsManager(){
        
    }
    
//     void updateInvalidCell(){
//         if(invalid.size() > 0){
//             String targetKey = invalid.iterator().next();
//             invalid.remove(targetKey);
//             Vector3i vector = getVectorFromKey(targetKey);
//             int currentCellX = cellX - drawRadius + vector.x;
//             int currentCellY = cellY - drawRadius + vector.y;
//             int currentCellZ = cellZ - drawRadius + vector.z;
            
            
//             if(
//                     currentCellX >= 0 &&
//                     currentCellX < Globals.clientWorldData.getWorldDiscreteSize() &&
//                     currentCellY >= 0 &&
//                     currentCellY < Globals.clientWorldData.getWorldDiscreteSize() &&
//                     currentCellZ >= 0 &&
//                     currentCellZ < Globals.clientWorldData.getWorldDiscreteSize()
//                     ){
//                 if(containsChunkDataAtWorldPoint(currentCellX, currentCellY, currentCellZ)){
//                     PhysicsDataCell cell = PhysicsDataCell.generateTerrainCell(
//                         realm,
//                         currentCellX,
//                         currentCellY,
//                         currentCellZ,
//                         Globals.serverTerrainManager.getChunk(currentCellX, currentCellY, currentCellZ)
//                     );
//                     cell.generatePhysics();
//                     cells.add(cell);
//                     keyCellMap.put(targetKey,cell);
//                 } else {
//                     if(!hasRequested.contains(targetKey)){
//                         //client should request chunk data from terrain manager
//                         hasRequested.add(targetKey);
//                     }
//                 }
//             }
//         }
//     }
    
    
//     /**
//      * Updates a cell that can be updated
//      */
//     void updateCellPhysics(){
//         if(updateable.size() > 0){
//             String targetKey = updateable.iterator().next();
//             updateable.remove(targetKey);
//             Vector3i vector = getVectorFromKey(targetKey);
//             int currentCellX = cellX - drawRadius + vector.x;
//             int currentCellY = cellY - drawRadius + vector.y;
//             int currentCellZ = cellZ - drawRadius + vector.z;
//             if(
//                     currentCellX >= 0 &&
//                     currentCellX < Globals.clientWorldData.getWorldDiscreteSize() &&
//                     currentCellY >= 0 &&
//                     currentCellY < Globals.clientWorldData.getWorldDiscreteSize() &&
//                     currentCellZ >= 0 &&
//                     currentCellZ < Globals.clientWorldData.getWorldDiscreteSize()
//                     ){
// //                if(Math.abs(drawRadius + 1 - targetX) < physicsRadius && Math.abs(drawRadius + 1 - targetY) < physicsRadius){
// //                    needsPhysics[targetX][targetY] = true;
// //                }
//                 // int dist = (int)Math.sqrt((targetX - drawRadius)*(targetX - drawRadius) + (targetY - drawRadius) * (targetY - drawRadius)); //Math.abs(targetX - drawRadius) * Math.abs(targetY - drawRadius);
//                 // int stride = Math.min(commonWorldData.getDynamicInterpolationRatio()/2, Math.max(1, dist / drawStepdownInterval * drawStepdownValue));
//                 // while(commonWorldData.getDynamicInterpolationRatio() % stride != 0){
//                 //     stride = stride + 1;
//                 // }
//                 keyCellMap.get(targetKey).generatePhysics();
//             }
//         }
//     }
    
    
//     public boolean containsInvalidCell(){
//         return invalid.size() > 0;
//     }
    
//     public boolean containsUpdateableCell(){
//         return updateable.size() > 0;
//     }
    
//     /**
//      * If any cells need physics, generates physics for them
//      */
//     // public void addPhysicsToCell(){
//     //     if(needsPhysics.size() > 0){
//     //         String targetKey = updateable.iterator().next();
//     //         updateable.remove(targetKey);
//     //         Vector3i vector = getVectorFromKey(targetKey);
//     //         int currentCellX = cellX - drawRadius + vector.x;
//     //         int currentCellY = cellY - drawRadius + vector.y;
//     //         int currentCellZ = cellZ - drawRadius + vector.z;
//     //         if(
//     //                 currentCellX >= 0 &&
//     //                 currentCellX < Globals.clientWorldData.getWorldDiscreteSize() &&
//     //                 currentCellY >= 0 &&
//     //                 currentCellY < Globals.clientWorldData.getWorldDiscreteSize() &&
//     //                 currentCellZ >= 0 &&
//     //                 currentCellZ < Globals.clientWorldData.getWorldDiscreteSize()
//     //                 ){
//     //             keyCellMap.get(targetKey).generatePhysics();
//     //         }
//     //         needsPhysics.remove(targetKey);
//     //     }
//     // }
    
    
    
//     public void shiftChunksNegX(){
//         //retire old graphics
//         for(int y = 0; y < drawRadius * 2 + 1; y++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX + drawRadius * 2, cellY + y, cellZ + z);
//                 PhysicsDataCell cell = keyCellMap.get(cellKey);
//                 cell.retireCell();

//                 invalid.remove(cellKey);
//             }
//         }
//         //invalidate edge of draw array
//         for(int y = 0; y < drawRadius * 2 + 1; y++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX - drawRadius * 2, cellY + y, cellZ + z);
//                 invalid.add(cellKey);
//             }
//         }
//     }
    
//     public void shiftChunksPosX(){
//         //retire old graphics
//         for(int y = 0; y < drawRadius * 2 + 1; y++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX - drawRadius * 2, cellY + y, cellZ + z);
//                 PhysicsDataCell cell = keyCellMap.get(cellKey);
//                 cell.retireCell();

//                 invalid.remove(cellKey);
//             }
//         }
//         //invalidate edge of draw array
//         for(int y = 0; y < drawRadius * 2 + 1; y++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX + drawRadius * 2, cellY + y, cellZ + z);
//                 invalid.add(cellKey);
//             }
//         }
//     }

//     public void shiftChunksNegY(){
//         //retire old graphics
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX + x, cellY + drawRadius * 2, cellZ + z);
//                 PhysicsDataCell cell = keyCellMap.get(cellKey);
//                 cell.retireCell();

//                 invalid.remove(cellKey);
//             }
//         }
//         //invalidate edge of draw array
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX + x, cellY - drawRadius * 2, cellZ + z);
//                 invalid.add(cellKey);
//             }
//         }
//     }
    
//     public void shiftChunksPosY(){
//         //retire old graphics
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX + x, cellY - drawRadius * 2, cellZ + z);
//                 PhysicsDataCell cell = keyCellMap.get(cellKey);
//                 cell.retireCell();

//                 invalid.remove(cellKey);
//             }
//         }
//         //invalidate edge of draw array
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int z = 0; z < drawRadius * 2 + 1; z++){
//                 String cellKey = getCellKey(cellX + x, cellY + drawRadius * 2, cellZ + z);
//                 invalid.add(cellKey);
//             }
//         }
//     }

//     public void shiftChunksNegZ(){
//         //retire old graphics
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int y = 0; y < drawRadius * 2 + 1; y++){
//                 String cellKey = getCellKey(cellX + x, cellY + y, cellZ + drawRadius * 2);
//                 PhysicsDataCell cell = keyCellMap.get(cellKey);
//                 cell.retireCell();

//                 invalid.remove(cellKey);
//             }
//         }
//         //invalidate edge of draw array
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int y = 0; y < drawRadius * 2 + 1; y++){
//                 String cellKey = getCellKey(cellX + x, cellY + y, cellZ - drawRadius * 2);
//                 invalid.add(cellKey);
//             }
//         }
//     }
    
//     public void shiftChunksPosZ(){
//         //retire old graphics
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int y = 0; y < drawRadius * 2 + 1; y++){
//                 String cellKey = getCellKey(cellX + x, cellY + y, cellZ - drawRadius * 2);
//                 PhysicsDataCell cell = keyCellMap.get(cellKey);
//                 cell.retireCell();
                
//                 invalid.remove(cellKey);
//             }
//         }
//         //invalidate edge of draw array
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int y = 0; y < drawRadius * 2 + 1; y++){
//                 String cellKey = getCellKey(cellX + x, cellY + y, cellZ + drawRadius * 2);
//                 invalid.add(cellKey);
//             }
//         }
//     }
    
    
//     public int transformRealSpaceToCellSpace(double input){
//         return (int)(input / Globals.clientWorldData.getDynamicInterpolationRatio());
//     }
    
//     /**
//      * Clears the valid set and adds all keys to invalid set
//      */
//     public void invalidateAllCells(){
//         for(int x = 0; x < drawRadius * 2 + 1; x++){
//             for(int y = 0; y < drawRadius * 2 + 1; y++){
//                 for(int z = 0; z < drawRadius * 2 + 1; z++){
//                     invalid.add(getCellKey(x, y, z));
//                 }
//             }
//         }
//     }
    
//     /**
//      * Calculates whether the position of the player has changed and if so, invalidates and cleans up cells accordingly
//      * @param oldPosition The position of the player entity on previous frame
//      * @param newPosition The position of the player entity on current frame
//      */
//     public void calculateDeltas(Vector3d oldPosition, Vector3d newPosition){
//         // if(transformRealSpaceToCellSpace(newPosition.x()) < transformRealSpaceToCellSpace(oldPosition.x())){
//         //     shiftChunksNegX();
//         //     setCellX(transformRealSpaceToCellSpace(newPosition.x()));
//         //     setCellY(transformRealSpaceToCellSpace(newPosition.y()));
//         //     setCellZ(transformRealSpaceToCellSpace(newPosition.z()));
//         // } else if(transformRealSpaceToCellSpace(newPosition.x()) > transformRealSpaceToCellSpace(oldPosition.x())){
//         //     shiftChunksPosX();
//         //     setCellX(transformRealSpaceToCellSpace(newPosition.x()));
//         //     setCellY(transformRealSpaceToCellSpace(newPosition.y()));
//         //     setCellZ(transformRealSpaceToCellSpace(newPosition.z()));
//         // }
        
//         // if(transformRealSpaceToCellSpace(newPosition.y()) < transformRealSpaceToCellSpace(oldPosition.y())){
//         //     shiftChunksNegY();
//         //     setCellX(transformRealSpaceToCellSpace(newPosition.x()));
//         //     setCellY(transformRealSpaceToCellSpace(newPosition.y()));
//         //     setCellZ(transformRealSpaceToCellSpace(newPosition.z()));
//         // } else if(transformRealSpaceToCellSpace(newPosition.y()) > transformRealSpaceToCellSpace(oldPosition.y())){
//         //     shiftChunksPosY();
//         //     setCellX(transformRealSpaceToCellSpace(newPosition.x()));
//         //     setCellY(transformRealSpaceToCellSpace(newPosition.y()));
//         //     setCellZ(transformRealSpaceToCellSpace(newPosition.z()));
//         // }

//         // if(transformRealSpaceToCellSpace(newPosition.z()) < transformRealSpaceToCellSpace(oldPosition.z())){
//         //     shiftChunksNegZ();
//         //     setCellX(transformRealSpaceToCellSpace(newPosition.x()));
//         //     setCellY(transformRealSpaceToCellSpace(newPosition.y()));
//         //     setCellZ(transformRealSpaceToCellSpace(newPosition.z()));
//         // } else if(transformRealSpaceToCellSpace(newPosition.z()) > transformRealSpaceToCellSpace(oldPosition.z())){
//         //     shiftChunksPosZ();
//         //     setCellX(transformRealSpaceToCellSpace(newPosition.x()));
//         //     setCellY(transformRealSpaceToCellSpace(newPosition.y()));
//         //     setCellZ(transformRealSpaceToCellSpace(newPosition.z()));
//         // }
//     }
    
//     /**
//      * Updates cells that need updating in this manager
//      */
//     public void update(){
//         if(update){
//             if(containsInvalidCell()){
//                 updateInvalidCell();
//             } else if(containsUpdateableCell()){
//                 updateCellPhysics();
//             }
//         }
//     }
    
//     public boolean coordsInPhysicsSpace(int worldX, int worldY){
//         return worldX <= cellX + physicsRadius && worldX >= cellX - physicsRadius && worldY <= cellY + physicsRadius && worldY >= cellY - physicsRadius;
//     }

//     public void setGenerateDrawables(boolean generate){
//         this.generateDrawables = generate;
//     }

//     boolean containsChunkDataAtWorldPoint(int currentCellX, int currentCellY, int currentCellZ){
//         if(Globals.clientTerrainManager != null){
//             return Globals.clientTerrainManager.containsChunkDataAtWorldPoint(currentCellX, currentCellY, currentCellZ);
//         }
//         return true;
//     }

//     /**
//      * Gets the chunk data at a given point
//      * @param currentCellX The x coord
//      * @param currentCellY The y coord
//      * @param currentCellZ The z coord
//      * @return The chunk data at the specified points
//      */
//     ChunkData getChunkDataAtPoint(int currentCellX, int currentCellY, int currentCellZ){
//         return Globals.clientTerrainManager.getChunkDataAtWorldPoint(currentCellX, currentCellY, currentCellZ);
//     }

//     // float[][] getHeightmapAtPoint(int currentCellX, int currentCellY, int currentCellZ){
//     //     if(Globals.clientTerrainManager != null){
//     //         return Globals.clientTerrainManager.getHeightmapAtPoint(currentCellX, currentCellY, currentCellZ);
//     //     }
//     //     return Globals.serverTerrainManager.getChunk(currentCellX, currentCellY).getHeightMap();
//     // }

//     // float[][] getTextureMapAtPoint(int currentCellX, int currentCellY){
//     //     if(Globals.clientTerrainManager != null){
//     //         return Globals.clientTerrainManager.getTextureMapAtPoint(currentCellX,currentCellY);
//     //     } else {
//     //         //hacky fix to +2 to this, I think the interpolation ratio was different for server/client data
//     //         //now that we're merging/ambiguous within this class, it's out of bounds-ing unless I +2
//     //         float[][] rVal = new float[commonWorldData.getDynamicInterpolationRatio() + 2][commonWorldData.getDynamicInterpolationRatio() + 2];
//     //         rVal[1][1] = 1;
//     //         rVal[2][1] = 1;
//     //         rVal[3][1] = 1;
//     //         rVal[4][1] = 1;
//     //         rVal[5][1] = 1;
//     //         rVal[5][2] = 1;
//     //         rVal[6][1] = 1;
//     //         rVal[6][2] = 1;
//     //         return rVal;
//     //     }
//     // }

//     /**
//      * Gets a unique key for the cell
//      * @param worldX The world position x component of the cell
//      * @param worldY The world position y component of the cell
//      * @param worldZ The world position z component of the cell
//      * @return The key
//      */
//     private String getCellKey(int worldX, int worldY, int worldZ){
//         return worldX + "_" + worldY + "_" + worldZ;
//     }

//     /**
//      * Parses a vector3i from the cell key
//      * @param key The cell key
//      * @return The vector3i containing the components of the cell key
//      */
//     private Vector3i getVectorFromKey(String key){
//         String[] keyComponents = key.split("_");
//         return new Vector3i(Integer.parseInt(keyComponents[0]),Integer.parseInt(keyComponents[1]),Integer.parseInt(keyComponents[2]));
//     }


    
//    public 
    
}
