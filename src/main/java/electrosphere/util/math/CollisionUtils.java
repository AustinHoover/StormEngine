package electrosphere.util.math;

import org.joml.AABBd;
import org.joml.Vector3d;

import electrosphere.collision.physics.CollisionResult;
import io.github.studiorailgun.MathUtils;

/**
 * Utilities for performing collisions of geometry
 */
public class CollisionUtils {
    
    /**
     * Checks if a capsule intersects an AABB
     * @param capsuleStart The start of the capsule
     * @param capsuleEnd The end of the capsule
     * @param radius The radius of the capsule
     * @param box The box
     * @return true if they intersect, false otherwise
     */
    public static boolean capsuleIntersectsAABB(Vector3d start, Vector3d end, double radius, AABBd box){
        // Step 1: Find closest point on capsule segment to the AABB
        // Approximate by projecting the center of the AABB onto the segment
        double boxCenterX = (box.minX + box.maxX) * 0.5;
        double boxCenterY = (box.minY + box.maxY) * 0.5;
        double boxCenterZ = (box.minZ + box.maxZ) * 0.5;

        double abX = end.x - start.x;
        double abY = end.y - start.y;
        double abZ = end.z - start.z;

        double lenSquared = (boxCenterX - abX) * (boxCenterX - abX) + (boxCenterY - abY) * (boxCenterY - abY) + (boxCenterZ - abZ) * (boxCenterZ - abZ);

        double t = MathUtils.dot(
            boxCenterX - start.x,
            boxCenterY - start.y,
            boxCenterZ - start.z,
            abX,
            abY,
            abZ
        ) / lenSquared;
        t = Math.max(0f, Math.min(1f, t)); // clamp to [0,1]

        double segClosesX = start.x + (abX * t);
        double segClosesY = start.y + (abY * t);
        double segClosesZ = start.z + (abZ * t);
        
        // Step 2: Find closest point on AABB to that segment point
        double boxClosestX = MathUtils.clamp(segClosesX, box.minX, box.maxX);
        double boxClosestY = MathUtils.clamp(segClosesY, box.minY, box.maxY);
        double boxClosestZ = MathUtils.clamp(segClosesZ, box.minZ, box.maxZ);
        
        // Step 3: Compute distance squared
        double diffX = segClosesX - boxClosestX;
        double diffY = segClosesY - boxClosestY;
        double diffZ = segClosesZ - boxClosestZ;

        double distSq = (diffX * diffX) + (diffY * diffY) + (diffZ * diffZ);
        
        return distSq <= radius * radius;
    }

    /**
     * Checks if a capsule intersects an AABB
     * @param capsuleStart The start of the capsule
     * @param capsuleEnd The end of the capsule
     * @param radius The radius of the capsule
     * @param box The box
     * @return true if they intersect, false otherwise
     */
    public static CollisionResult collideCapsuleAABB(Vector3d start, Vector3d end, double radius, AABBd box){
        CollisionResult rVal = new CollisionResult();
        // Step 1: Find closest point on capsule segment to the AABB
        // Approximate by projecting the center of the AABB onto the segment
        double boxCenterX = (box.minX + box.maxX) * 0.5;
        double boxCenterY = (box.minY + box.maxY) * 0.5;
        double boxCenterZ = (box.minZ + box.maxZ) * 0.5;

        double abX = end.x - start.x;
        double abY = end.y - start.y;
        double abZ = end.z - start.z;

        double lenSquared = (boxCenterX - abX) * (boxCenterX - abX) + (boxCenterY - abY) * (boxCenterY - abY) + (boxCenterZ - abZ) * (boxCenterZ - abZ);

        double t = MathUtils.dot(
            boxCenterX - start.x,
            boxCenterY - start.y,
            boxCenterZ - start.z,
            abX,
            abY,
            abZ
        ) / lenSquared;
        t = Math.max(0f, Math.min(1f, t)); // clamp to [0,1]

        double segClosesX = start.x + (abX * t);
        double segClosesY = start.y + (abY * t);
        double segClosesZ = start.z + (abZ * t);
        
        // Step 2: Find closest point on AABB to that segment point
        double boxClosestX = MathUtils.clamp(segClosesX, box.minX, box.maxX);
        double boxClosestY = MathUtils.clamp(segClosesY, box.minY, box.maxY);
        double boxClosestZ = MathUtils.clamp(segClosesZ, box.minZ, box.maxZ);
        
        // Step 3: Compute distance squared
        double diffX = segClosesX - boxClosestX;
        double diffY = segClosesY - boxClosestY;
        double diffZ = segClosesZ - boxClosestZ;

        //compute distance squared
        double distSq = (diffX * diffX) + (diffY * diffY) + (diffZ * diffZ);

        //early return if no collision occurred
        if(distSq > radius * radius){
            return null;
        }

        //compute distance
        double dist = Math.sqrt(distSq);

        //compute penetration
        double penetration = radius - dist;
        rVal.setPenetration(penetration);

        //compute normal
        Vector3d normal = null;
        if(dist > 1e-6){
            normal = new Vector3d(diffX,diffY,diffZ).mul(1 / dist);
        }
        rVal.setNormal(normal);


        return rVal;
    }

}
