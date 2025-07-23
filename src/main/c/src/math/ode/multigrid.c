#include <stdlib.h>
#include <stdio.h>
#include <math.h>

#include "fluid/queue/chunk.h"
#include "fluid/sim/grid2/utilities.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "math/ode/gauss_seidel.h"
#include "math/ode/multigrid.h"
#include "util/vector.h"

/**
 * Dimension of the half resolution grid
 */
static int halfDim = ((DIM - 2) / 2) + 2;

/**
 * Dimension of the quarter resolution grid
 */
static int quarterDim = ((DIM - 2) / 4) + 2;
static int LOWEST_DIM = ((DIM - 2) / 4) + 2;
static int LOWEST_PARALLEL_DIM = ((DIM - 2) / 1) + 2;

/**
 * The full resolution grids
 */
static float * fullGridResidual = NULL;

/**
 * The half resolution grids
 */
static float * halfGridPhi = NULL;
static float * halfGridPhi0 = NULL;
static float * halfGridResidual = NULL;

/**
 * The quarter resolution grids
 */
static float * quarterGridPhi = NULL;
static float * quarterGridPhi0 = NULL;
static float * quarterGridResidual = NULL;

/**
 * The eighth resolution grids
 */
static float * eighthGridPhi = NULL;
static float * eighthGridPhi0 = NULL;
static float * eighthGridResidual = NULL;


float solver_multigrid_calculate_residual_norm_serial(float * phi, float * phi0, float a, float c, int GRIDDIM);
float solver_multigrid_calculate_residual_parallel(float * phi, float * phi0, float a, float c, int GRIDDIM);
void solver_multigrid_store_residual_serial(float * phi, float * phi0, float * residualGrid, float a, float c, int GRIDDIM);

//parallelized operations
void restrict_parallel(float * currResidual, int GRIDDIM, float * lowerPhi, float * lowerPhi0, int LOWERDIM);
void prolongate_parallel(float * phi, int GRIDDIM, float * lowerPhi, int LOWERDIM);
void solver_multigrid_store_residual_parallel(float * phi, float * phi0, float * residualGrid, float a, float c, int GRIDDIM);

/**
 * Relaxes an ODE matrix by 1 iteration of multigrid method
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @param GRIDDIM The dimension of the phi grid
 * @return The residual
 */
