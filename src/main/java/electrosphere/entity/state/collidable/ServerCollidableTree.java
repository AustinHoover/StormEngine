package electrosphere.entity.state.collidable;

import electrosphere.collision.collidable.Collidable;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.ServerEntityUtils;
import electrosphere.entity.btree.BehaviorTree;
import electrosphere.entity.state.gravity.ServerGravityTree;
import electrosphere.entity.state.movement.fall.ServerFallTree;

import org.joml.Vector3d;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;

/**
 * Server collidable tree
 */
public class ServerCollidableTree implements BehaviorTree {
    
    /**
     * The parent of the collidable
     */
    private Entity parent;

    /**
     * The body
     */
    protected DBody body;

    /**
     * The geom
     */
    protected DGeom geom;

    /**
     * The collidable
     */
    private Collidable collidable;
    

    /**
     * Constructor
     * @param e The entity
     * @param collidable The collidable
     * @param body The body
     */
    public ServerCollidableTree(Entity e, Collidable collidable, DBody body){
        parent = e;
        this.collidable = collidable;
        this.body = body;
    }

    /**
     * Constructor
     * @param e The entity
     * @param collidable The collidable
     * @param geom The Geom
     */
    public ServerCollidableTree(Entity e, Collidable collidable, DGeom geom){
        parent = e;
        this.collidable = collidable;
        this.geom = geom;
    }
    
    /**
     * Simulates the collidable tree
     * @param deltaTime The amount of time to simulate by
     */
    public void simulate(float deltaTime){
        //have we hit a terrain impulse?
        //handle impulses
        Impulse[] impulses = collidable.getImpulses();
        Vector3d pos = EntityUtils.getPosition(parent);
        for(int i = 0; i < collidable.getImpulseCount(); i++){
            if(impulses[i].type.equals(Collidable.TYPE_CREATURE)){
                if(ServerGravityTree.getServerGravityTree(parent)!=null){
                    ServerGravityTree.getServerGravityTree(parent).start();
                }
            }
            if(impulses[i].type.equals(Collidable.TYPE_WORLD_BOUND) || impulses[i].type.equals(Collidable.TYPE_STATIC)){
                this.resetGravityFall();
                pos.add(impulses[i].getDirection().mul(impulses[i].getForce()));
            }
        }
        if(geom != null){
            ServerEntityUtils.repositionEntity(parent, pos);
        }

        collidable.setReady(true);
        
        //capsule-specific block collision logic
        // if(body.isEnabled() && body.getFirstGeom() != null && (body.getFirstGeom() instanceof DCapsule)){
        //     Realm realm = Globals.serverState.realmManager.getEntityRealm(parent);
        //     if(realm.getDataCellManager() instanceof VoxelCellManager){
        //         VoxelCellManager voxelCellManager = (VoxelCellManager)realm.getDataCellManager();

        //         //get capsule params
        //         DCapsule capsuleGeom = (DCapsule)body.getFirstGeom();
        //         double length = capsuleGeom.getLength();
        //         double halfLength = length / 2.0;
        //         double radius = capsuleGeom.getRadius();
        //         Vector3d bodyOffset = PhysicsUtils.odeVecToJomlVec(body.getFirstGeom().getOffsetPosition());

        //         //entity spatial transforms
        //         Vector3d entRealPos = EntityUtils.getPosition(parent);
        //         Quaterniond entRot = EntityUtils.getRotation(parent);

        //         //start and end of capsule
        //         Vector3d realStart = new Vector3d(0,-halfLength,0).rotate(entRot).add(entRealPos).add(bodyOffset);
        //         Vector3d realEnd = new Vector3d(0,halfLength,0).rotate(entRot).add(entRealPos).add(bodyOffset);


        //         //block position of body
        //         Vector3d blockPos = ServerWorldData.clampRealToBlock(entRealPos);
        //         Vector3d currBlockPos = new Vector3d();

        //         //get dims to scan along (ceil to overcompensate -- better to over scan than underscan)
        //         int halfRadBlockLen = (int)Math.ceil(halfLength / BlockChunkData.BLOCK_SIZE_MULTIPLIER);
        //         int radBlockLen = (int)Math.ceil(radius / BlockChunkData.BLOCK_SIZE_MULTIPLIER);

        //         //final corrected position
        //         Vector3d corrected = new Vector3d(entRealPos);

        //         //scan for all potential blocks
        //         for(int x = -radBlockLen; x <= radBlockLen; x++){
        //             for(int z = -radBlockLen; z <= radBlockLen; z++){
        //                 for(int y = -halfRadBlockLen; y <= halfRadBlockLen; y++){
        //                     currBlockPos.set(blockPos).add(
        //                         x * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
        //                         y * BlockChunkData.BLOCK_SIZE_MULTIPLIER,
        //                         z * BlockChunkData.BLOCK_SIZE_MULTIPLIER
        //                     );
        //                     Vector3i chunkPos = ServerWorldData.convertRealToChunkSpace(currBlockPos);
        //                     Vector3i entBlockPos = ServerWorldData.convertRealToLocalBlockSpace(currBlockPos);

        //                     //error check bounds
        //                     if(chunkPos.x < 0 || chunkPos.y < 0 || chunkPos.z < 0){
        //                         continue;
        //                     }
                            
        //                     //get block data for block to check
        //                     BlockChunkData data = voxelCellManager.getBlocksAtPosition(chunkPos);
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

        //         //apply correction
        //         CollisionObjUtils.serverPositionCharacter(parent, corrected);
        //     }
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
     * Checks if the entity has a server collidable tree
     * @param e The entity
     * @return true if it has a collidable tree, false otherwise
     */
    public static boolean hasServerCollidableTree(Entity e){
        return e.containsKey(EntityDataStrings.SERVER_COLLIDABLE_TREE);
    }

    /**
     * Gets the server collidable tree on an entity
     * @param e The entity
     * @return The tree if it exists, false otherwise
     */
    public static ServerCollidableTree getServerCollidableTree(Entity e){
        return (ServerCollidableTree)e.getData(EntityDataStrings.SERVER_COLLIDABLE_TREE);
    }

    /**
     * Adds a terrain collision to the collidable list
     */
    protected void resetGravityFall(){
        if(ServerGravityTree.getServerGravityTree(parent)!=null){
            ServerGravityTree.getServerGravityTree(parent).stop();
        }
        if(ServerFallTree.getFallTree(parent)!=null){
            ServerFallTree.getFallTree(parent).land();
        }
    }
    
    
}
