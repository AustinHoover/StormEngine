package electrosphere.server.datacell.interfaces;

import java.util.List;

import org.joml.Vector3d;

import electrosphere.server.pathfinding.recast.PathingProgressiveData;

/**
 * Performs pathfinding
 */
public interface PathfindingManager {
    
    /**
     * Solves a path
     * @param start The start point
     * @param end The end point
     * @return The path if it exists, null otherwise
     */
    public List<Vector3d> findPath(Vector3d start, Vector3d end);

    /**
     * Solves a path
     * @param start The start point
     * @param end The end point
     * @return The path if it exists, null otherwise
     */
    public PathingProgressiveData findPathAsync(Vector3d start, Vector3d end);

}
