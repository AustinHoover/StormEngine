package electrosphere.client.entity.debug;

import java.util.function.Consumer;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.util.math.SpatialMathUtils;

/**
 * Debug tools for visualizing things in the game engine
 */
public class DebugVisualizerUtils {
    

    /**
     * Spawns an entity that visualizes a vector
     * @param position The position to start the vector
     * @param rotation The rotation of the vector
     * @param scale The scale of the vector
     * @return The entity that visualizes the vector
     */
    public static Entity clientSpawnVectorVisualizer(Vector3d position, Quaterniond rotation, Vector3d scale){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(rVal, "Models/basic/geometry/unitvector.glb");
        Vector3d pos = new Vector3d(position);
        EntityUtils.setPosition(rVal, pos);
        EntityUtils.getScale(rVal).set(scale);
        EntityUtils.getRotation(rVal).set(rotation);
        return rVal;
    }

    /**
     * Spawns a vector that visualizes the distance between two points
     * @param point1 The first point
     * @param point2 The second point
     * @return The entity that visualizes the vector
     */
    public static Entity clientSpawnVectorVisualizer(Vector3d point1, Vector3d point2){
        Vector3d position = point1;
        Quaterniond rotation = SpatialMathUtils.calculateRotationFromPointToPoint(point1, point2);
        Vector3d scale = new Vector3d(point1.distance(point2));
        return clientSpawnVectorVisualizer(position, rotation, scale);
    }

    /**
     * Spawns an entity that visualizes a vector. It's transform is constantly updated by a provided callback.
     * @param callback The callback which should update the transforms of the vector
     * @return The entity that visualizes the vector
     */
    public static Entity clientSpawnVectorVisualizer(Consumer<Entity> callback){
        Entity rVal = EntityCreationUtils.createClientSpatialEntity();
        EntityCreationUtils.makeEntityDrawable(rVal, "Models/basic/geometry/unitvector.glb");
        BehaviorTree updateTree = new BehaviorTree() {
            @Override
            public void simulate(float deltaTime) {
                callback.accept(rVal);
            }
        };
        Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(updateTree);
        return rVal;
    }

}
