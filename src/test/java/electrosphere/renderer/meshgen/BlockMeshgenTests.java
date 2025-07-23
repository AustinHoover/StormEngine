package electrosphere.renderer.meshgen;

import static org.junit.jupiter.api.Assertions.*;

import electrosphere.client.block.BlockChunkData;
import electrosphere.renderer.meshgen.BlockMeshgen.BlockMeshData;
import electrosphere.renderer.meshgen.accel.MeshGenStore;
import electrosphere.renderer.meshgen.accel.QuadMeshCache;
import electrosphere.renderer.meshgen.accel.QuadMeshCache.QuadMesh;
import electrosphere.test.annotations.UnitTest;

/**
 * Tests for block mesh generation
 */
public class BlockMeshgenTests {
    
    /**
     * If a normal vector has same length across all three axis, this will be the length
     */
    static final float NORMAL_MULTIPLIER = 0.57735026f;

    @UnitTest
    public void test_fillQuadMeshes_1(){

        //expected data
        QuadMesh expectedQuad = new QuadMesh();
        expectedQuad.set(0,0,0,1,1,1);
        QuadMesh[] expectedData = new QuadMesh[]{
            expectedQuad,
        };

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);
        QuadMeshCache cache = QuadMeshCache.getF();

        //call
        BlockMeshgen.fillQuadMeshes(cache, chunkData);


        //error check result
        assertEquals(expectedData.length, cache.getActiveCount());

        for(QuadMesh expected : expectedData){
            boolean found = false;
            for(int i = 0; i < cache.getActiveCount(); i++){
                QuadMesh actual = cache.getActive(i);
                if(expected.x == actual.x && expected.y == actual.y && expected.w == actual.w && expected.h == actual.h){
                    found = true;
                    assertEquals(expected.x, actual.x);
                    assertEquals(expected.y, actual.y);
                    assertEquals(expected.w, actual.w);
                    assertEquals(expected.h, actual.h);
                    break;
                }
            }
            assertEquals(true, found);
        }

