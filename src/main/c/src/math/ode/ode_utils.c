#include "immintrin.h"

#include "fluid/queue/chunk.h"
#include "math/ode/ode_utils.h"

// /**
//  * Computes the stencil of a given source array at a given position
//  */
// __m256 ode_poisson_stencil_parallel(float * source, float * sourcePrev, int x, int y, int z){
//     __m256 POISSON_STENCIL_C_SCALAR = _mm256_set1_ps(6);
//     //get values from neighbors
//     __m256 stencil =                _mm256_loadu_ps(&source[ode_index( x-1, y,   z,   DIM )]);
//     stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&source[ode_index( x+1, y,   z,   DIM )]));
//     stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&source[ode_index( x,   y-1, z,   DIM )]));
//     stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&source[ode_index( x,   y+1, z,   DIM )]));
//     stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&source[ode_index( x,   y,   z-1, DIM )]));
//     stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&source[ode_index( x,   y,   z+1, DIM )]));
//     //add previous value
//     stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&sourcePrev[ode_index(x,y,z,DIM)]));
//     //divide by 6
//     stencil = _mm256_div_ps(stencil,POISSON_STENCIL_C_SCALAR);
//     _mm256_storeu_ps(&source[ode_index(x,y,z,DIM)],stencil);
// }