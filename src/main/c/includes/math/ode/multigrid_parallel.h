#ifndef MATH_ODE_MULTIGRID_PARALLEL_H
#define MATH_ODE_MULTIGRID_PARALLEL_H

#include "math/ode/gauss_seidel.h"
#include "math/ode/multigrid.h"

#define MULTIGRID_PARALLEL_LOWEST_PARALLEL_DIM ((DIM - 2) / 2) + 2







/**
 * Serially restricts the current residual into the lower phi grid
 */
static inline void restrict_parallel(float * currResidual, int GRIDDIM, float * lowerPhi, float * lowerPhi0, int LOWERDIM){
    if(LOWERDIM < 10){
        solver_multigrid_restrict_serial(currResidual,GRIDDIM,lowerPhi,lowerPhi0,LOWERDIM);
        return;
    }
    int x, y, z;
    __m256 zeroVec = _mm256_setzero_ps();
    __m256 residuals;
    __m256i offsets = _mm256_set_epi32(0, 1, 2, 3, 4, 5, 6, 7);

    //
    //set first plane
    //
    x = 0;
    for(y = 0; y < LOWERDIM; y++){
        for(z = 0; z < LOWERDIM-7; z=z+8){
            _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
        }
        z = LOWERDIM - 8;
        _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
    }

    //
    //set main volume
    //
    for(x = 1; x < LOWERDIM - 1; x++){

        //
        //set the first edge
        //
        y = 0;
        for(z = 0; z < LOWERDIM-7; z=z+8){
            _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
        }
        z = LOWERDIM - 8;
        _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);


        //
        //copy the main contents
        //
        for(y = 1; y < LOWERDIM - 1; y++){
            lowerPhi[solver_gauss_seidel_get_index(x,y,0,LOWERDIM)] = 0;
            for(z = 1; z < LOWERDIM-7; z=z+8){
                _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
                residuals = _mm256_i32gather_ps(&currResidual[solver_gauss_seidel_get_index(x*2,y*2,z*2,GRIDDIM)],offsets,sizeof(float));
                _mm256_storeu_ps(&lowerPhi0[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],residuals);
            }
            lowerPhi[solver_gauss_seidel_get_index(x,y,LOWERDIM - 1,LOWERDIM)] = 0;
        }

        //
        //set the last edge
        //
        for(z = 0; z < LOWERDIM-7; z=z+8){
            _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
        }
        z = LOWERDIM - 8;
        _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
    }

    //
    //set last plane
    //  
    x = LOWERDIM - 1;
    for(y = 0; y < LOWERDIM; y++){
        for(z = 0; z < LOWERDIM-7; z=z+8){
            _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
        }
        //zero out the end
        z = LOWERDIM - 8;
        _mm256_storeu_ps(&lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)],zeroVec);
    }

    //populate grid
    for(x = 1; x < LOWERDIM - 1; x++){
        for(y = 1; y < LOWERDIM - 1; y++){
            for(z = 1; z < LOWERDIM - 1; z++){
                //direct transfer operator (faster, lower accuracy)
                lowerPhi0[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)] = currResidual[solver_gauss_seidel_get_index(x*2,y*2,z*2,GRIDDIM)];
            }
        }
    }
}





/**
 * Calculates the residual of the grid
 */
