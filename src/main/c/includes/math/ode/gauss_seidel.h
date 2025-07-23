
#ifndef MATH_GAUSS_SEIDEL_H
#define MATH_GAUSS_SEIDEL_H

#include "immintrin.h"

#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"

/**
 * Gets the index into the array
 */
static inline int solver_gauss_seidel_get_index(int x, int y, int z, int N){
    return (x + (N * y) + (N * N * z));
}


/**
 * Relaxes an ODE matrix by 1 iteration of gauss seidel serially
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @param gridDim The dimensions of the grid
 */
static inline void solver_gauss_seidel_iterate_serial(float * phi, float * phi0, float a, float c, int gridDim){
    int i, j, k, l, m;
    
    //transform u direction
    for(k=1; k<gridDim-1; k++){
        for(j=1; j<gridDim-1; j++){
            int n = 0;
            //If there is any leftover, perform manual solving
            for(i=1; i < gridDim-1; i++){
                phi[solver_gauss_seidel_get_index(i,j,k,gridDim)] =
                (
                    phi0[solver_gauss_seidel_get_index(i,j,k,gridDim)] + 
                    a * (
                        phi[solver_gauss_seidel_get_index(i-1,j,k,gridDim)] +
                        phi[solver_gauss_seidel_get_index(i+1,j,k,gridDim)] +
                        phi[solver_gauss_seidel_get_index(i,j-1,k,gridDim)] +
                        phi[solver_gauss_seidel_get_index(i,j+1,k,gridDim)] +
                        phi[solver_gauss_seidel_get_index(i,j,k-1,gridDim)] +
                        phi[solver_gauss_seidel_get_index(i,j,k+1,gridDim)]
                    )
                ) / c;
            }
        }
    }
}

/**
 * Relaxes an ODE matrix by 1 iteration of gauss seidel parallelized
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @param gridDim The dimensions of the grid
 */
static inline void solver_gauss_seidel_iterate_parallel(float * phi, float * phi0, float a, float c, int gridDim){
    int i, j, k, l, m;
    if(gridDim >= 10){
        __m256 aScalar = _mm256_set1_ps(a);
        __m256 cScalar = _mm256_set1_ps(c);
        //transform u direction
        for(k=1; k<gridDim-1; k++){
            for(j=1; j<gridDim-1; j++){
                int n = 0;
                //solve as much as possible vectorized
                for(i = 1; i < gridDim-1; i=i+8){
                    __m256 vector = _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i-1,j,k,gridDim)]);
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i+1,j,k,gridDim)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j-1,k,gridDim)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j+1,k,gridDim)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k-1,gridDim)]));
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k+1,gridDim)]));
                    vector = _mm256_mul_ps(vector,aScalar);
                    vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi0[solver_gauss_seidel_get_index(i,j,k,gridDim)]));
                    vector = _mm256_div_ps(vector,cScalar);
                    _mm256_storeu_ps(&phi[solver_gauss_seidel_get_index(i,j,k,gridDim)],vector);
                }
                //If there is any leftover, perform manual solving
                if(i>gridDim-1){
                    for(i=i-8; i < gridDim-1; i++){
                        phi[solver_gauss_seidel_get_index(i,j,k,gridDim)] =
                        (
                            phi0[solver_gauss_seidel_get_index(i,j,k,gridDim)] + 
                            a * (
                                phi[solver_gauss_seidel_get_index(i-1,j,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i+1,j,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j-1,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j+1,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j,k-1,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j,k+1,gridDim)]
                            )
                        ) / c;
                    }
                }
            }
        }
    } else if(gridDim >= 6){
        __m128 aScalar = _mm_set1_ps(a);
        __m128 cScalar = _mm_set1_ps(c);
        //transform u direction
        for(k=1; k<gridDim-1; k++){
            for(j=1; j<gridDim-1; j++){
                int n = 0;
                //solve as much as possible vectorized
                for(i = 1; i < gridDim-1; i=i+8){
                    __m128 vector = _mm_loadu_ps(&phi[solver_gauss_seidel_get_index(i-1,j,k,gridDim)]);
                    vector = _mm_add_ps(vector,_mm_loadu_ps(&phi[solver_gauss_seidel_get_index(i+1,j,k,gridDim)]));
                    vector = _mm_add_ps(vector,_mm_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j-1,k,gridDim)]));
                    vector = _mm_add_ps(vector,_mm_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j+1,k,gridDim)]));
                    vector = _mm_add_ps(vector,_mm_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k-1,gridDim)]));
                    vector = _mm_add_ps(vector,_mm_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k+1,gridDim)]));
                    vector = _mm_mul_ps(vector,aScalar);
                    vector = _mm_add_ps(vector,_mm_loadu_ps(&phi0[solver_gauss_seidel_get_index(i,j,k,gridDim)]));
                    vector = _mm_div_ps(vector,cScalar);
                    _mm_storeu_ps(&phi[solver_gauss_seidel_get_index(i,j,k,gridDim)],vector);
                }
                //If there is any leftover, perform manual solving
                if(i>gridDim-1){
                    for(i=i-8; i < gridDim-1; i++){
                        phi[solver_gauss_seidel_get_index(i,j,k,gridDim)] =
                        (
                            phi0[solver_gauss_seidel_get_index(i,j,k,gridDim)] + 
                            a * (
                                phi[solver_gauss_seidel_get_index(i-1,j,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i+1,j,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j-1,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j+1,k,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j,k-1,gridDim)]+
                                phi[solver_gauss_seidel_get_index(i,j,k+1,gridDim)]
                            )
                        ) / c;
                    }
                }
            }
        }
    } else {
        solver_gauss_seidel_iterate_serial(phi,phi0,a,c,gridDim);
    }
}



#endif