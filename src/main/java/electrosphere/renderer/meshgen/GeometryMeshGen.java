package electrosphere.renderer.meshgen;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;

import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.model.Material;
import electrosphere.renderer.model.Mesh;
import electrosphere.renderer.shader.VisualShader;

/**
 * Routines for generating meshes of basic geometry
 */
public class GeometryMeshGen {

    /**
     * Name of sphere mesh
     */
    public static final String SPHERE_MESH_NAME = "sphere";
    
    /**
     * Meshes a box
     * @param verts The list of verts to store into
     * @param normals The list of normals to store into
     * @param uvs The list of uvs to store into
     * @param indices The list of indices to store into
     * @param width The width of the box
     * @param height The height of the box
     * @param depth THe depth of the box
     */
    public static Mesh genBox(float width, float height, float depth){
        Mesh mesh = new Mesh("box");
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        mesh.generateVAO(openGLState);
        List<Vector3f> verts = new LinkedList<Vector3f>();
        List<Vector3f> normals = new LinkedList<Vector3f>();
        List<Vector2f> uvs = new LinkedList<Vector2f>();
        List<Integer> indices = new LinkedList<Integer>();
        //
        //face 1
        //

        //verts
        verts.add(new Vector3f(0,            0,            0));
        verts.add(new Vector3f(0 + width,    0,            0));
        verts.add(new Vector3f(0,            0 + height,   0));
        verts.add(new Vector3f(0 + width,    0 + height,   0));
        //indices
        indices.add(0);
        indices.add(2);
        indices.add(3);
        indices.add(0);
        indices.add(3);
        indices.add(1);
        //normals
        normals.add(new Vector3f(0,0,-1));
        normals.add(new Vector3f(0,0,-1));
        normals.add(new Vector3f(0,0,-1));
        normals.add(new Vector3f(0,0,-1));
        //uvs
        uvs.add(new Vector2f(     0,      0));
        uvs.add(new Vector2f(width,      0));
        uvs.add(new Vector2f(     0, height));
        uvs.add(new Vector2f(width, height));

        //
        //face 2
        //

        //verts
        verts.add(new Vector3f(0,  0,            0        ));
        verts.add(new Vector3f(0,  0,            0 + depth));
        verts.add(new Vector3f(0,  0 + height,   0        ));
        verts.add(new Vector3f(0,  0 + height,   0 + depth));
        //indices
        indices.add(4);
        indices.add(7);
        indices.add(6);
        indices.add(4);
        indices.add(5);
        indices.add(7);
        //normals
        normals.add(new Vector3f(-1,0,0));
        normals.add(new Vector3f(-1,0,0));
        normals.add(new Vector3f(-1,0,0));
        normals.add(new Vector3f(-1,0,0));
        //uvs
        uvs.add(new Vector2f(    0,      0));
        uvs.add(new Vector2f(depth,      0));
        uvs.add(new Vector2f(    0, height));
        uvs.add(new Vector2f(depth, height));

        //
        //face 3
        //

        //verts
        verts.add(new Vector3f(0,           0,   0        ));
        verts.add(new Vector3f(0,           0,   0 + depth));
        verts.add(new Vector3f(0 + width,   0,   0        ));
        verts.add(new Vector3f(0 + width,   0,   0 + depth));
        //indices
        indices.add( 8);
        indices.add(10);
        indices.add(11);
        indices.add( 9);
        indices.add( 8);
        indices.add(11);
        //normals
        normals.add(new Vector3f(0,-1,0));
        normals.add(new Vector3f(0,-1,0));
        normals.add(new Vector3f(0,-1,0));
        normals.add(new Vector3f(0,-1,0));
        //uvs
        uvs.add(new Vector2f(    0,      0));
        uvs.add(new Vector2f(depth,      0));
        uvs.add(new Vector2f(    0, width));
        uvs.add(new Vector2f(depth, width));

        //
        //face 4
        //

        //verts
        verts.add(new Vector3f(0,            0,            0 + depth));
        verts.add(new Vector3f(0 + width,    0,            0 + depth));
        verts.add(new Vector3f(0,            0 + height,   0 + depth));
        verts.add(new Vector3f(0 + width,    0 + height,   0 + depth));
        //indices
        indices.add(12);
        indices.add(15);
        indices.add(14);
        indices.add(12);
        indices.add(13);
        indices.add(15);
        //normals
        normals.add(new Vector3f(0,0,1));
        normals.add(new Vector3f(0,0,1));
        normals.add(new Vector3f(0,0,1));
        normals.add(new Vector3f(0,0,1));
        //uvs
        uvs.add(new Vector2f(     0,      0));
        uvs.add(new Vector2f(width,       0));
        uvs.add(new Vector2f(     0, height));
        uvs.add(new Vector2f(width,  height));


        //
        //face 5
        //

        //verts
        verts.add(new Vector3f(0 + width,  0,            0        ));
        verts.add(new Vector3f(0 + width,  0,            0 + depth));
        verts.add(new Vector3f(0 + width,  0 + height,   0        ));
        verts.add(new Vector3f(0 + width,  0 + height,   0 + depth));
        //indices
        indices.add(16);
        indices.add(18);
        indices.add(19);
        indices.add(16);
        indices.add(19);
        indices.add(17);
        //normals
        normals.add(new Vector3f(1,0,0));
        normals.add(new Vector3f(1,0,0));
        normals.add(new Vector3f(1,0,0));
        normals.add(new Vector3f(1,0,0));
        //uvs
        uvs.add(new Vector2f(    0,      0));
        uvs.add(new Vector2f(depth,      0));
        uvs.add(new Vector2f(    0, height));
        uvs.add(new Vector2f(depth, height));


        //
        //face 6
        //

        //verts
        verts.add(new Vector3f(0,           0 + height,   0        ));
        verts.add(new Vector3f(0,           0 + height,   0 + depth));
        verts.add(new Vector3f(0 + width,   0 + height,   0        ));
        verts.add(new Vector3f(0 + width,   0 + height,   0 + depth));
        //indices
        indices.add(20);
        indices.add(23);
        indices.add(22);
        indices.add(20);
        indices.add(21);
        indices.add(23);
        //normals
        normals.add(new Vector3f(0,1,0));
        normals.add(new Vector3f(0,1,0));
        normals.add(new Vector3f(0,1,0));
        normals.add(new Vector3f(0,1,0));
        //uvs
        uvs.add(new Vector2f(    0,      0));
        uvs.add(new Vector2f(depth,      0));
        uvs.add(new Vector2f(    0, width));
        uvs.add(new Vector2f(depth, width));

        

        //store into arrays
        FloatBuffer vertBuffer;
        FloatBuffer normalBuffer;
        FloatBuffer uvBuffer;
        IntBuffer faceBuffer;
        //verts
        float[] vertices = new float[verts.size() * 3];
        for(int i = 0; i < verts.size(); i++){
            Vector3f currentVert = verts.get(i);
            vertices[3 * i + 0] = currentVert.x;
            vertices[3 * i + 1] = currentVert.y;
            vertices[3 * i + 2] = currentVert.z;
        }
        vertBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertBuffer.put(vertices);

        //faces
        int[] faceElements = new int[indices.size()];
        for(int i = 0; i < indices.size(); i++){
            faceElements[i] = indices.get(i);
        }
        faceBuffer = BufferUtils.createIntBuffer(faceElements.length);
        faceBuffer.put(faceElements);

        //normals
        float[] normalsArr = new float[normals.size() * 3];
        for(int i = 0; i < normals.size(); i++){
            Vector3f currentNormal = normals.get(i);
            normalsArr[3 * i + 0] = currentNormal.x;
            normalsArr[3 * i + 1] = currentNormal.y;
            normalsArr[3 * i + 2] = currentNormal.z;
        }
        normalBuffer = BufferUtils.createFloatBuffer(normalsArr.length);
        normalBuffer.put(normalsArr);

        //uvs
        float[] uvsArr = new float[uvs.size() * 2];
        for(int i = 0; i < uvs.size(); i++){
            Vector2f currentUV = uvs.get(i);
            uvsArr[2 * i + 0] = currentUV.x;
            uvsArr[2 * i + 1] = currentUV.y;
        }
        uvBuffer = BufferUtils.createFloatBuffer(uvsArr.length);
        uvBuffer.put(uvsArr);




        //actually store in mesh
        int elementCount = faceElements.length;
        try {
            //actually buffer vertices
            if(vertBuffer.position() > 0){
                vertBuffer.flip();
                mesh.bufferVertices(vertBuffer, 3);
            }
            //actually buffer normals
            if(normalBuffer != null && normalBuffer.position() > 0){
                normalBuffer.flip();
                mesh.bufferNormals(normalBuffer, 3);
            }
            //actually buffer UVs
            if(uvBuffer != null && uvBuffer.position() > 0){
                uvBuffer.flip();
                mesh.bufferTextureCoords(uvBuffer, 2);
            }
            //buffer element indices
            if(faceBuffer.position() > 0){
                faceBuffer.flip();
                mesh.bufferFaces(faceBuffer, elementCount);
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }

        return mesh;
    }

    /**
     * Generates a prism mesh
     * @param points The points of the base of the prism
     * @param height The height of the prism
     * @return The mesh of the prism
     */
    public static Mesh genPrism(Vector3d[] points, double height){
        //error check
        if(points.length < 3){
            throw new Error("Invalid number of points! " + points.length);
        }

        //allocate top level objects
        Mesh mesh = new Mesh("prism");
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        mesh.generateVAO(openGLState);

        //calculate centerpoint
        Vector3d centerpoint = new Vector3d(points[0]);
        for(int i = 1; i < points.length; i++){
            centerpoint = centerpoint.add(points[i]);
        }
        centerpoint = centerpoint.mul(1.0f / (float)points.length);
        

        //allocate buffers
        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer((points.length + 2) * 2 * 3);
        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer((points.length + 2) * 2 * 3);
        FloatBuffer uvBuffer = BufferUtils.createFloatBuffer((points.length + 2) * 2 * 2);
        IntBuffer faceBuffer = BufferUtils.createIntBuffer(points.length * 12);


        //
        //add data for center point
        //
        vertBuffer.put((float)centerpoint.x);
        vertBuffer.put((float)centerpoint.y);
        vertBuffer.put((float)centerpoint.z);
        vertBuffer.put((float)centerpoint.x);
        vertBuffer.put((float)(centerpoint.y + height));
        vertBuffer.put((float)centerpoint.z);
        normalBuffer.put(0f);
        normalBuffer.put(-1f);
        normalBuffer.put(0f);
        normalBuffer.put(0f);
        normalBuffer.put(1f);
        normalBuffer.put(0f);
        uvBuffer.put(0);
        uvBuffer.put(0);
        uvBuffer.put(0);
        uvBuffer.put(0);
        

        //
        //iterate along points
        //
        for(int i = 0; i < points.length - 1; i++){
            Vector3d point1 = points[i];
            //add vert data
            vertBuffer.put((float)point1.x);
            vertBuffer.put((float)point1.y);
            vertBuffer.put((float)point1.z);
            vertBuffer.put((float)point1.x);
            vertBuffer.put((float)(point1.y + height));
            vertBuffer.put((float)point1.z);
            normalBuffer.put(0f);
            normalBuffer.put(-1f);
            normalBuffer.put(0f);
            normalBuffer.put(0f);
            normalBuffer.put(11f);
            normalBuffer.put(0f);
            uvBuffer.put(1);
            uvBuffer.put(1);
            uvBuffer.put(1);
            uvBuffer.put(1);
            int p1index = ((i + 0) * 2) + 2;
            int p2index = ((i + 0) * 2) + 3;
            int p3index = ((i + 1) * 2) + 2;
            int p4index = ((i + 1) * 2) + 3;
            //add face data
            //bottom triangle
            faceBuffer.put(0);
            faceBuffer.put(p1index);
            faceBuffer.put(p3index);
            //top triangle
            faceBuffer.put(1);
            faceBuffer.put(p2index);
            faceBuffer.put(p4index);
            //perimeter face 1
            faceBuffer.put(p2index);
            faceBuffer.put(p1index);
            faceBuffer.put(p3index);
            //perimeter face 2
            faceBuffer.put(p2index);
            faceBuffer.put(p3index);
            faceBuffer.put(p4index);
            // System.out.println(p2index + "--" + p4index);
            // System.out.println("|\\ |");
            // System.out.println("| \\|");
            // System.out.println(p1index + "--" + p3index);
        }

        //
        //Handle wrap-around
        //
        Vector3d lastPoint = points[points.length - 1];
        vertBuffer.put((float)lastPoint.x);
        vertBuffer.put((float)lastPoint.y);
        vertBuffer.put((float)lastPoint.z);
        vertBuffer.put((float)lastPoint.x);
        vertBuffer.put((float)(lastPoint.y + height));
        vertBuffer.put((float)lastPoint.z);
        normalBuffer.put(0f);
        normalBuffer.put(-1f);
        normalBuffer.put(0f);
        normalBuffer.put(0f);
        normalBuffer.put(11f);
        normalBuffer.put(0f);
        uvBuffer.put(1);
        uvBuffer.put(1);
        uvBuffer.put(1);
        uvBuffer.put(1);
        //add face data
        int p1index = (3 * 2) + 2;
        int p2index = (3 * 2) + 3;
        int p3index = (0 * 2) + 2;
        int p4index = (0 * 2) + 3;
        //bottom triangle
        faceBuffer.put(0);
        faceBuffer.put(p1index);
        faceBuffer.put(p3index);
        //top triangle
        faceBuffer.put(1);
        faceBuffer.put(p2index);
        faceBuffer.put(p4index);
        //perimeter face 1
        faceBuffer.put(p2index);
        faceBuffer.put(p1index);
        faceBuffer.put(p3index);
        //perimeter face 2
        faceBuffer.put(p2index);
        faceBuffer.put(p3index);
        faceBuffer.put(p4index);



        //actually store in mesh
        try {
            //actually buffer vertices
            if(vertBuffer.position() > 0){
                vertBuffer.flip();
                mesh.bufferVertices(vertBuffer, 3);
            }
            //actually buffer normals
            if(normalBuffer.position() > 0){
                normalBuffer.flip();
                mesh.bufferNormals(normalBuffer, 3);
            }
            //actually buffer UVs
            if(uvBuffer.position() > 0){
                uvBuffer.flip();
                mesh.bufferTextureCoords(uvBuffer, 2);
            }
            //buffer element indices
            if(faceBuffer.position() > 0){
                faceBuffer.flip();
                mesh.bufferFaces(faceBuffer, faceBuffer.limit());
            }
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }

        //
        // Calculate bounding sphere
        //
        double maxDist = 0;
        for(int i = 0; i < points.length; i++){
            double dist = points[i].distance(centerpoint);
            if(dist > maxDist){
                maxDist = dist;
            }
        }
        mesh.updateBoundingSphere(centerpoint.x, centerpoint.y, centerpoint.z, maxDist + height);

        return mesh;
    }

    /**
     * Generates a unit sphere model
     * @param slices The number of slices of the sphere
     * @param stacks The number of stacks of the sphere
     * @return The model
     */
    public static Mesh genSphere(int slices, int stacks){
        Mesh sphereMesh = new Mesh(GeometryMeshGen.SPHERE_MESH_NAME);
        OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
        sphereMesh.generateVAO(openGLState);
        
        //buffer coords
        ParShapesMesh data = ParShapes.par_shapes_create_parametric_sphere(slices, stacks);
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
            FloatBuffer texCoords = data.tcoords(numPoints * 2);
            FloatBuffer texCoordsFinal = BufferUtils.createFloatBuffer(texCoords.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            texCoordsFinal.put(texCoords);
            texCoordsFinal.flip();
            sphereMesh.bufferTextureCoords(texCoordsFinal, 2);
        }

        //normals
        {
            FloatBuffer normals = data.normals(numPoints * 3);
            FloatBuffer normalsFinal = BufferUtils.createFloatBuffer(normals.limit()); //reallocating to BufferUtils buffer to help minimize memory errors
            normalsFinal.put(normals);
            normalsFinal.flip();
            sphereMesh.bufferNormals(normalsFinal, 3);
        }


        //setup extra structures
        Material mat = Material.createExisting(AssetDataStrings.TEXTURE_TEAL_TRANSPARENT);
        sphereMesh.setMaterial(mat);
        sphereMesh.setShader(VisualShader.smartAssembleShader());

        return sphereMesh;
    }

}
