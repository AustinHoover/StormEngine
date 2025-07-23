package electrosphere.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.engine.Globals;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEntityUtils;

/**
 * Basic entity spawning integration tests
 */
public class SpawningCreaturesTest extends EntityTestTemplate {
    
    //must wait on viewport testing, otherwise number of entities isn't going to be correct because the player character is spawning
    @IntegrationTest
    public void testSpawnCreature(){
        CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        assertEquals(1, TestEntityUtils.numberOfEntitiesInBox(new Vector3d(-1,-1,-1),new Vector3d(1,1,1)));
    }

    //must wait on viewport testing, otherwise number of entities isn't going to be correct because the player character is spawning
    @IntegrationTest
    public void testSpawnMultipleCreatures(){
        int numberToSpawn = 100;
        for(int i = 0; i < numberToSpawn; i++){
            CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        }
        assertEquals(numberToSpawn, TestEntityUtils.numberOfEntitiesInBox(new Vector3d(-1,-1,-1),new Vector3d(1,1,1)));
    }

}
