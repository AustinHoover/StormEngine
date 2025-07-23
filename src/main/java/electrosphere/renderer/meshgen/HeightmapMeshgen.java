package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.model.Model;
import electrosphere.renderer.shader.VisualShader;

public class HeightmapMeshgen {
    
    public static Model createTerrainModelPrecomputedShader(OpenGLState openGLState, float[][] heightfield, float[][] texturemap, VisualShader program, int stride){
        Model rVal = new Model();
        Mesh m = new Mesh("terrain");
        int width = heightfield.length;
        int height = heightfield[0].length;
        
        int actualWidth = (int)Math.ceil(1.0f * width / (1.0f * stride));
        int actualHeight = (int)Math.ceil(1.0f * height / (1.0f * stride));
        
        
//        System.out.println(actualWidth + " " + actualHeight);
        
//        System.out.println((actualWidth - 1) * (actualHeight - 1));
        
        FloatBuffer vertices;
        FloatBuffer normals;
        IntBuffer faces;
        FloatBuffer texture_coords;
        FloatBuffer textureIndices;
        if(stride * actualWidth > width){
            int drawWidth = actualWidth + 1;
            int drawHeight = actualHeight + 1;
            vertices = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 12);
            normals = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 12);
            faces = BufferUtils.createIntBuffer((drawWidth - 1) * (drawHeight - 1) * 2 * 3);
            texture_coords = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 8);
            textureIndices = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 16);
        } else {
            vertices = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 12);
            normals = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 12);
            faces = BufferUtils.createIntBuffer((actualWidth - 1) * (actualHeight - 1) * 2 * 3);
            texture_coords = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 8);
            textureIndices = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 16);
        }
        
        int incrementer = 0;
        // int numFaces = (actualWidth - 1) * (actualHeight - 1) * 2 * 3;
        for(int x = 0; x < width - 1; x = x + stride){
            for(int y = 0; y < height - 1; y = y + stride){
                //deal with vertex
                //0,0
                vertices.put(x);
                vertices.put(heightfield[x][y]);
                vertices.put(y);
                //1,0
                vertices.put(x + stride);
                vertices.put(heightfield[x+stride][y]);
                vertices.put(y);
                //0,1
                vertices.put(x);
                vertices.put(heightfield[x][y+stride]);
                vertices.put(y + stride);
                //1,1
                vertices.put(x + stride);
                vertices.put(heightfield[x+stride][y+stride]);
                vertices.put(y + stride);
                //deal with normal
                Vector3f normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x, y);
                normals.put(normal.x);
                normals.put(normal.y);
                normals.put(normal.z);
                normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x + stride, y);
                normals.put(normal.x);
                normals.put(normal.y);
                normals.put(normal.z);
                normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x, y + stride);
                normals.put(normal.x);
                normals.put(normal.y);
                normals.put(normal.z);
                normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x + stride, y + stride);
                normals.put(normal.x);
                normals.put(normal.y);
                normals.put(normal.z);
                //deal with texture coordinates
