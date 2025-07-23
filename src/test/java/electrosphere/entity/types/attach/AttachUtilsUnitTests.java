package electrosphere.entity.types.attach;

import org.junit.jupiter.api.Assertions;

import electrosphere.test.annotations.FastTest;
import electrosphere.test.annotations.UnitTest;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.state.attach.AttachUtils;

/**
 * Unit tests for attach utils
 */
public class AttachUtilsUnitTests {
    
    @UnitTest
    @FastTest
    public void isAttached_NullEntity_False(){
        boolean result = AttachUtils.isAttached(null);
        Assertions.assertEquals(false, result);
    }

    @UnitTest
    @FastTest
    public void isAttached_FalseValue_False(){
        Entity testEnt = EntityCreationUtils.TEST_createEntity();
        testEnt.putData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED, false);
        boolean result = AttachUtils.isAttached(testEnt);
        Assertions.assertEquals(false, result);
    }

    @UnitTest
    @FastTest
    public void isAttached_NullValue_False(){
        Entity testEnt = EntityCreationUtils.TEST_createEntity();
        boolean result = AttachUtils.isAttached(testEnt);
        Assertions.assertEquals(false, result);
    }

    @UnitTest
    @FastTest
    public void isAttached_TrueValue_True(){
        Entity testEnt = EntityCreationUtils.TEST_createEntity();
        testEnt.putData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED, true);
        boolean result = AttachUtils.isAttached(testEnt);
        Assertions.assertEquals(true, result);
    }

}
