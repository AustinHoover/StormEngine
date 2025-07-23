package electrosphere.data;

import java.util.LinkedList;
import java.util.List;

import electrosphere.data.audio.SurfaceAudioCollection;
import electrosphere.data.biome.BiomeTypeMap;
import electrosphere.data.block.BlockData;
import electrosphere.data.crafting.RecipeDataMap;
import electrosphere.data.entity.common.CommonEntityLoader;
import electrosphere.data.entity.common.CommonEntityMap;
import electrosphere.data.entity.common.CommonEntityType;
import electrosphere.data.entity.creature.CreatureData;
import electrosphere.data.entity.creature.CreatureTypeLoader;
import electrosphere.data.entity.creature.CreatureTypeMap;
import electrosphere.data.entity.creature.attack.AttackMoveResolver;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.foliage.FoliageTypeLoader;
import electrosphere.data.entity.foliage.FoliageTypeMap;
import electrosphere.data.entity.item.ItemDataMap;
import electrosphere.data.entity.item.source.ItemSourcingMap;
import electrosphere.data.entity.projectile.ProjectileTypeHolder;
import electrosphere.data.macro.job.CharaJobDataLoader;
import electrosphere.data.macro.struct.StructureDataLoader;
import electrosphere.data.macro.units.UnitDefinitionFile;
import electrosphere.data.macro.units.UnitLoader;
import electrosphere.data.settings.UserSettings;
import electrosphere.data.tutorial.HintDefinition;
import electrosphere.data.voxel.VoxelData;
import electrosphere.data.voxel.sampler.SamplerFile;
import electrosphere.logger.LoggerInterface;
import electrosphere.net.config.NetConfig;
import electrosphere.server.macro.character.race.RaceMap;
import electrosphere.server.macro.symbolism.SymbolMap;
import electrosphere.util.FileUtils;

/**
 * Current configuration for the data of the game
 */
public class Config {

    /**
     * Top level user settings object
     */
    private UserSettings userSettings;

    /**
     * Optional config that can be included alongside engine to inject a default network to populate on the multiplayer screens
     */
    private NetConfig netConfig;
    
    /**
     * The container for all creature definitions
     */
    private CreatureTypeLoader creatureTypeLoader;

    /**
     * The container for all item definitions
     */
    private ItemDataMap itemMap;

    /**
     * The container for all foliage definitions
     */
    private FoliageTypeLoader foliageMap;

    /**
     * The object type loader
     */
    private CommonEntityMap objectTypeLoader;

    /**
     * The symbol map
     */
    private SymbolMap symbolMap;

    /**
     * The race data
     */
    private RaceMap raceMap;

    /**
     * The projectile data holder
     */
    private ProjectileTypeHolder projectileTypeHolder;

    /**
     * data about every voxel type
     */
    private VoxelData voxelData;

    /**
     * The block data
     */
    private BlockData blockData;

    /**
     * the hints that are defined
     */
    private HintDefinition hintData;

    /**
     * The surface audio definitions
     */
    private SurfaceAudioCollection surfaceAudioCollection;

    /**
     * The unit loader
     */
    private UnitLoader unitLoader;

    /**
     * The crafting recipe map
     */
    private RecipeDataMap recipeMap;

    /**
     * The biome map
     */
    private BiomeTypeMap biomeMap;

    /**
     * The list of sampler definitions
     */
    private List<SamplerFile> samplerDefinitions;

    /**
     * The structure data
     */
    private StructureDataLoader structureData;

    /**
     * The item sourcing map for items
     */
    private ItemSourcingMap itemSourcingMap;

    /**
     * Definitions for job types
     */
    private CharaJobDataLoader charaJobs;
    
