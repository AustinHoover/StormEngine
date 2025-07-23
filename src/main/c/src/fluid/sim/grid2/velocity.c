#include <stdio.h>
#include <immintrin.h>
#include <stdint.h>

#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/velocity.h"
#include "fluid/sim/grid2/utilities.h"
#include "math/ode/gauss_seidel.h"
#include "math/ode/multigrid_parallel.h"
#include "math/ode/conjugate_gradient.h"
#include "math/ode/diffusion_ode.h"
#include "util/matrix.h"

#define SET_BOUND_IGNORE 0
#define SET_BOUND_USE_NEIGHBOR 1


/*
 * Adds force to all vectors
 */
void fluid_grid2_addSourceToVectors(
    Environment * environment,
    float ** jru,
    float ** jrv,
    float ** jrw,
    float ** jru0,
    float ** jrv0,
    float ** jrw0,
    float dt
){
    fluid_grid2_add_source(GET_ARR_RAW(jru,CENTER_LOC),GET_ARR_RAW(jru0,CENTER_LOC),dt);
    fluid_grid2_add_source(GET_ARR_RAW(jrv,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),dt);
    fluid_grid2_add_source(GET_ARR_RAW(jrw,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
}

/*
 * Solves vector diffusion along all axis
 */
LIBRARY_API void fluid_grid2_solveVectorDiffuse(
    Environment * environment,
    float ** jru,
    float ** jrv,
    float ** jrw,
    float ** jru0,
    float ** jrv0,
    float ** jrw0,
    float dt
){
    float a = dt*FLUID_GRID2_VISCOSITY_CONSTANT/(FLUID_GRID2_H*FLUID_GRID2_H);
    float c = 1+6*a;
    int i, j, k, l, m;
    float * u = GET_ARR_RAW(jru,CENTER_LOC);
    float * v = GET_ARR_RAW(jrv,CENTER_LOC);
    float * w = GET_ARR_RAW(jrw,CENTER_LOC);
    float * u0 = GET_ARR_RAW(jru0,CENTER_LOC);
    float * v0 = GET_ARR_RAW(jrv0,CENTER_LOC);
    float * w0 = GET_ARR_RAW(jrw0,CENTER_LOC);
    
    //about ~30% faster
    for(int l = 0; l < FLUID_GRID2_LINEARSOLVERTIMES; l++){
        //transform u direction
        solver_gauss_seidel_iterate_parallel(u,u0,a,c,DIM);

        //transform v direction
        solver_gauss_seidel_iterate_parallel(v,v0,a,c,DIM);

        //transform w direction
        solver_gauss_seidel_iterate_parallel(w,w0,a,c,DIM);

        //set bounds
        fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_U,u);
        fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_V,v);
        fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_W,w);
    }

    float residual;
    int iterations;

    // residual = 1;
    // iterations = 0;
    // while(iterations < FLUID_GRID2_LINEARSOLVERTIMES && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
    //     residual = solver_multigrid_parallel_iterate(u,u0,a,c);
    //     fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_U,u);
    //     iterations++;
    // }

    // residual = 1;
    // iterations = 0;
    // while(iterations < FLUID_GRID2_LINEARSOLVERTIMES && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
    //     residual = solver_multigrid_parallel_iterate(v,v0,a,c);
    //     fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_V,v);
    //     iterations++;
    // }

    // residual = 1;
    // iterations = 0;
    // while(iterations < FLUID_GRID2_LINEARSOLVERTIMES && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
    //     residual = solver_multigrid_parallel_iterate(w,w0,a,c);
    //     fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_W,w);
    //     iterations++;
    // }





    // //init CG solver
    // solver_conjugate_gradient_init_serial(u,u0);
    // residual = 1;
    // iterations = 0;
    // //solve with CG
    // while(iterations < FLUID_GRID2_SOLVER_CG_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_CG_TOLERANCE || residual < -FLUID_GRID2_SOLVER_CG_TOLERANCE)){
    //     residual = solver_conjugate_gradient_iterate_serial(u,u0,ode_diffusion_cg_stencil, (OdeData *)&(environment->state.grid2.diffuseData));
    //     fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_U,u);
    //     iterations++;
    // }

    // //init CG solver
    // solver_conjugate_gradient_init_serial(v,v0);
    // residual = 1;
    // iterations = 0;
    // //solve with CG
    // while(iterations < FLUID_GRID2_SOLVER_CG_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_CG_TOLERANCE || residual < -FLUID_GRID2_SOLVER_CG_TOLERANCE)){
    //     residual = solver_conjugate_gradient_iterate_parallel(v,v0,a,c);
    //     residual = solver_conjugate_gradient_iterate_serial(v,v0,ode_diffusion_cg_stencil, (OdeData *)&(environment->state.grid2.diffuseData));
    //     fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_V,v);
    //     iterations++;
    // }

    // //init CG solver
    // solver_conjugate_gradient_init_serial(w,w0);
    // residual = 1;
    // iterations = 0;
    // //solve with CG
    // while(iterations < FLUID_GRID2_SOLVER_CG_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_CG_TOLERANCE || residual < -FLUID_GRID2_SOLVER_CG_TOLERANCE)){
    //     residual = solver_conjugate_gradient_iterate_serial(w,w0,ode_diffusion_cg_stencil, (OdeData *)&(environment->state.grid2.diffuseData));
    //     fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_DIFFUSE_PHI_W,w);
    //     iterations++;
    // }
}

