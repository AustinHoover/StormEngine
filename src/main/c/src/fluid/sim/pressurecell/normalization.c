#include <math.h>

#include "fluid/sim/pressurecell/normalization.h"
#include "fluid/sim/pressurecell/solver_consts.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"


/**
 * Inverts the force applied to the border
 */
#define INVERT_BORDER_FORCE 0

/**
 * Zeroes the force applied to the border
 */
#define ZERO_BORDER_FORCE 1

/**
 * Calculates the expected density and pressure
 */
LIBRARY_API void fluid_pressurecell_calculate_expected_intake(Environment * env, Chunk * chunk){
    int x, y, z;
    double sum;
    for(x = 1; x < DIM-1; x++){
        for(y = 1; y < DIM-1; y++){
            for(z = 1; z < DIM-1; z++){
                sum = sum + chunk->d[CENTER_LOC][IX(x,y,z)] + chunk->d0[CENTER_LOC][IX(x,y,z)];
            }
        }
    }
    chunk->pressureCellData.densitySum = sum;
}


/**
 * Normalizes the chunk
 */
LIBRARY_API void fluid_pressurecell_normalize_chunk(Environment * env, Chunk * chunk){
    int x, y, z;
    //calculate ratio
    double sum;
    for(x = 1; x < DIM-1; x++){
        for(y = 1; y < DIM-1; y++){
            for(z = 1; z < DIM-1; z++){
                sum = sum + chunk->d[CENTER_LOC][IX(x,y,z)];
            }
        }
    }
    double expected = chunk->pressureCellData.densitySum;
    if(sum > 0){
        double normalizationRatio = expected / sum;
        chunk->pressureCellData.normalizationRatio = normalizationRatio;
    } else {
        if(expected > 0.001f){
            printf("We've managed to completely delete all density! (expected: %f) \n", expected);
            sum = 0;
            for(x = 1; x < DIM-1; x++){
                for(y = 1; y < DIM-1; y++){
                    for(z = 1; z < DIM-1; z++){
                        sum = sum + chunk->dTempCache[IX(x,y,z)];
                    }
                }
            }
            printf("dTempCache sum: %lf \n", sum);
        }
        chunk->pressureCellData.normalizationRatio = 1.0f;
    }
    //apply ratio
    double ratio = chunk->pressureCellData.normalizationRatio;
    for(x = 1; x < DIM-1; x++){
        for(y = 1; y < DIM-1; y++){
            for(z = 1; z < DIM-1; z++){
                chunk->d[CENTER_LOC][IX(x,y,z)] = chunk->d[CENTER_LOC][IX(x,y,z)] * ratio;
            }
        }
    }
    //metadata work
    env->state.existingDensity = env->state.existingDensity + expected;
}


/**
 * Recaptures density that has flowed into boundaries
 */