//                if(x / stride % 2 == 0){
//                    if(y / stride % 2 == 0){
//                        texture_coords.put(0);
//                        texture_coords.put(0);
//                        texture_coords.put(1);
//                        texture_coords.put(0);
//                        texture_coords.put(0);
//                        texture_coords.put(1);
//                        texture_coords.put(1);
//                        texture_coords.put(1);
//                    } else {
//                        texture_coords.put(0);
//                        texture_coords.put(1);
//                    }
//                } else {
//                    if(y / stride % 2 == 0){
//                        texture_coords.put(1);
//                        texture_coords.put(0);
//                    } else {
//                        texture_coords.put(1);
//                        texture_coords.put(1);
//                    }
//                }
                texture_coords.put(0);
                texture_coords.put(0);
                texture_coords.put(1);
                texture_coords.put(0);
                texture_coords.put(0);
                texture_coords.put(1);
                texture_coords.put(1);
                texture_coords.put(1);
                
                if(x + stride < width - 1 && y + stride < height - 1){
//                    texturemap[x+stride][y+stride];
                    for(int i = 0; i < 4 ; i++){
//                        textureIndices.put(1);
//                        textureIndices.put(0);
//                        textureIndices.put(0);
//                        textureIndices.put(0);
                        textureIndices.put(texturemap[x][y]);
                        textureIndices.put(texturemap[x+stride][y]);
                        textureIndices.put(texturemap[x][y+stride]);
                        textureIndices.put(texturemap[x+stride][y+stride]);
                    }
                } else {
                    for(int i = 0; i < 4 ; i++){
                        textureIndices.put(0);
                        textureIndices.put(0);
                        textureIndices.put(0);
                        textureIndices.put(0);
                    }
                }
                
                
                //deal with faces
                if(1.0f * x / stride < actualWidth - 1 && 1.0f * y / stride < actualHeight - 1){
                    faces.put(incrementer * 4 + 0);
                    faces.put(incrementer * 4 + 1);
                    faces.put(incrementer * 4 + 2);
                    faces.put(incrementer * 4 + 1);
                    faces.put(incrementer * 4 + 2);
                    faces.put(incrementer * 4 + 3);
                }
                incrementer++;
            }
        }
        
        vertices.flip();
        normals.flip();
        faces.flip();
        texture_coords.flip();
        textureIndices.flip();
        
        m.generateVAO(openGLState);
        //buffer vertices
        m.bufferVertices(vertices, 3);
        //buffer normals
        m.bufferNormals(normals, 3);
        //buffer faces
        m.bufferFaces(faces,incrementer*2);
        //buffer texture coords
        m.bufferTextureCoords(texture_coords, 2);
        //texture indices
        m.bufferCustomFloatAttribArray(textureIndices, 4, 5);
        m.setShader(program);
        openGLState.glBindVertexArray(0);
        
        Material groundMat = Material.create("/Textures/Ground/Dirt1.png");
        m.setMaterial(groundMat);
        
        rVal.addMesh(m);
        return rVal;
    }
    
    
    static float MINIMIZATION_DIFF_MAX = 0.001f;
    
    public static Model createMinimizedTerrainModelPrecomputedShader(float[][] heightfield, float[][] texturemap, VisualShader program, int stride){
        
        class QuadToGenerate {
            //coords are inclusive
            int startX;
            int endX;
            int startY;
            int endY;
            float min;
            float max;
            float texture;
            boolean homogeneousTexture;
            
            QuadToGenerate(int startX, int startY, int endX, int endY, float diff, float min, float max, boolean homogeneousTexture, float texture){
                this.startX = startX;
                this.startY = startY;
                this.endX = endX;
                this.endY = endY;
                this.min = min;
                this.max = max;
                this.texture = texture;
                this.homogeneousTexture = homogeneousTexture;
            }
            
        }
        
        
        Model rVal = new Model();
        Mesh m = new Mesh("terrain");
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        int width = heightfield.length;
        int height = heightfield[0].length;
        
        int actualWidth = (int)Math.ceil(1.0f * width / (1.0f * stride));
        int actualHeight = (int)Math.ceil(1.0f * height / (1.0f * stride));
        
        
//        System.out.println(actualWidth + " " + actualHeight);
        
//        System.out.println((actualWidth - 1) * (actualHeight - 1));
        
        FloatBuffer vertices;
        FloatBuffer normals;
        IntBuffer faces;
        FloatBuffer texture_coords;
        FloatBuffer textureIndices;
        if(stride * actualWidth > width){
            int drawWidth = actualWidth + 1;
            int drawHeight = actualHeight + 1;
            vertices = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 12);
            normals = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 12);
            faces = BufferUtils.createIntBuffer((drawWidth - 1) * (drawHeight - 1) * 2 * 3);
            texture_coords = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 8);
            textureIndices = BufferUtils.createFloatBuffer(drawWidth * drawHeight * 16);
        } else {
            vertices = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 12);
            normals = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 12);
            faces = BufferUtils.createIntBuffer((actualWidth - 1) * (actualHeight - 1) * 2 * 3);
            texture_coords = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 8);
            textureIndices = BufferUtils.createFloatBuffer(actualWidth * actualHeight * 16);
        }
        
        
        //merge along y
        
        List<QuadToGenerate> firstPhaseQuads = new LinkedList<QuadToGenerate>();
        QuadToGenerate quadCurrent = null;
        float minVal = 0;
        float maxVal = 0;
        for(int x = 0; x < width - 1; x = x + stride){
            quadCurrent = null;
            for(int y = 0; y < height - 1; y = y + stride){
                if((x == 5 && y == 2)){
                    // System.out.println(quadCurrent);
//                    continue;
                }
                if(quadCurrent == null){
                    minVal = 100000000;
                    maxVal = 0;
                    //minval
                    if(heightfield[x][y] < minVal){
                        minVal = heightfield[x][y];
                    }
                    if(heightfield[x+stride][y] < minVal){
                        minVal = heightfield[x+stride][y];
                    }
                    if(heightfield[x][y+stride] < minVal){
                        minVal = heightfield[x][y+stride];
                    }
                    if(heightfield[x+stride][y+stride] < minVal){
                        minVal = heightfield[x+stride][y+stride];
                    }
                    //maxval
                    if(heightfield[x][y] > maxVal){
                        maxVal = heightfield[x][y];
                    }
                    if(heightfield[x+stride][y] > maxVal){
                        maxVal = heightfield[x+stride][y];
                    }
                    if(heightfield[x][y+stride] > maxVal){
                        maxVal = heightfield[x][y+stride];
                    }
                    if(heightfield[x+stride][y+stride] > maxVal){
                        maxVal = heightfield[x+stride][y+stride];
                    }
                    boolean textureMatch = false;
                    float texture = -1;
                    if(x+stride < width - 1 && y+stride < height - 1 && 
                            texturemap[x][y] == texturemap[x+stride][y] &&
                            texturemap[x][y] == texturemap[x][y+stride] &&
                            texturemap[x][y] == texturemap[x+stride][y+stride]){
                        
                        textureMatch = true;
                        texture = texturemap[x][y];
                    } else {
//                        if(x > 8 && (x+stride < width - 1) && (y+stride < height -1)){
//                            System.out.println(
//                                    (x+stride < width - 1) + " " +
//                                    (y+stride < height -1) + " " +
//                                    (texturemap[x][y] == texturemap[x+stride][y]) + " " +
//                            (texturemap[x][y] == texturemap[x][y+stride]) + " " +
//                            (texturemap[x][y] == texturemap[x+stride][y+stride])
//                            );
//                        }
                    }
                    if(textureMatch){
                        quadCurrent = new QuadToGenerate(x,y,x+stride,y+stride,maxVal - minVal,minVal,maxVal,textureMatch,texture);
                    } else {
                        firstPhaseQuads.add(new QuadToGenerate(x,y,x+stride,y+stride,maxVal - minVal,minVal,maxVal,textureMatch,texture));
                        // quadCurrent = null;
                    }
                } else {
                    float newMin = minVal;
                    float newMax = maxVal;
                    //min
                    if(heightfield[x][y+stride] < newMin){
                        newMin = heightfield[x][y+stride];
                    }
                    if(heightfield[x+stride][y+stride] < newMin){
                        newMin = heightfield[x+stride][y+stride];
                    }
                    //max
                    if(heightfield[x][y+stride] > newMax){
                        newMax = heightfield[x][y+stride];
                    }
                    if(heightfield[x+stride][y+stride] > newMax){
                        newMax = heightfield[x+stride][y+stride];
                    }
                    if(y+stride < height - 1 && x+stride < width - 1){
                        if(newMax - newMin < MINIMIZATION_DIFF_MAX &&
                            quadCurrent.texture == texturemap[x+stride][y] &&
                            quadCurrent.texture == texturemap[x       ][y+stride] &&
                            quadCurrent.texture == texturemap[x+stride][y+stride] &&
                            quadCurrent.homogeneousTexture
                                ){
                            //add to quad
                            quadCurrent.endY = y + stride;
                            quadCurrent.min = newMax;
                            quadCurrent.max = newMax;
                        } else {
                            //push quad
                            firstPhaseQuads.add(quadCurrent);
                            firstPhaseQuads.add(new QuadToGenerate(x,y,x+stride,y+stride,maxVal - minVal,minVal,maxVal,false,0));
                            quadCurrent = null;
//                            System.out.println("Push");
                        }
                    } else {
                        if(newMax - newMin < MINIMIZATION_DIFF_MAX){
                            //add to quad
                            quadCurrent.endY = y + stride;
                            quadCurrent.min = newMax;
                            quadCurrent.max = newMax;
                        } else {
                            //push quad that we were building
                            firstPhaseQuads.add(quadCurrent);
                            // quadCurrent = null;
                            //create new quad from what we were just analyzing
                            boolean textureMatch = false;
                            float texture = -1;
                            quadCurrent = new QuadToGenerate(x,y,x+stride,y+stride,maxVal - minVal,minVal,maxVal,textureMatch,texture);
//                            System.out.println("Push");
                        }
                    }
                }
            }
            if(quadCurrent != null){
                firstPhaseQuads.add(quadCurrent);
            }
        }
        
        List<QuadToGenerate> finalQuads = new LinkedList<QuadToGenerate>();
        // for(QuadToGenerate current : firstPhaseQuads){
        //     finalQuads.add(current);
        // }
        
