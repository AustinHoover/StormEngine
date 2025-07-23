package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import electrosphere.client.terrain.cells.VoxelTextureAtlas;
import electrosphere.client.terrain.data.TerrainChunkData;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.mem.JomlPool;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.server.physics.terrain.manager.ServerTerrainChunk;

/**
 * Utility functions for generating transvoxel based meshes
 */
public class TransvoxelModelGeneration {

    /**
     * Number of ints required to store an element
     */
    public static final int INTS_PER_ELEMENT = 1;

    /**
     * Number of elements per triangle
     */
    public static final int ELEMENTS_PER_TRIANGLE = 3;


    /**
     * Number of floats per vert
     */
    public static final int FLOATS_PER_VERT = 3;

    /**
     * Number of vertices per triangle
     */
    public static final int VERTS_PER_TRIANGLE = ELEMENTS_PER_TRIANGLE;

    /**
     * Number of floats per UV
     */
    public static final int FLOATS_PER_UV = 2;

    /**
     * The number of sampler indices per triangle
     */
    public static final int SAMPLER_INDICES_PER_TRIANGLE = 3;

    /**
     * The number of sampler values to store for each vertex
     */
    public static final int SAMPLER_VALUES_PER_VERT = 3;

    /**
     * Size of the vector pool
     */
    public static final int VECTOR_POOL_SIZE = 13;

    /**
     * Threshold of normal dot product
     */
    public static final float NORMAL_DOT_THRESHOLD = 0.99f;

    /**
     * The min dist to check
     */
    public static final double MIN_DIST = 0.00001;


    //this is the width of the transition cell
    //it should be between 0 and 1, exclusive
    //the higher the value, the more of the high resolution chunk we will see
    //the lower the value, the more of the low resolution chunk we will see
    public static final float TRANSITION_CELL_WIDTH = 0.5f;

    /**
     * The dimension of the array for the face generator. It must be 2 * <size of chunk> + 1. The extra 1 is for the neighbor value
     */
    public static final int FACE_DATA_DIMENSIONS = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE + ServerTerrainChunk.CHUNK_DIMENSION;








    /**
     * A single generated triangle
     */
    private static class Triangle {
        //the indices
        int[] indices = new int[3]; //array of size 3

        /**
         * Creates a triangle object
         * @param index0
         * @param index1
         * @param index2
         */
        public Triangle(int index0, int index1, int index2){
            indices[0] = index0;
            indices[1] = index1;
            indices[2] = index2;
        }
    }

    /**
     * The grid cell currently being looked at
     */
    private static class GridCell {
        Vector3f[] points = new Vector3f[8]; //array of size 8
        double[] val = new double[8]; //array of size 8
        int[] atlasValues = new int[8]; //array of size 8
        public void setValues(
            double val1, double val2, double val3, double val4,
            double val5, double val6, double val7, double val8,
            int atl1, int atl2, int atl3, int atl4,
            int atl5, int atl6, int atl7, int atl8
        ){
            //iso values
            val[0] = val1; val[1] = val2; val[2] = val3; val[3] = val4;
            val[4] = val5; val[5] = val6; val[6] = val7; val[7] = val8;
            //atlas values
            atlasValues[0] = atl1; atlasValues[1] = atl2; atlasValues[2] = atl3; atlasValues[3] = atl4;
            atlasValues[4] = atl5; atlasValues[5] = atl6; atlasValues[6] = atl7; atlasValues[7] = atl8;
        }
        public void setVectors(
            Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4,
            Vector3f p5, Vector3f p6, Vector3f p7, Vector3f p8
        ){
            //triangle points
            points[0] = p1; points[1] = p2; points[2] = p3; points[3] = p4;
            points[4] = p5; points[5] = p6; points[6] = p7; points[7] = p8;
        }
    }

    /**
     * The transition grid cell currently being looked at
     */
    private static class TransitionGridCell {
        Vector3f[][] complexFacePoints = new Vector3f[3][3];
        Vector3f[][] simpleFacePoints = new Vector3f[2][2];
        float[][] complexFaceValues = new float[3][3];
        float[][] simpleFaceValues = new float[2][2];
        int[][] complexFaceAtlasValues = new int[3][3];
        int[][] simpleFaceAtlasValues = new int[2][2];
        public void setValues(
            Vector3f c1, Vector3f c2, Vector3f c3,
            Vector3f c4, Vector3f c5, Vector3f c6,
            Vector3f c7, Vector3f c8, Vector3f c9,

            Vector3f s1, Vector3f s2,
            Vector3f s3, Vector3f s4,

            float cVal1, float cVal2, float cVal3,
            float cVal4, float cVal5, float cVal6,
            float cVal7, float cVal8, float cVal9,

            float sVal1, float sVal2,
            float sVal3, float sVal4,

            int cA1, int cA2, int cA3, int cA4,
            int cA5, int cA6, int cA7, int cA8,
            int cA9,

            int sA1, int sA2, int sA3, int sA4
        ){
            //triangle points
            complexFacePoints[0][0] = c1; complexFacePoints[0][1] = c2; complexFacePoints[0][2] = c3;
            complexFacePoints[1][0] = c4; complexFacePoints[1][1] = c5; complexFacePoints[1][2] = c6;
            complexFacePoints[2][0] = c7; complexFacePoints[2][1] = c8; complexFacePoints[2][2] = c9;
            simpleFacePoints[0][0] = s1; simpleFacePoints[0][1] = s2;
            simpleFacePoints[1][0] = s3; simpleFacePoints[1][1] = s4;
            //iso values
            complexFaceValues[0][0] = cVal1; complexFaceValues[0][1] = cVal2; complexFaceValues[0][2] = cVal3;
            complexFaceValues[1][0] = cVal4; complexFaceValues[1][1] = cVal5; complexFaceValues[1][2] = cVal6;
            complexFaceValues[2][0] = cVal7; complexFaceValues[2][1] = cVal8; complexFaceValues[2][2] = cVal9;
            simpleFaceValues[0][0] = sVal1; simpleFaceValues[0][1] = sVal2;
            simpleFaceValues[1][0] = sVal3; simpleFaceValues[1][1] = sVal4;
            //atlas values
            complexFaceAtlasValues[0][0] = cA1; complexFaceAtlasValues[0][1] = cA2; complexFaceAtlasValues[0][2] = cA3;
            complexFaceAtlasValues[1][0] = cA4; complexFaceAtlasValues[1][1] = cA5; complexFaceAtlasValues[1][2] = cA6;
            complexFaceAtlasValues[2][0] = cA7; complexFaceAtlasValues[2][1] = cA8; complexFaceAtlasValues[2][2] = cA9;
            simpleFaceAtlasValues[0][0] = sA1; simpleFaceAtlasValues[0][1] = sA2;
            simpleFaceAtlasValues[1][0] = sA3; simpleFaceAtlasValues[1][1] = sA4;
        }
    }









    /**
     * location of the sampler index data in the shader
     */
    public static final int SAMPLER_INDEX_ATTRIB_LOC = 5;

    /**
     * the ratio vectors of how much to pull from each texture
     */
    public static final int SAMPLER_RATIO_ATTRIB_LOC = 6;










