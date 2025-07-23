package electrosphere.entity.state.gravity;

import static electrosphere.test.testutils.Assertions.assertEventually;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.jump.ServerJumpTree;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests server gravity tree
 */
public class ServerGravityTreeTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void isActive_OnSpawn_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerGravityTree serverGravityTree = ServerGravityTree.getServerGravityTree(serverEntity);
        assertTrue(serverGravityTree.isActive());
    }

    @IntegrationTest
    public void physicsIsValid_eventually_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerGravityTree serverGravityTree = ServerGravityTree.getServerGravityTree(serverEntity);
        assertEventually(() -> serverGravityTree.physicsIsValid());
    }

    @IntegrationTest
    public void isActive_AfterOneFrame_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        //settle engine
        TestEngineUtils.simulateFrames(1);

        ServerGravityTree serverGravityTree = ServerGravityTree.getServerGravityTree(serverEntity);
        assertFalse(serverGravityTree.isActive());
    }

    @IntegrationTest
    public void activates_on_jump(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        //settle engine
        TestEngineUtils.simulateFrames(1);

        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(serverEntity);
        serverJumpTree.start();

        ServerGravityTree serverGravityTree = ServerGravityTree.getServerGravityTree(serverEntity);

        assertEventually(() -> serverGravityTree.isActive());
    }

    @IntegrationTest
    public void deactivates_on_collide_world_bound(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerGravityTree serverGravityTree = ServerGravityTree.getServerGravityTree(serverEntity);

        //wait for gravity to settle
        assertTrue(serverGravityTree.isActive());
        TestEngineUtils.simulateFrames(1);
        assertEventually(() -> !serverGravityTree.isActive());

        //jump
        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(serverEntity);
        serverJumpTree.start();

        //wait for jump to activate it
        assertEventually(() -> serverGravityTree.isActive());


        //wait for it to settle after jump finishes
        assertEventually(() -> !serverGravityTree.isActive());
    }

}
