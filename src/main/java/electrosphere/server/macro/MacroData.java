package electrosphere.server.macro;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.data.macro.temporal.MacroTemporalData;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.character.Character;
import electrosphere.server.macro.character.data.CharacterDataStrings;
import electrosphere.server.macro.character.race.Race;
import electrosphere.server.macro.character.race.RaceMap;
import electrosphere.server.macro.civilization.Civilization;
import electrosphere.server.macro.civilization.CivilizationGenerator;
import electrosphere.server.macro.civilization.road.Road;
import electrosphere.server.macro.civilization.town.Town;
import electrosphere.server.macro.region.MacroRegion;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.server.macro.spatial.MacroObject;
import electrosphere.server.macro.spatial.path.MacroPathCache;
import electrosphere.server.macro.structure.VirtualStructure;
import electrosphere.util.FileUtils;
import electrosphere.util.annotation.Exclude;
import electrosphere.util.math.GeomUtils;

import java.util.Random;

import org.joml.AABBd;
import org.joml.Vector3d;

/**
 * Server macro level data
 */
public class MacroData {

    /**
     * List of races
     */
    private List<Race> races = new LinkedList<Race>();

    /**
     * List of civilizations
     */
    private List<Civilization> civilizations = new LinkedList<Civilization>();

    /**
     * List of towns
     */
    private List<Town> towns = new LinkedList<Town>();

    /**
     * List of structures
     */
    private List<VirtualStructure> structures = new LinkedList<VirtualStructure>();

    /**
     * The list of all regions
     */
    private List<MacroRegion> regions = new LinkedList<MacroRegion>();

    /**
     * Maps structure id -> structure
     */
    @Exclude
    private Map<Integer,VirtualStructure> idStructMap = new HashMap<Integer,VirtualStructure>();

    /**
     * Maps region id -> region
     */
    @Exclude
    private Map<Long,MacroRegion> idRegionMap = new HashMap<Long,MacroRegion>();

    /**
     * List of roads
     */
    private List<Road> roads = new LinkedList<Road>();

    /**
     * The pathing cache
     */
    private MacroPathCache pathingCache = new MacroPathCache();

    /**
     * The macro temporal data
     */
    private MacroTemporalData temporalData = new MacroTemporalData();

    /**
     * Generates a world
     * @param seed The seed for the world
     * @return The world
     */
    public static MacroData generateWorld(long seed, ServerWorldData serverWorldData){
        Random random = new Random(seed);
        MacroData rVal = new MacroData();

        
        //generate initial races
        if(Globals.gameConfigCurrent.getRaceMap().getRaces().size() < 3){
            for(Race race : Globals.gameConfigCurrent.getRaceMap().getRaces()){
                rVal.races.add(race);
            }
        } else {
            RaceMap raceMap = Globals.gameConfigCurrent.getRaceMap();
            int numRacesToGenerate = 3 + random.nextInt(Math.min(raceMap.getRaces().size() - 3,7));
            for(int i = 0; i < numRacesToGenerate; i++){
                Race raceToAdd = raceMap.getRaces().get(random.nextInt(raceMap.getRaces().size()));
                while(rVal.races.contains(raceToAdd)){
                    raceToAdd = raceMap.getRaces().get(random.nextInt(raceMap.getRaces().size()));
                }
                rVal.races.add(raceToAdd);
            }
        }

        //init civilizations
        CivilizationGenerator.generate(serverWorldData, rVal, Globals.gameConfigCurrent);
        
        //spawn initial characters in each race
        //find initial positions to place characters at per race
        //generate initial characters
        //place them
        // List<Vector2i> occupiedStartingPositions = new LinkedList<Vector2i>();
        // for(Race race : rVal.races){
        //     boolean foundPlacementLocation = false;
        //     int attempts = 0;
        //     while(!foundPlacementLocation){
        //         // Vector2i start = new Vector2i(random.nextInt(Globals.serverTerrainManager.getWorldDiscreteSize()),random.nextInt(Globals.serverTerrainManager.getWorldDiscreteSize()));
        //         // //are we above sea level?
        //         // if(Globals.serverTerrainManager.getDiscreteValue(start.x, start.y) > 25){
        //         //     //is this position already occupied?
        //         //     boolean match = false;
        //         //     for(Vector2i known : occupiedStartingPositions){
        //         //         if(known.x == start.x && known.y == start.y){
        //         //             match = true;
        //         //             break;
        //         //         }
        //         //     }
        //         //     if(!match){
        //         //         //occupy position
        //         //         occupiedStartingPositions.add(start);
        //         //         foundPlacementLocation = true;
        //         //         //make characters
        //         //         int numCharactersToMake = 5 + random.nextInt(20);
        //         //         for(int i = 0; i < numCharactersToMake; i++){
        //         //             Character character = new Character();
        //         //             CharacterUtils.addDiscretePosition(character, start.x, start.y);
        //         //             CharacterUtils.addRace(character, race);
        //         //             rVal.characters.add(character);
        //         //             rVal.aliveCharacters.add(character);
        //         //         }
        //         //     }
        //         // }
        //         // attempts++;
        //         // if(attempts > MAX_PLACEMENT_ATTEMPTS){
        //         //     break;
        //         // }
        //     }
        // }
        
        return rVal;
    }

