#include <math.h>

#include "fluid/queue/chunk.h"
#include "fluid/sim/pressurecell/velocity.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "math/ode/multigrid_parallel.h"


/**
 * Velocity magnitude at which we warn
 */
#define WARNING_VELOCITY_MAGNITUDE 1000

/**
 * Adds velocity from the delta buffer to this chunk
*/
LIBRARY_API void pressurecell_add_gravity(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * dArr = chunk->d[CENTER_LOC];
    float * vSourceArr = chunk->v0[CENTER_LOC];
    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM-1; x++){
                float gravForce = dArr[IX(x,y,z)] * environment->consts.gravity * environment->consts.dt;
                gravForce = fmax(fmin(1.0f, gravForce),-1.0f);
                vSourceArr[IX(x,y,z)] = vSourceArr[IX(x,y,z)] + gravForce;
            }
        }
    }
}


/**
 * Adds velocity from the delta buffer to this chunk
*/
LIBRARY_API void pressurecell_add_velocity(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * uArr = chunk->u[CENTER_LOC];
    float * vArr = chunk->v[CENTER_LOC];
    float * wArr = chunk->w[CENTER_LOC];
    float * uTemp = chunk->uTempCache;
    float * vTemp = chunk->vTempCache;
    float * wTemp = chunk->wTempCache;
    float * uSourceArr = chunk->u0[CENTER_LOC];
    float * vSourceArr = chunk->v0[CENTER_LOC];
    float * wSourceArr = chunk->w0[CENTER_LOC];
    for(z = 0; z < DIM; z++){
        for(y = 0; y < DIM; y++){
            for(x = 0; x < DIM; x++){
                uTemp[IX(x,y,z)] = uArr[IX(x,y,z)] + uSourceArr[IX(x,y,z)];
                vTemp[IX(x,y,z)] = vArr[IX(x,y,z)] + vSourceArr[IX(x,y,z)];
                wTemp[IX(x,y,z)] = wArr[IX(x,y,z)] + wSourceArr[IX(x,y,z)];
                if(vTemp[IX(x,y,z)] > WARNING_VELOCITY_MAGNITUDE){
                    printf("Invalid add velocity!\n");
                    printf("%f %f %f  \n",  vTemp[IX(x,y,z)], vArr[IX(x,y,z)], vSourceArr[IX(x,y,z)] );
                    printf("\n");
                    int a = 1;
                    int b = 0;
                    int c = a / b;
                }
            }
        }
    }
}