        QuadMeshCache.release(cache);
    }

    @UnitTest
    public void test_fillQuadMeshes_2(){
        //expected data
        QuadMesh[] expectedData = new QuadMesh[]{
            new QuadMesh(),
        };
        expectedData[0].set(0,0,0,1,2,1);

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 1, 0, (short)1);
        QuadMeshCache cache = QuadMeshCache.getF();

        //call
        BlockMeshgen.fillQuadMeshes(cache, chunkData);


        //error check result
        assertEquals(expectedData.length, cache.getActiveCount());

        for(QuadMesh expected : expectedData){
            boolean found = false;
            for(int i = 0; i < cache.getActiveCount(); i++){
                QuadMesh actual = cache.getActive(i);
                if(expected.x == actual.x && expected.y == actual.y && expected.w == actual.w && expected.h == actual.h){
                    found = true;
                    assertEquals(expected.x, actual.x);
                    assertEquals(expected.y, actual.y);
                    assertEquals(expected.w, actual.w);
                    assertEquals(expected.h, actual.h);
                    break;
                }
            }
            assertEquals(true, found);
        }
        QuadMeshCache.release(cache);
    }

    @UnitTest
    public void test_fillQuadMeshes_3(){
        //expected data
        QuadMesh[] expectedData = new QuadMesh[]{
            new QuadMesh(),
        };
        expectedData[0].set(0,0,0,2,2,1);

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 1, 0, (short)1);
        chunkData.setType(1, 0, 0, (short)1);
        chunkData.setType(1, 1, 0, (short)1);
        QuadMeshCache cache = QuadMeshCache.getF();

        //call
        BlockMeshgen.fillQuadMeshes(cache, chunkData);


        //error check result
        assertEquals(expectedData.length, cache.getActiveCount());

        for(QuadMesh expected : expectedData){
            boolean found = false;
            for(int i = 0; i < cache.getActiveCount(); i++){
                QuadMesh actual = cache.getActive(i);
                if(expected.x == actual.x && expected.y == actual.y && expected.w == actual.w && expected.h == actual.h){
                    found = true;
                    assertEquals(expected.x, actual.x);
                    assertEquals(expected.y, actual.y);
                    assertEquals(expected.w, actual.w);
                    assertEquals(expected.h, actual.h);
                    break;
                }
            }
            assertEquals(true, found);
        }
        QuadMeshCache.release(cache);
    }

    @UnitTest
    public void test_fillQuadMeshes_4(){
        //expected data
        QuadMesh[] expectedData = new QuadMesh[]{
            new QuadMesh(),
            new QuadMesh(),
        };
        expectedData[0].set(0,0,0,1,1,1);
        expectedData[1].set(0,2,0,1,1,1);

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 2, 0, (short)1);
        QuadMeshCache cache = QuadMeshCache.getF();

        //call
        BlockMeshgen.fillQuadMeshes(cache, chunkData);


        //error check result
        assertEquals(expectedData.length, cache.getActiveCount());

        for(QuadMesh expected : expectedData){
            boolean found = false;
            for(int i = 0; i < cache.getActiveCount(); i++){
                QuadMesh actual = cache.getActive(i);
                if(expected.x == actual.x && expected.y == actual.y && expected.w == actual.w && expected.h == actual.h){
                    found = true;
                    assertEquals(expected.x, actual.x);
                    assertEquals(expected.y, actual.y);
                    assertEquals(expected.w, actual.w);
                    assertEquals(expected.h, actual.h);
                    break;
                }
            }
            assertEquals(true, found);
        }
        QuadMeshCache.release(cache);
    }

    @UnitTest
    public void test_fillQuadMeshes_5(){
        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        QuadMeshCache cache = QuadMeshCache.getF();

        //call
        BlockMeshgen.fillQuadMeshes(cache, chunkData);

        //error check result
        assertEquals(0, cache.getActiveCount());

        QuadMeshCache.release(cache);
    }

    @UnitTest
    public void test_fillQuadMeshes_6(){
        //expected data
        QuadMesh[] expectedData = new QuadMesh[]{
            new QuadMesh(),
            new QuadMesh(),
            new QuadMesh(),
        };
        expectedData[0].set(5,0,8,1,1,1);
        expectedData[1].set(8,0,8,1,1,1);
        expectedData[2].set(7,0,4,1,1,1);

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(5, 0, 8, (short)1);
        chunkData.setType(8, 0, 8, (short)1);
        chunkData.setType(7, 0, 4, (short)1);
        QuadMeshCache cache = QuadMeshCache.getF();

        //call
        BlockMeshgen.fillQuadMeshes(cache, chunkData);


        //error check result
        assertEquals(expectedData.length, cache.getActiveCount());

        for(QuadMesh expected : expectedData){
            boolean found = false;
            for(int i = 0; i < cache.getActiveCount(); i++){
                QuadMesh actual = cache.getActive(i);
                if(expected.x == actual.x && expected.y == actual.y && expected.w == actual.w && expected.h == actual.h){
                    found = true;
                    assertEquals(expected.x, actual.x);
                    assertEquals(expected.y, actual.y);
                    assertEquals(expected.w, actual.w);
                    assertEquals(expected.h, actual.h);
                    break;
                }
            }
            assertEquals(true, found);
        }
        QuadMeshCache.release(cache);
    }

    @UnitTest
    public void test_meshifyBox_verts(){
        MeshGenStore store = MeshGenStore.get();
        store.clear();
        //expected data
        float[] expectedData = new float[]{
            0,0,0,
            1,0,0,
            0,1,0,
            1,1,0,

            0,0,0,
            0,0,1,
            0,1,0,
            0,1,1,

            0,0,0,
            0,0,1,
            1,0,0,
            1,0,1,

            0,0,1,
            1,0,1,
            0,1,1,
            1,1,1,

            1,0,0,
            1,0,1,
            1,1,0,
            1,1,1,

            0,1,0,
            0,1,1,
            1,1,0,
            1,1,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        QuadMesh quad = new QuadMesh();
        quad.set(0, 0, 0, 1, 1,1);

        //call
        BlockMeshgen.meshifyBox(store, quad, 1, 1, 1);


        //error check result
        assertEquals(expectedData.length, store.getVertCount());

        float[] verts = store.getVertArr();
        for(int i = 0; i < store.getVertCount(); i++){
            assertEquals(expectedData[i], verts[i]);
        }

        MeshGenStore.release(store);
    }

    @UnitTest
    public void test_meshifyBox_normals(){
        MeshGenStore store = MeshGenStore.get();
        store.clear();
        //expected data
        float[] expectedData = new float[]{
             0, 0,-1,
             0, 0,-1,
             0, 0,-1,
             0, 0,-1,

             -1, 0, 0,
             -1, 0, 0,
             -1, 0, 0,
             -1, 0, 0,

             0,-1, 0,
             0,-1, 0,
             0,-1, 0,
             0,-1, 0,

             0, 0, 1,
             0, 0, 1,
             0, 0, 1,
             0, 0, 1,


             1, 0, 0,
             1, 0, 0,
             1, 0, 0,
             1, 0, 0,


             0, 1, 0,
             0, 1, 0,
             0, 1, 0,
             0, 1, 0,
        };

        //setup data
        QuadMesh quad = new QuadMesh();
        quad.set(0, 0, 0, 1, 1, 1);

        //call
        BlockMeshgen.meshifyBox(store, quad, 1, 1, 1);


        //error check result
        assertEquals(expectedData.length / 3, store.getNormalCount() / 3);

        float[] normals = store.getNormalArr();
        for(int i = 0; i < store.getNormalCount(); i++){
            assertEquals(expectedData[i], normals[i]);
        }

        MeshGenStore.release(store);
    }

    @UnitTest
    public void test_meshifyBox_uvs(){
        MeshGenStore store = MeshGenStore.get();
        store.clear();
        //expected data
        float[] expectedData = new float[]{
            0, 0,
            1, 0,
            0, 1,
            1, 1,

            0, 0,
            1, 0,
            0, 1,
            1, 1,

            0, 0,
            1, 0,
            0, 1,
            1, 1,

            0, 0,
            1, 0,
            0, 1,
            1, 1,

            0, 0,
            1, 0,
            0, 1,
            1, 1,

            0, 0,
            1, 0,
            0, 1,
            1, 1,
        };

        //setup data
        QuadMesh quad = new QuadMesh();
        quad.set(0, 0, 0, 1, 1, 1);

        //call
        BlockMeshgen.meshifyBox(store, quad, 1, 1, 1);


        //error check result
        assertEquals(expectedData.length / 2, store.getUvCount() / 2);

        float[] uvs = store.getUvArr();
        for(int i = 0; i < store.getUvCount(); i++){
            assertEquals(expectedData[i], uvs[i]);
        }

        MeshGenStore.release(store);
    }

    @UnitTest
    public void test_rasterize_1(){
        //expected data
        float[] expectedData = new float[]{
            0,0,0,
            1,0,0,
            0,1,0,
            1,1,0,

            0,0,0,
            0,0,1,
            0,1,0,
            0,1,1,

            0,0,0,
            0,0,1,
            1,0,0,
            1,0,1,

            0,0,1,
            1,0,1,
            0,1,1,
            1,1,1,

            1,0,0,
            1,0,1,
            1,1,0,
            1,1,1,

            0,1,0,
            0,1,1,
            1,1,0,
            1,1,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_2(){
        //expected data
        float[] expectedData = new float[]{
            0,0,0,
            1,0,0,
            0,2,0,
            1,2,0,

            0,0,0,
            0,0,1,
            0,2,0,
            0,2,1,

            0,0,0,
            0,0,1,
            1,0,0,
            1,0,1,

            0,0,1,
            1,0,1,
            0,2,1,
            1,2,1,

            1,0,0,
            1,0,1,
            1,2,0,
            1,2,1,

            0,2,0,
            0,2,1,
            1,2,0,
            1,2,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 1, 0, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_3(){
        //expected data
        float[] expectedData = new float[]{
            0,0,0,
            2,0,0,
            0,2,0,
            2,2,0,

            0,0,0,
            0,0,1,
            0,2,0,
            0,2,1,

            0,0,0,
            0,0,1,
            2,0,0,
            2,0,1,

            0,0,1,
            2,0,1,
            0,2,1,
            2,2,1,

            2,0,0,
            2,0,1,
            2,2,0,
            2,2,1,

            0,2,0,
            0,2,1,
            2,2,0,
            2,2,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 1, 0, (short)1);
        chunkData.setType(1, 0, 0, (short)1);
        chunkData.setType(1, 1, 0, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_4(){
        //expected data
        float[] expectedData = new float[]{
            0,0,0,
            2,0,0,
            0,2,0,
            2,2,0,

            0,0,0,
            0,0,2,
            0,2,0,
            0,2,2,

            0,0,0,
            0,0,2,
            2,0,0,
            2,0,2,

            0,0,2,
            2,0,2,
            0,2,2,
            2,2,2,

            2,0,0,
            2,0,2,
            2,2,0,
            2,2,2,

            0,2,0,
            0,2,2,
            2,2,0,
            2,2,2,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 1, 0, (short)1);
        chunkData.setType(1, 0, 0, (short)1);
        chunkData.setType(1, 1, 0, (short)1);
        chunkData.setType(0, 0, 1, (short)1);
        chunkData.setType(0, 1, 1, (short)1);
        chunkData.setType(1, 0, 1, (short)1);
        chunkData.setType(1, 1, 1, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_5(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            0,0,0,
            1,0,0,
            0,1,0,
            1,1,0,

            0,0,0,
            0,0,1,
            0,1,0,
            0,1,1,

            0,0,0,
            0,0,1,
            1,0,0,
            1,0,1,

            0,0,1,
            1,0,1,
            0,1,1,
            1,1,1,

            1,0,0,
            1,0,1,
            1,1,0,
            1,1,1,

            0,1,0,
            0,1,1,
            1,1,0,
            1,1,1,
            
            //block 2
            0,2,0,
            1,2,0,
            0,3,0,
            1,3,0,

            0,2,0,
            0,2,1,
            0,3,0,
            0,3,1,

            0,2,0,
            0,2,1,
            1,2,0,
            1,2,1,

            0,2,1,
            1,2,1,
            0,3,1,
            1,3,1,

            1,2,0,
            1,2,1,
            1,3,0,
            1,3,1,

            0,3,0,
            0,3,1,
            1,3,0,
            1,3,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(0, 0, 0, (short)1);

        //block 2
        chunkData.setType(0, 2, 0, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_6(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            0,0,0,
            1,0,0,
            0,1,0,
            1,1,0,

            0,0,0,
            0,0,1,
            0,1,0,
            0,1,1,

            0,0,0,
            0,0,1,
            1,0,0,
            1,0,1,

            0,0,1,
            1,0,1,
            0,1,1,
            1,1,1,

            1,0,0,
            1,0,1,
            1,1,0,
            1,1,1,

            0,1,0,
            0,1,1,
            1,1,0,
            1,1,1,
            
            //block 2
            0,0,2,
            1,0,2,
            0,1,2,
            1,1,2,

            0,0,2,
            0,0,3,
            0,1,2,
            0,1,3,

            0,0,2,
            0,0,3,
            1,0,2,
            1,0,3,

            0,0,3,
            1,0,3,
            0,1,3,
            1,1,3,

            1,0,2,
            1,0,3,
            1,1,2,
            1,1,3,

            0,1,2,
            0,1,3,
            1,1,2,
            1,1,3,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(0, 0, 0, (short)1);

        //block 2
        chunkData.setType(0, 0, 2, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_7(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            0,0,0,
            1,0,0,
            0,1,0,
            1,1,0,

            0,0,0,
            0,0,1,
            0,1,0,
            0,1,1,

            0,0,0,
            0,0,1,
            1,0,0,
            1,0,1,

            0,0,1,
            1,0,1,
            0,1,1,
            1,1,1,

            1,0,0,
            1,0,1,
            1,1,0,
            1,1,1,

            0,1,0,
            0,1,1,
            1,1,0,
            1,1,1,
            
            //block 2
            2,0,0,
            3,0,0,
            2,1,0,
            3,1,0,

            2,0,0,
            2,0,1,
            2,1,0,
            2,1,1,

            2,0,0,
            2,0,1,
            3,0,0,
            3,0,1,

            2,0,1,
            3,0,1,
            2,1,1,
            3,1,1,

            3,0,0,
            3,0,1,
            3,1,0,
            3,1,1,

            2,1,0,
            2,1,1,
            3,1,0,
            3,1,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(0, 0, 0, (short)1);

        //block 2
        chunkData.setType(2, 0, 0, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_8(){
        //expected data
        float[] expectedData = new float[]{};

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_9(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            0,0,0,
            1,0,0,
            0,2,0,
            1,2,0,

            0,0,0,
            0,0,2,
            0,2,0,
            0,2,2,

            0,0,0,
            0,0,2,
            1,0,0,
            1,0,2,

            0,0,2,
            1,0,2,
            0,2,2,
            1,2,2,

            1,0,0,
            1,0,2,
            1,2,0,
            1,2,2,

            0,2,0,
            0,2,2,
            1,2,0,
            1,2,2,
            
            //block 2
            2,0,0,
            3,0,0,
            2,1,0,
            3,1,0,

            2,0,0,
            2,0,1,
            2,1,0,
            2,1,1,

            2,0,0,
            2,0,1,
            3,0,0,
            3,0,1,

            2,0,1,
            3,0,1,
            2,1,1,
            3,1,1,

            3,0,0,
            3,0,1,
            3,1,0,
            3,1,1,

            2,1,0,
            2,1,1,
            3,1,0,
            3,1,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 1, 0, (short)1);
        chunkData.setType(0, 1, 1, (short)1);
        chunkData.setType(0, 0, 1, (short)1);

        //block 2
        chunkData.setType(2, 0, 0, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_10(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            0,0,0,
            1,0,0,
            0,2,0,
            1,2,0,

            0,0,0,
            0,0,2,
            0,2,0,
            0,2,2,

            0,0,0,
            0,0,2,
            1,0,0,
            1,0,2,

            0,0,2,
            1,0,2,
            0,2,2,
            1,2,2,

            1,0,0,
            1,0,2,
            1,2,0,
            1,2,2,

            0,2,0,
            0,2,2,
            1,2,0,
            1,2,2,
            
            //block 2
            1,0,0,
            2,0,0,
            1,1,0,
            2,1,0,

            1,0,0,
            1,0,1,
            1,1,0,
            1,1,1,

            1,0,0,
            1,0,1,
            2,0,0,
            2,0,1,

            1,0,1,
            2,0,1,
            1,1,1,
            2,1,1,

            2,0,0,
            2,0,1,
            2,1,0,
            2,1,1,

            1,1,0,
            1,1,1,
            2,1,0,
            2,1,1,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(0, 0, 0, (short)1);
        chunkData.setType(0, 1, 0, (short)1);
        chunkData.setType(0, 1, 1, (short)1);
        chunkData.setType(0, 0, 1, (short)1);

        //block 2
        chunkData.setType(1, 0, 0, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_11(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            1,1,1,
            2,1,1,
            1,3,1,
            2,3,1,

            1,1,1,
            1,1,3,
            1,3,1,
            1,3,3,

            1,1,1,
            1,1,3,
            2,1,1,
            2,1,3,

            1,1,3,
            2,1,3,
            1,3,3,
            2,3,3,

            2,1,1,
            2,1,3,
            2,3,1,
            2,3,3,

            1,3,1,
            1,3,3,
            2,3,1,
            2,3,3,
            
            //block 2
            2,1,1,
            3,1,1,
            2,2,1,
            3,2,1,

            2,1,1,
            2,1,2,
            2,2,1,
            2,2,2,

            2,1,1,
            2,1,2,
            3,1,1,
            3,1,2,

            2,1,2,
            3,1,2,
            2,2,2,
            3,2,2,

            3,1,1,
            3,1,2,
            3,2,1,
            3,2,2,

            2,2,1,
            2,2,2,
            3,2,1,
            3,2,2,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(1, 1, 1, (short)1);
        chunkData.setType(1, 2, 1, (short)1);
        chunkData.setType(1, 2, 2, (short)1);
        chunkData.setType(1, 1, 2, (short)1);

        //block 2
        chunkData.setType(2, 1, 1, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            assertEquals(expectedData[i], meshData.vertices[i]);
        }
    }

    @UnitTest
    public void test_rasterize_12(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            1,1,1,
            2,1,1,
            1,5,1,
            2,5,1,

            1,1,1,
            1,1,2,
            1,5,1,
            1,5,2,

            1,1,1,
            1,1,2,
            2,1,1,
            2,1,2,

            1,1,2,
            2,1,2,
            1,5,2,
            2,5,2,

            2,1,1,
            2,1,2,
            2,5,1,
            2,5,2,

            1,5,1,
            1,5,2,
            2,5,1,
            2,5,2,
            
            //block 2
            2,4,1,
            3,4,1,
            2,5,1,
            3,5,1,

            2,4,1,
            2,4,2,
            2,5,1,
            2,5,2,

            2,4,1,
            2,4,2,
            3,4,1,
            3,4,2,

            2,4,2,
            3,4,2,
            2,5,2,
            3,5,2,

            3,4,1,
            3,4,2,
            3,5,1,
            3,5,2,

            2,5,1,
            2,5,2,
            3,5,1,
            3,5,2,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(1, 1, 1, (short)1);
        chunkData.setType(1, 2, 1, (short)1);
        chunkData.setType(1, 3, 1, (short)1);
        chunkData.setType(1, 4, 1, (short)1);

        //block 2
        chunkData.setType(2, 4, 1, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            if(expectedData[i] != meshData.vertices[i]){
                System.err.println("Vertex was incorrect! " + i);
                String message = "Vertex was incorrect!\n" +
                "index: " + i + "\n" +
                "expected: " + expectedData[i] + "\n" +
                "actual: " + meshData.vertices[i] + "\n";
                assertDoesNotThrow(() -> {
                    throw new Error(message);
                });
            }
        }
    }

    @UnitTest
    public void test_rasterize_13(){
        //expected data
        float[] expectedData = new float[]{
            //block 1
            1,1,1,
            2,1,1,
            1,5,1,
            2,5,1,

            1,1,1,
            1,1,2,
            1,5,1,
            1,5,2,

            1,1,1,
            1,1,2,
            2,1,1,
            2,1,2,

            1,1,2,
            2,1,2,
            1,5,2,
            2,5,2,

            2,1,1,
            2,1,2,
            2,5,1,
            2,5,2,

            1,5,1,
            1,5,2,
            2,5,1,
            2,5,2,
            
            //block 2
            2,4,1,
            4,4,1,
            2,5,1,
            4,5,1,

            2,4,1,
            2,4,2,
            2,5,1,
            2,5,2,

            2,4,1,
            2,4,2,
            4,4,1,
            4,4,2,

            2,4,2,
            4,4,2,
            2,5,2,
            4,5,2,

            4,4,1,
            4,4,2,
            4,5,1,
            4,5,2,

            2,5,1,
            2,5,2,
            4,5,1,
            4,5,2,
        };
        for(int i = 0; i < expectedData.length; i++){
            expectedData[i] = expectedData[i] * BlockChunkData.BLOCK_SIZE_MULTIPLIER;
        }

        //setup data
        BlockChunkData chunkData = new BlockChunkData();
        short[] types = new short[BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH * BlockChunkData.CHUNK_DATA_WIDTH];
        chunkData.setType(types);
        //block 1
        chunkData.setType(1, 1, 1, (short)1);
        chunkData.setType(1, 2, 1, (short)1);
        chunkData.setType(1, 3, 1, (short)1);
        chunkData.setType(1, 4, 1, (short)1);

        //block 2
        chunkData.setType(2, 4, 1, (short)1);
        chunkData.setType(3, 4, 1, (short)1);

        //call
        BlockMeshData meshData = BlockMeshgen.rasterize(chunkData);


        //error check result
        assertEquals(expectedData.length, meshData.vertices.length);

        for(int i = 0; i < expectedData.length; i++){
            if(expectedData[i] != meshData.vertices[i]){
                System.err.println("Vertex was incorrect! " + i);
                String message = "Vertex was incorrect!\n" +
                "index: " + i + "\n" +
                "expected: " + expectedData[i] + "\n" +
                "actual: " + meshData.vertices[i] + "\n";
                assertDoesNotThrow(() -> {
                    throw new Error(message);
                });
            }
        }
    }

}
