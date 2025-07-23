package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3i;
import org.lwjgl.BufferUtils;

import electrosphere.client.block.BlockChunkData;
import electrosphere.engine.Globals;
import electrosphere.entity.state.collidable.MultiShapeTriGeomData;
import electrosphere.entity.state.collidable.TriGeomData;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.meshgen.accel.MeshGenStore;
import electrosphere.renderer.meshgen.accel.QuadMeshCache;
import electrosphere.renderer.meshgen.accel.QuadMeshCache.QuadMesh;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;

/**
 * Generates a model for a block
 */
public class BlockMeshgen {

    /**
     * The indices to draw faces on cubes
     */
    static final int[] CUBE_INDICES = new int[]{
        //Top
        2, 6, 7,
        2, 3, 7,

        //Bottom
        0, 4, 5,
        0, 1, 5,

        //Left
        0, 2, 6,
        0, 4, 6,

        //Right
        1, 3, 7,
        1, 5, 7,

        //Front
        0, 2, 3,
        0, 1, 3,

        //Back
        4, 6, 7,
        4, 5, 7
    };

    /**
     * Position of the sampler data in the buffer
     */
    static final int SAMPLER_SHADER_POSITION = 5;

    /**
     * The size of the sampler data per-vertex
     */
    static final int SAMPLER_DATA_SIZE = 1;

    /**
     * Default scaling factor
     */
    public static final int DEFAULT_SCALING_FACTOR = 1;


    /**
     * Checks whether this block should be rasterized or not
     * @param data The data
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param solids true to rasterize solids, false to rasterize transparents
     * @param solidsMap The map of block type to solid status
     * @return true if it should be rasterized, false otherwise
     */
    protected static boolean shouldRasterize(BlockMeshgenData data, int x, int y, int z, boolean solids, Map<Integer,Boolean> solidsMap){
        if(data.isEmpty(x, y, z)){
            return false;
        }
        if(solidsMap == null){
            return true;
        }
        return solids == solidsMap.get((int)data.getType(x, y, z));
    }

    /**
     * Calculates the quad meshes for the provided data
     * @param quadMeshes The quad mesh list to fill
     * @param data The block data
     */
    protected static void fillQuadMeshes(QuadMeshCache quadMeshCache, BlockMeshgenData data){
        BlockMeshgen.fillQuadMeshes(quadMeshCache, data, true, null);
    }