/**
 * Diffuses the velocity in this chunk
*/
LIBRARY_API void pressurecell_diffuse_velocity(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * dArr = chunk->d[CENTER_LOC];
    float * uArr = chunk->u0[CENTER_LOC];
    float * vArr = chunk->v0[CENTER_LOC];
    float * wArr = chunk->w0[CENTER_LOC];
    float * uTemp = chunk->uTempCache;
    float * vTemp = chunk->vTempCache;
    float * wTemp = chunk->wTempCache;
    float * bounds = chunk->bounds[CENTER_LOC];
    float a = FLUID_PRESSURECELL_DIFFUSION_CONSTANT * environment->consts.dt / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);
    float c = 1+6*a;
    // float residual = 1;
    // int iterations = 0;
    // while(iterations < FLUID_GRID2_SOLVER_MULTIGRID_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
    //     residual = solver_multigrid_parallel_iterate(uArr,uTemp,a,c);
    //     // fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY_PHI,x);
    //     for(x = 1; x < DIM-1; x++){
    //         for(y = 1; y < DIM-1; y++){
    //             //diffuse back into the grid
    //             uArr[IX(0,x,y)] =     0;
    //             uArr[IX(DIM-1,x,y)] = 0;
    //             uArr[IX(x,0,y)] =     0;
    //             uArr[IX(x,DIM-1,y)] = 0;
    //             uArr[IX(x,y,0)] =     0;
    //             uArr[IX(x,y,DIM-1)] = 0;
    //         }
    //     }
    //     iterations++;
    // }
    // while(iterations < FLUID_GRID2_SOLVER_MULTIGRID_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
    //     residual = solver_multigrid_parallel_iterate(vArr,vTemp,a,c);
    //     for(x = 1; x < DIM-1; x++){
    //         for(y = 1; y < DIM-1; y++){
    //             //diffuse back into the grid
    //             vArr[IX(0,x,y)] =     0;
    //             vArr[IX(DIM-1,x,y)] = 0;
    //             vArr[IX(x,0,y)] =     0;
    //             vArr[IX(x,DIM-1,y)] = 0;
    //             vArr[IX(x,y,0)] =     0;
    //             vArr[IX(x,y,DIM-1)] = 0;
    //         }
    //     }
    //     // fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY_PHI,x);
    //     iterations++;
    // }
    // while(iterations < FLUID_GRID2_SOLVER_MULTIGRID_MAX_ITERATIONS && (residual > FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE || residual < -FLUID_GRID2_SOLVER_MULTIGRID_TOLERANCE)){
    //     residual = solver_multigrid_parallel_iterate(wArr,wTemp,a,c);
    //     // fluid_grid2_set_bounds(environment,BOUND_SET_DENSITY_PHI,x);
    //     for(x = 1; x < DIM-1; x++){
    //         for(y = 1; y < DIM-1; y++){
    //             //diffuse back into the grid
    //             wArr[IX(0,x,y)] =     0;
    //             wArr[IX(DIM-1,x,y)] = 0;
    //             wArr[IX(x,0,y)] =     0;
    //             wArr[IX(x,DIM-1,y)] = 0;
    //             wArr[IX(x,y,0)] =     0;
    //             wArr[IX(x,y,DIM-1)] = 0;
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
                uArr[IX(x,y,z)] = uTemp[IX(x,y,z)] +
                (
                    uTemp[IX(x,y,z)] * -(
                        (1.0f - bounds[IX(x-1,y,z)]) +
                        (1.0f - bounds[IX(x+1,y,z)]) +
                        (1.0f - bounds[IX(x,y-1,z)]) +
                        (1.0f - bounds[IX(x,y+1,z)]) +
                        (1.0f - bounds[IX(x,y,z-1)]) +
                        (1.0f - bounds[IX(x,y,z+1)])
                    ) +
                    (
                        uTemp[IX(x-1,y,z)] * (1.0f - bounds[IX(x-1,y,z)]) +
                        uTemp[IX(x+1,y,z)] * (1.0f - bounds[IX(x+1,y,z)]) +
                        uTemp[IX(x,y-1,z)] * (1.0f - bounds[IX(x,y-1,z)]) +
                        uTemp[IX(x,y+1,z)] * (1.0f - bounds[IX(x,y+1,z)]) +
                        uTemp[IX(x,y,z-1)] * (1.0f - bounds[IX(x,y,z-1)]) +
                        uTemp[IX(x,y,z+1)] * (1.0f - bounds[IX(x,y,z+1)])
                    )
                ) * a
                ;
            }
        }
    }

    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM-1; x++){
                if(bounds[IX(x,y,z)] > 0){
                    continue;
                }
                vArr[IX(x,y,z)] = vTemp[IX(x,y,z)] +
                (
                    vTemp[IX(x,y,z)] * -(
                        (1.0f - bounds[IX(x-1,y,z)]) +
                        (1.0f - bounds[IX(x+1,y,z)]) +
                        (1.0f - bounds[IX(x,y-1,z)]) +
                        (1.0f - bounds[IX(x,y+1,z)]) +
                        (1.0f - bounds[IX(x,y,z-1)]) +
                        (1.0f - bounds[IX(x,y,z+1)])
                    ) +
                    (
                        vTemp[IX(x-1,y,z)] * (1.0f - bounds[IX(x-1,y,z)]) +
                        vTemp[IX(x+1,y,z)] * (1.0f - bounds[IX(x+1,y,z)]) +
                        vTemp[IX(x,y-1,z)] * (1.0f - bounds[IX(x,y-1,z)]) +
                        vTemp[IX(x,y+1,z)] * (1.0f - bounds[IX(x,y+1,z)]) +
                        vTemp[IX(x,y,z-1)] * (1.0f - bounds[IX(x,y,z-1)]) +
                        vTemp[IX(x,y,z+1)] * (1.0f - bounds[IX(x,y,z+1)])
                    )
                ) * a
                ;
                if(vArr[IX(x,y,z)] > WARNING_VELOCITY_MAGNITUDE){
                    printf("Invalid diffuse!\n");
                    printf("%f \n", vArr[IX(x,y,z)]);
                    printf("%f\n", vTemp[IX(x,y,z)]);
                    printf("%f  %f  \n", vTemp[IX(x-1,y,z)], vTemp[IX(x+1,y,z)] );
                    printf("%f  %f  \n", vTemp[IX(x,y-1,z)], vTemp[IX(x,y+1,z)] );
                    printf("%f  %f  \n", vTemp[IX(x,y,z-1)], vTemp[IX(x,y,z+1)] );
                    printf("%f   %f   \n", a, c);
                    printf("\n");
                    int r = 1;
                    int s = 0;
                    int t = r / s;
                }
            }
        }
    }

    for(z = 1; z < DIM-1; z++){
        for(y = 1; y < DIM-1; y++){
            for(x = 1; x < DIM-1; x++){
                if(bounds[IX(x,y,z)] > 0){
                    continue;
                }
                wArr[IX(x,y,z)] = wTemp[IX(x,y,z)] +
                (
                    wTemp[IX(x,y,z)] * -(
                        (1.0f - bounds[IX(x-1,y,z)]) +
                        (1.0f - bounds[IX(x+1,y,z)]) +
                        (1.0f - bounds[IX(x,y-1,z)]) +
                        (1.0f - bounds[IX(x,y+1,z)]) +
                        (1.0f - bounds[IX(x,y,z-1)]) +
                        (1.0f - bounds[IX(x,y,z+1)])
                    ) +
                    (
                    wTemp[IX(x-1,y,z)] * (1.0f - bounds[IX(x-1,y,z)]) +
                    wTemp[IX(x+1,y,z)] * (1.0f - bounds[IX(x+1,y,z)]) +
                    wTemp[IX(x,y-1,z)] * (1.0f - bounds[IX(x,y-1,z)]) +
                    wTemp[IX(x,y+1,z)] * (1.0f - bounds[IX(x,y+1,z)]) +
                    wTemp[IX(x,y,z-1)] * (1.0f - bounds[IX(x,y,z-1)]) +
                    wTemp[IX(x,y,z+1)] * (1.0f - bounds[IX(x,y,z+1)])
                    )
                ) * a
                ;
            }
        }
    }
}

