package electrosphere.server.pathfinding.recast;

import org.joml.Vector3d;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.recast.AreaModification;
import org.recast4j.recast.CompactHeightfield;
import org.recast4j.recast.Heightfield;
import org.recast4j.recast.PolyMesh;
import org.recast4j.recast.PolyMeshDetail;
import org.recast4j.recast.RecastBuilder;
import org.recast4j.recast.RecastBuilder.RecastBuilderResult;
import org.recast4j.recast.RecastBuilderConfig;
import org.recast4j.recast.RecastConfig;
import org.recast4j.recast.RecastConstants;
import org.recast4j.recast.RecastConstants.PartitionType;
import org.recast4j.recast.Span;
import org.recast4j.recast.geom.SingleTrimeshInputGeomProvider;

import electrosphere.entity.state.collidable.TriGeomData;
import electrosphere.util.math.GeomUtils;

/**
 * Constructor methods for nav meshes
 */
public class NavMeshConstructor {

    /**
     * Minimum size of geometry aabb
     */
    public static final float AABB_MIN_SIZE = 0.01f;

    /**
     * Size of a recast cell
     */
    static final float RECAST_CELL_SIZE = 0.3f;

    /**
     * Height of a recast cell
     */
    static final float RECAST_CELL_HEIGHT = 0.2f;

    /**
     * Height of a recast agent
     */
    static final float RECAST_AGENT_HEIGHT = 1.0f;

    /**
     * Size of a recast agent
     */
    static final float RECAST_AGENT_SIZE = 0.5f;

    /**
     * Maximum height a recast agent can climb
     */
    static final float RECAST_AGENT_MAX_CLIMB = 0.9f;

    /**
     * Maximum slope a recast agent can handle
     */
    static final float RECAST_AGENT_MAX_SLOPE = 60.0f;

    /**
     * Minimum size of a recast region
     */
    static final int RECAST_MIN_REGION_SIZE = 0;

    /**
     * Merge size of a recast region
     */
    static final int RECAST_REGION_MERGE_SIZE = 0;

    static final float RECAST_REGION_EDGE_MAX_LEN = 20.0f;

    static final float RECAST_REGION_EDGE_MAX_ERROR = 3.3f;

    static final int RECAST_VERTS_PER_POLY = 6;

    static final float RECAST_DETAIL_SAMPLE_DIST = 1.0f;

    static final float RECAST_DETAIL_SAMPLE_MAX_ERROR = 1.0f;
    
    /**
     * Constructs a navmesh
     * @param terrainChunk The terrain chunk
     * @return the MeshData
     */
    public static MeshData constructNavmesh(TriGeomData geomData){
        MeshData rVal = null;

        try {

            //build polymesh
            RecastBuilderResult recastBuilderResult = NavMeshConstructor.buildPolymesh(geomData);
            PolyMesh polyMesh = recastBuilderResult.getMesh();
            
            Vector3d polyMin = new Vector3d(polyMesh.bmin[0],polyMesh.bmin[1],polyMesh.bmin[2]);
            Vector3d polyMax = new Vector3d(polyMesh.bmax[0],polyMesh.bmax[1],polyMesh.bmax[2]);
            if(polyMin.x < 0 || polyMin.y < 0 || polyMin.z < 0){
                String message = "Min bound is less than 0\n" +
                NavMeshConstructor.polyMeshToString(polyMesh);
                throw new Error(message);
            }

            if(polyMin.distance(polyMax) < AABB_MIN_SIZE){
                String message = "Bounding box is too small for polymesh\n" +
                NavMeshConstructor.polyMeshToString(polyMesh);
                throw new Error(message);
            }

            //error check the built result
            if(polyMesh.nverts < 1){
                String message = "Failed to generate verts in poly mesh\n" +
                NavMeshConstructor.polyMeshToString(polyMesh);
                throw new Error(message);
            }
            if(polyMesh.npolys < 1){
                String message = "Failed to generate polys in poly mesh\n" +
                NavMeshConstructor.polyMeshToString(polyMesh);
                throw new Error(message);
            }

            //set flags
            for(int i = 0; i < polyMesh.npolys; i++){
                polyMesh.flags[i] = 1;
            }

            //set params
            NavMeshDataCreateParams params = new NavMeshDataCreateParams();
            params.verts = polyMesh.verts;
            params.vertCount = polyMesh.nverts;
            params.polys = polyMesh.polys;
            params.polyAreas = polyMesh.areas;
            params.polyFlags = polyMesh.flags;
            params.polyCount = polyMesh.npolys;
            params.nvp = polyMesh.nvp;
            params.walkableHeight = RECAST_AGENT_HEIGHT;
            params.walkableRadius = RECAST_AGENT_SIZE;
            params.walkableClimb = RECAST_AGENT_MAX_CLIMB;
            params.bmin = polyMesh.bmin;
            params.bmax = polyMesh.bmax;
            params.cs = RECAST_CELL_SIZE;
            params.ch = RECAST_CELL_HEIGHT;
            params.buildBvTree = true;

            PolyMeshDetail polyMeshDetail = recastBuilderResult.getMeshDetail();
            if(polyMeshDetail != null){
                params.detailMeshes = polyMeshDetail.meshes;
                params.detailVerts = polyMeshDetail.verts;
                params.detailVertsCount = polyMeshDetail.nverts;
                params.detailTris = polyMeshDetail.tris;
                params.detailTriCount = polyMeshDetail.ntris;
            }

            // params.offMeshConVerts = new float[6];
            // params.offMeshConVerts[0] = 0.1f;
            // params.offMeshConVerts[1] = 0.2f;
            // params.offMeshConVerts[2] = 0.3f;
            // params.offMeshConVerts[3] = 0.4f;
            // params.offMeshConVerts[4] = 0.5f;
            // params.offMeshConVerts[5] = 0.6f;
            // params.offMeshConRad = new float[1];
            // params.offMeshConRad[0] = 0.1f;
            // params.offMeshConDir = new int[1];
            // params.offMeshConDir[0] = 1;
            // params.offMeshConAreas = new int[1];
            // params.offMeshConAreas[0] = 2;
            // params.offMeshConFlags = new int[1];
            // params.offMeshConFlags[0] = 12;
            // params.offMeshConUserID = new int[1];
            // params.offMeshConUserID[0] = 0x4567;
            // params.offMeshConCount = 1;

            //actually build
            rVal = NavMeshBuilder.createNavMeshData(params);


        } catch (Exception e){
            e.printStackTrace();
        }

        return rVal;
    }

