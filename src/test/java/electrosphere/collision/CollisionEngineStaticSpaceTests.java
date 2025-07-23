package electrosphere.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3d;

import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.server.datacell.Realm;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.EntityTestTemplate;

/**
 * Tests for specifically the static space geoms
 */
public class CollisionEngineStaticSpaceTests extends EntityTestTemplate {
    
    @IntegrationTest
    public void staticSpaceTest1(){
        Realm realm = Globals.serverState.realmManager.first();
        CollisionEngine collisionEngine = realm.getCollisionEngine();

        //base plane + static space
        assertEquals(0, collisionEngine.getSpace().getNumGeoms());
    }

    @IntegrationTest
    public void test_nearCollisionCount_1(){
        Realm realm = Globals.serverState.realmManager.first();
        CollisionEngine collisionEngine = realm.getCollisionEngine();

        //simulate the physics
        collisionEngine.simulatePhysics();

        //base plane + static space  TIMES  number of calls to simulate physics
        int expected = 0 * CollisionEngine.PHYSICS_SIMULATION_RESOLUTION;
        assertEquals(expected, collisionEngine.getFinalCollisionCount());
    }

    @IntegrationTest
    public void test_nearCollisionCount_2(){
        Realm realm = Globals.serverState.realmManager.first();
        CollisionEngine collisionEngine = realm.getCollisionEngine();

        collisionEngine.createCubeGeom(new Vector3d(1,1,1), 0);

        //simulate the physics
        collisionEngine.simulatePhysics();

        assertEquals(0, collisionEngine.getFinalCollisionCount());
    }

    @IntegrationTest
    public void test_nearCollisionCount_3(){
        Realm realm = Globals.serverState.realmManager.first();
        CollisionEngine collisionEngine = realm.getCollisionEngine();

        Entity ent1 = EntityCreationUtils.createServerEntity(realm, new Vector3d(0));
        PhysicsEntityUtils.serverAttachGeom(realm, ent1, CollidableTemplate.getBoxTemplate(new Vector3d(1,1,1)), new Vector3d(0));

        PhysicsEntityUtils.serverAttachCollidableTemplate(realm, ent1, CollidableTemplate.getBoxTemplate(new Vector3d(1,1,1)), new Vector3d(0));

        int expectedBoxBoxCollisions = 3;

        int expectedTotalCollisions = expectedBoxBoxCollisions * CollisionEngine.PHYSICS_SIMULATION_RESOLUTION;
        

        //simulate the physics
        collisionEngine.simulatePhysics();

        assertEquals(expectedTotalCollisions, collisionEngine.getFinalCollisionCount());
    }

}
