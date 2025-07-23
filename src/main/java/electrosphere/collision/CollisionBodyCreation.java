package electrosphere.collision;

import java.nio.IntBuffer;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.ode4j.math.DMatrix3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DCapsule;
import org.ode4j.ode.DCylinder;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DSphere;
import org.ode4j.ode.DTriMesh;

import electrosphere.entity.state.collidable.MultiShapeTriGeomData;
import electrosphere.entity.state.collidable.TriGeomData;

/**
 * Utilities for creating types of rigid bodies
 */
public class CollisionBodyCreation {

    //Matrix for correcting initial axis of eg cylinders or capsules
    //this rotates by 90 degrees along the x axis
    public static final DMatrix3 AXIS_CORRECTION_MATRIX = new DMatrix3(
        1.0000000,  0.0000000,  0.0000000,
        0.0000000,  0.0000000, -1.0000000,
        0.0000000,  1.0000000,  0.0000000
    );


    
    //The width of a plane rigid body
    //It's really a box under the hood
    static final double PLANE_WIDTH = 0.3;

    /**
     * Creates a plane DBody. Dimensions x and z control the length and width of the plane;
     * @param dimensions The dimensions of the plane
     * @return The DBody
     */
    public static DBody createPlaneBody(CollisionEngine collisionEngine, Vector3d dimensions, long categoryBits){
        DBox geom = collisionEngine.createCubeGeom(new Vector3d(dimensions.x,PLANE_WIDTH,dimensions.z),categoryBits);
        return collisionEngine.createDBody(geom);
    }

    /**
     * Creates a cube DBody. Dimensions controlled by the dimensions vector.
     * @param collisionEngine The collision engine to create the body inside of
     * @param dimensions The dimensions of the cube
     * @return The DBody
     */
    public static DBody createCubeBody(CollisionEngine collisionEngine, Vector3d dimensions, long categoryBits){
        DBox geom = collisionEngine.createCubeGeom(new Vector3d(dimensions),categoryBits);
        return collisionEngine.createDBody(geom);
    }

    /**
     * Creates a cylinder DBody. Dimensions controlled by the dimensions vector.
     * @param collisionEngine The collision engine to create the body inside of
     * @param dimensions The dimensions of the cube
     * @return The DBody
     */
    public static DBody createCylinderBody(CollisionEngine collisionEngine, double radius, double length, long categoryBits){
        DCylinder geom = collisionEngine.createCylinderGeom(radius,length,categoryBits);
        DBody returnBody = collisionEngine.createDBody(geom);
        collisionEngine.setOffsetRotation(geom); //ode4j required geom to already be on body before rotating for some reason
        return returnBody;
    }

    /**
     * Creates a sphere body in the collision engine
     * @param collisionEngine The collision engine
     * @param radius The radius of the sphere
     * @return The DBody
     */
    public static DBody createSphereBody(CollisionEngine collisionEngine, double radius, long categoryBits){
        DSphere geom = collisionEngine.createSphereGeom(radius,categoryBits);
        return collisionEngine.createDBody(geom);
    }

    /**
     * Creates a capsule body in the collision engine
     * @param collisionEngine The collision engine
     * @param radius The radius of the sphere
     * @param length The length of the capsule (not including round part at ends)
     * @return The DBody
     */
    public static DBody createCapsuleBody(CollisionEngine collisionEngine, double radius, double length, long categoryBits){
        DCapsule geom = collisionEngine.createCapsuleGeom(radius,length,categoryBits);
        DBody rVal = collisionEngine.createDBody(geom);
        collisionEngine.setOffsetRotation(geom); //ode4j required geom to already be on body before rotating for some reason
        return rVal;
    }

    /**
     * Creates a dbody with existing shapes that are provided
     * @param collisionEngine the collision engine to create it in
     * @param geoms the geometries to attach
     * @return the dbody
     */
    public static DBody createBodyWithShapes(CollisionEngine collisionEngine, DGeom ... geoms){
        return collisionEngine.createDBody(geoms);
    }

    /**
     * Creates a sphere shape
     * @param collisionEngine the collision engine
     * @param radius the radius of the sphere
     * @param categoryBits the category bits for the shape
     * @return the sphere shape
     */
    public static DSphere createShapeSphere(CollisionEngine collisionEngine, double radius, long categoryBits){
        return collisionEngine.createSphereGeom(radius, categoryBits);
    }

    /**
     * Creates a capsule shape
     * @param collisionEngine The collision engine
     * @param radius the radius of the capsule
     * @param length the length of the capsule
     * @param categoryBits the category bits for the shape
     * @return the capsule shape
     */
    public static DCapsule createCapsuleShape(CollisionEngine collisionEngine, double radius, double length, long categoryBits){
        return collisionEngine.createCapsuleGeom(radius, length, categoryBits);
    }