void solver_multigrid_iterate_serial_recursive(float * phi, float * phi0, float a, float c, int GRIDDIM){
    int LOWERDIM = ((GRIDDIM - 2) / 2) + 2;
    float * currResidual = solver_multigrid_get_current_residual(GRIDDIM);
    float * lowerPhi = solver_multigrid_get_current_phi(LOWERDIM);
    float * lowerPhi0 = solver_multigrid_get_current_phi0(LOWERDIM);
    float * lowerResidual = solver_multigrid_get_current_residual(LOWERDIM);

    //smooth
    solver_gauss_seidel_iterate_parallel(phi,phi0,a,c,GRIDDIM);

    //compute residuals
    solver_multigrid_store_residual_parallel(phi,phi0,currResidual,a,c,GRIDDIM);

    //restrict
    restrict_parallel(currResidual,GRIDDIM,lowerPhi,lowerPhi0,LOWERDIM);

    //solve next-coarsest grid
    if(GRIDDIM <= LOWEST_DIM){
        float solution = 
            (
                phi0[solver_gauss_seidel_get_index(1,1,1,GRIDDIM)] + 
                phi0[solver_gauss_seidel_get_index(2,1,1,GRIDDIM)] + 
                phi0[solver_gauss_seidel_get_index(1,2,1,GRIDDIM)] + 
                phi0[solver_gauss_seidel_get_index(1,1,2,GRIDDIM)] +
                phi0[solver_gauss_seidel_get_index(1,2,2,GRIDDIM)] + 
                phi0[solver_gauss_seidel_get_index(2,1,2,GRIDDIM)] + 
                phi0[solver_gauss_seidel_get_index(2,2,1,GRIDDIM)] + 
                phi0[solver_gauss_seidel_get_index(2,2,2,GRIDDIM)]
            ) / 8.0f
        ;
        
        //interpolate solution to this grid
        for(int x = 1; x < GRIDDIM - 1; x++){
            for(int y = 1; y < GRIDDIM - 1; y++){
                for(int z = 1; z < GRIDDIM - 1; z++){
                    //direct transfer operator (faster, lower accuracy)
                    phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] = phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] + solution;
                }
            }
        }
    } else {
        solver_multigrid_iterate_serial_recursive(lowerPhi,lowerPhi0,a,c,LOWERDIM);
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
float solver_multigrid_iterate_serial(float * phi, float * phi0, float a, float c){
    solver_multigrid_initialization_check();
    solver_multigrid_iterate_serial_recursive(phi,phi0,a,c,DIM);
    return solver_multigrid_calculate_residual_norm_serial(phi,phi0,a,c,DIM);
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
void solver_multigrid_iterate_parallel_recursive(float * phi, float * phi0, float a, float c, int GRIDDIM){
    int LOWERDIM = ((GRIDDIM - 2) / 2) + 2;
    float * currResidual = solver_multigrid_get_current_residual(GRIDDIM);
    float * lowerPhi = solver_multigrid_get_current_phi(LOWERDIM);
    float * lowerPhi0 = solver_multigrid_get_current_phi0(LOWERDIM);
    float * lowerResidual = solver_multigrid_get_current_residual(LOWERDIM);

    //smooth
    solver_gauss_seidel_iterate_parallel(phi,phi0,a,c,GRIDDIM);

    //compute residuals
    solver_multigrid_store_residual_parallel(phi,phi0,currResidual,a,c,GRIDDIM);

    //restrict
    restrict_parallel(currResidual,GRIDDIM,lowerPhi,lowerPhi0,LOWERDIM);

    //solve next-coarsest grid
    if(GRIDDIM <= LOWEST_PARALLEL_DIM){
        solver_multigrid_iterate_serial_recursive(lowerPhi,lowerPhi0,a,c,LOWERDIM);
    } else {
        solver_multigrid_iterate_parallel_recursive(lowerPhi,lowerPhi0,a,c,LOWERDIM);
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
float solver_multigrid_iterate_parallel(float * phi, float * phi0, float a, float c){
    solver_multigrid_initialization_check();
    solver_multigrid_iterate_parallel_recursive(phi,phi0,a,c,DIM);
    return solver_multigrid_calculate_residual_norm_serial(phi,phi0,a,c,DIM);
}



/**
 * Gets the phi to use for the current level of the multigrid solver
 */
float * solver_multigrid_get_current_phi(int dim){
    if(dim == ((DIM-2)/2) + 2){
        return halfGridPhi;
    } else if(dim == ((DIM-2)/4) + 2){
        return quarterGridPhi;
    } else if(dim == ((DIM-2)/8) + 2){
        return eighthGridPhi;
    } else {
        printf("[get_current_phi] Invalid dim detected! %d\n",dim);
        fflush(stdout);
        return NULL;
    }
}

/**
 * Gets the phi0 to use for the current level of the multigrid solver
 */
float * solver_multigrid_get_current_phi0(int dim){
    if(dim == ((DIM-2)/2) + 2){
        return halfGridPhi0;
    } else if(dim == ((DIM-2)/4) + 2){
        return quarterGridPhi0;
    } else if(dim == ((DIM-2)/8) + 2){
        return eighthGridPhi0;
    } else {
        printf("[get_current_phi0] Invalid dim detected! %d\n",dim);
        fflush(stdout);
        return NULL;
    }
}

/**
 * Gets the residual to use for the current level of the multigrid solver
 */
float * solver_multigrid_get_current_residual(int dim){
    if(dim == 18){
        return fullGridResidual;
    } else if(dim == ((DIM-2)/2) + 2){
        return halfGridResidual;
    } else if(dim == ((DIM-2)/4) + 2){
        return quarterGridResidual;
    } else if(dim == ((DIM-2)/8) + 2){
        return eighthGridResidual;
    } else {
        printf("[get_current_residual] Invalid dim detected! %d\n",dim);
        fflush(stdout);
        return NULL;
    }
}

/**
 * Serially restricts the current residual into the lower phi grid
 */
void solver_multigrid_restrict_serial(float * currResidual, int GRIDDIM, float * lowerPhi, float * lowerPhi0, int LOWERDIM){
    int x, y, z;
    //restrict
    //(current operator is injection -- inject r^2 from this grid at phi0 of the smaller grid)
    for(int x = 0; x < LOWERDIM; x++){
        for(int y = 0; y < LOWERDIM; y++){
            for(int z = 0; z < LOWERDIM; z++){
                lowerPhi[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)] = 0;
                lowerPhi0[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)] = 0;
            }
        }
    }
    //populate grid
    for(int x = 1; x < LOWERDIM - 1; x++){
        for(int y = 1; y < LOWERDIM - 1; y++){
            for(int z = 1; z < LOWERDIM - 1; z++){
                //direct transfer operator (faster, lower accuracy)
                lowerPhi0[solver_gauss_seidel_get_index(x,y,z,LOWERDIM)] = currResidual[solver_gauss_seidel_get_index(x*2,y*2,z*2,GRIDDIM)];
            }
        }
    }
}

/**
 * Serially restricts the current residual into the lower phi grid
 */
void restrict_parallel(float * currResidual, int GRIDDIM, float * lowerPhi, float * lowerPhi0, int LOWERDIM){
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
 * Prolongates a lower grid into a higher grid
 */
void solver_multigrid_prolongate_serial(float * phi, int GRIDDIM, float * lowerPhi, int LOWERDIM){
    int x, y, z;
    for(int x = 1; x < GRIDDIM - 1; x++){
        for(int y = 1; y < GRIDDIM - 1; y++){
            for(int z = 1; z < GRIDDIM - 1; z++){
                //direct transfer operator (faster, lower accuracy)
                phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] = 
                phi[solver_gauss_seidel_get_index(x,y,z,GRIDDIM)] + 
                lowerPhi[solver_gauss_seidel_get_index(  x/2+0,  y/2+0,  z/2+0  ,LOWERDIM)]
                ;
            }
        }
    }
}

/**
 * Prolongates a lower grid into a higher grid
 */
void prolongate_parallel(float * phi, int GRIDDIM, float * lowerPhi, int LOWERDIM){
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
 * Verifies that all grids are allocated
 */
void solver_multigrid_initialization_check(){
    // full res
    if(fullGridResidual == NULL){
        fullGridResidual = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    }

    // half res
    if(halfGridPhi == NULL){
        halfGridPhi = (float *)calloc(1,halfDim * halfDim * halfDim * sizeof(float));
    }
    if(halfGridPhi0 == NULL){
        halfGridPhi0 = (float *)calloc(1,halfDim * halfDim * halfDim * sizeof(float));
    }
    if(halfGridResidual == NULL){
        halfGridResidual = (float *)calloc(1,halfDim * halfDim * halfDim * sizeof(float));
    }

    // quarter res
    if(quarterGridPhi == NULL){
        quarterGridPhi = (float *)calloc(1,quarterDim * quarterDim * quarterDim * sizeof(float));
    }
    if(quarterGridPhi0 == NULL){
        quarterGridPhi0 = (float *)calloc(1,quarterDim * quarterDim * quarterDim * sizeof(float));
    }
    if(quarterGridResidual == NULL){
        quarterGridResidual = (float *)calloc(1,quarterDim * quarterDim * quarterDim * sizeof(float));
    }

    // quarter res
    int eighthResDim = ((DIM-2)/8) + 2;
    if(eighthGridPhi == NULL){
        eighthGridPhi = (float *)calloc(1,eighthResDim * eighthResDim * eighthResDim * sizeof(float));
    }
    if(eighthGridPhi0 == NULL){
        eighthGridPhi0 = (float *)calloc(1,eighthResDim * eighthResDim * eighthResDim * sizeof(float));
    }
    if(eighthGridResidual == NULL){
        eighthGridResidual = (float *)calloc(1,eighthResDim * eighthResDim * eighthResDim * sizeof(float));
    }
}


/**
 * Calculates the residual of the grid
 * @return Returns the residual norm of the grid
 */
float solver_multigrid_calculate_residual_norm_serial(float * phi, float * phi0, float a, float c, int GRIDDIM){
    //calculate residual
    float residual_norm = 0;
    int i, j, k;
    int increment = 0;
    float h = FLUID_GRID2_H;
    float residual;
    for(k=1; k<GRIDDIM-1; k++){
        for(j=1; j<GRIDDIM-1; j++){
            for(i=1; i<GRIDDIM-1; i++){
                float laplacian = 
                (
                    6 * phi[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)] + 
                    - (
                        phi[solver_gauss_seidel_get_index(i-1,j,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i+1,j,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j-1,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j+1,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j,k-1,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j,k+1,GRIDDIM)]
                    )
                );
                residual = phi0[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)] - laplacian;
                residual_norm = residual_norm + (residual * residual);
                increment++;
                if(increment > GRIDDIM*GRIDDIM*GRIDDIM){
                    printf("INCREMENT FAILURE   %d %d %d %d \n",i,j,k,increment);
                    return -1;
                }
            }
        }
    }
    residual_norm = (float)sqrt(residual_norm);
    // printf("residual_norm: %lf\n",residual_norm);
    return residual_norm;
}


/**
 * Calculates the residual of the grid
 */
float solver_multigrid_calculate_residual_parallel(float * phi, float * phi0, float a, float c, int GRIDDIM){
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
 * Calculates the residual of the grid
 */
void solver_multigrid_store_residual_serial(float * phi, float * phi0, float * residualGrid, float a, float c, int GRIDDIM){
    //calculate residual
    int i, j, k;
    for(k=1; k<GRIDDIM-1; k++){
        for(j=1; j<GRIDDIM-1; j++){
            for(i=1; i<GRIDDIM-1; i++){
                float laplacian = 
                (
                    6 * phi[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)] + 
                    - (
                        phi[solver_gauss_seidel_get_index(i-1,j,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i+1,j,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j-1,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j+1,k,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j,k-1,GRIDDIM)]+
                        phi[solver_gauss_seidel_get_index(i,j,k+1,GRIDDIM)]
                    )
                );
                residualGrid[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)] = phi0[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)] - laplacian;
            }
        }
    }
}

/**
 * Calculates the residual of the grid
 */
void solver_multigrid_store_residual_parallel(float * phi, float * phi0, float * residualGrid, float a, float c, int GRIDDIM){
    if(GRIDDIM < 10){
        solver_multigrid_store_residual_serial(phi,phi0,residualGrid,a,c,GRIDDIM);
        return;
    }
    __m256 laplacian;
    __m256 constVec = _mm256_set1_ps(6);
    //calculate residual
    int i, j, k;
    for(k=1; k<GRIDDIM-1; k++){
        for(j=1; j<GRIDDIM-1; j++){
            for(i=1; i<GRIDDIM-1; i=i+8){
                laplacian = 
                _mm256_sub_ps(
                    _mm256_mul_ps(
                        _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k,GRIDDIM)]),
                        constVec
                    ),
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