#ifndef RANDUTILS_H
#define RANDUTILS_H

#include "public.h"


/**
 * Generates a random number given a seed value
 */
LIBRARY_API float randutils_rand1(float x);

/**
 * Generates a random number given two seed values
 */
LIBRARY_API float randutils_rand2(float x, float y);

/**
 * Generates a random number given three seed values
 */
LIBRARY_API float randutils_rand3(float x, float y, float z);

/**
 * Maps a float of range [0,1] to an integer range
 */
LIBRARY_API float randutils_map(float x, int min, int max);


#endif