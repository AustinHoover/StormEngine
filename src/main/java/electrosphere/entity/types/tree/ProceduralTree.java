package electrosphere.entity.types.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.ode4j.ode.DBody;

import electrosphere.client.entity.instance.InstanceTemplate;
import electrosphere.client.entity.instance.InstancedEntityUtils;
import electrosphere.collision.CollisionBodyCreation;
import electrosphere.collision.PhysicsEntityUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.data.entity.foliage.FoliageType;
import electrosphere.data.entity.foliage.ProceduralTreeBranchModel;
import electrosphere.data.entity.foliage.ProceduralTreeTrunkModel;
import electrosphere.data.entity.foliage.TreeModel;
import electrosphere.engine.Globals;
import electrosphere.entity.ClientEntityUtils;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityCreationUtils;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.renderer.actor.instance.InstancedActor;
import electrosphere.renderer.buffer.ShaderAttribute;
import electrosphere.renderer.buffer.HomogenousUniformBuffer.HomogenousBufferTypes;
import electrosphere.server.datacell.Realm;

/**
 * Used for generating procedural trees
 */
public class ProceduralTree {

    //The instance template for the branch
    static InstanceTemplate branchInstanceTemplate;

    //the instance template for a leaf blob
    static InstanceTemplate leafInstanceTemplate;

    //The model attribute
    static final ShaderAttribute modelMatrixAttribute = new ShaderAttribute(new int[]{
        5,6,7,8
    });

    //the bone attribute
    static final ShaderAttribute boneMatrixAttribute = new ShaderAttribute(new int[]{
        9,10,11,12
    });

    //the size of the base of the branch segment, attribute
    static final ShaderAttribute baseSizeAttribute = new ShaderAttribute(13);

    static final ShaderAttribute leafColorAttribute = new ShaderAttribute(9);

    static final float TREE_MASS = 1.0f;

    //The static setup logic
    static {
        //create map of attributes and register them
        Map<ShaderAttribute,HomogenousBufferTypes> attributes = new HashMap<ShaderAttribute,HomogenousBufferTypes>();
        attributes.put(modelMatrixAttribute,HomogenousBufferTypes.MAT4F);
        attributes.put(boneMatrixAttribute,HomogenousBufferTypes.MAT4F);
        attributes.put(baseSizeAttribute, HomogenousBufferTypes.FLOAT);

        branchInstanceTemplate = InstanceTemplate.createInstanceTemplate(
            1000,
            "Models/foliage/proceduralTree2/proceduralTree2v2.fbx",
            "Shaders/instanced/proceduraltree/proceduraltree.vs",
            "Shaders/instanced/proceduraltree/proceduraltree.fs",
            attributes);

        Map<ShaderAttribute,HomogenousBufferTypes> leafAttributes = new HashMap<ShaderAttribute,HomogenousBufferTypes>();
        leafAttributes.put(modelMatrixAttribute,HomogenousBufferTypes.MAT4F);
        leafAttributes.put(leafColorAttribute, HomogenousBufferTypes.VEC3F);
        leafInstanceTemplate = InstanceTemplate.createInstanceTemplate(
            10000,
            "Models/foliage/foliageBlockTemplate1Test1.fbx",
            "Shaders/instanced/colorshift/colorshift.vs",
            "Shaders/instanced/colorshift/colorshift.fs",
            leafAttributes
        );
    }

    /**
     * Client side function to generate a tree
     * @param type The type of tree as a string
     * @param seed The seed (lol) for the tree
     * @return The top level tree entity
     */
    public static Entity clientGenerateProceduralTree(String type, long seed){
        Random treeRandom = new Random(seed);

        //call recursive branching routine to generate branches from trunk + leaf blobs
        FoliageType foliageType = Globals.gameConfigCurrent.getFoliageMap().getType(type);
        TreeModel treeModel = foliageType.getGraphicsTemplate().getProceduralModel().getTreeModel();

        //generate trunk
        Entity trunkChild = EntityCreationUtils.createClientSpatialEntity();
        setProceduralActor(trunkChild, treeModel, treeRandom);

        return trunkChild;
    }