static inline float solver_multigrid_calculate_residual_parallel(float * phi, float * phi0, float a, float c, int GRIDDIM){
    //calculate residual
    __m256 aScalar = _mm256_set1_ps(a);
    __m256 cScalar = _mm256_set1_ps(c);
    __m256 collector = _mm256_setzero_ps();
    __m256 vector;
    float vec_sum_storage[8];
    float residual = 0;
    //transform u direction
    int i, j, k;
    int increment = 0;
    for(k=1; k<GRIDDIM-1; k++){
        for(j=1; j<GRIDDIM-1; j++){
            for(i=1; i<GRIDDIM-1; i=i+8){
                vector = _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i-1,j,k,GRIDDIM)]);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i+1,j,k,GRIDDIM)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j-1,k,GRIDDIM)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j+1,k,GRIDDIM)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k-1,GRIDDIM)]));
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k+1,GRIDDIM)]));
                vector = _mm256_mul_ps(vector,aScalar);
                vector = _mm256_add_ps(vector,_mm256_loadu_ps(&phi0[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)]));
                vector = _mm256_div_ps(vector,cScalar);
                collector = _mm256_add_ps(collector,vector);
                increment++;
                if(increment > GRIDDIM*GRIDDIM*GRIDDIM){
                    printf("INCREMENT FAILURE   %d %d %d %d \n",i,j,k,increment);
                    return -1;
                }
            }
        }
    }
    _mm256_storeu_ps(vec_sum_storage,collector);
    residual =
        vec_sum_storage[0] + vec_sum_storage[1] + 
        vec_sum_storage[2] + vec_sum_storage[3] + 
        vec_sum_storage[4] + vec_sum_storage[5] + 
        vec_sum_storage[6] + vec_sum_storage[7]
    ;
    return residual;
}

/**
 * Prolongates a lower grid into a higher grid
 */
static inline void prolongate_parallel(float * phi, int GRIDDIM, float * lowerPhi, int LOWERDIM){
    if(LOWERDIM < 10){
        solver_multigrid_prolongate_serial(phi,GRIDDIM,lowerPhi,LOWERDIM);
        return;
    }
    __m256i offsets = _mm256_set_epi32(0, 0, 1, 1, 2, 2, 3, 3);
    __m256 lowerPhiVec;
    __m256 phiVec;
    int x, y, z;
    for(int x = 1; x < GRIDDIM - 1; x++){
        for(int y = 1; y < GRIDDIM - 1; y++){
            for(int z = 1; z < GRIDDIM - 1; z=z+8){
                phiVec = _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)]);
                lowerPhiVec = _mm256_i32gather_ps(&lowerPhi[solver_gauss_seidel_get_index(x/2,y/2,z/2,LOWERDIM)],offsets,sizeof(float));
                _mm256_storeu_ps(
                    &phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)],
                    _mm256_add_ps(
                        phiVec,
                        lowerPhiVec
                    )
                );
            }
        }
    }
}





/**
 * Calculates the residual of the grid
 */
static inline void solver_multigrid_parallel_store_residual(float * phi, float * phi0, float * residualGrid, float a, float c, int GRIDDIM){
    if(GRIDDIM < 10){
        solver_multigrid_store_residual_serial(phi,phi0,residualGrid,a,c,GRIDDIM);
        return;
    }
    __m256 laplacian;
    __m256 constVecA = _mm256_set1_ps(a);
    __m256 constVecC = _mm256_set1_ps(c);
    //calculate residual
    int i, j, k;
    for(k=1; k<GRIDDIM-1; k++){
        for(j=1; j<GRIDDIM-1; j++){
            for(i=1; i<GRIDDIM-1; i=i+8){
                laplacian = 
                _mm256_sub_ps(
                    _mm256_mul_ps(
                        _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)]),
                        constVecC
                    ),
                    _mm256_mul_ps(
                        _mm256_add_ps(
                            _mm256_add_ps(
                                _mm256_add_ps(
                                    _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i-1,j,k,GRIDDIM)]),
                                    _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i+1,j,k,GRIDDIM)])
                                ),
                                _mm256_add_ps(
                                    _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j-1,k,GRIDDIM)]),
                                    _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j+1,k,GRIDDIM)])
                                )
                            ),
                            _mm256_add_ps(
                                _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k-1,GRIDDIM)]),
                                _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k+1,GRIDDIM)])
                            )
                        ),
                        constVecA
                    )
                );
                _mm256_storeu_ps(
                    &residualGrid[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)],
                    _mm256_sub_ps(
                        _mm256_loadu_ps(&phi0[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)]),
                        laplacian
                    )
                );
            }
        }
    }
}