    /**
     * Creates polygons for a voxel that is in the interior of this mesh
     * @param grid
     * @param isolevel
     * @param triangles
     * @param samplerIndices
     * @param vertMap
     * @param verts
     * @param normals
     * @param trianglesSharingVert
     * @param invertNormals Flag to invert normals
     * @return
     */
    protected static int polygonize(
        GridCell grid,
        List<Triangle> triangles,
        List<Vector3f> samplerIndices,
        Map<String,Integer> vertMap,
        List<Vector3f> verts,
        List<Vector3f> normals,
        List<Integer> trianglesSharingVert,
        boolean invertNormals,
        Vector3f[] vertList,
        int[] samplerIndex,
        boolean[] skip,
        StringBuilder builder
    ){
        int i;
        int ntriang;
        int cubeIndex = 0;

        float isolevel = TerrainChunkModelGeneration.MIN_ISO_VALUE;

        //essentially, because the iso level is 0, we can end up generating weird midpoints between 0 and negative values
        //in order to not actually generate triangles for these edge cases, the skip table is populated if the current edge is between 0 and a negative value
        //when storing triangles, all skip edges trigger the loop to skip to the next triangle set
        for(int j = 0; j < 12; j++){
            skip[j] = false;
            vertList[j].set(0,0,0);
        }

        //get lookup key (index) for edge table
        //edge table tells us which vertices are inside of the surface
        if(grid.val[0] < isolevel) cubeIndex |= 1;
        if(grid.val[1] < isolevel) cubeIndex |= 2;
        if(grid.val[2] < isolevel) cubeIndex |= 4;
        if(grid.val[3] < isolevel) cubeIndex |= 8;
        if(grid.val[4] < isolevel) cubeIndex |= 16;
        if(grid.val[5] < isolevel) cubeIndex |= 32;
        if(grid.val[6] < isolevel) cubeIndex |= 64;
        if(grid.val[7] < isolevel) cubeIndex |= 128;

        //Cube is entirely in/out of the surface
        if(TerrainChunkModelGeneration.edgeTable[cubeIndex] == 0){
            return 0;
        }

        //instead of having all intersections be perfectly at the midpoint,
        //for each edge this code calculates where along the edge to place the vertex
        //this should dramatically smooth the surface
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 1) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[0],grid.points[1],grid.val[0],grid.val[1],vertList[0]);
            if(grid.val[0] <= 0 && grid.val[1] <= 0){ skip[0] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 2) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[1],grid.points[2],grid.val[1],grid.val[2],vertList[1]);
            if(grid.val[1] <= 0 && grid.val[2] <= 0){ skip[1] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 4) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[2],grid.points[3],grid.val[2],grid.val[3],vertList[2]);
            if(grid.val[2] <= 0 && grid.val[3] <= 0){ skip[2] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 8) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[3],grid.points[0],grid.val[3],grid.val[0],vertList[3]);
            if(grid.val[3] <= 0 && grid.val[0] <= 0){ skip[3] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 16) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[4],grid.points[5],grid.val[4],grid.val[5],vertList[4]);
            if(grid.val[4] <= 0 && grid.val[5] <= 0){ skip[4] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 32) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[5],grid.points[6],grid.val[5],grid.val[6],vertList[5]);
            if(grid.val[5] <= 0 && grid.val[6] <= 0){ skip[5] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 64) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[6],grid.points[7],grid.val[6],grid.val[7],vertList[6]);
            if(grid.val[6] <= 0 && grid.val[7] <= 0){ skip[6] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 128) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[7],grid.points[4],grid.val[7],grid.val[4],vertList[7]);
            if(grid.val[7] <= 0 && grid.val[4] <= 0){ skip[7] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 256) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[0],grid.points[4],grid.val[0],grid.val[4],vertList[8]);
            if(grid.val[0] <= 0 && grid.val[4] <= 0){ skip[8] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 512) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[1],grid.points[5],grid.val[1],grid.val[5],vertList[9]);
            if(grid.val[1] <= 0 && grid.val[5] <= 0){ skip[9] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 1024) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[2],grid.points[6],grid.val[2],grid.val[6],vertList[10]);
            if(grid.val[2] <= 0 && grid.val[6] <= 0){ skip[10] = true; }
        }
        if((TerrainChunkModelGeneration.edgeTable[cubeIndex] & 2048) > 0){
            TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[3],grid.points[7],grid.val[3],grid.val[7],vertList[11]);
            if(grid.val[3] <= 0 && grid.val[7] <= 0){ skip[11] = true; }
        }

        if(grid.val[0] > isolevel){ samplerIndex[0] = 0; } else { samplerIndex[0] = 1; }
        if(grid.val[1] > isolevel){ samplerIndex[1] = 1; } else { samplerIndex[1] = 2; }
        if(grid.val[2] > isolevel){ samplerIndex[2] = 2; } else { samplerIndex[2] = 3; }
        if(grid.val[3] > isolevel){ samplerIndex[3] = 3; } else { samplerIndex[3] = 0; }
        if(grid.val[4] > isolevel){ samplerIndex[4] = 4; } else { samplerIndex[4] = 5; }
        if(grid.val[5] > isolevel){ samplerIndex[5] = 5; } else { samplerIndex[5] = 6; }
        if(grid.val[6] > isolevel){ samplerIndex[6] = 6; } else { samplerIndex[6] = 7; }
        if(grid.val[7] > isolevel){ samplerIndex[7] = 7; } else { samplerIndex[7] = 4; }
        if(grid.val[0] > isolevel){ samplerIndex[8] = 0; } else { samplerIndex[8] = 4; }
        if(grid.val[1] > isolevel){ samplerIndex[9] = 1; } else { samplerIndex[9] = 5; }
        if(grid.val[2] > isolevel){ samplerIndex[10] = 2; } else { samplerIndex[10] = 6; }
        if(grid.val[3] > isolevel){ samplerIndex[11] = 3; } else { samplerIndex[11] = 7; }

        //Create the triangle
        ntriang = 0;
        for(i=0; TerrainChunkModelGeneration.triTable[cubeIndex][i]!=-1; i+=3){

            //check skip table
            if(
                skip[TerrainChunkModelGeneration.triTable[cubeIndex][i+0]] ||
                skip[TerrainChunkModelGeneration.triTable[cubeIndex][i+1]] ||
                skip[TerrainChunkModelGeneration.triTable[cubeIndex][i+2]]
            ){
                continue;
            }
            //
            // Triangles calculation
            //
            //get indices
            Vector3f vert0 = vertList[TerrainChunkModelGeneration.triTable[cubeIndex][i+0]];
            Vector3f vert1 = vertList[TerrainChunkModelGeneration.triTable[cubeIndex][i+1]];
            Vector3f vert2 = vertList[TerrainChunkModelGeneration.triTable[cubeIndex][i+2]];
            int index0 = TransvoxelModelGeneration.getVertIndex(vert0,vertMap,verts,builder);
            int index1 = TransvoxelModelGeneration.getVertIndex(vert1,vertMap,verts,builder);
            int index2 = TransvoxelModelGeneration.getVertIndex(vert2,vertMap,verts,builder);
            if(index0 == index1 || index1 == index2 || index0 == index2){
                TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[6],grid.points[7],grid.val[6],grid.val[7],new Vector3f());
                TransvoxelModelGeneration.VertexInterp(isolevel,grid.points[2],grid.points[6],grid.val[2],grid.val[6],new Vector3f());
                String message = "Identical indices!\n" +
                "edge table: " + TerrainChunkModelGeneration.edgeTable[cubeIndex] + "\n" +
                "cube index: " + cubeIndex + "\n" +
                "triTable: " + TerrainChunkModelGeneration.triTable[cubeIndex][i+0] + "\n" +
                "triTable: " + TerrainChunkModelGeneration.triTable[cubeIndex][i+1] + "\n" +
                "triTable: " + TerrainChunkModelGeneration.triTable[cubeIndex][i+2] + "\n" +
                vert0 + "\n" +
                vert1 + "\n" +
                vert2 + "\n" +
                index0 + "\n" +
                index1 + "\n" +
                index2 + "\n";
                for(int j = 0; j < 12; j++){
                    message = message + "vertList[" + j + "]" + vertList[j] + "\n";
                }
                for(int j = 0; j < 8; j++){
                    message = message + "grid.points[" + j + "] " + grid.points[j] + "\n";
                }
                for(int j = 0; j < 8; j++){
                    message = message + "grid.val[" + j + "] " + grid.val[j] + "\n";
                }
                throw new Error(
                    message
                );
            }

            //add 0's to normals until it matches vert count
            while(trianglesSharingVert.size() < verts.size()){
                trianglesSharingVert.add(0);
                normals.add(new Vector3f());
            }


            //add new triangle
            Triangle newTriangle = new Triangle(index0,index1,index2);
            triangles.add(newTriangle);
            ntriang++;




            //
            //Sampler triangles
            //
            for(int j = 0; j < 3; j++){
                //we add the triangle three times so all three vertices have the same values
                //that way they don't interpolate when you're in a middle point of the fragment
                //this could eventually potentially be optimized to send 1/3rd the data, but
                //the current approach is easier to reason about
                Vector3f samplerTriangle = new Vector3f(
                    grid.atlasValues[samplerIndex[TerrainChunkModelGeneration.triTable[cubeIndex][i+0]]],
                    grid.atlasValues[samplerIndex[TerrainChunkModelGeneration.triTable[cubeIndex][i+1]]],
                    grid.atlasValues[samplerIndex[TerrainChunkModelGeneration.triTable[cubeIndex][i+2]]]
                );
                samplerIndices.add(samplerTriangle);
            }




            //
            // Normals calculation
            //


            //calculate normal for new triangle
            Vector3f u = JomlPool.getF().set(verts.get(index1)).sub(verts.get(index0));
            Vector3f v = JomlPool.getF().set(verts.get(index2)).sub(verts.get(index1));
            float dotVal = u.dot(v);
            Vector3f n;
            // if(dotVal > NORMAL_DOT_THRESHOLD || dotVal < -NORMAL_DOT_THRESHOLD){
            //     n.set(u);
            // } else {
            n = new Vector3f(u.y * v.z - u.z * v.y, u.z * v.x - u.x * v.z, u.x * v.y - u.y * v.x).normalize();
            // }
            if(invertNormals){
                n = n.mul(-1);
            }
            if(!Float.isFinite(n.length())){
                throw new Error("Invalid normal!\n" +
                    verts.get(index0) + "\n" +
                    verts.get(index1) + "\n" +
                    verts.get(index2) + "\n" +
                    u + "\n" +
                    v + "\n" +
                    n + "\n" +
                    n.length() + "\n" +
                    dotVal
                );
            }
            JomlPool.release(u);
            JomlPool.release(v);



            //for each vertex, average the new normal with the normals that are already there
            int trianglesSharingIndex0 = trianglesSharingVert.get(index0);
            //calculate proportion of each normal
            float oldProportion = trianglesSharingIndex0 / (float)(trianglesSharingIndex0 + 1);
            float newProportion = 1.0f / (float)(trianglesSharingIndex0 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index0, trianglesSharingIndex0 + 1);

            Vector3f currentNormal = normals.get(index0);
            TransvoxelModelGeneration.averageNormals(currentNormal,oldProportion,n,newProportion);





            int trianglesSharingIndex1 = trianglesSharingVert.get(index1);
            //calculate proportion of each normal
            oldProportion = trianglesSharingIndex1 / (float)(trianglesSharingIndex1 + 1);
            newProportion = 1.0f / (float)(trianglesSharingIndex1 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index1, trianglesSharingIndex1 + 1);

            currentNormal = normals.get(index1);
            TransvoxelModelGeneration.averageNormals(currentNormal,oldProportion,n,newProportion);








            int trianglesSharingIndex2 = trianglesSharingVert.get(index2);
            //calculate proportion of each normal
            oldProportion = trianglesSharingIndex2 / (float)(trianglesSharingIndex2 + 1);
            newProportion = 1.0f / (float)(trianglesSharingIndex2 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index2, trianglesSharingIndex2 + 1);

            currentNormal = normals.get(index2);
            TransvoxelModelGeneration.averageNormals(currentNormal,oldProportion,n,newProportion);

        }

        return(ntriang);
    }


    /**
     * Generates a transition cell
     * @param grid The main grid cell
     * @param highresFaceValues The values along the face that are double-resolution
     * @param isolevel The iso level 
     * @param triangles The triangles buffer
     * @param samplerIndices The Sampler indices buffer
     * @param vertMap The vert map
     * @param verts The list of verts
     * @param normals The normal map
     * @param trianglesSharingVert The indices of triangles sharing verts -- Used for making sure normals are pointing correctly
     * @param invertNormals Override to invert normals
     * @return The number of triangles created
     */
    protected static int polygonizeTransition(
            TransitionGridCell transitionCell,
            List<Triangle> triangles,
            List<Vector3f> samplerIndices,
            Map<String,Integer> vertMap,
            List<Vector3f> verts,
            List<Vector3f> normals,
            List<Integer> trianglesSharingVert,
            boolean invertNormals,
            Vector3f[] vertList1,
            int[] samplerIndex,
            StringBuilder builder
        ){

        float isolevel = TerrainChunkModelGeneration.MIN_ISO_VALUE;


        /**
        

        For a transition face, the face vertices are numbered differently

        complex face numbering:

       6        7        8
        +-------+-------+
        |       |       |
        |       |       |
        |       |4      |
       3+-------+-------+ 5
        |       |       |
        |       |       |
        |       |       |
        +-------+-------+
       0        1        2




       non complex face numbering:

      B                 C
       +---------------+
       |               |
       |               |
       |               |
       |               |
       |               |
       |               |
       |               |
       +---------------+
      9                 A



       Coordinage system:
      
       Y
       ^
       |
       |
       |
       +---------> X




         */





        int i;
        int ntriang;
        int caseIndex = 0;
        Vector3f[] vertList = new Vector3f[12];
        for(int j = 0; j < 12; j++){
            vertList[j] = new Vector3f();
            samplerIndex[j] = 0;
        }


        /**
        The case index is constructed by using the values of the high-resolution face.
        We assign each of the vertices the following values:

       40       20      10
        +-------+-------+
        |       |       |
        |       |       |
        |       |100    |
      80+-------+-------+ 08
        |       |       |
        |       |       |
        |       |       |
        +-------+-------+
       01       02      04

         */

        //get lookup key (index) for edge table
        //edge table tells us which vertices are inside of the surface
        if (transitionCell.complexFaceValues[0][0] < isolevel) caseIndex |= 1;
        if (transitionCell.complexFaceValues[1][0] < isolevel) caseIndex |= 2;
        if (transitionCell.complexFaceValues[2][0] < isolevel) caseIndex |= 4;
        if (transitionCell.complexFaceValues[2][1] < isolevel) caseIndex |= 8;
        if (transitionCell.complexFaceValues[2][2] < isolevel) caseIndex |= 16;
        if (transitionCell.complexFaceValues[1][2] < isolevel) caseIndex |= 32;
        if (transitionCell.complexFaceValues[0][2] < isolevel) caseIndex |= 64;
        if (transitionCell.complexFaceValues[0][1] < isolevel) caseIndex |= 128;
        if (transitionCell.complexFaceValues[1][1] < isolevel) caseIndex |= 256;

        //Cube is entirely in/out of the surface
        if(caseIndex == 0 || caseIndex == 511){
            return(0);
        }


        //the class of transition cell
        short cellClass = transitionCellClass[caseIndex];
        int windingOrder = (cellClass >>> 7) & 1;

        LoggerInterface.loggerRenderer.DEBUG("Cell class: " + cellClass + " " + String.format("0x%02X", cellClass));


        /**
        
        The complex edges that a vertex can lie on are numbered as follows:

           83       84
        +-------+-------+
        |       |       |
      16|       |46     |86
        |  43   |  44   |
        +-------+-------+ 
        |       |       |
      15|       |45     |85
        |       |       |
        +-------+-------+
          23       24



          The simple edges that a vertex can lie on are numbered as follows:

                   88
            +---------------+
            |               |
            |               |
            |               |
          19|               |89
            |               |
            |               |
            |               |
            +---------------+
                   28



         */


        /*
        

        The sample locations are assigned an 8 bit code


        Complex face:


        10       81      80
         +-------+-------+
         |       |       |
         |       |       |
         |       |40     |
       12+-------+-------+ 82
         |       |       |
         |       |       |
         |       |       |
         +-------+-------+
        30       21      20


        Simple face:

      17                 87
        +---------------+
        |               |
        |               |
        |               |
        |               |
        |               |
        |               |
        |               |
        +---------------+
      37                 27






         */


        //
        //
        // Calculate the locations of all vertices for this cube
        //
        //

        int[] vertexData = transitionVertexData[caseIndex];

        //instead of having all intersections be perfectly at the midpoint,
        //for each edge this code calculates where along the edge to place the vertex
        //this should dramatically smooth the surface
        if(vertexData.length > 0){
            for(i = 0; i < vertexData.length; i++){
                //get into from transvoxel tables

                //contains the corner indexes of the edge's endpoints in one nibble each
                short lowByte = (short)(vertexData[i] & 0xFF);

                LoggerInterface.loggerRenderer.DEBUG("Low byte: ");
                LoggerInterface.loggerRenderer.DEBUG(String.format("0x%02X", lowByte));

                //contains the vertex reuse data
                //the bit values 1 and 2 indicate we should subtract one from the x or y coordinate respectively
                //they're never simultaneously set
                //bit 4 indicates a new vertex is to be created on an interior edge were we cannot reuse
                //bit 8 indicates that a new vertex is created on the maximal edge where it can be reused
                short highByte = (short)(vertexData[i] >> 8 & 0xFF);
                // int subX = highByte & 0x01;
                // int subY = highByte & 0x02;
                int newInteriorVertex = highByte & 0x04;
                // int vertexCanBeReused = highByte & 0x08;

                LoggerInterface.loggerRenderer.DEBUG("High byte: ");
                LoggerInterface.loggerRenderer.DEBUG(String.format("0x%02X", highByte));

                if(newInteriorVertex > 0){
                    LoggerInterface.loggerRenderer.DEBUG("New interior Vertex");
                }

                //the corner indices to sample
                int firstCornerSampleIndex = (int)(lowByte >> 4 & 0xF);
                int secondCornerSampleIndex = (int)(lowByte & 0xF);

                LoggerInterface.loggerRenderer.DEBUG("Corner indices: " + firstCornerSampleIndex + " " + secondCornerSampleIndex);

                //get the iso sample values
                float firstSample = TransvoxelModelGeneration.getTransvoxelSampleValue(transitionCell.simpleFaceValues,transitionCell.complexFaceValues,firstCornerSampleIndex);
                float secondSample = TransvoxelModelGeneration.getTransvoxelSampleValue(transitionCell.simpleFaceValues,transitionCell.complexFaceValues,secondCornerSampleIndex);

                //skip if it's 0-0 or 0-negative number
                if(firstSample <= 0 && secondSample <= 0){
                    continue;
                }

                //
                //Sample check -- we should never be interpolating between two samples of 0 value
                if(firstSample < 0 && secondSample < 0){
                    String message = "" +
                    transitionCell.complexFaceValues[0][2] + " " + transitionCell.complexFaceValues[1][2] + " " + transitionCell.complexFaceValues[2][2] + "\n" +
                    transitionCell.complexFaceValues[0][1] + " " + transitionCell.complexFaceValues[1][1] + " " + transitionCell.complexFaceValues[2][1] + "\n" +
                    transitionCell.complexFaceValues[0][0] + " " + transitionCell.complexFaceValues[1][0] + " " + transitionCell.complexFaceValues[2][0] + "\n" +
                    "\n" +
                    transitionCell.simpleFaceValues[0][1] + " " + transitionCell.simpleFaceValues[1][1] + "\n" +
                    transitionCell.simpleFaceValues[0][0] + " " + transitionCell.simpleFaceValues[1][0]
                    ;
                    LoggerInterface.loggerRenderer.ERROR(message, new IllegalStateException("Two samples in transvoxel algorithm are both zero -- can't interpolate between them!"));
                }

                //get the vertices we're interpolating
                //we need to map 0x0 through 0xC to the coordinates we're actually modifying
                Vector3f firstVertex = TransvoxelModelGeneration.getTransvoxelVectorByIndex(transitionCell,firstCornerSampleIndex);
                Vector3f secondVertex = TransvoxelModelGeneration.getTransvoxelVectorByIndex(transitionCell,secondCornerSampleIndex);
                
                //calculate interpolated vertex between the two samples such that it lies on the edge of the isosurface
                TransvoxelModelGeneration.VertexInterp(isolevel,firstVertex,secondVertex,firstSample,secondSample,vertList[i]);

                //figure out what sample we're pulling texture from
                if(firstSample > isolevel){
                    samplerIndex[i] = TransvoxelModelGeneration.getTransvoxelTextureValue(transitionCell.simpleFaceAtlasValues,transitionCell.complexFaceAtlasValues,firstCornerSampleIndex);
                } else {
                    samplerIndex[i] = TransvoxelModelGeneration.getTransvoxelTextureValue(transitionCell.simpleFaceAtlasValues,transitionCell.complexFaceAtlasValues,secondCornerSampleIndex);
                }
            }
        }




        //
        //
        //  Create the triangles using the vertices we just created
        //
        //

        //the triangle data
        //basically, a list of indices into the vertex array where every three entries
        //in triangleData correspondes to three vertices which we want to turn into a triangle
        int lookupValue = cellClass & 0x7F; //per instruction, must be ANDed before lookup
        LoggerInterface.loggerRenderer.DEBUG("Triangulation lookup value: " + lookupValue);
        TransitionCellData triangleData = transitionCellData[lookupValue];

        short vertexCount = (short)(triangleData.geometryCounts >> 4 & 0xF);
        short triangleCount = (short)(triangleData.geometryCounts & 0xF);
        LoggerInterface.loggerRenderer.DEBUG("Vertex Count: " + vertexCount + " Triangle Count: " + triangleCount);

        //Create the triangle
        ntriang = 0;
        for (i=0; i < triangleData.vertexIndex.length; i+=3) {
            //
            // Triangles calculation
            //
            //get indices -- these values are the same as the indices we sample from (refer to figure 4.19 for the listing)
            char cornerReuseData0 = transitionCornerData[triangleData.vertexIndex[i+0]];
            char cornerReuseData1 = transitionCornerData[triangleData.vertexIndex[i+1]];
            char cornerReuseData2 = transitionCornerData[triangleData.vertexIndex[i+2]];
            LoggerInterface.loggerRenderer.DEBUG((int)triangleData.vertexIndex[i+0] + " " + (int)triangleData.vertexIndex[i+1] + " " + (int)triangleData.vertexIndex[i+2]);
            LoggerInterface.loggerRenderer.DEBUG((int)cornerReuseData0 + " " + (int)cornerReuseData1 + " " + (int)cornerReuseData2);
            Vector3f vert0 = vertList[triangleData.vertexIndex[i+0]];
            Vector3f vert1 = vertList[triangleData.vertexIndex[i+1]];
            Vector3f vert2 = vertList[triangleData.vertexIndex[i+2]];

            //the verts can be zero if the samples are 0 and a negative number
            //in this case, we don't want to generate triangles
            if(vert0 == null || vert1 == null || vert2 == null){
                continue;
            }

            LoggerInterface.loggerRenderer.DEBUG(vert0 + "");
            LoggerInterface.loggerRenderer.DEBUG(vert1 + "");
            LoggerInterface.loggerRenderer.DEBUG(vert2 + "");
            int index0 = TransvoxelModelGeneration.getVertIndex(vert0,vertMap,verts,builder);
            int index1 = TransvoxelModelGeneration.getVertIndex(vert1,vertMap,verts,builder);
            int index2 = TransvoxelModelGeneration.getVertIndex(vert2,vertMap,verts,builder);

            if(windingOrder == 1){
                index0 = TransvoxelModelGeneration.getVertIndex(vert2,vertMap,verts,builder);
                index2 = TransvoxelModelGeneration.getVertIndex(vert0,vertMap,verts,builder);
            }

            //add 0's to normals until it matches vert count
            while(trianglesSharingVert.size() < verts.size()){
                trianglesSharingVert.add(0);
                normals.add(new Vector3f());
            }


            //add new triangle
            Triangle newTriangle = new Triangle(index0,index1,index2);
            triangles.add(newTriangle);
            ntriang++;




            //
            //Sampler triangles
            //
            for(int j = 0; j < 3; j++){
                //we add the triangle three times so all three vertices have the same values
                //that way they don't interpolate when you're in a middle point of the fragment
                //this could eventually potentially be optimized to send 1/3rd the data, but
                //the current approach is easier to reason about
                Vector3f samplerTriangle = new Vector3f(
                    samplerIndex[triangleData.vertexIndex[i+0]],
                    samplerIndex[triangleData.vertexIndex[i+1]],
                    samplerIndex[triangleData.vertexIndex[i+2]]
                );
                samplerIndices.add(samplerTriangle);
            }




            //
            // Normals calculation
            //


            //calculate normal for new triangle
            Vector3f u = JomlPool.getF().set(verts.get(index1)).sub(verts.get(index0));
            Vector3f v = JomlPool.getF().set(verts.get(index2)).sub(verts.get(index1));
            Vector3f n = new Vector3f(u.y * v.z - u.z * v.y, u.z * v.x - u.x * v.z, u.x * v.y - u.y * v.x).normalize();
            if(invertNormals){
                n = n.mul(-1);
            }
            JomlPool.release(u);
            JomlPool.release(v);



            //for each vertex, average the new normal with the normals that are already there
            int trianglesSharingIndex0 = trianglesSharingVert.get(index0);
            //calculate proportion of each normal
            float oldProportion = trianglesSharingIndex0 / (float)(trianglesSharingIndex0 + 1);
            float newProportion = 1.0f / (float)(trianglesSharingIndex0 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index0, trianglesSharingIndex0 + 1);

            Vector3f currentNormal = normals.get(index0);
            TransvoxelModelGeneration.averageNormals(currentNormal,oldProportion,n,newProportion);





            int trianglesSharingIndex1 = trianglesSharingVert.get(index1);
            //calculate proportion of each normal
            oldProportion = trianglesSharingIndex1 / (float)(trianglesSharingIndex1 + 1);
            newProportion = 1.0f / (float)(trianglesSharingIndex1 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index1, trianglesSharingIndex1 + 1);

            currentNormal = normals.get(index1);
            TransvoxelModelGeneration.averageNormals(currentNormal,oldProportion,n,newProportion);








            int trianglesSharingIndex2 = trianglesSharingVert.get(index2);
            //calculate proportion of each normal
            oldProportion = trianglesSharingIndex2 / (float)(trianglesSharingIndex2 + 1);
            newProportion = 1.0f / (float)(trianglesSharingIndex2 + 1);
            //increment number of triangles sharing vert
            trianglesSharingVert.set(index2, trianglesSharingIndex2 + 1);

            currentNormal = normals.get(index2);
            TransvoxelModelGeneration.averageNormals(currentNormal,oldProportion,n,newProportion);

        }

        return(ntriang);
    }

    /**
     * Sample grid values based on the index
     * @param simpleEdge simple edge values
     * @param complexEdgeValues complex edge values
     * @param index index to sample at
     * @return the iso value
     */
    static float getTransvoxelSampleValue(float[][] simpleEdge, float[][] complexEdgeValues, int index){
        if(index < 9){
            //sample from complex edge
            switch(index){
                case 0:
                    return complexEdgeValues[0][0];
                case 1:
                    return complexEdgeValues[1][0];
                case 2:
                    return complexEdgeValues[2][0];
                case 3:
                    return complexEdgeValues[0][1];
                case 4:
                    return complexEdgeValues[1][1];
                case 5:
                    return complexEdgeValues[2][1];
                case 6:
                    return complexEdgeValues[0][2];
                case 7:
                    return complexEdgeValues[1][2];
                case 8:
                    return complexEdgeValues[2][2];
            }
        } else {
            //sample from non-complex edge
            switch(index){
                case 9:
                    return simpleEdge[0][0];
                case 10:
                    return simpleEdge[1][0];
                case 11:
                    return simpleEdge[0][1];
                case 12:
                    return simpleEdge[1][1];
            }
        }
        return 0.0f;
    }

    /**
     * Sample grid values based on the index
     * @param simpleEdge simple edge values
     * @param complexEdgeValues complex edge values
     * @param index index to sample at
     * @return the iso value
     */
    static int getTransvoxelTextureValue(int[][] simpleEdge, int[][] complexEdgeValues, int index){
        if(index < 9){
            //sample from complex edge
            switch(index){
                case 0:
                    return complexEdgeValues[0][0];
                case 1:
                    return complexEdgeValues[1][0];
                case 2:
                    return complexEdgeValues[2][0];
                case 3:
                    return complexEdgeValues[0][1];
                case 4:
                    return complexEdgeValues[1][1];
                case 5:
                    return complexEdgeValues[2][1];
                case 6:
                    return complexEdgeValues[0][2];
                case 7:
                    return complexEdgeValues[1][2];
                case 8:
                    return complexEdgeValues[2][2];
            }
        } else {
            //sample from non-complex edge
            switch(index){
                case 9:
                    return simpleEdge[0][0];
                case 10:
                    return simpleEdge[1][0];
                case 11:
                    return simpleEdge[0][1];
                case 12:
                    return simpleEdge[1][1];
            }
        }
        return 0;
    }

    /**
     * Gets the vector from the transition grid cell based on its index
     * @param transitionCell The transition cell
     * @param index The index
     * @return The vector
     */
    static Vector3f getTransvoxelVectorByIndex(TransitionGridCell transitionCell, int index){
        if(index < 9){
            //sample from complex edge
            switch(index){
                case 0:
                    return transitionCell.complexFacePoints[0][0];
                case 1:
                    return transitionCell.complexFacePoints[1][0];
                case 2:
                    return transitionCell.complexFacePoints[2][0];
                case 3:
                    return transitionCell.complexFacePoints[0][1];
                case 4:
                    return transitionCell.complexFacePoints[1][1];
                case 5:
                    return transitionCell.complexFacePoints[2][1];
                case 6:
                    return transitionCell.complexFacePoints[0][2];
                case 7:
                    return transitionCell.complexFacePoints[1][2];
                case 8:
                    return transitionCell.complexFacePoints[2][2];
            }
        } else {
            //sample from non-complex edge
            switch(index){
                case 9:
                    return transitionCell.simpleFacePoints[0][0];
                case 10:
                    return transitionCell.simpleFacePoints[1][0];
                case 11:
                    return transitionCell.simpleFacePoints[0][1];
                case 12:
                    return transitionCell.simpleFacePoints[1][1];
            }
        }
        //index should never be greater than 12 as there are only 13 points we're concerned with per transition voxel
        throw new UnknownError("Managed to index outside of expected range");
    }

    /**
     * interpolates the location that the edge gets cut based on the magnitudes of the scalars of the vertices at either end of the edge
     * @param isolevel
     * @param p1
     * @param p2
     * @param valp1
     * @param valp2
     * @param vec
     */
    protected static void VertexInterp(double isolevel, Vector3f p1, Vector3f p2, double valp1, double valp2, Vector3f vec){
        double mu;
        float x, y, z;

        if(valp1 == 0){
            if(valp2 == 0){
                mu = 0.5f;
            } else if(valp2 > 0){
                vec.set(p2);
                return;
            } else {
                mu = 0.5;
            }
        } else if(valp2 == 0){
            if(valp1 > 0){
                vec.set(p1);
                return;
            } else {
                mu = 0.5;
            }
        } else {
            if(Math.abs(valp1-valp2) < MIN_DIST){
                mu = 0.5;
            } else {
                mu = (isolevel - valp1) / (valp2 - valp1);
            }
        }

        if(valp1 > 0 && valp2 > 0){
            throw new Error("Both values are positive! " + valp1 + " " + valp2);
        } else if(valp1 < 0 && valp2 < 0){
            throw new Error("Both values are negative! " + valp1 + " " + valp2);
        }

        // if(Math.abs(valp1-valp2) < MIN_DIST){
        //     mu = 0.5;
        // } else {
        //     mu = (isolevel - valp1) / (valp2 - valp1);
        // }

        x = (float)(p1.x + mu * (p2.x - p1.x));
        y = (float)(p1.y + mu * (p2.y - p1.y));
        z = (float)(p1.z + mu * (p2.z - p1.z));

        vec.set(x,y,z);
    }

    /**
     * Gets the already existing index of this point
     * @param vert the vertex's raw position
     * @param vertMap the map of key ->Vert index
     * @param verts 
     * @return
     */
    private static int getVertIndex(Vector3f vert, Map<String,Integer> vertMap, List<Vector3f> verts, StringBuilder builder){
        if(vert == null){
            throw new Error("Provided null value! " + vert);
        }
        builder.delete(0,builder.length());
        builder.append(vert.x);
        builder.append("_");
        builder.append(vert.y);
        builder.append("_");
        builder.append(vert.z);
        int rVal = -1;
        // String vertKey = TransvoxelModelGeneration.getVertKeyFromPoints(vert.x,vert.y,vert.z);
        if(vertMap.containsKey(builder.toString())){
            return vertMap.get(builder.toString());
        } else {
            rVal = verts.size();
            verts.add(new Vector3f(vert));
            vertMap.put(builder.toString(),rVal);
            return rVal;
        }
    }

    /**
     * Mixes two normals given a proportion value for each
     * @param normal0 The first vector
     * @param proportion0 The proportion of the first vector
     * @param normal1 The second vector
     * @param proportion1 The proportion of the second vector
     * @return The mixed vector
     */
    private static void averageNormals(Vector3f normal0, float proportion0, Vector3f normal1, float proportion1){
        normal0.set(
            normal0.x * proportion0 + normal1.x * proportion1,
            normal0.y * proportion0 + normal1.y * proportion1,
            normal0.z * proportion0 + normal1.z * proportion1
        );
    }


    /**
     * Generates mesh data given chunk data
     * @param chunkData The chunk data
     * @return The mesh data
     */
    public static TerrainChunkData generateTerrainChunkData(TransvoxelChunkData chunkData){

        //            5             6
        //            +-------------+               +-----5-------+     ^ Y                
        //          / |           / |             / |            /|     |    _             
        //        /   |         /   |           4   9         6   10    |    /\  Z         
        //    4 +-----+-------+  7  |         +-----+7------+     |     |   /              
        //      |   1 +-------+-----+ 2       |     +-----1-+-----+     |  /               
        //      |   /         |   /           8   0        11   2       | /                
        //      | /           | /             | /           | /         |/                 
        //    0 +-------------+ 3             +------3------+           +---------------> X


        

        //the current grid cell
        GridCell currentCell = new GridCell();
        //Transition grid cell
        TransitionGridCell currentTransitionCell = new TransitionGridCell();
        //the list of all triangles
        List<Triangle> triangles = new LinkedList<Triangle>();
        //the map of vertex to index
        Map<String,Integer> vertMap = new HashMap<String,Integer>();
        //the list of all verts
        List<Vector3f> verts = new LinkedList<Vector3f>();
        //the list of all normals
        List<Vector3f> normals = new LinkedList<Vector3f>();
        //the list of number of triangles that share a vert
        List<Integer> trianglesSharingVert = new LinkedList<Integer>();
        //List of texture sampler values
        List<Vector3f> samplerTriangles = new LinkedList<Vector3f>();

        Vector3f[] vertList = new Vector3f[12];
        int[] samplerIndex = new int[12];
        boolean[] skip = new boolean[12];
        for(int i = 0; i < 12; i++){
            vertList[i] = new Vector3f();
        }
        StringBuilder builder = new StringBuilder();

        //the vector pool
        Vector3f[] vecPool = new Vector3f[VECTOR_POOL_SIZE];
        for(int i = 0; i < VECTOR_POOL_SIZE; i++){
            vecPool[i] = new Vector3f();
        }

        currentCell.setVectors(
            vecPool[0], vecPool[1], vecPool[2], vecPool[3],
            vecPool[4], vecPool[5], vecPool[6], vecPool[7]
        );


        //
        //Generate the interior of the mesh
        for(int x = 1; x < chunkData.terrainGrid.length - 2; x++){
            for(int y = 1; y < chunkData.terrainGrid[0].length - 2; y++){
                for(int z = 1; z < chunkData.terrainGrid[0][0].length - 2; z++){
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        // vecPool[0], vecPool[1], vecPool[2], vecPool[3],
                        // vecPool[4], vecPool[5], vecPool[6], vecPool[7],
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        }

        int chunkWidth = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE;


        LoggerInterface.loggerRenderer.DEBUG("Triangles prior to transition cells: " + triangles.size());


        int xStartIndex = chunkData.xNegativeEdgeIso != null ? 1 : 0;
        int xEndIndex = chunkData.xPositiveEdgeIso != null ? chunkWidth - 2 : chunkWidth - 1;
        int yStartIndex = chunkData.yNegativeEdgeIso != null ? 1 : 0;
        int yEndIndex = chunkData.yPositiveEdgeIso != null ? chunkWidth - 2 : chunkWidth - 1;
        int zStartIndex = chunkData.zNegativeEdgeIso != null ? 1 : 0;
        int zEndIndex = chunkData.zPositiveEdgeIso != null ? chunkWidth - 2 : chunkWidth - 1;

        //
        //generate the x-positive face
        if(chunkData.xPositiveEdgeIso != null){
            int x = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 2;
            for(int y = yStartIndex; y < yEndIndex; y++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x+1,y,z);
                    vecPool[1].set(x+1,y+TRANSITION_CELL_WIDTH,z);
                    vecPool[2].set(x+1,y+1,z);
                    vecPool[3].set(x+1,y,z+TRANSITION_CELL_WIDTH);
                    vecPool[4].set(x+1,y+TRANSITION_CELL_WIDTH,z+TRANSITION_CELL_WIDTH);
                    vecPool[5].set(x+1,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[6].set(x+1,y,z+1);
                    vecPool[7].set(x+1,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[8].set(x+1,y+1,z+1);

                    vecPool[9].set(x+TRANSITION_CELL_WIDTH,y,z);
                    vecPool[10].set(x+TRANSITION_CELL_WIDTH,y+1,z);
                    vecPool[11].set(x+TRANSITION_CELL_WIDTH,y,z+1);
                    vecPool[12].set(x+TRANSITION_CELL_WIDTH,y+1,z+1);
                    //
                    //Generate the transition cell
                    //
                    currentTransitionCell.setValues(
                        //complex face vertex coordinates
                        // new Vector3f(x+1,y,z),                          new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z),                        new Vector3f(x+1,y+1,z),
                        // new Vector3f(x+1,y,z+TRANSITION_CELL_WIDTH),    new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+TRANSITION_CELL_WIDTH),  new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH),
                        // new Vector3f(x+1,y,z+1),                        new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+1),                      new Vector3f(x+1,y+1,z+1),
                        // //simple face vertex coordinates
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y,z),      new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z),
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y,z+1),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1),

                        //complex face vertex coordinates
                        vecPool[0], vecPool[1], vecPool[2],
                        vecPool[3], vecPool[4], vecPool[5],
                        vecPool[6], vecPool[7], vecPool[8],
                        //simple face vertex coordinates
                        vecPool[9], vecPool[10],
                        vecPool[11], vecPool[12],
                        //complex face iso values
                        chunkData.xPositiveEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xPositiveEdgeIso[(y+0)*2+1][(z+0)*2+0], chunkData.xPositiveEdgeIso[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xPositiveEdgeIso[(y+0)*2+0][(z+0)*2+1], chunkData.xPositiveEdgeIso[(y+0)*2+1][(z+0)*2+1], chunkData.xPositiveEdgeIso[(y+1)*2+0][(z+0)*2+1],
                        chunkData.xPositiveEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeIso[(y+0)*2+1][(z+1)*2+0], chunkData.xPositiveEdgeIso[(y+1)*2+0][(z+1)*2+0],
                        //simple face iso values
                        chunkData.xPositiveEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xPositiveEdgeIso[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xPositiveEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeIso[(y+1)*2+0][(z+1)*2+0],
                        //complex face texture atlas values
                        chunkData.xPositiveEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xPositiveEdgeAtlas[(y+0)*2+1][(z+0)*2+0], chunkData.xPositiveEdgeAtlas[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xPositiveEdgeAtlas[(y+0)*2+0][(z+0)*2+1], chunkData.xPositiveEdgeAtlas[(y+0)*2+1][(z+0)*2+1], chunkData.xPositiveEdgeAtlas[(y+1)*2+0][(z+0)*2+1],
                        chunkData.xPositiveEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeAtlas[(y+0)*2+1][(z+1)*2+0], chunkData.xPositiveEdgeAtlas[(y+1)*2+0][(z+1)*2+0],
                        //simple face texture atlas values
                        chunkData.xPositiveEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xPositiveEdgeAtlas[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xPositiveEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeAtlas[(y+1)*2+0][(z+1)*2+0]
                    );
                    TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, true, vertList, samplerIndex, builder);

                    //
                    //Generate the normal cell with half width
                    //
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+TRANSITION_CELL_WIDTH,y+0,z+1);
                    vecPool[3].set(x+TRANSITION_CELL_WIDTH,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+TRANSITION_CELL_WIDTH,y+1,z+1);
                    vecPool[7].set(x+TRANSITION_CELL_WIDTH,y+1,z+0);
                    currentCell.setValues(
                        // new Vector3f(x+0,y+0,z+0), new Vector3f(x+0,y+0,z+1), new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+1), new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+0),
                        // new Vector3f(x+0,y+1,z+0), new Vector3f(x+0,y+1,z+1), new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1), new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+0),
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.xPositiveEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeIso[(y+0)*2+0][(z+0)*2+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.xPositiveEdgeIso[(y+1)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeIso[(y+1)*2+0][(z+0)*2+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.xPositiveEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeAtlas[(y+0)*2+0][(z+0)*2+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.xPositiveEdgeAtlas[(y+1)*2+0][(z+1)*2+0], chunkData.xPositiveEdgeAtlas[(y+1)*2+0][(z+0)*2+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        } else {
            int x = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 2;
            for(int y = yStartIndex; y < yEndIndex; y++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        // vecPool[0], vecPool[1], vecPool[2], vecPool[3],
                        // vecPool[4], vecPool[5], vecPool[6], vecPool[7],
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        }

        LoggerInterface.loggerRenderer.DEBUG("Triangles after transition cells: " + triangles.size());





        //
        //generate the x-negative face
        if(chunkData.xNegativeEdgeIso != null){
            int x = 0;
            for(int y = yStartIndex; y < yEndIndex; y++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x,y,z);
                    vecPool[1].set(x,y+TRANSITION_CELL_WIDTH,z);
                    vecPool[2].set(x,y+1,z);
                    vecPool[3].set(x,y,z+TRANSITION_CELL_WIDTH);
                    vecPool[4].set(x,y+TRANSITION_CELL_WIDTH,z+TRANSITION_CELL_WIDTH);
                    vecPool[5].set(x,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[6].set(x,y,z+1);
                    vecPool[7].set(x,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[8].set(x,y+1,z+1);

                    vecPool[9].set(x+TRANSITION_CELL_WIDTH,y,z);
                    vecPool[10].set(x+TRANSITION_CELL_WIDTH,y+1,z);
                    vecPool[11].set(x+TRANSITION_CELL_WIDTH,y,z+1);
                    vecPool[12].set(x+TRANSITION_CELL_WIDTH,y+1,z+1);
                    //
                    //Generate the transition cell
                    //
                    currentTransitionCell.setValues(
                        // //complex face vertex coordinates
                        // new Vector3f(x,y,z),                        new Vector3f(x,y+TRANSITION_CELL_WIDTH,z),                          new Vector3f(x,y+1,z),
                        // new Vector3f(x,y,z+TRANSITION_CELL_WIDTH),  new Vector3f(x,y+TRANSITION_CELL_WIDTH,z+TRANSITION_CELL_WIDTH),    new Vector3f(x,y+1,z+TRANSITION_CELL_WIDTH),
                        // new Vector3f(x,y,z+1),                      new Vector3f(x,y+TRANSITION_CELL_WIDTH,z+1),                        new Vector3f(x,y+1,z+1),
                        // //simple face vertex coordinates
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y,z),      new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z),
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y,z+1),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1),

                        //complex face vertex coordinates
                        vecPool[0], vecPool[1], vecPool[2],
                        vecPool[3], vecPool[4], vecPool[5],
                        vecPool[6], vecPool[7], vecPool[8],
                        //simple face vertex coordinates
                        vecPool[9], vecPool[10],
                        vecPool[11], vecPool[12],
                        //complex face iso values
                        chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+0)*2+1][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+1], chunkData.xNegativeEdgeIso[(y+0)*2+1][(z+0)*2+1], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+1],
                        chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeIso[(y+0)*2+1][(z+1)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+1)*2+0],
                        //simple face iso values
                        chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+1)*2+0],
                        //complex face texture atlas values
                        chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+0)*2+1][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+1], chunkData.xNegativeEdgeAtlas[(y+0)*2+1][(z+0)*2+1], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+1],
                        chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeAtlas[(y+0)*2+1][(z+1)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+1)*2+0],
                        //simple face texture atlas values
                        chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+0],
                        chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+1)*2+0]
                    );
                    TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, builder);

                    //
                    //Generate the normal cell with half width
                    //
                    vecPool[0].set(x+TRANSITION_CELL_WIDTH,y+0,z+0);
                    vecPool[1].set(x+TRANSITION_CELL_WIDTH,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+TRANSITION_CELL_WIDTH,y+1,z+0);
                    vecPool[5].set(x+TRANSITION_CELL_WIDTH,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    currentCell.setValues(
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+0), new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+1), new Vector3f(x+1,y+0,z+1), new Vector3f(x+1,y+0,z+0),
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+0), new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1), new Vector3f(x+1,y+1,z+1), new Vector3f(x+1,y+1,z+0),
                        chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+1)*2+0], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+1)*2+0], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        } else {
            int x = 0;
            for(int y = yStartIndex; y < yEndIndex; y++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        // vecPool[0], vecPool[1], vecPool[2], vecPool[3],
                        // vecPool[4], vecPool[5], vecPool[6], vecPool[7],
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        }






        //
        //generate the y-positive face
        if(chunkData.yPositiveEdgeIso != null){
            int y = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 2;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x,y+1,z);
                    vecPool[1].set(x+TRANSITION_CELL_WIDTH,y+1,z);
                    vecPool[2].set(x+1,y+1,z);
                    vecPool[3].set(x,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[4].set(x+TRANSITION_CELL_WIDTH,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[5].set(x+1,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[6].set(x,y+1,z+1);
                    vecPool[7].set(x+TRANSITION_CELL_WIDTH,y+1,z+1);
                    vecPool[8].set(x+1,y+1,z+1);

                    vecPool[9].set(x,y+TRANSITION_CELL_WIDTH,z);
                    vecPool[10].set(x+1,y+TRANSITION_CELL_WIDTH,z);
                    vecPool[11].set(x,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[12].set(x+1,y+TRANSITION_CELL_WIDTH,z+1);
                    //
                    //Generate the transition cell
                    //
                    currentTransitionCell.setValues(
                        // //complex face vertex coordinates
                        // new Vector3f(x,y+1,z),                          new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z),                        new Vector3f(x+1,y+1,z),
                        // new Vector3f(x,y+1,z+TRANSITION_CELL_WIDTH),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+TRANSITION_CELL_WIDTH),  new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH),
                        // new Vector3f(x,y+1,z+1),                        new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1),                      new Vector3f(x+1,y+1,z+1),
                        // //simple face vertex coordinates
                        // new Vector3f(x,y+TRANSITION_CELL_WIDTH,z),      new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z),
                        // new Vector3f(x,y+TRANSITION_CELL_WIDTH,z+1),    new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+1),

                        //complex face vertex coordinates
                        vecPool[0], vecPool[1], vecPool[2],
                        vecPool[3], vecPool[4], vecPool[5],
                        vecPool[6], vecPool[7], vecPool[8],
                        //simple face vertex coordinates
                        vecPool[9], vecPool[10],
                        vecPool[11], vecPool[12],
                        //complex face iso values
                        chunkData.yPositiveEdgeIso[(x+0)*2+0][(z+0)*2+0], chunkData.yPositiveEdgeIso[(x+0)*2+1][(z+0)*2+0], chunkData.yPositiveEdgeIso[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yPositiveEdgeIso[(x+0)*2+0][(z+0)*2+1], chunkData.yPositiveEdgeIso[(x+0)*2+1][(z+0)*2+1], chunkData.yPositiveEdgeIso[(x+1)*2+0][(z+0)*2+1],
                        chunkData.yPositiveEdgeIso[(x+0)*2+0][(z+1)*2+0], chunkData.yPositiveEdgeIso[(x+0)*2+1][(z+1)*2+0], chunkData.yPositiveEdgeIso[(x+1)*2+0][(z+1)*2+0],
                        //simple face iso values
                        chunkData.yPositiveEdgeIso[(x+0)*2+0][(z+0)*2+0], chunkData.yPositiveEdgeIso[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yPositiveEdgeIso[(x+0)*2+0][(z+1)*2+0], chunkData.yPositiveEdgeIso[(x+1)*2+0][(z+1)*2+0],
                        //complex face texture atlas values
                        chunkData.yPositiveEdgeAtlas[(x+0)*2+0][(z+0)*2+0], chunkData.yPositiveEdgeAtlas[(x+0)*2+1][(z+0)*2+0], chunkData.yPositiveEdgeAtlas[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yPositiveEdgeAtlas[(x+0)*2+0][(z+0)*2+1], chunkData.yPositiveEdgeAtlas[(x+0)*2+1][(z+0)*2+1], chunkData.yPositiveEdgeAtlas[(x+1)*2+0][(z+0)*2+1],
                        chunkData.yPositiveEdgeAtlas[(x+0)*2+0][(z+1)*2+0], chunkData.yPositiveEdgeAtlas[(x+0)*2+1][(z+1)*2+0], chunkData.yPositiveEdgeAtlas[(x+1)*2+0][(z+1)*2+0],
                        //simple face texture atlas values
                        chunkData.yPositiveEdgeAtlas[(x+0)*2+0][(z+0)*2+0], chunkData.yPositiveEdgeAtlas[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yPositiveEdgeAtlas[(x+0)*2+0][(z+1)*2+0], chunkData.yPositiveEdgeAtlas[(x+1)*2+0][(z+1)*2+0]
                    );
                    TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, builder);

                    //
                    //Generate the normal cell with half width
                    //
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+TRANSITION_CELL_WIDTH,z+0);
                    vecPool[5].set(x+0,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[6].set(x+1,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[7].set(x+1,y+TRANSITION_CELL_WIDTH,z+0);
                    currentCell.setValues(
                        // new Vector3f(x+0,y+0,z+0),                      new Vector3f(x+0,y+0,z+1),                      new Vector3f(x+1,y,z+1),                        new Vector3f(x+1,y,z+0),
                        // new Vector3f(x+0,y+TRANSITION_CELL_WIDTH,z+0),  new Vector3f(x+0,y+TRANSITION_CELL_WIDTH,z+1),  new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+1),  new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+0),
                        chunkData.terrainGrid[x+0][y+0][z+0],               chunkData.terrainGrid[x+0][y+0][z+1],               chunkData.terrainGrid[x+1][y+0][z+1],               chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.yPositiveEdgeIso[(x+0)*2+0][(z+0)*2+0],   chunkData.yPositiveEdgeIso[(x+0)*2+0][(z+1)*2+0],   chunkData.yPositiveEdgeIso[(x+1)*2+0][(z+1)*2+0],   chunkData.yPositiveEdgeIso[(x+1)*2+0][(z+0)*2+0],
                        chunkData.textureGrid[x+0][y+0][z+0],                   chunkData.textureGrid[x+0][y+0][z+1],                   chunkData.textureGrid[x+1][y+0][z+1],                   chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.yPositiveEdgeAtlas[(x+0)*2+0][(z+0)*2+0],     chunkData.yPositiveEdgeAtlas[(x+0)*2+0][(z+1)*2+0],     chunkData.yPositiveEdgeAtlas[(x+1)*2+0][(z+1)*2+0],     chunkData.yPositiveEdgeAtlas[(x+1)*2+0][(z+0)*2+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        } else {
            int y = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 2;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        // vecPool[0], vecPool[1], vecPool[2], vecPool[3],
                        // vecPool[4], vecPool[5], vecPool[6], vecPool[7],
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        }






        //
        //generate the y-negative face
        if(chunkData.yNegativeEdgeIso != null){
            int y = 0;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x,y+0,z);
                    vecPool[1].set(x+TRANSITION_CELL_WIDTH,y+0,z);
                    vecPool[2].set(x+1,y+0,z);
                    vecPool[3].set(x,y+0,z+TRANSITION_CELL_WIDTH);
                    vecPool[4].set(x+TRANSITION_CELL_WIDTH,y+0,z+TRANSITION_CELL_WIDTH);
                    vecPool[5].set(x+1,y+0,z+TRANSITION_CELL_WIDTH);
                    vecPool[6].set(x,y+0,z+1);
                    vecPool[7].set(x+TRANSITION_CELL_WIDTH,y+0,z+1);
                    vecPool[8].set(x+1,y+0,z+1);

                    vecPool[9].set(x,y+TRANSITION_CELL_WIDTH,z);
                    vecPool[10].set(x+1,y+TRANSITION_CELL_WIDTH,z);
                    vecPool[11].set(x,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[12].set(x+1,y+TRANSITION_CELL_WIDTH,z+1);
                    //
                    //Generate the transition cell
                    //
                    currentTransitionCell.setValues(
                        // //complex face vertex coordinates
                        // new Vector3f(x,y+0,z),                          new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z),                        new Vector3f(x+1,y+0,z),
                        // new Vector3f(x,y+0,z+TRANSITION_CELL_WIDTH),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+TRANSITION_CELL_WIDTH),  new Vector3f(x+1,y+0,z+TRANSITION_CELL_WIDTH),
                        // new Vector3f(x,y+0,z+1),                        new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+1),                      new Vector3f(x+1,y+0,z+1),
                        // //simple face vertex coordinates
                        // new Vector3f(x,y+TRANSITION_CELL_WIDTH,z),      new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z),
                        // new Vector3f(x,y+TRANSITION_CELL_WIDTH,z+1),    new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+1),

                        //complex face vertex coordinates
                        vecPool[0], vecPool[1], vecPool[2],
                        vecPool[3], vecPool[4], vecPool[5],
                        vecPool[6], vecPool[7], vecPool[8],
                        //simple face vertex coordinates
                        vecPool[9], vecPool[10],
                        vecPool[11], vecPool[12],
                        //complex face iso values
                        chunkData.yNegativeEdgeIso[(x+0)*2+0][(z+0)*2+0], chunkData.yNegativeEdgeIso[(x+0)*2+1][(z+0)*2+0], chunkData.yNegativeEdgeIso[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yNegativeEdgeIso[(x+0)*2+0][(z+0)*2+1], chunkData.yNegativeEdgeIso[(x+0)*2+1][(z+0)*2+1], chunkData.yNegativeEdgeIso[(x+1)*2+0][(z+0)*2+1],
                        chunkData.yNegativeEdgeIso[(x+0)*2+0][(z+1)*2+0], chunkData.yNegativeEdgeIso[(x+0)*2+1][(z+1)*2+0], chunkData.yNegativeEdgeIso[(x+1)*2+0][(z+1)*2+0],
                        //simple face iso values
                        chunkData.yNegativeEdgeIso[(x+0)*2+0][(z+0)*2+0], chunkData.yNegativeEdgeIso[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yNegativeEdgeIso[(x+0)*2+0][(z+1)*2+0], chunkData.yNegativeEdgeIso[(x+1)*2+0][(z+1)*2+0],
                        //complex face texture atlas values
                        chunkData.yNegativeEdgeAtlas[(x+0)*2+0][(z+0)*2+0], chunkData.yNegativeEdgeAtlas[(x+0)*2+1][(z+0)*2+0], chunkData.yNegativeEdgeAtlas[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yNegativeEdgeAtlas[(x+0)*2+0][(z+0)*2+1], chunkData.yNegativeEdgeAtlas[(x+0)*2+1][(z+0)*2+1], chunkData.yNegativeEdgeAtlas[(x+1)*2+0][(z+0)*2+1],
                        chunkData.yNegativeEdgeAtlas[(x+0)*2+0][(z+1)*2+0], chunkData.yNegativeEdgeAtlas[(x+0)*2+1][(z+1)*2+0], chunkData.yNegativeEdgeAtlas[(x+1)*2+0][(z+1)*2+0],
                        //simple face texture atlas values
                        chunkData.yNegativeEdgeAtlas[(x+0)*2+0][(z+0)*2+0], chunkData.yNegativeEdgeAtlas[(x+1)*2+0][(z+0)*2+0],
                        chunkData.yNegativeEdgeAtlas[(x+0)*2+0][(z+1)*2+0], chunkData.yNegativeEdgeAtlas[(x+1)*2+0][(z+1)*2+0]
                    );
                    TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, true, vertList, samplerIndex, builder);

                    //
                    //Generate the normal cell with half width
                    //
                    vecPool[0].set(x+0,y+1,z+0);
                    vecPool[1].set(x+0,y+1,z+1);
                    vecPool[2].set(x+1,y+1,z+1);
                    vecPool[3].set(x+1,y+1,z+0);
                    vecPool[4].set(x+0,y+TRANSITION_CELL_WIDTH,z+0);
                    vecPool[5].set(x+0,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[6].set(x+1,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[7].set(x+1,y+TRANSITION_CELL_WIDTH,z+0);
                    currentCell.setValues(
                        // new Vector3f(x+0,y+1,z+0),                      new Vector3f(x+0,y+1,z+1),                      new Vector3f(x+1,y+1,z+1),                      new Vector3f(x+1,y+1,z+0),
                        // new Vector3f(x+0,y+TRANSITION_CELL_WIDTH,z+0),  new Vector3f(x+0,y+TRANSITION_CELL_WIDTH,z+1),  new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+1),  new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+0),
                        chunkData.terrainGrid[x+0][y+1][z+0],               chunkData.terrainGrid[x+0][y+1][z+1],               chunkData.terrainGrid[x+1][y+1][z+1],               chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.yNegativeEdgeIso[(x+0)*2+0][(z+0)*2+0],   chunkData.yNegativeEdgeIso[(x+0)*2+0][(z+1)*2+0],   chunkData.yNegativeEdgeIso[(x+1)*2+0][(z+1)*2+0],   chunkData.yNegativeEdgeIso[(x+1)*2+0][(z+0)*2+0],
                        chunkData.textureGrid[x+0][y+1][z+0],                   chunkData.textureGrid[x+0][y+1][z+1],                   chunkData.textureGrid[x+1][y+1][z+1],                   chunkData.textureGrid[x+1][y+1][z+0],
                        chunkData.yNegativeEdgeAtlas[(x+0)*2+0][(z+0)*2+0],     chunkData.yNegativeEdgeAtlas[(x+0)*2+0][(z+1)*2+0],     chunkData.yNegativeEdgeAtlas[(x+1)*2+0][(z+1)*2+0],     chunkData.yNegativeEdgeAtlas[(x+1)*2+0][(z+0)*2+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, true, vertList, samplerIndex, skip, builder);
                }
            }
        } else {
            int y = 0;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int z = zStartIndex; z < zEndIndex; z++){
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        // vecPool[0], vecPool[1], vecPool[2], vecPool[3],
                        // vecPool[4], vecPool[5], vecPool[6], vecPool[7],
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        }







        //
        //generate the z-positive face
        if(chunkData.zPositiveEdgeIso != null){
            int z = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 2;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int y = yStartIndex; y < yEndIndex; y++){
                    vecPool[0].set(x+0,y,z+1);
                    vecPool[1].set(x+0,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[2].set(x+0,y+1,z+1);
                    vecPool[3].set(x+TRANSITION_CELL_WIDTH,y,z+1);
                    vecPool[4].set(x+TRANSITION_CELL_WIDTH,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[5].set(x+TRANSITION_CELL_WIDTH,y+1,z+1);
                    vecPool[6].set(x+1,y,z+1);
                    vecPool[7].set(x+1,y+TRANSITION_CELL_WIDTH,z+1);
                    vecPool[8].set(x+1,y+1,z+1);

                    vecPool[9].set(x+0,y,z+TRANSITION_CELL_WIDTH);
                    vecPool[10].set(x+0,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[11].set(x+1,y,z+TRANSITION_CELL_WIDTH);
                    vecPool[12].set(x+1,y+1,z+TRANSITION_CELL_WIDTH);
                    //
                    //Generate the transition cell
                    //
                    currentTransitionCell.setValues(
                        // //complex face vertex coordinates
                        // new Vector3f(x+0,y,z+1),                        new Vector3f(x+0,y+TRANSITION_CELL_WIDTH,z+1),                          new Vector3f(x+0,y+1,z+1),
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y,z+1),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+TRANSITION_CELL_WIDTH,z+1),      new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1),
                        // new Vector3f(x+1,y,z+1),                        new Vector3f(x+1,y+TRANSITION_CELL_WIDTH,z+1),                          new Vector3f(x+1,y+1,z+1),
                        // //simple face vertex coordinates
                        // new Vector3f(x+0,y,z+TRANSITION_CELL_WIDTH), new Vector3f(x+0,y+1,z+TRANSITION_CELL_WIDTH),
                        // new Vector3f(x+1,y,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH),

                        //complex face vertex coordinates
                        vecPool[0], vecPool[1], vecPool[2],
                        vecPool[3], vecPool[4], vecPool[5],
                        vecPool[6], vecPool[7], vecPool[8],
                        //simple face vertex coordinates
                        vecPool[9], vecPool[10],
                        vecPool[11], vecPool[12],
                        //complex face iso values
                        chunkData.zPositiveEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeIso[(x+0)*2+0][(y+0)*2+1], chunkData.zPositiveEdgeIso[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zPositiveEdgeIso[(x+0)*2+1][(y+0)*2+0], chunkData.zPositiveEdgeIso[(x+0)*2+1][(y+0)*2+1], chunkData.zPositiveEdgeIso[(x+0)*2+1][(y+1)*2+0],
                        chunkData.zPositiveEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeIso[(x+1)*2+0][(y+0)*2+1], chunkData.zPositiveEdgeIso[(x+1)*2+0][(y+1)*2+0],
                        //simple face iso values
                        chunkData.zPositiveEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeIso[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zPositiveEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeIso[(x+1)*2+0][(y+1)*2+0],
                        //complex face texture atlas values
                        chunkData.zPositiveEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeAtlas[(x+0)*2+0][(y+0)*2+1], chunkData.zPositiveEdgeAtlas[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zPositiveEdgeAtlas[(x+0)*2+1][(y+0)*2+0], chunkData.zPositiveEdgeAtlas[(x+0)*2+1][(y+0)*2+1], chunkData.zPositiveEdgeAtlas[(x+0)*2+1][(y+1)*2+0],
                        chunkData.zPositiveEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeAtlas[(x+1)*2+0][(y+0)*2+1], chunkData.zPositiveEdgeAtlas[(x+1)*2+0][(y+1)*2+0],
                        //simple face texture atlas values
                        chunkData.zPositiveEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeAtlas[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zPositiveEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeAtlas[(x+1)*2+0][(y+1)*2+0]
                    );
                    TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, builder);

                    //
                    //Generate the normal cell with half width
                    //
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+TRANSITION_CELL_WIDTH);
                    vecPool[2].set(x+1,y+0,z+TRANSITION_CELL_WIDTH);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[6].set(x+1,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[7].set(x+1,y+1,z+0);
                    currentCell.setValues(
                        // new Vector3f(x+0,y+0,z+0), new Vector3f(x+0,y+0,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+0,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+0,z+0),
                        // new Vector3f(x+0,y+1,z+0), new Vector3f(x+0,y+1,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+1,z+0),
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.zPositiveEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.zPositiveEdgeIso[(x+0)*2+0][(y+1)*2+0], chunkData.zPositiveEdgeIso[(x+1)*2+0][(y+1)*2+0], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.zPositiveEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zPositiveEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.zPositiveEdgeAtlas[(x+0)*2+0][(y+1)*2+0], chunkData.zPositiveEdgeAtlas[(x+1)*2+0][(y+1)*2+0], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        } else {
            int z = ServerTerrainChunk.CHUNK_DATA_GENERATOR_SIZE - 2;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int y = yStartIndex; y < yEndIndex; y++){
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        // vecPool[0], vecPool[1], vecPool[2], vecPool[3],
                        // vecPool[4], vecPool[5], vecPool[6], vecPool[7],
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        }







        //
        //generate the z-negative face
        if(chunkData.zNegativeEdgeIso != null){
            int z = 0;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int y = yStartIndex; y < yEndIndex; y++){
                    vecPool[0].set(x+0,                    y,z);
                    vecPool[1].set(x+0,                    y+TRANSITION_CELL_WIDTH,z);
                    vecPool[2].set(x+0,                    y+1,z);
                    vecPool[3].set(x+TRANSITION_CELL_WIDTH,y,z);
                    vecPool[4].set(x+TRANSITION_CELL_WIDTH,y+TRANSITION_CELL_WIDTH,z);
                    vecPool[5].set(x+TRANSITION_CELL_WIDTH,y+1,z);
                    vecPool[6].set(x+1,                    y,z);
                    vecPool[7].set(x+1,                    y+TRANSITION_CELL_WIDTH,z);
                    vecPool[8].set(x+1,                    y+1,z);

                    vecPool[9].set(x+0,y,z+TRANSITION_CELL_WIDTH);
                    vecPool[10].set(x+0,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[11].set(x+1,y,z+TRANSITION_CELL_WIDTH);
                    vecPool[12].set(x+1,y+1,z+TRANSITION_CELL_WIDTH);
                    //
                    //Generate the transition cell
                    //
                    currentTransitionCell.setValues(
                        // //complex face vertex coordinates
                        // new Vector3f(x+0,                    y,z),  new Vector3f(x+0,                    y+TRANSITION_CELL_WIDTH,z),    new Vector3f(x+0,                    y+1,z),
                        // new Vector3f(x+TRANSITION_CELL_WIDTH,y,z),  new Vector3f(x+TRANSITION_CELL_WIDTH,y+TRANSITION_CELL_WIDTH,z),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z),
                        // new Vector3f(x+1,                    y,z),  new Vector3f(x+1,                    y+TRANSITION_CELL_WIDTH,z),    new Vector3f(x+1,                    y+1,z),
                        // //simple face vertex coordinates
                        // new Vector3f(x+0,y,z+TRANSITION_CELL_WIDTH), new Vector3f(x+0,y+1,z+TRANSITION_CELL_WIDTH),
                        // new Vector3f(x+1,y,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH),

                        //complex face vertex coordinates
                        vecPool[0], vecPool[1], vecPool[2],
                        vecPool[3], vecPool[4], vecPool[5],
                        vecPool[6], vecPool[7], vecPool[8],
                        //simple face vertex coordinates
                        vecPool[9], vecPool[10],
                        vecPool[11], vecPool[12],
                        //complex face iso values
                        chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zNegativeEdgeIso[(x+0)*2+1][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+0)*2+1][(y+0)*2+1], chunkData.zNegativeEdgeIso[(x+0)*2+1][(y+1)*2+0],
                        chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+1)*2+0],
                        //simple face iso values
                        chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+1)*2+0],
                        //complex face texture atlas values
                        chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zNegativeEdgeAtlas[(x+0)*2+1][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+0)*2+1][(y+0)*2+1], chunkData.zNegativeEdgeAtlas[(x+0)*2+1][(y+1)*2+0],
                        chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+1)*2+0],
                        //simple face texture atlas values
                        chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+1)*2+0],
                        chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+1)*2+0]
                    );
                    TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, true, vertList, samplerIndex, builder);

                    //
                    //Generate the normal cell with half width
                    //
                    vecPool[0].set(x+0,y+0,z+1);
                    vecPool[1].set(x+0,y+0,z+TRANSITION_CELL_WIDTH);
                    vecPool[2].set(x+1,y+0,z+TRANSITION_CELL_WIDTH);
                    vecPool[3].set(x+1,y+0,z+1);
                    vecPool[4].set(x+0,y+1,z+1);
                    vecPool[5].set(x+0,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[6].set(x+1,y+1,z+TRANSITION_CELL_WIDTH);
                    vecPool[7].set(x+1,y+1,z+1);
                    currentCell.setValues(
                        // new Vector3f(x+0,y+0,z+1), new Vector3f(x+0,y+0,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+0,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+0,z+1),
                        // new Vector3f(x+0,y+1,z+1), new Vector3f(x+0,y+1,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+1,z+1),
                        chunkData.terrainGrid[x+0][y+0][z+1], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.terrainGrid[x+1][y+0][z+1],
                        chunkData.terrainGrid[x+0][y+1][z+1], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+1)*2+0], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+1)*2+0], chunkData.terrainGrid[x+1][y+1][z+1],
                        chunkData.textureGrid[x+0][y+0][z+1], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.textureGrid[x+1][y+0][z+1],
                        chunkData.textureGrid[x+0][y+1][z+1], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+1)*2+0], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+1)*2+0], chunkData.textureGrid[x+1][y+1][z+1]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, true, vertList, samplerIndex, skip, builder);
                }
            }
        } else {
            int z = 0;
            for(int x = xStartIndex; x < xEndIndex; x++){
                for(int y = yStartIndex; y < yEndIndex; y++){
                    vecPool[0].set(x+0,y+0,z+0);
                    vecPool[1].set(x+0,y+0,z+1);
                    vecPool[2].set(x+1,y+0,z+1);
                    vecPool[3].set(x+1,y+0,z+0);
                    vecPool[4].set(x+0,y+1,z+0);
                    vecPool[5].set(x+0,y+1,z+1);
                    vecPool[6].set(x+1,y+1,z+1);
                    vecPool[7].set(x+1,y+1,z+0);
                    //push the current cell's values into the gridcell
                    currentCell.setValues(
                        // vecPool[0], vecPool[1], vecPool[2], vecPool[3],
                        // vecPool[4], vecPool[5], vecPool[6], vecPool[7],
                        chunkData.terrainGrid[x+0][y+0][z+0], chunkData.terrainGrid[x+0][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
                        chunkData.terrainGrid[x+0][y+1][z+0], chunkData.terrainGrid[x+0][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
                        chunkData.textureGrid[x+0][y+0][z+0], chunkData.textureGrid[x+0][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
                        chunkData.textureGrid[x+0][y+1][z+0], chunkData.textureGrid[x+0][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
                    );
                    //polygonize the current gridcell
                    TransvoxelModelGeneration.polygonize(currentCell, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false, vertList, samplerIndex, skip, builder);
                }
            }
        }

        
        //xn-zn edge
        // if(chunkData.xNegativeEdgeIso != null && chunkData.zNegativeEdgeIso != null){
        //     int x = 0;
        //     int z = 0;
        //     int edgeLength = chunkWidth - (chunkData.yNegativeEdgeIso != null ? 1 : 0) - (chunkData.yPositiveEdgeIso != null ? 1 : 0);
        //     int startIndex = 0 + (chunkData.yNegativeEdgeIso != null ? 1 : 0);
        //     for(int i = startIndex; i < edgeLength - 1; i++){
        //         int y = i;
        //         //
        //         //Generate the x-side transition cell
        //         //
        //         currentTransitionCell.setValues(
        //             //complex face vertex coordinates
        //             new Vector3f(x,y,z),                        new Vector3f(x,y+TRANSITION_CELL_WIDTH,z),                          new Vector3f(x,y+1,z),
        //             new Vector3f(x,y,z+TRANSITION_CELL_WIDTH),  new Vector3f(x,y+TRANSITION_CELL_WIDTH,z+TRANSITION_CELL_WIDTH),    new Vector3f(x,y+1,z+TRANSITION_CELL_WIDTH),
        //             new Vector3f(x,y,z+1),                      new Vector3f(x,y+TRANSITION_CELL_WIDTH,z+1),                        new Vector3f(x,y+1,z+1),
        //             //simple face vertex coordinates
        //             new Vector3f(x+TRANSITION_CELL_WIDTH,y,z+TRANSITION_CELL_WIDTH),      new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+TRANSITION_CELL_WIDTH),
        //             new Vector3f(x+TRANSITION_CELL_WIDTH,y,z+1),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1),
        //             //complex face iso values
        //             chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+0)*2+1][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+0],
        //             chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+1], chunkData.xNegativeEdgeIso[(y+0)*2+1][(z+0)*2+1], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+1],
        //             chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeIso[(y+0)*2+1][(z+1)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+1)*2+0],
        //             //simple face iso values
        //             chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+0],
        //             chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+1)*2+0],
        //             //complex face texture atlas values
        //             chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+0)*2+1][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+0],
        //             chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+1], chunkData.xNegativeEdgeAtlas[(y+0)*2+1][(z+0)*2+1], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+1],
        //             chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeAtlas[(y+0)*2+1][(z+1)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+1)*2+0],
        //             //simple face texture atlas values
        //             chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+0],
        //             chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+1)*2+0]
        //         );
        //         TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, TerrainChunkModelGeneration.MIN_ISO_VALUE, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, false);

        //         //
        //         //Generate the z-side transition cell
        //         //
        //         currentTransitionCell.setValues(
        //             //complex face vertex coordinates
        //             new Vector3f(x+0,                    y,z),  new Vector3f(x+0,                    y+TRANSITION_CELL_WIDTH,z),    new Vector3f(x+0,                    y+1,z),
        //             new Vector3f(x+TRANSITION_CELL_WIDTH,y,z),  new Vector3f(x+TRANSITION_CELL_WIDTH,y+TRANSITION_CELL_WIDTH,z),    new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z),
        //             new Vector3f(x+1,                    y,z),  new Vector3f(x+1,                    y+TRANSITION_CELL_WIDTH,z),    new Vector3f(x+1,                    y+1,z),
        //             //simple face vertex coordinates
        //             new Vector3f(x+TRANSITION_CELL_WIDTH,y,z+TRANSITION_CELL_WIDTH), new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+TRANSITION_CELL_WIDTH),
        //             new Vector3f(x+1,y,z+TRANSITION_CELL_WIDTH), new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH),
        //             //complex face iso values
        //             chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+1)*2+0],
        //             chunkData.zNegativeEdgeIso[(x+0)*2+1][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+0)*2+1][(y+0)*2+1], chunkData.zNegativeEdgeIso[(x+0)*2+1][(y+1)*2+0],
        //             chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+1)*2+0],
        //             //simple face iso values
        //             chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+0)*2+0][(y+1)*2+0],
        //             chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeIso[(x+1)*2+0][(y+1)*2+0],
        //             //complex face texture atlas values
        //             chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+1)*2+0],
        //             chunkData.zNegativeEdgeAtlas[(x+0)*2+1][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+0)*2+1][(y+0)*2+1], chunkData.zNegativeEdgeAtlas[(x+0)*2+1][(y+1)*2+0],
        //             chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+0)*2+1], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+1)*2+0],
        //             //simple face texture atlas values
        //             chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+0)*2+0][(y+1)*2+0],
        //             chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+0)*2+0], chunkData.zNegativeEdgeAtlas[(x+1)*2+0][(y+1)*2+0]
        //         );
        //         TransvoxelModelGeneration.polygonizeTransition(currentTransitionCell, TerrainChunkModelGeneration.MIN_ISO_VALUE, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, true);

        //         //
        //         //Generate the normal cell with half width
        //         //
        //         vecPool[0].set(x+TRANSITION_CELL_WIDTH,y+0,z+TRANSITION_CELL_WIDTH);
        //         vecPool[1].set(x+TRANSITION_CELL_WIDTH,y+0,z+1);
        //         vecPool[2].set(x+1,y+0,z+1);
        //         vecPool[3].set(x+1,y+0,z+TRANSITION_CELL_WIDTH);
        //         vecPool[4].set(x+TRANSITION_CELL_WIDTH,y+1,z+TRANSITION_CELL_WIDTH);
        //         vecPool[5].set(x+TRANSITION_CELL_WIDTH,y+1,z+1);
        //         vecPool[6].set(x+1,y+1,z+1);
        //         vecPool[7].set(x+1,y+1,z+TRANSITION_CELL_WIDTH);
        //         currentCell.setValues(
        //             // new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+TRANSITION_CELL_WIDTH), new Vector3f(x+TRANSITION_CELL_WIDTH,y+0,z+1), new Vector3f(x+1,y+0,z+1), new Vector3f(x+1,y+0,z+TRANSITION_CELL_WIDTH),
        //             // new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+TRANSITION_CELL_WIDTH), new Vector3f(x+TRANSITION_CELL_WIDTH,y+1,z+1), new Vector3f(x+1,y+1,z+1), new Vector3f(x+1,y+1,z+TRANSITION_CELL_WIDTH),
        //             chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+0)*2+0][(z+1)*2+0], chunkData.terrainGrid[x+1][y+0][z+1], chunkData.terrainGrid[x+1][y+0][z+0],
        //             chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeIso[(y+1)*2+0][(z+1)*2+0], chunkData.terrainGrid[x+1][y+1][z+1], chunkData.terrainGrid[x+1][y+1][z+0],
        //             chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+0)*2+0][(z+1)*2+0], chunkData.textureGrid[x+1][y+0][z+1], chunkData.textureGrid[x+1][y+0][z+0],
        //             chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+0)*2+0], chunkData.xNegativeEdgeAtlas[(y+1)*2+0][(z+1)*2+0], chunkData.textureGrid[x+1][y+1][z+1], chunkData.textureGrid[x+1][y+1][z+0]
        //         );
        //         //polygonize the current gridcell
        //         TransvoxelModelGeneration.polygonize(currentCell, TerrainChunkModelGeneration.MIN_ISO_VALUE, triangles, samplerTriangles, vertMap, verts, normals, trianglesSharingVert, true);
        //     }
        // }


        if (verts.size() != normals.size()){
            throw new Error("Generated invalid number of elements in lists! " + verts.size() + " " + normals.size());
        }


        //incrementer
        int i = 0;


        //all elements of faces in order
        int[] elementsFlat = new int[triangles.size() * ELEMENTS_PER_TRIANGLE * INTS_PER_ELEMENT];
        //all verts in order, flattened as an array of floats instead of vecs
        float[] vertsFlat = new float[triangles.size() * VERTS_PER_TRIANGLE * FLOATS_PER_VERT];
        //all normals in order, flattened as an array of floats instead of vecs
        float[] normalsFlat = new float[triangles.size() * VERTS_PER_TRIANGLE * FLOATS_PER_VERT];
        //List of UVs
        float[] UVs = new float[triangles.size() * VERTS_PER_TRIANGLE * FLOATS_PER_UV];
        //List of texture sampler values
        float[] textureSamplers = new float[samplerTriangles.size() * SAMPLER_INDICES_PER_TRIANGLE];
        //List of texture ratio values
        float[] textureRatioData = new float[triangles.size() * SAMPLER_INDICES_PER_TRIANGLE * SAMPLER_VALUES_PER_VERT];

        float scalingFactor = (float)Math.pow(2,chunkData.levelOfDetail);


        //store indices
        for(int j = 0; j < elementsFlat.length; j++){
            //assigning a unique number to each element guarantees there's no vertex sharing between triangles
            //we don't want vertex sharing because then the UVs are shared
            elementsFlat[j] = j;
        }

        //flatten verts + normals + uvs
        i = 0;
        Vector3f vert = null;
        Vector3f normal = null;
        for(Triangle triangle : triangles){

            //
            //point 1 of the triangle
            vert = verts.get(triangle.indices[0]);
            normal = normals.get(triangle.indices[0]);
            vertsFlat[i*9+0] = vert.x * scalingFactor;
            vertsFlat[i*9+1] = vert.y * scalingFactor;
            vertsFlat[i*9+2] = vert.z * scalingFactor;

            normalsFlat[i*9+0] = normal.x;
            normalsFlat[i*9+1] = normal.y;
            normalsFlat[i*9+2] = normal.z;

            UVs[i*6+0] = vert.z * Math.abs(normal.x) + vert.x * Math.abs(normal.y) + vert.x * Math.abs(normal.z);
            UVs[i*6+1] = vert.y * Math.abs(normal.x) + vert.z * Math.abs(normal.y) + vert.y * Math.abs(normal.z);


            //
            //point 2 of the triangle
            vert = verts.get(triangle.indices[1]);
            normal = normals.get(triangle.indices[1]);
            vertsFlat[i*9+3] = vert.x * scalingFactor;
            vertsFlat[i*9+4] = vert.y * scalingFactor;
            vertsFlat[i*9+5] = vert.z * scalingFactor;

            normalsFlat[i*9+3] = normal.x;
            normalsFlat[i*9+4] = normal.y;
            normalsFlat[i*9+5] = normal.z;

            UVs[i*6+2] = vert.z * Math.abs(normal.x) + vert.x * Math.abs(normal.y) + vert.x * Math.abs(normal.z);
            UVs[i*6+3] = vert.y * Math.abs(normal.x) + vert.z * Math.abs(normal.y) + vert.y * Math.abs(normal.z);


            //
            //point 3 of the triangle
            vert = verts.get(triangle.indices[2]);
            normal = normals.get(triangle.indices[2]);
            vertsFlat[i*9+6] = vert.x * scalingFactor;
            vertsFlat[i*9+7] = vert.y * scalingFactor;
            vertsFlat[i*9+8] = vert.z * scalingFactor;
            
            normalsFlat[i*9+6] = normal.x;
            normalsFlat[i*9+7] = normal.y;
            normalsFlat[i*9+8] = normal.z;

            UVs[i*6+4] = vert.z * Math.abs(normal.x) + vert.x * Math.abs(normal.y) + vert.x * Math.abs(normal.z);
            UVs[i*6+5] = vert.y * Math.abs(normal.x) + vert.z * Math.abs(normal.y) + vert.y * Math.abs(normal.z);

            
            i++;
        }

        //flatten sampler indices
        i = 0;
        for(Vector3f samplerVec : samplerTriangles){
            textureSamplers[i*3+0] = (float)Globals.voxelTextureAtlas.getVoxelTypeOffset((int)samplerVec.x);
            textureSamplers[i*3+1] = (float)Globals.voxelTextureAtlas.getVoxelTypeOffset((int)samplerVec.y);
            textureSamplers[i*3+2] = (float)Globals.voxelTextureAtlas.getVoxelTypeOffset((int)samplerVec.z);
            i++;
        }
        if(i * 3 < textureSamplers.length){
            throw new Error("Did not add all elements! " + samplerTriangles.size() + " " + textureSamplers.length);
        }
        
        //set ratio dat
        for(int j = 0; j < textureRatioData.length / 9; j++){
            //first vertex
            textureRatioData[j*9+0] = 1.0f;
            textureRatioData[j*9+1] = 0.0f;
            textureRatioData[j*9+2] = 0.0f;

            //second vertex
            textureRatioData[j*9+3] = 0.0f;
            textureRatioData[j*9+4] = 1.0f;
            textureRatioData[j*9+5] = 0.0f;

            //third vertex
            textureRatioData[j*9+6] = 0.0f;
            textureRatioData[j*9+7] = 0.0f;
            textureRatioData[j*9+8] = 1.0f;
        }

        //List<Float> vertices, List<Float> normals, List<Integer> faceElements, List<Float> uvs
        TerrainChunkData rVal = new TerrainChunkData(vertsFlat, normalsFlat, elementsFlat, UVs, textureSamplers, textureRatioData, chunkData.levelOfDetail);
        return rVal;
    }


    /**
     * Generates a mesh based on a terrainchunkdata object
     * @param data The terrain chunk data object
     * @return The mesh
     */
    protected static Mesh generateTerrainMesh(OpenGLState openGLState, TerrainChunkData data){
        
        Mesh mesh = new Mesh("terrainChunk");


        //
        //  VAO
        //
        mesh.generateVAO(openGLState);
        
        
        
        
        FloatBuffer vertexArrayBufferData = data.getVertexArrayBufferData();
        FloatBuffer normalArrayBufferData = data.getNormalArrayBufferData();
        FloatBuffer textureArrayBufferData = data.getTextureArrayBufferData();
        IntBuffer elementArrayBufferData = data.getElementArrayBufferData();
        FloatBuffer samplerBuffer = data.getSamplerBuffer();
        FloatBuffer ratioBuffer = data.getRatioBuffer();

        

        
        //
        //  Buffer data to GPU
        //
        int elementCount = data.getFaceElements().length;
        if(elementCount < 1){
            throw new Error("Invalid mesh data!");
        }
        try {

            //actually buffer vertices
            if(vertexArrayBufferData.position() > 0){
                vertexArrayBufferData.flip();
                mesh.bufferVertices(vertexArrayBufferData, 3);
            }

            //actually buffer normals
            if(normalArrayBufferData.position() > 0){
                normalArrayBufferData.flip();
                mesh.bufferNormals(normalArrayBufferData, 3);
            }

            //actually buffer UVs
            if(textureArrayBufferData.position() > 0){
                textureArrayBufferData.flip();
                mesh.bufferTextureCoords(textureArrayBufferData, 2);
            }

            //buffer element indices
            if(elementArrayBufferData.position() > 0){
                elementArrayBufferData.flip();
                mesh.bufferFaces(elementArrayBufferData, elementCount);
            }


        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
        
        

        //
        //   SAMPLER INDICES
        //
        try {
            if(samplerBuffer.position() > 0){
                samplerBuffer.flip();
                mesh.bufferCustomFloatAttribArray(samplerBuffer, 3, SAMPLER_INDEX_ATTRIB_LOC);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }

        //
        //   SAMPLER RATIO DATA
        //
        try {
            if(ratioBuffer.position() > 0){
                ratioBuffer.flip();
                mesh.bufferCustomFloatAttribArray(ratioBuffer, 3, SAMPLER_RATIO_ATTRIB_LOC);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }




        //bounding sphere logic
        int distance = ServerTerrainChunk.CHUNK_DIMENSION / 2 * (int)Math.pow(2,data.getLOD());
        mesh.updateBoundingSphere(
            distance,
            distance,
            distance,
            (float)Math.sqrt(
                distance * distance +
                distance * distance +
                distance * distance
            ));
        
        
        
        openGLState.glBindVertexArray(0);
        return mesh;
    }


    /**
     * Generates a model based on a terrainchunkdata object
     * @param data The terrain chunk data object
     * @param atlas The atlas texture for the chunk
     * @return The model
     */
    public static Model generateTerrainModel(TerrainChunkData data, VoxelTextureAtlas atlas){
        Model rVal = new Model();
        Mesh m = TransvoxelModelGeneration.generateTerrainMesh(Globals.renderingEngine.getOpenGLState(), data);
        
        //construct the material for the chunk
        Material groundMat = Material.create(atlas.getSpecular(), atlas.getNormal());
        m.setMaterial(groundMat);

        //shader logic
        m.setShader(Globals.terrainShaderProgram);
        
        rVal.addMesh(m);

        return rVal;
    }







    /**
     * Contains data required to generate a transvoxel mesh
     */
    public static class TransvoxelChunkData {

        /**
        
                    5             6
                    +-------------+               +-----5-------+     ^ Y                
                  / |           / |             / |            /|     |    _             
                /   |         /   |           4   9         6   10    |    /\  Z         
            4 +-----+-------+  7  |         +-----+7------+     |     |   /              
              |   1 +-------+-----+ 2       |     +-----1-+-----+     |  /               
              |   /         |   /           8   0        11   2       | /                
              | /           | /             | /           | /         |/                 
            0 +-------------+ 3             +------3------+           +---------------> X


        */

        /**
        The assumption is that the edge arrays are laid out perfectly adjacent
        ie


       +---------------+---------------+
       |               |               |
       |               |               |
       |   higher res  |   higher res  |
       |    chunk 1    |    chunk 2    |
       |               |               |
       |               |               |
       |               |               |
       +---------------+---------------+
       |               |               |
       |               |               |
       |   higher res  |   higher res  |
       |    chunk 3    |    chunk 4    |
       |               |               |
       |               |               |
       |               |               |
       +---------------+---------------+


         */

        float[][] xPositiveEdgeIso = null; //full-resolution values for the x-positive edge if it is twice the resolution of the core mesh
        int[][] xPositiveEdgeAtlas = null;
        float[][] xNegativeEdgeIso = null; //full-resolution values for the x-negative edge if it is twice the resolution of the core mesh
        int[][] xNegativeEdgeAtlas = null;
        float[][] yPositiveEdgeIso = null; //full-resolution values for the y-positive edge if it is twice the resolution of the core mesh
        int[][] yPositiveEdgeAtlas = null;
        float[][] yNegativeEdgeIso = null; //full-resolution values for the y-negative edge if it is twice the resolution of the core mesh
        int[][] yNegativeEdgeAtlas = null;
        float[][] zPositiveEdgeIso = null; //full-resolution values for the z-positive edge if it is twice the resolution of the core mesh
        int[][] zPositiveEdgeAtlas = null;
        float[][] zNegativeEdgeIso = null; //full-resolution values for the z-negative edge if it is twice the resolution of the core mesh
        int[][] zNegativeEdgeAtlas = null;

        //the core voxel data for the main part of the mesh
        public float[][][] terrainGrid;

        //the core texture data for the main part of the mesh
        public int[][][] textureGrid;

        int levelOfDetail;

        /**
         * Constructor -- please note that the edge arrays do not need to be defined
         * They are left as null if there is not a higher resolution edge to this chunk
         */
        public TransvoxelChunkData(
            float[][][] terrainGrid,
            int[][][] textureGrid,
            int levelOfDetail
        ){
            this.terrainGrid = terrainGrid;
            this.textureGrid = textureGrid;
            this.levelOfDetail = levelOfDetail;
        }


        /**
         * Adds values for the face along positive x
         * @param isoValues the iso values
         * @param atlasValues the atlas values
         */
        public void addXPositiveEdge(float[][] isoValues, int[][] atlasValues){
            this.xPositiveEdgeIso = isoValues;
            this.xPositiveEdgeAtlas = atlasValues;
        }

        /**
         * Adds values for the face along negative x
         * @param isoValues the iso values
         * @param atlasValues the atlas values
         */
        public void addXNegativeEdge(float[][] isoValues, int[][] atlasValues){
            this.xNegativeEdgeIso = isoValues;
            this.xNegativeEdgeAtlas = atlasValues;
        }

        /**
         * Adds values for the face along positive y
         * @param isoValues the iso values
         * @param atlasValues the atlas values
         */
        public void addYPositiveEdge(float[][] isoValues, int[][] atlasValues){
            this.yPositiveEdgeIso = isoValues;
            this.yPositiveEdgeAtlas = atlasValues;
        }

        /**
         * Adds values for the face along negative y
         * @param isoValues the iso values
         * @param atlasValues the atlas values
         */
        public void addYNegativeEdge(float[][] isoValues, int[][] atlasValues){
            this.yNegativeEdgeIso = isoValues;
            this.yNegativeEdgeAtlas = atlasValues;
        }

        /**
         * Adds values for the face along positive z
         * @param isoValues the iso values
         * @param atlasValues the atlas values
         */
        public void addZPositiveEdge(float[][] isoValues, int[][] atlasValues){
            this.zPositiveEdgeIso = isoValues;
            this.zPositiveEdgeAtlas = atlasValues;
        }

        /**
         * Adds values for the face along negative z
         * @param isoValues the iso values
         * @param atlasValues the atlas values
         */
        public void addZNegativeEdge(float[][] isoValues, int[][] atlasValues){
            this.zNegativeEdgeIso = isoValues;
            this.zNegativeEdgeAtlas = atlasValues;
        }



    }



































    













    //The lookup table that maps iso value to the index to lookup for texture
    static final int[][] sampleIndexTable = new int[][]{
        {0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3},
        {1, 2, 3, 0, 5, 6, 7, 4, 4, 5, 6, 7},
    };











    // The transitionCellClass table maps a 9-bit transition cell case index to an equivalence
    // class index. Even though there are 73 equivalence classes in the Transvoxel Algorithm,
    // several of them use the same exact triangulations, just with different vertex locations.
    // We combined those classes for this table so that the class index ranges from 0 to 55.
    // The high bit is set in the cases for which the inverse state of the voxel data maps to
    // the equivalence class, meaning that the winding order of each triangle should be reversed.
    static final short transitionCellClass[] = new short[]{
        0x00, 0x01, 0x02, 0x84, 0x01, 0x05, 0x04, 0x04, 0x02, 0x87, 0x09, 0x8C, 0x84, 0x0B, 0x05, 0x05,
        0x01, 0x08, 0x07, 0x8D, 0x05, 0x0F, 0x8B, 0x0B, 0x04, 0x0D, 0x0C, 0x1C, 0x04, 0x8B, 0x85, 0x85,
        0x02, 0x07, 0x09, 0x8C, 0x87, 0x10, 0x0C, 0x0C, 0x09, 0x12, 0x15, 0x9A, 0x8C, 0x19, 0x90, 0x10,
        0x84, 0x8D, 0x8C, 0x9C, 0x0B, 0x9D, 0x0F, 0x0F, 0x05, 0x1B, 0x10, 0xAC, 0x05, 0x0F, 0x8B, 0x0B,
        0x01, 0x05, 0x87, 0x0B, 0x08, 0x0F, 0x0D, 0x8B, 0x07, 0x10, 0x12, 0x19, 0x8D, 0x9D, 0x1B, 0x0F,
        0x05, 0x0F, 0x10, 0x9D, 0x0F, 0x1E, 0x1D, 0xA1, 0x8B, 0x1D, 0x99, 0x32, 0x0B, 0xA1, 0x8F, 0x94,
        0x04, 0x8B, 0x0C, 0x0F, 0x0D, 0x1D, 0x1C, 0x8F, 0x0C, 0x99, 0x1A, 0x31, 0x1C, 0x32, 0x2C, 0xA7,
        0x04, 0x0B, 0x0C, 0x0F, 0x8B, 0xA1, 0x8F, 0x96, 0x85, 0x8F, 0x90, 0x27, 0x85, 0x94, 0x8B, 0x8A,
        0x02, 0x04, 0x09, 0x05, 0x07, 0x8B, 0x0C, 0x85, 0x09, 0x0C, 0x15, 0x90, 0x8C, 0x0F, 0x10, 0x8B,
        0x87, 0x0D, 0x12, 0x1B, 0x10, 0x1D, 0x99, 0x8F, 0x0C, 0x1C, 0x1A, 0x2C, 0x0C, 0x8F, 0x90, 0x8B,
        0x09, 0x0C, 0x15, 0x10, 0x12, 0x99, 0x1A, 0x90, 0x15, 0x1A, 0x23, 0x30, 0x9A, 0x31, 0x30, 0x19,
        0x8C, 0x1C, 0x9A, 0xAC, 0x19, 0x32, 0x31, 0x27, 0x90, 0x2C, 0x30, 0x29, 0x10, 0xA7, 0x19, 0x24,
        0x84, 0x04, 0x8C, 0x05, 0x8D, 0x0B, 0x1C, 0x85, 0x8C, 0x0C, 0x9A, 0x10, 0x9C, 0x0F, 0xAC, 0x0B,
        0x0B, 0x8B, 0x19, 0x0F, 0x9D, 0xA1, 0x32, 0x94, 0x0F, 0x8F, 0x31, 0xA7, 0x0F, 0x96, 0x27, 0x8A,
        0x05, 0x85, 0x90, 0x8B, 0x1B, 0x8F, 0x2C, 0x8B, 0x10, 0x90, 0x30, 0x19, 0xAC, 0x27, 0x29, 0x24,
        0x05, 0x85, 0x10, 0x0B, 0x0F, 0x94, 0xA7, 0x8A, 0x8B, 0x8B, 0x19, 0x24, 0x0B, 0x8A, 0x24, 0x83,
        0x03, 0x06, 0x0A, 0x8B, 0x06, 0x0E, 0x0B, 0x0B, 0x0A, 0x91, 0x14, 0x8F, 0x8B, 0x17, 0x05, 0x85,
        0x06, 0x13, 0x11, 0x98, 0x0E, 0x1F, 0x97, 0x2B, 0x0B, 0x18, 0x0F, 0x36, 0x0B, 0xAB, 0x05, 0x85,
        0x0A, 0x11, 0x16, 0x8F, 0x91, 0x20, 0x0F, 0x8F, 0x14, 0x22, 0x21, 0x1D, 0x8F, 0x2D, 0x0B, 0x8B,
        0x8B, 0x98, 0x8F, 0xB7, 0x17, 0xAE, 0x8C, 0x0C, 0x05, 0x2F, 0x8B, 0xB5, 0x85, 0xA6, 0x84, 0x04,
        0x06, 0x0E, 0x91, 0x17, 0x13, 0x1F, 0x18, 0xAB, 0x11, 0x20, 0x22, 0x2D, 0x98, 0xAE, 0x2F, 0xA6,
        0x0E, 0x1F, 0x20, 0xAE, 0x1F, 0x33, 0x2E, 0x2A, 0x97, 0x2E, 0xAD, 0x28, 0x2B, 0x2A, 0x26, 0x25,
        0x0B, 0x97, 0x0F, 0x8C, 0x18, 0x2E, 0x37, 0x8C, 0x0F, 0xAD, 0x9D, 0x90, 0x36, 0x28, 0x35, 0x07,
        0x0B, 0x2B, 0x8F, 0x0C, 0xAB, 0x2A, 0x8C, 0x89, 0x05, 0x26, 0x0B, 0x87, 0x85, 0x25, 0x84, 0x82,
        0x0A, 0x0B, 0x14, 0x05, 0x11, 0x97, 0x0F, 0x05, 0x16, 0x0F, 0x21, 0x0B, 0x8F, 0x8C, 0x8B, 0x84,
        0x91, 0x18, 0x22, 0x2F, 0x20, 0x2E, 0xAD, 0x26, 0x0F, 0x37, 0x9D, 0x35, 0x8F, 0x8C, 0x0B, 0x84,
        0x14, 0x0F, 0x21, 0x8B, 0x22, 0xAD, 0x9D, 0x0B, 0x21, 0x9D, 0x9E, 0x8F, 0x1D, 0x90, 0x8F, 0x85,
        0x8F, 0x36, 0x1D, 0xB5, 0x2D, 0x28, 0x90, 0x87, 0x0B, 0x35, 0x8F, 0x34, 0x8B, 0x07, 0x85, 0x81,
        0x8B, 0x0B, 0x8F, 0x85, 0x98, 0x2B, 0x36, 0x85, 0x8F, 0x8F, 0x1D, 0x8B, 0xB7, 0x0C, 0xB5, 0x04,
        0x17, 0xAB, 0x2D, 0xA6, 0xAE, 0x2A, 0x28, 0x25, 0x8C, 0x8C, 0x90, 0x07, 0x0C, 0x89, 0x87, 0x82,
        0x05, 0x05, 0x0B, 0x84, 0x2F, 0x26, 0x35, 0x84, 0x8B, 0x0B, 0x8F, 0x85, 0xB5, 0x87, 0x34, 0x81,
        0x85, 0x85, 0x8B, 0x04, 0xA6, 0x25, 0x07, 0x82, 0x84, 0x84, 0x85, 0x81, 0x04, 0x82, 0x81, 0x80
    };


    // The transitionCellData table holds the triangulation data for all 56 distinct classes to
    // which a case can be mapped by the transitionCellClass table. The class index should be ANDed
    // with 0x7F before using it to look up triangulation data in this table.
    static final TransitionCellData[] transitionCellData = new TransitionCellData[]{
        new TransitionCellData((char)0x00, new char[]{}),
        new TransitionCellData((char)0x42, new char[]{0, 1, 3, 1, 2, 3}),
        new TransitionCellData((char)0x31, new char[]{0, 1, 2}),
        new TransitionCellData((char)0x42, new char[]{0, 1, 2, 0, 2, 3}),
        new TransitionCellData((char)0x53, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3}),
        new TransitionCellData((char)0x64, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4}),
        new TransitionCellData((char)0x84, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 6, 4, 6, 7}),
        new TransitionCellData((char)0x73, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 6}),
        new TransitionCellData((char)0x84, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 7, 5, 6, 7}),
        new TransitionCellData((char)0x62, new char[]{0, 1, 2, 3, 4, 5}),
        new TransitionCellData((char)0x53, new char[]{0, 1, 3, 0, 3, 4, 1, 2, 3}),
        new TransitionCellData((char)0x75, new char[]{0, 1, 6, 1, 2, 6, 2, 5, 6, 2, 3, 5, 3, 4, 5}),
        new TransitionCellData((char)0x84, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3, 5, 6, 7}),
        new TransitionCellData((char)0x95, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3, 5, 6, 8, 6, 7, 8}),
        new TransitionCellData((char)0xA6, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4, 6, 7, 8, 6, 8, 9}),
        new TransitionCellData((char)0x86, new char[]{0, 1, 7, 1, 2, 7, 2, 3, 7, 3, 6, 7, 3, 4, 6, 4, 5, 6}),
        new TransitionCellData((char)0x95, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4, 6, 7, 8}),
        new TransitionCellData((char)0x95, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 7, 4, 7, 8, 5, 6, 7}),
        new TransitionCellData((char)0xA4, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9}),
        new TransitionCellData((char)0xC6, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 7, 5, 6, 7, 8, 9, 10, 8, 10, 11}),
        new TransitionCellData((char)0x64, new char[]{0, 1, 3, 1, 2, 3, 0, 3, 4, 0, 4, 5}),
        new TransitionCellData((char)0x93, new char[]{0, 1, 2, 3, 4, 5, 6, 7, 8}),
        new TransitionCellData((char)0x64, new char[]{0, 1, 4, 0, 4, 5, 1, 3, 4, 1, 2, 3}),
        new TransitionCellData((char)0x97, new char[]{0, 1, 8, 1, 7, 8, 1, 2, 7, 2, 3, 7, 3, 4, 7, 4, 5, 7, 5, 6, 7}),
        new TransitionCellData((char)0xB7, new char[]{0, 1, 6, 1, 2, 6, 2, 5, 6, 2, 3, 5, 3, 4, 5, 7, 8, 10, 8, 9, 10}),
        new TransitionCellData((char)0xA6, new char[]{0, 1, 6, 1, 2, 6, 2, 5, 6, 2, 3, 5, 3, 4, 5, 7, 8, 9}),
        new TransitionCellData((char)0xB5, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3, 5, 6, 7, 8, 9, 10}),
        new TransitionCellData((char)0xA6, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4, 6, 7, 9, 7, 8, 9}),
        new TransitionCellData((char)0xA6, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3, 5, 6, 9, 6, 8, 9, 6, 7, 8}),
        new TransitionCellData((char)0x97, new char[]{0, 1, 8, 1, 2, 8, 2, 3, 8, 3, 7, 8, 3, 4, 7, 4, 5, 7, 5, 6, 7}),
        new TransitionCellData((char)0x86, new char[]{0, 1, 7, 1, 6, 7, 1, 2, 6, 2, 5, 6, 2, 4, 5, 2, 3, 4}),
        new TransitionCellData((char)0xC8, new char[]{0, 1, 7, 1, 2, 7, 2, 3, 7, 3, 6, 7, 3, 4, 6, 4, 5, 6, 8, 9, 10, 8, 10, 11}),
        new TransitionCellData((char)0xB7, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4, 6, 9, 10, 6, 7, 9, 7, 8, 9}),
        new TransitionCellData((char)0x75, new char[]{0, 1, 6, 1, 3, 6, 1, 2, 3, 3, 4, 6, 4, 5, 6}),
        new TransitionCellData((char)0xA6, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 9, 5, 8, 9, 5, 6, 8, 6, 7, 8}),
        new TransitionCellData((char)0xC4, new char[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}),
        new TransitionCellData((char)0x86, new char[]{1, 2, 4, 2, 3, 4, 0, 1, 7, 1, 4, 7, 4, 6, 7, 4, 5, 6}),
        new TransitionCellData((char)0x64, new char[]{0, 4, 5, 0, 1, 4, 1, 3, 4, 1, 2, 3}),
        new TransitionCellData((char)0x86, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3, 0, 4, 7, 4, 6, 7, 4, 5, 6}),
        new TransitionCellData((char)0x97, new char[]{1, 2, 3, 1, 3, 4, 1, 4, 5, 0, 1, 8, 1, 5, 8, 5, 7, 8, 5, 6, 7}),
        new TransitionCellData((char)0xA6, new char[]{0, 1, 3, 1, 2, 3, 4, 5, 9, 5, 8, 9, 5, 6, 8, 6, 7, 8}),
        new TransitionCellData((char)0xC8, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4, 6, 7, 11, 7, 10, 11, 7, 8, 10, 8, 9, 10}),
        new TransitionCellData((char)0x97, new char[]{0, 1, 8, 1, 2, 8, 2, 7, 8, 2, 3, 7, 3, 6, 7, 3, 4, 6, 4, 5, 6}),
        new TransitionCellData((char)0x97, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3, 0, 4, 8, 4, 7, 8, 4, 5, 7, 5, 6, 7}),
        new TransitionCellData((char)0xB7, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4, 6, 7, 10, 7, 9, 10, 7, 8, 9}),
        new TransitionCellData((char)0xA8, new char[]{0, 1, 9, 1, 2, 9, 2, 8, 9, 2, 3, 8, 3, 7, 8, 3, 4, 7, 4, 6, 7, 4, 5, 6}),
        new TransitionCellData((char)0xB9, new char[]{0, 1, 7, 1, 6, 7, 1, 2, 6, 2, 5, 6, 2, 3, 5, 3, 4, 5, 0, 7, 10, 7, 9, 10, 7, 8, 9}),
        new TransitionCellData((char)0xA6, new char[]{0, 1, 5, 1, 4, 5, 1, 2, 4, 2, 3, 4, 6, 7, 9, 7, 8, 9}),
        new TransitionCellData((char)0xC6, new char[]{0, 1, 5, 1, 2, 5, 2, 4, 5, 2, 3, 4, 6, 7, 8, 9, 10, 11}),
        new TransitionCellData((char)0xB7, new char[]{0, 1, 7, 1, 2, 7, 2, 3, 7, 3, 6, 7, 3, 4, 6, 4, 5, 6, 8, 9, 10}),
        new TransitionCellData((char)0xA8, new char[]{1, 2, 3, 1, 3, 4, 1, 4, 6, 4, 5, 6, 0, 1, 9, 1, 6, 9, 6, 8, 9, 6, 7, 8}),
        new TransitionCellData((char)0xCC, new char[]{0, 1, 9, 1, 8, 9, 1, 2, 8, 2, 11, 8, 2, 3, 11, 3, 4, 11, 4, 5, 11, 5, 10, 11, 5, 6, 10, 6, 9, 10, 6, 7, 9, 7, 0, 9}),
        new TransitionCellData((char)0x86, new char[]{0, 1, 2, 0, 2, 3, 0, 6, 7, 0, 3, 6, 1, 4, 5, 1, 5, 2}),
        new TransitionCellData((char)0x97, new char[]{0, 1, 4, 1, 3, 4, 1, 2, 3, 2, 5, 6, 2, 6, 3, 0, 7, 8, 0, 4, 7}),
        new TransitionCellData((char)0xA8, new char[]{0, 1, 5, 1, 4, 5, 1, 2, 4, 2, 3, 4, 3, 6, 7, 3, 7, 4, 0, 8, 9, 0, 5, 8}),
        new TransitionCellData((char)0xA8, new char[]{0, 1, 5, 1, 4, 5, 1, 2, 4, 2, 3, 4, 2, 6, 3, 3, 6, 7, 0, 8, 9, 0, 5, 8}),
    };

    // The transitionCornerData table contains the transition cell corner reuse data
    // shown in Figure 4.19
    static final char[] transitionCornerData = new char[]{
        0x30, 0x21, 0x20, 0x12, 0x40, 0x82, 0x10, 0x81, 0x80, 0x37, 0x27, 0x17, 0x87
    };


    // The transitionVertexData table gives the vertex locations for every one of the 512 possible
    // cases in the Tranvoxel Algorithm. Each 16-bit value also provides information about whether
    // a vertex can be reused from a neighboring cell. See Section 4.5 for details. The low byte
    // contains the indexes for the two endpoints of the edge on which the vertex lies, as numbered
    // in Figure 4.16. The high byte contains the vertex reuse data shown in Figure 4.17.
    static final int transitionVertexData[][] = new int[][]{
        {},
        {0x2301, 0x1503, 0x199B, 0x289A},
        {0x2301, 0x2412, 0x4514},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B},
        {0x8525, 0x2412, 0x289A, 0x89AC},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC},
        {0x8525, 0x4514, 0x1503, 0x199B, 0x89AC},
        {0x8525, 0x8658, 0x4445},
        {0x1503, 0x2301, 0x289A, 0x199B, 0x8658, 0x8525, 0x4445},
        {0x8525, 0x8658, 0x4445, 0x2301, 0x2412, 0x4514},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B, 0x8658, 0x8525, 0x4445},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC},
        {0x8658, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC},
        {0x8658, 0x4445, 0x4514, 0x1503, 0x199B, 0x89AC},
        {0x8478, 0x8658, 0x89AC, 0x88BC},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x8478, 0x8658, 0x89AC, 0x88BC},
        {0x8478, 0x8658, 0x89AC, 0x88BC, 0x2301, 0x2412, 0x4514},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B, 0x8658, 0x8478, 0x88BC, 0x89AC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x88BC},
        {0x2301, 0x4514, 0x8525, 0x8658, 0x8478, 0x88BC, 0x289A},
        {0x8478, 0x8658, 0x8525, 0x4514, 0x1503, 0x199B, 0x88BC},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x1503, 0x199B, 0x289A},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x2412, 0x4514},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2412, 0x4514, 0x1503, 0x199B, 0x289A},
        {0x8478, 0x4445, 0x2412, 0x289A, 0x88BC},
        {0x1503, 0x2301, 0x2412, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x2301, 0x4514, 0x4445, 0x8478, 0x88BC, 0x289A},
        {0x1503, 0x4514, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x8367, 0x4647},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x8478, 0x8367, 0x4647},
        {0x2301, 0x2412, 0x4514, 0x8478, 0x8367, 0x4647},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B, 0x8367, 0x8478, 0x4647},
        {0x2412, 0x8525, 0x89AC, 0x289A, 0x8367, 0x8478, 0x4647},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8525, 0x4514, 0x1503, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8478, 0x8367, 0x4647, 0x8525, 0x8658, 0x4445},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x8478, 0x8367, 0x4647, 0x8525, 0x8658, 0x4445},
        {0x8478, 0x8367, 0x4647, 0x8525, 0x8658, 0x4445, 0x2301, 0x2412, 0x4514},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B, 0x8658, 0x8525, 0x4445, 0x8367, 0x8478, 0x4647},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A, 0x8367, 0x8478, 0x4647},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x2301, 0x4514, 0x4445, 0x8658, 0x89AC, 0x289A, 0x8367, 0x8478, 0x4647},
        {0x8658, 0x4445, 0x4514, 0x1503, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC},
        {0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC, 0x1503, 0x2301, 0x289A, 0x199B},
        {0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC, 0x2412, 0x2301, 0x4514},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B, 0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC},
        {0x1503, 0x2301, 0x2412, 0x8525, 0x8658, 0x4647, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x4514, 0x2301, 0x289A, 0x88BC},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x4514, 0x1503, 0x199B, 0x88BC},
        {0x8367, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC},
        {0x8367, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x1503, 0x199B, 0x289A},
        {0x8367, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x2412, 0x4514},
        {0x8525, 0x4445, 0x4647, 0x8367, 0x88BC, 0x89AC, 0x1503, 0x4514, 0x2412, 0x289A, 0x199B},
        {0x8367, 0x4647, 0x4445, 0x2412, 0x289A, 0x88BC},
        {0x8367, 0x4647, 0x4445, 0x2412, 0x2301, 0x1503, 0x199B, 0x88BC},
        {0x2301, 0x4514, 0x4445, 0x4647, 0x8367, 0x88BC, 0x289A},
        {0x8367, 0x4647, 0x4445, 0x4514, 0x1503, 0x199B, 0x88BC},
        {0x1636, 0x8367, 0x88BC, 0x199B},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A},
        {0x8367, 0x1636, 0x199B, 0x88BC, 0x2412, 0x2301, 0x4514},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x1636, 0x8367, 0x88BC, 0x199B},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x89AC},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x1636, 0x1503, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x1636, 0x8367, 0x88BC, 0x199B, 0x8525, 0x8658, 0x4445},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x1636, 0x8367, 0x88BC, 0x199B, 0x8525, 0x8658, 0x4445, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A, 0x8367, 0x1636, 0x199B, 0x88BC},
        {0x8367, 0x1636, 0x1503, 0x2301, 0x2412, 0x4445, 0x8658, 0x89AC, 0x88BC},
        {0x8658, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x8367, 0x88BC, 0x199B},
        {0x8658, 0x4445, 0x4514, 0x1503, 0x1636, 0x8367, 0x88BC, 0x89AC},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x289A},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x8658, 0x8478, 0x8367, 0x1636, 0x1503, 0x4514, 0x2412, 0x289A, 0x89AC},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x199B},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x1503},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x4514, 0x2301, 0x289A, 0x199B},
        {0x1503, 0x4514, 0x8525, 0x8658, 0x8478, 0x8367, 0x1636},
        {0x8525, 0x4445, 0x8478, 0x8367, 0x1636, 0x199B, 0x89AC},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x4445, 0x8525, 0x89AC, 0x289A},
        {0x8525, 0x4445, 0x8478, 0x8367, 0x1636, 0x199B, 0x89AC, 0x2412, 0x2301, 0x4514},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x8367, 0x8478, 0x4445, 0x8525, 0x89AC, 0x289A},
        {0x1636, 0x8367, 0x8478, 0x4445, 0x2412, 0x289A, 0x199B},
        {0x2412, 0x4445, 0x8478, 0x8367, 0x1636, 0x1503, 0x2301},
        {0x2301, 0x4514, 0x4445, 0x8478, 0x8367, 0x1636, 0x199B, 0x289A},
        {0x8367, 0x1636, 0x1503, 0x4514, 0x4445, 0x8478},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4647, 0x1636, 0x1503, 0x2301, 0x289A, 0x88BC},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x4647, 0x8478, 0x88BC, 0x289A},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8525, 0x2412, 0x289A, 0x89AC},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x1636, 0x4647, 0x8478, 0x88BC, 0x89AC},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4647, 0x1636, 0x1503, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8525, 0x8658, 0x4445},
        {0x8478, 0x4647, 0x1636, 0x1503, 0x2301, 0x289A, 0x88BC, 0x8658, 0x8525, 0x4445},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8525, 0x8658, 0x4445, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x4647, 0x8478, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8658, 0x4445, 0x2412, 0x289A, 0x89AC},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x1503, 0x1636, 0x4647, 0x8478, 0x88BC, 0x89AC},
        {0x8658, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4647, 0x1636, 0x1503, 0x4514, 0x4445, 0x8658, 0x89AC, 0x88BC},
        {0x1636, 0x4647, 0x8658, 0x89AC, 0x199B},
        {0x2301, 0x1503, 0x1636, 0x4647, 0x8658, 0x89AC, 0x289A},
        {0x1636, 0x4647, 0x8658, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x4647, 0x8658, 0x89AC, 0x289A},
        {0x2412, 0x8525, 0x8658, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x8658, 0x4647, 0x1636, 0x1503, 0x2301, 0x2412, 0x8525},
        {0x2301, 0x4514, 0x8525, 0x8658, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x1503, 0x4514, 0x8525, 0x8658, 0x4647, 0x1636},
        {0x8525, 0x4445, 0x4647, 0x1636, 0x199B, 0x89AC},
        {0x8525, 0x4445, 0x4647, 0x1636, 0x1503, 0x2301, 0x289A, 0x89AC},
        {0x8525, 0x4445, 0x4647, 0x1636, 0x199B, 0x89AC, 0x2412, 0x2301, 0x4514},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x4647, 0x4445, 0x8525, 0x89AC, 0x289A},
        {0x2412, 0x4445, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x1503, 0x2301, 0x2412, 0x4445, 0x4647, 0x1636},
        {0x2301, 0x4514, 0x4445, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x1503, 0x4514, 0x4445, 0x4647, 0x1636},
        {0x1636, 0x1503, 0x4334},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A},
        {0x2301, 0x2412, 0x4514, 0x1636, 0x1503, 0x4334},
        {0x2412, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x2301, 0x2412, 0x8525, 0x89AC, 0x199B},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x8525, 0x89AC, 0x199B},
        {0x1636, 0x1503, 0x4334, 0x8525, 0x8658, 0x4445},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x8525, 0x8658, 0x4445, 0x2301, 0x2412, 0x4514, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x2412, 0x289A, 0x199B, 0x8658, 0x8525, 0x4445},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A, 0x1503, 0x1636, 0x4334},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x4334, 0x1636, 0x199B, 0x89AC},
        {0x8658, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x4445, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x8478, 0x88BC, 0x89AC, 0x1503, 0x1636, 0x4334},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x8658, 0x89AC, 0x88BC},
        {0x8478, 0x8658, 0x89AC, 0x88BC, 0x2301, 0x2412, 0x4514, 0x1636, 0x1503, 0x4334},
        {0x2412, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x8658, 0x89AC, 0x88BC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x4334, 0x1636, 0x199B, 0x88BC},
        {0x2301, 0x4514, 0x8525, 0x8658, 0x8478, 0x88BC, 0x289A, 0x1503, 0x1636, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x8525, 0x8658, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x2412, 0x4514, 0x1636, 0x1503, 0x4334},
        {0x2412, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC},
        {0x8478, 0x4445, 0x2412, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x2301, 0x2412, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x2301, 0x4514, 0x4445, 0x8478, 0x88BC, 0x289A, 0x1503, 0x1636, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x1636, 0x1503, 0x4334, 0x8478, 0x8367, 0x4647},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x8367, 0x4647},
        {0x2301, 0x2412, 0x4514, 0x1636, 0x1503, 0x4334, 0x8478, 0x8367, 0x4647},
        {0x2412, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x8367, 0x4647},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x1636, 0x1503, 0x4334, 0x8478, 0x8367, 0x4647},
        {0x1636, 0x4334, 0x2301, 0x2412, 0x8525, 0x89AC, 0x199B, 0x8367, 0x8478, 0x4647},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x1503, 0x4334, 0x8478, 0x8367, 0x4647},
        {0x1636, 0x4334, 0x4514, 0x8525, 0x89AC, 0x199B, 0x8367, 0x8478, 0x4647},
        {0x1636, 0x1503, 0x4334, 0x8478, 0x8367, 0x4647, 0x8525, 0x8658, 0x4445},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x8367, 0x4647, 0x8525, 0x8658, 0x4445},
        {0x2301, 0x2412, 0x4514, 0x1636, 0x1503, 0x4334, 0x8478, 0x8367, 0x4647, 0x8525, 0x8658, 0x4445},
        {0x2412, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A, 0x8478, 0x8367, 0x4647, 0x8525, 0x8658, 0x4445},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A, 0x8367, 0x8478, 0x4647, 0x1503, 0x1636, 0x4334},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x4334, 0x1636, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8658, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1636, 0x1503, 0x4334, 0x8478, 0x8367, 0x4647},
        {0x8658, 0x4445, 0x4514, 0x4334, 0x1636, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC, 0x1503, 0x1636, 0x4334},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A, 0x8367, 0x4647, 0x8658, 0x89AC, 0x88BC},
        {0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC, 0x1503, 0x1636, 0x4334, 0x2412, 0x2301, 0x4514},
        {0x1636, 0x4334, 0x4514, 0x2412, 0x289A, 0x199B, 0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x2412, 0x2301, 0x4334, 0x1636, 0x199B, 0x88BC},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x4514, 0x2301, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x4514, 0x4334, 0x1636, 0x199B, 0x88BC},
        {0x8525, 0x4445, 0x4647, 0x8367, 0x88BC, 0x89AC, 0x1503, 0x1636, 0x4334},
        {0x8367, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x4334, 0x1636, 0x199B, 0x289A},
        {0x8367, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x2412, 0x4514, 0x1636, 0x1503, 0x4334},
        {0x8367, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x2412, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A},
        {0x8367, 0x4647, 0x4445, 0x2412, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x2301, 0x2412, 0x4445, 0x4647, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x4647, 0x4445, 0x4514, 0x2301, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x8367, 0x4647, 0x4445, 0x4514, 0x4334, 0x1636, 0x199B, 0x88BC},
        {0x8367, 0x4334, 0x1503, 0x199B, 0x88BC},
        {0x2301, 0x4334, 0x8367, 0x88BC, 0x289A},
        {0x8367, 0x4334, 0x1503, 0x199B, 0x88BC, 0x2412, 0x2301, 0x4514},
        {0x2412, 0x4514, 0x4334, 0x8367, 0x88BC, 0x289A},
        {0x8367, 0x4334, 0x1503, 0x199B, 0x88BC, 0x2412, 0x8525, 0x89AC, 0x289A},
        {0x8525, 0x2412, 0x2301, 0x4334, 0x8367, 0x88BC, 0x89AC},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1503, 0x4334, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x4334, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x8367, 0x4334, 0x1503, 0x199B, 0x88BC, 0x8658, 0x8525, 0x4445},
        {0x2301, 0x4334, 0x8367, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x8367, 0x4334, 0x1503, 0x199B, 0x88BC, 0x2412, 0x2301, 0x4514, 0x8658, 0x8525, 0x4445},
        {0x2412, 0x4514, 0x4334, 0x8367, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A, 0x8367, 0x4334, 0x1503, 0x199B, 0x88BC},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x4334, 0x8367, 0x88BC, 0x89AC},
        {0x2301, 0x4514, 0x4445, 0x8658, 0x89AC, 0x289A, 0x8367, 0x4334, 0x1503, 0x199B, 0x88BC},
        {0x8658, 0x4445, 0x4514, 0x4334, 0x8367, 0x88BC, 0x89AC},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x8478, 0x8367, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x4334, 0x8367, 0x8478, 0x8658, 0x89AC, 0x289A},
        {0x2412, 0x8525, 0x8658, 0x8478, 0x8367, 0x4334, 0x1503, 0x199B, 0x289A},
        {0x8367, 0x4334, 0x2301, 0x2412, 0x8525, 0x8658, 0x8478},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x8658, 0x8525, 0x4514, 0x2301, 0x289A, 0x199B},
        {0x8658, 0x8478, 0x8367, 0x4334, 0x4514, 0x8525},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x4445, 0x8525, 0x89AC, 0x199B},
        {0x8525, 0x4445, 0x8478, 0x8367, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x4445, 0x8525, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x8525, 0x4445, 0x8478, 0x8367, 0x4334, 0x4514, 0x2412, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x4445, 0x2412, 0x289A, 0x199B},
        {0x8367, 0x4334, 0x2301, 0x2412, 0x4445, 0x8478},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x4445, 0x4514, 0x2301, 0x289A, 0x199B},
        {0x8367, 0x4334, 0x4514, 0x4445, 0x8478},
        {0x1503, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4647, 0x4334, 0x2301, 0x289A, 0x88BC},
        {0x8478, 0x4647, 0x4334, 0x1503, 0x199B, 0x88BC, 0x2412, 0x2301, 0x4514},
        {0x8478, 0x4647, 0x4334, 0x4514, 0x2412, 0x289A, 0x88BC},
        {0x1503, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8525, 0x2412, 0x289A, 0x89AC},
        {0x8478, 0x4647, 0x4334, 0x2301, 0x2412, 0x8525, 0x89AC, 0x88BC},
        {0x1503, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8525, 0x4514, 0x2301, 0x289A, 0x89AC},
        {0x8478, 0x4647, 0x4334, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x1503, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8525, 0x8658, 0x4445},
        {0x8478, 0x4647, 0x4334, 0x2301, 0x289A, 0x88BC, 0x8658, 0x8525, 0x4445},
        {0x1503, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B, 0x8525, 0x8658, 0x4445, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x4334, 0x4647, 0x8478, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x8478, 0x4647, 0x4334, 0x1503, 0x199B, 0x88BC, 0x2412, 0x4445, 0x8658, 0x89AC, 0x289A},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x4334, 0x4647, 0x8478, 0x88BC, 0x89AC},
        {0x8658, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC, 0x1503, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8658, 0x4445, 0x4514, 0x4334, 0x4647, 0x8478, 0x88BC, 0x89AC},
        {0x1503, 0x4334, 0x4647, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x4647, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x4647, 0x8658, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x4334, 0x4647, 0x8658, 0x89AC, 0x289A},
        {0x1503, 0x4334, 0x4647, 0x8658, 0x8525, 0x2412, 0x289A, 0x199B},
        {0x2412, 0x8525, 0x8658, 0x4647, 0x4334, 0x2301},
        {0x2301, 0x4514, 0x8525, 0x8658, 0x4647, 0x4334, 0x1503, 0x199B, 0x289A},
        {0x8658, 0x4647, 0x4334, 0x4514, 0x8525},
        {0x8525, 0x4445, 0x4647, 0x4334, 0x1503, 0x199B, 0x89AC},
        {0x8525, 0x4445, 0x4647, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x4647, 0x4445, 0x8525, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x2412, 0x4514, 0x4334, 0x4647, 0x4445, 0x8525, 0x89AC, 0x289A},
        {0x1503, 0x4334, 0x4647, 0x4445, 0x2412, 0x289A, 0x199B},
        {0x2412, 0x4445, 0x4647, 0x4334, 0x2301},
        {0x1503, 0x4334, 0x4647, 0x4445, 0x4514, 0x2301, 0x289A, 0x199B},
        {0x4514, 0x4445, 0x4647, 0x4334},
        {0x4514, 0x4445, 0x4647, 0x4334},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x4334, 0x4514, 0x4445, 0x4647},
        {0x2412, 0x4445, 0x4647, 0x4334, 0x2301},
        {0x1503, 0x4334, 0x4647, 0x4445, 0x2412, 0x289A, 0x199B},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x4514, 0x4445, 0x4647, 0x4334},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC, 0x4514, 0x4445, 0x4647, 0x4334},
        {0x8525, 0x4445, 0x4647, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x8525, 0x4445, 0x4647, 0x4334, 0x1503, 0x199B, 0x89AC},
        {0x8658, 0x4647, 0x4334, 0x4514, 0x8525},
        {0x1503, 0x2301, 0x289A, 0x199B, 0x8525, 0x4514, 0x4334, 0x4647, 0x8658},
        {0x2412, 0x8525, 0x8658, 0x4647, 0x4334, 0x2301},
        {0x1503, 0x4334, 0x4647, 0x8658, 0x8525, 0x2412, 0x289A, 0x199B},
        {0x2412, 0x4514, 0x4334, 0x4647, 0x8658, 0x89AC, 0x289A},
        {0x8658, 0x4647, 0x4334, 0x4514, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC},
        {0x8658, 0x4647, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x4647, 0x8658, 0x89AC, 0x199B},
        {0x8478, 0x8658, 0x89AC, 0x88BC, 0x4445, 0x4647, 0x4334, 0x4514},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x8478, 0x8658, 0x89AC, 0x88BC, 0x4334, 0x4514, 0x4445, 0x4647},
        {0x8478, 0x8658, 0x89AC, 0x88BC, 0x2412, 0x4445, 0x4647, 0x4334, 0x2301},
        {0x1503, 0x4334, 0x4647, 0x4445, 0x2412, 0x289A, 0x199B, 0x8658, 0x8478, 0x88BC, 0x89AC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC, 0x4445, 0x4647, 0x4334, 0x4514},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x88BC, 0x4514, 0x4445, 0x4647, 0x4334},
        {0x2301, 0x4334, 0x4647, 0x4445, 0x8525, 0x8658, 0x8478, 0x88BC, 0x289A},
        {0x8478, 0x8658, 0x8525, 0x4445, 0x4647, 0x4334, 0x1503, 0x199B, 0x88BC},
        {0x8478, 0x4647, 0x4334, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x8478, 0x4647, 0x4334, 0x4514, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x1503, 0x199B, 0x289A},
        {0x8478, 0x4647, 0x4334, 0x2301, 0x2412, 0x8525, 0x89AC, 0x88BC},
        {0x8478, 0x4647, 0x4334, 0x1503, 0x2412, 0x8525, 0x199B, 0x289A, 0x89AC, 0x88BC},
        {0x8478, 0x4647, 0x4334, 0x4514, 0x2412, 0x289A, 0x88BC},
        {0x1503, 0x2301, 0x2412, 0x4514, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4647, 0x4334, 0x2301, 0x289A, 0x88BC},
        {0x1503, 0x4334, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8367, 0x4334, 0x4514, 0x4445, 0x8478},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x8367, 0x4334, 0x4514, 0x4445, 0x8478},
        {0x8367, 0x4334, 0x2301, 0x2412, 0x4445, 0x8478},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x4445, 0x2412, 0x289A, 0x199B},
        {0x2412, 0x8525, 0x89AC, 0x289A, 0x8478, 0x4445, 0x4514, 0x4334, 0x8367},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC, 0x8367, 0x4334, 0x4514, 0x4445, 0x8478},
        {0x8525, 0x4445, 0x8478, 0x8367, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x4445, 0x8525, 0x89AC, 0x199B},
        {0x8658, 0x8478, 0x8367, 0x4334, 0x4514, 0x8525},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x8367, 0x4334, 0x4514, 0x8525, 0x8658, 0x8478},
        {0x8367, 0x4334, 0x2301, 0x2412, 0x8525, 0x8658, 0x8478},
        {0x2412, 0x8525, 0x8658, 0x8478, 0x8367, 0x4334, 0x1503, 0x199B, 0x289A},
        {0x2412, 0x4514, 0x4334, 0x8367, 0x8478, 0x8658, 0x89AC, 0x289A},
        {0x8658, 0x8478, 0x8367, 0x4334, 0x4514, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC},
        {0x8658, 0x8478, 0x8367, 0x4334, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x4334, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x4445, 0x4514, 0x4334, 0x8367, 0x88BC, 0x89AC},
        {0x8658, 0x4445, 0x4514, 0x4334, 0x8367, 0x88BC, 0x89AC, 0x1503, 0x2301, 0x289A, 0x199B},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x4334, 0x8367, 0x88BC, 0x89AC},
        {0x8658, 0x4445, 0x2412, 0x1503, 0x4334, 0x8367, 0x289A, 0x199B, 0x88BC, 0x89AC},
        {0x8367, 0x4334, 0x4514, 0x4445, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC},
        {0x1503, 0x2301, 0x2412, 0x8525, 0x8658, 0x4445, 0x4514, 0x4334, 0x8367, 0x88BC, 0x199B},
        {0x2301, 0x4334, 0x8367, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x8367, 0x4334, 0x1503, 0x199B, 0x88BC, 0x8658, 0x8525, 0x4445},
        {0x8367, 0x4334, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x8367, 0x4334, 0x4514, 0x8525, 0x89AC, 0x88BC, 0x2301, 0x1503, 0x199B, 0x289A},
        {0x8525, 0x2412, 0x2301, 0x4334, 0x8367, 0x88BC, 0x89AC},
        {0x1503, 0x4334, 0x8367, 0x8525, 0x2412, 0x88BC, 0x89AC, 0x289A, 0x199B},
        {0x2412, 0x4514, 0x4334, 0x8367, 0x88BC, 0x289A},
        {0x1503, 0x2301, 0x2412, 0x4514, 0x4334, 0x8367, 0x88BC, 0x199B},
        {0x2301, 0x4334, 0x8367, 0x88BC, 0x289A},
        {0x8367, 0x4334, 0x1503, 0x199B, 0x88BC},
        {0x1636, 0x8367, 0x88BC, 0x199B, 0x4647, 0x4334, 0x4514, 0x4445},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A, 0x4334, 0x4514, 0x4445, 0x4647},
        {0x8367, 0x1636, 0x199B, 0x88BC, 0x2301, 0x4334, 0x4647, 0x4445, 0x2412},
        {0x2412, 0x4445, 0x4647, 0x4334, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x1636, 0x8367, 0x88BC, 0x199B, 0x4514, 0x4445, 0x4647, 0x4334},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x89AC, 0x4334, 0x4514, 0x4445, 0x4647},
        {0x8525, 0x4445, 0x4647, 0x4334, 0x2301, 0x289A, 0x89AC, 0x1636, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x1636, 0x1503, 0x4334, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC},
        {0x1636, 0x8367, 0x88BC, 0x199B, 0x8658, 0x4647, 0x4334, 0x4514, 0x8525},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A, 0x8658, 0x4647, 0x4334, 0x4514, 0x8525},
        {0x1636, 0x8367, 0x88BC, 0x199B, 0x8658, 0x4647, 0x4334, 0x2301, 0x2412, 0x8525},
        {0x2412, 0x8525, 0x8658, 0x4647, 0x4334, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A},
        {0x2412, 0x4514, 0x4334, 0x4647, 0x8658, 0x89AC, 0x289A, 0x8367, 0x1636, 0x199B, 0x88BC},
        {0x8367, 0x1636, 0x1503, 0x2301, 0x2412, 0x4514, 0x4334, 0x4647, 0x8658, 0x89AC, 0x88BC},
        {0x8658, 0x4647, 0x4334, 0x2301, 0x289A, 0x89AC, 0x1636, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x1636, 0x1503, 0x4334, 0x4647, 0x8658, 0x89AC, 0x88BC},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B, 0x4647, 0x4334, 0x4514, 0x4445},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x289A, 0x4647, 0x4334, 0x4514, 0x4445},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B, 0x2412, 0x4445, 0x4647, 0x4334, 0x2301},
        {0x8658, 0x8478, 0x8367, 0x1636, 0x1503, 0x4334, 0x4647, 0x4445, 0x2412, 0x289A, 0x89AC},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x199B, 0x4445, 0x4647, 0x4334, 0x4514},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x2412, 0x4334, 0x4514, 0x4445, 0x4647},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x4445, 0x4647, 0x4334, 0x2301, 0x289A, 0x199B},
        {0x1503, 0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x4445, 0x4647, 0x4334},
        {0x8525, 0x4514, 0x4334, 0x4647, 0x8478, 0x8367, 0x1636, 0x199B, 0x89AC},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x4647, 0x4334, 0x4514, 0x8525, 0x89AC, 0x289A},
        {0x8525, 0x2412, 0x2301, 0x4334, 0x4647, 0x8478, 0x8367, 0x1636, 0x199B, 0x89AC},
        {0x2412, 0x8525, 0x89AC, 0x289A, 0x1503, 0x1636, 0x8367, 0x8478, 0x4647, 0x4334},
        {0x1636, 0x8367, 0x8478, 0x4647, 0x4334, 0x4514, 0x2412, 0x289A, 0x199B},
        {0x2412, 0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x4647, 0x4334, 0x4514},
        {0x1636, 0x8367, 0x8478, 0x4647, 0x4334, 0x2301, 0x289A, 0x199B},
        {0x1636, 0x8367, 0x8478, 0x4647, 0x4334, 0x1503},
        {0x1636, 0x4334, 0x4514, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4445, 0x4514, 0x4334, 0x1636, 0x1503, 0x2301, 0x289A, 0x88BC},
        {0x1636, 0x4334, 0x2301, 0x2412, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4445, 0x2412, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x4445, 0x8478, 0x88BC, 0x199B, 0x8525, 0x2412, 0x289A, 0x89AC},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x1636, 0x4334, 0x4514, 0x4445, 0x8478, 0x88BC, 0x89AC},
        {0x1636, 0x4334, 0x2301, 0x8525, 0x4445, 0x8478, 0x289A, 0x89AC, 0x88BC, 0x199B},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x8525, 0x8658, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x8658, 0x8525, 0x4514, 0x4334, 0x1636, 0x1503, 0x2301, 0x289A, 0x88BC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x4334, 0x1636, 0x199B, 0x88BC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC, 0x1636, 0x1503, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x2412, 0x8658, 0x8478, 0x289A, 0x89AC, 0x88BC, 0x199B},
        {0x8658, 0x8478, 0x88BC, 0x89AC, 0x2412, 0x2301, 0x1503, 0x1636, 0x4334, 0x4514},
        {0x1636, 0x4334, 0x2301, 0x8658, 0x8478, 0x289A, 0x89AC, 0x88BC, 0x199B},
        {0x8658, 0x8478, 0x88BC, 0x89AC, 0x1503, 0x1636, 0x4334},
        {0x1636, 0x4334, 0x4514, 0x4445, 0x8658, 0x89AC, 0x199B},
        {0x2301, 0x1503, 0x1636, 0x4334, 0x4514, 0x4445, 0x8658, 0x89AC, 0x289A},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x4334, 0x1636, 0x199B, 0x89AC},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A, 0x1503, 0x1636, 0x4334},
        {0x2412, 0x8525, 0x8658, 0x4445, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A},
        {0x8658, 0x8525, 0x2412, 0x2301, 0x1503, 0x1636, 0x4334, 0x4514, 0x4445},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x1636, 0x1503, 0x4334, 0x8525, 0x8658, 0x4445},
        {0x1636, 0x4334, 0x4514, 0x8525, 0x89AC, 0x199B},
        {0x2301, 0x1503, 0x1636, 0x4334, 0x4514, 0x8525, 0x89AC, 0x289A},
        {0x1636, 0x4334, 0x2301, 0x2412, 0x8525, 0x89AC, 0x199B},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x1636, 0x1503, 0x4334},
        {0x2412, 0x4514, 0x4334, 0x1636, 0x199B, 0x289A},
        {0x2301, 0x1503, 0x1636, 0x4334, 0x4514, 0x2412},
        {0x2301, 0x4334, 0x1636, 0x199B, 0x289A},
        {0x1636, 0x1503, 0x4334},
        {0x1503, 0x4514, 0x4445, 0x4647, 0x1636},
        {0x2301, 0x4514, 0x4445, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x1503, 0x2301, 0x2412, 0x4445, 0x4647, 0x1636},
        {0x2412, 0x4445, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x1503, 0x4514, 0x4445, 0x4647, 0x1636},
        {0x1636, 0x4647, 0x4445, 0x4514, 0x2301, 0x2412, 0x8525, 0x89AC, 0x199B},
        {0x8525, 0x4445, 0x4647, 0x1636, 0x1503, 0x2301, 0x289A, 0x89AC},
        {0x8525, 0x4445, 0x4647, 0x1636, 0x199B, 0x89AC},
        {0x1503, 0x4514, 0x8525, 0x8658, 0x4647, 0x1636},
        {0x2301, 0x4514, 0x8525, 0x8658, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x8658, 0x4647, 0x1636, 0x1503, 0x2301, 0x2412, 0x8525},
        {0x2412, 0x8525, 0x8658, 0x4647, 0x1636, 0x199B, 0x289A},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x4647, 0x8658, 0x89AC, 0x289A},
        {0x1636, 0x4647, 0x8658, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x2301, 0x1503, 0x1636, 0x4647, 0x8658, 0x89AC, 0x289A},
        {0x1636, 0x4647, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x8478, 0x88BC, 0x89AC, 0x1636, 0x4647, 0x4445, 0x4514, 0x1503},
        {0x2301, 0x4514, 0x4445, 0x4647, 0x1636, 0x199B, 0x289A, 0x8478, 0x8658, 0x89AC, 0x88BC},
        {0x8478, 0x8658, 0x89AC, 0x88BC, 0x2412, 0x4445, 0x4647, 0x1636, 0x1503, 0x2301},
        {0x2412, 0x4445, 0x4647, 0x1636, 0x199B, 0x289A, 0x8478, 0x8658, 0x89AC, 0x88BC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC, 0x1503, 0x4514, 0x4445, 0x4647, 0x1636},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x4514, 0x4445, 0x4647, 0x1636, 0x199B, 0x88BC},
        {0x2301, 0x1503, 0x1636, 0x4647, 0x4445, 0x8525, 0x8658, 0x8478, 0x88BC, 0x289A},
        {0x8478, 0x8658, 0x8525, 0x4445, 0x4647, 0x1636, 0x199B, 0x88BC},
        {0x8478, 0x4647, 0x1636, 0x1503, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x2301, 0x4514, 0x8525, 0x8478, 0x4647, 0x1636, 0x89AC, 0x88BC, 0x199B, 0x289A},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x1636, 0x4647, 0x8478, 0x88BC, 0x89AC},
        {0x8478, 0x4647, 0x1636, 0x2412, 0x8525, 0x199B, 0x289A, 0x89AC, 0x88BC},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x4647, 0x8478, 0x88BC, 0x289A},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x8478, 0x4647, 0x1636, 0x1503, 0x2301, 0x289A, 0x88BC},
        {0x1636, 0x4647, 0x8478, 0x88BC, 0x199B},
        {0x8367, 0x1636, 0x1503, 0x4514, 0x4445, 0x8478},
        {0x2301, 0x4514, 0x4445, 0x8478, 0x8367, 0x1636, 0x199B, 0x289A},
        {0x2412, 0x4445, 0x8478, 0x8367, 0x1636, 0x1503, 0x2301},
        {0x1636, 0x8367, 0x8478, 0x4445, 0x2412, 0x289A, 0x199B},
        {0x8525, 0x2412, 0x289A, 0x89AC, 0x1503, 0x4514, 0x4445, 0x8478, 0x8367, 0x1636},
        {0x1636, 0x8367, 0x8478, 0x4445, 0x4514, 0x2301, 0x2412, 0x8525, 0x89AC, 0x199B},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x4445, 0x8525, 0x89AC, 0x289A},
        {0x8525, 0x4445, 0x8478, 0x8367, 0x1636, 0x199B, 0x89AC},
        {0x1503, 0x4514, 0x8525, 0x8658, 0x8478, 0x8367, 0x1636},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x4514, 0x2301, 0x289A, 0x199B},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x1503},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x199B},
        {0x8658, 0x8478, 0x8367, 0x1636, 0x1503, 0x4514, 0x2412, 0x289A, 0x89AC},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B, 0x2301, 0x2412, 0x4514},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x289A},
        {0x1636, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x4445, 0x4514, 0x1503, 0x1636, 0x8367, 0x88BC, 0x89AC},
        {0x2301, 0x4514, 0x4445, 0x8658, 0x8367, 0x1636, 0x89AC, 0x88BC, 0x199B, 0x289A},
        {0x8367, 0x1636, 0x1503, 0x2301, 0x2412, 0x4445, 0x8658, 0x89AC, 0x88BC},
        {0x8658, 0x4445, 0x2412, 0x1636, 0x8367, 0x289A, 0x199B, 0x88BC, 0x89AC},
        {0x8367, 0x1636, 0x1503, 0x4514, 0x4445, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC},
        {0x8367, 0x1636, 0x199B, 0x88BC, 0x8658, 0x8525, 0x2412, 0x2301, 0x4514, 0x4445},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A, 0x8525, 0x8658, 0x4445},
        {0x1636, 0x8367, 0x88BC, 0x199B, 0x8525, 0x8658, 0x4445},
        {0x8367, 0x1636, 0x1503, 0x4514, 0x8525, 0x89AC, 0x88BC},
        {0x2301, 0x4514, 0x8525, 0x8367, 0x1636, 0x89AC, 0x88BC, 0x199B, 0x289A},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x89AC},
        {0x2412, 0x8525, 0x8367, 0x1636, 0x89AC, 0x88BC, 0x199B, 0x289A},
        {0x2412, 0x4514, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A},
        {0x8367, 0x1636, 0x199B, 0x88BC, 0x2412, 0x2301, 0x4514},
        {0x2301, 0x1503, 0x1636, 0x8367, 0x88BC, 0x289A},
        {0x1636, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x4647, 0x4445, 0x4514, 0x1503, 0x199B, 0x88BC},
        {0x2301, 0x4514, 0x4445, 0x4647, 0x8367, 0x88BC, 0x289A},
        {0x8367, 0x4647, 0x4445, 0x2412, 0x2301, 0x1503, 0x199B, 0x88BC},
        {0x8367, 0x4647, 0x4445, 0x2412, 0x289A, 0x88BC},
        {0x8367, 0x4647, 0x4445, 0x4514, 0x1503, 0x199B, 0x88BC, 0x2412, 0x8525, 0x89AC, 0x289A},
        {0x8525, 0x2412, 0x2301, 0x4514, 0x4445, 0x4647, 0x8367, 0x88BC, 0x89AC},
        {0x8525, 0x4445, 0x4647, 0x8367, 0x1503, 0x2301, 0x88BC, 0x199B, 0x289A, 0x89AC},
        {0x8367, 0x4647, 0x4445, 0x8525, 0x89AC, 0x88BC},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x4514, 0x1503, 0x199B, 0x88BC},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x4514, 0x2301, 0x289A, 0x88BC},
        {0x1503, 0x2301, 0x2412, 0x8525, 0x8658, 0x4647, 0x8367, 0x88BC, 0x199B},
        {0x8367, 0x4647, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC},
        {0x2412, 0x4514, 0x1503, 0x8367, 0x4647, 0x8658, 0x199B, 0x88BC, 0x89AC, 0x289A},
        {0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC, 0x2412, 0x2301, 0x4514},
        {0x8367, 0x4647, 0x8658, 0x2301, 0x1503, 0x89AC, 0x289A, 0x199B, 0x88BC},
        {0x8658, 0x4647, 0x8367, 0x88BC, 0x89AC},
        {0x1503, 0x4514, 0x4445, 0x4647, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x8478, 0x8367, 0x4647, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC},
        {0x1503, 0x2301, 0x2412, 0x4445, 0x4647, 0x8367, 0x8478, 0x8658, 0x89AC, 0x199B},
        {0x8658, 0x8478, 0x8367, 0x4647, 0x4445, 0x2412, 0x289A, 0x89AC},
        {0x2412, 0x8525, 0x8658, 0x8478, 0x8367, 0x4647, 0x4445, 0x4514, 0x1503, 0x199B, 0x289A},
        {0x8367, 0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x4514, 0x4445, 0x4647},
        {0x1503, 0x2301, 0x289A, 0x199B, 0x8367, 0x8478, 0x8658, 0x8525, 0x4445, 0x4647},
        {0x8478, 0x8658, 0x8525, 0x4445, 0x4647, 0x8367},
        {0x8525, 0x4514, 0x1503, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC, 0x8478, 0x8367, 0x4647},
        {0x2412, 0x8525, 0x89AC, 0x289A, 0x8367, 0x8478, 0x4647},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B, 0x8367, 0x8478, 0x4647},
        {0x2301, 0x2412, 0x4514, 0x8478, 0x8367, 0x4647},
        {0x2301, 0x1503, 0x199B, 0x289A, 0x8478, 0x8367, 0x4647},
        {0x8478, 0x8367, 0x4647},
        {0x1503, 0x4514, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x2301, 0x4514, 0x4445, 0x8478, 0x88BC, 0x289A},
        {0x1503, 0x2301, 0x2412, 0x4445, 0x8478, 0x88BC, 0x199B},
        {0x8478, 0x4445, 0x2412, 0x289A, 0x88BC},
        {0x1503, 0x4514, 0x4445, 0x8478, 0x88BC, 0x199B, 0x8525, 0x2412, 0x289A, 0x89AC},
        {0x8525, 0x2412, 0x2301, 0x4514, 0x4445, 0x8478, 0x88BC, 0x89AC},
        {0x8525, 0x4445, 0x8478, 0x1503, 0x2301, 0x88BC, 0x199B, 0x289A, 0x89AC},
        {0x8478, 0x4445, 0x8525, 0x89AC, 0x88BC},
        {0x8478, 0x8658, 0x8525, 0x4514, 0x1503, 0x199B, 0x88BC},
        {0x2301, 0x4514, 0x8525, 0x8658, 0x8478, 0x88BC, 0x289A},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x88BC},
        {0x8478, 0x8658, 0x8525, 0x2412, 0x289A, 0x88BC},
        {0x2412, 0x4514, 0x1503, 0x8478, 0x8658, 0x199B, 0x88BC, 0x89AC, 0x289A},
        {0x8478, 0x8658, 0x89AC, 0x88BC, 0x2301, 0x2412, 0x4514},
        {0x1503, 0x2301, 0x8658, 0x8478, 0x289A, 0x89AC, 0x88BC, 0x199B},
        {0x8478, 0x8658, 0x89AC, 0x88BC},
        {0x8658, 0x4445, 0x4514, 0x1503, 0x199B, 0x89AC},
        {0x8658, 0x4445, 0x4514, 0x2301, 0x289A, 0x89AC},
        {0x8658, 0x4445, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC},
        {0x2412, 0x4445, 0x8658, 0x89AC, 0x289A},
        {0x2412, 0x8525, 0x8658, 0x4445, 0x4514, 0x1503, 0x199B, 0x289A},
        {0x8525, 0x2412, 0x2301, 0x4514, 0x4445, 0x8658},
        {0x1503, 0x2301, 0x289A, 0x199B, 0x8658, 0x8525, 0x4445},
        {0x8525, 0x8658, 0x4445},
        {0x8525, 0x4514, 0x1503, 0x199B, 0x89AC},
        {0x8525, 0x4514, 0x2301, 0x289A, 0x89AC},
        {0x8525, 0x2412, 0x2301, 0x1503, 0x199B, 0x89AC},
        {0x8525, 0x2412, 0x289A, 0x89AC},
        {0x1503, 0x4514, 0x2412, 0x289A, 0x199B},
        {0x2301, 0x2412, 0x4514},
        {0x2301, 0x1503, 0x199B, 0x289A},
        {}
    };


    /**
     * The RegularCellData structure holds information about the triangulation
     * used for a single equivalence class in the modified Marching Cubes algorithm,
     * described in Section 3.2.
     */
    static class RegularCellData {
        char geometryCounts; // High nibble is vertex count, low nibble is triangle count.
        char[] vertexIndex; // Groups of 3 indexes giving the triangulation.
        
        long getVertexCount(){
            return (geometryCounts >> 4);
        }
        
        long getTriangleCount(){
            return (geometryCounts & 0x0F);
        }

        public RegularCellData(char geometryCounts, char[] vertexIndex){
            this.geometryCounts = geometryCounts;
            this.vertexIndex = vertexIndex;
        }

    };

    /**
     * The TransitionCellData structure holds information about the triangulation
     * used for a single equivalence class in the Transvoxel Algorithm transition cell,
     * described in Section 4.3.
     */
    static class TransitionCellData {
        long geometryCounts; // High nibble is vertex count, low nibble is triangle count.
        char[] vertexIndex; // Groups of 3 indexes giving the triangulation.
        
        long getVertexCount(){
            return (geometryCounts >> 4);
        }
        
        long getTriangleCount(){
            return (geometryCounts & 0x0F);
        }

        public TransitionCellData(char geometryCounts, char[] vertexIndex){
            this.geometryCounts = geometryCounts;
            this.vertexIndex = vertexIndex;
        }
    };
    
}