    /**
     * Creates a capsule shape
     * @param collisionEngine The collision engine
     * @param radius the radius of the capsule
     * @param length the length of the capsule
     * @param categoryBits the category bits for the shape
     * @return the capsule shape
     */
    public static DCapsule createCylinderShape(CollisionEngine collisionEngine, double radius, double length, long categoryBits){
        return collisionEngine.createCapsuleGeom(radius, length, categoryBits);
    }

    /**
     * Creates the cube shape
     * @param collisionEngine The collision engine
     * @param dimensions The dimensions of the cube
     * @param categoryBits The category bits
     * @return The cube shape
     */
    public static DBox createCubeShape(CollisionEngine collisionEngine, Vector3d dimensions, long categoryBits){
        return collisionEngine.createCubeGeom(dimensions, categoryBits);
    }

    /**
     * Sets the mass on the dbody
     * @param collisionEngine The collision engine
     * @param body The body
     * @param mass The mass value
     * @param radius The radius of the cylinder
     * @param length The length of the cylinder
     * @return The DMass object
     */
    public static DMass setCylinderMass(CollisionEngine collisionEngine, DBody body, double mass, double radius, double length, Vector3d offset, Quaterniond rotation){
        return collisionEngine.createCylinderMass(mass, radius, length, body, offset, rotation);
    }

    /**
     * Sets the mass on the dbody
     * @param collisionEngine The collision engine
     * @param body The body
     * @param mass The mass value
     * @param dims The dimensions of the box
     * @return The DMass object
     */
    public static DMass setBoxMass(CollisionEngine collisionEngine, DBody body, double mass, Vector3d dims, Vector3d offset, Quaterniond rotation){
        return collisionEngine.createBoxMass(mass, dims, body, offset, rotation);
    }

    /**
     * Sets the mass on the dbody
     * @param collisionEngine The collision engine
     * @param body The body
     * @param mass The mass value
     * @param radius The radius of the capsule
     * @param length The length of the capsule
     * @return The DMass object
     */
    public static DMass setCapsuleMass(CollisionEngine collisionEngine, DBody body, double mass, double radius, double length, Vector3d offset, Quaterniond rotation){
        return collisionEngine.createCapsuleMass(mass, radius, length, body, offset, rotation);
    }

    /**
     * Sets the autodisable flags on the body
     * @param collisionEngine The collision engine
     * @param body The body
     * @param autoDisable true to autodisable, false otherwise
     * @param linearThreshold the linear velocity threshold to disable under
     * @param angularThreshold the angular velocity threshold to disable under
     * @param steps the number of simulation steps below thresholds before autodisable
     */
    public static void setAutoDisable(CollisionEngine collisionEngine, DBody body, boolean autoDisable, double linearThreshold, double angularThreshold, int steps){
        collisionEngine.setAutoDisableFlags(body, autoDisable, linearThreshold, angularThreshold, steps);
    }

    /**
     * Sets the damping for the body
     * @param collisionEngine The collision engine
     * @param body The body
     * @param linearDamping The linear damping
     * @param angularDamping The angular damping
     */
    public static void setDamping(CollisionEngine collisionEngine, DBody body, double linearDamping, double angularDamping){
        collisionEngine.setDamping(body, linearDamping, angularDamping);
    }

    /**
     * Sets the provided body to be a kinematic body (no gravity applied)
     * @param collisionEngine The collision engine
     * @param body The body
     */
    public static void setKinematic(CollisionEngine collisionEngine, DBody body){
        collisionEngine.setKinematic(body);
    }

    /**
     * Sets the gravity mode of the body
     * @param collisionEngine the collision engine
     * @param body the body
     * @param gravityMode the gravity mode value
     */
    public static void setGravityMode(CollisionEngine collisionEngine, DBody body, boolean gravityMode){
        collisionEngine.setGravityMode(body, gravityMode);
    }

    /**
     * Sets the offset position of the first geometry in a given body (local to the parent)
     * @param collisionEngine The collision engine
     * @param body The body
     * @param offsetPosition The position to offset the first geometry by
     */
    public static void setOffsetPosition(CollisionEngine collisionEngine, DBody body, Vector3d offsetPosition){
        collisionEngine.setOffsetPosition(body, offsetPosition);
    }

    /**
     * Removes a geom from a body
     * @param collisionEngine the collision engine
     * @param body the body
     * @param geom the geometry
     */
    public static void removeShapeFromBody(CollisionEngine collisionEngine, DBody body, DGeom geom){
        collisionEngine.removeGeometryFromBody(body, geom);
    }

    /**
     * Destroys a geometry
     * @param collisionEngine The collision engine
     * @param geom the geometry
     */
    public static void destroyShape(CollisionEngine collisionEngine, DGeom geom){
        collisionEngine.destroyDGeom(geom);
    }

