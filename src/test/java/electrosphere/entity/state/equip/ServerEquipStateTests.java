package electrosphere.entity.state.equip;

import static electrosphere.test.testutils.Assertions.assertEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.joml.Vector3d;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Server equip state tests
 */
public class ServerEquipStateTests extends EntityTestTemplate {
    
    /**
     * Try equipping an item
     */
    @IntegrationTest
    public void testServerEquipItem(){
        TestEngineUtils.simulateFrames(1);
        //spawn entities
        ObjectTemplate creatureTemplate = ObjectTemplate.createDefault(EntityType.CREATURE, "human");
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", creatureTemplate);
        Entity katana = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //equip
        Entity inInventoryItem = ServerInventoryState.attemptStoreItemAnyInventory(creature, katana);
        ServerEquipState serverEquipState = ServerEquipState.getServerEquipState(creature);
        serverEquipState.commandAttemptEquip(inInventoryItem, serverEquipState.getEquipPoint("handsCombined"));

        //propagate to client
        assertEventually(() -> {
            List<Entity> children = AttachUtils.getChildrenList(creature);
            return children.size() == 1;
        });
        
        //
        //verify was equipped
        assertNotNull(serverEquipState.getEquippedItemAtPoint("handsCombined"));
        List<Entity> children = AttachUtils.getChildrenList(creature);
        assertNotNull(children);
        assertEquals(1, children.size());
        Entity child = children.get(0);
        assertTrue(ItemUtils.isItem(child));
        assertTrue(ItemUtils.isWeapon(child));
        assertNotNull(AttachUtils.getParent(child));
        assertEquals(AttachUtils.getParent(child), creature);
    }

    /**
     * Try equipping two items to the same slot
     */
    @IntegrationTest
    public void testServerFailEquipToOccupied(){
        TestEngineUtils.simulateFrames(1);
        //spawn entities
        ObjectTemplate creatureTemplate = ObjectTemplate.createDefault(EntityType.CREATURE, "human");
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", creatureTemplate);
        Entity katana = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");
        Entity katana2 = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //equip
        Entity inInventoryItem = ServerInventoryState.attemptStoreItemAnyInventory(creature, katana);
        ServerEquipState serverEquipState = ServerEquipState.getServerEquipState(creature);
        serverEquipState.commandAttemptEquip(inInventoryItem, serverEquipState.getEquipPoint("handsCombined"));

        //render a frame so network propagates to client
        assertEventually(() -> {
            List<Entity> children = AttachUtils.getChildrenList(creature);
            return children.size() == 1;
        });

        //attempt to equip second katana
        Entity inInventoryItem2 = ServerInventoryState.attemptStoreItemAnyInventory(creature, katana2);
        serverEquipState.commandAttemptEquip(inInventoryItem2, serverEquipState.getEquipPoint("handsCombined"));

        //propagate to client
        TestEngineUtils.simulateFrames(2);
        
        //
        //verify that only one item was equipped
        assertNotNull(serverEquipState.getEquippedItemAtPoint("handsCombined"));
        List<Entity> children = AttachUtils.getChildrenList(creature);
        assertNotNull(children);
        assertEquals(1, children.size());
        Entity child = children.get(0);
        assertTrue(ItemUtils.isItem(child));
        assertTrue(ItemUtils.isWeapon(child));
        assertNotNull(AttachUtils.getParent(child));
        assertEquals(AttachUtils.getParent(child), creature);
    }
    
}
