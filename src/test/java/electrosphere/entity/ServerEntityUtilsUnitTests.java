package electrosphere.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.test.annotations.UnitTest;
import electrosphere.engine.Globals;
import electrosphere.server.datacell.Realm;

/**
 * Unit tests for the server entity utils
 */
public class ServerEntityUtilsUnitTests {
    
    @UnitTest
    public void destroyEntity_ValidEntity_NoRealm(){
        //setup
        Globals.initGlobals();
        Realm realm = Globals.serverState.realmManager.createViewportRealm(new Vector3d(0,0,0), new Vector3d(1,1,1));
        Entity entity = EntityCreationUtils.createServerEntity(realm, new Vector3d());

        //perform action
        ServerEntityUtils.destroyEntity(entity);

        //verify
        assertEquals(null, Globals.serverState.realmManager.getEntityRealm(entity));
    }

}
