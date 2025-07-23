package electrosphere.entity.state.collidable;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.ode4j.ode.DBody;

import electrosphere.collision.PhysicsUtils;
import electrosphere.collision.collidable.Collidable;
import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.btree.BehaviorTree;

/**
 * Client collidable tree
 */
public class ClientCollidableTree implements BehaviorTree {
    
    /**
     * The parent entity of this tree
     */
    private Entity parent;

    /**
     * The body for this tree
     */
    protected DBody body;

    /**
     * The collidable for this tree
     */
    protected Collidable collidable;

    /**
     * Constructor
     * @param e The entity
     * @param collidable The collidable
     * @param body The body
     */
    public ClientCollidableTree(Entity e, Collidable collidable, DBody body){
        parent = e;
        this.collidable = collidable;
        this.body = body;
    }
    

    /**
     * Simulates the component
     */
    public void simulate(float deltaTime){
        Vector3d position = EntityUtils.getPosition(parent);
        Quaterniond rotation = EntityUtils.getRotation(parent);
        Vector3d newPosition = new Vector3d(position);
        //bound to world bounds
        if(Globals.clientState.clientWorldData != null){
            if(newPosition.x < Globals.clientState.clientWorldData.getWorldBoundMin().x){
                newPosition.x = Globals.clientState.clientWorldData.getWorldBoundMin().x;
            }
            if(newPosition.y < Globals.clientState.clientWorldData.getWorldBoundMin().y){
                newPosition.y = Globals.clientState.clientWorldData.getWorldBoundMin().y;
            }
            if(newPosition.z < Globals.clientState.clientWorldData.getWorldBoundMin().z){
                newPosition.z = Globals.clientState.clientWorldData.getWorldBoundMin().z;
            }
        }
        PhysicsUtils.setRigidBodyTransform(Globals.clientState.clientSceneWrapper.getCollisionEngine(), newPosition, rotation, body);

        collidable.setReady(true);

        //capsule-specific block collision logic
        // if(body.isEnabled() && body.getFirstGeom() != null && (body.getFirstGeom() instanceof DCapsule)){
        //     //get capsule params
        //     DCapsule capsuleGeom = (DCapsule)body.getFirstGeom();
        //     double length = capsuleGeom.getLength();
        //     double halfLength = length / 2.0;
        //     double radius = capsuleGeom.getRadius();
        //     Vector3d bodyOffset = PhysicsUtils.odeVecToJomlVec(body.getFirstGeom().getOffsetPosition());

        //     //entity spatial transforms
        //     Vector3d entRealPos = EntityUtils.getPosition(parent);
        //     Quaterniond entRot = EntityUtils.getRotation(parent);

        //     //start and end of capsule
        //     Vector3d realStart = new Vector3d(0,-halfLength,0).rotate(entRot).add(entRealPos).add(bodyOffset);
        //     Vector3d realEnd = new Vector3d(0,halfLength,0).rotate(entRot).add(entRealPos).add(bodyOffset);


        //     //block position of body
        //     Vector3d blockPos = ClientWorldData.clampRealToBlock(entRealPos);
        //     Vector3d currBlockPos = new Vector3d();

        //     //get dims to scan along (ceil to overcompensate -- better to over scan than underscan)
        //     int halfRadBlockLen = (int)Math.ceil(halfLength / BlockChunkData.BLOCK_SIZE_MULTIPLIER);
        //     int radBlockLen = (int)Math.ceil(radius / BlockChunkData.BLOCK_SIZE_MULTIPLIER);

        //     //final corrected position
        //     Vector3d corrected = new Vector3d(entRealPos);

        //     //scan for all potential blocks
        //     for(int x = -radBlockLen; x <= radBlockLen; x++){
        //         for(int z = -radBlockLen; z <= radBlockLen; z++){
        //             for(int y = -halfRadBlockLen; y <= halfRadBlockLen; y++){
        //                 currBlockPos.set(blockPos).add(
        //                     x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
        //                     y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
        //                     z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
        //                 );
        //                 Vector3i chunkPos = ClientWorldData.convertRealToChunkSpace(currBlockPos);
        //                 Vector3i entBlockPos = ClientWorldData.convertRealToLocalBlockSpace(currBlockPos);

        //                 //error check bounds
        //                 if(chunkPos.x < 0 || chunkPos.y < 0 || chunkPos.z < 0){
        //                     continue;
        //                 }
                        
        //                 //get block data for block to check
        //                 BlockChunkData data = Globals.clientState.clientBlockManager.getChunkDataAtWorldPoint(chunkPos, BlockChunkData.LOD_FULL_RES);
        //                 if(data != null){
        //                     //check type of voxel to skip math
        //                     short type = data.getType(entBlockPos.x, entBlockPos.y, entBlockPos.z);
        //                     if(type != BlockChunkData.BLOCK_TYPE_EMPTY){

        //                         //AABB for the voxel
        //                         AABBd voxelBox = new AABBd(
        //                             currBlockPos.x,
        //                             currBlockPos.y,
        //                             currBlockPos.z,
        //                             currBlockPos.x + BlockChunkData.BLOCK_SIZE_MULTIPLIER,
        //                             currBlockPos.y + BlockChunkData.BLOCK_SIZE_MULTIPLIER,
        //                             currBlockPos.z + BlockChunkData.BLOCK_SIZE_MULTIPLIER
        //                         );

        //                         //actually collision check
        //                         CollisionResult collisionResult = CollisionUtils.collideCapsuleAABB(realStart, realEnd, radius, voxelBox);
        //                         if(collisionResult != null){
        //                             double pen = collisionResult.getPenetration();
        //                             double forceMul = pen * 0.3;
        //                             Vector3d normal = collisionResult.getNormal().mul(forceMul);
        //                             if(normal != null){
        //                                 // body.addForce(normal.x, normal.y, normal.z);
        //                                 //correct the position of the capsule
        //                                 corrected.add(normal);
        //                             }
        //                         }
        //                     }
        //                 }
        //             }
        //         }
        //     }

        //     //apply correction
        //     CollisionObjUtils.clientPositionCharacter(parent, newPosition, entRot);
        // }
    }
    
    /**
     * Sets the structures backing this collidable tree
     * @param body The ode body
     * @param collidable The collidable
     */
    public void setCollisionObject(DBody body, Collidable collidable){
        this.body = body;
        this.collidable = collidable;
    }

    /**
     * Checks if the entity has a collidable tree
     * @param e The entity
     * @return true if the entity contains a collidable tree, false otherwise
     */
    public static boolean hasClientCollidableTree(Entity e){
        return e.containsKey(EntityDataStrings.CLIENT_COLLIDABLE_TREE);
    }

    /**
     * Gets the collidable tree on the entity
     * @param e The entity
     * @return The tree
     */
    public static ClientCollidableTree getClientCollidableTree(Entity e){
        return (ClientCollidableTree)e.getData(EntityDataStrings.CLIENT_COLLIDABLE_TREE);
    }
    
    
}
