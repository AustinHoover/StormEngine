package electrosphere.collision.collision;

import static electrosphere.test.testutils.Assertions.assertEventually;

import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests for physics entity utils
 */
public class PhysicsEntityUtilsTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void test_server_RigidBody_NotNull(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        //warm up engine
        TestEngineUtils.simulateFrames(3);

        assertEventually(() -> PhysicsEntityUtils.getDBody(serverEntity) != null);
    }

    @IntegrationTest
    public void test_client_RigidBody_NotNull(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);

        //warm up engine
        TestEngineUtils.simulateFrames(3);

        assertEventually(() -> PhysicsEntityUtils.getDBody(clientEntity) != null);
    }

}
