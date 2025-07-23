#include <math.h>


#include "math/mathutils.h"


/**
 * Calculates the fractional component of a float
 */
LIBRARY_API float fract(float x){
    return x - floor(x);
}

/**
 * Calculates the dot product of 2D vectors
 */
LIBRARY_API float dot2(float x1, float y1, float x2, float y2){
    return (x1 * x2) + (y1 * y2);
}

/**
 * Calculates the dot product of 3D vectors
 */
LIBRARY_API float dot3(float x1, float y1, float z1, float x2, float y2, float z2){
    return (x1 * x2) + (y1 * y2) + (z1 * z2);
}

