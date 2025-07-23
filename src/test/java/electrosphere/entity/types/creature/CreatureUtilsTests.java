package electrosphere.entity.types.creature;

import static electrosphere.test.testutils.Assertions.assertEventually;

import org.joml.Vector3d;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests creature utils
 */
public class CreatureUtilsTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void isActive_OnSpawn_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity clientEntity = TestEngineUtils.getClientEquivalent(serverEntity);
        assertEventually(() -> PhysicsEntityUtils.containsDBody(clientEntity));
    }

}
