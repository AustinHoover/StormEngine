#include <stdlib.h>
#include <math.h>
#include <immintrin.h>

#include "fluid/queue/chunk.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "math/ode/conjugate_gradient.h"
#include "math/ode/ode_utils.h"
#include "math/ode/gauss_seidel.h"
#include "math/ode/ode.h"


/**
 * Used for preventing divide by 0's
 */
static float CONJUGATE_GRADIENT_EPSILON = 1e-6;

static float CONJUGATE_GRADIENT_CONVERGENCE_THRESHOLD = 0.0001f;

/**
 * The search direction array
 */
static float * p = NULL;

/**
 * Maxmum frames to iterate for
 */
static int max_frames = 100;

/**
 * The residual array
 */
static float * r = NULL;

static float * A = NULL;


/**
 * Iniitalizes the conjugate gradient solver with the phi values
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return 1 if the system has already been solved, 0 otherwise
 */
int solver_conjugate_gradient_init(float * phi, float * phi0, float a, float c){
    if(p == NULL){
        p = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    if(r == NULL){
        r = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    if(A == NULL){
        A = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }

    int i, j, k;
    
    __m256 aScalar = _mm256_set1_ps(a);
    __m256 cScalar = _mm256_set1_ps(c);
    __m256 f, residual, stencil;

    //iniitalize the r (residual) and p (search direction) arrays
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            //set borders
            i = 0;
            _mm256_storeu_ps(&r[ode_index(i,j,k,DIM)],_mm256_setzero_ps());
            _mm256_storeu_ps(&p[ode_index(i,j,k,DIM)],_mm256_setzero_ps());
            i = 8;
            _mm256_storeu_ps(&r[ode_index(i,j,k,DIM)],_mm256_setzero_ps());
            _mm256_storeu_ps(&p[ode_index(i,j,k,DIM)],_mm256_setzero_ps());
            i = DIM-9;
            _mm256_storeu_ps(&r[ode_index(i,j,k,DIM)],_mm256_setzero_ps());
            _mm256_storeu_ps(&p[ode_index(i,j,k,DIM)],_mm256_setzero_ps());

            //solve as much as possible vectorized
            for(i = 1; i < DIM-1; i=i+8){
                //calculate the stencil applied to phi
                //get values from neighbors
                stencil =                       _mm256_loadu_ps(&phi[ode_index( i-1, j,   k,   DIM )]);
                stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&phi[ode_index( i+1, j,   k,   DIM )]));
                stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&phi[ode_index( i,   j-1, k,   DIM )]));
                stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&phi[ode_index( i,   j+1, k,   DIM )]));
                stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&phi[ode_index( i,   j,   k-1, DIM )]));
                stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&phi[ode_index( i,   j,   k+1, DIM )]));
                //multiply by a
                stencil = _mm256_mul_ps(stencil,aScalar);
                //add previous value
                stencil = _mm256_add_ps(stencil,_mm256_loadu_ps(&phi[ode_index(i,j,k,DIM)]));
                //divide by 6
                stencil = _mm256_div_ps(stencil,cScalar);

                //grab the f value (the value of phi0)
                f = _mm256_loadu_ps(&phi0[ode_index(i,j,k,DIM)]);

                //subtract stencil from f
                residual = _mm256_sub_ps(f,stencil);

                //store in residual and searchDir
                _mm256_storeu_ps(&r[ode_index(i,j,k,DIM)],residual);
                _mm256_storeu_ps(&p[ode_index(i,j,k,DIM)],residual);
            }
        }
    }
    float sampleResidual = r[ode_index(3,3,3,DIM)];
    if(sampleResidual <= CONJUGATE_GRADIENT_EPSILON){
        return 1;
    }
    printf("sampleResidual: %f\n",sampleResidual);
    return 0;
}

/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method parallelly
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return The residual
 */
