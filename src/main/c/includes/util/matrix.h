#ifndef UTIL_MATRIX_H
#define UTIL_MATRIX_H

#include <immintrin.h>


/**
 * Copies the contents of one matrix into another
 * @param source The source matrix
 * @param destination The destination matrix
 * @param matDim The dimension of the matrix
 */
void util_matrix_copy(
    float * source,
    float * destination,
    int matDim
){
    int i = 0;
    int size=matDim*matDim*matDim;
    for(i=0; i<size; i=i+8){
        _mm256_storeu_ps(&destination[i],
            _mm256_loadu_ps(&source[i])
        );
    }
    //copy remainder without scalars
    for(i=i; i<size; i++){
        destination[i] = source[i];
    }
}


#endif