/**
 * Sets up a projection system of equations
 * It stores the first derivative of the field in pr, and zeroes out divr.
 * This allows you to calculate the second derivative into divr using the derivative stored in pr.
 * @param ur The x velocity grid
 * @param vr The y velocity grid
 * @param wr The z velocity grid
 * @param pr The grid that will contain the first derivative
 * @param divr The grid that will be zeroed out in preparation of the solver
 * @param DIFFUSION_CONST The diffusion constant
 * @param VISCOSITY_CONST The viscosity constant
 * @param dt The timestep for the simulation
 */
LIBRARY_API void fluid_grid2_setupProjection(
    Environment * environment,
    Chunk * chunk,
    float ** ur,
    float ** vr,
    float ** wr,
    float ** pr,
    float ** divr,
    float dt
){
    int i, j, k;

    __m256 constScalar = _mm256_set1_ps(-0.5f * FLUID_GRID2_H);
    __m256 zeroVec = _mm256_set1_ps(0);
    __m256 vector, vector2, vector3;

    float * u = GET_ARR_RAW(ur,CENTER_LOC);
    float * v = GET_ARR_RAW(vr,CENTER_LOC);
    float * w = GET_ARR_RAW(wr,CENTER_LOC);

    float * p = GET_ARR_RAW(pr,CENTER_LOC);
    float * div = GET_ARR_RAW(divr,CENTER_LOC);

    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            //
            //lower
            //
            i = 1;
            //first part
            vector = _mm256_loadu_ps(&u[IX(i+1,j,k)]);
            vector = _mm256_sub_ps(vector,_mm256_loadu_ps(&u[IX(i-1,j,k)]));
            //second part
            vector2 = _mm256_loadu_ps(&v[IX(i,j+1,k)]);
            vector2 = _mm256_sub_ps(vector2,_mm256_loadu_ps(&v[IX(i,j-1,k)]));
            //third part
            vector3 = _mm256_loadu_ps(&w[IX(i,j,k+1)]);
            vector3 = _mm256_sub_ps(vector3,_mm256_loadu_ps(&w[IX(i,j,k-1)]));
            //multiply and finalize
            vector = _mm256_add_ps(vector,_mm256_add_ps(vector2,vector3));
            vector = _mm256_mul_ps(vector,constScalar);
            //store
            _mm256_storeu_ps(&div[IX(i,j,k)],vector);
            _mm256_storeu_ps(&p[IX(i,j,k)],zeroVec);

            //
            //upper
            //
            i = 9;
            //first part
            vector = _mm256_loadu_ps(&u[IX(i+1,j,k)]);
            vector = _mm256_sub_ps(vector,_mm256_loadu_ps(&u[IX(i-1,j,k)]));
            //second part
            vector2 = _mm256_loadu_ps(&v[IX(i,j+1,k)]);
            vector2 = _mm256_sub_ps(vector2,_mm256_loadu_ps(&v[IX(i,j-1,k)]));
            //third part
            vector3 = _mm256_loadu_ps(&w[IX(i,j,k+1)]);
            vector3 = _mm256_sub_ps(vector3,_mm256_loadu_ps(&w[IX(i,j,k-1)]));
            //multiply and finalize
            vector = _mm256_add_ps(vector,_mm256_add_ps(vector2,vector3));
            vector = _mm256_mul_ps(vector,constScalar);
            //store
            _mm256_storeu_ps(&div[IX(i,j,k)],vector);
            _mm256_storeu_ps(&p[IX(i,j,k)],zeroVec);
            
        }
    }
    //store divergence in cache
    util_matrix_copy(div,chunk->divergenceCache[CENTER_LOC],DIM);
    fluid_grid2_set_bounds(environment,BOUND_SET_PROJECTION_PHI,p);
    fluid_grid2_set_bounds(environment,BOUND_SET_PROJECTION_PHI_0,div);
}

/**
 * Solves a projection system of equations.
 * This performs a single iteration across a the p grid to approximate the gradient field.
 * @param jru0 The gradient field
 * @param jrv0 The first derivative field
 */
