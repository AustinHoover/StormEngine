#include<math.h>

#include "fluid/sim/pressurecell/tracking.h"




/**
 * Updates the tracking data for this chunk
 */
LIBRARY_API void pressurecell_update_tracking(Environment * environment, Chunk * chunk){
    int x, y, z;
    double pressureSum = 0;
    double velocitySum = 0;
    float * u = chunk->u[CENTER_LOC];
    float * v = chunk->v[CENTER_LOC];
    float * w = chunk->w[CENTER_LOC];
    float * pressure = chunk->pressureCache[CENTER_LOC];
    for(x = 0; x < DIM; x++){
        for(y = 0; y < DIM; y++){
            for(z = 0; z < DIM; z++){
                float velocityMagnitude = sqrt(u[IX(x,y,z)] * u[IX(x,y,z)] + v[IX(x,y,z)] * v[IX(x,y,z)] + w[IX(x,y,z)] * w[IX(x,y,z)]);
                velocitySum = velocitySum + velocityMagnitude;
                pressureSum = pressureSum + pressure[IX(x,y,z)];
            }
        }
    }
    chunk->pressureCellData.pressureTotal = pressureSum;
    chunk->pressureCellData.velocityMagTotal = velocitySum;
}


/**
 * Updates the inflow/outflow for this chunk
 */
LIBRARY_API void pressurecell_update_flows(Environment * environment, Chunk * chunk){
    int x, y;
    for(x = 1; x < DIM-1; x++){
        for(y = 1; y < DIM-1; y++){
            
        }
    }
}



