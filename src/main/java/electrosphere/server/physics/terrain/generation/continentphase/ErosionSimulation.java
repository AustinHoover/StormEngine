package electrosphere.server.physics.terrain.generation.continentphase;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import electrosphere.server.physics.terrain.processing.TerrainInterpolator;

/**
 * Performs an erosion simulation that expands the heightmap and simulates drainage across the world
 */
public class ErosionSimulation {

    //The number of threads to farm simulation chunks out to
    private static final int NUMBER_THREADS = 16;


    //The initial heightmap passed into the simulation
    private float[][] startHeightmap;

    //The actually valid data is flipped between primaryHeightmap and alternateHeightmap as the simulation runs
    //so that no more than one allocation has to happen. isStartHeightmap tracks which one contains the
    //valid data.

    //The primary data heightmap
    private float[][] primaryHeightmap;
    //The second array to hold values
    private float[][] alternateHeightmap;
    //Controls which heightmap contains the hot data
    private boolean isPrimaryHeightmap = true;

    //Keeps track of how much water is in a given location
    private float[][] primaryHydrationMap;
    private float[][] alternateHydrationMap;

    //The height at which the ocean begins. No erosion simulation will happen below this point.
    private float oceanLevel;

    //The size of the chunks of simulation that are created
    private int interpolationRatio;

    //Random for seeding worker threads
    Random rand;

    //threadpool for the step phase
    ThreadPoolExecutor threadPool;

    protected ErosionSimulation(float[][] heightmap, float oceanLevel, int interpolationRatio, long randomSeed){
        this.interpolationRatio = interpolationRatio;
        this.startHeightmap = heightmap;
        this.primaryHeightmap = new float[heightmap.length * interpolationRatio][heightmap[0].length * interpolationRatio];
        this.alternateHeightmap = new float[heightmap.length * interpolationRatio][heightmap[0].length * interpolationRatio];
        this.primaryHydrationMap = new float[heightmap.length * interpolationRatio][heightmap[0].length * interpolationRatio];
        this.alternateHydrationMap = new float[heightmap.length * interpolationRatio][heightmap[0].length * interpolationRatio];
        this.oceanLevel = oceanLevel;
        this.rand = new Random(randomSeed);
    }

    /**
     * Runs the erosion simulation
     */
    protected void simulate(){
        setup();
        float totalHydration = getTotalHydration();
        float waterLevelRatio = totalHydration / (this.primaryHydrationMap.length * this.primaryHydrationMap[0].length);
        while(totalHydration > 0){
            CountDownLatch latch = new CountDownLatch(this.startHeightmap.length * this.startHeightmap[0].length);
            for(int x = 0; x < this.startHeightmap.length; x++){
                for(int y = 0; y < this.startHeightmap[0].length; y++){
                    //queue location
                    ErosionJob job = new ErosionJob(
                        primaryHeightmap,
                        alternateHeightmap,
                        primaryHydrationMap,
                        alternateHydrationMap,
                        isPrimaryHeightmap,
                        new Vector(x,y),
                        interpolationRatio,
                        oceanLevel,
                        waterLevelRatio,
                        rand.nextLong(),
                        latch
                    );
                    threadPool.submit(job);
                }
            }
            //await all jobs
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            totalHydration = getTotalHydration();
            waterLevelRatio = totalHydration / (this.primaryHydrationMap.length * this.primaryHydrationMap[0].length);
            //flip primary map
            isPrimaryHeightmap = !isPrimaryHeightmap;
            System.out.println(totalHydration + " - " + waterLevelRatio);
        }
    }