float solver_conjugate_gradient_iterate_parallel(float * phi, float * phi0, float a, float c){
    int i, j, k;

    __m256 constVec = _mm256_set1_ps(6);
    __m256 convergenceVec, denominatorVec;
    __m256 alphaVec;
    __m256 rdotVec;
    __m256 rVec;
    __m256 betaVec;
    float vec_sum_storage[8];
    float convergence, denominator;
    float laplacian, alpha, r_new_dot, beta;
    //solve Ap
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            // for(i = 1; i < DIM-1; i++){
            //     laplacian = 
            //     (
            //         6 * p[ode_index (i   , j   , k   ,DIM)] +
            //         - (
            //             p[ode_index (i+1 , j   , k   ,DIM)] +
            //             p[ode_index (i-1 , j   , k   ,DIM)] +
            //             p[ode_index (i   , j+1 , k   ,DIM)] +
            //             p[ode_index (i   , j-1 , k   ,DIM)] +
            //             p[ode_index (i   , j   , k+1 ,DIM)] +
            //             p[ode_index (i   , j   , k-1 ,DIM)]
            //         )
            //     );
            //     A[ode_index(i,j,k,DIM)] = laplacian;
            // }
            for(i = 1; i < DIM-1; i=i+8){
                _mm256_storeu_ps(
                    &A[ode_index(i,j,k,DIM)],
                    _mm256_sub_ps(
                        _mm256_mul_ps(
                            _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j,k,DIM)]),
                            constVec
                        ),
                        _mm256_add_ps(
                            _mm256_add_ps(
                                _mm256_add_ps(
                                    _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i-1,j,k,DIM)]),
                                    _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i+1,j,k,DIM)])
                                ),
                                _mm256_add_ps(
                                    _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j-1,k,DIM)]),
                                    _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j+1,k,DIM)])
                                )
                            ),
                            _mm256_add_ps(
                                _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j,k-1,DIM)]),
                                _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j,k+1,DIM)])
                            )
                        )
                    )
                );
            }
        }
    }
    convergenceVec = _mm256_setzero_ps();
    denominatorVec = _mm256_set1_ps(CONJUGATE_GRADIENT_EPSILON);
    convergence = 0;
    denominator = CONJUGATE_GRADIENT_EPSILON;
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            // for(i = 1; i < DIM-1; i++){
            //     convergence = convergence + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
            //     // denominator = denominator + p[ode_index(i,j,k,DIM)] * A[ode_index(i,j,k,DIM)];
            // }
            for(i = 1; i < DIM-1; i=i+8){
                //convergence = convergence + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
                rVec = _mm256_loadu_ps(&r[solver_gauss_seidel_get_index(i,j,k,DIM)]);
                rdotVec = _mm256_mul_ps(
                    rVec,
                    rVec
                );
                convergenceVec = _mm256_add_ps(
                    convergenceVec,
                    rdotVec
                );
                //denominator = denominator + p[ode_index(i,j,k,DIM)] * A[ode_index(i,j,k,DIM)];
                denominatorVec = _mm256_add_ps(
                    denominatorVec,
                    _mm256_mul_ps(
                        _mm256_loadu_ps(&A[solver_gauss_seidel_get_index(i,j,k,DIM)]),
                        _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j,k,DIM)])
                    )
                );
            }
        }
    }

    //collect convergence
    _mm256_storeu_ps(vec_sum_storage,convergenceVec);
    convergence =
        vec_sum_storage[0] + vec_sum_storage[1] + 
        vec_sum_storage[2] + vec_sum_storage[3] + 
        vec_sum_storage[4] + vec_sum_storage[5] + 
        vec_sum_storage[6] + vec_sum_storage[7]
    ;

    //collect denominator
    _mm256_storeu_ps(vec_sum_storage,denominatorVec);
    denominator =
        vec_sum_storage[0] + vec_sum_storage[1] + 
        vec_sum_storage[2] + vec_sum_storage[3] + 
        vec_sum_storage[4] + vec_sum_storage[5] + 
        vec_sum_storage[6] + vec_sum_storage[7]
    ;

    //have hit the desired level of convergence
    if(convergence < CONJUGATE_GRADIENT_EPSILON && convergence > -CONJUGATE_GRADIENT_EPSILON){
        return 0.0f;
    }

    //error check
    if(denominator < CONJUGATE_GRADIENT_EPSILON && denominator > -CONJUGATE_GRADIENT_EPSILON){
        printf("Divide by 0! %f \n", denominator);
        fflush(stdout);
    }


    alpha = convergence / denominator;
    r_new_dot = 0;
    alphaVec = _mm256_set1_ps(alpha);
    
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            // for(i = 1; i < DIM-1; i++){
            //     phi[ode_index(i,j,k,DIM)] = phi[ode_index(i,j,k,DIM)] + alpha * p[ode_index(i,j,k,DIM)];
            //     r[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] - alpha * A[ode_index(i,j,k,DIM)];
            //     r_new_dot = r_new_dot + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
            // }
            for(i = 1; i < DIM-1; i=i+8){
                //phi[ode_index(i,j,k,DIM)] = phi[ode_index(i,j,k,DIM)] + alpha * p[ode_index(i,j,k,DIM)];
                _mm256_storeu_ps(
                    &phi[ode_index(i,j,k,DIM)],
                    _mm256_add_ps(
                        _mm256_loadu_ps(&phi[solver_gauss_seidel_get_index(i,j,k,DIM)]),
                        _mm256_mul_ps(
                            alphaVec,
                            _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j,k,DIM)])
                        )
                    )
                );
                // r[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] - alpha * A[ode_index(i,j,k,DIM)];
                _mm256_storeu_ps(
                    &r[ode_index(i,j,k,DIM)],
                    _mm256_sub_ps(
                        _mm256_loadu_ps(&r[solver_gauss_seidel_get_index(i,j,k,DIM)]),
                        _mm256_mul_ps(
                            alphaVec,
                            _mm256_loadu_ps(&A[solver_gauss_seidel_get_index(i,j,k,DIM)])
                        )
                    )
                );
                // r_new_dot = r_new_dot + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
                rdotVec = _mm256_add_ps(
                    rdotVec,
                    _mm256_mul_ps(
                        _mm256_loadu_ps(&r[solver_gauss_seidel_get_index(i,j,k,DIM)]),
                        _mm256_loadu_ps(&r[solver_gauss_seidel_get_index(i,j,k,DIM)])
                    )
                );
            }
        }
    }

    //collect rdot
    _mm256_storeu_ps(vec_sum_storage,rdotVec);
    r_new_dot =
        vec_sum_storage[0] + vec_sum_storage[1] + 
        vec_sum_storage[2] + vec_sum_storage[3] + 
        vec_sum_storage[4] + vec_sum_storage[5] + 
        vec_sum_storage[6] + vec_sum_storage[7]
    ;

    //calculate beta
    beta = r_new_dot / convergence;
    betaVec = _mm256_set1_ps(beta);

    //update p
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            // for(i = 1; i < DIM-1; i++){
            //     p[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] + beta * p[ode_index(i,j,k,DIM)];
            // }
            for(i = 1; i < DIM-1; i=i+8){
                //p[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] + beta * p[ode_index(i,j,k,DIM)];
                _mm256_storeu_ps(
                    &p[ode_index(i,j,k,DIM)],
                    _mm256_add_ps(
                        _mm256_loadu_ps(&r[solver_gauss_seidel_get_index(i,j,k,DIM)]),
                        _mm256_mul_ps(
                            betaVec,
                            _mm256_loadu_ps(&p[solver_gauss_seidel_get_index(i,j,k,DIM)])
                        )
                    )
                );
            }
        }
    }
    return (float)sqrt(convergence);
}