//        System.out.println(finalQuads.size());
        
        //merge along x
        
    //    QuadToGenerate currentQuad = null;
        List<QuadToGenerate> toSkip = new LinkedList<QuadToGenerate>();
       for(QuadToGenerate currentQuad : firstPhaseQuads){
        // toRemove.clear();
            if(toSkip.contains(currentQuad)){
                continue;
            }
            for(QuadToGenerate currentPotentialMatch : firstPhaseQuads){
               if(currentPotentialMatch.startX <= currentQuad.startX){
                   continue;
               }
               if(currentPotentialMatch.startX > currentQuad.endX){
                   break;
               }
               if(currentPotentialMatch.startY != currentQuad.startY){
                   continue;
               }
               if(currentPotentialMatch.endY != currentQuad.endY){
                   continue;
               }
               if(
                   !(currentQuad.homogeneousTexture &&
                   currentPotentialMatch.homogeneousTexture &&
                   currentQuad.texture == currentPotentialMatch.texture)
                   ){
                    continue;
                }
                if(currentQuad.min < currentPotentialMatch.min && currentQuad.max < currentPotentialMatch.max){
                    float min = currentQuad.min;
                    float max = currentPotentialMatch.max;
                    if(max - min < MINIMIZATION_DIFF_MAX){
                        currentQuad.endX = currentPotentialMatch.endX;
                        currentQuad.max = currentPotentialMatch.max;
                        toSkip.add(currentPotentialMatch);
                    }
                } else if(currentQuad.min > currentPotentialMatch.min && currentQuad.max > currentPotentialMatch.max){
                    float min = currentPotentialMatch.min;
                    float max = currentQuad.max;
                    if(max - min < MINIMIZATION_DIFF_MAX){
                        currentQuad.endX = currentPotentialMatch.endX;
                        currentQuad.min = currentPotentialMatch.min;
                        toSkip.add(currentPotentialMatch);
                    }
                } else {
                    if(currentQuad.min < currentPotentialMatch.min){
                        currentQuad.endX = currentPotentialMatch.endX;
                    } else {
                        currentQuad.endX = currentPotentialMatch.endX;
                        currentQuad.min = currentPotentialMatch.min;
                        currentQuad.max = currentPotentialMatch.max;
                    }
                    toSkip.add(currentPotentialMatch);
                }
            }
           finalQuads.add(currentQuad);
       }
    //    for(QuadToGenerate currentIteration : firstPhaseQuads){
    //        if(currentQuad == null){
    //            currentQuad = currentIteration;
    //        } else {
    //            //if should merge:
    //            if(
    //                currentQuad.homogeneousTexture &&
    //                currentIteration.homogeneousTexture &&
    //                currentQuad.texture == currentIteration.texture
    //                ){
    //                 if(currentQuad.min < currentIteration.min && currentQuad.max < currentIteration.max){
    //                     float min = currentQuad.min;
    //                     float max = currentIteration.max;
    //                     if(max - min < MINIMIZATION_DIFF_MAX){
    //                         currentQuad.endX = currentIteration.endX;
    //                         currentQuad.max = currentIteration.max;
    //                     } else {
    //                         finalQuads.add(currentQuad);
    //                         currentQuad = currentIteration;
    //                     }
    //                 } else if(currentQuad.min > currentIteration.min && currentQuad.max > currentIteration.max){
    //                     float min = currentIteration.min;
    //                     float max = currentQuad.max;
    //                     if(max - min < MINIMIZATION_DIFF_MAX){
    //                         currentQuad.endX = currentIteration.endX;
    //                         currentQuad.min = currentIteration.min;
    //                     } else {
    //                         finalQuads.add(currentQuad);
    //                         currentQuad = currentIteration;
    //                     }
    //                 } else {
    //                     if(currentQuad.min < currentIteration.min){
    //                         currentQuad.endX = currentIteration.endX;
    //                     } else {
    //                         currentQuad.endX = currentIteration.endX;
    //                         currentQuad.min = currentIteration.min;
    //                         currentQuad.max = currentIteration.max;
    //                     }
    //                 }
    //             } else {
    //                 finalQuads.add(currentQuad);
    //                 currentQuad = currentIteration;
    //             }
    //        }
    //    }
    //    finalQuads.add(currentQuad);
        
        // for(QuadToGenerate current : finalQuads){
        //     if(current.startX > 0 && current.startY > 0 && current.endX < 99 && current.endY < 99){
        //         System.out.println(current.startX + " " + current.startY + " " + current.endX + " " + current.endY);
        //     }
        // }

        // System.out.println("AAAAAAAAAAAAAAAAAA");
