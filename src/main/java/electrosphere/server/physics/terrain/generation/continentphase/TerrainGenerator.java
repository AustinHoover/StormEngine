package electrosphere.server.physics.terrain.generation.continentphase;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import electrosphere.logger.LoggerInterface;
import electrosphere.server.physics.terrain.models.TerrainModel;

/**
 *
 * @author satellite
 */
public class TerrainGenerator {
    int elevation[][];
    int continentPhaseDimension = 128;
    //the interpolation ratio applied to the statically generated terrain
    int interpolationRatio = 1;
    //the interpolated phase dimension
    int interpolationPhaseDimension = continentPhaseDimension * interpolationRatio;
    //vertical static interpolation ratio
    int verticalInterpolationRatio = 10;
    //the interpolation ratio applied to the dynamically generated terrain per chunk
    int dynamicInterpRatio = 1000;
    JFrame frame;
    InterpolationDisplay graphics;
    int brightness = 0;
    int brightnessHoldIncrementer = 0;
    boolean brightnessIncreasing = true;
    int displayToggle = 0;
    int max_Display_Toggle = 6;

    static final int MAX_HEIGHT = 100;
    
    int[][] mountainParsed;
    static final int MOUNTAIN_THRESHOLD = 75;
    static final int MOUNTAIN_RANGE_SIZE_MINIMUM = 10;
    
    int[][] oceanParsed;
    static final int OCEAN_THRESHOLD = 25;
    
    Vector wind_field[][];
    
    int[][] precipitationChart;
    static final int RAIN_SHADOW_BLOCKER_HEIGHT = 80;
    
    int[][] temperatureChart;

    static final int EROSION_INTERPOLATION_RATIO = 4;
    
    /*
    0 - ocean
    1 - tropical
    2 - subtropical
    3 - temperate
    4 - dry
    5 - polar
    6 - mountainous
    */
    public int[][] climateCategory;
    
    public int numberContinents = 0;
    public int[][] continentIdField;
    
    public List<Continent> continents = new ArrayList<Continent>();
    
    
    public int current_Continent = 1;
    public int current_Region_X = 0;
    public int current_Region_Y = 0;

    Random rand = new Random();

    float[][] erosionHeightmap;
    
    
    /**
     * Generates a new TerrainModel
     * @return The TerrainModel
     */
    public TerrainModel generateModel(){
        TerrainModel rVal;
        
        //generate continent-phase terrain
        TectonicSimulation tergen = new TectonicSimulation(1);
        tergen.setDimension(continentPhaseDimension);
        tergen.setLifespan(15000);
        tergen.run();
        elevation = tergen.getTerrain();
        
        //interpolate terrain to smooth what has been received from TerrainGenerator
        elevation = interpolateElevationRaws(elevation);
        
        //While the TerrainGenerator does a great job of outlining a rough sketch of how the terrain should look,
        //a lot of filters are applied to give the terrain a much cleaner and realistic look. This is arguably
        //where the real magic of the terrain generation happens

        //flatten the terrain and raise it above sealevel
        elevation = compressionFilter(elevation);
        elevation = threeHalvesFilter(elevation);

        //knead the terrain
        elevation = smoothTerrainFurther(elevation);
        elevation = smallKernelSharpen(elevation);
        elevation = smoothTerrainFurther(elevation);

        //raise terrain
        elevation = threeHalvesFilter(elevation);

        //knead terrain mode
        elevation = smallKernelSmooth(elevation);
        elevation = smallKernelSharpen(elevation);
        elevation = smoothTerrainFurther(elevation);
        elevation = smoothTerrainFurther(elevation);

        //Make the land part of the heightmap scale exponentially instead of linearly
        //Basically makes the terrain sharper
        elevation = landExponentialFilter(elevation);
        elevation = landExponentialFilter(elevation);
        elevation = landExponentialFilter(elevation);
        elevation = landExponentialFilter(elevation);
        elevation = landExponentialFilter(elevation);
        elevation = landExponentialFilter(elevation);

        mountainParsed = new int[continentPhaseDimension][continentPhaseDimension];
        oceanParsed = new int[continentPhaseDimension][continentPhaseDimension];
        continentIdField = new int[continentPhaseDimension][continentPhaseDimension];
        
        //generate designation arrays for features about the terrain
        mountainParsed = parseMountainscapes(elevation);
        oceanParsed = parseOceans(elevation);
        wind_field = mapWindField();
        precipitationChart = calculateRainShadows(elevation,oceanParsed,wind_field);
        temperatureChart = generateTemperatureChart();
        climateCategory = inferClimateCategory();
        determineContinents();
        
        //generate continent objects
        fillContinents();
        
        float[][] modelInput = interpolateIntegerElevations(elevation);
        
        modelInput = applyRandomizationFilter(modelInput);


        float[][] erosionInput = new float[elevation.length][elevation[0].length];
        for(int x = 0; x < elevation.length; x++){
            for(int y = 0; y < elevation[0].length; y++){
                erosionInput[x][y] = elevation[x][y];
            }
        }
        ErosionSimulation erosionSimulation = new ErosionSimulation(erosionInput, OCEAN_THRESHOLD, EROSION_INTERPOLATION_RATIO, rand.nextLong());
        erosionSimulation.simulate();
        erosionHeightmap = erosionSimulation.getData();
        
        // rVal = new TerrainModel(
        //         interpolationPhaseDimension,
        //         modelInput,
        //         OCEAN_THRESHOLD * verticalInterpolationRatio,
        //         MOUNTAIN_THRESHOLD * verticalInterpolationRatio,
        //         dynamicInterpRatio
        // );
        rVal = TerrainModel.create(0);


        //create internal renderer
        // createRenderer();
        
        // boolean test = true;
        // while(test){
        //     if(brightnessIncreasing){
        //         if(brightness < 100){
        //             brightness++;
        //         } else {
        //         }
        //     } else {
        //         if(brightness > 0){
        //             brightness--;
        //         } else {
        //             brightnessIncreasing = true;
        //             displayToggle++;
        //             if(displayToggle > 1){
        //                 displayToggle = 0;
        //             }
        //         }
        //     }
        //     frame.repaint();
        //     Utilities.sleep(10);
        // }
        
        return rVal;
    }
    
