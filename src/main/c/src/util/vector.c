#include "util/vector.h"


/**
 * Used for summing vecs
 */
static float vec_sum_storage[8];

/**
 * Sums a float vector's elements
 */
LIBRARY_API float vec_sum(__m256 x) {
    _mm256_storeu_ps(vec_sum_storage,x);
    return
        vec_sum_storage[0] + vec_sum_storage[1] + 
        vec_sum_storage[2] + vec_sum_storage[3] + 
        vec_sum_storage[4] + vec_sum_storage[5] + 
        vec_sum_storage[6] + vec_sum_storage[7]
    ;
}