//        System.out.println(finalQuads.size());
        
        int incrementer = 0;
        
        for(QuadToGenerate current : finalQuads){
            //deal with vertex
            //0,0
            vertices.put(current.startX);
            vertices.put(heightfield[current.startX][current.startY]);
            vertices.put(current.startY);
            //1,0
            vertices.put(current.endX);
            vertices.put(heightfield[current.endX][current.startY]);
            vertices.put(current.startY);
            //0,1
            vertices.put(current.startX);
            vertices.put(heightfield[current.startX][current.endY]);
            vertices.put(current.endY);
            //1,1
            vertices.put(current.endX);
            vertices.put(heightfield[current.endX][current.endY]);
            vertices.put(current.endY);
            //deal with normal
            Vector3f normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, current.startX, current.startY);
            normals.put(normal.x);
            normals.put(normal.y);
            normals.put(normal.z);
            normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, current.endX, current.startY);
            normals.put(normal.x);
            normals.put(normal.y);
            normals.put(normal.z);
            normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, current.startX, current.endY);
            normals.put(normal.x);
            normals.put(normal.y);
            normals.put(normal.z);
            normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, current.endX, current.endY);
            normals.put(normal.x);
            normals.put(normal.y);
            normals.put(normal.z);
            //deal with texture coordinates