    /**
     * Attaches a geom to a body
     * @param collisionEngine the collision engine
     * @param body the body
     * @param geom the geometry
     */
    public static void attachGeomToBody(CollisionEngine collisionEngine, DBody body, DGeom geom){
        collisionEngine.attachGeomToBody(body, geom);
    }

    
    /**
     * Creates an ode DBody from a terrain chunk data object
     * @param data The terrain data
     * @return The DBody
     */
    public static DBody generateBodyFromTerrainData(CollisionEngine collisionEngine, TriGeomData data, long categoryBits){
        DBody body = null;

        //create trimesh
        if(data.getFaceElements().length > 0){
            DTriMesh triMesh = collisionEngine.createTrimeshGeom(data.getVertices(),data.getFaceElements(),categoryBits);
            body = collisionEngine.createDBody(triMesh);
            collisionEngine.setKinematic(body);
            collisionEngine.setGravityMode(body, false);
        }

        return body;
    }

    /**
     * Creates an ode collider from a terrain chunk data object
     * @param data The terrain data
     * @return The geom
     */
    public static DGeom generateGeomFromTerrainData(CollisionEngine collisionEngine, TriGeomData data, long categoryBits){
        DGeom geom = null;

        //create trimesh
        if(data.getFaceElements().length > 0){
            geom = collisionEngine.createTrimeshGeom(data.getVertices(),data.getFaceElements(),categoryBits);
        }

        return geom;
    }

    /**
     * Creates an ode DBody from a mesh data set containing multiple shapes
     * @param data The mesh data data
     * @return The DBody
     */
    public static DBody generateBodyFromMultiShapeMeshData(CollisionEngine collisionEngine, MultiShapeTriGeomData data, long categoryBits){
        DBody body = null;

        DGeom[] geoms = new DGeom[data.getData().size()];

        //create trimeshes
        int i = 0;
        for(TriGeomData shapeData : data.getData()){
            if(shapeData.getFaceElements().length > 0){
                DTriMesh triMesh = collisionEngine.createTrimeshGeom(shapeData.getVertices(),shapeData.getFaceElements(),categoryBits);
                geoms[i] = triMesh;
            }
            i++;
        }

        //create body from shapes
        body = collisionEngine.createDBody(geoms);
        collisionEngine.setKinematic(body);
        collisionEngine.setGravityMode(body, false);


        return body;
    }

    /**
     * Creates an ode DBody from a mesh data set containing multiple shapes
     * @param data The mesh data data
     * @return The DBody
     */
    public static DGeom generateColliderFromMultiShapeMeshData(CollisionEngine collisionEngine, MultiShapeTriGeomData data, long categoryBits){
        DSpace multishape = collisionEngine.createSpace();

        //create trimeshes
        for(TriGeomData shapeData : data.getData()){
            if(shapeData.getFaceElements().length > 0){
                collisionEngine.createTrimeshGeom(shapeData.getVertices(),shapeData.getFaceElements(),categoryBits,multishape);
            }
        }

        return multishape;
    }

    /**
     * Generates a body from an AIScene
     * @param scene The AIScene to generate a rigid body off of
     * @return A rigid body based on the AIScene
     */
    public static DBody generateRigidBodyFromAIScene(CollisionEngine collisionEngine, AIScene scene, long categoryBits){

        DBody body = collisionEngine.createDBody((DGeom[])null);

        PointerBuffer meshesBuffer = scene.mMeshes();
        while(meshesBuffer.hasRemaining()){
            float[] verts;
            int numVertices;
            int[] indices;
            int numTriangles;

            AIMesh aiMesh = AIMesh.create(meshesBuffer.get());
            //allocate array for vertices
            numVertices = aiMesh.mNumVertices();
            verts = new float[numVertices * 3];
            //read vertices
            AIVector3D.Buffer vertexBuffer = aiMesh.mVertices();
            int vertPos = 0;
            while(vertexBuffer.hasRemaining()){
                AIVector3D vector = vertexBuffer.get();
                verts[vertPos+0] = vector.x();
                verts[vertPos+1] = vector.y();
                verts[vertPos+2] = vector.z();
                vertPos = vertPos + 3;
            }
            numTriangles = aiMesh.mNumFaces();
            indices = new int[numTriangles * 3];
            int indicesPos = 0;
            //read faces
            AIFace.Buffer faceBuffer = aiMesh.mFaces();
            while(faceBuffer.hasRemaining()){
                AIFace currentFace = faceBuffer.get();
                IntBuffer indexBuffer = currentFace.mIndices();
                while(indexBuffer.hasRemaining()){
                    int index = indexBuffer.get();
                    indices[indicesPos] = index;
                    indicesPos++;
                }
            }
            DTriMesh meshGeom = collisionEngine.createTrimeshGeom(verts, indices,categoryBits);
            meshGeom.setBody(body);
        }
        
        return body;
    }
    
}
