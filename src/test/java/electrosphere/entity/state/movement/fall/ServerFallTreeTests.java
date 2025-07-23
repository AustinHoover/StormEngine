package electrosphere.entity.state.movement.fall;

import static org.junit.jupiter.api.Assertions.*;


import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import static electrosphere.test.testutils.Assertions.*;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the server fall tree
 */
public class ServerFallTreeTests extends EntityTestTemplate {

    @IntegrationTest
    public void isFalling_AtRest_false(){
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerFallTree serverFallTree = ServerFallTree.getFallTree(creature);
        assertEquals(false, serverFallTree.isFalling());
    }

    @IntegrationTest
    public void isFalling_AfterJump_true(){
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerFallTree serverFallTree = ServerFallTree.getFallTree(creature);
        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(creature);
        serverJumpTree.start();

        //make sure we're in in the air
        TestEngineUtils.waitForCondition(() -> !serverJumpTree.isJumping(), 100);

        //Make sure we're eventually falling
        assertEventually(() -> serverFallTree.isFalling());
    }
    
    @IntegrationTest
    public void isFalling_AfterLand_false(){
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerFallTree serverFallTree = ServerFallTree.getFallTree(creature);
        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(creature);
        serverJumpTree.start();

        //make sure we're in in the air
        TestEngineUtils.simulateFrames(3);


        assertEventually(() -> {
            return !serverFallTree.isFalling();
        });
    }

}