    /**
     * Sets up the simulation
     */
    private void setup(){
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_THREADS);
        //interpolate start heightmap into full heightmap
        int sampleCount = 25;
        int[] xSampleOffset = new int[]{
            -2,-1,0,1,2,
            -2,-1,0,1,2,
            -2,-1,0,1,2,
            -2,-1,0,1,2,
            -2,-1,0,1,2,
        };
        int[] ySampleOffset = new int[]{
            -2,-2,-2,-2,-2,
            -1,-1,-1,-1,-1,
            0,0,0,0,0,
            1,1,1,1,1,
            2,2,2,2,2,
        };
        for(int x = 0; x < startHeightmap.length; x++){
            for(int y = 0; y < startHeightmap[0].length; y++){
                //get the array of samples
                float[][] sample = new float[5][5];
                for(int i = 0; i < sampleCount; i++){
                    if(x + xSampleOffset[i] >= 0 && x + xSampleOffset[i] < startHeightmap.length &&
                    y + ySampleOffset[i] >= 0 && y + ySampleOffset[i] < startHeightmap[0].length){
                        //have to add 2 to xSampleOffset and ySampleOffset to get accurate position in sample array
                        sample[xSampleOffset[i] + 2][ySampleOffset[i] + 2] = startHeightmap[x + xSampleOffset[i]][y + ySampleOffset[i]];
                    } else {
                        sample[xSampleOffset[i] + 2][ySampleOffset[i] + 2] = 0;
                    }
                }
                float[][] interpolatedValues = TerrainInterpolator.getBicubicInterpolatedChunk(sample, interpolationRatio);
                for(int m = 0; m < interpolationRatio; m++){
                    for(int n = 0; n < interpolationRatio; n++){
                        primaryHeightmap[x * interpolationRatio + m][y * interpolationRatio + n]   = interpolatedValues[m][n];
                        alternateHeightmap[x * interpolationRatio + m][y * interpolationRatio + n] = interpolatedValues[m][n];
                        //seed initial hydration map
                        primaryHydrationMap[x * interpolationRatio + m][y * interpolationRatio + n] = 1;
                        alternateHydrationMap[x * interpolationRatio + m][y * interpolationRatio + n] = 1;
                    }
                }
            }
        }
    }

    /**
     * Gets the data resulting from the erosion simulation
     * @return The data
     */
    protected float[][] getData(){
        if(isPrimaryHeightmap){
            return primaryHeightmap;
        } else {
            return alternateHeightmap;
        }
    }

    /**
     * Returns the total hydration of the currently active map
     * @return The total hydration
     */
    private float getTotalHydration(){
        float sum = 0;
        float highestElevation = 0;
        float highestHydration = 0;
        if(isPrimaryHeightmap){
            for(int x = 0; x < primaryHydrationMap.length; x++){
                for(int y = 0; y < primaryHydrationMap[0].length; y++){
                    sum = sum + primaryHydrationMap[x][y];
                    if(primaryHeightmap[x][y] > highestElevation){
                        highestElevation = primaryHeightmap[x][y];
                    }
                    if(primaryHydrationMap[x][y] > highestHydration){
                        highestHydration = primaryHydrationMap[x][y];
                    }
                }
            }
        } else {
            for(int x = 0; x < alternateHydrationMap.length; x++){
                for(int y = 0; y < alternateHydrationMap[0].length; y++){
                    sum = sum + alternateHydrationMap[x][y];
                    if(alternateHeightmap[x][y] > highestElevation){
                        highestElevation = alternateHeightmap[x][y];
                    }
                    if(alternateHydrationMap[x][y] > highestHydration){
                        highestHydration = alternateHydrationMap[x][y];
                    }
                }
            }
        }
        System.out.println("Highest elev: " + highestElevation);
        System.out.println("Highest hydra: " + highestHydration);
        return sum;
    }

    /**
     * A runnable job of simulation erosion on a single chunk
     */
    static class ErosionJob implements Runnable {

        static final int MAX_HYDRATION = 25;

        float primaryHeightmap[][];
        float alternateHeightmap[][];
        float[][] primaryHydrationMap;
        float[][] alternateHydrationMap;
        boolean usePrimaryMaps;
        Vector targetLocation;
        int interpolationRatio;
        float oceanLevel;
        float waterLevelRatio;
        Random rand;
        CountDownLatch latch;

        protected ErosionJob(
            float[][] primaryHeightmap,
            float[][] alternateHeightmap,
            float[][] primaryHydrationMap,
            float[][] alternateHydrationMap,
            boolean usePrimaryMaps,
            Vector targetLocation,
            int interpolationRatio,
            float oceanLevel,
            float waterLevelRatio,
            long randomSeed,
            CountDownLatch latch
        ){
            this.primaryHeightmap = primaryHeightmap;
            this.alternateHeightmap = alternateHeightmap;
            this.primaryHydrationMap = primaryHydrationMap;
            this.alternateHydrationMap = alternateHydrationMap;
            this.usePrimaryMaps = usePrimaryMaps;
            this.targetLocation = targetLocation;
            this.interpolationRatio = interpolationRatio;
            this.oceanLevel = oceanLevel;
            this.waterLevelRatio = waterLevelRatio;
            this.rand = new Random(randomSeed);
            this.latch = latch;
        }

        static final int[] offsetX = new int[]{
            -1,1,0,0
        };
        static final int[] offsetY = new int[]{
            0,0,-1,1
        };
        @Override
        public void run() {
            for(int x = 0; x < interpolationRatio; x++){
                for(int y = 0; y < interpolationRatio; y++){
                    float currentHeight = 0;
                    int targetX = targetLocation.x * interpolationRatio + x;
                    int targetY = targetLocation.y * interpolationRatio + y;
                    if(usePrimaryMaps){
                        currentHeight = primaryHeightmap[targetX][targetY];
                        float oldHydration = primaryHydrationMap[targetX][targetY];
                        float newHydration = 0;
                        float highestEncounteredElevation = 0;
                        int numberHydrationHits = 0;
                        // if(targetX == 728 && targetY == 732){
                        //     System.out.println("asdf");
                        // }
                        //calculate total hydration
                        for(int i = 0; i < 4; i++){
                            if(targetX + offsetX[i] >= 0 && targetX + offsetX[i] < primaryHeightmap.length &&
                            targetY + offsetY[i] >= 0 && targetY + offsetY[i] < primaryHeightmap[0].length
                            ){
                                if(currentHeight < primaryHeightmap[targetX + offsetX[i]][targetY + offsetY[i]]){
                                    numberHydrationHits++;
                                    float sourceHydration = primaryHydrationMap[targetX + offsetX[i]][targetY + offsetY[i]];
                                    float percentageFromSource = calculatePercentageRunoff(
                                        targetX + offsetX[i],
                                        targetY + offsetY[i],
                                        targetX,
                                        targetY,
                                        primaryHeightmap
                                    );
                                    newHydration = newHydration + sourceHydration * percentageFromSource;
                                } else if(currentHeight == primaryHeightmap[targetX + offsetX[i]][targetY + offsetY[i]] && rand.nextInt() % alternateHydrationMap[targetX][targetY] > 5) {
                                    // numberHydrationHits++;
                                    // alternateHydrationMap[targetX][targetY] = alternateHydrationMap[targetX][targetY] + primaryHydrationMap[targetX + offsetX[i]][targetY + offsetY[i]];
                                } else {
                                    if(primaryHeightmap[targetX + offsetX[i]][targetY + offsetY[i]] > highestEncounteredElevation){
                                        highestEncounteredElevation = primaryHeightmap[targetX + offsetX[i]][targetY + offsetY[i]];
                                    }
                                }
                            }
                        }
                        //calculate shear due to hydration
                        float shear = Math.abs(newHydration - oldHydration);
                        if(waterLevelRatio > 1.0f){
                            shear = alternateHydrationMap[targetX][targetY] / waterLevelRatio;
                        }
                        //clamp hydration value
                        alternateHydrationMap[targetX][targetY] = Math.min(newHydration,MAX_HYDRATION);
                        if(numberHydrationHits == 4){
                            alternateHydrationMap[targetX][targetY] = 0;
                        }
                        if(currentHeight > oceanLevel && numberHydrationHits < 4){
                            alternateHeightmap[targetX][targetY] = Math.max(highestEncounteredElevation,currentHeight - 0.1f / shear);
                        } else {
                            //if below sea level, delete hydration
                            alternateHeightmap[targetX][targetY] = currentHeight;
                            alternateHydrationMap[targetX][targetY] = 0;
                        }
                    } else {
                        // if(targetX == 728 && targetY == 732){
                        //     System.out.println("asdf");
                        // }
                        currentHeight = alternateHeightmap[targetX][targetY];
                        float oldHydration = primaryHydrationMap[targetX][targetY];
                        float newHydration = 0;
                        float highestEncounteredElevation = 0;
                        int numberHydrationHits = 0;
                        //check each neighbor to see who we can interact with
                        for(int i = 0; i < 4; i++){
                            if(targetX + offsetX[i] >= 0 && targetX + offsetX[i] < primaryHeightmap.length &&
                            targetY + offsetY[i] >= 0 && targetY + offsetY[i] < primaryHeightmap[0].length
                            ){
                                //if the neighbor is taller, pull hydration from it
                                if(currentHeight < alternateHeightmap[targetX + offsetX[i]][targetY + offsetY[i]]){
                                    numberHydrationHits++;
                                    float sourceHydration = primaryHydrationMap[targetX + offsetX[i]][targetY + offsetY[i]];
                                    float percentageFromSource = calculatePercentageRunoff(
                                        targetX + offsetX[i],
                                        targetY + offsetY[i],
                                        targetX,
                                        targetY,
                                        alternateHeightmap
                                    );
                                    newHydration = newHydration + sourceHydration * percentageFromSource;
                                } else if(currentHeight == alternateHeightmap[targetX + offsetX[i]][targetY + offsetY[i]] && rand.nextInt() % primaryHydrationMap[targetX][targetY] > 5) {
                                    //if the neighbor is the same height, have a chance to pull hydration from it
                                    // numberHydrationHits++;
                                    // primaryHydrationMap[targetX][targetY] = primaryHydrationMap[targetX][targetY] + alternateHydrationMap[targetX + offsetX[i]][targetY + offsetY[i]];
                                } else {
                                    //if the neighbor is smaller, but taller than the tallest neighbor currently encountered, record its height
                                    if(alternateHeightmap[targetX + offsetX[i]][targetY + offsetY[i]] > highestEncounteredElevation){
                                        highestEncounteredElevation = alternateHeightmap[targetX + offsetX[i]][targetY + offsetY[i]];
                                    }
                                }
                            }
                        }
                        //calculate shear due to hydration
                        float shear = Math.abs(newHydration - oldHydration);
                        //bound the hydration by the total hydration of the map
                        //This keeps the hydration from explosively increasing unbounded
                        if(waterLevelRatio > 1.0f){
                            shear = alternateHydrationMap[targetX][targetY] / waterLevelRatio;
                        }
                        //clamp hydration value
                        primaryHydrationMap[targetX][targetY] = Math.min(newHydration,MAX_HYDRATION);
                        //If every neighbor is taller, this is a local minimum and should be treated as a lake (removes all hydration)
                        if(numberHydrationHits == 4){
                            primaryHydrationMap[targetX][targetY] = 0;
                        }
                        if(currentHeight > oceanLevel && numberHydrationHits < 4){
                            primaryHeightmap[targetX][targetY] = Math.max(highestEncounteredElevation,currentHeight - 0.1f / shear);
                        } else {
                            //if below sea level, delete hydration
                            primaryHeightmap[targetX][targetY] = currentHeight;
                            primaryHydrationMap[targetX][targetY] = 0;
                        }
                    }
                }
            }
            latch.countDown();
        }

        /**
         * Basically calculates how much runoff should go from source to destination. The percentage is based on how much of the total drop to all neighbors destination would be.
         * @param sourceX The x coordinate of the source of the water
         * @param sourceY The y coordinate of the source of the water
         * @param destinationX The x coordinate of the destination of the water
         * @param destinationY The y coordinate of the destination of the water
         * @param elevationMapToCheck The elevation map to use as reference
         * @return The percentage of water to pull from source to destination
         */
        private float calculatePercentageRunoff(int sourceX, int sourceY, int destinationX, int destinationY, float[][] elevationMapToCheck){
            //the difference between the source and destination points in elevation
            float heightDifferencToDestination = elevationMapToCheck[sourceX][sourceY] - elevationMapToCheck[destinationX][destinationY];
            //the sum difference between source and all its smaller neighbors
            float totalHeightDifference = 0;
            for(int i = 0; i < 4; i++){
                if(sourceX + offsetX[i] >= 0 && sourceX + offsetX[i] < primaryHeightmap.length &&
                sourceY + offsetY[i] >= 0 && sourceY + offsetY[i] < primaryHeightmap[0].length
                ){
                    if(elevationMapToCheck[sourceX][sourceY] > elevationMapToCheck[sourceX + offsetX[i]][sourceY + offsetY[i]]){
                        totalHeightDifference = totalHeightDifference + elevationMapToCheck[sourceX][sourceY] - elevationMapToCheck[sourceX + offsetX[i]][sourceY + offsetY[i]];
                    }
                }
            }
            return heightDifferencToDestination / totalHeightDifference;
        }

        /**
         * Checks if a given location has all neighbors that are the same height
         * @param sourceX The location to check x coordinate
         * @param sourceY The location to check y coordinate
         * @param elevationMapToCheck The elevation map to reference
         * @return True if all neighbors are flat, false otherwise
         */
        protected boolean neighborIsFlat(int sourceX, int sourceY, float[][] elevationMapToCheck){
            for(int i = 0; i < 4; i++){
                if(sourceX + offsetX[i] >= 0 && sourceX + offsetX[i] < primaryHeightmap.length &&
                sourceY + offsetY[i] >= 0 && sourceY + offsetY[i] < primaryHeightmap[0].length
                ){
                    if(elevationMapToCheck[sourceX][sourceY] != elevationMapToCheck[sourceX + offsetX[i]][sourceY + offsetY[i]]){
                        return false;
                    }
                }
            }
            return true;
        }
    }

    void createVisualization(){
        
    }

}
