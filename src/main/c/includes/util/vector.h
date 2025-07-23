#ifndef UTIL_VECTOR_H
#define UTIL_VECTOR_H

#include <immintrin.h>

#include "public.h"


/**
 * Sums a float vector's elements
 */
LIBRARY_API float vec_sum(__m256 x);

#endif