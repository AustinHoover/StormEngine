#include <math.h>

#include "fluid/sim/pressurecell/density.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "math/ode/multigrid_parallel.h"

/**
 * Adds density from the delta buffer to this chunk
*/
LIBRARY_API void fluid_pressurecell_add_density(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * densityArr = chunk->d[CENTER_LOC];
    float * sourceArr = chunk->d0[CENTER_LOC];
    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM-1; x++){
                densityArr[IX(x,y,z)] = densityArr[IX(x,y,z)] + sourceArr[IX(x,y,z)];
            }
        }
    }
}

/**
 * Diffuses the density in this chunk
*/
LIBRARY_API void fluid_pressurecell_diffuse_density(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * densityArr = chunk->d[CENTER_LOC];
    float * densityTemp = chunk->dTempCache;
    float * bounds = chunk->bounds[CENTER_LOC];
    float a = FLUID_PRESSURECELL_DIFFUSION_CONSTANT * environment->consts.dt / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);
    float c = 1+6*a;
    // float residual = 1;
    // int iterations = 0;
    // while(iterations < FLUID_GRID2_SOLVER_MULTIGRID_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
    //     residual = solver_multigrid_parallel_iterate(densityTemp,densityArr,a,c);
    //     // fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY_PHI,x);
    //     for(x = 1; x < DIM-1; x++){
    //         for(y = 1; y < DIM-1; y++){
    //             //diffuse back into the grid
    //             densityTemp[IX(0,x,y)] =     0;
    //             densityTemp[IX(DIM-1,x,y)] = 0;
    //             densityTemp[IX(x,0,y)] =     0;
    //             densityTemp[IX(x,DIM-1,y)] = 0;
    //             densityTemp[IX(x,y,0)] =     0;
    //             densityTemp[IX(x,y,DIM-1)] = 0;
    //         }
    //     }
    //     iterations++;
    // }
    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM-1; x++){
                if(bounds[IX(x,y,z)] > 0){
                    continue;
                }
                // if(x == 4 && y == 4 && z == 4){
                //     printf("arrs\n");
                //     printf("%f \n", densityArr[IX(x,y,z)]);
                //     printf("%f \n", densityArr[IX(x-1,y,z)]);
                //     printf("%f \n", densityArr[IX(x+1,y,z)]);
                //     printf("%f \n", densityArr[IX(x,y-1,z)]);
                //     printf("%f \n", densityArr[IX(x,y+1,z)]);
                //     printf("%f \n", densityArr[IX(x,y,z-1)]);
                //     printf("%f \n", densityArr[IX(x,y,z+1)]);

                //     printf("bounds\n");
                //     printf("%f \n", bounds[IX(x-1,y,z)]);
                //     printf("%f \n", bounds[IX(x+1,y,z)]);
                //     printf("%f \n", bounds[IX(x,y-1,z)]);
                //     printf("%f \n", bounds[IX(x,y+1,z)]);
                //     printf("%f \n", bounds[IX(x,y,z-1)]);
                //     printf("%f \n", bounds[IX(x,y,z+1)]);
                //     printf("a: %f \n", a);
                //     float boundSum = -(
                //         (1.0f - bounds[IX(x-1,y,z)]) +
                //         (1.0f - bounds[IX(x+1,y,z)]) +
                //         (1.0f - bounds[IX(x,y-1,z)]) +
                //         (1.0f - bounds[IX(x,y+1,z)]) +
                //         (1.0f - bounds[IX(x,y,z-1)]) +
                //         (1.0f - bounds[IX(x,y,z+1)])
                //     );
                //     printf("boundSum: %f\n", boundSum);
                //     float neighborSum = (
                //         densityArr[IX(x-1,y,z)] * (1.0f - bounds[IX(x-1,y,z)]) +
                //         densityArr[IX(x+1,y,z)] * (1.0f - bounds[IX(x+1,y,z)]) +
                //         densityArr[IX(x,y-1,z)] * (1.0f - bounds[IX(x,y-1,z)]) +
                //         densityArr[IX(x,y+1,z)] * (1.0f - bounds[IX(x,y+1,z)]) +
                //         densityArr[IX(x,y,z-1)] * (1.0f - bounds[IX(x,y,z-1)]) +
                //         densityArr[IX(x,y,z+1)] * (1.0f - bounds[IX(x,y,z+1)])
                //     );
                //     printf("neighborSum: %f\n", neighborSum);
                //     float combined = densityArr[IX(x,y,z)] * -(
                //         (1.0f - bounds[IX(x-1,y,z)]) +
                //         (1.0f - bounds[IX(x+1,y,z)]) +
                //         (1.0f - bounds[IX(x,y-1,z)]) +
                //         (1.0f - bounds[IX(x,y+1,z)]) +
                //         (1.0f - bounds[IX(x,y,z-1)]) +
                //         (1.0f - bounds[IX(x,y,z+1)])
                //     ) +
                //     (
                //         densityArr[IX(x-1,y,z)] * (1.0f - bounds[IX(x-1,y,z)]) +
                //         densityArr[IX(x+1,y,z)] * (1.0f - bounds[IX(x+1,y,z)]) +
                //         densityArr[IX(x,y-1,z)] * (1.0f - bounds[IX(x,y-1,z)]) +
                //         densityArr[IX(x,y+1,z)] * (1.0f - bounds[IX(x,y+1,z)]) +
                //         densityArr[IX(x,y,z-1)] * (1.0f - bounds[IX(x,y,z-1)]) +
                //         densityArr[IX(x,y,z+1)] * (1.0f - bounds[IX(x,y,z+1)])
                //     );
                //     printf("combined: %f\n", combined);
                //     float modified = combined * a;
                //     printf("modified: %f\n", modified);
                // }
                densityTemp[IX(x,y,z)] = densityArr[IX(x,y,z)] +
                (
                    densityArr[IX(x,y,z)] * -(
                        (1.0f - bounds[IX(x-1,y,z)]) +
                        (1.0f - bounds[IX(x+1,y,z)]) +
                        (1.0f - bounds[IX(x,y-1,z)]) +
                        (1.0f - bounds[IX(x,y+1,z)]) +
                        (1.0f - bounds[IX(x,y,z-1)]) +
                        (1.0f - bounds[IX(x,y,z+1)])
                    ) +
                    (
                        densityArr[IX(x-1,y,z)] * (1.0f - bounds[IX(x-1,y,z)]) +
                        densityArr[IX(x+1,y,z)] * (1.0f - bounds[IX(x+1,y,z)]) +
                        densityArr[IX(x,y-1,z)] * (1.0f - bounds[IX(x,y-1,z)]) +
                        densityArr[IX(x,y+1,z)] * (1.0f - bounds[IX(x,y+1,z)]) +
                        densityArr[IX(x,y,z-1)] * (1.0f - bounds[IX(x,y,z-1)]) +
                        densityArr[IX(x,y,z+1)] * (1.0f - bounds[IX(x,y,z+1)])
                    )
                ) * a
                ;
                if(FLUID_PRESSURECELL_ENABLE_CLAMP_MIN_DENSITY && densityTemp[IX(x,y,z)] < FLUID_PRESSURECELL_MIN_DENSITY_CLAMP_CUTOFF){
                    densityTemp[IX(x,y,z)] = 0;
                }
            }
        }
    }
    // for(x = 1; x < DIM-1; x++){
    //     for(y = 1; y < DIM-1; y++){
    //         //diffuse back into the grid
    //         densityTemp[IX(1,x,y)] =     densityTemp[IX(1,x,y)] +     densityTemp[IX(0,x,y)];
    //         densityTemp[IX(DIM-2,x,y)] = densityTemp[IX(DIM-2,x,y)] + densityTemp[IX(DIM-1,x,y)];
    //         densityTemp[IX(x,1,y)] =     densityTemp[IX(x,1,y)] +     densityTemp[IX(x,0,y)];
    //         densityTemp[IX(x,DIM-2,y)] = densityTemp[IX(x,DIM-2,y)] + densityTemp[IX(x,DIM-1,y)];
    //         densityTemp[IX(x,y,1)] =     densityTemp[IX(x,y,1)] +     densityTemp[IX(x,y,0)];
    //         densityTemp[IX(x,y,DIM-2)] = densityTemp[IX(x,y,DIM-2)] + densityTemp[IX(x,y,DIM-1)];
    //     }
    // }
}