    /**
     * Rebuilds datastructures for the macro data
     */
    public void rebuildDatastructures(){
        for(VirtualStructure struct : this.structures){
            this.idStructMap.put(struct.getId(),struct);
        }
        for(MacroRegion region : this.regions){
            this.idRegionMap.put(region.getId(),region);
        }
        if(this.pathingCache != null){
            this.pathingCache.reconstruct();
        }
    }

    /**
     * Saves this macro data to a save path
     * @param saveName The name of the save
     */
    public void save(String saveName){
        FileUtils.serializeObjectToSavePath(saveName, "./macro.json", this);
    }
    
    /**
     * Gets the list of civilizations
     * @return The list of civilizations
     */
    public List<Civilization> getCivilizations(){
        return civilizations;
    }

    /**
     * Gets a civilization by its id
     * @param id The id
     * @return The civilization if it exists, null otherwise
     */
    public Civilization getCivilization(int id){
        for(Civilization civ : this.civilizations){
            if(civ.getId() == id){
                return civ;
            }
        }
        return null;
    }
    
    /**
     * Adds a civilization
     * @param race the race founding the civilization
     */
    public void addCivilization(Civilization civ){
        civ.setId(civilizations.size());
        civilizations.add(civ);
    }
    
    /**
     * Gets the list of towns
     * @return The list of towns
     */
    public List<Town> getTowns(){
        return towns;
    }
    
    /**
     * Adds a town
     * @param center The center point of the town
     * @param radius The radius of the town
     * @param parentCivId The id of the parent civilization
     */
    public void addTown(Town town){
        town.setId(towns.size());
        towns.add(town);
    }

    /**
     * Gets a town by its id
     * @param id The id of the town
     * @return The town
     */
    public Town getTown(int id){
        for(Town town : towns){
            if(town.getId() == id){
                return town;
            }
        }
        return null;
    }
    
    /**
     * Adds a road
     * @param road The road
     */
    public void addRoad(Road road){
        road.setId(this.roads.size());
        this.roads.add(road);
    }

    /**
     * Gets the roads in the macro data
     * @return The list of roads
     */
    public List<Road> getRoads(){
        return this.roads;
    }

    /**
     * Registers a macro region
     * @param region The macro region
     */
    public void registerRegion(MacroRegion region){
        region.setId(regions.size());
        regions.add(region);
        idRegionMap.put(region.getId(),region);
    }

    /**
     * Gets a macro region by its id
     * @param id The id
     * @return The macro region if it exists, null otherwise
     */
    public MacroRegion getRegion(long id){
        return idRegionMap.get(id);
    }

