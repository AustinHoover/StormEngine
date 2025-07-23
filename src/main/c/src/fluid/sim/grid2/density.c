#include <stdio.h>
#include <immintrin.h>
#include <stdint.h>
#include <math.h>

#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/grid2/density.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/utilities.h"
#include "math/ode/multigrid.h"
#include "math/ode/multigrid_parallel.h"
#include "math/ode/gauss_seidel.h"


/**
 * Adds density to the density array
 * @return The change in density within this chunk for this frame
*/
void fluid_grid2_addDensity(
    Environment * environment,
    float ** d,
    float ** d0,
    float dt
){
    int i;
    int size=DIM*DIM*DIM;
    __m256 minVec = _mm256_set1_ps(MIN_FLUID_VALUE);
    __m256 maxVec = _mm256_set1_ps(MAX_FLUID_VALUE);
    __m256 existing;
    __m256 delta;
    __m256 dtVec = _mm256_set1_ps(dt);
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    float * s = GET_ARR_RAW(d0,CENTER_LOC);
    for(i=0; i<size; i=i+8){
        existing = _mm256_loadu_ps(&x[i]);
        delta = _mm256_loadu_ps(&s[i]);
        _mm256_storeu_ps(&x[i],
            _mm256_max_ps(
                _mm256_min_ps(
                    _mm256_add_ps(
                        existing,
                        _mm256_mul_ps(
                            delta,
                            dtVec
                        )
                    ),
                    maxVec
                ),
                minVec
            )
        );
    }
}

/*
 * A single iteration of the jacobi to solve density diffusion
 */
LIBRARY_API void fluid_grid2_solveDiffuseDensity(
    Environment * environment,
    float ** d,
    float ** d0,
    float dt
){
    float a=dt*FLUID_GRID2_DIFFUSION_CONSTANT/(FLUID_GRID2_H*FLUID_GRID2_H);
    float c=1+6*a;
    int i, j, k, l, m;
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    float * x0 = GET_ARR_RAW(d0,CENTER_LOC);

    //about ~40% faster than gauss seidel
    float residual = 1;
    int iterations = 0;
    while(iterations < FLUID_GRID2_SOLVER_MULTIGRID_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
        residual = solver_multigrid_parallel_iterate(x,x0,a,c);
        fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY_PHI,x);
        iterations++;
    }
    
    // //about ~40% slower than multigrid
    // for(int l = 0; l < FLUID_GRID2_LINEARSOLVERTIMES; l++){
    //     //iterate
    //     solver_gauss_seidel_iterate_parallel(x,x0,a,c,DIM);

    //     //set bounds
    //     fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY_PHI,x);
    // }
}