//                if(x / stride % 2 == 0){
//                    if(y / stride % 2 == 0){
//                        texture_coords.put(0);
//                        texture_coords.put(0);
//                        texture_coords.put(1);
//                        texture_coords.put(0);
//                        texture_coords.put(0);
//                        texture_coords.put(1);
//                        texture_coords.put(1);
//                        texture_coords.put(1);
//                    } else {
//                        texture_coords.put(0);
//                        texture_coords.put(1);
//                    }
//                } else {
//                    if(y / stride % 2 == 0){
//                        texture_coords.put(1);
//                        texture_coords.put(0);
//                    } else {
//                        texture_coords.put(1);
//                        texture_coords.put(1);
//                    }
//                }
            texture_coords.put(0);
            texture_coords.put(0);
            texture_coords.put(current.endX - current.startX);
            texture_coords.put(0);
            texture_coords.put(0);
            texture_coords.put(current.endY - current.startY);
            texture_coords.put(current.endX - current.startX);
            texture_coords.put(current.endY - current.startY);

            if(current.endX < width - 1 && current.endY < height - 1){
//                    texturemap[x+stride][y+stride];
                for(int i = 0; i < 4 ; i++){
//                        textureIndices.put(1);
//                        textureIndices.put(0);
//                        textureIndices.put(0);
//                        textureIndices.put(0);
                    textureIndices.put(texturemap[current.startX][current.startY]);
                    textureIndices.put(texturemap[current.endX][current.startY]);
                    textureIndices.put(texturemap[current.startX][current.endY]);
                    textureIndices.put(texturemap[current.endX][current.endY]);
                }
            } else {
                for(int i = 0; i < 4 ; i++){
                    textureIndices.put(0);
                    textureIndices.put(0);
                    textureIndices.put(0);
                    textureIndices.put(0);
                }
            }


            //deal with faces
            faces.put(incrementer * 4 + 0);
            faces.put(incrementer * 4 + 1);
            faces.put(incrementer * 4 + 2);
            faces.put(incrementer * 4 + 1);
            faces.put(incrementer * 4 + 2);
            faces.put(incrementer * 4 + 3);
            
            incrementer++;
        }
        
        
        
