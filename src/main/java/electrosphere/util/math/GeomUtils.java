package electrosphere.util.math;

import org.joml.AABBd;
import org.joml.Intersectiond;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.util.ds.Spline3d;

/**
 * Utilities for dealing with geometry
 */
public class GeomUtils {

    /**
     * A really big number
     */
    public static final int REALLY_BIG_NUMBER = 1000000;

    /**
     * Cutoff after which we just take the distance between the current axis
     */
    public static final int SIMPLIFICATION_CUTOFF = 100000;

    /**
     * Amount to sample splines by
     */
    public static final int SPLINE_SAMPLE_RATE = 10;

    /**
     * Gets the minimum distance from a point to an axis aligned cube
     * @param pos the position to check against
     * @param cubeMin The min position of the cube
     * @param cubeMax The max position of the cube
     * @return the distance
     */
    public static double getMinDistanceAABB(Vector3d pos, Vector3d cubeMin, Vector3d cubeMax){
        double minX = cubeMin.x;
        double minY = cubeMin.y;
        double minZ = cubeMin.z;
        double maxX = cubeMax.x;
        double maxY = cubeMax.y;
        double maxZ = cubeMax.z;
        if(pos.x > minX && pos.x < maxX){
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return 0;
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(pos.x,pos.y,minZ);
                } else {
                    return pos.distance(pos.x,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(pos.x,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(pos.x,minY,minZ);
                } else {
                    return pos.distance(pos.x,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(pos.x,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(pos.x,maxY,minZ);
                } else {
                    return pos.distance(pos.x,maxY,maxZ);
                }
            }
        } else if(Math.abs(pos.x - minX) < Math.abs(pos.x - maxX)){
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(minX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(minX,pos.y,minZ);
                } else {
                    return pos.distance(minX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(minX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(minX,minY,minZ);
                } else {
                    return pos.distance(minX,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(minX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(minX,maxY,minZ);
                } else {
                    return pos.distance(minX,maxY,maxZ);
                }
            }
        } else {
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(maxX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(maxX,pos.y,minZ);
                } else {
                    return pos.distance(maxX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(maxX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(maxX,minY,minZ);
                } else {
                    return pos.distance(maxX,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(maxX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(maxX,maxY,minZ);
                } else {
                    return pos.distance(maxX,maxY,maxZ);
                }
            }
        }
    }

    /**
     * Gets the minimum squared distance from a point to an axis aligned cube
     * @param pos the position to check against
     * @param cubeMin The min position of the cube
     * @param cubeMax The max position of the cube
     * @return the distance
     */
    public static double approxMinDistanceAABB(Vector3i pos, Vector3i cubeMin, Vector3i cubeMax){
        int minX = cubeMin.x;
        int minY = cubeMin.y;
        int minZ = cubeMin.z;
        int maxX = cubeMax.x;
        int maxY = cubeMax.y;
        int maxZ = cubeMax.z;
        if(pos.x >= minX && pos.x <= maxX){
            if(pos.y >= minY && pos.y <= maxY){
                if(pos.z >= minZ && pos.z <= maxZ){
                    return 0;
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(pos.x,pos.y,minZ);
                } else {
                    return pos.distance(pos.x,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(Math.abs(pos.y - minY) > SIMPLIFICATION_CUTOFF){
                    return Math.abs(pos.y - minY);
                }
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(pos.x,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(pos.x,minY,minZ);
                } else {
                    return pos.distance(pos.x,minY,maxZ);
                }
            } else {
                if(Math.abs(pos.y - maxY) > SIMPLIFICATION_CUTOFF){
                    return Math.abs(pos.y - maxY);
                }
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(pos.x,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(pos.x,maxY,minZ);
                } else {
                    return pos.distance(pos.x,maxY,maxZ);
                }
            }
        } else if(Math.abs(pos.x - minX) < Math.abs(pos.x - maxX)){
            if(Math.abs(pos.x - minX) > SIMPLIFICATION_CUTOFF){
                return Math.abs(pos.x - minX);
            }
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(minX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(minX,pos.y,minZ);
                } else {
                    return pos.distance(minX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(Math.abs(pos.y - minY) > SIMPLIFICATION_CUTOFF){
                    return Math.abs(pos.y - minY);
                }
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(minX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(minX,minY,minZ);
                } else {
                    return pos.distance(minX,minY,maxZ);
                }
            } else {
                if(Math.abs(pos.y - maxY) > SIMPLIFICATION_CUTOFF){
                    return Math.abs(pos.y - maxY);
                }
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(minX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(minX,maxY,minZ);
                } else {
                    return pos.distance(minX,maxY,maxZ);
                }
            }
        } else {
            if(Math.abs(pos.x - maxX) > SIMPLIFICATION_CUTOFF){
                return Math.abs(pos.x - maxX);
            }
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(maxX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(maxX,pos.y,minZ);
                } else {
                    return pos.distance(maxX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(Math.abs(pos.y - minY) > SIMPLIFICATION_CUTOFF){
                    return Math.abs(pos.y - minY);
                }
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(maxX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(maxX,minY,minZ);
                } else {
                    return pos.distance(maxX,minY,maxZ);
                }
            } else {
                if(Math.abs(pos.y - maxY) > SIMPLIFICATION_CUTOFF){
                    return Math.abs(pos.y - maxY);
                }
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distance(maxX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distance(maxX,maxY,minZ);
                } else {
                    return pos.distance(maxX,maxY,maxZ);
                }
            }
        }
    }

    /**
     * Gets the minimum squared distance from a point to an axis aligned cube
     * @param pos the position to check against
     * @param cubeMin The min position of the cube
     * @param cubeMax The max position of the cube
     * @return the distance
     */
    public static double getMinSquaredDistanceAABB(Vector3d pos, Vector3d cubeMin, Vector3d cubeMax){
        double minX = cubeMin.x;
        double minY = cubeMin.y;
        double minZ = cubeMin.z;
        double maxX = cubeMax.x;
        double maxY = cubeMax.y;
        double maxZ = cubeMax.z;
        if(pos.x > minX && pos.x < maxX){
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return 0;
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(pos.x,pos.y,minZ);
                } else {
                    return pos.distanceSquared(pos.x,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(pos.x,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(pos.x,minY,minZ);
                } else {
                    return pos.distanceSquared(pos.x,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(pos.x,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(pos.x,maxY,minZ);
                } else {
                    return pos.distanceSquared(pos.x,maxY,maxZ);
                }
            }
        } else if(Math.abs(pos.x - minX) < Math.abs(pos.x - maxX)){
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(minX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(minX,pos.y,minZ);
                } else {
                    return pos.distanceSquared(minX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(minX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(minX,minY,minZ);
                } else {
                    return pos.distanceSquared(minX,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(minX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(minX,maxY,minZ);
                } else {
                    return pos.distanceSquared(minX,maxY,maxZ);
                }
            }
        } else {
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(maxX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(maxX,pos.y,minZ);
                } else {
                    return pos.distanceSquared(maxX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(maxX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(maxX,minY,minZ);
                } else {
                    return pos.distanceSquared(maxX,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(maxX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(maxX,maxY,minZ);
                } else {
                    return pos.distanceSquared(maxX,maxY,maxZ);
                }
            }
        }
    }

    /**
     * Gets the minimum squared distance from a point to an axis aligned cube
     * @param pos the position to check against
     * @param cubeMin The min position of the cube
     * @param cubeMax The max position of the cube
     * @return the distance
     */
    public static long getMinSquaredDistanceAABB(Vector3i pos, Vector3i cubeMin, Vector3i cubeMax){
        int minX = cubeMin.x;
        int minY = cubeMin.y;
        int minZ = cubeMin.z;
        int maxX = cubeMax.x;
        int maxY = cubeMax.y;
        int maxZ = cubeMax.z;
        if(pos.x > minX && pos.x < maxX){
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return 0;
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(pos.x,pos.y,minZ);
                } else {
                    return pos.distanceSquared(pos.x,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(pos.x,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(pos.x,minY,minZ);
                } else {
                    return pos.distanceSquared(pos.x,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(pos.x,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(pos.x,maxY,minZ);
                } else {
                    return pos.distanceSquared(pos.x,maxY,maxZ);
                }
            }
        } else if(Math.abs(pos.x - minX) < Math.abs(pos.x - maxX)){
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(minX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(minX,pos.y,minZ);
                } else {
                    return pos.distanceSquared(minX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(minX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(minX,minY,minZ);
                } else {
                    return pos.distanceSquared(minX,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(minX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(minX,maxY,minZ);
                } else {
                    return pos.distanceSquared(minX,maxY,maxZ);
                }
            }
        } else {
            if(pos.y > minY && pos.y < maxY){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(maxX,pos.y,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(maxX,pos.y,minZ);
                } else {
                    return pos.distanceSquared(maxX,pos.y,maxZ);
                }
            } else if(Math.abs(pos.y - minY) < Math.abs(pos.y - maxY)){
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(maxX,minY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(maxX,minY,minZ);
                } else {
                    return pos.distanceSquared(maxX,minY,maxZ);
                }
            } else {
                if(pos.z > minZ && pos.z < maxZ){
                    return pos.distanceSquared(maxX,maxY,pos.z);
                } else if(Math.abs(pos.z - minZ) < Math.abs(pos.z - maxZ)){
                    return pos.distanceSquared(maxX,maxY,minZ);
                } else {
                    return pos.distanceSquared(maxX,maxY,maxZ);
                }
            }
        }
    }

    /**
     * Gets the minimum squared distance from a point to an axis aligned cube
     * @param pos the position to check against
     * @param cubeMin The min position of the cube
     * @param cubeMax The max position of the cube
     * @return the distance
     */
    public static double getMinSquaredDistanceAABBUnrolled(int posX, int posY, int posZ, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        if(posX > minX && posX < maxX){
            if(posY > minY && posY < maxY){
                if(posZ > minZ && posZ < maxZ){
                    return 0;
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posZ - minZ) * (posZ - minZ);// pos.distanceSquared(posX,posY,minZ);
                } else {
                    return (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(posX,posY,maxZ);
                }
            } else if(Math.abs(posY - minY) < Math.abs(posY - maxY)){
                if(posZ > minZ && posZ < maxZ){
                    return (posY - minY) * (posY - minY);//pos.distanceSquared(posX,minY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posY - minY) * (posY - minY) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(posX,minY,minZ);
                } else {
                    return (posY - minY) * (posY - minY) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(posX,minY,maxZ);
                }
            } else {
                if(posZ > minZ && posZ < maxZ){
                    return (posY - maxY) * (posY - maxY);//pos.distanceSquared(posX,maxY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posY - maxY) * (posY - maxY) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(posX,maxY,minZ);
                } else {
                    return (posY - maxY) * (posY - maxY) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(posX,maxY,maxZ);
                }
            }
        } else if(Math.abs(posX - minX) < Math.abs(posX - maxX)){
            if(posY > minY && posY < maxY){
                if(posZ > minZ && posZ < maxZ){
                    return (posX - minX) * (posX - minX);//pos.distanceSquared(minX,posY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posX - minX) * (posX - minX) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(minX,posY,minZ);
                } else {
                    return (posX - minX) * (posX - minX) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(minX,posY,maxZ);
                }
            } else if(Math.abs(posY - minY) < Math.abs(posY - maxY)){
                if(posZ > minZ && posZ < maxZ){
                    return (posX - minX) * (posX - minX);//pos.distanceSquared(minX,minY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posX - minX) * (posX - minX) + (posY - minY) * (posY - minY) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(minX,minY,minZ);
                } else {
                    return (posX - minX) * (posX - minX) + (posY - minY) * (posY - minY) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(minX,minY,maxZ);
                }
            } else {
                if(posZ > minZ && posZ < maxZ){
                    return (posX - minX) * (posX - minX) + (posY - maxY) * (posY - maxY);//pos.distanceSquared(minX,maxY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posX - minX) * (posX - minX) + (posY - maxY) * (posY - maxY) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(minX,maxY,minZ);
                } else {
                    return (posX - minX) * (posX - minX) + (posY - maxY) * (posY - maxY) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(minX,maxY,maxZ);
                }
            }
        } else {
            if(posY > minY && posY < maxY){
                if(posZ > minZ && posZ < maxZ){
                    return (posX - maxX) * (posX - maxX);//pos.distanceSquared(maxX,posY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posX - maxX) * (posX - maxX) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(maxX,posY,minZ);
                } else {
                    return (posX - maxX) * (posX - maxX) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(maxX,posY,maxZ);
                }
            } else if(Math.abs(posY - minY) < Math.abs(posY - maxY)){
                if(posZ > minZ && posZ < maxZ){
                    return (posX - maxX) * (posX - maxX);//pos.distanceSquared(maxX,minY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posX - maxX) * (posX - maxX) + (posY - minY) * (posY - minY) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(maxX,minY,minZ);
                } else {
                    return (posX - maxX) * (posX - maxX) + (posY - minY) * (posY - minY) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(maxX,minY,maxZ);
                }
            } else {
                if(posZ > minZ && posZ < maxZ){
                    return (posX - maxX) * (posX - maxX) + (posY - maxY) * (posY - maxY);//pos.distanceSquared(maxX,maxY,posZ);
                } else if(Math.abs(posZ - minZ) < Math.abs(posZ - maxZ)){
                    return (posX - maxX) * (posX - maxX) + (posY - maxY) * (posY - maxY) + (posZ - minZ) * (posZ - minZ);//pos.distanceSquared(maxX,maxY,minZ);
                } else {
                    return (posX - maxX) * (posX - maxX) + (posY - maxY) * (posY - maxY) + (posZ - maxZ) * (posZ - maxZ);//pos.distanceSquared(maxX,maxY,maxZ);
                }
            }
        }
    }

    /**
     * Checks the winding of a given set of geometry
     * @param verts The vertices
     * @param indices The indices
     */
    public static void checkWinding(float[] verts, int[] indices) {
        for (int i = 0; i < indices.length; i += 3) {
            int ia = indices[i] * 3;
            int ib = indices[i + 1] * 3;
            int ic = indices[i + 2] * 3;
    
            float ax = verts[ia];
            float az = verts[ia + 2];
            float bx = verts[ib];
            float bz = verts[ib + 2];
            float cx = verts[ic];
            float cz = verts[ic + 2];
    
            // Compute signed area of the triangle in XZ plane
            float signedArea = (bx - ax) * (cz - az) - (cx - ax) * (bz - az);
    
            if (signedArea < 0) {
                System.out.println("Triangle " + (i / 3) + " is wound CLOCKWISE (probably incorrect)");
            } else if (signedArea > 0) {
                System.out.println("Triangle " + (i / 3) + " is wound COUNTER-CLOCKWISE (likely correct)");
            } else {
                System.out.println("Triangle " + (i / 3) + " is degenerate (zero area)");
            }
        }
    }

    /**
     * Checks that the winding of the geometry it clockwise
     * @param verts The verts
     * @param indices The indices
     * @return true if all triangles are wound clockwise, false otherwise
     */
    public static boolean isWindingClockwise(float[] verts, int[] indices){
        for (int i = 0; i < indices.length; i += 3) {
            int ia = indices[i] * 3;
            int ib = indices[i + 1] * 3;
            int ic = indices[i + 2] * 3;
    
            float ax = verts[ia];
            float az = verts[ia + 2];
            float bx = verts[ib];
            float bz = verts[ib + 2];
            float cx = verts[ic];
            float cz = verts[ic + 2];
    
            // Compute signed area of the triangle in XZ plane
            float signedArea = (bx - ax) * (cz - az) - (cx - ax) * (bz - az);
    
            if (signedArea > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the squared distance between a spline and a point
     * @param point The point
     * @param spline The tube's spline
     * @param samplesPerSegment The samples per segment
     * @return the distance squared
     */
    public static double getPointSplineDist(Vector3d point, Spline3d spline, int samplesPerSegment) {
        double closestDist = Double.POSITIVE_INFINITY;
        float incrementAmount = 1/(float)samplesPerSegment;

        for(double t = 1; t < spline.getPoints().size() - 1; t=t+incrementAmount){
            Vector3d splinePoint = spline.getPos(t);
            double dist = splinePoint.distance(point);

            if(dist < closestDist){
                closestDist = dist;
            }
        }

        return closestDist;
    }

    /**
     * Gets the squared distance between a spline and a point
     * @param point The point
     * @param spline The tube's spline
     * @return the distance squared
     */
    public static double getPointSplineDist(Vector3d point, Spline3d spline) {
        return GeomUtils.getPointSplineDist(point, spline, SPLINE_SAMPLE_RATE);
    }

    /**
     * Checks whether a point intersects a tube defined by a spline
     * @param point The point
     * @param spline The tube's spline
     * @param radius The radius of the tube around the spline
     * @param samplesPerSegment The samples per segment
     * @return true if they intersect, false otherwise
     */
    public static boolean pointIntersectsSpline(Vector3d point, Spline3d spline, double radius, int samplesPerSegment) {
        double closestDistSquared = Double.POSITIVE_INFINITY;
        float incrementAmount = 1/(float)samplesPerSegment;

        for(double t = 1; t < spline.getPoints().size() - 1; t=t+incrementAmount){
            Vector3d splinePoint = spline.getPos(t);
            double distSquared = splinePoint.distance(point);
            if(distSquared < radius){
                return true; // early exit
            }

            if(distSquared < closestDistSquared){
                closestDistSquared = distSquared;
            }
        }

        return false;
    }

    /**
     * Checks whether a point intersects a tube defined by a spline
     * @param point The point
     * @param spline The tube's spline
     * @param radius The radius of the tube around the spline
     * @param samplesPerSegment The samples per segment
     * @return true if they intersect, false otherwise
     */
    public static boolean pointIntersectsSpline(Vector3d point, Spline3d spline, double radius) {
        return GeomUtils.pointIntersectsSpline(point, spline, radius, SPLINE_SAMPLE_RATE);
    }

    /**
     * Checks whether a point intersects a tube defined by a line
     * @param point The point
     * @param lineStart The start of the line segment
     * @param lineEnd The end of the line segment
     * @param radius The radius of the tube around the line
     * @return true if they intersect, false otherwise
     */
    public static boolean pointIntersectsLineSegment(Vector3d point, Vector3d lineStart, Vector3d lineEnd, double radius) {
        double dist = Intersectiond.distancePointLine(point.x, point.y, point.z, lineStart.x, lineStart.y, lineStart.z, lineEnd.x, lineEnd.y, lineEnd.z);
        return dist < radius;
    }

    /**
     * Tests if an aabb intersects a tube
     * @param aabb The aabb
     * @param tubeStart The start of the tube
     * @param tubeEnd The end of the tube
     * @param radius The radius of the tube
     * @return true if they intersect, false
     */
    public static boolean intersectAABBTube(AABBd aabb, Vector3d tubeStart, Vector3d tubeEnd, double radius){
        Vector2d resDat = new Vector2d();
        int res = Intersectiond.intersectLineSegmentAab(
            tubeStart.x, tubeStart.y, tubeStart.z,
            tubeEnd.x, tubeEnd.y, tubeEnd.z,
            aabb.minX - radius, aabb.minY - radius, aabb.minZ - radius,
            aabb.maxX + radius, aabb.maxY + radius, aabb.maxZ + radius,
            resDat
        );
        if(res != Intersectiond.OUTSIDE){
            return true;
        }
        return false;
    }

    /**
     * Checks if a point falls within a prism or not
     * @param point The point
     * @param prismPoints The points defining the base of the prism
     * @return true if the point is within the prism, false otherwise
     */
    public static boolean pointIntersectsConvexPrism(Vector3d point, Vector3d[] prismPoints, double height){
        //error check quad definition
        if(height <= 0){
            throw new Error("Quad has invalid height! " + height);
        }
        if(prismPoints.length < 3){
            throw new Error("Too few points! " + prismPoints.length);
        }
        //make sure the base is on the same y level
        for(int i = 1; i < prismPoints.length; i++){
            if(prismPoints[i].y != prismPoints[0].y){
                throw new Error("Quadrilateral isn't aligned along y axis " + prismPoints[0].y + " " + prismPoints[i].y + " " + i);
            }
        }
        //outside of y-bounds
        if(point.y < prismPoints[0].y || point.y > prismPoints[0].y + height){
            return false;
        }
        //check if the point is within the x-z quadrilaterial
        int crossings = 0;
        for(int i = 0; i < prismPoints.length; i++){
            Vector3d point1 = prismPoints[i];
            Vector3d point2 = prismPoints[(i + 1) % prismPoints.length];
            if (((point1.z > point.z) != (point2.z > point.z)) && (point.x < (point2.x - point1.x) * (point.z - point1.z) / (point2.z - point1.z) + point1.x)) {
                crossings++;
            }
        }
        return (crossings % 2 == 1);
    }
    
}
