#include <math.h>

#include "math/mathutils.h"
#include "math/randutils.h"

/**
 * The magnitude of the random oscillator
 */
#define MATHUTILS_RAND_MAG 100000.0f

/**
 * Vectors used for prng generation
 */
#define MATHUTILS_RAND_VEC_X 111.154315f
#define MATHUTILS_RAND_VEC_Y 123.631631f
#define MATHUTILS_RAND_VEC_Z 117.724545f

/**
 * Generates a random number given a seed value
 */
LIBRARY_API float randutils_rand1(float x){
    return fract(sin(x) * MATHUTILS_RAND_MAG);
}

/**
 * Generates a random number given two seed values
 */
LIBRARY_API float randutils_rand2(float x, float y){
    return fract(sin(dot2(x,y,MATHUTILS_RAND_VEC_X,MATHUTILS_RAND_VEC_Y)) * MATHUTILS_RAND_MAG);
}

/**
 * Generates a random number given three seed values
 */
LIBRARY_API float randutils_rand3(float x, float y, float z){
    return fract(sin(dot3(x,y,z,MATHUTILS_RAND_VEC_X,MATHUTILS_RAND_VEC_Y,MATHUTILS_RAND_VEC_Z)) * MATHUTILS_RAND_MAG);
}

/**
 * Maps a float of range [0,1] to an integer range
 */
LIBRARY_API float randutils_map(float x, int min, int max){
    return (int)(x * (max - min) + min);
}


