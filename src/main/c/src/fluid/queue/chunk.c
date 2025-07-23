#include<stdlib.h>

#include "fluid/queue/chunk.h"


/**
 * Allocates a new chunk
 */
LIBRARY_API Chunk * chunk_create(){
    Chunk * rVal = (Chunk *)calloc(1,sizeof(Chunk));
    rVal->dTempCache = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    rVal->uTempCache = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    rVal->vTempCache = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    rVal->wTempCache = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));
    rVal->pressureTempCache = (float *)calloc(1,DIM*DIM*DIM*sizeof(float));

    /**
     * Should store 5 tables:
     * 1x1x1
     * 2x2x2
     * 4x4x4
     * 8x8x8
     * 16x16x16
     */
    // rVal->interestTree = (char **)calloc(1,sizeof(char *) * 6);
    // //allocating extra data for ghost cells even though we will not evaluate the ghost cells
    // rVal->interestTree[0] = (char *)calloc(3*3*3,sizeof(char));
    // rVal->interestTree[1] = (char *)calloc(4*4*4,sizeof(char));
    // rVal->interestTree[2] = (char *)calloc(6*6*6,sizeof(char));
    // rVal->interestTree[3] = (char *)calloc(10*10*10,sizeof(char));
    // rVal->interestTree[4] = (char *)calloc(18*18*18,sizeof(char));

    return rVal;
}
