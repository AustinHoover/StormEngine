package electrosphere.entity.state.movement.jump;

import static electrosphere.test.testutils.Assertions.assertEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests the client jump tree
 */
public class ClientJumpTreeTests extends EntityTestTemplate {

    @IntegrationTest
    public void isJumping_AtRest_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        assertEquals(false, clientJumpTree.isJumping());
    }

    @IntegrationTest
    public void isJumping_WhileJumping_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        assertEventually(() -> clientJumpTree.isJumping());
    }

    @IntegrationTest
    public void isJumping_AfterLanding_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        assertEventually(() -> clientJumpTree.isJumping());

        assertEventually(() -> !clientJumpTree.isJumping());
    }

    @IntegrationTest
    public void verticalMovement_WhileJumping_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        assertEventually(() -> EntityUtils.getPosition(clientEntity).y > 0.3);
    }

    @IntegrationTest
    public void jumpEnablesBody_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        assertEventually(() -> PhysicsEntityUtils.getDBody(clientEntity).isEnabled());
    }
    
}