    /**
     * Builds a builder result from a geom data
     * @param geomData The geom data
     * @return The builder result
     */
    protected static RecastBuilderResult buildPolymesh(TriGeomData geomData){
        //create the geometry provider and error check
        SingleTrimeshInputGeomProvider geomProvider = NavMeshConstructor.getSingleTrimeshInputGeomProvider(geomData);

        //build configs
        RecastConfig recastConfig = new RecastConfig(
            PartitionType.WATERSHED,
            RECAST_CELL_SIZE,
            RECAST_CELL_HEIGHT,
            RECAST_AGENT_HEIGHT,
            RECAST_AGENT_SIZE,
            RECAST_AGENT_MAX_CLIMB,
            RECAST_AGENT_MAX_SLOPE,
            RECAST_MIN_REGION_SIZE,
            RECAST_REGION_MERGE_SIZE,
            RECAST_REGION_EDGE_MAX_LEN,
            RECAST_REGION_EDGE_MAX_ERROR,
            RECAST_VERTS_PER_POLY,
            RECAST_DETAIL_SAMPLE_DIST,
            RECAST_DETAIL_SAMPLE_MAX_ERROR,
            new AreaModification(1)
        );
        float[] boundMax = new float[]{
            geomProvider.getMeshBoundsMax()[0],
            geomProvider.getMeshBoundsMax()[1] + RECAST_AGENT_HEIGHT,
            geomProvider.getMeshBoundsMax()[2]
        };
        RecastBuilderConfig recastBuilderConfig = new RecastBuilderConfig(recastConfig, geomProvider.getMeshBoundsMin(), boundMax);
        RecastBuilder recastBuilder = new RecastBuilder();

        //actually build polymesh
        RecastBuilderResult recastBuilderResult = recastBuilder.build(geomProvider, recastBuilderConfig);

        return recastBuilderResult;
    }

    /**
     * Creates the geom provider from a given trimesh
     * @return The geom provider
     */
    protected static SingleTrimeshInputGeomProvider getSingleTrimeshInputGeomProvider(TriGeomData geomData){
        //error check input
        if(!GeomUtils.isWindingClockwise(geomData.getVertices(), geomData.getFaceElements())){
            throw new Error("Geometry is not wound clockwise!");
        }
        //create the geometry provider and error check
        SingleTrimeshInputGeomProvider geomProvider = new SingleTrimeshInputGeomProvider(geomData.getVertices(), geomData.getFaceElements());
        //check the bounding box
        Vector3d aabbStart = new Vector3d(geomProvider.getMeshBoundsMin()[0],geomProvider.getMeshBoundsMin()[1],geomProvider.getMeshBoundsMin()[2]);
        Vector3d aabbEnd = new Vector3d(geomProvider.getMeshBoundsMax()[0],geomProvider.getMeshBoundsMax()[1],geomProvider.getMeshBoundsMax()[2]);
        if(aabbStart.distance(aabbEnd) < AABB_MIN_SIZE){
            throw new Error("Geometry provider's AABB is too small " + aabbStart.distance(aabbEnd));
        }
        return geomProvider;
    }

    /**
     * Counts the spans of a building result
     * @param recastBuilderResult The result
     * @return The number of spans
     */
    protected static int countSpans(RecastBuilderResult recastBuilderResult){
        Heightfield heightfield = recastBuilderResult.getSolidHeightfield();
        int count = 0;
        int w = heightfield.width;
        int h = heightfield.height;
        for(int y = 0; y < h; ++y) {
            for(int x = 0; x < w; ++x) {
                for(Span s = heightfield.spans[x + y * w]; s != null; s = s.next) {
                    if(s.area != RecastConstants.RC_NULL_AREA){
                        count++;
                    }
                }
            }
        }
        return count;
    }


    /**
     * Counts the walkable spans in the compact heightfield
     * @param recastBuilderResult The build result
     * @return The number of walkable spans
     */
    protected static int countWalkableSpans(RecastBuilderResult recastBuilderResult){
        CompactHeightfield chf = recastBuilderResult.getCompactHeightfield();
        int count = 0;
        for (int i = 0; i < chf.spanCount; i++){
            if(chf.spans[i] != null){
                count++;
            }
        }
        return count;
    }

    /**
     * Converts a polymesh to a string
     * @param polyMesh The polymesh
     * @return The string
     */
    protected static String polyMeshToString(PolyMesh polyMesh){
        return "" +
        "nverts: " + polyMesh.nverts + "\n" +
        "verts.length: " + polyMesh.verts.length + "\n" +
        "npolys: " + polyMesh.npolys + "\n" +
        "polys.length: " + polyMesh.polys.length + "\n" +
        "bmin: " + polyMesh.bmin[0] + "," + polyMesh.bmin[1] + "," + polyMesh.bmin[2] + "\n" +
        "bmin: " + polyMesh.bmax[0] + "," + polyMesh.bmax[1] + "," + polyMesh.bmax[2] + "\n" +
        "";
    }

}
