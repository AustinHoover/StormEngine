package electrosphere.server.pathfinding.recast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.Result;
import org.recast4j.detour.Status;
import org.recast4j.detour.StraightPathItem;

import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Performs pathfinding
 */
public class RecastPathfinder {

    /**
     * Maximum points in a straight path
     */
    static final int MAX_STRAIGHT_PATH_POINTS = 100;

    /**
     * The root navmesh
     */
    NavMesh navMesh;

    /**
     * The map of ref -> nav tile
     */
    Map<MeshData,Long> meshRefMap = new HashMap<MeshData,Long>();

    /**
     * The lock for thread safety
     */
    ReentrantLock lock = new ReentrantLock();

    /**
     * Creates the pathfinder
     */
    public RecastPathfinder(){
        NavMeshParams params = new NavMeshParams();
        params.tileHeight = ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
        params.tileWidth = ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET;
        params.orig[0] = 0;
        params.orig[1] = 0;
        params.orig[2] = 0;
        params.maxTiles = 1000;
        params.maxPolys = 100000;
        this.navMesh = new NavMesh(params, NavMeshConstructor.RECAST_VERTS_PER_POLY);
    }

    /**
     * Adds a tile to the pathfinder
     * @param tile The file
     */
    public void addTile(MeshData tile){
        lock.lock();
        long ref = this.navMesh.addTile(tile, 0, 0);
        meshRefMap.put(tile,ref);
        lock.unlock();
    }

    /**
     * Removes a tile from the navmesh
     * @param tile The tile
     */
    public void removeTile(MeshData tile){
        lock.lock();
        this.navMesh.removeTile(MAX_STRAIGHT_PATH_POINTS);
        lock.unlock();
    }
    
    /**
     * Solves for a path 
     * @param mesh The navmesh
     * @param startPos The start point
     * @param endPos The end point
     * @return The set of points to path along
     */
    public List<Vector3d> solve(MeshData mesh, Vector3d startPos, Vector3d endPos){
        lock.lock();
        List<Vector3d> rVal = new LinkedList<Vector3d>();

        if(mesh == null){
            throw new Error("Mesh data is null!");
        }

        //construct objects
        NavMeshQuery query = new NavMeshQuery(this.navMesh);
        QueryFilter filter = new DefaultQueryFilter();

        //convert points to correct datatypes
        float[] startArr = new float[]{(float)startPos.x, (float)startPos.y, (float)startPos.z};
        float[] endArr = new float[]{(float)endPos.x, (float)endPos.y, (float)endPos.z};
        float[] polySearchBounds = new float[]{ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET,ServerTerrainChunk.CHUNK_PLACEMENT_OFFSET};

        //find start poly
        Result<FindNearestPolyResult> startPolyResult = query.findNearestPoly(startArr, polySearchBounds, filter);
        if(!startPolyResult.succeeded()){
            String message = "Failed to solve for start polygon!\n" +
            startPolyResult.message
            ;
            throw new Error(message);
        }
        long startRef = startPolyResult.result.getNearestRef();

        //find end poly
        Result<FindNearestPolyResult> endPolyResult = query.findNearestPoly(endArr, polySearchBounds, filter);
        if(!endPolyResult.succeeded()){
            String message = "Failed to solve for end polygon!\n" +
            endPolyResult.message
            ;
            throw new Error(message);
        }
        long endRef = endPolyResult.result.getNearestRef();

        if(startRef == 0){
            throw new Error("Start ref is 0!");
        }
        if(endRef == 0){
            throw new Error("End ref is 0!");
        }

        //solve path
        Result<List<Long>> pathResult = query.findPath(startRef, endRef, startArr, endArr, filter);
        if(pathResult.failed()){
            String message = "Failed to solve for path!\n" +
            pathResult.message + "\n" +
            pathResult.status + "\n" +
            ""
            ;
            if(pathResult.status == Status.FAILURE_INVALID_PARAM){
                message = "Failed to solve for path -- invalid param!\n" +
                "Message: " + pathResult.message + "\n" +
                "Status: " + pathResult.status + "\n" +
                RecastPathfinder.checkInvalidParam(this.navMesh,startRef,endRef,startArr,endArr) + "\n" +
                ""
                ;
            }
            throw new Error(message);
        }

        //straighten the path ("string pull")
        Result<List<StraightPathItem>> straightPathResult = query.findStraightPath(startArr, endArr, pathResult.result, MAX_STRAIGHT_PATH_POINTS, 0);
        if(straightPathResult.failed()){
            String message = "Failed to straighten path!\n" +
            straightPathResult.message
            ;
            throw new Error(message);
        }

        //convert to usable structures
        for(StraightPathItem pathItem : straightPathResult.result){
            rVal.add(new Vector3d(pathItem.getPos()[0],pathItem.getPos()[1],pathItem.getPos()[2]));
        }

        lock.unlock();

        return rVal;
    }

    /**
     * Checks params to a path query
     * @param mesh The mesh
     * @param startRef The start ref
     * @param endRef The end ref
     * @param startPos The start pos
     * @param endPos THe end pos
     * @return The string containing the data
     */
    private static String checkInvalidParam(NavMesh mesh, long startRef, long endRef, float[] startPos, float[] endPos){
        //none of these should be true
        return "" +
        "startRef: " + startRef + "\n" +
        "endRef:   " + endRef + "\n" +
        "StartRef poly area succeeded: " + mesh.getPolyArea(startRef).succeeded() + "\n" +
        "EndRef poly area succeeded:   " + mesh.getPolyArea(endRef).succeeded() + "\n" +
        "StartPos is null: " + Objects.isNull(startPos) + "\n" +
        "StartPos is finite: " + !new Vector3f(startPos[0],startPos[1],startPos[2]).isFinite() + "\n" +
        "EndPos is null:   " + Objects.isNull(endPos) + "\n" +
        "EndPos is finite:   " + !new Vector3f(endPos[0],endPos[1],endPos[2]).isFinite() + "\n" +
        ""
        ;
    }

}
