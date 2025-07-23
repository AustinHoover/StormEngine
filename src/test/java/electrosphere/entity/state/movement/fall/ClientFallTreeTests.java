package electrosphere.entity.state.movement.fall;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.movement.jump.ClientJumpTree;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;
import static electrosphere.test.testutils.Assertions.*;

/**
 * Tests the fall tree
 */
public class ClientFallTreeTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void isFalling_AtRest_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientFallTree clientFallTree = ClientFallTree.getFallTree(clientEntity);
        assertEquals(false, clientFallTree.isFalling());
    }

    @IntegrationTest
    public void isFalling_AfterJump_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientFallTree clientFallTree = ClientFallTree.getFallTree(clientEntity);
        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        //make sure we're in in the air
        assertEventually(() -> clientFallTree.isFalling(), 100);
    }
    
    @IntegrationTest
    public void isFalling_AfterLand_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        ClientFallTree clientFallTree = ClientFallTree.getFallTree(clientEntity);
        ClientJumpTree clientJumpTree = ClientJumpTree.getClientJumpTree(clientEntity);
        clientJumpTree.start();

        //make sure we're in in the air
        TestEngineUtils.simulateFrames(3);

        assertEventually(() -> {
            return !clientFallTree.isFalling();
        });
    }

}