LIBRARY_API void fluid_pressurecell_recapture_density(Environment * env, Chunk * chunk){
    int x, y, z;
    float * dArr = chunk->d[CENTER_LOC];
    float * dTemp = chunk->dTempCache;
    float * uArr = chunk->uTempCache;
    float * vArr = chunk->vTempCache;
    float * wArr = chunk->wTempCache;
    float * pressure = chunk->pressureTempCache;
    int ghostIndex, adjacentIndex;
    float overdraw, estimatedLoss, invertedForce;
    int neighbor;

    //clear neighbor outgoing values
    for(int i = 0; i < 9; i++){
        chunk->pressureCellData.outgoingDensity[i] = 0;
        chunk->pressureCellData.outgoingPressure[i] = 0;
    }

    //clear dtemp
    float innerTotal = 0;
    for(x = 0; x < DIM; x++){
        for(y = 0; y < DIM; y++){
            for(z = 0; z < DIM; z++){
                dTemp[IX(x,y,z)] = fmax(MIN_FLUID_VALUE,dArr[IX(x,y,z)]);
                if(x > 0 && y > 0 && z > 0 && x < DIM-1 && y < DIM-1 && z < DIM-1){
                    innerTotal = innerTotal + dTemp[IX(x,y,z)];
                }
            }
        }
    }




    //check +x plane
    neighbor = CK(2,1,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            for(y = 1; y < DIM-1; y++){
                ghostIndex = IX(DIM-1,x,y);
                adjacentIndex = IX(DIM-2,x,y);
                if(uArr[adjacentIndex] > 0 && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                    invertedForce = 1.0f - fabs(uArr[adjacentIndex]);
                    if(INVERT_BORDER_FORCE){
                        uArr[adjacentIndex] = -uArr[adjacentIndex];
                    }
                    if(ZERO_BORDER_FORCE){
                        uArr[adjacentIndex] = 0;
                    }
                    if(invertedForce > MIN_FLUID_VALUE){
                        estimatedLoss = dArr[adjacentIndex] / invertedForce;
                        dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                        overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                        pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                        if(overdraw > 0){
                            chunk->pressureCellData.recaptureDensity += overdraw;
                            dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                        }
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     for(y = 2; y < DIM-2; y++){
        //         ghostIndex = IX(x,y,0);
        //         adjacentIndex = IX(x,y,1);
        //         if(dArr[ghostIndex] > 0){
        //             chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //         }
        //     }
        // }
    }

    //check -x plane
    neighbor = CK(0,1,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            for(y = 1; y < DIM-1; y++){
                ghostIndex = IX(0,x,y);
                adjacentIndex = IX(1,x,y);
                if(uArr[adjacentIndex] < 0 && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                    invertedForce = 1.0f - fabs(uArr[adjacentIndex]);
                    if(INVERT_BORDER_FORCE){
                        uArr[adjacentIndex] = -uArr[adjacentIndex];
                    }
                    if(ZERO_BORDER_FORCE){
                        uArr[adjacentIndex] = 0;
                    }
                    if(invertedForce > MIN_FLUID_VALUE){
                        estimatedLoss = dArr[adjacentIndex] / invertedForce;
                        dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                        overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                        pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                        if(overdraw > 0){
                            chunk->pressureCellData.recaptureDensity += overdraw;
                            dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                        }
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     for(y = 2; y < DIM-2; y++){
        //         ghostIndex = IX(0,x,y);
        //         adjacentIndex = IX(1,x,y);
        //         if(dArr[ghostIndex] > 0){
        //             chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //         }
        //     }
        // }
    }

    //check +y plane
    neighbor = CK(1,2,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            for(y = 1; y < DIM-1; y++){
                ghostIndex = IX(x,DIM-1,y);
                adjacentIndex = IX(x,DIM-2,y);
                if(vArr[adjacentIndex] > 0 && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                    invertedForce = 1.0f - fabs(vArr[adjacentIndex]);
                    if(INVERT_BORDER_FORCE){
                        vArr[adjacentIndex] = -vArr[adjacentIndex];
                    }
                    if(ZERO_BORDER_FORCE){
                        vArr[adjacentIndex] = 0;
                    }
                    if(invertedForce > MIN_FLUID_VALUE){
                        estimatedLoss = dArr[adjacentIndex] / invertedForce;
                        dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                        overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                        pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                        if(overdraw > 0){
                            chunk->pressureCellData.recaptureDensity += overdraw;
                            dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                        }
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     for(y = 2; y < DIM-2; y++){
        //         ghostIndex = IX(x,DIM-1,y);
        //         adjacentIndex = IX(x,DIM-2,y);
        //         if(dArr[ghostIndex] > 0){
        //             chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //         }
        //     }
        // }
    }

    //check -y plane
    neighbor = CK(1,0,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            for(y = 1; y < DIM-1; y++){
                ghostIndex = IX(x,0,y);
                adjacentIndex = IX(x,1,y);
                if(vArr[adjacentIndex] < 0 && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                    invertedForce = 1.0f - fabs(vArr[adjacentIndex]);
                    if(INVERT_BORDER_FORCE){
                        vArr[adjacentIndex] = -vArr[adjacentIndex];
                    }
                    if(ZERO_BORDER_FORCE){
                        vArr[adjacentIndex] = 0;
                    }
                    if(invertedForce > MIN_FLUID_VALUE){
                        estimatedLoss = dArr[adjacentIndex] / invertedForce;
                        dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                        overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                        pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                        if(overdraw > 0){
                            chunk->pressureCellData.recaptureDensity += overdraw;
                            dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                        }
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     for(y = 2; y < DIM-2; y++){
        //         ghostIndex = IX(x,0,y);
        //         adjacentIndex = IX(x,1,y);
        //         if(dArr[ghostIndex] > 0){
        //             chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //         }
        //     }
        // }
    }

    //check +z plane
    neighbor = CK(1,1,2);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            for(y = 1; y < DIM-1; y++){
                ghostIndex = IX(x,y,DIM-1);
                adjacentIndex = IX(x,y,DIM-2);
                if(wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                    invertedForce = 1.0f - fabs(wArr[adjacentIndex]);
                    if(INVERT_BORDER_FORCE){
                        wArr[adjacentIndex] = -wArr[adjacentIndex];
                    }
                    if(ZERO_BORDER_FORCE){
                        wArr[adjacentIndex] = 0;
                    }
                    if(invertedForce > MIN_FLUID_VALUE){
                        estimatedLoss = dArr[adjacentIndex] / invertedForce;
                        dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                        overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                        pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                        if(overdraw > 0){
                            chunk->pressureCellData.recaptureDensity += overdraw;
                            dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                        }
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     for(y = 2; y < DIM-2; y++){
        //         ghostIndex = IX(x,y,DIM-1);
        //         adjacentIndex = IX(x,y,DIM-2);
        //         if(dArr[ghostIndex] > 0){
        //             chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //         }
        //     }
        // }
    }

    //check -z plane
    neighbor = CK(1,1,0);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            for(y = 1; y < DIM-1; y++){
                ghostIndex = IX(x,y,0);
                adjacentIndex = IX(x,y,1);
                if(wArr[adjacentIndex] < 0 && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                    invertedForce = 1.0f - fabs(wArr[adjacentIndex]);
                    if(INVERT_BORDER_FORCE){
                        wArr[adjacentIndex] = -wArr[adjacentIndex];
                    }
                    if(ZERO_BORDER_FORCE){
                        wArr[adjacentIndex] = 0;
                    }
                    if(invertedForce > MIN_FLUID_VALUE){
                        estimatedLoss = dArr[adjacentIndex] / invertedForce;
                        dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                        overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                        pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                        if(overdraw > 0){
                            chunk->pressureCellData.recaptureDensity += overdraw;
                            dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                        }
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     for(y = 2; y < DIM-2; y++){
        //         ghostIndex = IX(x,y,0);
        //         adjacentIndex = IX(x,y,1);
        //         if(dArr[ghostIndex] > 0){
        //             chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //         }
        //     }
        // }
    }





















    //check -x,-y edge
    neighbor = CK(0,0,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(0,0,x);
            adjacentIndex = IX(1,1,x);
            if((uArr[adjacentIndex] < 0 && vArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    vArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(0,0,x);
        //     adjacentIndex = IX(1,1,x);
        //     if(uArr[adjacentIndex] < 0 && vArr[adjacentIndex] < 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check -x,+y edge
    neighbor = CK(0,2,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(0,DIM-1,x);
            adjacentIndex = IX(1,DIM-2,x);
            if((uArr[adjacentIndex] < 0 && vArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    vArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(0,DIM-1,x);
        //     adjacentIndex = IX(1,DIM-2,x);
        //     if(uArr[adjacentIndex] < 0 && vArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check +x,-y edge
    neighbor = CK(2,0,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(DIM-1,0,x);
            adjacentIndex = IX(DIM-2,1,x);
            if((uArr[adjacentIndex] > 0 && vArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    vArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(DIM-1,0,x);
        //     adjacentIndex = IX(DIM-2,1,x);
        //     if(uArr[adjacentIndex] > 0 && vArr[adjacentIndex] < 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check +x,+y edge
    neighbor = CK(2,2,1);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(DIM-1,DIM-1,x);
            adjacentIndex = IX(DIM-2,DIM-2,x);
            if((uArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    vArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(DIM-1,DIM-1,x);
        //     adjacentIndex = IX(DIM-2,DIM-2,x);
        //     if(uArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }







    //check -x,-z edge
    neighbor = CK(0,1,0);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(0,x,0);
            adjacentIndex = IX(1,x,1);
            if((uArr[adjacentIndex] < 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(0,x,0);
        //     adjacentIndex = IX(1,x,1);
        //     if(uArr[adjacentIndex] < 0 && wArr[adjacentIndex] < 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check -x,+z edge
    neighbor = CK(0,1,2);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(0,x,DIM-1);
            adjacentIndex = IX(1,x,DIM-2);
            if((uArr[adjacentIndex] < 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(0,x,DIM-1);
        //     adjacentIndex = IX(1,x,DIM-2);
        //     if(uArr[adjacentIndex] < 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check +x,-z edge
    neighbor = CK(2,1,0);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(DIM-1,x,0);
            adjacentIndex = IX(DIM-2,x,1);
            if((uArr[adjacentIndex] > 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(DIM-1,x,0);
        //     adjacentIndex = IX(DIM-2,x,1);
        //     if(uArr[adjacentIndex] > 0 && wArr[adjacentIndex] < 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check +x,+z edge
    neighbor = CK(2,1,2);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(DIM-1,x,DIM-1);
            adjacentIndex = IX(DIM-2,x,DIM-2);
            if((uArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    uArr[adjacentIndex] = -uArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    uArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(DIM-1,x,DIM-1);
        //     adjacentIndex = IX(DIM-2,x,DIM-2);
        //     if(uArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }





    //check -y,-z edge
    neighbor = CK(1,0,0);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(x,0,0);
            adjacentIndex = IX(x,1,1);
            if((vArr[adjacentIndex] < 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    vArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(x,0,0);
        //     adjacentIndex = IX(x,1,1);
        //     if(vArr[adjacentIndex] < 0 && wArr[adjacentIndex] < 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check -y,+z edge
    neighbor = CK(1,0,2);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(x,0,DIM-1);
            adjacentIndex = IX(x,1,DIM-2);
            if((vArr[adjacentIndex] < 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    vArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(x,0,DIM-1);
        //     adjacentIndex = IX(x,1,DIM-2);
        //     if(vArr[adjacentIndex] < 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check +y,-z edge
    neighbor = CK(1,2,0);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(x,DIM-1,0);
            adjacentIndex = IX(x,DIM-2,1);
            if((vArr[adjacentIndex] > 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    vArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(x,DIM-1,0);
        //     adjacentIndex = IX(x,DIM-2,1);
        //     if(vArr[adjacentIndex] > 0 && wArr[adjacentIndex] < 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }

    //check +y,+z edge
    neighbor = CK(1,2,2);
    if(chunk->d[neighbor] == NULL){
        for(x = 1; x < DIM-1; x++){
            ghostIndex = IX(x,DIM-1,DIM-1);
            adjacentIndex = IX(x,DIM-2,DIM-2);
            if((vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
                invertedForce = (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
                if(INVERT_BORDER_FORCE){
                    vArr[adjacentIndex] = -vArr[adjacentIndex];
                    wArr[adjacentIndex] = -wArr[adjacentIndex];
                }
                if(ZERO_BORDER_FORCE){
                    vArr[adjacentIndex] = 0;
                    wArr[adjacentIndex] = 0;
                }
                if(invertedForce > MIN_FLUID_VALUE){
                    estimatedLoss = dArr[adjacentIndex] / invertedForce;
                    dTemp[adjacentIndex] = dArr[adjacentIndex] - estimatedLoss;
                    overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                    pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                    if(overdraw > 0){
                        chunk->pressureCellData.recaptureDensity += overdraw;
                        dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                    }
                }
            }
        }
    } else {
        // for(x = 2; x < DIM-2; x++){
        //     ghostIndex = IX(x,DIM-1,DIM-1);
        //     adjacentIndex = IX(x,DIM-2,DIM-2);
        //     if(vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //         chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        //     }
        // }
    }



































    //check corner -x, -y, -z
    neighbor = CK(0,0,0);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(0,0,0);
        adjacentIndex = IX(1,1,1);
        if((uArr[adjacentIndex] < 0 && vArr[adjacentIndex] < 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(0,0,0);
        // adjacentIndex = IX(1,1,1);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }



    //check corner -x, +y, -z
    neighbor = CK(0,2,0);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(0,DIM-1,0);
        adjacentIndex = IX(1,DIM-2,1);
        if((uArr[adjacentIndex] < 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(0,DIM-1,0);
        // adjacentIndex = IX(1,DIM-2,1);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }


    //check corner +x, -y, -z
    neighbor = CK(2,0,0);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(DIM-1,0,0);
        adjacentIndex = IX(DIM-2,1,1);
        if((uArr[adjacentIndex] > 0 && vArr[adjacentIndex] < 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(DIM-1,0,0);
        // adjacentIndex = IX(DIM-2,1,1);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }



    //check corner +x, +y, -z
    neighbor = CK(2,2,0);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(DIM-1,DIM-1,0);
        adjacentIndex = IX(DIM-2,DIM-2,1);
        if((uArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] < 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(DIM-1,DIM-1,0);
        // adjacentIndex = IX(DIM-2,DIM-2,1);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }







    //check corner -x, -y, +z
    neighbor = CK(0,0,2);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(0,0,DIM-1);
        adjacentIndex = IX(1,1,DIM-2);
        if((uArr[adjacentIndex] < 0 && vArr[adjacentIndex] < 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(0,0,DIM-1);
        // adjacentIndex = IX(1,1,DIM-2);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }



    //check corner -x, +y, +z
    neighbor = CK(0,2,2);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(0,DIM-1,DIM-1);
        adjacentIndex = IX(1,DIM-2,DIM-2);
        if((uArr[adjacentIndex] < 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(0,DIM-1,DIM-1);
        // adjacentIndex = IX(1,DIM-2,DIM-2);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }


    //check corner +x, -y, +z
    neighbor = CK(2,0,2);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(DIM-1,0,DIM-1);
        adjacentIndex = IX(DIM-2,1,DIM-2);
        if((uArr[adjacentIndex] > 0 && vArr[adjacentIndex] < 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(DIM-1,0,DIM-1);
        // adjacentIndex = IX(DIM-2,1,DIM-2);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }



    //check corner +x, +y, +z
    neighbor = CK(2,2,2);
    if(chunk->d[neighbor] == NULL){
        ghostIndex = IX(DIM-1,DIM-1,DIM-1);
        adjacentIndex = IX(DIM-2,DIM-2,DIM-2);
        if((uArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0) && dArr[adjacentIndex] > MIN_FLUID_VALUE){
            invertedForce = (1.0f - fabs(uArr[adjacentIndex])) * (1.0f - fabs(vArr[adjacentIndex])) * (1.0f - fabs(wArr[adjacentIndex]));
            if(INVERT_BORDER_FORCE){
                uArr[adjacentIndex] = -uArr[adjacentIndex];
                vArr[adjacentIndex] = -vArr[adjacentIndex];
                wArr[adjacentIndex] = -wArr[adjacentIndex];
            }
            if(ZERO_BORDER_FORCE){
                uArr[adjacentIndex] = 0;
                vArr[adjacentIndex] = 0;
                wArr[adjacentIndex] = 0;
            }
            if(invertedForce > MIN_FLUID_VALUE){
                estimatedLoss = dArr[adjacentIndex] / invertedForce;
                dTemp[adjacentIndex] = dArr[adjacentIndex] + estimatedLoss;
                overdraw = dTemp[adjacentIndex] - MAX_FLUID_VALUE;
                pressure[adjacentIndex] = pressure[adjacentIndex] + FLUID_PRESSURECELL_RECAPTURE_PRESSURE;
                if(overdraw > 0){
                    chunk->pressureCellData.recaptureDensity += overdraw;
                    dTemp[adjacentIndex] = MAX_FLUID_VALUE;
                }
            }
        }
    } else {
        // ghostIndex = IX(DIM-1,DIM-1,DIM-1);
        // adjacentIndex = IX(DIM-2,DIM-2,DIM-2);
        // if(vArr[adjacentIndex] > 0 && vArr[adjacentIndex] > 0 && wArr[adjacentIndex] > 0 && dArr[adjacentIndex] > 0){
        //     chunk->pressureCellData.outgoingDensity[neighbor] += dArr[ghostIndex];
        // }
    }
    


    //add extra density from edges to inner part of chunk
    float innerRebalanceRatio = (innerTotal + chunk->pressureCellData.recaptureDensity) / innerTotal;
    for(x = 1; x < DIM-1; x++){
        for(y = 1; y < DIM-1; y++){
            for(z = 1; z < DIM-1; z++){
                dTemp[IX(x,y,z)] = dTemp[IX(x,y,z)] * innerRebalanceRatio;
            }
        }
    }




    //clear dtemp
    for(x = 0; x < DIM; x++){
        for(y = 0; y < DIM; y++){
            for(z = 0; z < DIM; z++){
                dArr[IX(x,y,z)] = fmax(0.0f,dTemp[IX(x,y,z)]);
            }
        }
    }






}