    /**
     * Calculates the quad meshes for the provided data
     * @param quadMeshes The quad mesh list to fill
     * @param data The block data
     */
    protected static void fillQuadMeshes(QuadMeshCache quadMeshCache, BlockMeshgenData data, boolean solids, Map<Integer,Boolean> solidsMap){
        Vector3i dimensions = data.getDimensions();
        for(int z = 0; z < dimensions.z; z++){
            for(int x = 0; x < dimensions.x; x++){
                QuadMesh currentQuad = null;
                for(int y = 0; y < dimensions.y; y++){
                    if(!BlockMeshgen.shouldRasterize(data,x,y,z,solids,solidsMap)){
                        if(currentQuad == null){
                            continue;
                        } else {
                            currentQuad.h = y - currentQuad.y;
                            //check if should merge with previous quad
                            for(int j = 0; j < quadMeshCache.getActiveCount(); j++){
                                QuadMesh prevMesh = quadMeshCache.getActive(j);
                                if(prevMesh == currentQuad){
                                    continue;
                                }
                                if(prevMesh.x + prevMesh.w == currentQuad.x && prevMesh.y == currentQuad.y && prevMesh.h == currentQuad.h && prevMesh.z == currentQuad.z){
                                    prevMesh.w = prevMesh.w + 1;
                                    quadMeshCache.destroy(currentQuad);
                                    break;
                                }
                            }
                            currentQuad = null;
                        }
                    } else {
                        if(currentQuad == null){
                            currentQuad = quadMeshCache.getNew();
                            currentQuad.x = x;
                            currentQuad.y = y;
                            currentQuad.z = z;
                            currentQuad.w = 1;
                            currentQuad.h = 1;
                            currentQuad.type = data.getType(x, y, z);
                        } else if(currentQuad.type == data.getType(x, y, z)) {
                            continue;
                        } else {
                            currentQuad.h = y - currentQuad.y;
                            //check if should merge with previous quad
                            for(int j = 0; j < quadMeshCache.getActiveCount(); j++){
                                QuadMesh prevMesh = quadMeshCache.getActive(j);
                                if(prevMesh == currentQuad){
                                    continue;
                                }
                                if(prevMesh.x + prevMesh.w == currentQuad.x && prevMesh.y == currentQuad.y && prevMesh.h == currentQuad.h && prevMesh.z == currentQuad.z){
                                    prevMesh.w = prevMesh.w + 1;
                                    quadMeshCache.destroy(currentQuad);
                                    break;
                                }
                            }
                            currentQuad = quadMeshCache.getNew();
                            currentQuad.x = x;
                            currentQuad.y = y;
                            currentQuad.z = z;
                            currentQuad.w = 1;
                            currentQuad.h = 1;
                            currentQuad.type = data.getType(x, y, z);
                        }
                    }
                }
                if(currentQuad != null){
                    currentQuad.h = dimensions.y - currentQuad.y;
                    //check if should merge with previous quad
                    for(int j = 0; j < quadMeshCache.getActiveCount(); j++){
                        QuadMesh prevMesh = quadMeshCache.getActive(j);
                        if(prevMesh == currentQuad){
                            continue;
                        }
                        if(prevMesh.x + prevMesh.w == currentQuad.x && prevMesh.y == currentQuad.y && prevMesh.h == currentQuad.h && prevMesh.z == currentQuad.z){
                            prevMesh.w = prevMesh.w + 1;
                            quadMeshCache.destroy(currentQuad);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Meshes a box
     * @param meshStore The store for mesh data
     * @param quad The quad
     * @param depth The depth of the box
     * @param blockType The type of block
     * @param indexOffset The offset for the indices that will be added (ie, if there are already vertices in the verts list, pass in the size of the vert list)
     * @param scalingFactor The factor to scale by
     */
    protected static void meshifyBox(MeshGenStore meshStore, QuadMesh quad, int depth, int blockType, int scalingFactor){

        int indexOffset = meshStore.getVertCount() / 3;

        float finalScalingFactor = scalingFactor * BlockChunkData.BLOCK_SIZE_MULTIPLIER;

        //
        //face 1
        //
        int samplerIndex = Globals.blockTextureAtlas.getVoxelTypeOffset(blockType);

        //verts
        meshStore.addVert(quad.x * finalScalingFactor,            quad.y * finalScalingFactor,            quad.z * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,  quad.y * finalScalingFactor,            quad.z * finalScalingFactor);
        meshStore.addVert(quad.x * finalScalingFactor,            (quad.y + quad.h) * finalScalingFactor,  quad.z * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,  (quad.y + quad.h) * finalScalingFactor,  quad.z * finalScalingFactor);
        //indices
        meshStore.addFace(indexOffset + 0, indexOffset + 2, indexOffset + 3);
        meshStore.addFace(indexOffset + 0, indexOffset + 3, indexOffset + 1);
        //normals
        meshStore.addNormal(0, 0, -1);
        meshStore.addNormal(0, 0, -1);
        meshStore.addNormal(0, 0, -1);
        meshStore.addNormal(0, 0, -1);
        //uvs
        meshStore.addUV(     0,      0);
        meshStore.addUV(quad.w,      0);
        meshStore.addUV(     0, quad.h);
        meshStore.addUV(quad.w, quad.h);
        //samplers
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);

        //
        //face 2
        //

        //verts
        meshStore.addVert(quad.x * finalScalingFactor,            quad.y * finalScalingFactor,            quad.z * finalScalingFactor);
        meshStore.addVert(quad.x * finalScalingFactor,            quad.y * finalScalingFactor,            (quad.z + depth) * finalScalingFactor);
        meshStore.addVert(quad.x * finalScalingFactor,            (quad.y + quad.h) * finalScalingFactor,            quad.z * finalScalingFactor);
        meshStore.addVert(quad.x * finalScalingFactor,            (quad.y + quad.h) * finalScalingFactor,            (quad.z + depth) * finalScalingFactor);
        //indices
        meshStore.addFace(indexOffset + 4, indexOffset + 7, indexOffset + 6);
        meshStore.addFace(indexOffset + 4, indexOffset + 5, indexOffset + 7);
        //normals
        meshStore.addNormal(-1, 0, 0);
        meshStore.addNormal(-1, 0, 0);
        meshStore.addNormal(-1, 0, 0);
        meshStore.addNormal(-1, 0, 0);
        //uvs
        meshStore.addUV(     0,      0);
        meshStore.addUV( depth,      0);
        meshStore.addUV(     0, quad.h);
        meshStore.addUV( depth, quad.h);
        //samplers
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);

        //
        //face 3
        //

        //verts
        meshStore.addVert(quad.x * finalScalingFactor,            quad.y * finalScalingFactor,            quad.z * finalScalingFactor);
        meshStore.addVert(quad.x * finalScalingFactor,            quad.y * finalScalingFactor,            (quad.z + depth) * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,            quad.y * finalScalingFactor,            quad.z * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,            quad.y * finalScalingFactor,            (quad.z + depth) * finalScalingFactor);
        //indices
        meshStore.addFace(indexOffset + 8, indexOffset + 10, indexOffset + 11);
        meshStore.addFace(indexOffset + 9, indexOffset + 8, indexOffset + 11);
        //normals
        meshStore.addNormal(0, -1, 0);
        meshStore.addNormal(0, -1, 0);
        meshStore.addNormal(0, -1, 0);
        meshStore.addNormal(0, -1, 0);
        //uvs
        meshStore.addUV(     0,      0);
        meshStore.addUV( depth,      0);
        meshStore.addUV(     0, quad.w);
        meshStore.addUV( depth, quad.w);
        //samplers
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);

        //
        //face 4
        //

        //verts
        meshStore.addVert(quad.x * finalScalingFactor,            quad.y * finalScalingFactor,            (quad.z + depth) * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor, quad.y * finalScalingFactor,            (quad.z + depth) * finalScalingFactor);
        meshStore.addVert(quad.x * finalScalingFactor,            (quad.y + quad.h) * finalScalingFactor, (quad.z + depth) * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor, (quad.y + quad.h) * finalScalingFactor, (quad.z + depth) * finalScalingFactor);
        //indices
        meshStore.addFace(indexOffset + 12, indexOffset + 15, indexOffset + 14);
        meshStore.addFace(indexOffset + 12, indexOffset + 13, indexOffset + 15);
        //normals
        meshStore.addNormal(0, 0, 1);
        meshStore.addNormal(0, 0, 1);
        meshStore.addNormal(0, 0, 1);
        meshStore.addNormal(0, 0, 1);
        //uvs
        meshStore.addUV(     0,      0);
        meshStore.addUV(quad.w,      0);
        meshStore.addUV(     0, quad.h);
        meshStore.addUV(quad.w, quad.h);
        //samplers
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);


        //
        //face 5
        //

        //verts
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,            quad.y * finalScalingFactor,            quad.z * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,            quad.y * finalScalingFactor,            (quad.z + depth) * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,            (quad.y + quad.h) * finalScalingFactor, quad.z * finalScalingFactor);
        meshStore.addVert((quad.x + quad.w) * finalScalingFactor,            (quad.y + quad.h) * finalScalingFactor, (quad.z + depth) * finalScalingFactor);
        // verts.add(new Vector3f(quad.x + quad.w,  quad.y,            quad.z        ).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        // verts.add(new Vector3f(quad.x + quad.w,  quad.y,            quad.z + depth).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        // verts.add(new Vector3f(quad.x + quad.w,  quad.y + quad.h,   quad.z        ).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        // verts.add(new Vector3f(quad.x + quad.w,  quad.y + quad.h,   quad.z + depth).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        //indices
        meshStore.addFace(indexOffset + 16, indexOffset + 18, indexOffset + 19);
        meshStore.addFace(indexOffset + 16, indexOffset + 19, indexOffset + 17);
        //normals
        meshStore.addNormal(1, 0, 0);
        meshStore.addNormal(1, 0, 0);
        meshStore.addNormal(1, 0, 0);
        meshStore.addNormal(1, 0, 0);
        //uvs
        meshStore.addUV(     0,      0);
        meshStore.addUV( depth,      0);
        meshStore.addUV(     0, quad.h);
        meshStore.addUV( depth, quad.h);
        //samplers
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);


        //
        //face 6
        //

        //verts
        meshStore.addVert(quad.x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,            (quad.y + quad.h) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,            quad.z * BlockChunkData.BLOCK_SIZE_MULTIPLIER);
        meshStore.addVert(quad.x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,            (quad.y + quad.h) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,            (quad.z + depth) * BlockChunkData.BLOCK_SIZE_MULTIPLIER);
        meshStore.addVert((quad.x + quad.w) * BlockChunkData.BLOCK_SIZE_MULTIPLIER, (quad.y + quad.h) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,            quad.z * BlockChunkData.BLOCK_SIZE_MULTIPLIER);
        meshStore.addVert((quad.x + quad.w) * BlockChunkData.BLOCK_SIZE_MULTIPLIER, (quad.y + quad.h) * BlockChunkData.BLOCK_SIZE_MULTIPLIER,            (quad.z + depth) * BlockChunkData.BLOCK_SIZE_MULTIPLIER);
        // verts.add(new Vector3f(quad.x,           quad.y + quad.h,   quad.z        ).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        // verts.add(new Vector3f(quad.x,           quad.y + quad.h,   quad.z + depth).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        // verts.add(new Vector3f(quad.x + quad.w,  quad.y + quad.h,   quad.z        ).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        // verts.add(new Vector3f(quad.x + quad.w,  quad.y + quad.h,   quad.z + depth).mul(BlockChunkData.BLOCK_SIZE_MULTIPLIER));
        //indicesindexOffset + 
        meshStore.addFace(indexOffset + 20, indexOffset + 23, indexOffset + 22);
        meshStore.addFace(indexOffset + 20, indexOffset + 21, indexOffset + 23);
        //normals
        meshStore.addNormal(0, 1, 0);
        meshStore.addNormal(0, 1, 0);
        meshStore.addNormal(0, 1, 0);
        meshStore.addNormal(0, 1, 0);
        //uvs
        meshStore.addUV(     0,      0);
        meshStore.addUV( depth,      0);
        meshStore.addUV(     0, quad.w);
        meshStore.addUV( depth, quad.w);
        //samplers
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
        meshStore.addCustom2(samplerIndex);
    }