//        int numFaces = (actualWidth - 1) * (actualHeight - 1) * 2 * 3;
//        for(int x = 0; x < width - 1; x = x + stride){
//            for(int y = 0; y < height - 1; y = y + stride){
//                //deal with vertex
//                //0,0
//                vertices.put(x);
//                vertices.put(heightfield[x][y]);
//                vertices.put(y);
//                //1,0
//                vertices.put(x + stride);
//                vertices.put(heightfield[x+stride][y]);
//                vertices.put(y);
//                //0,1
//                vertices.put(x);
//                vertices.put(heightfield[x][y+stride]);
//                vertices.put(y + stride);
//                //1,1
//                vertices.put(x + stride);
//                vertices.put(heightfield[x+stride][y+stride]);
//                vertices.put(y + stride);
//                //deal with normal
//                Vector3f normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x, y);
//                normals.put(normal.x);
//                normals.put(normal.y);
//                normals.put(normal.z);
//                normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x + stride, y);
//                normals.put(normal.x);
//                normals.put(normal.y);
//                normals.put(normal.z);
//                normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x, y + stride);
//                normals.put(normal.x);
//                normals.put(normal.y);
//                normals.put(normal.z);
//                normal = calculateTerrainNormal(heightfield, actualWidth, actualHeight, stride, x + stride, y + stride);
//                normals.put(normal.x);
//                normals.put(normal.y);
//                normals.put(normal.z);
//                //deal with texture coordinates
////                if(x / stride % 2 == 0){
////                    if(y / stride % 2 == 0){
////                        texture_coords.put(0);
////                        texture_coords.put(0);
////                        texture_coords.put(1);
////                        texture_coords.put(0);
////                        texture_coords.put(0);
////                        texture_coords.put(1);
////                        texture_coords.put(1);
////                        texture_coords.put(1);
////                    } else {
////                        texture_coords.put(0);
////                        texture_coords.put(1);
////                    }
////                } else {
////                    if(y / stride % 2 == 0){
////                        texture_coords.put(1);
////                        texture_coords.put(0);
////                    } else {
////                        texture_coords.put(1);
////                        texture_coords.put(1);
////                    }
////                }
//                texture_coords.put(0);
//                texture_coords.put(0);
//                texture_coords.put(1);
//                texture_coords.put(0);
//                texture_coords.put(0);
//                texture_coords.put(1);
//                texture_coords.put(1);
//                texture_coords.put(1);
//                
//                if(x + stride < width - 1 && y + stride < height - 1){
////                    texturemap[x+stride][y+stride];
//                    for(int i = 0; i < 4 ; i++){
////                        textureIndices.put(1);
////                        textureIndices.put(0);
////                        textureIndices.put(0);
////                        textureIndices.put(0);
//                        textureIndices.put(texturemap[x][y]);
//                        textureIndices.put(texturemap[x+stride][y]);
//                        textureIndices.put(texturemap[x][y+stride]);
//                        textureIndices.put(texturemap[x+stride][y+stride]);
//                    }
//                } else {
//                    for(int i = 0; i < 4 ; i++){
//                        textureIndices.put(0);
//                        textureIndices.put(0);
//                        textureIndices.put(0);
//                        textureIndices.put(0);
//                    }
//                }
//                
//                
//                //deal with faces
//                if(1.0f * x / stride < actualWidth - 1 && 1.0f * y / stride < actualHeight - 1){
//                    faces.put(incrementer * 4 + 0);
//                    faces.put(incrementer * 4 + 1);
//                    faces.put(incrementer * 4 + 2);
//                    faces.put(incrementer * 4 + 1);
//                    faces.put(incrementer * 4 + 2);
//                    faces.put(incrementer * 4 + 3);
//                }
//                incrementer++;
//            }
//        }

        // System.out.println(incrementer + " quads");
        
        vertices.flip();
        normals.flip();
        faces.flip();
        texture_coords.flip();
        textureIndices.flip();
        
        m.generateVAO(openGLState);
        //buffer vertices
        m.bufferVertices(vertices, 3);
        //buffer normals
        m.bufferNormals(normals, 3);
        //buffer faces
        m.bufferFaces(faces,incrementer*2);
        //buffer texture coords
        m.bufferTextureCoords(texture_coords, 2);
        //texture indices
        m.bufferCustomFloatAttribArray(textureIndices, 4, 5);
        m.setShader(program);
        openGLState.glBindVertexArray(0);
        
        Material groundMat = Material.create("/Textures/Ground/Dirt1.png");
        m.setMaterial(groundMat);
        
        rVal.addMesh(m);
        return rVal;
    }
    
    static Vector3f calculateTerrainNormal(float[][] heightfield, int actualWidth, int actualHeight, int stride, int x, int y){
        Vector3f rVal = new Vector3f();
        if(x / stride < actualWidth - 1){
            if(y / stride < actualHeight - 1){
                float hL;
                if(x > 0){
                    hL = heightfield[x-1][y];
                } else {
                    hL = heightfield[x][y];
                }
                float hR = heightfield[x+1][y];
                float hD = heightfield[x][y+1];
                float hU;
                if(y > 0){
                    hU = heightfield[x][y-1];
                } else {
                    hU = heightfield[x][y];
                }
                rVal = new Vector3f(hL - hR, 2.0f, hD - hU);
                rVal.normalize();
            } else {
                float hL;
                if(x > 0){
                    hL = heightfield[x-1][y];
                } else {
                    hL = heightfield[x][y];
                }
                float hR = heightfield[x+1][y];
                float hD = heightfield[x][y];
                float hU = heightfield[x][y-1];
                rVal = new Vector3f(hL - hR, 2.0f, hD - hU);
                rVal.normalize();
            }
        } else {
            if(y / stride < actualHeight - 1){
                float hL = heightfield[x-1][y];
                float hR = heightfield[x][y];
                float hD = heightfield[x][y+1];
                float hU;
                if(y > 0){
                    hU = heightfield[x][y-1];
                } else {
                    hU = heightfield[x][y];
                }
                rVal = new Vector3f(hL - hR, 2.0f, hD - hU);
                rVal.normalize();
            } else {
                float hL = heightfield[x-1][y];
                float hR = heightfield[x][y];
                float hD = heightfield[x][y];
                float hU = heightfield[x][y-1];
                rVal = new Vector3f(hL - hR, 2.0f, hD - hU);
                rVal.normalize();
            }
        }
        return rVal;
    }

}