LIBRARY_API void fluid_grid2_solveProjection(
    Environment * environment,
    Chunk * chunk,
    float ** jru0,
    float ** jrv0,
    float dt
){
    float a = 1;
    float c = 6;
    int i, j, k, l, m;
    __m256 cScalar = _mm256_set1_ps(c);
    __m256 vector;

    float * p = GET_ARR_RAW(jru0,CENTER_LOC);
    float * div = GET_ARR_RAW(jrv0,CENTER_LOC);

    //perform iteration of v cycle multigrid method
    chunk->projectionResidual = 1;
    chunk->projectionIterations = 0;
    while(chunk->projectionIterations < FLUID_GRID2_SOLVER_MULTIGRID_MAX_ITERATIONS && (chunk->projectionResidual > FLUID_GRID2_PROJECTION_CONVERGENCE_TOLERANCE || chunk->projectionResidual < -FLUID_GRID2_PROJECTION_CONVERGENCE_TOLERANCE)){
        chunk->projectionResidual = solver_multigrid_parallel_iterate(p,div,a,c);
        fluid_grid2_set_bounds(environment,BOUND_SET_PROJECTION_PHI,p);
        chunk->projectionIterations++;
    }
    // if(chunk->projectionResidual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || chunk->projectionResidual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE){
    //     // printf("Projection residual didn't converge!   %f  \n",residual);
    // }

    // for(i = 0; i < 100; i++){
    //     solver_gauss_seidel_iterate_parallel(p,div,a,c,DIM);
    //     fluid_grid2_set_bounds(FLUID_GRID2_BOUND_NO_DIR,p);
    // }

    //init CG solver
    // solver_conjugate_gradient_init_serial(p,div);
    // chunk->projectionIterations = 0;
    // //solve with CG
    // while(chunk->projectionIterations < FLUID_GRID2_SOLVER_CG_MAX_ITERATIONS && (chunk->projectionResidual > FLUID_GRID2_SOLVER_CG_TOLERANCE || chunk->projectionResidual < -FLUID_GRID2_SOLVER_CG_TOLERANCE)){
    //     chunk->projectionResidual = solver_conjugate_gradient_iterate_parallel(p,div,a,c);;
    //     fluid_grid2_set_bounds(environment,BOUND_SET_PROJECTION_PHI,p);
    //     chunk->projectionIterations++;
    // }
    // if(chunk->projectionResidual > FLUID_GRID2_SOLVER_CG_TOLERANCE || chunk->projectionResidual < -FLUID_GRID2_SOLVER_CG_TOLERANCE){
    //     // printf("Projection residual didn't converge!   %f  \n",chunk->projectionResidual);
    // }

    //store scalar potential in cache
    util_matrix_copy(p,chunk->pressureCache[CENTER_LOC],DIM);
}

/**
 * Finalizes a projection.
 * This subtracts the difference delta along the approximated gradient field.
 * Thus we are left with an approximately mass-conserved field. 
 */
