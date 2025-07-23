package electrosphere.server.physics.terrain.generation.continentphase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Core continent phase terrain generator
 */
class TectonicSimulation {

    //size of a parallelized chunk
    static final int PARALLEL_CHUNK_SIZE = 32;
    //number of threads for threadpool
    static final int THREAD_POOL_COUNT = 16;

    //the dimensions of the map
    int DIMENSION = 200;
    int[][] asthenosphereHeat;
    int[][] rockHardness;
    int[][] elevation;
    int[][] smoothedElevation;
    int currentElev[][];
    int newElevation[][];
    //currents used for pushing terrain elevation around the map
    Vector[][] currents;
    //hotspots that thrust rock up from the ocean floor
    List<Hotspot> spots = new ArrayList<Hotspot>();
    int time = 0;
    int lifespan = 75000;

    Random rand;

    //thread pool for parallelized force calculation
    ThreadPoolExecutor threadPool;
    
    /**
     * Constructor
     * @param seed Seed for random
     */
    protected TectonicSimulation(long seed){
        this.rand = new Random(seed);
        threadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(THREAD_POOL_COUNT);
    }
    
    /**
     * Sets the data width
     * @param newDim The dimension of the data
     */
    protected void setDimension(int newDim){
        DIMENSION = newDim;
        if(DIMENSION % PARALLEL_CHUNK_SIZE != 0){
            //this requirement is for parallelization purposes
            throw new Error("DIMENSION MUST BE A MULTIPLE OF 16!");
        }
    }
    
    /**
     * Sets the simulation lifespan for the Continent Phase
     * @param newLifespan The lifespan in units of simulation frames
     */
    protected void setLifespan(int newLifespan){
        lifespan = newLifespan;
    }
    
    /**
     * Runs the continent phase simulation. Blocks until completed
     */
    protected void run(){
        allocateData();
        
        
        
        long lastTime = System.currentTimeMillis();
        
        //construct convection cells prior to simulation
        constructConvectionCells();
        

        //main simulation
        while(true){
            time++;
            simulateHotspots();
            heatToElevation();
            applyVectorsToElevationParallel();
            calculateSmoothedElevations();

            //            try {
//                TimeUnit.MILLISECONDS.sleep(1);
//            } catch (InterruptedException ex) {
//            }
            if(time % 500 == 0) {
                long new_Time = System.currentTimeMillis();
                long time_Delta = new_Time - lastTime;
                lastTime = new_Time;
                System.out.println("Progress: " + time + "/" + lifespan + " ETA: " + (time_Delta * (lifespan - time) / 1000 / 500) + "S");
            }
            if(time > lifespan){
                break;
            }
        }

        //next subphase is to find large areas without continents and place ones there
        //the terrain added in this next phase will be made with a more quick and dirty implementation
        

        //shutdown threadpool
        threadPool.shutdown();
    }
    
    /**
     * Gets the raw terrain
     * @return The raw terrain
     */
    protected int[][] getTerrain(){
        return elevation;
    }
    
    /**
     * Gets the terrain smoothed
     * @return The terrain smoothed
     */
    protected int[][] getTerrainSmoothed(){
        return smoothedElevation;
    }


