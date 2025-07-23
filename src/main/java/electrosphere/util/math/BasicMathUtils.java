package electrosphere.util.math;

/**
 * Basic math functions
 */
public class BasicMathUtils {
    
    /**
     * Linearly interpolates between two doubles
     * @param a The first double
     * @param b The second double
     * @param percent The percentage to interpolate between them
     * @return The interpolated value
     */
    public static double lerp(double a, double b, double percent){
        return a * (1.0 - percent) + (b * percent);
    }

}