    /**
     * Gets the list of structures
     * @return The list of structures
     */
    public List<VirtualStructure> getStructures(){
        return structures;
    }

    /**
     * Gets a structure by its id
     * @param id The id of the structure
     * @return The structure if it exists, null otherwise
     */
    public VirtualStructure getStructure(int id){
        return this.idStructMap.get(id);
    }
    
    /**
     * Adds a structure
     * @param structure The structure
     */
    public void addStructure(VirtualStructure structure){
        structure.setId(structures.size());
        structures.add(structure);
        this.idStructMap.put(structure.getId(),structure);
    }

    /**
     * Gets the nearby objects
     * @param position The position to search near
     * @return The list of objects
     */
    public List<MacroObject> getNearbyObjects(Vector3d position){
        List<MacroObject> rVal = new LinkedList<MacroObject>();
        rVal.addAll(this.roads);
        rVal.addAll(this.towns);
        return rVal;
    }

    /**
     * Checks if the aabb intersects any existing structs
     * @param aabb The aabb
     * @return true if it intersects any existing structs, false otheriwse
     */
    public boolean intersectsStruct(AABBd aabb){
        for(VirtualStructure struct : this.structures){
            if(struct.getAABB().testAABB(aabb)){
                return true;
            }
        }
        for(Road road : this.roads){
            //broad phase
            if(road.getAABB().testAABB(aabb)){
                //near phase
                if(GeomUtils.intersectAABBTube(aabb, road.getPoint1(), road.getPoint2(), road.getRadius())){
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Describes the world
     */
    public void describeWorld(){
        LoggerInterface.loggerEngine.WARNING("Initial dieties");
        LoggerInterface.loggerEngine.WARNING("==========================");
        // for(Character chara : initialDieties){
        //     LoggerInterface.loggerEngine.WARNING("Diety");
        //     Diety diety = CharacterUtils.getDiety(chara);
        //     for(Symbol symbol : diety.getSymbols()){
        //         LoggerInterface.loggerEngine.WARNING(symbol.getName() + " ");
        //     }
        //     LoggerInterface.loggerEngine.WARNING("\n");
        // }
        LoggerInterface.loggerEngine.WARNING("==========================");
        LoggerInterface.loggerEngine.WARNING("\n\n");
        LoggerInterface.loggerEngine.WARNING("Initial races");
        LoggerInterface.loggerEngine.WARNING("==========================");
        for(Race race : races){
            LoggerInterface.loggerEngine.WARNING(race.getId());
            int numCharsOfRace = 0;
            //n*m complexity - yikes! - as long as we're not making a million chars at start this should be _ok_
            for(Character chara : Globals.serverState.characterService.getAllCharacters()){
                if(chara.containsKey(CharacterDataStrings.RACE)){
                    if(Race.getRace(chara).equals(race)){
                        numCharsOfRace++;
                    }
                }
            }
            LoggerInterface.loggerEngine.WARNING(numCharsOfRace + " initial characters");
            LoggerInterface.loggerEngine.WARNING("\n");
        }
        LoggerInterface.loggerEngine.WARNING("==========================");
    }

    /**
     * Gets the list of content-blocking macro objects
     * @return The list
     */
    public List<MacroAreaObject> getContentBlockers(){
        List<MacroAreaObject> blockers = new LinkedList<MacroAreaObject>();
        blockers.addAll(this.structures);
        blockers.addAll(this.roads);
        blockers.addAll(this.regions);
        return blockers;
    }

    /**
     * Gets the path cache in the macro data
     * @return The macro pathing cache
     */
    public MacroPathCache getPathCache(){
        return this.pathingCache;
    }

    /**
     * Gets the temporal data
     * @return The temporal data
     */
    public MacroTemporalData getTemporalData(){
        return this.temporalData;
    }
    
}