    float[][] applyRandomizationFilter(float[][] elevation){
        float[][] rVal = new float[elevation.length][elevation[0].length];
        for(int x = 0; x < continentPhaseDimension-1; x++){
            for(int y = 0; y < continentPhaseDimension-1; y++){
                rVal[x][y] = elevation[x][y] + Utilities.random_Integer(0, (int)(elevation[x][y] / verticalInterpolationRatio), rand);
            }
        }
        return rVal;
    }
    
    float[][] interpolateIntegerElevations(int[][] elevation){
        int interpRatio = interpolationRatio;
        float[][] rVal = new float[continentPhaseDimension * interpRatio][continentPhaseDimension * interpRatio];
        for(int x = 0; x < continentPhaseDimension-1; x++){
            for(int y = 0; y < continentPhaseDimension-1; y++){
                for(int i = 0; i < interpRatio; i++){
                    for(int j = 0; j < interpRatio; j++){
                        float ratio1 = (i*j) * 1.0f / (interpRatio*interpRatio);
                        float ratio2 = ((interpRatio-i)*j) * 1.0f / (interpRatio*interpRatio);
                        float ratio3 = (i*(interpRatio-j)) * 1.0f / (interpRatio*interpRatio);
                        float ratio4 = ((interpRatio-i)*(interpRatio-j)) * 1.0f / (interpRatio*interpRatio);
                        rVal[x*interpRatio+interpRatio-i][y*interpRatio+interpRatio-j] = 
                                (elevation[x  ][y  ] * ratio1 +
                                elevation[x+1][y  ] * ratio2 + 
                                elevation[x  ][y+1] * ratio3 +
                                elevation[x+1][y+1] * ratio4) *
                                verticalInterpolationRatio
                                ;
                    }
                }
            }
        }
        interpolationPhaseDimension = continentPhaseDimension * interpRatio;
        return rVal;
    }
    
    public void anchor_To_Real_Region(){
        //current_Region_X
        //continents
        //current_Continent
        Continent target_continent = continents.get(current_Continent);
        for(int x = 0; x < target_continent.dim_x; x++){
            for(int y = 0; y < target_continent.dim_y; y++){
                if(target_continent.regions[x][y]!=null){
                    current_Region_X = x;
                    current_Region_Y = y;
                    x = target_continent.dim_x;
                    y = target_continent.dim_y;
                } else {
                    
                }
            }
        }
    }
    
    int[][] simulate_Drainage(int[][] water_level, int[][] elevation, int dim_x, int dim_y){
        int[][] rVal = new int[dim_x][dim_y];
        for(int x = 0; x < dim_x; x++){
            for(int y = 0; y < dim_y; y++){
                rVal[x][y] = 0;
            }
        }
        int kernel_offset_x[] = {
          -1,0,0,1  
        };
        int kernel_offset_y[] = {
            0,-1,1,0
        };
        int offset_kernel_size = 4;
        for(int x = 0; x < dim_x; x++){
            for(int y = 0; y < dim_y; y++){
                int num_Water_Particles = water_level[x][y];
                // int elevation_Center = elevation[x][y];
                int attractor_Values[] = new int[offset_kernel_size];
                for(int j = 0; j < offset_kernel_size; j++) {
                    if(x + kernel_offset_x[j] >= 0 && x + kernel_offset_x[j] < dim_x && y + kernel_offset_y[j] >= 0 && y + kernel_offset_y[j] < dim_y) {
                        if(elevation[x+kernel_offset_x[j]][y+kernel_offset_y[j]] < elevation[x][y]){
                            attractor_Values[j] = (int)((elevation[x][y]-elevation[x+kernel_offset_x[j]][y+kernel_offset_y[j]])*1.0*water_level[x+kernel_offset_x[j]][y+kernel_offset_y[j]]);
                        } else {
                            attractor_Values[j] = 0;
                        }
                    }
                }
                int rand_num = 0;
                int rand_Space = 0;
                for(int j = 0; j < offset_kernel_size; j++){
                    rand_Space = rand_Space + attractor_Values[j];
                }
                rand_Space = rand_Space + water_level[x][y]; // account for water not moving downwards
                int rand_Incrementer = 0;
                boolean done = false;
                for(int i = 0; i < num_Water_Particles; i++){
                    done = false;
                    rand_Incrementer = 0;
                    rand_num = Utilities.random_Integer(0, rand_Space, rand);
                    for(int j = 0; j < offset_kernel_size; j++){
                        rand_Incrementer = rand_Incrementer + attractor_Values[j];
                        if(rand_num < rand_Incrementer){
                            if(attractor_Values[j] > 0){
                                if(rVal[x+kernel_offset_x[j]][y+kernel_offset_y[j]] < 400){
                                    rVal[x+kernel_offset_x[j]][y+kernel_offset_y[j]]++;
                                }
                                done = true;
                            }
                        }
                    }
                    if(!done){
                        rVal[x][y]++;
                    }
                }
            }
        }
        return rVal;
    }
    
    int sumIntegerArray(int[][] arr, int dim_x, int dim_y){
        int sum = 0;
        for(int x = 0; x < dim_x; x++){
            for(int y = 0; y < dim_y; y++){
                sum = sum + arr[x][y];
            }
        }
        return sum;
    }
    
