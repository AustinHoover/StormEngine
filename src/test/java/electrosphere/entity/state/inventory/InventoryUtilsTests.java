package electrosphere.entity.state.inventory;

import static electrosphere.test.testutils.Assertions.assertEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Tests for the inventory utils functions
 */
public class InventoryUtilsTests extends EntityTestTemplate {

    /**
     * Make sure clientAttemptStoreItem performs its intended function
     */
    @IntegrationTest
    public void test_clientAttemptStoreItem(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn entities
        TestEngineUtils.spawnPlayerEntity();
        Entity katana = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //wait for entities to propagate to client
        assertEventually(() -> {
            Set<Entity> localCreatureSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
            return localCreatureSet.size() == 1;
        });
        assertEventually(() -> {
            Set<Entity> localItemSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
            return localItemSet.size() == 1;
        });

        //verify the client got the extra entities
        Set<Entity> clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);

        //grab player entity
        Entity clientCreature = clientSideCreatures.iterator().next();
        Entity clientKatana = TestEngineUtils.getClientEquivalent(katana);
        Globals.clientState.playerEntity = clientCreature;

        //attempt to store
        ClientInventoryState.clientAttemptStoreItem(clientCreature, clientKatana);

        //wait for the store to propagate
        assertEventually(() -> {
            Set<Entity> localCreatureSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
            return localCreatureSet.size() == 1;
        });
        assertEventually(() -> {
            Set<Entity> localItemSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
            return localItemSet.size() == 1;
        });

        //verify we still have everything
        clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);

        //
        //verify was created properly
        assertEventually(() -> {
            Set<Entity> localItemSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
            Entity localChild = localItemSet.iterator().next();
            return ItemUtils.isItem(localChild) && ItemUtils.isWeapon(localChild) && ItemUtils.getContainingParent(localChild) != null;
        });

        //
        //verify the item is stored in the inventory properly
        UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(clientCreature);
        assertEquals(naturalInventory.getItems().size(),1);
    }

    /**
     * Make sure clientAttemptEjectItem performs its intended function
     */
    @IntegrationTest
    public void test_clientAttemptEjectItem(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn entities
        TestEngineUtils.spawnPlayerEntity();
        Entity katana = ItemUtils.serverSpawnBasicItem(Globals.serverState.realmManager.first(), new Vector3d(0,0,0), "Katana2H");

        //wait for entities to propagate to client
        assertEventually(() -> {
            Set<Entity> localCreatureSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
            return localCreatureSet.size() == 1;
        });
        assertEventually(() -> {
            Set<Entity> localItemSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
            return localItemSet.size() == 1;
        });

        //grab player entity
        Set<Entity> clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        Entity clientCreature = clientSideCreatures.iterator().next();
        Entity clientKatana = TestEngineUtils.getClientEquivalent(katana);
        Globals.clientState.playerEntity = clientCreature;

        //attempt to store
        ClientInventoryState.clientAttemptStoreItem(clientCreature, clientKatana);

        //wait for item to store
        assertEventually(() -> {
            Set<Entity> localItemSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
            Entity localChild = localItemSet.iterator().next();
            return ItemUtils.isItem(localChild) && ItemUtils.isWeapon(localChild) && ItemUtils.getContainingParent(localChild) != null;
        });

        //attempt to eject
        Set<Entity> clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        clientKatana = clientSideItems.iterator().next();
        ClientInventoryState.clientAttemptEjectItem(clientCreature, clientKatana);

        //wait for item to eject
        assertEventually(() -> {
            Set<Entity> localItemSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
            Entity localChild = localItemSet.iterator().next();
            return ItemUtils.isItem(localChild) && ItemUtils.isWeapon(localChild) && ItemUtils.getContainingParent(localChild) == null;
        });

        //verify we still have everything
        clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        assertEquals(1, clientSideCreatures.size());
        clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEquals(1, clientSideItems.size());

        //grab the item in particular
        Entity child = clientSideItems.iterator().next();
        
        //
        //verify was created properly
        assertTrue(ItemUtils.isItem(child));
        assertTrue(ItemUtils.isWeapon(child));
        assertNull(ItemUtils.getContainingParent(child));

        //
        //verify the item is stored in the inventory properly
        UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(clientCreature);
        assertEquals(naturalInventory.getItems().size(),0);
    }

    /**
     * Make sure clientConstructInInventoryItem performs its intended function
     */
    @IntegrationTest
    public void test_clientConstructInInventoryItem(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn entities
        TestEngineUtils.spawnPlayerEntity();

        //wait for entities to propagate to client
        assertEventually(() -> {
            Set<Entity> localCreatureSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
            return localCreatureSet.size() == 1;
        });

        //grab player entity
        Set<Entity> clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        Entity clientCreature = clientSideCreatures.iterator().next();
        Globals.clientState.playerEntity = clientCreature;

        //try creating the item on the client
        ClientInventoryState.clientConstructInInventoryItem(clientCreature, "Katana2H");

        //verify we still have everything
        clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        Set<Entity> clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEquals(1, clientSideCreatures.size());
        clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEquals(1, clientSideItems.size());

        //grab the item in particular
        Entity child = clientSideItems.iterator().next();
        
        //
        //verify was created properly
        assertTrue(ItemUtils.isItem(child));
        assertTrue(ItemUtils.isWeapon(child));
        assertNotNull(ItemUtils.getContainingParent(child));

        //
        //verify the item is stored in the inventory properly
        UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(clientCreature);
        assertEquals(naturalInventory.getItems().size(),1);
    }
    
}
