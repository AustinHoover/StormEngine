package electrosphere.entity.types.item;

import static electrosphere.test.testutils.Assertions.assertEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.data.entity.item.Item;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityTags;
import electrosphere.entity.state.inventory.InventoryUtils;
import electrosphere.entity.state.inventory.UnrelationalInventoryState;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;
import electrosphere.test.testutils.TestEngineUtils;

/**
 * Integration tests for ItemUtils
 */
public class ItemUtilsTests extends EntityTestTemplate {
    
    /**
     * Make sure clientAttemptStoreItem performs its intended function
     */
    @IntegrationTest
    public void test_serverCreateContainerItem(){
        //warm up engine
        TestEngineUtils.simulateFrames(1);

        //spawn entities
        TestEngineUtils.spawnPlayerEntity();

        //wait for entities to propagate to client
        assertEventually(() -> {
            Set<Entity> localCreatureSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
            return localCreatureSet.size() == 1;
        });

        //verify the client got the extra entities
        Set<Entity> clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        Set<Entity> clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);

        //get server equivalent of client entity
        Entity serverEquivalent = TestEngineUtils.getServerEquivalent(clientSideCreatures.iterator().next());

        //grab player entity
        Entity clientCreature = clientSideCreatures.iterator().next();
        Globals.clientState.playerEntity = clientCreature;

        //attempt to store
        Item item = Globals.gameConfigCurrent.getItemMap().getItem("Katana2H");
        ItemUtils.serverCreateContainerItem(serverEquivalent, item);

        //propagate to client
        TestEngineUtils.simulateFrames(2);

        //verify we still have everything
        clientSideCreatures = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.CREATURE);
        clientSideItems = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
        assertEventually(() -> {
            Set<Entity> localItemSet = Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.ITEM);
            Entity child = localItemSet.iterator().next();
            return ItemUtils.getContainingParent(child) != null;
        });

        //grab the item in particular
        Entity child = clientSideItems.iterator().next();
        
        //
        //verify was created properly
        assertTrue(ItemUtils.isItem(child));
        assertTrue(ItemUtils.isWeapon(child));
        assertNotNull(ItemUtils.getContainingParent(child));
        assertNull(PhysicsEntityUtils.getCollidable(child));

        //
        //verify the item is stored in the inventory properly
        UnrelationalInventoryState naturalInventory = InventoryUtils.getNaturalInventory(clientCreature);
        assertEquals(naturalInventory.getItems().size(),1);
    }

}