/**
 * Advects the velocity of this chunk
*/
LIBRARY_API void pressurecell_advect_velocity(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * uArr = chunk->u0[CENTER_LOC];
    float * uTemp = chunk->uTempCache;
    float * vArr = chunk->v0[CENTER_LOC];
    float * vTemp = chunk->vTempCache;
    float * wArr = chunk->w0[CENTER_LOC];
    float * wTemp = chunk->wTempCache;
    int x0, x1, y0, y1, z0, z1;
    float xp, yp, zp;
    float s0, s1, t0, t1, u0, u1;
    float interpolatedU, interpolatedV, interpolatedW;
    float vecU, vecV, vecW;
    float magnitude, maxMagnitude;
    float interpConst = environment->consts.dt / (FLUID_PRESSURECELL_SPACING * FLUID_PRESSURECELL_SPACING);
    for(y = 1; y < DIM-1; y++){
        for(z = 1; z < DIM-1; z++){
            for(x = 1; x < DIM-1; x++){
                magnitude = sqrt(interpolatedU * interpolatedU + interpolatedV * interpolatedV + interpolatedW * interpolatedW);
                if(maxMagnitude < magnitude){
                    maxMagnitude = magnitude;
                }
            }
        }
    }
    magnitude = 0;
    for(y = 1; y < DIM-1; y++){
        for(z = 1; z < DIM-1; z++){
            for(x = 1; x < DIM-1; x++){
                //figure how far we're advecting
                vecU = uArr[IX(x,y,z)] * interpConst;
                vecV = vArr[IX(x,y,z)] * interpConst;
                vecW = wArr[IX(x,y,z)] * interpConst;
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


                //calculate the real (float) position we are at
                xp = x - vecU;
                yp = y - vecV;
                zp = z - vecW;

                //clamp to border
                x0 = xp;
                y0 = yp;
                z0 = zp;

                //make sure we're not grabbing from outside bounds
                if(x0 < 0){
                    x0 = 0;
                } else if(x0 > DIM-2){
                    x0 = DIM-2;
                }
                if(y0 < 0){
                    y0 = 0;
                } else if(y0 > DIM-2){
                    y0 = DIM-2;
                }
                if(z0 < 0){
                    z0 = 0;
                } else if(z0 > DIM-2){
                    z0 = DIM-2;
                }
                
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


                interpolatedU = 
                s0*(
                    t0*u0*uArr[IX(x0,y0,z0)]+
                    t1*u0*uArr[IX(x0,y1,z0)]+
                    t0*u1*uArr[IX(x0,y0,z1)]+
                    t1*u1*uArr[IX(x0,y1,z1)]
                )+
                s1*(
                    t0*u0*uArr[IX(x1,y0,z0)]+
                    t1*u0*uArr[IX(x1,y1,z0)]+
                    t0*u1*uArr[IX(x1,y0,z1)]+
                    t1*u1*uArr[IX(x1,y1,z1)]
                );
                interpolatedV = 
                s0*(
                    t0*u0*vArr[IX(x0,y0,z0)]+
                    t1*u0*vArr[IX(x0,y1,z0)]+
                    t0*u1*vArr[IX(x0,y0,z1)]+
                    t1*u1*vArr[IX(x0,y1,z1)]
                )+
                s1*(
                    t0*u0*vArr[IX(x1,y0,z0)]+
                    t1*u0*vArr[IX(x1,y1,z0)]+
                    t0*u1*vArr[IX(x1,y0,z1)]+
                    t1*u1*vArr[IX(x1,y1,z1)]
                );
                interpolatedW = 
                s0*(
                    t0*u0*wArr[IX(x0,y0,z0)]+
                    t1*u0*wArr[IX(x0,y1,z0)]+
                    t0*u1*wArr[IX(x0,y0,z1)]+
                    t1*u1*wArr[IX(x0,y1,z1)]
                )+
                s1*(
                    t0*u0*wArr[IX(x1,y0,z0)]+
                    t1*u0*wArr[IX(x1,y1,z0)]+
                    t0*u1*wArr[IX(x1,y0,z1)]+
                    t1*u1*wArr[IX(x1,y1,z1)]
                );

                magnitude = sqrt(interpolatedU * interpolatedU + interpolatedV * interpolatedV + interpolatedW * interpolatedW);

                if(
                    x0 < 0     || y0 < 0     || z0 < 0 ||
                    x0 > DIM-1 || y0 > DIM-1 || z0 > DIM-1 ||
                    x1 < 0     || y1 < 0     || z1 < 0 ||
                    x1 > DIM-1 || y1 > DIM-1 || z1 > DIM-1
                    // || magnitude > 1
                ){
                    printf("advect vel: out of bounds \n");
                    printf("%d %d %d\n", x, y, z);
                    printf("%d %d %d\n", x0, y0, z0);
                    printf("%d %d %d\n", x1, y1, z1);
                    printf("percentages: \n");
                    printf("%f %f %f\n", s0, t0, u0);
                    printf("%f %f %f\n", s1, t1, u1);
                    printf("%f %f %f\n", xp, yp, zp);
                    printf("interpolated:\n");
                    printf("%f %f %f\n", interpolatedU, interpolatedV, interpolatedW);
                    printf("%f %f %f\n", uArr[IX(x,y,z)], vArr[IX(x,y,z)], wArr[IX(x,y,z)]);
                    printf("%f %f %f\n", uTemp[IX(x,y,z)], vTemp[IX(x,y,z)], wTemp[IX(x,y,z)]);
                    printf("%f\n", environment->consts.dt);
                    printf("%f\n", magnitude);
                    printf("values:\n");
                    printf("%f  %f     %f  %f  \n",  vArr[IX(x0,y0,z0)],  vArr[IX(x0,y1,z0)],     vArr[IX(x1,y0,z0)],  vArr[IX(x1,y1,z0)]);
                    printf("%f  %f     %f  %f  \n",  vArr[IX(x0,y0,z1)],  vArr[IX(x0,y1,z1)],     vArr[IX(x1,y0,z1)],  vArr[IX(x1,y1,z1)]);
                    printf("\n");
                    fflush(stdout);
                }


                // magnitude = sqrt(interpolatedU * interpolatedU + interpolatedV * interpolatedV + interpolatedW * interpolatedW);

                // if(magnitude > 1){
                //     interpolatedU = interpolatedU / magnitude;
                //     interpolatedV = interpolatedV / magnitude;
                //     interpolatedW = interpolatedW / magnitude;
                // }

                if(magnitude > WARNING_VELOCITY_MAGNITUDE){
                    printf("advect invalid set: %f  %f  %f  \n",  uArr[IX(x,y,z)],  vArr[IX(x,y,z)],  wArr[IX(x,y,z)]);
                    int a = 1;
                    int b = 0;
                    int c = a / b;
                }

                // if(maxMagnitude > 1){
                //     interpolatedU = interpolatedU / maxMagnitude;
                //     interpolatedV = interpolatedV / maxMagnitude;
                //     interpolatedW = interpolatedW / maxMagnitude;
                //     printf("maxMagnitude: %f   \n", maxMagnitude);
                // }
                
                uTemp[IX(x,y,z)] = interpolatedU;
                vTemp[IX(x,y,z)] = interpolatedV;
                wTemp[IX(x,y,z)] = interpolatedW;
            }
        }
    }
}

