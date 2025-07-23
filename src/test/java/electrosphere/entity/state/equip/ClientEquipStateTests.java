package electrosphere.entity.state.equip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.joml.Vector3d;
import org.junit.jupiter.api.Disabled;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.inventory.ClientInventoryState;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.ServerInventoryState;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;
import electrosphere.test.testutils.TestViewportUtils;

/**
 * Tests for client side equip state
 */
public class ClientEquipStateTests extends EntityTestTemplate {
    

    /**
     * Make sure server notifies client if ANY item is equipped
     */
    @IntegrationTest
    @Disabled
    public void testClientEquipItem(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn entities
        ObjectTemplate creatureTemplate = ObjectTemplate.createDefault(EntityType.CREATURE, "human");
        Entity creature = CreatureUtils.serverSpawnBasicCreature(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "human", creatureTemplate);
        Entity katana = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //wait for entities to propagate to client
        TestEngineUtils.simulateFrames(5);

        //verify the client got the extra entities
        Set<Entity> clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        assertEquals(1, clientSideCreatures.size());
        Set<Entity> clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEquals(1, clientSideItems.size());

        //equip
        Entity inInventoryItem = ServerInventoryState.attemptStoreItemAnyInventory(creature, katana);
        ServerEquipState serverEquipState = ServerEquipState.getServerEquipState(creature);
        serverEquipState.commandAttemptEquip(inInventoryItem, serverEquipState.getEquipPoint("handsCombined"));

        //propagate to client
        TestEngineUtils.simulateFrames(2);

        //verify we still have everything
        clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        assertEquals(1, clientSideCreatures.size());
        clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEquals(1, clientSideItems.size());

        //grab the item in particular
        Entity child = clientSideItems.iterator().next();
        
        //
        //verify was equipped
        assertTrue(ItemUtils.isItem(child));
        assertTrue(ItemUtils.isWeapon(child));
        assertNotNull(AttachUtils.getParent(child));
        Entity parentOfChild = AttachUtils.getParent(child);
        assertTrue(CreatureUtils.isCreature(parentOfChild));
        assertNotNull(AttachUtils.getChildrenList(parentOfChild));
        assertEquals(1, AttachUtils.getChildrenList(parentOfChild).size());
    }

    /**
     * Try requesting that an item is equipped from the client
     */
    @IntegrationTest
    @Disabled
    public void testClientPlayerRequestEquip(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn entities
        TestViewportUtils.spawnPlayerCharacter("human");
        //TODO: associate creature with player object created for viewport
        ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //wait for entities to propagate to client
        TestEngineUtils.simulateFrames(5);

        //verify the client got the extra entities
        Set<Entity> clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        assertEquals(1, clientSideCreatures.size());
        Set<Entity> clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEquals(1, clientSideItems.size());

        //try to store item in inventory
        Entity katanaOnClient = clientSideItems.iterator().next();
        ClientInventoryState.clientAttemptStoreItem(Globals.clientState.playerEntity, katanaOnClient);

        //wait for server to perform transform
        TestEngineUtils.simulateFrames(5);

        //try equipping
        UnrelationalInventoryState inventory = InventoryUtils.getNaturalInventory(Globals.clientState.playerEntity);
        assertEquals(1, inventory.getItems().size());
        Entity inInventoryItem = inventory.getItems().get(0);
        ClientEquipState clientEquipState = ClientEquipState.getClientEquipState(Globals.clientState.playerEntity);
        clientEquipState.commandAttemptEquip(inInventoryItem, clientEquipState.getEquipPoint("handsCombined"));

        //propagate to client
        TestEngineUtils.simulateFrames(5);

        //verify we still have everything
        clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        assertEquals(1, clientSideCreatures.size());
        clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEquals(2, clientSideItems.size());

        //verify the equip state thinks it has something equipped
        assertEquals(1,clientEquipState.getEquippedPoints().size());

        //grab the item in particular
        Entity child = clientEquipState.getEquippedItemAtPoint("handsCombined");
        
        //
        //verify was equipped
        assertTrue(ItemUtils.isItem(child));
        assertTrue(ItemUtils.isWeapon(child));
        assertNotNull(AttachUtils.getParent(child));
        Entity parentOfChild = AttachUtils.getParent(child);
        assertNotNull(AttachUtils.getChildrenList(parentOfChild));
        assertEquals(1, AttachUtils.getChildrenList(parentOfChild).size());
    }


}