/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method serially
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 * @return The residual
 */
float solver_conjugate_gradient_iterate_navier_stokes_serial(float * phi, float * phi0, float a, float c){
    int i, j, k;
    float convergence, denominator;
    float laplacian, alpha, r_new_dot, beta;
    //solve Ap
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                laplacian = 
                (
                    6 * p[ode_index (i   , j   , k   ,DIM)] +
                    - (
                        p[ode_index (i+1 , j   , k   ,DIM)] +
                        p[ode_index (i-1 , j   , k   ,DIM)] +
                        p[ode_index (i   , j+1 , k   ,DIM)] +
                        p[ode_index (i   , j-1 , k   ,DIM)] +
                        p[ode_index (i   , j   , k+1 ,DIM)] +
                        p[ode_index (i   , j   , k-1 ,DIM)]
                    )
                );
                A[ode_index(i,j,k,DIM)] = laplacian;
            }
        }
    }
    convergence = 0;
    denominator = CONJUGATE_GRADIENT_EPSILON;
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                convergence = convergence + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
                denominator = denominator + p[ode_index(i,j,k,DIM)] * A[ode_index(i,j,k,DIM)];
            }
        }
    }
    if(denominator < CONJUGATE_GRADIENT_EPSILON && denominator > -CONJUGATE_GRADIENT_EPSILON){
        printf("Divide by 0! %f \n", denominator);
        fflush(stdout);
    }
    alpha = convergence / denominator;
    r_new_dot = 0;
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                phi[ode_index(i,j,k,DIM)] = phi[ode_index(i,j,k,DIM)] + alpha * p[ode_index(i,j,k,DIM)];
                r[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] - alpha * A[ode_index(i,j,k,DIM)];
                r_new_dot = r_new_dot + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
            }
        }
    }

    beta = r_new_dot / convergence;
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                p[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] + beta * p[ode_index(i,j,k,DIM)];
            }
        }
    }
    return (float)sqrt(convergence);
}