    /**
     * Allocates all arrays generated based on the dimension provided
     */
    private void allocateData(){
        asthenosphereHeat = new int[DIMENSION][DIMENSION];
        elevation = new int[DIMENSION][DIMENSION];
        smoothedElevation = new int[DIMENSION][DIMENSION];
        newElevation = new int[DIMENSION][DIMENSION];
        currents = new Vector[DIMENSION][DIMENSION];
        currentElev = new int[DIMENSION][DIMENSION];
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                currents[x][y] = new Vector();
            }
        }
    }
    
    /**
     * If the asthenosphere is sufficiently hot, increases elevation of position
     */
    private void heatToElevation(){
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                if(asthenosphereHeat[x][y] > 25){
                    if(electrosphere.server.physics.terrain.generation.continentphase.Utilities.random_Integer(1, 10, rand) == 10){
                        elevation[x][y] = elevation[x][y] + 1;
                        if(elevation[x][y] > 100){
                            elevation[x][y] = 100;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Constructs convection cells in the force vector field
     */
    private void constructConvectionCells(){
        //controls whether the cell rotates clockwise or couterclockwise
        boolean isCellType1 = false;
        //one fourth of the width of the data set
        int fourth = DIMENSION / 4;
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                //the current position RELATIVE to the center point of the current convection cell center
                int normalizedX = x;
                int normalizedY = y;
                
                //determine relative position and whether convection cell type one or two
                if(y < fourth || (y < fourth * 3 && y > (fourth * 2) - 1)){
                    isCellType1 = true;
                    if(normalizedY > fourth){
                        normalizedY = normalizedY - fourth * 2;
                    }
                } else {
                    isCellType1 = false;
                    if(normalizedY > fourth * 2 + 1){
                        normalizedY = normalizedY - fourth * 3;
                    } else {
                        normalizedY = normalizedY - fourth;
                    }
                }
                while(normalizedX > fourth){
                    normalizedX = normalizedX - fourth;
                }
                if(normalizedX < 0){
                    normalizedX = 0;
                }
                if(normalizedY < 0){
                    normalizedY = 0;
                }

                //one eighth of the width of the data set
                int eigth = fourth / 2;
                //Moves the relative position to be in its correct eighth
                normalizedY = normalizedY - eigth;
                normalizedX = normalizedX - eigth;

                //calculates the distance from convection cell center to the relative position
                float magnitude = (float)Math.sqrt(Math.pow(normalizedY, 2) + Math.pow(normalizedX, 2));

                //If the distance is small enough we stretch it along the X axis ... ?
                if(magnitude < fourth / 10){
                    normalizedX = normalizedX + fourth / 10;
                    magnitude = (float)Math.sqrt(Math.pow(normalizedY, 2) + Math.pow(normalizedX, 2));
                }

                //calculates the angle of the point relative to convection cell center
                double offsetAngle = Math.atan2(normalizedY / magnitude, normalizedX / magnitude);
                if(offsetAngle < 0){
                    offsetAngle = offsetAngle + Math.PI * 2;
                }
                
                //rotate based on cell type
                if(isCellType1){
                    offsetAngle = offsetAngle + Math.PI / 2;
                } else {
                    offsetAngle = offsetAngle - Math.PI / 2;
                }
                //normalize
                while(offsetAngle > Math.PI * 2){
                    offsetAngle = offsetAngle - Math.PI * 2;
                }
                while(offsetAngle < 0){
                    offsetAngle = offsetAngle + Math.PI * 2;
                }
                //Lastly, actually set the force vector
                currents[x][y].x = (int)(99 * Math.cos(offsetAngle));
                currents[x][y].y = (int)(99 * Math.sin(offsetAngle));
            }
        }
    }
    
    /**
     * Moves the terrain around based on the vector field
     */
    protected void applyVectorsToElevation(){
        //allocate new elevation array
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                newElevation[x][y] = 0;
                currentElev[x][y] = elevation[x][y];
            }
        }
        //transfer terrain
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                boolean transfer = false;
                if (Utilities.random_Integer(1, 50, rand) == 1) {
                    transfer = true;
                }
                int transfer_goal;
                if(Utilities.random_Integer(1, 2, rand)==1){
                    transfer_goal = Utilities.random_Integer(20, 60, rand);
                } else {
                    transfer_goal = Utilities.random_Integer(0, 60, rand);
                }
                if(Utilities.random_Integer(1, 2, rand)==1){
                if (currents[x][y].x >= 0) {
                        if (transfer) {
                            if (x + 1 < DIMENSION) {
                                while(newElevation[x + 1][y] + currentElev[x + 1][y] < 99 && currentElev[x][y] > transfer_goal){
                                    newElevation[x + 1][y]++;
                                    currentElev[x][y]--;
                                }
                            } else {
                            }
                        }
                    } else {
                        if (transfer) {
                            if (x - 1 >= 0) {
                                while(newElevation[x - 1][y] + currentElev[x - 1][y] < 99 && currentElev[x][y] > transfer_goal){
                                    newElevation[x - 1][y]++;
                                    currentElev[x][y]--;
                                }
                            } else {
                            }
                        }
                    }
                } else {
                    if (currents[x][y].y >= 0) {
                        if (transfer) {
                            if (y + 1 < DIMENSION) {                                   //  V    REPLACE THIS WITH GOAL
                                while(newElevation[x][y + 1] + currentElev[x][y + 1] < 99 && currentElev[x][y] > transfer_goal){
                                    newElevation[x][y + 1]++;
                                    currentElev[x][y]--;
                                }
                            } else {
                            }
                        }
                    } else {
                        if (transfer) {
                            if (y - 1 >= 0) {
                                    while(newElevation[x][y - 1] + currentElev[x][y - 1] < 99 && currentElev[x][y] > transfer_goal){
                                        newElevation[x][y - 1]++;
                                    currentElev[x][y]--;
                                }
                            } else {
                            }
                        }
                    }
                }
            }
        }
        //move data from temporary array to main array
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                newElevation[x][y] = newElevation[x][y] + currentElev[x][y];
                while(newElevation[x][y] > 99){
                    newElevation[x][y] = 99;
                }
                elevation[x][y] = newElevation[x][y];
            }
        }
    }
    
    /**
     * Applies a smooth kernel to the terrain data
     */
    private void calculateSmoothedElevations(){
        int[][] buffer = new int[DIMENSION][DIMENSION];
        for(int x = 1; x < DIMENSION - 2; x++){
            for(int y = 1; y < DIMENSION - 2; y++){
                buffer[x][y] = elevation[x][y] * 4 * elevation[x+1][y] * 2 + elevation[x-1][y] * 2 + elevation[x][y+1] * 2 +
                        elevation[x][y-1] * 2 + elevation[x+1][y+1] + elevation[x+1][y-1] + elevation[x-1][y+1] + elevation[x-1][y-1];
                buffer[x][y] = (int)(buffer[x][y] / 16.0);
                while(buffer[x][y] > 100){
                    buffer[x][y] = buffer[x][y]/2;
                }
                smoothedElevation[x][y] = buffer[x][y];
            }
        }
        for(int x = 1; x < DIMENSION - 2; x++){
            for(int y = 1; y < DIMENSION - 2; y++){
                buffer[x][y] = smoothedElevation[x][y] * 4 * smoothedElevation[x+1][y] * 2 + smoothedElevation[x-1][y] * 2 + smoothedElevation[x][y+1] * 2 +
                smoothedElevation[x][y-1] * 2 + smoothedElevation[x+1][y+1] + smoothedElevation[x+1][y-1] + smoothedElevation[x-1][y+1] + smoothedElevation[x-1][y-1];
                buffer[x][y] = (int)(buffer[x][y] / 16.0);
                while(buffer[x][y] > 100){
                    buffer[x][y] = buffer[x][y]/2;
                }
                smoothedElevation[x][y] = buffer[x][y];
            }
        }
    }

    /**
     * simulates the hotspot logic
     */
    private void simulateHotspots(){
        if(spots.size() >= 1){
            List<Hotspot> to_Remove = new ArrayList<Hotspot>();
            Iterator<Hotspot> spot_Iterator = spots.iterator();
            while(spot_Iterator.hasNext()){
                Hotspot current_Spot = spot_Iterator.next();
                if(current_Spot.life_current >= current_Spot.life_max){
                    to_Remove.add(current_Spot);
                }
            }
            spot_Iterator = to_Remove.iterator();
            while(spot_Iterator.hasNext()){
                Hotspot current_Spot = spot_Iterator.next();
                spots.remove(current_Spot);
            }
        }
        if(spots.size() < 5){
            spots.add(new Hotspot(
                Utilities.random_Integer(0, DIMENSION - 1, rand),
                Utilities.random_Integer(0, DIMENSION - 1, rand),
                Utilities.random_Integer(6000, 10000, rand),
                Utilities.random_Integer(3, 5, rand),
                this));
        }
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                asthenosphereHeat[x][y] = 0;
            }
        }
        if(spots.size() >= 1){
            Iterator<Hotspot> spot_Iterator = spots.iterator();
            while(spot_Iterator.hasNext()){
                Hotspot current_Spot = spot_Iterator.next();
                current_Spot.simulate();
            }
        }
    }


    /**
     * Fills in the gaps not covered by the main chunks
     */
    private void applyVectorToElevationGaps(){
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                if(x % 16 == 0 || x % 16 == 1 || y % 16 == 0 || y % 16 == 1){
                    boolean transfer = false;
                    if (Utilities.random_Integer(1, 50, rand) == 1) {
                        transfer = true;
                    }
                    int transfer_goal;
                    if(Utilities.random_Integer(1, 2, rand)==1){
                        transfer_goal = Utilities.random_Integer(20, 60, rand);
                    } else {
                        transfer_goal = Utilities.random_Integer(0, 60, rand);
                    }
                    if(Utilities.random_Integer(1, 2, rand)==1){
                        if (currents[x][y].x >= 0) {
                            if (transfer) {
                                if (x + 1 < DIMENSION) {
                                    while(newElevation[x + 1][y] + currentElev[x + 1][y] < 99 && currentElev[x][y] > transfer_goal){
                                        newElevation[x + 1][y]++;
                                        currentElev[x][y]--;
                                    }
                                } else {
                                }
                            }
                        } else {
                            if (transfer) {
                                if (x - 1 >= 0) {
                                    while(newElevation[x - 1][y] + currentElev[x - 1][y] < 99 && currentElev[x][y] > transfer_goal){
                                        newElevation[x - 1][y]++;
                                        currentElev[x][y]--;
                                    }
                                } else {
                                }
                            }
                        }
                    } else {
                        if (currents[x][y].y >= 0) {
                            if (transfer) {
                                if (y + 1 < DIMENSION) {                                   //  V    REPLACE THIS WITH GOAL
                                    while(newElevation[x][y + 1] + currentElev[x][y + 1] < 99 && currentElev[x][y] > transfer_goal){
                                        newElevation[x][y + 1]++;
                                        currentElev[x][y]--;
                                    }
                                } else {
                                }
                            }
                        } else {
                            if (transfer) {
                                if (y - 1 >= 0) {
                                        while(newElevation[x][y - 1] + currentElev[x][y - 1] < 99 && currentElev[x][y] > transfer_goal){
                                            newElevation[x][y - 1]++;
                                        currentElev[x][y]--;
                                    }
                                } else {
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    //latch for synchronizing parallel force vector computation
    CountDownLatch latch;
    /**
     * Moves the terrain around based on the vector field, parallelized
     */
    private void applyVectorsToElevationParallel(){
        //allocate new elevation array
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                newElevation[x][y] = 0;
                currentElev[x][y] = elevation[x][y];
            }
        }
        latch = new CountDownLatch(DIMENSION / PARALLEL_CHUNK_SIZE * DIMENSION / PARALLEL_CHUNK_SIZE);
        //transfer terrain in main chunks
        for(int x = 0; x < DIMENSION / PARALLEL_CHUNK_SIZE; x++){
            for(int y = 0; y < DIMENSION / PARALLEL_CHUNK_SIZE; y++){
                threadPool.execute(new TerrainMovementWorker(
                    DIMENSION,
                    x,
                    y,
                    new Random(rand.nextLong()),
                    currents,
                    newElevation,
                    currentElev,
                    latch
                ));
            }
        }
        //await main chunks
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //fill in gaps
        applyVectorToElevationGaps();
        //move data from temporary array to main array
        for(int x = 0; x < DIMENSION; x++){
            for(int y = 0; y < DIMENSION; y++){
                newElevation[x][y] = newElevation[x][y] + currentElev[x][y];
                while(newElevation[x][y] > 99){
                    newElevation[x][y] = 99;
                }
                elevation[x][y] = newElevation[x][y];
            }
        }
    }


    /**
     * A worker thread for simulating terrain moving due to force vector field
     */
    static class TerrainMovementWorker implements Runnable {

        //size of data map
        int continentPhaseDimension;
        //The offsets into the data array
        int offsetX;
        int offsetY;
        //random
        Random rand;
        //force vector field
        Vector[][] currents;
        //new elevation map to fill in
        int[][] newElevation;
        //reference elevation map to pull from
        int[][] referenceElevation;
        //latch to resynchronize threads
        CountDownLatch latch;

        protected TerrainMovementWorker(
            int continentPhaseDimension,
            int offsetX,
            int offsetY,
            Random rand,
            Vector[][] currents,
            int[][] newElevation,
            int[][] referenceElevation,
            CountDownLatch latch
        ){
            this.continentPhaseDimension = continentPhaseDimension;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.rand = rand;
            this.currents = currents;
            this.newElevation = newElevation;
            this.referenceElevation = referenceElevation;
            this.latch = latch;
        }


        /**
         * Runs the terrain movement simulation for this worker
         */
        @Override
        public void run() {
            for(int x = 0; x < PARALLEL_CHUNK_SIZE; x++){
                for(int y = 0; y < PARALLEL_CHUNK_SIZE; y++){
                    if(x % PARALLEL_CHUNK_SIZE != 0 && x % PARALLEL_CHUNK_SIZE != 1 && y % PARALLEL_CHUNK_SIZE != 0 && y % PARALLEL_CHUNK_SIZE != 1){
                        //current absolute position in data arrays
                        int currentX = x + offsetX * PARALLEL_CHUNK_SIZE;
                        int currentY = y + offsetY * PARALLEL_CHUNK_SIZE;

                        //roll whether should transfer terrain or not
                        boolean transfer = false;
                        if (Utilities.random_Integer(1, 50, rand) == 1) {
                            transfer = true;
                        }
                        //sets the goal of how much elevation to transfer to neighbors
                        int transferGoal;
                        if(Utilities.random_Integer(1, 2, rand)==1){
                            transferGoal = Utilities.random_Integer(20, 60, rand);
                        } else {
                            transferGoal = Utilities.random_Integer(0, 60, rand);
                        }
                        //roll whether to transfer horizontally or vertically
                        if(Utilities.random_Integer(1, 2, rand)==1){
                            //transfers horizontally
                            if (currents[currentX][currentY].x >= 0) {
                                if (transfer) {
                                    if (currentX + 1 < continentPhaseDimension) {
                                        while(
                                        newElevation[currentX + 1][currentY] + referenceElevation[currentX + 1][currentY] < 99 && 
                                        referenceElevation[currentX][currentY] > transferGoal){
                                            newElevation[currentX + 1][currentY]++;
                                            referenceElevation[currentX][currentY]--;
                                        }
                                    }
                                }
                            } else {
                                if (transfer) {
                                    if (currentX - 1 >= 0) {
                                        while(
                                        newElevation[currentX - 1][currentY] + referenceElevation[currentX - 1][currentY] < 99 && 
                                        referenceElevation[currentX][currentY] > transferGoal){
                                            newElevation[currentX - 1][currentY]++;
                                            referenceElevation[currentX][currentY]--;
                                        }
                                    }
                                }
                            }
                        } else {
                            //transfer vertically
                            if (currents[currentX][currentY].y >= 0) {
                                if (transfer) {
                                    if (currentY + 1 < continentPhaseDimension) {                                   //  V    REPLACE THIS WITH GOAL
                                        while(
                                        newElevation[currentX][currentY + 1] + referenceElevation[currentX][currentY + 1] < 99 && 
                                        referenceElevation[currentX][currentY] > transferGoal){
                                            newElevation[currentX][currentY + 1]++;
                                            referenceElevation[currentX][currentY]--;
                                        }
                                    }
                                }
                            } else {
                                if (transfer) {
                                    if (currentY - 1 >= 0) {
                                            while(
                                            newElevation[currentX][currentY - 1] + referenceElevation[currentX][currentY - 1] < 99 &&
                                            referenceElevation[currentX][currentY] > transferGoal){
                                            newElevation[currentX][currentY - 1]++;
                                            referenceElevation[currentX][currentY]--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            latch.countDown();
        }

    }
}