    /**
     * Loads the default data
     * @return The config
     */
    public static Config loadDefaultConfig(){
        Config config = new Config();
        config.userSettings = UserSettings.loadUserSettings();
        config.netConfig = NetConfig.readNetConfig();
        config.creatureTypeLoader = Config.loadCreatureTypes("Data/entity/creatures.json");
        config.itemMap = ItemDataMap.loadItemFiles("Data/entity/items.json");
        config.foliageMap = Config.loadFoliageTypes("Data/entity/foliage.json");
        config.objectTypeLoader = Config.loadCommonEntityTypes("Data/entity/objects.json");
        config.symbolMap = FileUtils.loadObjectFromAssetPath("Data/game/symbolism.json", SymbolMap.class);
        config.raceMap = FileUtils.loadObjectFromAssetPath("Data/game/races.json", RaceMap.class);
        config.voxelData = FileUtils.loadObjectFromAssetPath("Data/game/voxelTypes.json", VoxelData.class);
        config.blockData = FileUtils.loadObjectFromAssetPath("Data/game/blockTypes.json", BlockData.class);
        config.blockData.constructSolidsMap();
        config.projectileTypeHolder = FileUtils.loadObjectFromAssetPath("Data/entity/projectile.json", ProjectileTypeHolder.class);
        config.hintData = FileUtils.loadObjectFromAssetPath("Data/tutorial/hints.json", HintDefinition.class);
        config.surfaceAudioCollection = FileUtils.loadObjectFromAssetPath("Data/audio/surface.json", SurfaceAudioCollection.class);
        config.projectileTypeHolder.init();
        config.unitLoader = UnitLoader.create(FileUtils.loadObjectFromAssetPath("Data/game/units/units.json", UnitDefinitionFile.class));
        config.recipeMap = RecipeDataMap.loadRecipeFiles("Data/game/recipes.json");
        config.biomeMap = BiomeTypeMap.loadBiomeFile("Data/game/biomes.json");
        config.samplerDefinitions = SamplerFile.readSamplerDefinitionFiles("Data/game/voxel");
        config.structureData = StructureDataLoader.loadStructureFiles("Data/game/structure.json");
        config.charaJobs = CharaJobDataLoader.loadJobFiles("Data/macro/jobs.json");

        //create procedural item types
        ItemDataMap.loadSpawnItems(config.itemMap, config.recipeMap, config.objectTypeLoader);
        ItemDataMap.generateBlockItems(config.itemMap, config.blockData);
        ItemDataMap.generateVoxelItems(config.itemMap, config.voxelData);

        //construct the sourcing map
        config.itemSourcingMap = ItemSourcingMap.parse(config);

        //validate
        ConfigValidator.valdiate(config);

        
        return config;
    }

    /**
     * Saves all edits made to the config
     * @param config The config
     */
    public static void save(Config config){
        LoggerInterface.loggerFileIO.WARNING("Warning! Creatures, items, objects, voxels, blocks are all unsupported currently!");
        config.structureData.save();
    }

