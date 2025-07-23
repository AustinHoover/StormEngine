package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.VisualShader;

/**
 * Generates core engine models
 */
public class EngineMeshgen {
    
    public static int createScreenTextureVAO(OpenGLState openGLState){
        int rVal = GL45.glGenVertexArrays();
        openGLState.glBindVertexArray(rVal);
        //vertices
        FloatBuffer vertexArrayBufferData = BufferUtils.createFloatBuffer(12);
        vertexArrayBufferData.put(-1.0f);
        vertexArrayBufferData.put( 1.0f);
        
        vertexArrayBufferData.put(-1.0f);
        vertexArrayBufferData.put(-1.0f);
        
        vertexArrayBufferData.put( 1.0f);
        vertexArrayBufferData.put(-1.0f);
        
        vertexArrayBufferData.put(-1.0f);
        vertexArrayBufferData.put( 1.0f);
        
        vertexArrayBufferData.put( 1.0f);
        vertexArrayBufferData.put(-1.0f);
        
        vertexArrayBufferData.put( 1.0f);
        vertexArrayBufferData.put( 1.0f);
        vertexArrayBufferData.flip();
        int vertexBuffer = GL45.glGenBuffers();
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, vertexBuffer);
        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, vertexArrayBufferData, GL45.GL_STATIC_DRAW);
        GL45.glVertexAttribPointer(0, 2, GL45.GL_FLOAT, false, 0, 0);
        GL45.glEnableVertexAttribArray(0);
        
        
        
        
        
        //texture coords
        FloatBuffer textureArrayBufferData = BufferUtils.createFloatBuffer(12);
        textureArrayBufferData.put(0.0f);
        textureArrayBufferData.put(1.0f);
        
        textureArrayBufferData.put(0.0f);
        textureArrayBufferData.put(0.0f);
        
        textureArrayBufferData.put(1.0f);
        textureArrayBufferData.put(0.0f);
        
        textureArrayBufferData.put(0.0f);
        textureArrayBufferData.put(1.0f);
        
        textureArrayBufferData.put(1.0f);
        textureArrayBufferData.put(0.0f);
        
        textureArrayBufferData.put(1.0f);
        textureArrayBufferData.put(1.0f);
        textureArrayBufferData.flip();
        int textureCoordBuffer = GL45.glGenBuffers();
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, textureCoordBuffer);
        GL45.glBufferData(GL45.GL_ARRAY_BUFFER, textureArrayBufferData, GL45.GL_STATIC_DRAW);
        GL45.glVertexAttribPointer(1, 2, GL45.GL_FLOAT, false, 0, 0);
        GL45.glEnableVertexAttribArray(1);
        
        return rVal;
    }
    
    
    
    
    public static Model createParticleModel(){
        Model particleModel = new Model();
        
        
        Mesh particleMesh = new Mesh("particleBillboard");
        
        
        //
        //  VAO
        //
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        particleMesh.generateVAO(openGLState);
        
        
        
        
        
        
        
        
        
        
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
            particleMesh.bufferVertices(vertexArrayBufferData, 3);
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
        particleMesh.bufferFaces(elementArrayBufferData,elementCount);
        
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
        particleMesh.bufferTextureCoords(texture_coords, 2);
        
        
        
        VisualShader shader = VisualShader.smartAssembleOITProgram();
        particleMesh.setShader(shader);
        
        
        
        
        openGLState.glBindVertexArray(0);
        
        
        
        
        particleMesh.setMaterial(Material.createExisting(AssetDataStrings.TEXTURE_PARTICLE));
        
        
        
        
        particleMesh.setParent(particleModel);
        
        
        particleModel.addMesh(particleMesh);
        
        
        return particleModel;
    }


    @Deprecated
    public static Model createBitmapDisplay(){
        
        Model rVal = new Model();
        Mesh m = new Mesh(AssetDataStrings.ASSET_STRING_BITMAP_FONT_MESH_NAME);
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        m.generateVAO(openGLState);
        //vertices
        FloatBuffer vertexArrayBufferData = BufferUtils.createFloatBuffer(12);
        vertexArrayBufferData.put( 0);
        vertexArrayBufferData.put( 1);
        
        vertexArrayBufferData.put( 0);
        vertexArrayBufferData.put( 0);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put( 0);
        
        vertexArrayBufferData.put( 0);
        vertexArrayBufferData.put( 1);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put( 0);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.flip();
        
        
        IntBuffer faceArrayBufferData = BufferUtils.createIntBuffer(6);
        faceArrayBufferData.put(0);
        faceArrayBufferData.put(1);
        faceArrayBufferData.put(2);
        
        faceArrayBufferData.put(3);
        faceArrayBufferData.put(4);
        faceArrayBufferData.put(5);
        faceArrayBufferData.flip();
        
        
        
        //texture coords
        FloatBuffer textureArrayBufferData = BufferUtils.createFloatBuffer(12);
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(1);
        
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(1);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(1);
        textureArrayBufferData.flip();
        
        
        //buffer vertices
        m.bufferVertices(vertexArrayBufferData, 2);
        //buffer normals
        m.bufferNormals(vertexArrayBufferData, 2);
        //buffer faces
        m.bufferFaces(faceArrayBufferData, 2);
        //buffer texture coords
        m.bufferTextureCoords(textureArrayBufferData, 2);
        
        
        m.setShader(VisualShader.loadSpecificShader("/Shaders/ui/font/basicbitmap/basicbitmap.vs", "/Shaders/ui/font/basicbitmap/basicbitmap.fs"));
        
        
        openGLState.glBindVertexArray(0);
        m.setParent(rVal);
        
        Material uiMat = Material.create("/Textures/Fonts/myfont1-harsher.png");
        m.setMaterial(uiMat);
        rVal.getMaterials().add(uiMat);
        
        rVal.addMesh(m);
        
        return rVal;
    }
    
    
    
    
    
    /**
     * Creates a model to use to show bitmap characters
     * @return The model
     */
    public static Model createBitmapCharacter(){
        
        Model rVal = new Model();
        Mesh m = new Mesh(AssetDataStrings.ASSET_STRING_BITMAP_FONT_MESH_NAME);
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        m.generateVAO(openGLState);
        //vertices
        FloatBuffer vertexArrayBufferData = BufferUtils.createFloatBuffer(12);
        vertexArrayBufferData.put(-1);
        vertexArrayBufferData.put( 1);
        
        vertexArrayBufferData.put(-1);
        vertexArrayBufferData.put(-1);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put(-1);
        
        vertexArrayBufferData.put(-1);
        vertexArrayBufferData.put( 1);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put(-1);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.flip();
        
        
        IntBuffer faceArrayBufferData = BufferUtils.createIntBuffer(6);
        faceArrayBufferData.put(0);
        faceArrayBufferData.put(1);
        faceArrayBufferData.put(2);
        
        faceArrayBufferData.put(3);
        faceArrayBufferData.put(4);
        faceArrayBufferData.put(5);
        faceArrayBufferData.flip();
        
        
        
        //texture coords
        FloatBuffer textureArrayBufferData = BufferUtils.createFloatBuffer(12);
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(1);
        
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(1);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(1);
        textureArrayBufferData.flip();
        
        
        //buffer vertices
        m.bufferVertices(vertexArrayBufferData, 2);
        //buffer normals
        m.bufferNormals(vertexArrayBufferData, 2);
        //buffer faces
        m.bufferFaces(faceArrayBufferData, 6);
        //buffer texture coords
        m.bufferTextureCoords(textureArrayBufferData, 2);
        
        
        m.setShader(VisualShader.loadSpecificShader("/Shaders/ui/font/bitmapchar/bitmapchar.vs", "/Shaders/ui/font/bitmapchar/bitmapchar.fs"));
        
        
        openGLState.glBindVertexArray(0);
        
        rVal.addMesh(m);
        
        return rVal;
    }



    public static Model createInWindowPanel(String vertexShader, String fragmentShader){
        
        Model rVal = new Model();
        Mesh m = new Mesh("plane");
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        m.generateVAO(openGLState);
        //vertices
        FloatBuffer vertexArrayBufferData = BufferUtils.createFloatBuffer(12);
        vertexArrayBufferData.put(-1);
        vertexArrayBufferData.put( 1);
        
        vertexArrayBufferData.put(-1);
        vertexArrayBufferData.put(-1);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put(-1);
        
        vertexArrayBufferData.put(-1);
        vertexArrayBufferData.put( 1);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put(-1);
        
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.put( 1);
        vertexArrayBufferData.flip();
        
        
        IntBuffer faceArrayBufferData = BufferUtils.createIntBuffer(6);
        faceArrayBufferData.put(0);
        faceArrayBufferData.put(1);
        faceArrayBufferData.put(2);
        
        faceArrayBufferData.put(3);
        faceArrayBufferData.put(4);
        faceArrayBufferData.put(5);
        faceArrayBufferData.flip();
        
        
        
        //texture coords
        FloatBuffer textureArrayBufferData = BufferUtils.createFloatBuffer(12);
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(1);
        
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(0);
        textureArrayBufferData.put(1);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(0);
        
        textureArrayBufferData.put(1);
        textureArrayBufferData.put(1);
        textureArrayBufferData.flip();
        
        
        //buffer vertices
        m.bufferVertices(vertexArrayBufferData, 2);
        //buffer normals
        m.bufferNormals(vertexArrayBufferData, 2);
        //buffer faces
        m.bufferFaces(faceArrayBufferData, 2);
        //buffer texture coords
        m.bufferTextureCoords(textureArrayBufferData, 2);
        
        
        m.setShader(VisualShader.loadSpecificShader(vertexShader, fragmentShader));
        
        
        openGLState.glBindVertexArray(0);
        
        rVal.addMesh(m);
        
        return rVal;
    }

}
