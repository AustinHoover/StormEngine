package electrosphere.client.fluid.cells;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.client.scene.ClientWorldData;
import electrosphere.client.terrain.cache.ChunkData;
import electrosphere.client.terrain.manager.ClientTerrainManager;
import electrosphere.engine.Globals;
import electrosphere.entity.EntityUtils;
import electrosphere.net.parser.net.message.TerrainMessage;
import electrosphere.renderer.shader.VisualShader;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 *
 * @author satellite
 */
public class FluidCellManager {

    /**
     * The number of times to iteratively update per frame
     */
    static final int UPDATE_COUNT = 27;
    
    
    //the center of this cell manager's array in cell space
    int cellX;
    int cellY;
    int cellZ;
    
    
    //the dimensions of the world that this cell manager can handles
    int cellWidth;
    
    //the width of a minicell in this manager
    int miniCellWidth;
    
    //all currently displaying mini cells
    Set<FluidCell> cells;
    Map<String,FluidCell> keyCellMap = new HashMap<String,FluidCell>();
    Set<String> hasNotRequested;
    Set<String> hasRequested;
    Set<String> drawable;
    Set<String> undrawable;
    Set<String> updateable;
    
    
    VisualShader program;
    
    
    
    // int drawRadius = 5;
    int drawStepdownInterval = 3;
    int drawStepdownValue = 25;

    double drawRadius = 5 * ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
    
    int physicsRadius = 3;
    
    int worldBoundDiscreteMin = 0;
    int worldBoundDiscreteMax = 0;
        
    
    
    //ready to start updating?
    boolean update = false;

    //controls whether we try to generate the drawable entities
    //we want this to be false when in server-only mode
    boolean generateDrawables = false;
    
    
    /**
     * DrawCellManager constructor
     * @param commonWorldData The common world data
     * @param clientTerrainManager The client terrain manager
     * @param discreteX The initial discrete position X coordinate
     * @param discreteY The initial discrete position Y coordinate
     */
    public FluidCellManager(ClientTerrainManager clientTerrainManager, int discreteX, int discreteY, int discreteZ){
        if(Globals.clientState.clientWorldData != null){
            worldBoundDiscreteMax = (int)(Globals.clientState.clientWorldData.getWorldBoundMin().x / ServerTerrainChunk.CHUNK_DIMENSION * 1.0f);
        }
        cells = new HashSet<FluidCell>();
        hasNotRequested = new HashSet<String>();
        drawable = new HashSet<String>();
        undrawable = new HashSet<String>();
        updateable = new HashSet<String>();
        hasRequested = new HashSet<String>();
        
        cellX = discreteX;
        cellY = discreteY;
        cellZ = discreteZ;
        
        program = Globals.terrainShaderProgram;
        
        // drawRadius = Globals.userSettings.getGraphicsPerformanceLODChunkRadius();
        drawStepdownInterval = Globals.gameConfigCurrent.getSettings().getGameplayPhysicsCellRadius();
        physicsRadius = Globals.gameConfigCurrent.getSettings().getGameplayPhysicsCellRadius();
        
        invalidateAllCells();

        update = true;
    }
    
    FluidCellManager(){
        
    }

    public void setCell(Vector3i cellPos){
        cellX = cellPos.x;
        cellY = cellPos.y;
        cellZ = cellPos.z;
    }
    
    void updateUnrequestedCell(){
        if(hasNotRequested.size() > 0){
            String targetKey = hasNotRequested.iterator().next();
            hasNotRequested.remove(targetKey);
            Vector3i worldPos = getVectorFromKey(targetKey);
            // Vector3i vector = getVectorFromKey(targetKey);
            // int currentCellX = cellX - drawRadius + vector.x;
            // int currentCellY = cellY - drawRadius + vector.y;
            // int currentCellZ = cellZ - drawRadius + vector.z;
            
            if(
                worldPos.x >= 0 &&
                worldPos.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                worldPos.y >= 0 &&
                worldPos.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                worldPos.z >= 0 &&
                worldPos.z < Globals.clientState.clientWorldData.getWorldDiscreteSize()
                ){
                // if(!hasRequested.contains(targetKey)){
                    //client should request chunk data from server
                    Globals.clientState.clientConnection.queueOutgoingMessage(TerrainMessage.constructRequestFluidDataMessage(
                        worldPos.x,
                        worldPos.y,
                        worldPos.z
                    ));
                    undrawable.add(targetKey);
                    hasRequested.add(targetKey);
                // }
            }
        }
    }
    
