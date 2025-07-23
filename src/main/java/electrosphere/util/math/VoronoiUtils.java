package electrosphere.util.math;

import org.joml.Vector3d;
import org.joml.Vector3i;

import io.github.studiorailgun.RandUtils;

/**
 * Utilities for dealing with voronoi noise
 */
public class VoronoiUtils {

    /**
     * x offsets for a 3x3x3 kernel
     */
    static final int[] KERNEL_3_3_3_X = new int[]{
        1, 0, -1,
        1, 0, -1,
        1, 0, -1,
        1, 0, -1,
        1, 0, -1,
        1, 0, -1,
        1, 0, -1,
        1, 0, -1,
        1, 0, -1,
    };

    /**
     * y offsets for a 3x3x3 kernel
     */
    static final int[] KERNEL_3_3_3_Y = new int[]{
        1,  1,  1,
        0,  0,  0,
       -1, -1, -1,
        1,  1,  1,
        0,  0,  0,
       -1, -1, -1,
        1,  1,  1,
        0,  0,  0,
       -1, -1, -1,
    };

    /**
     * z offsets for a 3x3x3 kernel
     */
    static final int[] KERNEL_3_3_3_Z = new int[]{
        1,  1,  1,
        1,  1,  1,
        1,  1,  1,
        0,  0,  0,
        0,  0,  0,
        0,  0,  0,
       -1, -1, -1,
       -1, -1, -1,
       -1, -1, -1,
    };

    /**
     * Calculates The voronoi cell that the point falls within
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The voronoi cell
     */
    public static Vector3i solveVoronoiCell(double x, double y, double z){
        //integer of the point coordinates
        int x_i = (int)Math.floor(x);
        int y_i = (int)Math.floor(y);
        int z_i = (int)Math.floor(z);
        return new Vector3i(x_i,y_i,z_i);
    }

    /**
     * Calculates voronoi noise within a cube
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param relaxationFactor The relaxation factor
     * @return The voronoi value
     */
    public static Vector3d solveClosestVoronoiNode(double x, double y, double z, double relaxationFactor){
        //integer of the point coordinates
        double x_i = Math.floor(x);
        double y_i = Math.floor(y);
        double z_i = Math.floor(z);

        //get the point
        double p_x = RandUtils.rand(x_i, y_i, z_i, 0);
        double p_y = RandUtils.rand(x_i, y_i, z_i, 1);
        double p_z = RandUtils.rand(x_i, y_i, z_i, 2);

        //relax the point based on relaxation factor
        double x_relaxed = p_x * (1.0 - relaxationFactor) + (relaxationFactor / 2.0);
        double y_relaxed = p_y * (1.0 - relaxationFactor) + (relaxationFactor / 2.0);
        double z_relaxed = p_z * (1.0 - relaxationFactor) + (relaxationFactor / 2.0);

        return new Vector3d(x_relaxed,y_relaxed,z_relaxed);
    }
    
}
