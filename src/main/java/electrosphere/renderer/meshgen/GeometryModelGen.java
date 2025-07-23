package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.VisualShader;

/**
 * Generates full models of basic geometry
 */
public class GeometryModelGen {



    /**
     * Name of the mesh for the single block model
     */
    public static final String MESH_NAME_BLOCK_SINGLE = "cube";


    


    /**
     * Generates a plane model
     * @param vertexShader The vertex shader
     * @param fragmentShader The fragment shader
     * @return The model
     */
    public static Model createPlaneModel(String vertexShader, String fragmentShader){
        Model rVal = new Model();
        Mesh planeMesh = new Mesh("plane");
        //
        //  VAO
        //
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        planeMesh.generateVAO(openGLState);
        
        float[] vertexcoords = {
            -1.0f,  1.0f, 0.0f,
             1.0f,  1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
             1.0f, -1.0f, 0.0f,
            
        };
        
        //
        //Buffer data to GPU
        //
        
        try {
            int vertexCount = vertexcoords.length / 3;
            FloatBuffer vertexArrayBufferData = BufferUtils.createFloatBuffer(vertexCount * 3);
            float[] temp = new float[3];
            for (int i = 0; i < vertexCount; i++) {
                temp[0] = vertexcoords[i * 3 + 0];
                temp[1] = vertexcoords[i * 3 + 1];
                temp[2] = vertexcoords[i * 3 + 2];
                vertexArrayBufferData.put(temp);
            }
            vertexArrayBufferData.flip();
            planeMesh.bufferVertices(vertexArrayBufferData, 3);
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
        
        int[] facedata = {
            0,1,2,
            1,2,3,
        };
        
        //
        //  FACES
        //
        int faceCount = facedata.length / 3;
        int elementCount = facedata.length;
        IntBuffer elementArrayBufferData = BufferUtils.createIntBuffer(elementCount);
        for(int i = 0; i < faceCount; i++){
            int[] temp = new int[3];
            temp[0] = facedata[i * 3 + 0];
            temp[1] = facedata[i * 3 + 1];
            temp[2] = facedata[i * 3 + 2];
            elementArrayBufferData.put(temp);
        }
        elementArrayBufferData.flip();
        planeMesh.bufferFaces(elementArrayBufferData,elementCount);
        
        //
        //  TEXTURE COORDS
        //
        FloatBuffer texture_coords = BufferUtils.createFloatBuffer(8);
        float[] texturedata = {
            0,1,
            1,1,
            0,0,
            1,0
        };
        texture_coords.put(texturedata);
        texture_coords.flip();
        planeMesh.bufferTextureCoords(texture_coords, 2);
        
        
        planeMesh.setShader(VisualShader.loadSpecificShader(vertexShader,fragmentShader));
        openGLState.glBindVertexArray(0);
        rVal.addMesh(planeMesh);
        return rVal;
    }

    /**
     * Generates a unit sphere model
     * @return The model
     */
    public static Model createUnitSphere(){
        Model model = new Model();
        Mesh sphereMesh = new Mesh("sphere");
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        sphereMesh.generateVAO(openGLState);
        
        //buffer coords
        ParShapesMesh data = ParShapes.par_shapes_create_parametric_sphere(10, 5);
        int numPoints = data.npoints();

        //verts
        {
            FloatBuffer verts = data.points(numPoints * 3);
            FloatBuffer vertsFinal = BufferUtils.createFloatBuffer(verts.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            vertsFinal.put(verts);
            vertsFinal.flip();
            sphereMesh.bufferVertices(vertsFinal, 3);
        }

        //indices
        {
            IntBuffer indices = data.triangles(data.ntriangles() * 3);
            IntBuffer indicesFinal = BufferUtils.createIntBuffer(indices.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            indicesFinal.put(indices);
            indicesFinal.flip();
            sphereMesh.bufferFaces(indicesFinal, data.ntriangles() * 3);
        }
        
        //texture coords
        {
            FloatBuffer texCoords = data.tcoords(numPoints * 3);
            FloatBuffer texCoordsFinal = BufferUtils.createFloatBuffer(texCoords.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            texCoordsFinal.put(texCoords);
            texCoordsFinal.flip();
            sphereMesh.bufferTextureCoords(texCoordsFinal, 2);
        }


        //setup extra structures
        Material mat = Material.createExisting(AssetDataStrings.TEXTURE_TEAL_TRANSPARENT);
        sphereMesh.setMaterial(mat);
        sphereMesh.setShader(VisualShader.smartAssembleShader());
        openGLState.glBindVertexArray(0);
        model.addMesh(sphereMesh);

        return model;
    }

    /**
     * Creates a unit cylinder model
     * @return The model
     */
    public static Model createUnitCylinder(){
        Model model = new Model();
        Mesh sphereMesh = new Mesh("cylinder");
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        sphereMesh.generateVAO(openGLState);

        //buffer coords
        ParShapesMesh data = ParShapes.par_shapes_create_cylinder(10, 2);
        ParShapes.par_shapes_rotate(data, (float)(Math.PI / 2.0), new float[]{-1,0,0});
        ParShapes.par_shapes_translate(data, 0, -0.5f, 0);
        ParShapes.par_shapes_scale(data, -1.0f, 2.0f, 1.0f);
        int numPoints = data.npoints();

        //verts
        {
            FloatBuffer verts = data.points(numPoints * 3);
            FloatBuffer vertsFinal = BufferUtils.createFloatBuffer(verts.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            vertsFinal.put(verts);
            vertsFinal.flip();
            sphereMesh.bufferVertices(vertsFinal, 3);
        }

        //indices
        {
            IntBuffer indices = data.triangles(data.ntriangles() * 3);
            IntBuffer indicesFinal = BufferUtils.createIntBuffer(indices.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            indicesFinal.put(indices);
            indicesFinal.flip();
            sphereMesh.bufferFaces(indicesFinal, data.ntriangles() * 3);
        }
        
        //texture coords
        {
            FloatBuffer texCoords = data.tcoords(numPoints * 3);
            FloatBuffer texCoordsFinal = BufferUtils.createFloatBuffer(texCoords.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            texCoordsFinal.put(texCoords);
            texCoordsFinal.flip();
            sphereMesh.bufferTextureCoords(texCoordsFinal, 2);
        }

        //setup extra structures
        Material mat = Material.createExisting(AssetDataStrings.TEXTURE_TEAL_TRANSPARENT);
        sphereMesh.setMaterial(mat);
        sphereMesh.setShader(VisualShader.smartAssembleShader());
        openGLState.glBindVertexArray(0);
        model.addMesh(sphereMesh);

        return model;
    }

    /**
     * Creates a unit cylinder model
     * @return The model
     */
    public static Model createUnitCube(){
        Model model = new Model();
        Mesh sphereMesh = new Mesh("cube");
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        sphereMesh.generateVAO(openGLState);

        //buffer coords
        int numTriangles = 12;

        //verts
        BufferUtils.createFloatBuffer(3 * 8);
        FloatBuffer verts = BufferUtils.createFloatBuffer(3 * 8);
        verts.put(new float[]{
            -0.5f,-0.5f,-0.5f,
             0.5f,-0.5f,-0.5f,
            -0.5f, 0.5f,-0.5f,
             0.5f, 0.5f,-0.5f,
            
            -0.5f,-0.5f, 0.5f,
             0.5f,-0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
             0.5f, 0.5f, 0.5f,
        });
        verts.flip();
        sphereMesh.bufferVertices(verts, 3);

        //indices
        IntBuffer indices = BufferUtils.createIntBuffer(3*12);
        indices.put(new int[]{
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
        });
        indices.flip();
        sphereMesh.bufferFaces(indices, numTriangles * 3);

        //texture coords
        FloatBuffer texCoords = BufferUtils.createFloatBuffer(2*8);
        texCoords.put(new float[]{
            0,0,
            1,0,
            0,1,
            1,1,

            0,0,
            0,1,
            1,0,
            1,1,
        });
        texCoords.flip();
        sphereMesh.bufferTextureCoords(texCoords, 2);

        //setup extra structures
        Material mat = Material.createExisting(AssetDataStrings.TEXTURE_TEAL_TRANSPARENT);
        sphereMesh.setMaterial(mat);
        sphereMesh.setShader(VisualShader.smartAssembleShader());
        openGLState.glBindVertexArray(0);
        model.addMesh(sphereMesh);

        return model;
    }

    /**
     * Creates a unit cylinder model
     * @return The model
     */
    public static Model createBlockSingleModel(){
        Model model = new Model();
        Mesh cubeMesh = new Mesh(GeometryModelGen.MESH_NAME_BLOCK_SINGLE);
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        cubeMesh.generateVAO(openGLState);

        //buffer coords
        int numTriangles = 12;

        //verts
        BufferUtils.createFloatBuffer(3 * 8);
        FloatBuffer verts = BufferUtils.createFloatBuffer(3 * 8);
        verts.put(new float[]{
            -0.1f,-0.1f,-0.1f,
             0.1f,-0.1f,-0.1f,
            -0.1f, 0.1f,-0.1f,
             0.1f, 0.1f,-0.1f,
            
            -0.1f,-0.1f, 0.1f,
             0.1f,-0.1f, 0.1f,
            -0.1f, 0.1f, 0.1f,
             0.1f, 0.1f, 0.1f,
        });
        verts.flip();
        cubeMesh.bufferVertices(verts, 3);

        //indices
        IntBuffer indices = BufferUtils.createIntBuffer(3*12);
        indices.put(new int[]{
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
        });
        indices.flip();
        cubeMesh.bufferFaces(indices, numTriangles * 3);

        //texture coords
        FloatBuffer texCoords = BufferUtils.createFloatBuffer(2*8);
        texCoords.put(new float[]{
            0,0,
            1,0,
            0,1,
            1,1,

            0,0,
            0,1,
            1,0,
            1,1,
        });
        texCoords.flip();
        cubeMesh.bufferTextureCoords(texCoords, 2);

        //setup extra structures
        Material mat = Material.createExisting(AssetDataStrings.TEXTURE_BLOCK_ATLAS);
        cubeMesh.setMaterial(mat);
        cubeMesh.setShader(VisualShader.loadSpecificShader(AssetDataStrings.SHADER_BLOCK_SINGLE_VERT, AssetDataStrings.SHADER_BLOCK_SINGLE_FRAG));
        openGLState.glBindVertexArray(0);
        model.addMesh(cubeMesh);

        return model;
    }

}
