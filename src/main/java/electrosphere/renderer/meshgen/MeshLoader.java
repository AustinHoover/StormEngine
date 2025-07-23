package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

import org.joml.Matrix4d;
import org.joml.Vector4d;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAABB;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVertexWeight;

import electrosphere.engine.EngineState;
import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.loading.ModelPretransforms;
import electrosphere.renderer.model.Bone;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.shader.VisualShader;

/**
 * Main class for loading meshes from assimp scenes
 */
public class MeshLoader {

    public static Mesh createMeshFromAIScene(AIMesh mesh, ModelPretransforms.MeshMetadata metadata){
        Mesh rVal = new Mesh(mesh.mName().dataString());
        
        //
        //  VAO
        //
        //Check for headless to not call gl functions when not running with gpu
        if(!EngineState.EngineFlags.HEADLESS){
            OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
            rVal.generateVAO(openGLState);
        }
        

        
        //Basic checks
        //check num vertices
        int numVertices = mesh.mNumVertices();
        AIVector3D.Buffer vertexData = mesh.mVertices();
        // while(vertexData.hasRemaining()){
        //     vertexData.get();
        //     numVertices++;
        // }
        // vertexData = vertexData.rewind();
        //check num normals
        int numNormals = mesh.mNumVertices();
        // AIVector3D.Buffer normalData = mesh.mNormals();
        // while(normalData.hasRemaining()){
        //     normalData.get();
        //     numNormals++;
        // }
        // normalData.rewind();
        if(numVertices != numNormals){
            LoggerInterface.loggerNetworking.ERROR("Catastrophic failure: Number of vertices =/= Number of normals", new Exception("Catastrophic failure: Number of vertices =/= Number of normals"));
        }
        
        
        
        
        
        
        
        
        Matrix4d vertexPretransform = new Matrix4d().identity();
        Matrix4d normalPretransform = new Matrix4d().identity();
        if(metadata != null){
            LoggerInterface.loggerRenderer.DEBUG("Pretransforming");
            vertexPretransform.translationRotateScale(metadata.getOffset(), metadata.getRotation(), metadata.getScale());
            normalPretransform.rotate(metadata.getRotation());
        }
        
        //
        //Buffer data to GPU
        //
        vertexData.rewind();
        
        int vertexCount = 0;
        try {
            vertexCount = mesh.mNumVertices();
            FloatBuffer vertexArrayBufferData = BufferUtils.createFloatBuffer(vertexCount * 3);
            float[] temp = new float[3];
            boolean definedDimensions = false;
            float minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;
            for (int i = 0; i < vertexCount; i++) {
                AIVector3D vertex = vertexData.get();
                float x = vertex.x();
                float y = vertex.y();
                float z = vertex.z();
                //store dimensions of the model
                if(definedDimensions){
                    if(x < minX){ minX = x; }
                    if(x > maxX){ maxX = x; }
                    if(y < minY){ minY = y; }
                    if(y > maxY){ maxY = y; }
                    if(z < minZ){ minZ = z; }
                    if(z > maxZ){ maxZ = z; }
                } else {
                    definedDimensions = true;
                    minX = maxX = x;
                    minY = maxY = y;
                    minZ = maxZ = z;
                }
                //store vertex data
                Vector4d transformedVertex = vertexPretransform.transform(new Vector4d(x,y,z,1.0));
                transformedVertex.w = 1.0;
                temp[0] = (float)transformedVertex.x;
                temp[1] = (float)transformedVertex.y;
                temp[2] = (float)transformedVertex.z;
                vertexArrayBufferData.put(temp);
            }
            if(vertexArrayBufferData.position() > 0){
                vertexArrayBufferData.flip();
                rVal.bufferVertices(vertexArrayBufferData, 3);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
        //
        //  NORMALS
        //
        AIVector3D.Buffer normals = mesh.mNormals();
        try {
            int normalCount = mesh.mNumVertices();
            FloatBuffer normalArrayBufferData;
            if(normalCount > 0){
                normalArrayBufferData = BufferUtils.createFloatBuffer(normalCount * 3);
                float[] temp = new float[3];
                for (int i = 0; i < normalCount; i++) {
                    AIVector3D normal = normals.get(i);
                    Vector4d transformedNormal = normalPretransform.transform(new Vector4d(normal.x(),normal.y(),normal.z(),1.0));
                    temp[0] = (float)transformedNormal.x();
                    temp[1] = (float)transformedNormal.y();
                    temp[2] = (float)transformedNormal.z();
                    normalArrayBufferData.put(temp);
                }
                if(normalArrayBufferData.position() > 0){
                    normalArrayBufferData.flip();
                    rVal.bufferNormals(normalArrayBufferData, 3);
                }
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
        //
        //  FACES
        //
        int faceCount = mesh.mNumFaces();
        int elementCount = faceCount * 3;
        IntBuffer elementArrayBufferData = BufferUtils.createIntBuffer(elementCount);
        AIFace.Buffer facesBuffer = mesh.mFaces();
        for(int i = 0; i < faceCount; i++){
            AIFace face = facesBuffer.get(i);
            if(face.mNumIndices() != 3){
                throw new IllegalStateException("AIFace.mNumIndices() != 3");
            }
            elementArrayBufferData.put(face.mIndices());
        }
        if(elementArrayBufferData.position() > 0){
            elementArrayBufferData.flip();
            rVal.bufferFaces(elementArrayBufferData,elementCount);
        }
        
        
        
        
        
        //
        //  TEXTURE COORDINATES
        //
        if(mesh.mTextureCoords().capacity() > 0){
            AIVector3D.Buffer texturecoords = mesh.mTextureCoords(0);
            try {
                if(texturecoords != null){
                    int textureCoordCount = texturecoords.capacity();
                    FloatBuffer textureArrayBufferData;
                    if(textureCoordCount > 0){
                        textureArrayBufferData = BufferUtils.createFloatBuffer(textureCoordCount * 2);
                        float[] temp = new float[2];
                        for (int i = 0; i < textureCoordCount; i++) {
                            AIVector3D normal = texturecoords.get(i);
                            temp[0] = normal.x();
                            temp[1] = normal.y();
    //                        temp[2] = normal.z();
                            textureArrayBufferData.put(temp);
                        }
                        if(textureArrayBufferData.position() > 0){
                            textureArrayBufferData.flip();
                            rVal.bufferTextureCoords(textureArrayBufferData, 2);
                        }
                    }
                }
            } catch (NullPointerException ex){
                LoggerInterface.loggerRenderer.ERROR("Error reading texture coordinates", ex);
            }
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        //
        //Read in bones
        //AND buffer data (weights) to GPU
        //
        PointerBuffer boneBuffer = mesh.mBones();
        if(boneBuffer != null){
            while(boneBuffer.hasRemaining()){
                long currentAddr = boneBuffer.get();
                AIBone currentBoneData = AIBone.createSafe(currentAddr);
//                System.out.println("Num weights: " + currentBoneData.mNumWeights());
                Bone currentBone = new Bone(currentBoneData);
                currentBone.boneID = currentBoneData.mName().dataString();
                currentBone.setMOffset(electrosphere.util.Utilities.convertAIMatrixd(currentBoneData.mOffsetMatrix()));
                Iterator<AIVertexWeight> weightIterator = currentBoneData.mWeights().iterator();
                while(weightIterator.hasNext()){
                    AIVertexWeight currentWeightData = weightIterator.next();
                    currentBone.putWeight(currentWeightData.mVertexId(), currentWeightData.mWeight());
                }
                rVal.getBones().add(currentBone);
                rVal.registerBoneId(currentBone.boneID);
            }
            FloatBuffer boneWeightDataBuffer = BufferUtils.createFloatBuffer(4 * vertexCount);//FloatBuffer.allocate(4 * vertexCount);
            FloatBuffer boneIndexDataBuffer = BufferUtils.createFloatBuffer(4 * vertexCount);//IntBuffer.allocate(4 * vertexCount);
            Iterator<Bone> boneIterator;
            for(int i = 0; i < vertexCount; i++){
                float[] weight = new float[4];
                float[] index = new float[4];
                int boneCounter = 0;
                boneIterator = rVal.getBones().iterator();
                while(boneIterator.hasNext()){
                    Bone currentBone = boneIterator.next();
                    float boneVal = 0;
                    if(currentBone.getWeights().get(i) != null){
                        boneVal = currentBone.getWeights().get(i);
                    }
                    if(boneVal > 0){
                        if(boneVal > weight[0]){
                            weight[3] = weight[2];
                            weight[2] = weight[1];
                            weight[1] = weight[0];
                            weight[0] = boneVal;
                            index[3] = index[2];
                            index[2] = index[1];
                            index[1] = index[0];
                            index[0] = boneCounter;
                            // if(rVal.nodeID.equals("Torso")){
                            //     System.out.println(index[3] + " " + index[2] + " " + index[1] + " " + index[0]);
                            // }
                        } else if(boneVal > weight[1]){
                            weight[3] = weight[2];
                            weight[2] = weight[1];
                            weight[1] = boneVal;
                            index[3] = index[2];
                            index[2] = index[1];
                            index[1] = boneCounter;
                        } else if(boneVal > weight[2]){
                            weight[3] = weight[2];
                            weight[2] = boneVal;
                            index[3] = index[2];
                            index[2] = boneCounter;
                        } else if(boneVal > weight[3]){
                            weight[3] = boneVal;
                            index[3] = boneCounter;
                        }
                    }
                    boneCounter++;
                }
                float total = weight[0] + weight[1] + weight[2] + weight[3];
                if(total != 1.0f){
                    weight[0] = weight[0] * (1.0f / total);
                    weight[1] = weight[1] * (1.0f / total);
                    weight[2] = weight[2] * (1.0f / total);
                    weight[3] = weight[3] * (1.0f / total);
                }
                //If all are 0 (for instance the vertex doesn't have any bones with any weight > 0), the values for each weight will be NaN after the divide immediately above
                //If NaN, set all to 0
                if(Float.isNaN(weight[0])){
                    weight[0] = 0;
                    weight[1] = 0;
                    weight[2] = 0;
                    weight[3] = 0;
                }
                boneIndexDataBuffer.put(index);
                boneWeightDataBuffer.put(weight);
            }
            if(boneIndexDataBuffer.position() > 0){
                boneIndexDataBuffer.flip();
            }
            if(boneWeightDataBuffer.position() > 0){
                boneWeightDataBuffer.flip();
            }
            
            if(!EngineState.EngineFlags.HEADLESS){
                rVal.bufferBoneIndices(boneIndexDataBuffer);

                rVal.bufferBoneWeights(boneWeightDataBuffer);
            }
        }
        
        
        
        
        
        //bounding sphere work
        {
            AIAABB aabbData = mesh.mAABB();
            AIVector3D max = aabbData.mMax();
            AIVector3D min = aabbData.mMin();
            double dist = new Vector3d(max.x(),max.y(),max.z()).distance(new Vector3d(min.x(),min.y(),min.z()));
            rVal.getBoundingSphere().r = dist / 2.0;
        }
        
        
        
        
        
        if(!EngineState.EngineFlags.HEADLESS){
            rVal.setShader(VisualShader.smartAssembleShader());
            rVal.setShader(VisualShader.smartAssembleShader());
            rVal.setOITShader(VisualShader.smartAssembleOITProgram());
        }
        
        
        
        return rVal;
    }
    
}
