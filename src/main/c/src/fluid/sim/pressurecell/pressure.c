#include <math.h>

#include "fluid/sim/pressurecell/pressure.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "math/ode/multigrid_parallel.h"

/**
 * Divergence level we warn at
 */
#define DIVERGENCE_WARNING_VALUE 300.0f

/**
 * Approximates the pressure for this chunk
*/
LIBRARY_API void pressurecell_approximate_pressure(Environment * environment, Chunk * chunk){
    int x, y, z;
    //values stored across frames
    float * presureCache = chunk->pressureCache[CENTER_LOC];
    float * divCache = chunk->divergenceCache[CENTER_LOC];
    float * uArr = chunk->u[CENTER_LOC];
    float * vArr = chunk->v[CENTER_LOC];
    float * wArr = chunk->w[CENTER_LOC];
    float * border = chunk->bounds[CENTER_LOC];
    // float * uArr = chunk->uTempCache;
    // float * vArr = chunk->vTempCache;
    // float * wArr = chunk->wTempCache;
    //temporary caches
    float * pressureCache = chunk->pressureCache[CENTER_LOC];
    float * pressureTemp = chunk->pressureTempCache;
    float * phi0 = chunk->dTempCache;
    //consts/vars
    float du, dv, dw;
    float newPressure;
    float dt = environment->consts.dt;
    // for(z = 1; z < DIM-1; z++){
    //     for(y = 1; y < DIM-1; y++){
    //         for(x = 1; x < DIM-1; x++){
    //             //divergence part
    //             //positive value means inflow
    //             //negative value means outflow
    //             float newDiv = 
    //             (
    //                 uArr[IX(x+1,y,z)] - uArr[IX(x-1,y,z)] +
    //                 vArr[IX(x,y+1,z)] - vArr[IX(x,y-1,z)] +
    //                 wArr[IX(x,y,z+1)] - wArr[IX(x,y,z-1)]
    //             ) / (2 * gridSpacing);
    //             float finalDiv = divArr[IX(x,y,z)];


    //             //poisson stencil
    //             float stencil = (
    //                 -6 * pressureCache[IX(x,y,z)] +
    //                 (
    //                     pressureCache[IX(x+1,y,z)] +
    //                     pressureCache[IX(x-1,y,z)] +
    //                     pressureCache[IX(x,y+1,z)] +
    //                     pressureCache[IX(x,y-1,z)] +
    //                     pressureCache[IX(x,y,z+1)] +
    //                     pressureCache[IX(x,y,z-1)]
    //                 )
    //             ) / gridSpacing;
    //             float residual = stencil - FLUID_PRESSURECELL_RESIDUAL_MULTIPLIER * finalDiv;
    //             // if(residual > 0){
    //             //     printf("%f \n", finalDiv);
    //             //     printf("%f \n", stencil);
    //             //     printf("%f \n", residual);
    //             // }

    //             //divergence caused by static outflow due to diffusion
    //             float outflowDiv = -FLUID_PRESSURECELL_DIFFUSION_CONSTANT * 6 * dt;

    //             //compute the new pressure value
    //             // newPressure = pressureCache[IX(x,y,z)] + residual;
    //             newPressure = FLUID_PRESSURECELL_DIV_PRESSURE_CONST * (newDiv + outflowDiv);
    //             // if(newPressure > 0){
    //             //     printf("%f  \n",newPressure);
    //             // }
    //             pressureTemp[IX(x,y,z)] = pressureCache[IX(x,y,z)] + newPressure;
    //             pressureTemp[IX(x,y,z)] = fmax(FLUID_PRESSURECELL_MIN_PRESSURE,fmin(FLUID_PRESSURECELL_MAX_PRESSURE,pressureTemp[IX(x,y,z)]));
    //         }
    //     }
    // }

    //setup multigrid
    for(z = 0; z < DIM; z++){
        for(y = 0; y < DIM; y++){
            for(x = 0; x < DIM; x++){
                phi0[IX(x,y,z)] = divCache[IX(x,y,z)] * FLUID_PRESSURECELL_DIVERGENCE_BACKDOWN_FACTOR;
                pressureTemp[IX(x,y,z)] = pressureCache[IX(x,y,z)] * FLUID_PRESSURECELL_PRESSURE_BACKDOWN_FACTOR;
                // pressureTemp[IX(x,y,z)] = 0;
                if(divCache[IX(x,y,z)] > DIVERGENCE_WARNING_VALUE){
                    printf("invalid divergence!\n");
                    printf("%f \n", divCache[IX(x,y,z)]);
                    printf("\n");
                }
            }
        }
    }
    // for(x = 0; x < DIM; x++){
    //     for(y = 0; y < DIM; y++){
    //         //pressure borders
    //         //essentially, if the pressure builds up next to an edge, we don't have to have a 0 pressure area right next to it on the edge itself
    //         //there are two values that should potentially be set to here
    //         //either, same pressure as voxel in normal direction if this edge is actually an edge
    //         //otherwise, set to the pressure of the neighboring chunk
    //         pressureTemp[IX(0,x,y)]     = pressureCache[IX(0,x,y)];
    //         pressureTemp[IX(DIM-1,x,y)] = pressureCache[IX(DIM-1,x,y)];
    //         pressureTemp[IX(x,0,y)]     = pressureCache[IX(x,0,y)];
    //         pressureTemp[IX(x,DIM-1,y)] = pressureCache[IX(x,DIM-1,y)];
    //         pressureTemp[IX(x,y,0)]     = pressureCache[IX(x,y,0)];
    //         pressureTemp[IX(x,y,DIM-1)] = pressureCache[IX(x,y,DIM-1)];
    //         // pressureTemp[IX(0,x,y)]     = border[IX(0,x,y)]     * FLUID_PRESSURECELL_BOUND_PRESSURE;
    //         // pressureTemp[IX(DIM-1,x,y)] = border[IX(DIM-1,x,y)] * FLUID_PRESSURECELL_BOUND_PRESSURE;
    //         // pressureTemp[IX(x,0,y)]     = border[IX(x,0,y)]     * FLUID_PRESSURECELL_BOUND_PRESSURE;
    //         // pressureTemp[IX(x,DIM-1,y)] = border[IX(x,DIM-1,y)] * FLUID_PRESSURECELL_BOUND_PRESSURE;
    //         // pressureTemp[IX(x,y,0)]     = border[IX(x,y,0)]     * FLUID_PRESSURECELL_BOUND_PRESSURE;
    //         // pressureTemp[IX(x,y,DIM-1)] = border[IX(x,y,DIM-1)] * FLUID_PRESSURECELL_BOUND_PRESSURE;

    //         //divergence borders
    //         phi0[IX(0,x,y)]     = divCache[IX(0,x,y)];
    //         phi0[IX(DIM-1,x,y)] = divCache[IX(DIM-1,x,y)];
    //         phi0[IX(x,0,y)]     = divCache[IX(x,0,y)];
    //         phi0[IX(x,DIM-1,y)] = divCache[IX(x,DIM-1,y)];
    //         phi0[IX(x,y,0)]     = divCache[IX(x,y,0)];
    //         phi0[IX(x,y,DIM-1)] = divCache[IX(x,y,DIM-1)];
    //     }
    // }
    float a = 1;
    float c = 6;
    chunk->projectionIterations = 0;
    chunk->projectionResidual = 1;
    while(chunk->projectionIterations < FLUID_PRESSURECELL_SOLVER_MULTIGRID_MAX_ITERATIONS && (chunk->projectionResidual > FLUID_PRESSURECELL_PROJECTION_CONVERGENCE_TOLERANCE || chunk->projectionResidual < -FLUID_PRESSURECELL_PROJECTION_CONVERGENCE_TOLERANCE)){
        chunk->projectionResidual = solver_multigrid_parallel_iterate(pressureTemp,phi0,a,c);

        // //clamp pressure
        // for(z = 1; z < DIM-1; z++){
        //     for(y = 1; y < DIM-1; y++){
        //         for(x = 1; x < DIM-1; x++){
        //             pressureTemp[IX(x,y,z)] = fmax(FLUID_PRESSURECELL_MIN_PRESSURE,fmin(FLUID_PRESSURECELL_MAX_PRESSURE,pressureTemp[IX(x,y,z)]));
        //         }
        //     }
        // }
        //essentially, if the pressure builds up next to an edge, we don't have to have a 0 pressure area right next to it on the edge itself
        //there are two values that should potentially be set to here
        //either, same pressure as voxel in normal direction if this edge is actually an edge
        //otherwise, set to the pressure of the neighboring chunk
        for(x = 0; x < DIM; x++){
            for(y = 0; y < DIM; y++){
                //pressure borders
                // pressureTemp[IX(0,x,y)] = 0;
                // pressureTemp[IX(DIM-1,x,y)] = 0;
                // pressureTemp[IX(x,0,y)] = 0;
                // pressureTemp[IX(x,DIM-1,y)] = 0;
                // pressureTemp[IX(x,y,0)] = 0;
                // pressureTemp[IX(x,y,DIM-1)] = 0;
                if(border[IX(0,x,y)] > 0){
                    pressureTemp[IX(0,x,y)]     = pressureTemp[IX(1,x,y)];
                }
                if(border[IX(DIM-1,x,y)] > 0){
                    pressureTemp[IX(DIM-1,x,y)] = pressureTemp[IX(DIM-2,x,y)];
                }
                if(border[IX(x,0,y)] > 0){
                    pressureTemp[IX(x,0,y)]     = pressureTemp[IX(x,1,y)];
                }
                if(border[IX(x,DIM-1,y)] > 0){
                    pressureTemp[IX(x,DIM-1,y)] = pressureTemp[IX(x,DIM-2,y)];
                }
                if(border[IX(x,y,0)] > 0){
                    pressureTemp[IX(x,y,0)]     = pressureTemp[IX(x,y,1)];
                }
                if(border[IX(x,y,DIM-1)] > 0){
                    pressureTemp[IX(x,y,DIM-1)] = pressureTemp[IX(x,y,DIM-2)];
                }
                // pressureTemp[IX(0,x,y)]     = border[IX(0,x,y)]     * FLUID_PRESSURECELL_BOUND_PRESSURE;
                // pressureTemp[IX(DIM-1,x,y)] = border[IX(DIM-1,x,y)] * FLUID_PRESSURECELL_BOUND_PRESSURE;
                // pressureTemp[IX(x,0,y)]     = border[IX(x,0,y)]     * FLUID_PRESSURECELL_BOUND_PRESSURE;
                // pressureTemp[IX(x,DIM-1,y)] = border[IX(x,DIM-1,y)] * FLUID_PRESSURECELL_BOUND_PRESSURE;
                // pressureTemp[IX(x,y,0)]     = border[IX(x,y,0)]     * FLUID_PRESSURECELL_BOUND_PRESSURE;
                // pressureTemp[IX(x,y,DIM-1)] = border[IX(x,y,DIM-1)] * FLUID_PRESSURECELL_BOUND_PRESSURE;
            }
        }
        chunk->projectionIterations++;
    }

    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM; x++){
                if(pressureTemp[IX(x,y,z)] > FLUID_PRESSURECELL_MAX_PRESSURE){
                    printf("Invalid pressure!\n");
                    printf("%f %f \n", phi0[IX(x-1,y,z)], phi0[IX(x+1,y,z)]);
                    printf("%f \n", pressureTemp[IX(x,y,z)]);
                    printf("\n");
                }
            }
        }
    }

    // double pressureMean = 0;
    // for(z = 1; z < DIM-1; z++){
    //     for(y = 1; y < DIM-1; y++){
    //         for(x = 1; x < DIM; x++){
    //             pressureMean += pressureTemp[IX(x,y,z)];
    //         }
    //     }
    // }
    // pressureMean = pressureMean / ((DIM-2)*(DIM-2)*(DIM-2));

    // if(pressureMean > 1 || pressureMean < -1){
    //     for(z = 1; z < DIM-1; z++){
    //         for(y = 1; y < DIM-1; y++){
    //             for(x = 1; x < DIM; x++){
    //                 pressureTemp[IX(x,y,z)] = pressureTemp[IX(x,y,z)] - pressureMean;
    //             }
    //         }
    //     }
    // }
    // for(z = 1; z < DIM-1; z++){
    //     for(y = 1; y < DIM-1; y++){
    //         for(x = 1; x < DIM-1; x++){
    //             //check for NaNs
    //             if(pressureTemp[IX(x,y,z)] != pressureTemp[IX(x,y,z)]){
    //                 pressureTemp[IX(x,y,z)] = 0;
    //             }
    //             // pressureTemp[IX(x,y,z)] = pressureTemp[IX(x,y,z)] / 10.0f;
    //             pressureTemp[IX(x,y,z)] = fmax(FLUID_PRESSURECELL_MIN_PRESSURE,fmin(FLUID_PRESSURECELL_MAX_PRESSURE,pressureTemp[IX(x,y,z)]));
    //         }
    //     }
    // }

    double pressureMax = 0;
    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM; x++){
                if(fabs(pressureTemp[IX(x,y,z)]) > pressureMax){
                    pressureMax = pressureTemp[IX(x,y,z)];
                }
            }
        }
    }
    if(pressureMax > FLUID_PRESSURECELL_MAX_PRESSURE){
        printf("pressureMax: %f  \n",pressureMax);
        for(z = 1; z < DIM-1; z++){
            for(y = 1; y < DIM-1; y++){
                for(x = 1; x < DIM; x++){
                    if(pressureTemp[IX(x,y,z)] > pressureMax){
                        pressureTemp[IX(x,y,z)] = pressureTemp[IX(x,y,z)] / pressureMax;
                    }
                }
            }
        }
    }

    //do NOT zero out pressure on edges
    //this will cause the fluid to advect into the walls
    // for(x = 0; x < DIM; x++){
    //     for(y = 0; y < DIM; y++){
    //         //pressure borders
    //         pressureTemp[IX(0,x,y)] = 0;
    //         pressureTemp[IX(DIM-1,x,y)] = 0;
    //         pressureTemp[IX(x,0,y)] = 0;
    //         pressureTemp[IX(x,DIM-1,y)] = 0;
    //         pressureTemp[IX(x,y,0)] = 0;
    //         pressureTemp[IX(x,y,DIM-1)] = 0;
    //     }
    // }
}