/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method serially
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param a The a const
 * @param c The c const
 */
void solver_conjugate_gradient_init_navier_stokes_serial(float * phi, float * phi0, float a, float c){
    int i, j, k;
    if(p == NULL){
        p = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    if(r == NULL){
        r = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    if(A == NULL){
        A = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    
    //iniitalize the r (residual) and p (search direction) arrays
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                r[ode_index(i,j,k,DIM)] = phi0[ode_index(i,j,k,DIM)];
                p[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)];
            }
        }
    }
}

/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method serially
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 */
void solver_conjugate_gradient_init_serial(float * phi, float * phi0){
    int i, j, k;
    if(p == NULL){
        p = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    if(r == NULL){
        r = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    if(A == NULL){
        A = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    }
    
    //iniitalize the r (residual) and p (search direction) arrays
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                r[ode_index(i,j,k,DIM)] = phi0[ode_index(i,j,k,DIM)];
                p[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)];
            }
        }
    }
}



/**
 * Iteratively solves an ODE matrix by 1 iteration of conjugate gradient method serially
 * @param phi The phi array
 * @param phi0 The phi array from the last frame
 * @param approximation_func The function to approximate the ode
 * @param residual_func The function to compute the residual
 * @param odeData The ode data
 * @return The residual
 */
float solver_conjugate_gradient_iterate_serial(float * phi, float * phi0, ode_cg_search_direction_stencil stencil_func, OdeData * odeData){
    int i, j, k;
    float convergence, denominator;
    float laplacian, alpha, r_new_dot, beta;
    //solve Ap
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                laplacian = stencil_func(phi,i,j,k,odeData);
                A[ode_index(i,j,k,DIM)] = laplacian;
                // if(fabs(laplacian) > 1000000){
                //     printf("%f\n",laplacian);
                // }
            }
        }
    }
    convergence = 0;
    denominator = CONJUGATE_GRADIENT_EPSILON;
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                convergence = convergence + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
                denominator = denominator + p[ode_index(i,j,k,DIM)] * A[ode_index(i,j,k,DIM)];
                // if(fabs(p[ode_index(i,j,k,DIM)] * A[ode_index(i,j,k,DIM)]) > 1000){
                //     printf("convergence: %f       denominator: %f      \n",
                //         (r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)]),
                //         (p[ode_index(i,j,k,DIM)] * A[ode_index(i,j,k,DIM)])
                //     );
                //     printf("A: %f      \n",
                //        A[ode_index(i,j,k,DIM)]
                //     );
                //     printf("r: %f      \n",
                //        r[ode_index(i,j,k,DIM)]
                //     );
                //     printf("p: %f      \n",
                //        p[ode_index(i,j,k,DIM)]
                //     );
                //     printf("\n");
                //     fflush(stdout);
                // }
            }
        }
    }

    //have hit the desired level of convergence
    if(convergence < CONJUGATE_GRADIENT_EPSILON && convergence > -CONJUGATE_GRADIENT_EPSILON){
        return 0.0f;
    }
    if(denominator < CONJUGATE_GRADIENT_EPSILON && denominator > -CONJUGATE_GRADIENT_EPSILON){
        printf("Divide by 0! %f \n", denominator);
        printf("Convergence:  %f  \n",convergence);
        fflush(stdout);
    }
    alpha = convergence / denominator;
    r_new_dot = 0;
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                phi[ode_index(i,j,k,DIM)] = phi[ode_index(i,j,k,DIM)] + alpha * p[ode_index(i,j,k,DIM)];
                r[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] - alpha * A[ode_index(i,j,k,DIM)];
                r_new_dot = r_new_dot + r[ode_index(i,j,k,DIM)] * r[ode_index(i,j,k,DIM)];
            }
        }
    }

    beta = r_new_dot / convergence;
    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i = 1; i < DIM-1; i++){
                p[ode_index(i,j,k,DIM)] = r[ode_index(i,j,k,DIM)] + beta * p[ode_index(i,j,k,DIM)];
            }
        }
    }
    return (float)sqrt(convergence);
}

