package electrosphere.entity.state.attack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.joml.Vector3d;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attack.ClientAttackTree.AttackTreeState;
import electrosphere.entity.state.equip.ServerToolbarState;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.state.movement.fall.ServerFallTree;
import electrosphere.entity.state.movement.groundmove.ServerGroundMovementTree;
import electrosphere.entity.state.movement.groundmove.ClientGroundMovementTree.MovementRelativeFacing;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Testing the client's attacking trees
 */
public class ServerAttackTreeTests extends EntityTestTemplate {
    
    
    /**
     * Make sure can attack in default scene
     */
    @IntegrationTest
    public void testClientAttack(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn on server
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity katana = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //equip
        Entity inInventoryItem = ServerInventoryState.attemptStoreItemAnyInventory(creature, katana);
        ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(creature);
        serverToolbarState.attemptEquip(inInventoryItem, 0);
        
        //Get the server-side player's trees
        ServerAttackTree serverAttackTree = ServerAttackTree.getServerAttackTree(creature);
        ServerFallTree serverFallTree = ServerFallTree.getFallTree(creature);
        serverFallTree.land();
        
        //verify can attack
        assertEquals(true, serverAttackTree.canAttack(serverAttackTree.getAttackType()));

        //try attacking
        serverAttackTree.start();

        //wait for the attack to propagate back to the client
        TestEngineUtils.simulateFrames(2);

        //verify it was started on server
        assertNotEquals(serverAttackTree.getState(), AttackTreeState.IDLE);
    }

    /**
     * Make sure can attack in default scene
     */
    @IntegrationTest
    public void testCanAttack_WhileMoving_True(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn on server
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", ObjectTemplate.createDefault(EntityType.CREATURE, "human"));
        Entity katana = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //equip
        Entity inInventoryItem = ServerInventoryState.attemptStoreItemAnyInventory(creature, katana);
        ServerToolbarState serverToolbarState = ServerToolbarState.getServerToolbarState(creature);
        serverToolbarState.attemptEquip(inInventoryItem, 0);
        
        //Get the server-side player's trees
        ServerAttackTree serverAttackTree = ServerAttackTree.getServerAttackTree(creature);
        ServerGroundMovementTree serverGroundMovementTree = ServerGroundMovementTree.getServerGroundMovementTree(creature);
        serverGroundMovementTree.start(MovementRelativeFacing.FORWARD);

        //Make sure move tree is actually active
        TestEngineUtils.simulateFrames(1);
        
        //verify can attack
        assertEquals(true, serverAttackTree.canAttack(serverAttackTree.getAttackType()));
    }

}