    /**
     * Generates an elevation map of a given continent
     * @param c The continent to generate off of
     */
    void generateRegionElevationMaps(Continent c){
        int neighborOffsetX[] = {
            1,0,-1,1,-1,1,0,-1
        };
        int neighborOffsetY[] = {
            1,1,1,0,0,-1,-1,-1
        };
        //generate neighbor links
        for(int x = 0; x < c.dim_x; x++){
            for(int y = 0; y < c.dim_y; y++){
                if(c.regions[x][y]!=null){
                    for(int i = 0; i < 8; i++){
                        if(x + neighborOffsetX[i] >= 0 && x + neighborOffsetX[i] < c.dim_x && y + neighborOffsetY[i] >= 0 && y + neighborOffsetY[i] < c.dim_y){
                            //+1 comes from getting the center of the kernel
                            //ie kernel is 3x3 therefore the center position is at 1,1 array-wise
                            c.regions[x][y].neighbors[neighborOffsetX[i]+1][neighborOffsetY[i]+1] = c.regions[x + neighborOffsetX[i]][y + neighborOffsetY[i]];
                        } else {
                            c.regions[x][y].neighbors[neighborOffsetX[i]+1][neighborOffsetY[i]+1] = null;
                        }
                    }
                }
            }
        }
        int regionDim = Region.REGION_DIMENSION;
        //set elevation goals
        for(int x = 0; x < c.dim_x; x++){
            for(int y = 0; y < c.dim_y; y++){
                if(c.regions[x][y]!=null){
                    c.regions[x][y].elevation_Goal = c.elevation[x][y];
                }
            }
        }
        //interpolate base elevation values
        int interpolation_kernel_offset_x[] = {0,1,0,1};
        int interpolation_kernel_offset_y[] = {0,0,1,1};
        int interpolation_kernel_size = 4;
        int corner_Vals[][] = new int[2][2];
        for(int x = 0; x < c.dim_x; x++){
            for(int y = 0; y < c.dim_y; y++){
                if (c.regions[x][y] != null) {
                    c.regions[x][y].chart_Elevation = new int[Region.REGION_DIMENSION][Region.REGION_DIMENSION];
                    //first the north-west corner
                    for(int i = 0; i < interpolation_kernel_size; i++){
                        if(x-1+interpolation_kernel_offset_x[i] >= 0 && x-1+interpolation_kernel_offset_x[i] < c.dim_x &&
                                y-1+interpolation_kernel_offset_y[i] >= 0 && y-1+interpolation_kernel_offset_y[i] < c.dim_y){
                            if(c.regions[x-1+interpolation_kernel_offset_x[i]][y-1+interpolation_kernel_offset_y[i]]!=null){
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 
                                        c.regions[x-1+interpolation_kernel_offset_x[i]][y-1+interpolation_kernel_offset_y[i]].elevation_Goal;
                            } else {
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 0;
                            }
                        }
                    }
                    int midpoint = (int)(Region.REGION_DIMENSION/2.0);
                    int sum = 0;
                    for(int i = 0; i < midpoint; i++){
                        for(int j = 0; j < midpoint; j++){
                            sum = 0;
                            sum = sum + (midpoint-i)*(midpoint-j)*corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[0]];
                            sum = sum + (i)*(midpoint-j)*         corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[0]];
                            sum = sum + (midpoint-i)*(j)*         corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[1]];
                            sum = sum + (i)*(j)*                  corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[1]];
                            c.regions[x][y].chart_Elevation[i][j] = sum;
                        }
                    }
                    //now the north-east corner
                    for(int i = 0; i < interpolation_kernel_size; i++){
                        if(x+interpolation_kernel_offset_x[i] >= 0 && x+interpolation_kernel_offset_x[i] < c.dim_x &&
                                y-1+interpolation_kernel_offset_y[i] >= 0 && y-1+interpolation_kernel_offset_y[i] < c.dim_y){
                            if(c.regions[x+interpolation_kernel_offset_x[i]][y-1+interpolation_kernel_offset_y[i]]!=null){
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 
                                        c.regions[x+interpolation_kernel_offset_x[i]][y-1+interpolation_kernel_offset_y[i]].elevation_Goal;
                            } else {
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 0;
                            }
                        }
                    }
                    for(int i = midpoint; i < Region.REGION_DIMENSION; i++){
                        for(int j = 0; j < midpoint; j++){
                            sum = 0;
                            sum = sum + (Region.REGION_DIMENSION-i)*(midpoint-j)*corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[0]];
                            sum = sum + (i-midpoint)*(midpoint-j)*               corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[0]];
                            sum = sum + (Region.REGION_DIMENSION-i)*(j)*         corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[1]];
                            sum = sum + (i-midpoint)*(j)*                        corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[1]];
                            c.regions[x][y].chart_Elevation[i][j] = sum;
                        }
                    }
                    //now the south-west corner
                    for(int i = 0; i < interpolation_kernel_size; i++){
                        if(x-1+interpolation_kernel_offset_x[i] >= 0 && x-1+interpolation_kernel_offset_x[i] < c.dim_x &&
                                y+interpolation_kernel_offset_y[i] >= 0 && y+interpolation_kernel_offset_y[i] < c.dim_y){
                            if(c.regions[x-1+interpolation_kernel_offset_x[i]][y+interpolation_kernel_offset_y[i]]!=null){
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 
                                        c.regions[x-1+interpolation_kernel_offset_x[i]][y+interpolation_kernel_offset_y[i]].elevation_Goal;
                            } else {
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 0;
                            }
                        }
                    }
                    for(int i = 0; i < midpoint; i++){
                        for(int j = midpoint; j < Region.REGION_DIMENSION; j++){
                            sum = 0;
                            sum = sum + (midpoint-i)*(Region.REGION_DIMENSION-j)*corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[0]];
                            sum = sum + (i)*(Region.REGION_DIMENSION-j)*         corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[0]];
                            sum = sum + (midpoint-i)*(j-midpoint)*               corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[1]];
                            sum = sum + (i)*(j-midpoint)*                        corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[1]];
                            c.regions[x][y].chart_Elevation[i][j] = sum;
                        }
                    }
                    //now the south-east corner
                    for(int i = 0; i < interpolation_kernel_size; i++){
                        if(x+interpolation_kernel_offset_x[i] >= 0 && x+interpolation_kernel_offset_x[i] < c.dim_x &&
                                y+interpolation_kernel_offset_y[i] >= 0 && y+interpolation_kernel_offset_y[i] < c.dim_y){
                            if(c.regions[x+interpolation_kernel_offset_x[i]][y+interpolation_kernel_offset_y[i]]!=null){
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 
                                        c.regions[x+interpolation_kernel_offset_x[i]][y+interpolation_kernel_offset_y[i]].elevation_Goal;
                            } else {
                                corner_Vals[interpolation_kernel_offset_x[i]][interpolation_kernel_offset_y[i]] = 0;
                            }
                        }
                    }
                    for(int i = midpoint; i < Region.REGION_DIMENSION; i++){
                        for(int j = midpoint; j < Region.REGION_DIMENSION; j++){
                            sum = 0;
                            sum = sum + (Region.REGION_DIMENSION-i)*(Region.REGION_DIMENSION-j)*corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[0]];
                            sum = sum + (i-midpoint)*(Region.REGION_DIMENSION-j)*               corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[0]];
                            sum = sum + (Region.REGION_DIMENSION-i)*(j-midpoint)*               corner_Vals[interpolation_kernel_offset_x[0]][interpolation_kernel_offset_y[1]];
                            sum = sum + (i-midpoint)*(j-midpoint)*                              corner_Vals[interpolation_kernel_offset_x[1]][interpolation_kernel_offset_y[1]];
                            c.regions[x][y].chart_Elevation[i][j] = sum;
                            
                        }
                    }
                }
            }
        }
        Region region_Current = null;
        for(int x = 0; x < c.dim_x; x++){
            for(int y = 0; y < c.dim_y; y++){
                if(c.regions[x][y]!=null){
                    if(region_Current == null){
                        region_Current = c.regions[x][y];
                    } else if(c.regions[x][y].elevation_Goal > region_Current.elevation_Goal){
                        region_Current = c.regions[x][y];
                    }
                }
            }
        }
        //drainage simulation
        //operates on an open set
        int adjacency_array_x[] = {
            1,0,1,2
        };
        int adjacency_array_y[] = {
            0,1,2,1
        };
        boolean simulate_Drainage = true;
        List<Region> open_Set = new ArrayList<Region>();
        open_Set.add(region_Current);
        if(simulate_Drainage){
        while(!open_Set.isEmpty()){
            region_Current.chart_Drainage = new int[regionDim][regionDim];
            for(int x = 0; x < regionDim; x++){
                for(int y = 0; y < regionDim; y++){
                    region_Current.chart_Drainage[x][y] = 1;
                }
            }
            if (region_Current.neighbors[1][0] != null) {
                if (region_Current.neighbors[1][0].finished_Drainage_Simulation == true) {
                    for(int x = 0; x < regionDim; x++){
                        region_Current.chart_Drainage[x][0] = 1 + region_Current.neighbors[1][0].chart_Drainage[x][regionDim-1];
                    }
                }
            }
            if (region_Current.neighbors[0][1] != null) {
                if (region_Current.neighbors[0][1].finished_Drainage_Simulation == true) {
                    for(int x = 0; x < regionDim; x++){
                        region_Current.chart_Drainage[0][x] = 1 + region_Current.neighbors[0][1].chart_Drainage[regionDim-1][x];
                    }
                }
            }
            if (region_Current.neighbors[1][2] != null) {
                if (region_Current.neighbors[1][2].finished_Drainage_Simulation == true) {
                    for(int x = 0; x < regionDim; x++){
                        region_Current.chart_Drainage[x][regionDim-1] = 1 + region_Current.neighbors[1][2].chart_Drainage[x][0];
                    }
                }
            }
            if (region_Current.neighbors[2][1] != null) {
                if (region_Current.neighbors[2][1].finished_Drainage_Simulation == true) {
                    for(int x = 0; x < regionDim; x++){
                        region_Current.chart_Drainage[regionDim-1][x] = 1 + region_Current.neighbors[2][1].chart_Drainage[0][x];
                    }
                }
            }
            region_Current.chart_Max_Water_Flow = new int[regionDim][regionDim];
//            while(sum_Array(region_Current.chart_Drainage,region_Dim,region_Dim)>0){
//                region_Current.chart_Drainage = simulate_Drainage(region_Current.chart_Drainage,region_Current.chart_Elevation,Region.REGION_DIMENSION,Region.REGION_DIMENSION);
//                for(int x = 0; x < region_Dim; x++){
//                    for(int y = 0; y < region_Dim; y++){
//                        if(region_Current.chart_Drainage[x][y] > region_Current.chart_Max_Water_Flow[x][y]){
//                            region_Current.chart_Max_Water_Flow[x][y] = region_Current.chart_Drainage[x][y];
//                        }
//                    }
//                }
//            }
            region_Current.chart_Drainage = region_Current.chart_Max_Water_Flow;
            open_Set.remove(region_Current);
            //TODO: When adding regions to the set, check if _their_ neighbors are higher elevation recursively (probably going to need its own function)
            for (int i = 0; i < 4; i++) {
                if (region_Current.neighbors[adjacency_array_x[i]][adjacency_array_y[i]] != null) {
                    if (region_Current.neighbors[adjacency_array_x[i]][adjacency_array_y[i]].finished_Drainage_Simulation == false) {
                        int next_height = region_Current.neighbors[adjacency_array_x[i]][adjacency_array_y[i]].elevation_Goal;
                        int index_to_insert_at = 0;
                        Iterator<Region> region_Iterator = open_Set.iterator();
                        while (region_Iterator.hasNext()) {
                            Region next = region_Iterator.next();
                            if (next.elevation_Goal > next_height) {
                                index_to_insert_at++;
                            } else {
                                break;
                            }
                        }
                        if(!open_Set.contains(region_Current.neighbors[adjacency_array_x[i]][adjacency_array_y[i]])){
                            open_Set.add(index_to_insert_at, region_Current.neighbors[adjacency_array_x[i]][adjacency_array_y[i]]);
                        }
                    }
                }
            }
            region_Current.finished_Drainage_Simulation = true;
            if(open_Set.size() > 0){
                region_Current = open_Set.get(0);
            }
        }
        }
    }
    
    /**
     * Creates the internal renderer for displaying data
     */
    void createRenderer(){
        frame = new JFrame();
        graphics = new InterpolationDisplay(this);
        frame.setBounds(25, 25, 300 + continentPhaseDimension, 300 + continentPhaseDimension);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.add(graphics);
        frame.setVisible(true);
        frame.addKeyListener(new KeyListener(){
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_C){
                    increment_Current_Continent();
                } else if(ke.getKeyCode() == KeyEvent.VK_V){
                    incrementDisplayToggle();
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }
        }
        );
    }
    
    /**
     * Generates continent objects from the raw designation arrays
     */
    void fillContinents(){
        for(int i = 1; i < numberContinents + 1; i++){
            Continent current = new Continent();
            continents.add(current);
            //find dimensions
            int minX = -1;
            int minY = -1;
            int maxX = -1;
            int maxY = -1;
            for(int x = 0; x < continentPhaseDimension; x++){
                for(int y = 0; y < continentPhaseDimension; y++){
                    if(continentIdField[x][y] == i){
                        if(minX == -1){
                            minX = x;
                            minY = y;
                            maxX = x;
                            maxY = y;
                        } else {
                            if (x < minX) {
                                minX = x;
                            }
                            if (x > maxX) {
                                maxX = x;
                            }
                            if (y < minY) {
                                minY = y;
                            }
                            if (y > maxY) {
                                maxY = y;
                            }
                        }
                    }
                }
            }
            //The dimensions of the continent
            int dimX = maxX - minX + 1;
            int dimY = maxY - minY + 1;
            current.dim_x = dimX;
            current.dim_y = dimY;
            current.chart_Climate = new int[dimX][dimY];
            current.chart_Precipitation = new int[dimX][dimY];
            current.chart_Temperature = new int[dimX][dimY];
            current.chart_Wind_Macro = new int[dimX][dimY];
            current.elevation = new int[dimX][dimY];
            //zero out arrays
            for(int x = 0; x < dimX; x++){
                for(int y = 0; y < dimY; y++){
                    current.elevation[x][y] = 0;
                    current.chart_Climate[x][y] = 0;
                    current.chart_Precipitation[x][y] = 0;
                    current.chart_Temperature[x][y] = 50;
                    current.chart_Wind_Macro[x][y] = 0;
                }
            }
            //fill continent level arrays
            for(int x = 0; x < continentPhaseDimension; x++){
                for(int y = 0; y < continentPhaseDimension; y++){
                    if(continentIdField[x][y] == i){
                        current.size++;
                        current.elevation[x-minX][y-minY] = elevation[x][y];
                        current.chart_Climate[x-minX][y-minY] = climateCategory[x][y];
                        current.chart_Precipitation[x-minX][y-minY] = precipitationChart[x][y];
                        current.chart_Temperature[x-minX][y-minY] = temperatureChart[x][y];
                        current.chart_Wind_Macro[x-minX][y-minY] = wind_field[x][y].x;
                    }
                }
            }
            //create regions
            current.regions = new Region[dimX][dimY];
            for(int x = 0; x < dimX; x++){
                for(int y = 0; y < dimY; y++){
                    if(continentIdField[x+minX][y+minY] == i){
                        current.regions[x][y] = new Region();
                        current.regions[x][y].elevation_Goal = current.elevation[x][y];
                    } else {
                        current.regions[x][y] = null;
                    }
                }
            }
            generateRegionElevationMaps(current);
        }
    }
    
    /**
     * Recursive function to floodfill designations of a continent
     * @param x The x position to check
     * @param y The y position to check
     * @param number The continent id of the continent currently being floodfilled
     */
    void floodfillContinentNumber(int x, int y, int number){
        continentIdField[x][y] = number;
        int offset_X[] = {0,-1,0,1};
        int offset_Y[] = {1,0,-1,0};
        for(int i = 0; i < 4; i++){
            if(x + offset_X[i] >= 0 && x + offset_Y[i] < continentPhaseDimension){
                if(y + offset_Y[i] >= 0 && y + offset_Y[i] < continentPhaseDimension){
                    if(oceanParsed[x + offset_X[i]][y + offset_Y[i]] < 25){
                        if(continentIdField[x + offset_X[i]][y + offset_Y[i]] == 0){
                            floodfillContinentNumber(x + offset_X[i], y + offset_Y[i], number);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Uses flood fill to determine continents
     */
    void determineContinents(){
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                if(oceanParsed[x][y] < 25){
                    if(continentIdField[x][y] == 0){
                        numberContinents++;
                        floodfillContinentNumber(x,y,numberContinents);
                    }
                }
            }
        }
    }
    
    /**
     * Generates a map tagging each position into a climate classification
     * 
     * 0 - ocean
     * 1 - tropical
     * 2 - subtropical
     * 3 - temperate
     * 4 - dry
     * 5 - polar
     * 6 - mountainous
     * 
     * @return The classification map
    */
    int[][] inferClimateCategory(){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++) {
                if (elevation[x][y] > OCEAN_THRESHOLD) {
                    if (elevation[x][y] > MOUNTAIN_THRESHOLD) {
                        rVal[x][y] = 6;
                    } else {
                        if (precipitationChart[x][y] > 0) {
                            if (temperatureChart[x][y] > 94) {
                                rVal[x][y] = 1;
                            } else if (temperatureChart[x][y] > 80) {
                                rVal[x][y] = 2;
                            } else if (temperatureChart[x][y] > 40) {
                                rVal[x][y] = 3;
                            } else {
                                rVal[x][y] = 5;
                            }
                        } else {
                            if (temperatureChart[x][y] > 75) {
                                rVal[x][y] = 4;
                            } else if (temperatureChart[x][y] > 40) {
                                rVal[x][y] = 4;
                            } else if (temperatureChart[x][y] > 25) {
                                rVal[x][y] = 5;
                            } else {
                                rVal[x][y] = 5;
                            }
                        }
                    }
                } else {
                    if (temperatureChart[x][y] > 75) {
                            rVal[x][y] = 0;
                        } else if (temperatureChart[x][y] > 40) {
                            rVal[x][y] = 0;
                        } else if (temperatureChart[x][y] > 25) {
                            rVal[x][y] = 0;
                        } else {
                            rVal[x][y] = 5;
                        }
                }
            }
        }
        return rVal;
    }
    
    /**
     * Generates a map representing average temperature
     * @return The map
     */
    int[][] generateTemperatureChart(){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        int temp = 0;
        for(int x = 0; x < continentPhaseDimension; x++){
            temp = (int)(100 - (Math.abs(x - continentPhaseDimension/2.0) / (continentPhaseDimension/2.0) * 100.0));
            temp = (int)(Math.sin(temp/100.0 * Math.PI / 2.0) * 100.0);
            for(int y = 0; y < continentPhaseDimension; y++){
                rVal[y][x] = temp;
            }
        }
        return rVal;
    }
    
    /**
     * Calculates rain shadowing across the heightmap
     * @param elevation_raws The elevation data
     * @param ocean_parsed The ocean marking map
     * @param wind_field The field of average wind flow map
     * @return The map marking rain shadows
     */
    int[][] calculateRainShadows(int[][] elevation_raws, int[][] ocean_parsed, Vector[][] wind_field){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        int rainParticles[][] = new int[continentPhaseDimension][continentPhaseDimension];
        int numRainParticles = 0;
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                if(ocean_parsed[x][y] > 1){
                    rainParticles[x][y] = 1;
                    rVal[x][y] = 1;
                    numRainParticles++;
                }
            }
        }
        while(numRainParticles > continentPhaseDimension * 2) {
            for (int x = 0; x < continentPhaseDimension; x++) {
                for (int y = 0; y < continentPhaseDimension; y++) {
                    if (rainParticles[x][y] >= 1) {
                        if (wind_field[x][y].x == -1) {
                            if (x > 0) {
                                if (elevation_raws[x - 1][y] < RAIN_SHADOW_BLOCKER_HEIGHT) {
                                    rainParticles[x - 1][y] = 1;
                                    rVal[x - 1][y] = 1;
                                } else {
                                }
                                if (wind_field[x][y].y == -1) {
                                    if (y > 0) {
                                        if (elevation_raws[x - 1][y - 1] < RAIN_SHADOW_BLOCKER_HEIGHT) {
                                            rainParticles[x - 1][y - 1] = 1;
                                            rVal[x - 1][y - 1] = 1;
                                        }
                                    }
                                } else if (wind_field[x][y].y == 1) {
                                    if (y < continentPhaseDimension - 1) {
                                        if (elevation_raws[x - 1][y + 1] < RAIN_SHADOW_BLOCKER_HEIGHT) {
                                            rainParticles[x - 1][y + 1] = 1;
                                            rVal[x - 1][y + 1] = 1;
                                        }
                                    }
                                } 
                            } else {
                            }
                        } else {
                            if (x < continentPhaseDimension - 1) {
                                if (elevation_raws[x + 1][y] < RAIN_SHADOW_BLOCKER_HEIGHT) {
                                    rainParticles[x + 1][y] = 1;
                                    rVal[x + 1][y] = 1;
                                } else {
                                }
                                if (wind_field[x][y].y == -1) {
                                    if (y > 0) {
                                        if (elevation_raws[x + 1][y - 1] < RAIN_SHADOW_BLOCKER_HEIGHT) {
                                            rainParticles[x + 1][y - 1] = 1;
                                            rVal[x + 1][y - 1] = 1;
                                        }
                                    }
                                } else if (wind_field[x][y].y == 1) {
                                    if (y < continentPhaseDimension - 1) {
                                        if (elevation_raws[x + 1][y + 1] < RAIN_SHADOW_BLOCKER_HEIGHT) {
                                            rainParticles[x + 1][y + 1] = 1;
                                            rVal[x + 1][y + 1] = 1;
                                        }
                                    }
                                } 
                            } else {
                            }
                        }
                        rainParticles[x][y] = 0;
                    }
                }
            }
            numRainParticles = 0;
            for(int x = 0; x < continentPhaseDimension; x++){
                for(int y = 0; y < continentPhaseDimension; y++){
                    if(rainParticles[x][y] == 1){
                        numRainParticles++;
                    }
                }
            }
        }
        return rVal;
    }
    
    /**
     * Calculates a vector field representing average wind flow across the heightmap
     * @return The vector field representing average wind flow
     */
    Vector[][] mapWindField(){
        Vector rVal[][] = new Vector[continentPhaseDimension][continentPhaseDimension];
        //One sixth of the dimension of the heightmap
        int sixth = (int)(continentPhaseDimension / 6.0);
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                if(x < sixth){
                    rVal[x][y] = new Vector();
                    rVal[x][y].x = -1;
                    rVal[x][y].y = -1;
                } else if(x < sixth * 2){
                    rVal[x][y] = new Vector();
                    rVal[x][y].x = 1;
                    rVal[x][y].y = 1;
                } else if(x < sixth * 3){
                    rVal[x][y] = new Vector();
                    rVal[x][y].x = -1;
                    rVal[x][y].y = -1;
                } else if(x < sixth * 4){
                    rVal[x][y] = new Vector();
                    rVal[x][y].x = -1;
                    rVal[x][y].y = 1;
                } else if(x < sixth * 5){
                    rVal[x][y] = new Vector();
                    rVal[x][y].x = 1;
                    rVal[x][y].y = -1;
                } else {
                    rVal[x][y] = new Vector();
                    rVal[x][y].x = -1;
                    rVal[x][y].y = 1;
                }
            }
        }
        return rVal;
    }
    
    /**
     * Fills out an array that marks positions in the heightmap as oceans or not oceans
     * @param data The heightmap
     * @return A new array that contains the designations
     */
    int[][] parseOceans(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                if(data[x][y] < OCEAN_THRESHOLD){
                    rVal[x][y] = OCEAN_THRESHOLD;
                } else {
                    rVal[x][y] = 0;
                }
            }
        }
        return rVal;
    }
    
    /**
     * Fills out an array that marks positions in the heightmap as mountains or not mountains
     * @param data The heightmap
     * @return A new array that contains the designations
     */
    int[][] parseMountainscapes(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                if(data[x][y] > MOUNTAIN_THRESHOLD){
                    rVal[x][y] = MOUNTAIN_THRESHOLD;
                } else {
                    rVal[x][y] = 0;
                }
            }
        }
        return rVal;
    }
    
    int[][] closed_Set;
    
    int floodfill_Reach_Threshold(int[][] data, int[][] closed_Set, int x, int y, int min_val, int max_val){
        int rVal = 0;
        // int num_hits;
        int offset_X[] = {-1,-1,-1,0,0,0,1,1,1};
        int offset_Y[] = {-1,0,1,-1,0,1,-1,0,1};
        if(data[x][y] > min_val && data[x][y] < max_val){
            closed_Set[x][y] = 1;
            for(int i = 0; i < 9; i++){
                if(x + offset_X[i] >= 0 && 
                        x + offset_X[i] < continentPhaseDimension &&
                        y + offset_Y[i] >= 0 &&
                        y + offset_Y[i] < continentPhaseDimension){
                    if(closed_Set[x + offset_X[i]][y+offset_Y[i]] == 0){
                        rVal = rVal + floodfill_Reach_Threshold(data,closed_Set,x + offset_X[i],y + offset_Y[i], min_val, max_val);
                    }
                }
            }
        }
        return rVal;
    }
    
    int[][] remove_Small_Mountain_Ranged(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        // boolean removed_Something = false;
//        for (int x = 0; x < DIMENSION; x++) {
//            for (int y = 0; y < DIMENSION; y++) {
//                rVal[x][y] = data[x][y];
//            }
//        }
//        while (true) {
            for (int x = 0; x < continentPhaseDimension; x++) {
                for (int y = 0; y < continentPhaseDimension; y++) {
                    rVal[x][y] = data[x][y];
                    if (data[x][y] > MOUNTAIN_THRESHOLD) {
                        rVal[x][y] = rVal[x][y] - 5;
//                        closed_Set = new int[DIMENSION][DIMENSION];
//                        if (floodfill_Reach_Threshold(rVal, closed_Set, x, y, mountain_Threshold, 101) < mountain_Range_Size_Minimum) {
//                            rVal[x][y] = rVal[x][y] - 25;
////                            removed_Something = true;
//                        }
                    }
                }
            }
//            if(!removed_Something){
//                break;
////            } else {
//                removed_Something = false;
//            }
//        }
        return rVal;
    }
    
    /**
     * Compresses the extremes of the map towards the center half.
     * IE: 
     *     values less than 25 -> Center 50 <- values greater than 75
     * @param data The heightmap to apply the compression filter to
     * @return A new heightmap containing the compressed data
     */
    int[][] compressionFilter(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                if(data[x][y] < 25){
                    rVal[x][y] = (int)(data[x][y] * 3.0 / 2.0);
                } else if(data[x][y] > 75){
                    rVal[x][y] = (int)(data[x][y] / 3.0 * 2.0);
                } else {
                    rVal[x][y] = data[x][y];
                }
            }
        }
        return rVal;
    }
    
    /**
     * Applies a filter that pushes the terrain upwards by making all values "Three halves" of their original values.
     * @param data The heightmap to apply the filter to
     * @return A new array containing the data with filter applied
     */
    int[][] threeHalvesFilter(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                rVal[x][y] = (int)(data[x][y] * 3.0 / 2.0);
                if(rVal[x][y] > 100){
                    rVal[x][y] = 100;
                }
            }
        }
        return rVal;
    }
    
    int[][] two_third_filter(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                rVal[x][y] = (int)(data[x][y] * 2.0 / 3.0);
                if(rVal[x][y] < 0){
                    rVal[x][y] = 0;
                }
            }
        }
        return rVal;
    }
    
    int[][] high_end_two_third_filter(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++) {
                if (data[x][y] > 50) {
                    rVal[x][y] = (int) (data[x][y] * 2.0 / 3.0);
                    if (rVal[x][y] < 0) {
                        rVal[x][y] = 0;
                    }
                } else {
                    rVal[x][y] = data[x][y];
                }
            }
        }
        return rVal;
    }
    
    int[][] reduce_mid_high_end_filter(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++) {
                if (data[x][y] > 50 && data[x][y] < 75) {
                    rVal[x][y] = (int) (data[x][y] * 2.0 / 3.0);
                    if (rVal[x][y] < 0) {
                        rVal[x][y] = 0;
                    }
                } else {
                    rVal[x][y] = data[x][y];
                }
            }
        }
        return rVal;
    }
    
    /**
     * Applies a small radius vertical, horizontal, and radial smoothing kernel to the data
     * @param elevationMap The heightmap to apply the filter to
     * @return A new array containing the data with filter applied
     */
    int[][] smallKernelSmooth(int elevationMap[][]){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        int kernelOffsetX[] = {
                                -1,0,1,
                                -1,0,1,
                                -1,0,1
                                };
        int kernelOffsetY[] = {
                                 -1,-1,-1,
                                  0,0,0,
                                  1,1,1
                                  };
        int kernelModifier[] = {
                                1,2,1,
                                2,4,2,
                                1,2,1
                                };
        for(int x = 1; x < continentPhaseDimension - 1; x++){
            for(int y = 1; y < continentPhaseDimension - 1; y++){
                int sum = 0;
                for(int i = 0; i < 9; i++){
                    sum = sum + elevationMap[x+kernelOffsetX[i]][y+kernelOffsetY[i]] * kernelModifier[i];
                }
                sum = (int)(sum/13.0);
                if(sum > 100){
                    sum = 100;
                }
                rVal[x][y] = sum;
            }
        }
        return rVal;
    }
    
    /**
     * Applies a small horizontal, vertical, and radial sharpen filter to the data
     * @param elevationMap The heightmap to apply the filter to
     * @return A new array containing the data with filter applied
     */
    int[][] smallKernelSharpen(int elevationMap[][]){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        int kernelOffsetX[] = {
                                -1,0,1,
                                -1,0,1,
                                -1,0,1
                                };
        int kernelOffsetY[] = {
                                 -1,-1,-1,
                                  0,0,0,
                                  1,1,1
                                  };
        int kernelModifier[] = {
                                0,-1,0,
                                -1,5,-1,
                                0,-1,0
                                };
        for(int x = 1; x < continentPhaseDimension - 1; x++){
            for(int y = 1; y < continentPhaseDimension - 1; y++){
                int sum = 0;
                for(int i = 0; i < 9; i++){
                    sum = sum + elevationMap[x+kernelOffsetX[i]][y+kernelOffsetY[i]] * kernelModifier[i];
                }
//                sum = (int)(sum/13.0);
                if(sum > 100){
                    sum = 100;
                }
                if(sum < 0){
                    sum = 0;
                }
                rVal[x][y] = sum;
            }
        }
        return rVal;
    }
    

    /**
     * Applies a vertical, horizontal, and radial smoothing kernel to the heightmap data.
     * @param elevationMap The heightmap to apply the filter to
     * @return A new array containing the data with filter applied
     */
    int[][] smoothTerrainFurther(int elevationMap[][]){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        int kernelOffsetX[] = {-2,-1,0,1,2,
                                -2,-1,0,1,2,
                                -2,-1,0,1,2,
                                -2,-1,0,1,2,
                                -2,-1,0,1,2,
                                };
        int kernelOffsetY[] = {-2,-2,-2,-2,-2,
                                 -1,-1,-1,-1,-1,
                                  0,0,0,0,0,
                                  1,1,1,1,1,
                                  2,2,2,2,2};
        int kernelModifier[] = {
                                1,4,6,4,1,
                                4,16,24,16,4,
                                6,24,36,24,6,
                                4,16,24,16,4,
                                1,4,6,4,1
                                };
        for(int x = 2; x < continentPhaseDimension - 2; x++){
            for(int y = 2; y < continentPhaseDimension - 2; y++){
                int sum = 0;
                for(int i = 0; i < 25; i++){
                    sum = sum + elevationMap[x+kernelOffsetX[i]][y+kernelOffsetY[i]] * kernelModifier[i];
                }
                sum = (int)(sum/256.0);
                if(sum > 100){
                    sum = 100;
                }
                rVal[x][y] = sum;
            }
        }
        return rVal;
    }
    
    /**
     * Applies a filter that makes the land height scale exponentially
     * IE Terrain that looks like:
     *   ^
     *  / \
     * /   \
     * Becomes
     *    ^
     *   | |
     * __   __
     * @param data The data to apply the filter to
     * @return A new array containing the data with the filter applied
     */
    int[][] landExponentialFilter(int[][] data){
        int rVal[][] = new int[continentPhaseDimension][continentPhaseDimension];
        for(int x = 0; x < continentPhaseDimension; x++){
            for(int y = 0; y < continentPhaseDimension; y++){
                if(data[x][y] > TerrainGenerator.OCEAN_THRESHOLD && data[x][y] < 90){
                    //This is the total height that can be above land
                    //eg if ocean level is 25, and the maximum possible height is 100, the maximum height relative to sea level would be 75
                    // int maxHeightAboveLand = MAX_HEIGHT - OCEAN_THRESHOLD;
                    //This is the current height above sea level
                    int currentHeightAboveLand = data[x][y] - OCEAN_THRESHOLD;
                    //This is the percentage of the total height above sea level that the current height is
                    //In other words if max is 75 and current is 25, it would be 33% of the total height that it COULD be, relative to sea level
                    float percentageAboveLand = (float)currentHeightAboveLand / (float)currentHeightAboveLand;
                    //The weight for the exponential component of the calculation
                    float exponentialComponentWeight = 0.2f;
                    //The weight for the existing data
                    float existingComponentWeight = 0.8f;
                    //calculate rVal and make sure stays above sealevel as it's terrain after all
                    rVal[x][y] = (int)(data[x][y] * Math.exp(-(1.0f - percentageAboveLand)) * exponentialComponentWeight + data[x][y] * existingComponentWeight);
                    if(rVal[x][y] < TerrainGenerator.OCEAN_THRESHOLD){
                        rVal[x][y] = TerrainGenerator.OCEAN_THRESHOLD + 1;
                    }
                    //Because of how exponentials work, this check should never pass
                    if(Math.exp(-(1.0 - percentageAboveLand)) > 1.0){
                        LoggerInterface.loggerEngine.WARNING("WARNING!!");
                    }
                } else {
                    rVal[x][y] = data[x][y];
                }
            }
        }
        return rVal;
    }
    
    int[][] generate_Interpolation(int[][] data){
        /*
        Looks at 3x3 kernel
        */
        // int kernel_offset_x[] = {-1,0,1,-1,0,1,-1,0,1};
        // int kernel_offset_y[] = {1,1,1,0,0,0,-1,-1,-1};
        int rVal[][] = new int[100][100];
        return rVal;
    }
    
    /**
     * Doubles the dimensions of the map so that the new space can be interpolated linearly using existing values
     * @param raw The original input map
     * @return The new, interpolated output map
     */
    int[][] interpolateElevationRaws(int[][] raw){
        int rVal[][] = new int[continentPhaseDimension * 2][continentPhaseDimension * 2];
        //perform interpolation
        for(int x = 0; x < continentPhaseDimension - 1; x++){
            for(int y = 0; y < continentPhaseDimension - 1; y++){
                rVal[x*2][y*2] = raw[x][y];
                rVal[x*2+1][y*2] = (raw[x][y] + raw[x+1][y])/2;
                rVal[x*2][y*2+1] = (raw[x][y] + raw[x][y+1])/2;
                rVal[x*2+1][y*2+1] = (raw[x][y] + raw[x+1][y+1])/2;
            }
        }
        //double dimension to account for new array size
        continentPhaseDimension = continentPhaseDimension * 2;
        return rVal;
    }
    
    int[][] load_Data(String path){
        int rVal[][] = null;
        try (BufferedReader br = new BufferedReader(new FileReader(path));){
            String line;
            line = br.readLine();
            int dim_x = Integer.parseInt(line);
            interpolationPhaseDimension = dim_x;
            line = br.readLine();
            int dim_y = Integer.parseInt(line);
            rVal = new int[dim_x][dim_y];
            int incrementer_x = 0;
            int incrementer_y = 0;
            while ((line = br.readLine()) != null) {
                incrementer_y = 0;
                while(line != ""){
                    rVal[incrementer_x][incrementer_y] = Integer.parseInt(Utilities.string_To_First_Space(line));
                    if(line.contains(" ")){
                        line = line.substring(Utilities.get_Position_Of_Next_Instance_Of_Char_In_String(line, ' ') + 1);
                    } else {
                        line = "";
                    }
                    incrementer_y++;
                }
                incrementer_x++;
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        return rVal;
    }
    
    public void increment_Current_Continent(){
        current_Continent++;
        if (current_Continent > numberContinents - 1) {
            current_Continent = 0;
        }
        while(continents.get(current_Continent).size < 50){
            current_Continent++;
            if(current_Continent > numberContinents - 1){
                current_Continent = 0;
            }
        }
    }
    
    /**
     * Increments the display mode of the built in data renderer
     */
    public void incrementDisplayToggle(){
        displayToggle++;
        if(displayToggle > max_Display_Toggle){
            displayToggle = 0;
        }
    }
    
    //the interpolation ratio applied to the statically generated terrain
    public void setInterpolationRatio(int interpRatio){
        interpolationRatio = interpRatio;
    }
    
    //the interpolation ratio applied to the dynamically generated terrain per chunk
    public void setDynamicInterpolationRatio(int dynInterpRatio){
        dynamicInterpRatio = dynInterpRatio;
    }
    
    //Sets the vertical interpolation ratio
    public void setVerticalInterpolationRatio(int vertInterpRatio){
        verticalInterpolationRatio = vertInterpRatio;
    }
    
    /**
     * Sets the random seed for the terrain generator
     * @param seed The seed to set
     */
    public void setRandomSeed(long seed){
        Utilities.seed_Random_Functions(seed);
    }
}