    /**
     * Sets the procedural actor for the tree
     * @param trunkChild The entity
     * @param treeModel The tree model params
     * @param treeRandom The random
     */
    public static void setProceduralActor(Entity trunkChild, TreeModel treeModel, Random treeRandom){
        InstancedActor instancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(trunkChild, branchInstanceTemplate, modelMatrixAttribute);
        instancedActor.setAttribute(boneMatrixAttribute, new Matrix4d().identity());
        instancedActor.setAttribute(modelMatrixAttribute, new Matrix4d().identity());
        instancedActor.setAttribute(baseSizeAttribute, 1.0f);

        //attach physics
        DBody rigidBody = CollisionBodyCreation.createCylinderBody(
            Globals.clientState.clientSceneWrapper.getCollisionEngine(),
            treeModel.getPhysicsBody().getDimension1(),
            treeModel.getPhysicsBody().getDimension2(),
            Collidable.TYPE_STATIC_BIT
        );
        CollisionBodyCreation.setOffsetPosition(Globals.clientState.clientSceneWrapper.getCollisionEngine(), rigidBody, new Vector3d(0,treeModel.getPhysicsBody().getOffsetY(),0));
        CollisionBodyCreation.setKinematic(Globals.clientState.clientSceneWrapper.getCollisionEngine(), rigidBody);
        Collidable collidable = new Collidable(trunkChild, Collidable.TYPE_STATIC, true);
        PhysicsEntityUtils.setDBody(trunkChild, rigidBody);
        Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
            0,treeModel.getPhysicsBody().getOffsetY(),0, //translate
            0,0,0,1, //rotate
            1, 1, 1 //scale
        );
        trunkChild.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
        trunkChild.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, treeModel.getPhysicsBody());
        trunkChild.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
        trunkChild.putData(EntityDataStrings.PHYSICS_MASS, TREE_MASS);

        Globals.clientState.clientSceneWrapper.getCollisionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(trunkChild));


        //generate branches
        clientGenerateTrunk(
            treeModel,
            trunkChild,
            treeRandom,
            new TreeSegment(
                0,
                0,
                new Vector3d(0,0,0),
                new Quaterniond(0,0,0,1),
                new Vector3d(0,3,0),
                new Quaternionf(0,0,0,1),
                1,
                1,
                true,
                false
            )
        );

        ClientEntityUtils.initiallyPositionEntity(trunkChild, EntityUtils.getPosition(trunkChild), EntityUtils.getRotation(trunkChild));
    }

    /**
     * Generates trunk segments
     * @param type The type of tree
     * @param parent The parent entity
     * @param rand The randomizer
     * @param segment The current segment
     * @return The entities that were generated
     */
    private static List<Entity> clientGenerateTrunk(
        TreeModel type,
        Entity parent,
        Random rand,
        TreeSegment segment
    ){
        List<Entity> rVal = new LinkedList<Entity>();

        ProceduralTreeTrunkModel trunkModel = type.getTrunkModel();
        ProceduralTreeBranchModel branchModel = type.getBranchModel();

        //spawn trunk segments
        if(
            trunkModel.getCentralTrunk() &&
            segment.scalar > trunkModel.getMinimumTrunkScalar() &&
            segment.currentSegmentNumber < trunkModel.getMaximumTrunkSegments()
        ){
            //what we want to solve for:
            //get parent position + rotation
            //get an offset from the parent position for this position
            //get a rotation from the parent rotation for this rotation
            //get an offset from the current position for the child position
            //get a rotation from the current rotation for the child rotation

            //calculate transform from parent entity
            //this is the transform that will be applied every time the attachutils updates
            Matrix4d transformFromParent = new Matrix4d()
                .translate(new Vector3f(
                    (float)segment.offsetFromParent.x,
                    (float)segment.offsetFromParent.y,
                    (float)segment.offsetFromParent.z
                ))
                .rotate(new Quaternionf(
                    (float)segment.rotationFromParent.x,
                    (float)segment.rotationFromParent.y,
                    (float)segment.rotationFromParent.z,
                    (float)segment.rotationFromParent.w
                ));

            //calculate combined transform
            Matrix4d combinedTransform = new Matrix4d().translate(new Vector3f(
                (float)segment.parentPosition.x,
                (float)segment.parentPosition.y,
                (float)segment.parentPosition.z
            )).rotate(new Quaternionf(
                (float)segment.parentRotation.x,
                (float)segment.parentRotation.y,
                (float)segment.parentRotation.z,
                (float)segment.parentRotation.w
            )).mul(transformFromParent);







            //calculate current branch's stuff
            //get current position
            Vector4d currentPositionf = combinedTransform.transform(new Vector4d(
                0,
                0,
                0,
                1
            ));
            Vector3d currentPosition = new Vector3d(currentPositionf.x,currentPositionf.y,currentPositionf.z);

            //The new absolute rotation at the end of the bone
            Quaterniond currentAbsoluteRotation = combinedTransform.getNormalizedRotation(new Quaterniond()).normalize();

            //the rotation applied to the bone
            Quaternionf boneRotation = new Quaternionf(0,0,0,1);

            //calculates the bone transform matrix
            Matrix4d boneTransform = new Matrix4d().identity().rotate(boneRotation);

            //new position transform
            Matrix4d newPositionTransform = new Matrix4d().rotate(boneRotation).translate(0,type.getBranchHeight(),0);

            Vector4d newPositionRaw = newPositionTransform.transform(new Vector4d(0,0,0,1));
            Vector3d newPosition = new Vector3d(newPositionRaw.x,newPositionRaw.y,newPositionRaw.z);

            //get new scalar
            float newScalar = segment.scalar - trunkModel.getTrunkScalarFalloffFactor();

            //create entity

            Entity trunkSegment = EntityCreationUtils.createClientSpatialEntity();
            InstancedActor instancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(trunkSegment, branchInstanceTemplate, modelMatrixAttribute);
            instancedActor.setAttribute(boneMatrixAttribute, boneTransform.scale(newScalar,1,newScalar));
            instancedActor.setAttribute(baseSizeAttribute, segment.scalar);
            instancedActor.setAttribute(modelMatrixAttribute, new Matrix4d().identity());

            //set entity stuuff
            EntityUtils.getPosition(trunkSegment).set(currentPosition);
            EntityUtils.getScale(trunkSegment).set(1,1,1);
            EntityUtils.getRotation(trunkSegment).set(currentAbsoluteRotation);
            // AttachUtils.clientAttachEntityAtCurrentOffset(parent, branch);
            AttachUtils.clientAttachEntityAtTransform(parent, trunkSegment, transformFromParent);

            rVal.add(trunkSegment);
            clientGenerateTrunk(
                type,
                parent,
                rand,
                new TreeSegment(
                    0,
                    0,
                    currentPosition,
                    currentAbsoluteRotation,
                    newPosition,
                    boneRotation,
                    segment.scalar - trunkModel.getTrunkScalarFalloffFactor(),
                    segment.currentSegmentNumber + 1,
                    false, //can't be central trunk
                    false
                )
            );
        }

        //spawn branches
        if(segment.scalar > branchModel.getMinimumLimbScalar() && segment.currentSegmentNumber < branchModel.getMaximumBranchSegments()){
            clientGenerateBranches(type,parent,rand,segment);
        }
        return rVal;
    }

    private static List<Entity> clientGenerateBranches(
        TreeModel type,
        Entity parent,
        Random rand,
        TreeSegment segment
    ){
        List<Entity> rVal = new LinkedList<Entity>();

        ProceduralTreeBranchModel branchModel = type.getBranchModel();
        //how fast do the branches shrink in size
        float scalarFalloffFactor = branchModel.getLimbScalarFalloffFactor();
        //the minimum branch size before we stop generating branch segments/trunk segments
        float minimumScalar = branchModel.getMinimumLimbScalar();
        //how high is the model for a single branch segment
        float treeSegmentHeight = type.getBranchHeight();
        //how much to spread the branches along the current segment
        float minimumSegmentDispersion = branchModel.getMinimumLimbDispersion();
        float dispersionSpread = branchModel.getMaximumLimbDispersion() - branchModel.getMinimumLimbDispersion();
        //the number of branches to make per segment
        int minBranches = branchModel.getMinimumNumberForks();
        int maxBranches = branchModel.getMaximumNumberForks();
        //the maximum number of segments in an single arc for both trunk and branches
        int maximumBranchSegments = branchModel.getMaximumBranchSegments();
        
        if(segment.scalar > minimumScalar && segment.currentSegmentNumber < maximumBranchSegments){
            int minimumSegmentToSpawnLeaves = branchModel.getMinimumSegmentToSpawnLeaves();

            //how much does it peel off of the current vector
            double peelRotation = (rand.nextFloat() * dispersionSpread + minimumSegmentDispersion);
            //the initial rotation around Y that the branch will peel towards
            double offsetRotation = 0;
            double rotationInitialOffset = rand.nextFloat();
            int branchNum = rand.nextInt(maxBranches - minBranches) + minBranches;
            for(int i = 0; i < branchNum; i++){

                //what we want to solve for:
                //get parent position + rotation
                //get an offset from the parent position for this position
                //get a rotation from the parent rotation for this rotation
                //get an offset from the current position for the child position
                //get a rotation from the current rotation for the child rotation

                //calculate transform from parent entity
                //this is the transform that will be applied every time the attachutils updates
                Matrix4d transformFromParent = new Matrix4d()
                    .translate(new Vector3f(
                        (float)segment.offsetFromParent.x,
                        (float)segment.offsetFromParent.y,
                        (float)segment.offsetFromParent.z
                    ))
                    .rotate(new Quaternionf(
                        (float)segment.rotationFromParent.x,
                        (float)segment.rotationFromParent.y,
                        (float)segment.rotationFromParent.z,
                        (float)segment.rotationFromParent.w
                    ));

                //calculate combined transform
                Matrix4d combinedTransform = new Matrix4d().translate(new Vector3f(
                    (float)segment.parentPosition.x,
                    (float)segment.parentPosition.y,
                    (float)segment.parentPosition.z
                )).rotate(new Quaternionf(
                    (float)segment.parentRotation.x,
                    (float)segment.parentRotation.y,
                    (float)segment.parentRotation.z,
                    (float)segment.parentRotation.w
                )).mul(transformFromParent);







                //calculate current branch's stuff
                //get current position
                Vector4d currentPositionf = combinedTransform.transform(new Vector4d(
                    0,
                    0,
                    0,
                    1
                ));
                Vector3d currentPosition = new Vector3d(currentPositionf.x,currentPositionf.y,currentPositionf.z);

                //The new absolute rotation at the end of the bone
                Quaterniond currentAbsoluteRotation = combinedTransform.getNormalizedRotation(new Quaterniond()).normalize();





                


                    
                //calculate child stuff

                //update offsetrotation
                offsetRotation = rotationInitialOffset + (i + 1) * (2.0 * Math.PI / (float)branchNum);
                //get new rotation
                double pitchFactor = Math.sin(offsetRotation);
                double rollFactor = Math.cos(offsetRotation);

                //the rotation applied to the bone
                Quaternionf boneRotation = new Quaternionf(0,0,0,1).rotateLocalX((float)(pitchFactor * peelRotation)).rotateLocalZ((float)(rollFactor * peelRotation)).normalize();


                //calculates the bone transform matrix
                Matrix4d boneTransform = new Matrix4d().identity().rotate(boneRotation);

                //new position transform
                Matrix4d newPositionTransform = new Matrix4d().rotate(boneRotation).translate(0,treeSegmentHeight,0);

                Vector4d newPositionRaw = newPositionTransform.transform(new Vector4d(0,0,0,1));
                Vector3d newPosition = new Vector3d(newPositionRaw.x,newPositionRaw.y,newPositionRaw.z);

                //get new scalar
                float newScalar = segment.scalar - scalarFalloffFactor;






                //create entity

                Entity branch = EntityCreationUtils.createClientSpatialEntity();
                InstancedActor instancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(branch, branchInstanceTemplate, modelMatrixAttribute);
                instancedActor.setAttribute(boneMatrixAttribute, boneTransform.scale(newScalar,1,newScalar));
                instancedActor.setAttribute(baseSizeAttribute, segment.scalar);
                instancedActor.setAttribute(modelMatrixAttribute, new Matrix4d().identity());

                //set entity stuuff
                EntityUtils.getPosition(branch).set(currentPosition);
                EntityUtils.getScale(branch).set(1,1,1);
                EntityUtils.getRotation(branch).set(currentAbsoluteRotation);
                // AttachUtils.clientAttachEntityAtCurrentOffset(parent, branch);
                AttachUtils.clientAttachEntityAtTransform(parent, branch, transformFromParent);

                rVal.add(branch);





                //attach leaf blobs
                if(
                    !segment.isTrunk && 
                    segment.currentSegmentNumber >= minimumSegmentToSpawnLeaves
                ){
                    createLeafBlobsOnBranch(
                        type,
                        branch,
                        rand,
                        new TreeSegment(
                            peelRotation,
                            offsetRotation,
                            currentPosition,
                            currentAbsoluteRotation,
                            newPosition,
                            boneRotation,
                            segment.scalar - scalarFalloffFactor,
                            segment.currentSegmentNumber + 1,
                            false, //can't be central trunk
                            false
                        )
                    );
                }




                //recurse
                List<Entity> childBranches = clientGenerateBranches(
                    type,
                    branch,
                    rand,
                    new TreeSegment(
                        peelRotation,
                        offsetRotation,
                        currentPosition,
                        currentAbsoluteRotation,
                        newPosition,
                        boneRotation,
                        segment.scalar - scalarFalloffFactor,
                        segment.currentSegmentNumber + 1,
                        false, //can't be central trunk
                        false
                    )
                );

                //add behavior tree to update all child branch attachment points to new point
                if(childBranches.size() > 0 && segment.scalar >= type.getMinimumScalarToGenerateSwayTree() && segment.scalar <= type.getMaximumScalarToGenerateSwayTree()){
                    Globals.clientState.clientSceneWrapper.getScene().registerBehaviorTree(new BranchBehaviorTree(
                        type,
                        parent,
                        branch,
                        childBranches,
                        newScalar,
                        peelRotation,
                        offsetRotation,
                        treeSegmentHeight,
                        0l
                    ));
                }
            }
        }
        return rVal;
    }

    /**
     * Server side function to generate a tree
     * @param type The type of tree as a string
     * @param seed The seed (lol) for the tree
     * @return The top level tree entity
     */
    public static Entity serverGenerateProceduralTree(Realm realm, Vector3d position, FoliageType foliageType, long seed){
        //call recursive branching routine to generate branches from trunk + leaf blobs
        TreeModel treeModel = foliageType.getGraphicsTemplate().getProceduralModel().getTreeModel();

        //generate trunk
        Entity trunkChild = EntityCreationUtils.createServerEntity(realm,position);

        //attach physics
        DBody rigidBody = CollisionBodyCreation.createCylinderBody(
            realm.getCollisionEngine(),
            treeModel.getPhysicsBody().getDimension1(),
            treeModel.getPhysicsBody().getDimension2(),
            Collidable.TYPE_STATIC_BIT
        );
        CollisionBodyCreation.setOffsetPosition(realm.getCollisionEngine(), rigidBody, new Vector3d(0,treeModel.getPhysicsBody().getOffsetY(),0));
        CollisionBodyCreation.setKinematic(realm.getCollisionEngine(), rigidBody);
        Collidable collidable = new Collidable(trunkChild, Collidable.TYPE_STATIC, true);
        PhysicsEntityUtils.setDBody(trunkChild, rigidBody);
        Matrix4d offsetTransform = new Matrix4d().translationRotateScale(
            0,treeModel.getPhysicsBody().getOffsetY(),0, //translate
            0,0,0,1, //rotate
            1, 1, 1 //scale
        );
        trunkChild.putData(EntityDataStrings.PHYSICS_COLLISION_BODY_TRANSFORM, offsetTransform);
        trunkChild.putData(EntityDataStrings.PHYSICS_MODEL_TEMPLATE, treeModel.getPhysicsBody());
        trunkChild.putData(EntityDataStrings.PHYSICS_COLLIDABLE, collidable);
        trunkChild.putData(EntityDataStrings.PHYSICS_MASS, TREE_MASS);

        realm.getCollisionEngine().registerCollisionObject(rigidBody, collidable, EntityUtils.getPosition(trunkChild));

        return trunkChild;
    }

    // /**
    //  * Generates branches
    //  * @param type The type of branch
    //  * @param parent The immediate parent of the branch
    //  * @param rand The random
    //  * @param transform The current ik transform for the branch
    //  * @param scalar The scalar for the current width of the branch
    //  * @param isCentralTrunk True if the tree should generate a central trunk with branches coming off of it
    //  */
    // public static void clientGenerateBranches(
    //     TreeModel type,
    //     Entity parent,
    //     Random rand,
    //     Matrix4f transform,
    //     float scalar,
    //     int currentSegmentNumber,
    //     boolean isCentralTrunk
    // ){
    //     //how fast do the branches shrink in size
    //     float scalarFalloffFactor = type.getLimbScalarFalloffFactor();
    //     //the minimum branch size before we stop generating branch segments/trunk segments
    //     float minimumScalar = type.getMinimumLimbScalar();
    //     //how high is the model for a single branch segment
    //     float treeSegmentHeight = type.getBranchHeight();
    //     //how much to spread the branches along the current segment
    //     float minimumSegmentDispersion = type.getMinimumLimbDispersion();
    //     float dispersionSpread = type.getMaximumLimbDispersion() - type.getMinimumLimbDispersion();
    //     //the number of branches to make per segment
    //     int minBranches = type.getMinimumNumberForks();
    //     int maxBranches = type.getMaximumNumberForks();
    //     //the maximum number of segments in an single arc for both trunk and branches
    //     int maximumTrunkSegments = type.getMaximumTrunkSegments();
    //     int maximumBranchSegments = type.getMaximumBranchSegments();
        
    //     if(scalar > minimumScalar && currentSegmentNumber < maximumTrunkSegments){
    //         boolean hasCentralTrunk = type.getCentralTrunk();
    //         //if there is a central trunk and this is the central trunk, generate the next central trunk segment
    //         if(isCentralTrunk && hasCentralTrunk){
    //             //the rotation applied to the bone
    //             Quaternionf boneRotation = new Quaternionf(0,0,0,1).normalize();

    //             //get current position
    //             Vector4f currentPositionf = transform.transform(new Vector4f(0,0,0,1));
    //             Vector3d currentPosition = new Vector3d(currentPositionf.x,currentPositionf.y,currentPositionf.z);

    //             //The new absolute rotation at the end of the bone
    //             Quaterniond currentAbsoluteRotation = transform.getNormalizedRotation(new Quaterniond()).normalize();

    //             //calculates the bone transform matrix
    //             Matrix4f boneTransform = new Matrix4f().identity().rotate(boneRotation);

    //             //calculate attachment transform
    //             Matrix4f attachmentTransform = new Matrix4f().identity().translate(0,treeSegmentHeight,0).rotate(boneRotation);

    //             //new position transform
    //             Matrix4f newPositionTransform = new Matrix4f(transform).mul(boneTransform);

    //             //get new scalar
    //             float newScalar = scalar - scalarFalloffFactor;


    //             //create entity
    //             Entity branch = EntityCreationUtils.createClientSpatialEntity();
    //             InstancedActor instancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(branch, branchInstanceTemplate, modelMatrixAttribute);
    //             instancedActor.setAttribute(boneMatrixAttribute, boneTransform.scale(newScalar,1,newScalar));
    //             instancedActor.setAttribute(baseSizeAttribute, scalar);
    //             instancedActor.setAttribute(modelMatrixAttribute, new Matrix4f().identity());

    //             //set entity stuuff
    //             EntityUtils.getPosition(branch).set(currentPosition);
    //             EntityUtils.getScale(branch).set(1,1,1);
    //             EntityUtils.getRotation(branch).set(currentAbsoluteRotation);
    //             // AttachUtils.clientAttachEntityAtCurrentOffset(parent, branch);
    //             // AttachUtils.clientAttachEntityAtTransform(parent, branch, attachmentTransform);

    //             //recurse
    //             clientGenerateBranches(
    //                 type,
    //                 branch,
    //                 rand,
    //                 newPositionTransform,
    //                 scalar - scalarFalloffFactor,
    //                 currentSegmentNumber + 1,
    //                 true //can't be central trunk
    //             );
    //         }
    //     }
        
    //     if(scalar > minimumScalar && currentSegmentNumber < maximumBranchSegments){
    //         int minimumSegmentToSpawnLeaves = type.getMinimumSegmentToSpawnLeaves();

    //         //how much does it peel off of the current vector
    //         double peelRotation = (rand.nextFloat() * dispersionSpread + minimumSegmentDispersion);
    //         //the initial rotation around Y that the branch will peel towards
    //         double offsetRotation = 0;
    //         double rotationInitialOffset = rand.nextFloat();
    //         int branchNum = rand.nextInt(maxBranches - minBranches) + minBranches;
    //         for(int i = 0; i < branchNum; i++){
    //             //update offsetrotation
    //             offsetRotation = rotationInitialOffset + (i + 1) * (2.0 * Math.PI / (float)branchNum);
    //             //get new rotation
    //             double pitchFactor = Math.sin(offsetRotation);
    //             double rollFactor = Math.cos(offsetRotation);

    //             //the rotation applied to the bone
    //             Quaternionf boneRotation = new Quaternionf(0,0,0,1).rotateLocalX((float)(pitchFactor * peelRotation)).rotateLocalZ((float)(rollFactor * peelRotation)).normalize();

    //             //get current position
    //             Vector4f currentPositionf = transform.transform(new Vector4f(0,0,0,1));
    //             Vector3d currentPosition = new Vector3d(currentPositionf.x,currentPositionf.y,currentPositionf.z);

    //             //The new absolute rotation at the end of the bone
    //             Quaterniond currentAbsoluteRotation = transform.getNormalizedRotation(new Quaterniond()).normalize();

    //             //calculates the bone transform matrix
    //             Matrix4f boneTransform = new Matrix4f().identity().rotate(boneRotation);

    //             //new position transform
    //             Matrix4f newPositionTransform = new Matrix4f(transform).mul(boneTransform).translate(0,treeSegmentHeight,0);

    //             //get new scalar
    //             float newScalar = scalar - scalarFalloffFactor;


    //             //create entity
    //             Entity branch = EntityCreationUtils.createClientSpatialEntity();
    //             InstancedActor instancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(branch, branchInstanceTemplate, modelMatrixAttribute);
    //             instancedActor.setAttribute(boneMatrixAttribute, boneTransform.scale(newScalar,1,newScalar));
    //             instancedActor.setAttribute(baseSizeAttribute, scalar);
    //             instancedActor.setAttribute(modelMatrixAttribute, new Matrix4f().identity());

    //             //set entity stuuff
    //             EntityUtils.getPosition(branch).set(currentPosition);
    //             EntityUtils.getScale(branch).set(1,1,1);
    //             EntityUtils.getRotation(branch).set(currentAbsoluteRotation);
    //             // AttachUtils.clientAttachEntityAtCurrentOffset(parent, branch);
    //             // AttachUtils.clientAttachEntityAtTransform(parent, branch, attachmentTransform);

    //             //debug stuff
    //             // Vector4f newPositionF = newPositionTransform.transform(new Vector4f(0,0,0,1));
    //             // Vector3d newAbsolutePosition = new Vector3d(newPositionF.x,newPositionF.y,newPositionF.z);
    //             // Entity debugSphere = EntityCreationUtils.createClientSpatialEntity();
    //             // EntityCreationUtils.makeEntityDrawable(debugSphere, "Models/unitsphere_1.fbx");
    //             // EntityUtils.getScale(debugSphere).set(0.5f);
    //             // EntityUtils.getPosition(debugSphere).set(newAbsolutePosition);

    //             //attach leaf blobs
    //             if(
    //                 !isCentralTrunk && 
    //                 currentSegmentNumber >= minimumSegmentToSpawnLeaves
    //             ){
    //                 createLeafBlobsOnBranch(type,rand,transform,boneTransform,branch);
    //             }

    //             //recurse
    //             clientGenerateBranches(
    //                 type,
    //                 branch,
    //                 rand,
    //                 newPositionTransform,
    //                 scalar - scalarFalloffFactor,
    //                 currentSegmentNumber + 1,
    //                 false //can't be central trunk
    //             );
    //         }
    //     }
    // }

    // /**
    //  * Creates leaf blobs around branch segments
    //  * @param type The type of tree
    //  * @param rand The random
    //  * @param transform The current branch segment transform
    //  * @param boneTransform The bone transform to the next branch segment
    //  * @param branch The branch entity
    //  */
    // private static void createLeafBlobsOnBranch(TreeModel type, Random rand, Matrix4f transform, Matrix4f boneTransform, Entity branch){
    //     //how high is the model for a single branch segment
    //     float treeSegmentHeight = type.getBranchHeight();

    //     float minBranchHeightToStartSpawningLeaves = type.getMinBranchHeightToStartSpawningLeaves();
    //     float maxBranchHeightToStartSpawningLeaves = type.getMaxBranchHeightToStartSpawningLeaves();
    //     float leafIncrement = type.getLeafIncrement();
    //     int minLeavesToSpawnPerPoint = type.getMinLeavesToSpawnPerPoint();
    //     int maxLeavesToSpawnPerPoint = type.getMaxLeavesToSpawnPerPoint();
    //     for(
    //         float positionAlongBranch = minBranchHeightToStartSpawningLeaves;
    //         positionAlongBranch < maxBranchHeightToStartSpawningLeaves;
    //         positionAlongBranch = positionAlongBranch + leafIncrement
    //         ){
    //         int numToSpawn = rand.nextInt(maxLeavesToSpawnPerPoint - minLeavesToSpawnPerPoint) + minLeavesToSpawnPerPoint;
    //         double currentLeafRotation = rand.nextFloat();
    //         float distanceFromCenter = type.getLeafDistanceFromCenter();
    //         for(int leafIncrementer = 0; leafIncrementer < numToSpawn; leafIncrementer++){
    //             //offset radially
    //             float xOffset = (float)Math.sin(currentLeafRotation) * distanceFromCenter;
    //             float zOffset = (float)Math.cos(currentLeafRotation) * distanceFromCenter;
    //             //update offsetrotation
    //             currentLeafRotation = currentLeafRotation + (leafIncrementer + 1) * 2.0 * Math.PI / (float)numToSpawn;
    //             //construct model matrix
    //             Matrix4f leafPositionTransform = new Matrix4f(transform).mul(boneTransform).translate(xOffset,positionAlongBranch,zOffset);
    //             Vector4f leafCurrentPositionf = leafPositionTransform.transform(new Vector4f(0,0,0,1));
    //             Vector3d leafCurrentPosition = new Vector3d(leafCurrentPositionf.x,leafCurrentPositionf.y,leafCurrentPositionf.z);
    //             //create entity
    //             Entity leaf = EntityCreationUtils.createClientSpatialEntity();
    //             InstancedActor leafInstancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(leaf, leafInstanceTemplate, modelMatrixAttribute);
    //             leafInstancedActor.setAttribute(modelMatrixAttribute, new Matrix4f().identity());
    //             leafInstancedActor.setAttribute(leafColorAttribute, new Vector3f(36/255.0f,173/255.0f,31/255.0f));

    //             //set entity stuuff
    //             EntityUtils.getPosition(leaf).set(leafCurrentPosition);
    //             EntityUtils.getScale(leaf).set(1,1,1);
    //             EntityUtils.getRotation(leaf).set(new Quaterniond().identity());
    //             AttachUtils.clientAttachEntityAtCurrentOffset(branch, leaf);
    //         }
    //     }
    // }



    /**
     * Creates leaf blobs around branch segments
     * @param type The type of tree
     * @param rand The random
     * @param transform The current branch segment transform
     * @param boneTransform The bone transform to the next branch segment
     * @param branch The branch entity
     */
    private static void createLeafBlobsOnBranch(
        TreeModel type,
        Entity parent,
        Random rand,
        TreeSegment segment
        // Vector3d parentPosition,        // The parent's origin bone's position in space
        // Quaterniond parentRotation,     // The parent's origin bone's rotation
        // Vector3d offsetFromParent,      // The offset from the parent's origin bone that this branch's origin bone should be at
        // Quaternionf rotationFromParent, // The rotation of the parent's extended bone. Should be equivalent to the origin bone's rotation on this branch
        // float scalar,
        // int currentSegmentNumber,
        // boolean isCentralTrunk
    ){
        //get type data
        float minBranchHeightToStartSpawningLeaves = type.getMinBranchHeightToStartSpawningLeaves();
        float maxBranchHeightToStartSpawningLeaves = type.getMaxBranchHeightToStartSpawningLeaves();
        float leafIncrement = type.getLeafIncrement();
        int minLeavesToSpawnPerPoint = type.getMinLeavesToSpawnPerPoint();
        int maxLeavesToSpawnPerPoint = type.getMaxLeavesToSpawnPerPoint();

        for(
            float positionAlongBranch = minBranchHeightToStartSpawningLeaves;
            positionAlongBranch < maxBranchHeightToStartSpawningLeaves;
            positionAlongBranch = positionAlongBranch + leafIncrement
            ){
            int numToSpawn = rand.nextInt(maxLeavesToSpawnPerPoint - minLeavesToSpawnPerPoint) + minLeavesToSpawnPerPoint;
            double currentLeafRotation = rand.nextFloat();
            float distanceFromCenter = type.getLeafDistanceFromCenter();
            for(int leafIncrementer = 0; leafIncrementer < numToSpawn; leafIncrementer++){
                //what we want to solve for:
                //get parent position + rotation
                //get an offset from the parent position for this position
                //get a rotation from the parent rotation for this rotation

                //offset radially
                float xOffset = (float)Math.sin(currentLeafRotation) * distanceFromCenter;
                float zOffset = (float)Math.cos(currentLeafRotation) * distanceFromCenter;

                //update offsetrotation
                currentLeafRotation = currentLeafRotation + (leafIncrementer + 1) * 2.0 * Math.PI / (float)numToSpawn;

                //calculate the transform from parent
                Quaterniond parentLocalRotationFromVertical = new Quaterniond().rotationTo(new Vector3d(0,1,0), segment.offsetFromParent);
                Vector4d transformedPos = parentLocalRotationFromVertical.transform(new Vector4d(xOffset,positionAlongBranch,zOffset,1));

                //calculate transform from parent entity
                //this is the transform that will be applied every time the attachutils updates
                Matrix4d transformFromParent = new Matrix4d()
                    .translate(new Vector3f(
                        (float)transformedPos.x,
                        (float)transformedPos.y,
                        (float)transformedPos.z
                    ))
                    .rotate(new Quaternionf(
                        (float)segment.rotationFromParent.x,
                        (float)segment.rotationFromParent.y,
                        (float)segment.rotationFromParent.z,
                        (float)segment.rotationFromParent.w
                    ));

                
                //create entity
                Entity leaf = EntityCreationUtils.createClientSpatialEntity();
                InstancedActor leafInstancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(leaf, leafInstanceTemplate, modelMatrixAttribute);
                leafInstancedActor.setAttribute(modelMatrixAttribute, new Matrix4d().identity());
                leafInstancedActor.setAttribute(leafColorAttribute, new Vector3f(36/255.0f,173/255.0f,31/255.0f));

                //set entity stuuff
                // EntityUtils.getPosition(leaf).set(leafCurrentPosition);
                EntityUtils.getScale(leaf).set(1,1,1);
                EntityUtils.getRotation(leaf).set(new Quaterniond().identity());
                AttachUtils.clientAttachEntityAtCurrentOffset(parent, leaf);
                // AttachUtils.clientAttachEntityAtCurrentOffset(parent, branch);
                AttachUtils.clientAttachEntityAtTransform(parent, leaf, transformFromParent);
            }
        }
        // for(int i = 0; i < branchNum; i++){

        //     //what we want to solve for:
        //     //get parent position + rotation
        //     //get an offset from the parent position for this position
        //     //get a rotation from the parent rotation for this rotation

        //     Quaterniond parentLocalRotationFromVertical = new Quaterniond().rotationTo(new Vector3d(0,1,0), offsetFromParent);
        //     parentLocalRotationFromVertical.transform(new Vector4d(0,0,0,1));

        //     //calculate transform from parent entity
        //     //this is the transform that will be applied every time the attachutils updates
        //     Matrix4f transformFromParent = new Matrix4f()
        //         .translate(new Vector3f(
        //             (float)offsetFromParent.x,
        //             (float)offsetFromParent.y,
        //             (float)offsetFromParent.z
        //         ))
        //         .rotate(new Quaternionf(
        //             (float)rotationFromParent.x,
        //             (float)rotationFromParent.y,
        //             (float)rotationFromParent.z,
        //             (float)rotationFromParent.w
        //         ));

        //     //calculate combined transform
        //     Matrix4f combinedTransform = new Matrix4f().translate(new Vector3f(
        //         (float)parentPosition.x,
        //         (float)parentPosition.y,
        //         (float)parentPosition.z
        //     )).rotate(new Quaternionf(
        //         (float)parentRotation.x,
        //         (float)parentRotation.y,
        //         (float)parentRotation.z,
        //         (float)parentRotation.w
        //     )).mul(transformFromParent);







        //     //calculate current branch's stuff
        //     //get current position
        //     Vector4f currentPositionf = combinedTransform.transform(new Vector4f(
        //         0,
        //         0,
        //         0,
        //         1
        //     ));
        //     Vector3d currentPosition = new Vector3d(currentPositionf.x,currentPositionf.y,currentPositionf.z);

        //     //The new absolute rotation at the end of the bone
        //     Quaterniond currentAbsoluteRotation = combinedTransform.getNormalizedRotation(new Quaterniond()).normalize();





            


                
        //     //calculate child stuff

        //     //update offsetrotation
        //     offsetRotation = rotationInitialOffset + (i + 1) * (2.0 * Math.PI / (float)branchNum);
        //     //get new rotation
        //     double pitchFactor = Math.sin(offsetRotation);
        //     double rollFactor = Math.cos(offsetRotation);

        //     //the rotation applied to the bone
        //     Quaternionf boneRotation = new Quaternionf(0,0,0,1).rotateLocalX((float)(pitchFactor * peelRotation)).rotateLocalZ((float)(rollFactor * peelRotation)).normalize();


        //     //calculates the bone transform matrix
        //     Matrix4f boneTransform = new Matrix4f().identity().rotate(boneRotation);

        //     //new position transform
        //     Matrix4f newPositionTransform = new Matrix4f().rotate(boneRotation).translate(0,treeSegmentHeight,0);

        //     Vector4f newPositionRaw = newPositionTransform.transform(new Vector4f(0,0,0,1));
        //     Vector3d newPosition = new Vector3d(newPositionRaw.x,newPositionRaw.y,newPositionRaw.z);

        //     //get new scalar
        //     float newScalar = scalar - scalarFalloffFactor;






        //     //create entity

        //     Entity branch = EntityCreationUtils.createClientSpatialEntity();
        //     InstancedActor instancedActor = InstancedEntityUtils.makeEntityInstancedWithModelTransform(branch, branchInstanceTemplate, modelMatrixAttribute);
        //     instancedActor.setAttribute(boneMatrixAttribute, boneTransform.scale(newScalar,1,newScalar));
        //     instancedActor.setAttribute(baseSizeAttribute, scalar);
        //     instancedActor.setAttribute(modelMatrixAttribute, new Matrix4f().identity());

        //     //set entity stuuff
        //     EntityUtils.getPosition(branch).set(currentPosition);
        //     EntityUtils.getScale(branch).set(1,1,1);
        //     EntityUtils.getRotation(branch).set(currentAbsoluteRotation);
        //     // AttachUtils.clientAttachEntityAtCurrentOffset(parent, branch);
        //     AttachUtils.clientAttachEntityAtTransform(parent, branch, transformFromParent);
        // }
    }

    /**
     * A segment in a tree that is being generated
     */
    static class TreeSegment {
        protected double parentPeel;
        protected double parentRotationOffset;
        protected Vector3d parentPosition;          // The parent's origin bone's position in space
        protected Quaterniond parentRotation;       // The parent's origin bone's rotation
        protected Vector3d offsetFromParent;        // The offset from the parent's origin bone that this branch's origin bone should be at
        protected Quaternionf rotationFromParent;   // The rotation of the parent's extended bone. Should be equivalent to the origin bone's rotation on this branch
        protected float scalar;                     //the current size scalar
        protected int currentSegmentNumber;         //the current segment number
        protected boolean isTrunk;                  //true if this is a trunk segment
        protected boolean isBrandAdapter;           //true if this adapts from the trunk to a position on the trunk to branch from

        public TreeSegment(
            double parentPeel,
            double parentRotationOffset,
            Vector3d parentPosition,          // The parent's origin bone's position in space
            Quaterniond parentRotation,       // The parent's origin bone's rotation
            Vector3d offsetFromParent,        // The offset from the parent's origin bone that this branch's origin bone should be at
            Quaternionf rotationFromParent,   // The rotation of the parent's extended bone. Should be equivalent to the origin bone's rotation on this branch
            float scalar,                     //the current size scalar
            int currentSegmentNumber,         //the current segment number
            boolean isTrunk,                  //true if this is a trunk segment
            boolean isBrandAdapter            //true if this adapts from the trunk to a position on the trunk to branch from
        ){
            this.parentPeel = parentPeel;
            this.parentRotationOffset = parentRotationOffset;
            this.parentPosition = parentPosition;
            this.parentRotation = parentRotation;
            this.offsetFromParent = offsetFromParent;
            this.rotationFromParent = rotationFromParent;
            this.scalar = scalar;
            this.currentSegmentNumber = currentSegmentNumber;
            this.isTrunk = isTrunk;
            this.isBrandAdapter = isBrandAdapter;
        }
    }

    /**
     * The behavior tree for branches swaying in the wind
     */
    static class BranchBehaviorTree implements BehaviorTree {


        //The type of tree (controls many params of how the swaying works)
        TreeModel type;

        //The parent that the branch is attached to
        Entity parent;

        //The branch that is having its offset changed
        Entity branch;

        //the initial peel for the branch
        double initialPeel;

        //The current peel for the branch
        double currentPeel;

        //The peel to animate towards
        double targetPeel;

        //the last valid peel
        double lastPeel;

        //The initial yaw for the branch
        double initialYaw;

        //the current yaw for the branch
        double currentYaw;

        //The yaw to animate towards
        double targetYaw;

        //the last valid yaw
        double lastYaw;

        //Every child branch
        List<Entity> children;

        //The current scalar of the branch
        double currentScalar;

        //the current time until the current sway finishes
        int currentTime = 0;

        //the time until the sway finishes
        int maxTime = 0;

        //Offset applied to the time reading for prng measurements for branch sway
        long timeOffset;

        /**
         * Constructor
         * @param type The type of tree
         * @param parent The parent of the current branch model
         * @param branch The entity of the current branch model
         * @param children The list of children attached to the end of this branch model
         * @param currentScalar The current scalar of the current branch model (how wide it be)
         * @param peel The initial peel of the branch
         * @param yaw The initial yaw of the branch
         * @param treeSegmentHeight The height of a given tree branch segment
         * @param timeOffset The time offset applied to this tree branch behavior
         */
        protected BranchBehaviorTree(
            TreeModel type,
            Entity parent,
            Entity branch,
            List<Entity> children,
            double currentScalar,
            double peel,
            double yaw,
            float treeSegmentHeight,
            long timeOffset
        ){
            this.type = type;
            this.parent = parent;
            this.branch = branch;
            this.initialPeel = peel;
            this.currentPeel = peel;
            this.lastPeel = peel;
            this.initialYaw = yaw;
            this.currentYaw = yaw;
            this.lastYaw = yaw;
            this.children = children;
            this.currentScalar = currentScalar;
            this.timeOffset = timeOffset;
        }

        @Override
        public void simulate(float deltaTime) {

            if(currentTime >= maxTime - 1){
                currentTime = 0;
                maxTime = (int)(500 + 500 * new Random().nextDouble());
                targetPeel = initialPeel + ((new Random().nextDouble() - 0.5) * type.getPeelVariance());
                lastPeel = currentPeel;
                targetYaw = initialYaw + ((new Random().nextDouble() - 0.5) * type.getYawVariance());
                lastYaw = currentYaw;
            }

            double currentTimePercentage = (double)currentTime / (double)maxTime;

            double sigmoid = 1.0 / (1.0 + Math.pow((currentTimePercentage / (1.0 - currentTimePercentage)),-type.getSwaySigmoidFactor()));
            currentPeel = targetPeel * sigmoid + lastPeel * (1.0 - sigmoid);
            currentYaw = targetYaw * sigmoid + lastYaw * (1.0 - sigmoid);
            currentTime++;

            //get new rotation
            double pitchFactor = Math.sin(currentYaw);
            double rollFactor = Math.cos(currentYaw);

            //the rotation applied to the bone
            Quaternionf boneRotation = new Quaternionf(0,0,0,1).rotateLocalX((float)(pitchFactor * currentPeel)).rotateLocalZ((float)(rollFactor * currentPeel)).normalize();


            //calculates the bone transform matrix
            // Matrix4d boneTransform = new Matrix4d().identity().rotate(boneRotation);

            //new position transform
            Matrix4d newPositionTransform = new Matrix4d().rotate(boneRotation).translate(0,type.getBranchHeight(),0);
            Vector4d newPositionRaw = newPositionTransform.transform(new Vector4d(0,0,0,1));

            Matrix4d transformFromParent = new Matrix4d()
                .translate(new Vector3f(
                    (float)newPositionRaw.x,
                    (float)newPositionRaw.y,
                    (float)newPositionRaw.z
                ))
                .rotate(new Quaternionf(
                    (float)boneRotation.x,
                    (float)boneRotation.y,
                    (float)boneRotation.z,
                    (float)boneRotation.w
                ));


            //update branch actor branch matrix
            InstancedActor branchActor = InstancedActor.getInstancedActor(branch);
            branchActor.setAttribute(boneMatrixAttribute, new Matrix4d(transformFromParent).scale((float)currentScalar,1,(float)currentScalar));

            //update children positions
            for(Entity child : children){
                AttachUtils.updateAttachTransform(child,transformFromParent);
            }
        }

    }

    
}
