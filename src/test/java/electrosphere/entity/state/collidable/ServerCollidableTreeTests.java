package electrosphere.entity.state.collidable;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3d;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.fall.ServerFallTree;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests for the server collidable tree
 */
public class ServerCollidableTreeTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void testCollidableFallCancel(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn on server
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        
        //wait for the creature to land
        TestEngineUtils.simulateFrames(3);

        //verify it was started on server
        assertEquals(false, ServerFallTree.getFallTree(creature).isFalling());
    }

}