    /**
     * Rasterizes a block chunk data into mesh data
     * @param chunkData The block chunk data
     * @param solids true to rasterize solid blocks, false to rasterize transparent blocks
     * @param solidsMap The solids map
     * @param scalingFactor Scaling applied to the verts
     * @return The mesh data
     */
    public static BlockMeshData rasterize(BlockMeshgenData chunkData, boolean solids, Map<Integer,Boolean> solidsMap, int scalingFactor){
        BlockMeshData rVal = new BlockMeshData();

        //calculate quad meshes
        QuadMeshCache quadMeshCache = QuadMeshCache.getF();
        BlockMeshgen.fillQuadMeshes(quadMeshCache, chunkData, solids, solidsMap);

        //get acceleration structure
        MeshGenStore meshGenStore = MeshGenStore.get();
        meshGenStore.clear();

        //sort
        quadMeshCache.sort();

        int vertCount = 0;
        int faceCount = 0;

        //generate volumes
        QuadMesh quad1 = null;
        QuadMesh quad2 = null;
        int zEnd = 0;
        for(int i = 0; i < quadMeshCache.getActiveCount();){
            quad1 = quadMeshCache.getActive(i);
            zEnd = 1;
            for(int j = i + 1; j < quadMeshCache.getActiveCount(); j++){
                quad2 = quadMeshCache.getActive(j);
                if(quad1.x == quad2.x && quad1.y == quad2.y && quad1.w == quad2.w && quad1.h == quad2.h && quad1.z + zEnd == quad2.z){
                    zEnd++;
                } else {
                    BlockMeshgen.meshifyBox(meshGenStore,quad1,zEnd,quad1.type,scalingFactor);
                    quad1 = quad2;

                    BlockSingleShape blockSingleShape = BlockMeshgen.copyDataToShape(meshGenStore, vertCount, faceCount);
                    vertCount = meshGenStore.getVertCount();
                    faceCount = meshGenStore.getFaceCount();
                    rVal.shapeData.add(blockSingleShape);
                    break;
                }
            }
            i = i + zEnd;
        }
        if(quad1 != null){
            BlockMeshgen.meshifyBox(meshGenStore,quad1,zEnd,quad1.type,scalingFactor);
            BlockSingleShape blockSingleShape = BlockMeshgen.copyDataToShape(meshGenStore, vertCount, faceCount);
            vertCount = meshGenStore.getVertCount();
            faceCount = meshGenStore.getFaceCount();
            rVal.shapeData.add(blockSingleShape);
        }

        //
        //store in flat arrays
        //

        //verts
        rVal.vertices = Arrays.copyOf(meshGenStore.getVertArr(),meshGenStore.getVertCount());
        rVal.vertBuffer = BufferUtils.createFloatBuffer(rVal.vertices.length);
        rVal.vertBuffer.put(rVal.vertices);

        //faces
        rVal.faceElements = Arrays.copyOf(meshGenStore.getFaceArr(),meshGenStore.getFaceCount());
        rVal.faceBuffer = BufferUtils.createIntBuffer(rVal.faceElements.length);
        rVal.faceBuffer.put(rVal.faceElements);

        //normals
        rVal.normals = Arrays.copyOf(meshGenStore.getNormalArr(),meshGenStore.getNormalCount());
        rVal.normalBuffer = BufferUtils.createFloatBuffer(rVal.normals.length);
        rVal.normalBuffer.put(rVal.normals);

        //uvs
        rVal.uvs = Arrays.copyOf(meshGenStore.getUvArr(),meshGenStore.getUvCount());
        rVal.uvBuffer = BufferUtils.createFloatBuffer(rVal.uvs.length);
        rVal.uvBuffer.put(rVal.uvs);

        //samplers
        rVal.samplers = Arrays.copyOf(meshGenStore.getCustArr2(),meshGenStore.getCustCount2());
        rVal.samplerBuffer = BufferUtils.createIntBuffer(rVal.samplers.length);
        rVal.samplerBuffer.put(rVal.samplers);

        MeshGenStore.release(meshGenStore);
        QuadMeshCache.release(quadMeshCache);

        return rVal;
    }

