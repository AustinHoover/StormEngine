package electrosphere.entity.state.movement.jump;

import static electrosphere.test.testutils.Assertions.assertEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;

/**
 * Tests the server jump tree
 */
public class ServerJumpTreeTests extends EntityTestTemplate {

    /**
     * Expected height that a jump would get us to
     */
    static final double EXPECTED_HEIGHT = 0.5;

    @IntegrationTest
    public void isJumping_AtRest_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(serverEntity);
        assertEquals(false, serverJumpTree.isJumping());
    }

    @IntegrationTest
    public void isJumping_WhileJumping_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(serverEntity);
        serverJumpTree.start();

        assertEventually(() -> serverJumpTree.isJumping());
    }

    @IntegrationTest
    public void isJumping_AfterLanding_false(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(serverEntity);
        serverJumpTree.start();

        assertEventually(() -> serverJumpTree.isJumping());

        assertEventually(() -> !serverJumpTree.isJumping());
    }

    @IntegrationTest
    public void verticalMovement_WhileJumping_true(){
        Entity serverEntity = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));

        ServerJumpTree serverJumpTree = ServerJumpTree.getServerJumpTree(serverEntity);
        serverJumpTree.start();

        assertEventually(() -> EntityUtils.getPosition(serverEntity).y > EXPECTED_HEIGHT);
    }

}
