package electrosphere.server.entity;

import java.util.List;
import java.util.Random;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.data.biome.BiomeData;
import electrosphere.data.biome.BiomeFoliageDescription;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.growth.ServerGrowthComponent;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.ServerWorldData;
import electrosphere.server.macro.MacroData;
import electrosphere.server.macro.spatial.MacroAreaObject;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;
import io.github.studiorailgun.NoiseUtils;

/**
 * Generates content for a given datacell
 */
public class ServerContentGenerator {

    /**
     * The seed of the foliage
     */
    public static final int FOLIAGE_SEED = 0;

    /**
     * Adjustment applied to generated height value to approximate the height of the actual terrain.
     * The voxels don't generate QUITE to the height of the heightmap, so this is applied to make the value line up better for entity placement.
     */
    public static final float HEIGHT_MANUAL_ADJUSTMENT = -0.35f;

    /**
     * Generates content for a given data cell
     * @param realm The realm
     * @param macroData The macro data
     * @param cell The cell
     * @param worldPos The world position of the cell
     * @param randomizer The randomizer
     */
    public static void generateContent(Realm realm, MacroData macroData, ServerDataCell cell, Vector3i worldPos, long randomizer){
        //verify we have everything for chunk content gen
        if(realm.getServerWorldData() == null && realm.getServerWorldData().getServerTerrainManager() == null && realm.getServerWorldData().getServerTerrainManager().getModel() == null){
            throw new Error(
                "Trying to generate content for a realm that does not have a terrain model defined!\n" +
                realm.getServerWorldData() + "\n" +
                realm.getServerWorldData().getServerTerrainManager() + "\n" +
                realm.getServerWorldData().getServerTerrainManager().getModel()
            );
        }

        //setup
        Random random = new Random(randomizer);

        //fetches the list of macro data content blockers
        List<MacroAreaObject> macroContentBlockers = macroData.getContentBlockers();


        //generate foliage
        BiomeData biome = null;
        if(realm.getServerWorldData() != null && realm.getServerWorldData().getServerTerrainManager() != null && realm.getServerWorldData().getServerTerrainManager().getModel() != null){
            biome = realm.getServerWorldData().getServerTerrainManager().getModel().getClosestSurfaceBiome(worldPos.x, worldPos.z);
        }
        List<BiomeFoliageDescription> foliageDescriptions = biome.getSurfaceGenerationParams().getFoliageDescriptions();
        if(foliageDescriptions != null){
            for(int x = 0; x < ServerTerrainChunk.CHUNK_DIMENSION; x++){
                for(int z = 0; z < ServerTerrainChunk.CHUNK_DIMENSION; z++){
                    double height = realm.getServerWorldData().getServerTerrainManager().getElevation(worldPos.x, worldPos.z, x, z) + HEIGHT_MANUAL_ADJUSTMENT;
                    if(
                        ServerWorldData.convertVoxelToRealSpace(0, worldPos.y) < height &&
                        ServerWorldData.convertVoxelToRealSpace(ServerTerrainChunk.CHUNK_DIMENSION, worldPos.y) > height
                    ){
                        BiomeFoliageDescription toPlace = null;
                        double foundPriority = -1;
                        double realX = ServerWorldData.convertVoxelToRealSpace(x, worldPos.x);
                        double realZ = ServerWorldData.convertVoxelToRealSpace(z, worldPos.z);

                        //check if a macro object is blocking content here
                        boolean macroBlockingContent = false;
                        if(macroContentBlockers != null){
                            for(MacroAreaObject blocker : macroContentBlockers){
                                if(blocker.getAABB().testPoint(realX, height, realZ)){
                                    macroBlockingContent = true;
                                    break;
                                }
                            }
                            if(macroBlockingContent){
                                continue;
                            }
                        }

                        //figure out which foliage to place
                        for(BiomeFoliageDescription foliageDescription : foliageDescriptions){
                            double scale = foliageDescription.getScale();
                            double regularity = foliageDescription.getRegularity();
                            double threshold = foliageDescription.getThreshold();
                            double value = NoiseUtils.relaxedPointGen(realX * scale, realZ * scale, regularity, threshold);
                            if(value > 0){
                                if(toPlace == null){
                                    foundPriority = foliageDescription.getPriority();
                                    toPlace = foliageDescription;
                                } else if(foliageDescription.getPriority() > foundPriority) {
                                    foundPriority = foliageDescription.getPriority();
                                    toPlace = foliageDescription;
                                }
                            }
                        }
                        if(toPlace != null){
                            String type = toPlace.getEntityIDs().get(random.nextInt(0,toPlace.getEntityIDs().size()));
                            if(Globals.gameConfigCurrent.getFoliageMap().getType(type) == null){
                                LoggerInterface.loggerEngine.WARNING("Foliage declared in biome " + biome.getDisplayName() + " does not exist " + type);
                                continue;
                            }
                            Entity foliage = FoliageUtils.serverSpawnTreeFoliage(
                                realm,
                                new Vector3d(
                                    realX,
                                    height,
                                    realZ
                                ),
                                type
                            );
                            if(ServerGrowthComponent.hasServerGrowthComponent(foliage)){
                                ServerGrowthComponent.getServerGrowthComponent(foliage).maxGrowth();
                            }
                        }
                    }
                }
            }
        }

    }
    
}