LIBRARY_API void fluid_grid2_finalizeProjection(
    Environment * environment,
    float ** jru,
    float ** jrv,
    float ** jrw,
    float ** jru0,
    float ** jrv0,
    float dt
){
    int i, j, k;
    __m256 constScalar = _mm256_set1_ps(2.0f*FLUID_GRID2_H);
    __m256 vector, vector2, vector3;

    float * u = GET_ARR_RAW(jru,CENTER_LOC);
    float * v = GET_ARR_RAW(jrv,CENTER_LOC);
    float * w = GET_ARR_RAW(jrw,CENTER_LOC);

    float * p = GET_ARR_RAW(jru0,CENTER_LOC);
    float * div = GET_ARR_RAW(jrv0,CENTER_LOC);

    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            //
            //v
            //
            //lower
            vector =  _mm256_loadu_ps(&p[IX(1+1,j,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(1-1,j,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_div_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&u[IX(1,j,k)]),vector);
            _mm256_storeu_ps(&u[IX(1,j,k)],vector);
            //upper
            vector =  _mm256_loadu_ps(&p[IX(9+1,j,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(9-1,j,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_div_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&u[IX(9,j,k)]),vector);
            _mm256_storeu_ps(&u[IX(9,j,k)],vector);
            //
            //v
            //
            //lower
            vector =  _mm256_loadu_ps(&p[IX(1,j+1,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(1,j-1,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_div_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&v[IX(1,j,k)]),vector);
            _mm256_storeu_ps(&v[IX(1,j,k)],vector);
            //upper
            vector =  _mm256_loadu_ps(&p[IX(9,j+1,k)]);
            vector2 = _mm256_loadu_ps(&p[IX(9,j-1,k)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_div_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&v[IX(9,j,k)]),vector);
            _mm256_storeu_ps(&v[IX(9,j,k)],vector);
            //
            //w
            //
            //lower
            vector =  _mm256_loadu_ps(&p[IX(1,j,k+1)]);
            vector2 = _mm256_loadu_ps(&p[IX(1,j,k-1)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_div_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&w[IX(1,j,k)]),vector);
            _mm256_storeu_ps(&w[IX(1,j,k)],vector);
            //upper
            vector =  _mm256_loadu_ps(&p[IX(9,j,k+1)]);
            vector2 = _mm256_loadu_ps(&p[IX(9,j,k-1)]);
            vector =  _mm256_sub_ps(vector,vector2);
            vector = _mm256_div_ps(vector,constScalar);
            vector = _mm256_sub_ps(_mm256_loadu_ps(&w[IX(9,j,k)]),vector);
            _mm256_storeu_ps(&w[IX(9,j,k)],vector);
        }
    }
    //set bounds
    fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_U,u);
    fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_V,v);
    fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_W,w);
}

/*
 * Advects u, v, and w
 */
LIBRARY_API void fluid_grid2_advectVectors(
    Environment * environment,
    float ** jru,
    float ** jrv,
    float ** jrw,
    float ** jru0,
    float ** jrv0,
    float ** jrw0,
    float dt
){
    fluid_grid2_advect_velocity(environment,FLUID_GRID2_DIRECTION_U,jru,jru0,GET_ARR_RAW(jru0,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
    fluid_grid2_advect_velocity(environment,FLUID_GRID2_DIRECTION_V,jrv,jrv0,GET_ARR_RAW(jru0,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
    fluid_grid2_advect_velocity(environment,FLUID_GRID2_DIRECTION_W,jrw,jrw0,GET_ARR_RAW(jru0,CENTER_LOC),GET_ARR_RAW(jrv0,CENTER_LOC),GET_ARR_RAW(jrw0,CENTER_LOC),dt);
}

/**
 * Actually performs the advection
*/
void fluid_grid2_advect_velocity(Environment * environment, int b, float ** jrd, float ** jrd0, float * u, float * v, float * w, float dt){
    int i, j, k, i0, j0, k0, i1, j1, k1;
    int m,n,o;
    float x, y, z, s0, t0, s1, t1, u1, u0, dtx,dty,dtz;
    
    dtx = dt/FLUID_GRID2_H;
    dty = dt/FLUID_GRID2_H;
    dtz = dt/FLUID_GRID2_H;

    float * d = GET_ARR_RAW(jrd,CENTER_LOC);

    float * d0 = GET_ARR_RAW(jrd0,CENTER_LOC);

    for(k=1; k<DIM-1; k++){
        for(j=1; j<DIM-1; j++){
            for(i=1; i<DIM-1; i++){
                d0 = GET_ARR_RAW(jrd0,CENTER_LOC);
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
                    printf("advect vel: %d %d %d   %d %d %d ---  %f %f %f\n", i0, j0, k0, i1, j1, k1, x, y, z);
                    fflush(stdout);
                }
                

                d[IX(i,j,k)] = 
                s0*(
                    t0*u0*d0[IX(i0,j0,k0)]+
                    t1*u0*d0[IX(i0,j1,k0)]+
                    t0*u1*d0[IX(i0,j0,k1)]+
                    t1*u1*d0[IX(i0,j1,k1)]
                )+
                s1*(
                    t0*u0*d0[IX(i1,j0,k0)]+
                    t1*u0*d0[IX(i1,j1,k0)]+
                    t0*u1*d0[IX(i1,j0,k1)]+
                    t1*u1*d0[IX(i1,j1,k1)]
                );

                // if(i == 14 && j == 14 && k == 14){
                //     printf("density at <%d,%d,%d> \n",i,j,k);
                //     printf("sample point precise: %.2f %.2f %.2f\n",x,y,z);
                //     printf("sample box range: <%d,%d,%d> -> <%d,%d,%d> \n",i0,j0,k0,i1,j1,k1);
                //     printf("sample values: %.2f %.2f    %.2f %.2f  \n",
                //         d0[IX(i0,j0,k0)],
                //         d0[IX(i0,j1,k0)],
                //         d0[IX(i0,j0,k1)],
                //         d0[IX(i0,j1,k1)]
                //     );
                //     printf("sample values: %.2f %.2f    %.2f %.2f  \n",
                //         d0[IX(i1,j0,k0)],
                //         d0[IX(i1,j1,k0)],
                //         d0[IX(i1,j0,k1)],
                //         d0[IX(i1,j1,k1)]
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
    fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_U,u);
    fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_V,v);
    fluid_grid2_set_bounds(environment,BOUND_SET_VECTOR_W,w);
}
