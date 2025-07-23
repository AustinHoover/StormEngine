package electrosphere.server.ai.services;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joml.Vector3d;

import electrosphere.engine.threads.ThreadCounts;
import electrosphere.server.datacell.interfaces.VoxelCellManager;
import electrosphere.server.pathfinding.recast.PathingProgressiveData;
import electrosphere.server.pathfinding.voxel.VoxelPathfinder;

/**
 * Service for performing pathfinding
 */
public class PathfindingService implements AIService {

    /**
     * The executor service
     */
    static final ExecutorService executorService = Executors.newFixedThreadPool(ThreadCounts.PATHFINDING_THREADS);

    /**
     * Queues a pathfinding job
     * @param start The start point
     * @param end The end point
     * @param pathfinder The pathfinder object
     * @param voxelCellManager The voxel cell manager
     * @return The object that will eventually hold the pathfinding data
     */
    public PathingProgressiveData queuePathfinding(Vector3d start, Vector3d end, VoxelPathfinder pathfinder, VoxelCellManager voxelCellManager){
        PathingProgressiveData rVal = new PathingProgressiveData(end);
        executorService.submit(() -> {
            try {
                List<Vector3d> points = pathfinder.findPath(voxelCellManager, start, end, VoxelPathfinder.DEFAULT_MAX_COST);
                points.add(end);
                rVal.setPoints(points);
                rVal.setReady(true);
            } catch(Error e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
        return rVal;
    }

    @Override
    public void exec() {
        //No synchronous logic required yet
    }

    @Override
    public void shutdown() {
    }

    /**
     * Halts all threads in the pathfinding service
     */
    public static void haltThreads(){
        executorService.shutdownNow();
    }

}