/**
 * Advects the density of this chunk
*/
LIBRARY_API void fluid_pressurecell_advect_density(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * densityArr = chunk->d[CENTER_LOC];
    float * densityTemp = chunk->dTempCache;
    float * u = chunk->uTempCache;
    float * v = chunk->vTempCache;
    float * w = chunk->wTempCache;
    int x0, x1, y0, y1, z0, z1;
    float xp, yp, zp;
    float s0, s1, t0, t1, u0, u1;
    float interpolated;
    float vecU, vecV, vecW;
    float interpConst = environment->consts.dt / FLUID_PRESSURECELL_SPACING;
    for(y = 1; y < DIM-1; y++){
        //TODO: eventually skip y levels if there is no density to advect
        for(z = 1; z < DIM-1; z++){
            for(x = 1; x < DIM-1; x++){
                //calculate the real (float) position we are at
                vecU = u[IX(x,y,z)] * interpConst;
                vecV = v[IX(x,y,z)] * interpConst;
                vecW = w[IX(x,y,z)] * interpConst;
                if(vecU > 0.999f){
                    vecU = 0.999f;
                } else if(vecU < -0.999f){
                    vecU = -0.999f;
                }
                if(vecV > 0.999f){
                    vecV = 0.999f;
                } else if(vecV < -0.999f){
                    vecV = -0.999f;
                }
                if(vecW > 0.999f){
                    vecW = 0.999f;
                } else if(vecW < -0.999f){
                    vecW = -0.999f;
                }

                //calculate percentage to pull from existing vs other
                xp = x - vecU;
                yp = y - vecV;
                zp = z - vecW;

                //clamp to border
                x0 = xp;
                y0 = yp;
                z0 = zp;
                //find far border
                x1 = x0 + 1;
                y1 = y0 + 1;
                z1 = z0 + 1;

                //calculate the percentage to sample from each side of border
                s1 = xp-x0;
                s0 = 1-s1;

                t1 = yp-y0;
                t0 = 1-t1;

                u1 = zp-z0;
                u0 = 1-u1;


                if(
                    x0 < 0 || y0 < 0 || z0 < 0 ||
                    x0 > DIM-1 || y0 > DIM-1 || z0 > DIM-1 ||
                    x1 < 0 || y1 < 0 || z1 < 0 ||
                    x1 > DIM-1 || y1 > DIM-1 || z1 > DIM-1
                ){
                    printf("advect dens:\n");
                    printf("%d %d %d\n", x0, y0, z0);
                    printf("%d %d %d\n", x1, y1, z1);
                    printf("%f %f %f\n", xp, yp, zp);
                    printf("%f %f %f\n", u[IX(x,y,z)], v[IX(x,y,z)], w[IX(x,y,z)]);
                    printf("%f %f %f\n", vecU, vecV, vecW);
                    printf("%f\n", environment->consts.dt);
                    fflush(stdout);
                }

                interpolated = 
                s0*(
                    t0*u0*densityTemp[IX(x0,y0,z0)]+
                    t1*u0*densityTemp[IX(x0,y1,z0)]+
                    t0*u1*densityTemp[IX(x0,y0,z1)]+
                    t1*u1*densityTemp[IX(x0,y1,z1)]
                )+
                s1*(
                    t0*u0*densityTemp[IX(x1,y0,z0)]+
                    t1*u0*densityTemp[IX(x1,y1,z0)]+
                    t0*u1*densityTemp[IX(x1,y0,z1)]+
                    t1*u1*densityTemp[IX(x1,y1,z1)]
                );

                densityArr[IX(x,y,z)] = fmax(MIN_FLUID_VALUE,fmin(MAX_FLUID_VALUE,interpolated));
            }
        }
    }
}