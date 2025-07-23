package electrosphere.entity.state.gravity;

import static electrosphere.test.testutils.Assertions.assertEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests client gravity tree
 */
public class ClientGravityTreeTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void isActive_AtRest_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientGravityTree clientGravityTree = ClientGravityTree.getClientGravityTree(clientEntity);
        assertEquals(false, clientGravityTree.isActive());
    }

    @IntegrationTest
    public void physicsIsValid_eventually_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientGravityTree clientGravityTree = ClientGravityTree.getClientGravityTree(clientEntity);
        assertEventually(() -> clientGravityTree.physicsIsValid());
    }

    @IntegrationTest
    public void activates_on_jump(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        ClientGravityTree clientGravityTree = ClientGravityTree.getClientGravityTree(clientEntity);

        assertEventually(() -> clientGravityTree.isActive());
    }

    @IntegrationTest
    public void deactivates_on_collide_world_bound(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        ClientGravityTree clientGravityTree = ClientGravityTree.getClientGravityTree(clientEntity);

        assertEventually(() -> clientGravityTree.isActive());

        assertEventually(() -> !clientGravityTree.isActive());
    }

}
