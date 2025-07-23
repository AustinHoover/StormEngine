package electrosphere.server.physics.terrain.generation.continentphase;

/**
 * Contains information about a continent
 */
class Continent {
    //dimensions of the condinent
    public int dim_x;
    public int dim_y;
    public int size = 0;
    public int[][] elevation;
    public int[][] chart_Precipitation;
    public int[][] chart_Temperature;
    public int[][] chart_Climate;
    public int[][] chart_Wind_Macro;
    public Region[][] regions;
}
