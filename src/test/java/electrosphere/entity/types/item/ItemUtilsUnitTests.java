package electrosphere.entity.types.item;

import org.junit.jupiter.api.Assertions;

import electrosphere.test.annotations.FastTest;
import electrosphere.test.annotations.UnitTest;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;

/**
 * Unit tests for item utils
 */
public class ItemUtilsUnitTests {
    
    @UnitTest
    @FastTest
    public void isItem_NullEntity_False(){
        boolean result = ItemUtils.isItem(null);
        Assertions.assertEquals(false, result);
    }

    @UnitTest
    @FastTest
    public void hasEquipList_NullEntity_False(){
        boolean result = ItemUtils.hasEquipList(null);
        Assertions.assertEquals(false, result);
    }

    @UnitTest
    @FastTest
    public void getEquipClass_NullEntity_False(){
        String result = ItemUtils.getEquipClass(null);
        Assertions.assertEquals(null, result);
    }

    @UnitTest
    @FastTest
    public void itemIsInInventory_FalseValue_False(){
        Entity entity = EntityCreationUtils.TEST_createEntity();
        entity.putData(EntityDataStrings.ITEM_IS_IN_INVENTORY, false);
        boolean result = ItemUtils.itemIsInInventory(entity);
        Assertions.assertEquals(false, result);
    }

}