    /**
     * Reads a child entity defintion file
     * @param filename The filename
     * @return The list of entities in the file
     */
    private static List<CommonEntityType> recursiveReadEntityLoader(String filename){
        List<CommonEntityType> typeList = new LinkedList<CommonEntityType>();
        CommonEntityLoader loaderFile = FileUtils.loadObjectFromAssetPath(filename, CommonEntityLoader.class);
        //push the types from this file
        for(CommonEntityType type : loaderFile.getEntities()){
            typeList.add(type);
        }
        //push types from any other files
        for(String filepath : loaderFile.getFiles()){
            List<CommonEntityType> parsedTypeList = recursiveReadEntityLoader(filepath);
            for(CommonEntityType type : parsedTypeList){
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
     * Loads all common entity definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The common entity defintion interface
     */
    private static CommonEntityMap loadCommonEntityTypes(String initialPath) {
        CommonEntityMap rVal = new CommonEntityMap();
        List<CommonEntityType> typeList = recursiveReadEntityLoader(initialPath);
        for(CommonEntityType type : typeList){
            rVal.putType(type.getId(), type);
        }
        return rVal;
    }

    /**
     * Reads a child creature defintion file
     * @param filename The filename
     * @return The list of creatures in the file
     */
    private static List<CreatureData> readCreatureTypeFile(String filename){
        List<CreatureData> typeList = new LinkedList<CreatureData>();
        CreatureTypeMap typeMap = FileUtils.loadObjectFromAssetPath(filename, CreatureTypeMap.class);
        //push the types from this file
        for(CreatureData type : typeMap.getCreatures()){
            typeList.add(type);
        }
        //push types from any other files
        for(String filepath : typeMap.getFiles()){
            List<CreatureData> parsedTypeList = Config.readCreatureTypeFile(filepath);
            for(CreatureData type : parsedTypeList){
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
     * Loads all creature definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The creature defintion interface
     */
    private static CreatureTypeLoader loadCreatureTypes(String initialPath) {
        CreatureTypeLoader loader = new CreatureTypeLoader();
        List<CreatureData> typeList = Config.readCreatureTypeFile(initialPath);
        for(CreatureData type : typeList){
            if(type.getAttackMoves() != null){
                type.setAttackMoveResolver(new AttackMoveResolver(type.getAttackMoves()));
            }
            loader.putType(type.getId(), type);
            //loop through all creatures and add ones with PLAYABLE token to list of playable races
            for(String token : type.getTokens()){
                if(token.contains("PLAYABLE")){
                    loader.putPlayableRace(type.getId());
                }
            }
        }
        return loader;
    }

     /**
     * Reads a child foliage defintion file
     * @param filename The filename
     * @return The list of foliage in the file
     */
    private static List<FoliageType> readFoliageTypeFile(String filename){
        List<FoliageType> typeList = new LinkedList<FoliageType>();
        FoliageTypeMap typeMap = FileUtils.loadObjectFromAssetPath(filename, FoliageTypeMap.class);
        //push the types from this file
        for(FoliageType foliage : typeMap.getFoliageList()){
            typeList.add(foliage);
        }
        //push types from any other files
        for(String filepath : typeMap.getFiles()){
            List<FoliageType> parsedTypeList = Config.readFoliageTypeFile(filepath);
            for(FoliageType type : parsedTypeList){
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
     * Loads all creature definition files recursively
     * @param initialPath The initial path to recurse from
     * @return The creature defintion interface
     */
    private static FoliageTypeLoader loadFoliageTypes(String initialPath) {
        FoliageTypeLoader loader = new FoliageTypeLoader();
        List<FoliageType> typeList = Config.readFoliageTypeFile(initialPath);
        for(FoliageType type : typeList){
            loader.putType(type.getId(), type);
        }
        return loader;
    }

    /**
     * Gets the user settings
     * @return The user settings
     */
    public UserSettings getSettings(){
        return this.userSettings;
    }

    /**
     * Gets the interface for creature definitions loaded into memory
     * @return The interface
     */
    public CreatureTypeLoader getCreatureTypeLoader() {
        return creatureTypeLoader;
    }

    /**
     * Gets the data on all item types in memory
     * @return the data on all items
     */
    public ItemDataMap getItemMap() {
        return itemMap;
    }

    /**
     * Gets the data on all foliage types in memory
     * @return The foliage data
     */
    public FoliageTypeLoader getFoliageMap() {
        return foliageMap;
    }
    
    /**
     * Gets the symbolism map
     * @return The symbolism map
     */
    public SymbolMap getSymbolMap() {
        return symbolMap;
    }
    
    /**
     * Gets the data that defines all creature races in the engine
     * @return The data on all races
     */
    public RaceMap getRaceMap() {
        return raceMap;
    }

    /**
     * Gets the definitions of all object entities in memory
     * @return The objects
     */
    public CommonEntityMap getObjectTypeMap() {
        return objectTypeLoader;
    }

    /**
     * Gets the definitions all projectiles
     * @return the projectile data
     */
    public ProjectileTypeHolder getProjectileMap(){
        return projectileTypeHolder;
    }

    /**
     * Gets the voxel data
     * @return The voxel data
     */
    public VoxelData getVoxelData(){
        return voxelData;
    }

    /**
     * Gets the block data
     * @return The block data
     */
    public BlockData getBlockData(){
        return blockData;
    }

    /**
     * The tutorial hints data
     * @return The hints
     */
    public HintDefinition getHintData(){
        return hintData;
    }

    /**
     * Gets the surface audio collection
     * @return The surface audio collection
     */
    public SurfaceAudioCollection getSurfaceAudioCollection(){
        return this.surfaceAudioCollection;
    }

    /**
     * Gets the unit loader
     * @return The unit loader
     */
    public UnitLoader getUnitLoader(){
        return unitLoader;
    }

    /**
     * Gets the recipe map
     * @return The recipe map
     */
    public RecipeDataMap getRecipeMap(){
        return recipeMap;
    }

    /**
     * Gets the biome map
     * @return The biome map
     */
    public BiomeTypeMap getBiomeMap(){
        return biomeMap;
    }

    /**
     * Gets the list of all sampler files
     * @return The list of all sampler files
     */
    public List<SamplerFile> getSamplerFiles(){
        return this.samplerDefinitions;
    }

    /**
     * Gets the structure data
     * @return The structure data
     */
    public StructureDataLoader getStructureData(){
        return this.structureData;
    }

    /**
     * Gets the item sourcing map of the config
     * @return The item sourcing map
     */
    public ItemSourcingMap getItemSourcingMap(){
        return this.itemSourcingMap;
    }

    /**
     * Gets the network config file if it exists
     * @return The config if it exists, null otherwise
     */
    public NetConfig getNetConfig(){
        return netConfig;
    }

    /**
     * Gets the job definitions
     * @return The job definitions
     */
    public CharaJobDataLoader getJobDefinitions(){
        return charaJobs;
    }
    
}