/**
 * Interpolates between the advected velocity and the previous frame's velocity by the pressure divergence amount
*/
LIBRARY_API double pressurecell_project_velocity(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * pressureTemp = chunk->pressureTempCache;
    float * uArr = chunk->u[CENTER_LOC];
    float * vArr = chunk->v[CENTER_LOC];
    float * wArr = chunk->w[CENTER_LOC];
    // float * uTemp = chunk->uTempCache;
    // float * vTemp = chunk->vTempCache;
    // float * wTemp = chunk->wTempCache;
    //temporary caches
    float pressureDivergence;
    float magnitude = 0;
    float pressureDifferenceX, pressureDifferenceY, pressureDifferenceZ;
    double maxMagnitude = 0;
    //project
    for(y = 1; y < DIM-1; y++){
        for(z = 1; z < DIM-1; z++){
            for(x = 1; x < DIM-1; x++){
                /*

                lets say pressure is like:
                   1    0    0
                  x-1   x   x+1
                 
                The pressure gradient becomes
                -0.5

                this pressure gradient is subtracted from the existing x velocity
                so if the existing velocity was
                1

                it is now
                1 - (-0.5) = 1.5

                the higher pressure value has pushed the vector away from itself

                lets say pressure is like:
                 -300    500   1000
                  x-1     x     x+1

                the pressure gradient becomes
                -650

                so when we modify the velocity of this field, we will now get
                -649

                Obviously this is really high, so we account for this by normalizing the velocity
                if the vector was originally pushing up along y, now it is almost exclusively pushing along x

                 */
                pressureDifferenceX = (pressureTemp[IX(x+1,y,z)] - pressureTemp[IX(x-1,y,z)]) / (FLUID_PRESSURECELL_SPACING * 2.0f);
                pressureDifferenceY = (pressureTemp[IX(x,y+1,z)] - pressureTemp[IX(x,y-1,z)])  / (FLUID_PRESSURECELL_SPACING * 2.0f);
                pressureDifferenceZ = (pressureTemp[IX(x,y,z+1)] - pressureTemp[IX(x,y,z-1)]) / (FLUID_PRESSURECELL_SPACING * 2.0f);

                //check for NaNs
                if(pressureDifferenceX != pressureDifferenceX){
                    printf("NaN x pressure! %f \n",pressureDifferenceX);
                    pressureDifferenceX = 0;
                }
                if(pressureDifferenceY != pressureDifferenceY){
                    printf("NaN y pressure! %f \n",pressureDifferenceY);
                    pressureDifferenceY = 0;
                }
                if(pressureDifferenceZ != pressureDifferenceZ){
                    printf("NaN z pressure! %f \n",pressureDifferenceZ);
                    pressureDifferenceZ = 0;
                }
                
                //make sure the pressure gradient does not push the velocity into walls
                // if(x == 1 && pressureDifferenceX > 0){
                //     pressureDifferenceX = 0;
                // }
                // if(x == DIM-2 && pressureDifferenceX < 0){
                //     pressureDifferenceX = 0;
                // }
                // if(y == 1 && pressureDifferenceY > 0){
                //     pressureDifferenceY = 0;
                // }
                // if(y == DIM-2 && pressureDifferenceY < 0){
                //     pressureDifferenceY = 0;
                // }
                // if(z == 1 && pressureDifferenceZ > 0){
                //     pressureDifferenceZ = 0;
                // }
                // if(z == DIM-2 && pressureDifferenceZ < 0){
                //     pressureDifferenceZ = 0;
                // }

                float magnitude = sqrt(uArr[IX(x,y,z)] * uArr[IX(x,y,z)] + vArr[IX(x,y,z)] * vArr[IX(x,y,z)] + wArr[IX(x,y,z)] * wArr[IX(x,y,z)]);
                if(maxMagnitude < magnitude){
                    maxMagnitude = magnitude;
                }

                if(magnitude != magnitude || magnitude > WARNING_VELOCITY_MAGNITUDE){
                    printf("invalid magnitude! %f\n", magnitude);
                    printf("%f %f %f\n",  pressureDifferenceX, pressureDifferenceY, pressureDifferenceZ);
                    printf("%f %f %f\n",  uArr[IX(x,y,z)], vArr[IX(x,y,z)], wArr[IX(x,y,z)]);
                    printf("%f %f  \n",pressureTemp[IX(x+1,y,z)],pressureTemp[IX(x-1,y,z)]);
                    printf("\n");
                    int a = 1;
                    int b = 0;
                    int c = a / b;
                    uArr[IX(x,y,z)] = 0;
                    vArr[IX(x,y,z)] = 0;
                    wArr[IX(x,y,z)] = 0;
                }

                //project the pressure gradient onto the velocity field
                uArr[IX(x,y,z)] = uArr[IX(x,y,z)] - pressureDifferenceX;
                vArr[IX(x,y,z)] = vArr[IX(x,y,z)] - pressureDifferenceY;
                wArr[IX(x,y,z)] = wArr[IX(x,y,z)] - pressureDifferenceZ;

                //normalize if the projection has pushed us wayyy out of bounds
                //ie, large pressure differentials can create huge imbalances
                // if(magnitude > 1.0f){
                //     uArr[IX(x,y,z)] = uArr[IX(x,y,z)] / magnitude;
                //     vArr[IX(x,y,z)] = vArr[IX(x,y,z)] / magnitude;
                //     wArr[IX(x,y,z)] = wArr[IX(x,y,z)] / magnitude;
                // }

                // magnitude = sqrt(uTemp[IX(x,y,z)] * uTemp[IX(x,y,z)] + vTemp[IX(x,y,z)] * vTemp[IX(x,y,z)] + wTemp[IX(x,y,z)] * wTemp[IX(x,y,z)]);

                //map the new velocity field onto the old one
                // if(magnitude > 1.0f){
                //     uArr[IX(x,y,z)] = uTemp[IX(x,y,z)] / magnitude;
                //     vArr[IX(x,y,z)] = vTemp[IX(x,y,z)] / magnitude;
                //     wArr[IX(x,y,z)] = wTemp[IX(x,y,z)] / magnitude;
                // } else if(magnitude > 0.0f){
                //     uArr[IX(x,y,z)] = uTemp[IX(x,y,z)];
                //     vArr[IX(x,y,z)] = vTemp[IX(x,y,z)];
                //     wArr[IX(x,y,z)] = wTemp[IX(x,y,z)];
                // } else {
                //     uArr[IX(x,y,z)] = 0;
                //     vArr[IX(x,y,z)] = 0;
                //     wArr[IX(x,y,z)] = 0;
                // }

                // if(
                //     uArr[x,y,z] < -100.0f || uArr[x,y,z] > 100.0f ||
                //     vArr[x,y,z] < -100.0f || vArr[x,y,z] > 100.0f ||
                //     wArr[x,y,z] < -100.0f || wArr[x,y,z] > 100.0f
                //     || magnitude < -1000 || magnitude > 1000
                //     // pressureDivergence < -1000 || pressureDivergence > 1000
                // ){
                //     printf("pressure divergence thing is off!!\n");
                //     printf("%f  \n", magnitude);
                //     printf("%f  \n", pressureDivergence);
                //     printf("%f %f %f  \n", uArr[IX(x,y,z)], vArr[IX(x,y,z)], wArr[IX(x,y,z)]);
                //     printf("%f %f  \n", presureCache[IX(x+1,y,z)], presureCache[IX(x-1,y,z)]);
                //     printf("%f %f  \n", presureCache[IX(x,y+1,z)], presureCache[IX(x,y-1,z)]);
                //     printf("%f %f  \n", presureCache[IX(x,y,z+1)], presureCache[IX(x,y,z-1)]);
                //     printf("\n");
                // }
            }
        }
    }
    //normalize vector field
    if(FLUID_PRESSURECELL_ENABLE_VELOCITY_FIELD_NORMALIZAITON && maxMagnitude > 1){
        for(y = 1; y < DIM-1; y++){
            for(z = 1; z < DIM-1; z++){
                for(x = 1; x < DIM-1; x++){

                    //project the pressure gradient onto the velocity field
                    uArr[IX(x,y,z)] = uArr[IX(x,y,z)] / maxMagnitude;
                    vArr[IX(x,y,z)] = vArr[IX(x,y,z)] / maxMagnitude;
                    wArr[IX(x,y,z)] = wArr[IX(x,y,z)] / maxMagnitude;

                    //check for NaNs
                    if(uArr[IX(x,y,z)] != uArr[IX(x,y,z)]){
                        uArr[IX(x,y,z)] = 0;
                    }
                    if(vArr[IX(x,y,z)] != vArr[IX(x,y,z)]){
                        vArr[IX(x,y,z)] = 0;
                    }
                    if(wArr[IX(x,y,z)] != wArr[IX(x,y,z)]){
                        wArr[IX(x,y,z)] = 0;
                    }

                    if(
                        uArr[x,y,z] < -1.0f || uArr[x,y,z] > 1.0f ||
                        vArr[x,y,z] < -1.0f || vArr[x,y,z] > 1.0f ||
                        wArr[x,y,z] < -1.0f || wArr[x,y,z] > 1.0f
                        // || magnitude < -1000 || magnitude > 1000
                        // pressureDivergence < -1000 || pressureDivergence > 1000
                    ){
                        printf("pressure divergence thing is off!!\n");
                        printf("%f  \n", magnitude);
                        printf("%f  \n", pressureDivergence);
                        printf("%f %f %f  \n", uArr[IX(x,y,z)], vArr[IX(x,y,z)], wArr[IX(x,y,z)]);
                        printf("%f %f  \n", pressureTemp[IX(x+1,y,z)], pressureTemp[IX(x-1,y,z)]);
                        printf("%f %f  \n", pressureTemp[IX(x,y+1,z)], pressureTemp[IX(x,y-1,z)]);
                        printf("%f %f  \n", pressureTemp[IX(x,y,z+1)], pressureTemp[IX(x,y,z-1)]);
                        printf("\n");
                    }
                }
            }
        }
    }
    return maxMagnitude;
}