/**
 * Relaxes an ODE matrix by 1 iteration of multigrid method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @param GRIDDIM The dimension of the phi grid
 * @return The residual
 */
static inline void solver_multigrid_parallel_iterate_recurse_v_cycle(float * phi, float * phi0, float a, float c, int GRIDDIM){
    int LOWERDIM = ((GRIDDIM - 2) / 2) + 2;
    float * currResidual = solver_multigrid_get_current_residual(GRIDDIM);
    float * lowerPhi = solver_multigrid_get_current_phi(LOWERDIM);
    float * lowerPhi0 = solver_multigrid_get_current_phi0(LOWERDIM);
    float * lowerResidual = solver_multigrid_get_current_residual(LOWERDIM);

    //smooth
    solver_gauss_seidel_iterate_parallel(phi,phi0,a,c,GRIDDIM);

    //compute residuals
    solver_multigrid_parallel_store_residual(phi,phi0,currResidual,a,c,GRIDDIM);

    //restrict
    restrict_parallel(currResidual,GRIDDIM,lowerPhi,lowerPhi0,LOWERDIM);

    //solve next-coarsest grid
    if(GRIDDIM <= MULTIGRID_PARALLEL_LOWEST_PARALLEL_DIM){
        solver_multigrid_iterate_serial_recursive(lowerPhi,lowerPhi0,a,c,LOWERDIM);
    } else {
        solver_multigrid_parallel_iterate_recurse_v_cycle(lowerPhi,lowerPhi0,a,c,LOWERDIM);
    }

    //interpolate from the lower grid
    prolongate_parallel(phi,GRIDDIM,lowerPhi,LOWERDIM);

    //smooth
    solver_gauss_seidel_iterate_parallel(phi,phi0,a,c,GRIDDIM);
}


/**
 * Relaxes an ODE matrix by 1 iteration of multigrid method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @param GRIDDIM The dimension of the phi grid
 * @return The residual
 */
static inline void solver_multigrid_parallel_iterate_recurse_f_cycle(float * phi, float * phi0, float a, float c, int GRIDDIM){
    int LOWERDIM = ((GRIDDIM - 2) / 2) + 2;
    float * currResidual = solver_multigrid_get_current_residual(GRIDDIM);
    float * lowerPhi = solver_multigrid_get_current_phi(LOWERDIM);
    float * lowerPhi0 = solver_multigrid_get_current_phi0(LOWERDIM);
    float * lowerResidual = solver_multigrid_get_current_residual(LOWERDIM);

    //smooth
    solver_gauss_seidel_iterate_parallel(phi,phi0,a,c,GRIDDIM);

    //compute residuals
    solver_multigrid_parallel_store_residual(phi,phi0,currResidual,a,c,GRIDDIM);

    //restrict
    restrict_parallel(currResidual,GRIDDIM,lowerPhi,lowerPhi0,LOWERDIM);

    //solve next-coarsest grid
    if(GRIDDIM <= MULTIGRID_PARALLEL_LOWEST_PARALLEL_DIM){
        solver_multigrid_iterate_serial_recursive(lowerPhi,lowerPhi0,a,c,LOWERDIM);
    } else {
        solver_multigrid_parallel_iterate_recurse_v_cycle(lowerPhi,lowerPhi0,a,c,LOWERDIM);
    }

    //interpolate from the lower grid
    prolongate_parallel(phi,GRIDDIM,lowerPhi,LOWERDIM);

    //smooth
    solver_gauss_seidel_iterate_parallel(phi,phi0,a,c,GRIDDIM);
}

/**
 * Relaxes an ODE matrix by 1 iteration of multigrid method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return The residual
 */
static inline float solver_multigrid_parallel_iterate(float * phi, float * phi0, float a, float c){
    solver_multigrid_initialization_check();
    solver_multigrid_parallel_iterate_recurse_v_cycle(phi,phi0,a,c,DIM);
    return solver_multigrid_calculate_residual_norm_serial(phi,phi0,a,c,DIM);
}


#endif