/**
 * Advects the density based on the vectors
*/
LIBRARY_API void fluid_grid2_advectDensity(Environment * environment, float ** d, float ** d0, float ** ur, float ** vr, float ** wr, float dt){
    int i, j, k, i0, j0, k0, i1, j1, k1;
    int m,n,o;
    float x, y, z, s0, t0, s1, t1, u1, u0, dtx,dty,dtz;
    dtx = dt/FLUID_GRID2_H;
    dty = dt/FLUID_GRID2_H;
    dtz = dt/FLUID_GRID2_H;

    float * center_d = GET_ARR_RAW(d,CENTER_LOC);
    float * center_d0 = GET_ARR_RAW(d0,CENTER_LOC);

    float * u = GET_ARR_RAW(ur,CENTER_LOC);
    float * v = GET_ARR_RAW(vr,CENTER_LOC);
    float * w = GET_ARR_RAW(wr,CENTER_LOC);

    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i=1; i<DIM-1; i++){
                center_d0 = GET_ARR_RAW(d0,CENTER_LOC);
                //calculate location to pull from
                x = i-dtx*u[IX(i,j,k)];
                y = j-dty*v[IX(i,j,k)];
                z = k-dtz*w[IX(i,j,k)];

                //clamp location within chunk
                //get indices, and calculate percentage to pull from each index
                if(x < 0.5f){
                    x=0.5f;
                    i0=0;
                    i1=1;
                } else if(x > (DIM - 2) + 0.5f){
                    x = (DIM - 2) + 0.5f;
                    i0=DIM-2;
                    i1=DIM-1;
                } else {
                    i0=(int)x;
                    i1=i0+1;
                }

                if(y < 0.5f){
                    y=0.5f;
                    j0=(int)0;
                    j1=1;
                } else if(y > (DIM - 2) + 0.5f){
                    y = (DIM - 2) + 0.5f;
                    j0=DIM-2;
                    j1=DIM-1;
                } else {
                    j0=(int)y;
                    j1=j0+1;
                }


                if(z < 0.5f){
                    z=0.5f;
                    k0=(int)0;
                    k1=1;
                } else if(z > (DIM - 2) + 0.5f){
                    z = (DIM - 2) + 0.5f;
                    k0=DIM-2;
                    k1=DIM-1;
                } else {
                    k0=(int)z;
                    k1=k0+1;
                }

                s1 = x-i0;
                s0 = 1-s1;

                t1 = y-j0;
                t0 = 1-t1;

                u1 = z-k0;
                u0 = 1-u1;

                if(
                    i0 < 0 || j0 < 0 || k0 < 0 ||
                    i0 > DIM-1 || j0 > DIM-1 || k0 > DIM-1 ||
                    i1 < 0 || j1 < 0 || k1 < 0 ||
                    i1 > DIM-1 || j1 > DIM-1 || k1 > DIM-1
                ){
                    printf("advect dens: %d %d %d   %d %d %d ---  %f %f %f\n", i0, j0, k0, i1, j1, k1, x, y, z);
                    fflush(stdout);
                }
                
                center_d[IX(i,j,k)] = 
                s0*(
                    t0*u0*center_d0[IX(i0,j0,k0)]+
                    t1*u0*center_d0[IX(i0,j1,k0)]+
                    t0*u1*center_d0[IX(i0,j0,k1)]+
                    t1*u1*center_d0[IX(i0,j1,k1)]
                )+
                s1*(
                    t0*u0*center_d0[IX(i1,j0,k0)]+
                    t1*u0*center_d0[IX(i1,j1,k0)]+
                    t0*u1*center_d0[IX(i1,j0,k1)]+
                    t1*u1*center_d0[IX(i1,j1,k1)]
                );

                // if(i == 3 && j == 2 && k == 2){
                //     printf("density at <%d,%d,%d> \n",i,j,k);
                //     printf("sample point precise: %.2f %.2f %.2f\n",x,y,z);
                //     printf("sample box range: <%d,%d,%d> -> <%d,%d,%d> \n",i0,j0,k0,i1,j1,k1);
                //     printf("sample values: %.2f %.2f    %.2f %.2f  \n",
                //         center_d0[IX(i0,j0,k0)],
                //         center_d0[IX(i0,j1,k0)],
                //         center_d0[IX(i0,j0,k1)],
                //         center_d0[IX(i0,j1,k1)]
                //     );
                //     printf("sample values: %.2f %.2f    %.2f %.2f  \n",
                //         center_d0[IX(i1,j0,k0)],
                //         center_d0[IX(i1,j1,k0)],
                //         center_d0[IX(i1,j0,k1)],
                //         center_d0[IX(i1,j1,k1)]
                //     );
                //     //print ints
                //     // printf("i0: %d\n",i0);
                //     // printf("j0: %d\n",j0);
                //     // printf("k0: %d\n",k0);
                //     // printf("i1: %d\n",i1);
                //     // printf("j1: %d\n",j1);
                //     // printf("k1: %d\n",k1);
                //     // printf("m: %d\n",m);
                //     // printf("n: %d\n",n);
                //     // printf("o: %d\n",o);

                //     //print floats
                //     // printf("x: %f\n",x);
                //     // printf("y: %f\n",y);
                //     // printf("z: %f\n",z);

                //     // printf("t0: %f\n",s0);
                //     // printf("s0: %f\n",t0);
                //     // printf("t1: %f\n",s1);
                //     // printf("s1: %f\n",t1);
                //     // printf("u0: %f\n",u0);
                //     // printf("u1: %f\n",u1);

                //     // printf("dtx: %f\n",dtx);
                //     // printf("dty: %f\n",dty);
                //     // printf("dtz: %f\n",dtz);
                //     printf("\n");
                // }
            }
        }
    }
    //set bounds
    fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY,center_d);
}

/**
 * Normalizes the density array with a given ratio
 */
void fluid_grid2_normalizeDensity(Environment * environment, float ** d, float ratio){
    int j;
    int size=DIM*DIM*DIM;
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    //apply a n ease in-out quart function to weight mass normalization to places with already larger masses
    for(j=0; j<size; j++){
        float value = x[j];
        x[j] = value * ratio;
        // x[j] = value;
        // if(value > NORMALIZATION_CUTOFF){
        //     value = fmax(value,NORMALIZATION_CLAMP_VAL) * ratio;
        //     x[j] = value;
        // } else {
        //     x[j] = 0;
        // }
    }
}


/**
 * Normalizes the density array with a given ratio
 */
double fluid_grid2_sum_for_normalization(Environment * environment, Chunk * chunk){
    int j;
    int size=DIM*DIM*DIM;
    float * x = chunk->d[CENTER_LOC];
    double rVal = 0;
    for(int i = 1; i < DIM - 1; i++){
        for(int j = 1; j < DIM - 1; j++){
            for(int k = 1; k < DIM - 1; k++){
                float val = x[IX(i,j,k)];
                rVal = rVal + val;
                // if(val > NORMALIZATION_CUTOFF){
                //     rVal = rVal + fmax(val,NORMALIZATION_CLAMP_VAL);
                // }
            }
        }
    }
    return rVal;
}