/**
 * Approximates the divergence for this chunk
*/
LIBRARY_API void pressurecell_approximate_divergence(Environment * environment, Chunk * chunk){
    int x, y, z;
    //values stored across frames
    float * uArr = chunk->uTempCache;
    float * vArr = chunk->vTempCache;
    float * wArr = chunk->wTempCache;
    float * divArr = chunk->divergenceCache[CENTER_LOC];
    float * presureCache = chunk->pressureCache[CENTER_LOC];
    //temporary caches
    float * pressureTemp = chunk->pressureTempCache;
    float du, dv, dw;
    float newDivergence;
    float dt = environment->consts.dt;
    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM-1; x++){

                //divergence caused by static outflow due to diffusion
                float outflowDiv = FLUID_PRESSURECELL_DIFFUSION_CONSTANT * 6 * dt;

                //compute divergence
                du = (uArr[IX(x+1,y,z)] - uArr[IX(x-1,y,z)]);
                dv = (vArr[IX(x,y+1,z)] - vArr[IX(x,y-1,z)]);
                dw = (wArr[IX(x,y,z+1)] - wArr[IX(x,y,z-1)]);
                // if(x == 1){
                //     du = 0;
                // } else if(x == DIM-2){
                //     du = 0;
                // }
                // if(y == 1){
                //     dv = 0;
                // } else if(y == DIM-2){
                //     dv = 0;
                // }
                // if(z == 1){
                //     dw = 0;
                // } else if(z == DIM-2){
                //     dw = 0;
                // }
                newDivergence = (du+dv+dw) * (-0.5f * FLUID_PRESSURECELL_SPACING);
                // divArr[IX(x,y,z)] = divArr[IX(x,y,z)] + newDivergence - FLUID_PRESSURECELL_RESIDUAL_MULTIPLIER * divArr[IX(x,y,z)] + outflowDiv;
                if(newDivergence > DIVERGENCE_WARNING_VALUE || newDivergence < -DIVERGENCE_WARNING_VALUE){
                    printf("Invalid divergence! \n");
                    printf("%f \n",newDivergence);
                    printf("%f %f \n", uArr[IX(x+1,y,z)], uArr[IX(x-1,y,z)]);
                    printf("%f %f \n", vArr[IX(x,y+1,z)], vArr[IX(x,y-1,z)]);
                    printf("%f %f \n", wArr[IX(x,y,z+1)], wArr[IX(x,y,z-1)]);
                    printf("%f %f %f \n", du, dv, dw);
                    printf("\n");
                }
                newDivergence = fmax(-FLUID_PRESSURECELL_MAX_DIVERGENCE,fmin(FLUID_PRESSURECELL_MAX_DIVERGENCE,newDivergence));
                divArr[IX(x,y,z)] = newDivergence;
                
                //store pressure value from this frame
                presureCache[IX(x,y,z)] = pressureTemp[IX(x,y,z)];
            }
        }
    }
}

