package electrosphere.server.macro.civilization.town;

import electrosphere.engine.Globals;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.CharacterUtils;
import electrosphere.server.macro.civilization.Civilization;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.server.macro.spatial.MacroLODObject;
import electrosphere.server.macro.structure.VirtualStructure;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.joml.AABBd;
import org.joml.Vector3d;

/**
 * Server representation of a town
 */
public class Town implements MacroAreaObject, MacroLODObject {
    
    /**
     * Minimum data resolution town (ie hasn't generated structures or residents)
     */
    public static final int TOWN_RES_MIN = 0;

    /**
     * Maximim data resolution town (has fully generated all default structures and residents)
     */
    public static final int TOWN_RES_MAX = 1;

    /**
     * The id of the town
     */
    private int id;

    /**
     * Incrementer for IDs
     */
    static int idIncrementer = 0;

    /**
     * The resolution of data that has been generated for this town
     */
    private int resolution = TOWN_RES_MIN;
    
    /**
     * The position of the town
     */
    private Vector3d position;

    /**
     * The radius of the town
     */
    private double radius;
    
    /**
     * The structures inside the town
     */
    private List<Integer> structures = new LinkedList<Integer>();

    /**
     * The residents of the town
     */
    private List<Integer> residents = new LinkedList<Integer>();

    /**
     * The list of jobs queued in the town
     */
    private List<TownJob> jobs = new LinkedList<TownJob>();

    /**
     * The list of farm plot region idss
     */
    private List<Long> farmPlotRegions = new LinkedList<Long>();

    /**
     * The id of the parent civilization
     */
    private int parentCivId;

    /**
     * Constructor
     */
    private Town(){
        this.id = idIncrementer;
        idIncrementer++;
    }
    
    /**
     * Creates a town
     * @param position The position of the town
     * @param radius The radius of the town
     * @param parentCiv The id of the parent civilization
     * @return The town
     */
    public static Town createTown(MacroData macroData, Vector3d position, double radius, int parentCiv){
        Town rVal = new Town();
        rVal.position = position;
        rVal.radius = radius;
        rVal.parentCivId = parentCiv;
        macroData.addTown(rVal);
        return rVal;
    }
    
    /**
     * Adds a structure to the town
     * @param structure The structure
     */
    public void addStructure(VirtualStructure structure){
        structures.add(structure.getId());
    }
    
    /**
     * Gets the structures that are a part of the town
     * @return The list of structures
     */
    public List<VirtualStructure> getStructures(MacroData macroData){
        return structures.stream().map((Integer id) -> macroData.getStructure(id)).filter((VirtualStructure struct) -> struct != null).collect(Collectors.toList());
    }
    
    /**
     * Adds a resident to the town
     * @param resident The new resident
     */
    public void addResident(Character resident){
        residents.add(resident.getId());
        CharacterUtils.addHometown(resident, this);
    }
    
    /**
     * Gets the list of residents of the town
     * @return The list of residents
     */
    public List<Character> getResidents(MacroData macroData){
        return residents.stream().map((Integer id) -> Globals.serverState.characterService.getCharacter(id)).filter((Character chara) -> chara != null).collect(Collectors.toList());
    }

    /**
     * Gets the farm plots in the town
     * @param macroData The macro data
     * @return The list of farm plots
     */
    public List<MacroRegion> getFarmPlots(MacroData macroData){
        List<MacroRegion> regions = this.farmPlotRegions.stream().map((Long id) -> macroData.getRegion(id)).collect(Collectors.toList());
        return regions;
    }

    /**
     * Adds a farm plot region
     * @param farmPlotRegion The region for the farm plot
     */
    public void addFarmPlot(MacroRegion farmPlotRegion){
        this.farmPlotRegions.add(farmPlotRegion.getId());
    }

    /**
     * Adds a job to the town
     * @param job The job
     */
    public void addJob(TownJob job){
        this.jobs.add(job);
    }

    /**
     * Gets the jobs in the town
     * @return The list of jobs
     */
    public List<TownJob> getJobs(){
        return jobs;
    }

    /**
     * Removes a job from the town
     * @param job The job
     */
    public void removeJob(TownJob job){
        this.jobs.remove(job);
    }

    @Override
    public Vector3d getPos() {
        return this.position;
    }

    @Override
    public void setPos(Vector3d pos) {
        this.position = pos;
    }

    @Override
    public AABBd getAABB() {
        return new AABBd(new Vector3d(this.position).sub(this.radius,this.radius,this.radius), new Vector3d(this.position).add(this.radius,this.radius,this.radius));
    }

    /**
     * Sets the id of the town
     * @param id The id of the town
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Gets the ID of the town
     * @return The ID
     */
    public int getId(){
        return this.id;
    }

    /**
     * Gets the resolution of the data that has been generated for this town
     * @return The resolution of the data
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution of the data that has been generated for this town
     * @param resolution The resolution of the data
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    /**
     * Gets the parent civilization
     * @param macroData The macro data
     * @return The parent civilization
     */
    public Civilization getParent(MacroData macroData){
        return macroData.getCivilization(this.parentCivId);
    }

    @Override
    public boolean isFullRes() {
        return this.resolution == TOWN_RES_MAX;
    }
    
}