    /**
     * Rasterizes a block chunk data into mesh data
     * @param chunkData The block chunk data
     * @return The mesh data
     */
    public static BlockMeshData rasterize(BlockMeshgenData chunkData){
        return BlockMeshgen.rasterize(chunkData, true, null, BlockMeshgen.DEFAULT_SCALING_FACTOR);
    }

    /**
     * Copies vertex and index data from the combined array into a single shape
     * @param store The mesh generation store
     * @return The data containing a single shape's worth of geometry data
     */
    private static BlockSingleShape copyDataToShape(MeshGenStore store, int vertCount, int faceCount){
        BlockSingleShape blockSingleShape = new BlockSingleShape((store.getVertCount() - vertCount),(store.getFaceCount() - faceCount));
        for(int i = vertCount; i < store.getVertCount(); i++){
            blockSingleShape.vertices[(i - vertCount)] = store.getVert(i);
        }
        for(int i = faceCount; i < store.getFaceCount(); i++){
            blockSingleShape.faceElements[(i - faceCount)] = store.getFace(i);
        }
        return blockSingleShape;
    }

    /**
     * Generates a mesh based on a block mesh data object
     * @param data The block mesh data object
     * @return The mesh
     */
    protected static Mesh generateBlockMesh(OpenGLState openGLState, BlockMeshData meshData){
        Mesh mesh = new Mesh("blockChunk");


        //
        //  VAO
        //
        mesh.generateVAO(openGLState);
        
        
        
        
        FloatBuffer vertexArrayBufferData = meshData.vertBuffer;
        FloatBuffer normalArrayBufferData = meshData.normalBuffer;
        FloatBuffer textureArrayBufferData = meshData.uvBuffer;
        IntBuffer elementArrayBufferData = meshData.faceBuffer;
        IntBuffer samplerArrayBufferData = meshData.samplerBuffer;

        

        
        //
        //  Buffer data to GPU
        //
        int elementCount = meshData.faceElements.length;
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
            if(normalArrayBufferData != null && normalArrayBufferData.position() > 0){
                normalArrayBufferData.flip();
                mesh.bufferNormals(normalArrayBufferData, 3);
            }
            //actually buffer UVs
            if(textureArrayBufferData != null && textureArrayBufferData.position() > 0){
                textureArrayBufferData.flip();
                mesh.bufferTextureCoords(textureArrayBufferData, 2);
            }
            //buffer element indices
            if(elementArrayBufferData.position() > 0){
                elementArrayBufferData.flip();
                mesh.bufferFaces(elementArrayBufferData, elementCount);
            }
            //buffer sampler indices
            if(samplerArrayBufferData != null && samplerArrayBufferData.position() > 0){
                samplerArrayBufferData.flip();
                mesh.bufferCustomIntAttribArray(samplerArrayBufferData, SAMPLER_DATA_SIZE, SAMPLER_SHADER_POSITION);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }

        //bounding sphere logic
        int distance = BlockChunkData.CHUNK_DATA_WIDTH / 2;
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
     * Generates the model for the block mesh
     * @param chunkData The mesh data
     * @return The model object
     */
    public static Model generateBlockModel(BlockMeshData meshData){
        Model rVal = new Model();
        Mesh m = BlockMeshgen.generateBlockMesh(Globals.renderingEngine.getOpenGLState(), meshData);
        
        //construct the material for the chunk
        Material groundMat = Material.create(Globals.blockTextureAtlas.getSpecular(), Globals.blockTextureAtlas.getNormal());
        m.setMaterial(groundMat);

        //shader logic
        m.setShader(Globals.blockShader);
        m.setParent(rVal);
        
        rVal.addMesh(m);

        return rVal;
    }
    
    /**
     * Contains the geom data for a single shape in the block mesh
     */
    public static class BlockSingleShape implements TriGeomData {

        /**
         * The verts
         */
        float[] vertices;
        
        /**
         * The faces
         */
        int[] faceElements;

        /**
         * Constructor
         * @param vertCount The number of verts
         * @param faceCount The number of faces
         */
        public BlockSingleShape(int vertCount, int faceCount){
            vertices = new float[vertCount * 3];
            faceElements = new int[faceCount];
        }

        @Override
        public float[] getVertices() {
            return vertices;
        }

        @Override
        public int[] getFaceElements() {
            return faceElements;
        }

    }

    /**
     * Contains the geom data for a single shape in the block mesh
     */
    public static class BlockDims {

        /**
         * Width of the box
         */
        private int width;

        /**
         * Height of the box
         */
        private int height;

        /**
         * Length of the box
         */
        private int length;

        /**
         * X coordinate of the box
         */
        private int x;

        /**
         * Y coordinate of the box
         */
        private int y;

        /**
         * Z coordinate of the box
         */
        private int z;

        /**
         * Constructor
         * @param width Width of the box
         * @param height Height of the box
         * @param length Length of the box
         * @param x x coordinate of the box
         * @param y y coordinate of the box
         * @param z z coordinate of the box
         */
        public BlockDims(int width, int height, int length, int x, int y, int z){
            this.width = width;
            this.height = height;
            this.length = length;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Gets the width of the box
         * @return The width of the box
         */
        public int getWidth(){
            return width;
        }

        /**
         * Gets the height of the box
         * @return The height of the box
         */
        public int getHeight(){
            return height;
        }

        /**
         * Gets the length of the box
         * @return The length of the box
         */
        public int getLength(){
            return length;
        }

        /**
         * Gets the x coordinate of the box
         * @return The x coordinate
         */
        public int getX(){
            return x;
        }

        /**
         * Gets the y coordinate of the box
         * @return The y coordinate
         */
        public int getY(){
            return y;
        }

        /**
         * Gets the z coordinate of the box
         * @return The z coordinate
         */
        public int getZ(){
            return z;
        }

    }

    /**
     * The final rasterization data that is emitted
     */
    public static class BlockMeshData implements TriGeomData, MultiShapeTriGeomData {

        /**
         * Vertex data in array form
         */
        float[] vertices;

        /**
         * Normal data in array form
         */
        float[] normals;

        /**
         * Face data in array form
         */
        int[] faceElements;

        /**
         * UV data in array form
         */
        float[] uvs;

        /**
         * Sampler data in array form
         */
        int[] samplers;

        /**
         * Data broken out by each shape
         */
        List<TriGeomData> shapeData = new LinkedList<TriGeomData>();

        /**
         * Buffer of vertex data
         */
        FloatBuffer vertBuffer;

        /**
         * Buffer of normal data
         */
        FloatBuffer normalBuffer;

        /**
         * Buffer of face data
         */
        IntBuffer faceBuffer;

        /**
         * Buffer of UV data
         */
        FloatBuffer uvBuffer;

        /**
         * Buffer of sampler data
         */
        IntBuffer samplerBuffer;

        @Override
        public float[] getVertices() {
            return vertices;
        }

        @Override
        public int[] getFaceElements() {
            return faceElements;
        }

        @Override
        public Collection<TriGeomData> getData() {
            return shapeData;
        }
    }
    
}
