package electrosphere.server.physics.terrain.generation.continentphase;

/**
 *
 * @author satellite
 */
class Region {
    
    public static int REGION_DIMENSION = 100;
    int chart_Elevation[][];
    int chart_Drainage[][];
    int chart_Max_Water_Flow[][];
    int elevation_Goal;
    Region neighbors[][];
    public boolean finished_Drainage_Simulation = false;

    protected Region(){
        neighbors = new Region[3][3];
        neighbors[1][1] = null;
    }

}