/**
 * Copy temp velocities to next frame
*/
LIBRARY_API void pressurecell_copy_for_next_frame(Environment * environment, Chunk * chunk){
    int x, y, z;
    float * uArr = chunk->u[CENTER_LOC];
    float * vArr = chunk->v[CENTER_LOC];
    float * wArr = chunk->w[CENTER_LOC];
    float * uTemp = chunk->uTempCache;
    float * vTemp = chunk->vTempCache;
    float * wTemp = chunk->wTempCache;
    for(y = 1; y < DIM-1; y++){
        for(z = 1; z < DIM-1; z++){
            for(x = 1; x < DIM-1; x++){
                //project the pressure gradient onto the velocity field
                uArr[IX(x,y,z)] = uTemp[IX(x,y,z)];
                vArr[IX(x,y,z)] = vTemp[IX(x,y,z)];
                wArr[IX(x,y,z)] = wTemp[IX(x,y,z)];
                // if(uArr[IX(x,y,z)] > 1000 || vArr[IX(x,y,z)] > 1000 || wArr[IX(x,y,z)] > 1000){
                //     printf("invalid set: %f  %f  %f   \n", uArr[IX(x,y,z)], vArr[IX(x,y,z)], wArr[IX(x,y,z)]);
                // }
            }
        }
    }
}

