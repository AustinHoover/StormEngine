package electrosphere.server.entity;

import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.ServerWorldData;

import java.util.Random;

import org.joml.Vector3d;
import org.joml.Vector3i;

/**
 * Generates content for server data cells based on an environment type
 */
public class EnvironmentGenerator {
    
    
    public static void generatePlains(Realm realm, ServerDataCell cell, Vector3i worldPos, long randomizer){
        Random rand = new Random(randomizer);
        int targetNum = (int)(rand.nextFloat() * 10) + 10;
        LoggerInterface.loggerGameLogic.DEBUG("generate plains");
        for(int i = 0; i < targetNum; i++){
//             Entity newTree = FoliageUtils.spawnBasicFoliage("FallOak1");
//             cell.getScene().registerEntity(newTree);
//             double posX = worldPos.x * Globals.serverWorldData.getDynamicInterpolationRatio() + (float)(rand.nextFloat() * Globals.serverWorldData.getDynamicInterpolationRatio());
//             double posZ = worldPos.z * Globals.serverWorldData.getDynamicInterpolationRatio() + (float)(rand.nextFloat() * Globals.serverWorldData.getDynamicInterpolationRatio());
//             double posY = Globals.serverTerrainManager.getHeightAtPosition(posX, posZ);
// //            System.out.println("Spawning tree at: " + posX + "," + posY + "," + posZ);
// //            CollisionObjUtils.positionCharacter(newTree, new Vector3f(posX,posY,posZ));
//             EntityUtils.getPosition(newTree).set(posX,posY,posZ);
        }
    }

    public static void generateForest(Realm realm, ServerDataCell cell, Vector3i worldPos, long randomizer){
        Random rand = new Random(randomizer);
        int targetNum = (int)(rand.nextFloat() * 3) + 3;
        LoggerInterface.loggerGameLogic.DEBUG("generate forest");
        for(int i = 0; i < targetNum; i++){
            Vector3d position = new Vector3d(
                ServerWorldData.convertWorldToReal(worldPos.x) + rand.nextFloat() * 16,
                0,
                ServerWorldData.convertWorldToReal(worldPos.z) + rand.nextFloat() * 16
            );
            FoliageUtils.serverSpawnTreeFoliage(realm, position, "oak");
        }
    }
}
