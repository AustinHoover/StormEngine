package electrosphere.server.physics.fluid.models;

import electrosphere.util.annotation.Exclude;

public class FluidModel {
    
    int dynamicInterpolationRatio;
    float interpolationRandomDampener = 0.4f;
    
    int discreteArrayDimension;
    @Exclude
    private float[][] elevation;
    
    float realMountainThreshold;
    float realOceanThreshold;
    
    FluidModel() {
    }
    
    public FluidModel(
            int dimension,
            int dynamicInterpolationRatio
    ){
        
        this.dynamicInterpolationRatio = dynamicInterpolationRatio;
        this.discreteArrayDimension = dimension;
    }
    
    public static FluidModel constructFluidModel(int dimension, int dynamicInterpolationRatio){
        FluidModel rVal = new FluidModel();
        rVal.discreteArrayDimension = dimension;
        rVal.dynamicInterpolationRatio = dynamicInterpolationRatio;
        return rVal;
    }
    
    public float[][] getElevation(){
        return elevation;
    }
    
    public void setInterpolationRandomDampener(float f){
        interpolationRandomDampener = f;
    }
    
    /**
     * Dynamically interpolates a chunk of a specific size from the pre-existing elevation map
     * @param x The x position on the elevation map to get a chunk from
     * @param y The y position on the elevation map to get a chunk from
     * @return Dynamically interpolated float array of elevations of chunk
     */
    public float[][] getElevationForChunk(int x, int y){
        
        //this is what we intend to return from the function
        float[][] rVal = new float[dynamicInterpolationRatio][dynamicInterpolationRatio];
        
        /*
        So we're looking at chunk x,y
        
        if this is our grid:
        
            4  0.1    0.2     0.3     0.4
        ^
        |   3  0.1    0.2     0.3     0.4
        |
        |   2  0.1    0.2     0.3     0.4
        
        x   1  0.1    0.2     0.3     0.4
        
              0      1       2       3
        
              y  ---- >
        
        say we're looking at x=2,y=1
        
        "macroValues" should contain the values for bounds x = [1,3] and y = [0,2]
        
        the goal is to have the "center" of the output chunk have the value the
        elevation grid at x=2,y=1
        
        */
        
        
        //set macroValues
        float[][] macroValues = getMacroValuesAtPosition(x,y);
        
        
        
        
        int halfLength = dynamicInterpolationRatio/2;
        
        /*
        
        Four quadrants we're generating
        
             _____________________
            |1         |2         |
            |          |          |
            |          |          |
            |          |          |
            |__________|__________|
            |3         |4         |
            |          |          |
            |          |          |
            |__________|__________|
            
        First set of loops is quadrant 1
        then quadrant 2
        then quadrant 3
        then quadrant 4
        
        */
        
        int outXOffset = 0;
        int outYOffset = 0;
        
        for(int i = 0; i < halfLength; i++){
            for(int j = 0; j < halfLength; j++){
                rVal[i+outXOffset][j+outYOffset] = 
                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[0][0] +
                        (1.0f * (0          - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][0] +
                        (1.0f * (halfLength - i) * (0          - j))/(halfLength * halfLength) * macroValues[0][1] +
                        (1.0f * (0          - i) * (0          - j))/(halfLength * halfLength) * macroValues[1][1]
                        ;
            }
        }
        
        outXOffset = halfLength;
        for(int i = 0; i < halfLength; i++){
            for(int j = 0; j < halfLength; j++){
                rVal[i+outXOffset][j+outYOffset] = 
                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][0] +
                        (1.0f * (0          - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[2][0] +
                        (1.0f * (halfLength - i) * (0          - j))/(halfLength * halfLength) * macroValues[1][1] +
                        (1.0f * (0          - i) * (0          - j))/(halfLength * halfLength) * macroValues[2][1]
                        ;
            }
        }
        
        outXOffset = 0;
        outYOffset = halfLength;
        for(int i = 0; i < halfLength; i++){
            for(int j = 0; j < halfLength; j++){
                rVal[i+outXOffset][j+outYOffset] = 
                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[0][1] +
                        (1.0f * (0          - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][1] +
                        (1.0f * (halfLength - i) * (0          - j))/(halfLength * halfLength) * macroValues[0][2] +
                        (1.0f * (0          - i) * (0          - j))/(halfLength * halfLength) * macroValues[1][2]
                        ;
            }
        }
        
        outXOffset = halfLength;
        for(int i = 0; i < halfLength; i++){
            for(int j = 0; j < halfLength; j++){
                rVal[i+outXOffset][j+outYOffset] = 
                        (1.0f * (halfLength - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[1][1] +
                        (1.0f * (0          - i) * (halfLength - j))/(halfLength * halfLength) * macroValues[2][1] +
                        (1.0f * (halfLength - i) * (0          - j))/(halfLength * halfLength) * macroValues[1][2] +
                        (1.0f * (0          - i) * (0          - j))/(halfLength * halfLength) * macroValues[2][2]
                        ;
            }
        }
        
        
        return rVal;
    }
    
    
    /*
        So we're looking at chunk x,y
        
        if this is our grid:
        
            4  0.1    0.2     0.3     0.4
        ^
        |   3  0.1    0.2     0.3     0.4
        |
        |   2  0.1    0.2     0.3     0.4
        
        x   1  0.1    0.2     0.3     0.4
        
              0      1       2       3
        
              y  ---- >
        
        say we're looking at x=2,y=1
        
        "macroValues" should contain the values for x = [1,3] and y = [0,2]
        
        the goal is to have the "center" of the output chunk have the value the
        elevation grid at x=2,y=1
        
        */
    
    public float[][] getMacroValuesAtPosition(int x, int y){
        
        float[][] rVal = new float[3][3];
        rVal[1][1] = elevation[x][y];
        if(x - 1 >= 0){
            rVal[0][1] = elevation[x-1][y];
            if(y - 1 >= 0){
                rVal[0][0] = elevation[x-1][y-1];
            } else {
                rVal[0][0] = 0;
            }
            if(y + 1 < discreteArrayDimension){
                rVal[0][2] = elevation[x-1][y+1];
            } else {
                rVal[0][2] = 0;
            }
        } else { 
            rVal[0][0] = 0;
            rVal[0][1] = 0;
            rVal[0][2] = 0;
        }
        if(x + 1 < discreteArrayDimension){
            rVal[2][1] = elevation[x+1][y];
            if(y - 1 >= 0){
                rVal[2][0] = elevation[x+1][y-1];
            } else {
                rVal[2][0] = 0;
            }
            if(y + 1 < discreteArrayDimension){
                rVal[2][2] = elevation[x+1][y+1];
            } else {
                rVal[2][2] = 0;
            }
        } else {
            rVal[2][0] = 0;
            rVal[2][1] = 0;
            rVal[2][2] = 0;
        }
        if(y - 1 >= 0){
            rVal[1][0] = elevation[x][y-1];
        } else {
            rVal[1][0] = 0;
        }
        if(y + 1 < discreteArrayDimension){
            rVal[1][2] = elevation[x][y+1];
        } else {
            rVal[1][2] = 0;
        }
        
        return rVal;
    }
    
    
    
    
    
    public float[][] getRad5MacroValuesAtPosition(int x, int y){
        
        float[][] rVal = new float[5][5];
        for(int i = -2; i < 3; i++){
            for(int j = -2; j < 3; j++){
                if(x + i >= 0 && x + i < discreteArrayDimension && y + j >= 0 && y + j < discreteArrayDimension){
                    rVal[i+2][j+2] = elevation[x+i][y+j];
                } else {
                    rVal[i+2][j+2] = 0;
                }
            }
        }
        
        return rVal;
    }
    
    
    /*
        So we're looking at chunk x,y
        
        if this is our grid:
        
            4  0.1    0.2     0.3     0.4
        ^
        |   3  0.1    0.2     0.3     0.4
        |
        |   2  0.1    0.2     0.3     0.4
        
        x   1  0.1    0.2     0.3     0.4
        
              0      1       2       3
        
              y  ---- >
        
        say we're looking at x=2,y=1
        
        "macroValues" should contain the values for x = [1,3] and y = [0,2]
        
        the goal is to have the "center" of the output chunk have the value the
        elevation grid at x=2,y=1
        
        */
    
    
    public float getRandomDampener(){
        return interpolationRandomDampener;
    }
    
    public int getDynamicInterpolationRatio(){
        return dynamicInterpolationRatio;
    }

    public float getRealMountainThreshold() {
        return realMountainThreshold;
    }

    public float getRealOceanThreshold() {
        return realOceanThreshold;
    }
    
    public String getModificationKey(int x, int y, int z){
        return x + "_" + y + "_" + z;
    }
    
//     public void addModification(TerrainModification modification){
//         String key = getModificationKey(modification.getWorldPos().x,modification.getWorldPos().y,modification.getWorldPos().z);
//         ModificationList list;
//         if(!modifications.containsKey(key)){
//             list = new ModificationList();
//             modifications.put(key, list);
//         } else {
//             list = modifications.get(key);
//         }
//         list.addModification(modification);
//     }
    
//     public boolean containsModificationsAtCoord(int worldX, int worldY, int worldZ){
//         return modifications.containsKey(getModificationKey(worldX, worldY, worldZ));
//     }
    
//     public ModificationList getModifications(int worldX, int worldY, int worldZ){
// //        System.out.println("Got modifications at " + worldX + " " + worldY);
//         return modifications.get(getModificationKey(worldX, worldY, worldZ));
//     }

    /**
     * Sets the elevation array (For instance when read from save file on loading a save)
     * @param elevation The elevation array to set to
     */
    public void setElevationArray(float[][] elevation){
        this.elevation = elevation;
    }
    
}