    /**
     * Makes one of the undrawable cells drawable
     */
    void makeCellDrawable(){
        
        if(undrawable.size() > 0){
            String targetKey = undrawable.iterator().next();
            Vector3i worldPos = getVectorFromKey(targetKey);
            if(
                worldPos.x >= 0 &&
                worldPos.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                worldPos.y >= 0 &&
                worldPos.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                worldPos.z >= 0 &&
                worldPos.z < Globals.clientState.clientWorldData.getWorldDiscreteSize()
            ){
                if(containsChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z)){
                    FluidCell cell = FluidCell.generateFluidCell(
                        worldPos,
                        Globals.clientState.clientFluidManager.getChunkDataAtWorldPoint(worldPos.x, worldPos.y, worldPos.z),
                        program
                    );
                    cells.add(cell);
                    keyCellMap.put(targetKey,cell);
                    // undrawable.add(targetKey);
                    undrawable.remove(targetKey);
                    drawable.add(targetKey);
                    //make drawable entity
                    keyCellMap.get(targetKey).generateDrawableEntity();
                }
            }
        }
    }
    
    /**
     * Updates a cell that can be updated
     */
    void updateCellModel(){
        if(updateable.size() > 0){
            String targetKey = updateable.iterator().next();
            updateable.remove(targetKey);
            Vector3i worldPos = this.getVectorFromKey(targetKey);
            if(
                worldPos.x >= 0 &&
                worldPos.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                worldPos.y >= 0 &&
                worldPos.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                worldPos.z >= 0 &&
                worldPos.z < Globals.clientState.clientWorldData.getWorldDiscreteSize()
                    ){
//                if(Math.abs(drawRadius + 1 - targetX) < physicsRadius && Math.abs(drawRadius + 1 - targetY) < physicsRadius){
//                    needsPhysics[targetX][targetY] = true;
//                }
                // int dist = (int)Math.sqrt((targetX - drawRadius)*(targetX - drawRadius) + (targetY - drawRadius) * (targetY - drawRadius)); //Math.abs(targetX - drawRadius) * Math.abs(targetY - drawRadius);
                // int stride = Math.min(commonWorldData.getDynamicInterpolationRatio()/2, Math.max(1, dist / drawStepdownInterval * drawStepdownValue));
                // while(commonWorldData.getDynamicInterpolationRatio() % stride != 0){
                //     stride = stride + 1;
                // }
                if(keyCellMap.get(targetKey) != null){
                    keyCellMap.get(targetKey).destroy();
                    keyCellMap.get(targetKey).generateDrawableEntity();
                }
            }
            drawable.add(targetKey);
        }
    }
    
    /**
     * Checks if the manager has not requested a cell yet
     * @return true if a cell has not been requested yet, false otherwise
     */
    public boolean containsUnrequestedCell(){
        return hasNotRequested.size() > 0;
    }

    /**
     * Gets the number of unrequested cells
     * @return The number of unrequested cells
     */
    public int getUnrequestedSize(){
        return hasNotRequested.size();
    }
    
    /**
     * Checks if the manager has a cell that is not drawable
     * @return true if there is an undrawable cell, false otherwise
     */
    public boolean containsUndrawableCell(){
        return undrawable.size() > 0;
    }

    /**
     * Gets the number of undrawable cells
     * @return The number of undrawable cells
     */
    public int getUndrawableSize(){
        return undrawable.size();
    }
    
    /**
     * Checks if the manager has a cell that is updateable
     * @return true if there is an updateable cell, false otherwise
     */
    public boolean containsUpdateableCell(){
        return updateable.size() > 0;
    }
    
    
    
    /**
     * Transforms real space into cell space
     * @param input The real coordinate
     * @return The cell coordinate
     */
    public int transformRealSpaceToCellSpace(double input){
        return (int)(input / ServerTerrainChunk.CHUNK_DIMENSION);
    }
    
    /**
     * Clears the valid set and adds all keys to invalid set
     */
    public void invalidateAllCells(){
        drawable.clear();
        hasNotRequested.clear();
        clearOutOfBoundsCells();
        queueNewCells();
    }
    
    /**
     * Calculates whether the position of the player has changed and if so, invalidates and cleans up cells accordingly
     */
    private void calculateDeltas(){
        //check if any not requested cells no longer need to be requested
        clearOutOfBoundsCells();
        //check if any cells should be added
        queueNewCells();
    }

    /**
     * Clears all cells outside of draw radius
     */
    private void clearOutOfBoundsCells(){
        Set<FluidCell> cellsToRemove = new HashSet<FluidCell>();
        for(FluidCell cell : cells){
            Vector3d realPos = cell.getRealPos();
            if(Globals.clientState.playerEntity != null && EntityUtils.getPosition(Globals.clientState.playerEntity).distance(realPos) > drawRadius){
                cellsToRemove.add(cell);
            }
        }
        for(FluidCell cell : cellsToRemove){
            cells.remove(cell);
            String key = getCellKey(cell.worldPos.x, cell.worldPos.y, cell.worldPos.z);
            hasNotRequested.remove(key);
            drawable.remove(key);
            undrawable.remove(key);
            updateable.remove(key);
            keyCellMap.remove(key);
            hasRequested.remove(key);
        }
    }

    /**
     * Queues new cells that are in bounds but not currently accounted for
     */
    private void queueNewCells(){
        if(Globals.clientState.playerEntity != null && Globals.clientState.clientWorldData != null){
            Vector3d playerPos = EntityUtils.getPosition(Globals.clientState.playerEntity);
            for(int x = -(int)drawRadius; x < drawRadius; x = x + ChunkData.CHUNK_DATA_SIZE){
                for(int y = -(int)drawRadius; y < drawRadius; y = y + ChunkData.CHUNK_DATA_SIZE){
                    for(int z = -(int)drawRadius; z < drawRadius; z = z + ChunkData.CHUNK_DATA_SIZE){
                        Vector3d newPos = new Vector3d(playerPos.x + x, playerPos.y + y, playerPos.z + z);
                        Vector3i worldPos = new Vector3i(
                            ClientWorldData.convertRealToChunkSpace(newPos.x),
                            ClientWorldData.convertRealToChunkSpace(newPos.y),
                            ClientWorldData.convertRealToChunkSpace(newPos.z)
                        );
                        Vector3d chunkRealSpace = new Vector3d(
                            ClientWorldData.convertChunkToRealSpace(worldPos.x),
                            ClientWorldData.convertChunkToRealSpace(worldPos.y),
                            ClientWorldData.convertChunkToRealSpace(worldPos.z)
                        );
                        if(
                            playerPos.distance(chunkRealSpace) < drawRadius &&
                            worldPos.x >= 0 &&
                            worldPos.x < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            worldPos.y >= 0 &&
                            worldPos.y < Globals.clientState.clientWorldData.getWorldDiscreteSize() &&
                            worldPos.z >= 0 &&
                            worldPos.z < Globals.clientState.clientWorldData.getWorldDiscreteSize()
                            ){
                            String key = getCellKey(
                                worldPos.x,
                                worldPos.y,
                                worldPos.z
                            );
                            if(!keyCellMap.containsKey(key) && !hasNotRequested.contains(key) && !undrawable.contains(key) && !drawable.contains(key) &&
                            !hasRequested.contains(key)){
                                hasNotRequested.add(key);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Updates cells that need updating in this manager
     */
    public void update(){
        Globals.profiler.beginCpuSample("FluidCellManager.update");
        calculateDeltas();
        if(update){
            for(int i = 0; i < UPDATE_COUNT; i++){
                if(containsUnrequestedCell()){
                    updateUnrequestedCell();
                } else if(containsUndrawableCell()){
                    makeCellDrawable();
                } else if(containsUpdateableCell()){
                    updateCellModel();
                }
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Splits a cell key into its constituent coordinates in array format.
     * @param cellKey The cell key to split
     * @return The coordinates in array format
     */
    // private int[] splitKeyToCoordinates(String cellKey){
    //     int[] rVal = new int[3];
    //     String[] components = cellKey.split("_");
    //     for(int i = 0; i < 3; i++){
    //         rVal[i] = Integer.parseInt(components[i]);
    //     }
    //     return rVal;
    // }
    
    public boolean coordsInPhysicsSpace(int worldX, int worldY){
        return worldX <= cellX + physicsRadius && worldX >= cellX - physicsRadius && worldY <= cellY + physicsRadius && worldY >= cellY - physicsRadius;
    }

    public void setGenerateDrawables(boolean generate){
        this.generateDrawables = generate;
    }

    boolean containsChunkDataAtWorldPoint(int worldX, int worldY, int worldZ){
        if(Globals.clientState.clientFluidManager != null){
            return Globals.clientState.clientFluidManager.containsChunkDataAtWorldPoint(worldX,worldY,worldZ);
        }
        return true;
    }

    /**
     * Gets the chunk data at a given point
     * @param worldX The world position x component of the cell
     * @param worldY The world position y component of the cell
     * @param worldZ The world position z component of the cell
     * @return The chunk data at the specified points
     */
    ChunkData getChunkDataAtPoint(int worldX, int worldY, int worldZ){
        return Globals.clientState.clientTerrainManager.getChunkDataAtWorldPoint(worldX,worldY,worldZ,ChunkData.NO_STRIDE);
    }


    /**
     * Gets a unique key for the cell
     * @param worldX The world position x component of the cell
     * @param worldY The world position y component of the cell
     * @param worldZ The world position z component of the cell
     * @return The key
     */
    private String getCellKey(int worldX, int worldY, int worldZ){
        return worldX + "_" + worldY + "_" + worldZ;
    }

    /**
     * Parses a vector3i from the cell key
     * @param key The cell key
     * @return The vector3i containing the components of the cell key
     */
    private Vector3i getVectorFromKey(String key){
        String[] keyComponents = key.split("_");
        return new Vector3i(Integer.parseInt(keyComponents[0]),Integer.parseInt(keyComponents[1]),Integer.parseInt(keyComponents[2]));
    }

    /**
     * Marks a data cell as updateable (can be regenerated with a new model because the underlying data has changed)
     * @param chunkX The chunk x coordinate
     * @param chunkY The chunk y coordinate
     * @param chunkZ The chunk z coordinate
     */
    public void markUpdateable(int chunkX, int chunkY, int chunkZ){
        updateable.add(getCellKey(chunkX, chunkY, chunkZ));
    }


    
//    public 
    
}
