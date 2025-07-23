#include <stdlib.h>


#include "fluid/queue/chunk.h"
#include "fluid/queue/sparse.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"



int solveOffset(int chunkPos);

/**
 * Creates a sparse chunk array
 */
LIBRARY_API SparseChunkArray * fluid_sparse_array_create(){

    //allocate the object itself
    SparseChunkArray * rVal = (SparseChunkArray *)calloc(1,sizeof(SparseChunkArray));
    if(rVal == NULL){
        return NULL;
    }

    //allocate the sub-arrays
    rVal->d = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));
    rVal->d0 = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));
    rVal->u = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));
    rVal->v = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));
    rVal->w = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));
    rVal->u0 = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));
    rVal->v0 = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));
    rVal->w0 = (float *)calloc(1,SPARSE_ARRAY_FULL_SIZE * sizeof(float));

    //allocate the chunk-tracking array
    rVal->chunks = (Chunk **)calloc(1,sizeof(Chunk *) * SPARSE_ARRAY_TOTAL_CHUNKS);

    if(rVal->d == NULL){
        free(rVal->w0);
        free(rVal->v0);
        free(rVal->u0);
        free(rVal->w);
        free(rVal->v);
        free(rVal->u);
        free(rVal->d0);
        free(rVal->d);
        free(rVal);
        return NULL;
    }

    return rVal;
}

/**
 * Frees a sparse chunk array
 */
LIBRARY_API void fluid_sparse_array_free(SparseChunkArray * array){

    //free the constituent arrays
    free(array->d);
    free(array->d0);
    free(array->u);
    free(array->v);
    free(array->w);
    free(array->u0);
    free(array->v0);
    free(array->w0);

    //free the chunk-tracking structure
    free(array->chunks);

    //free the array itself
    free(array);
}


/**
 * Adds a chunk to the sparse array
 */
LIBRARY_API void fluid_sparse_array_add_chunk(SparseChunkArray * array, Chunk * chunk, int x, int y, int z){
    //solve chunk offsets
    int offsetX = solveOffset(x);
    int offsetY = solveOffset(y);
    int offsetZ = solveOffset(z);
    int minPos = SPARSE_ARRAY_BORDER_SIZE / 2;
    int i, j, k;

    if(array->chunks[GCI(x,y,z)] != NULL){
        return;
    }

    for(int m = 0; m < MAIN_ARRAY_DIM; m++){
        for(int n = 0; n < MAIN_ARRAY_DIM; n++){
            for(int o = 0; o < MAIN_ARRAY_DIM; o++){
                i = m + 1;
                j = n + 1;
                k = o + 1;
                array->d[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->d[CENTER_LOC][IX(i,j,k)];
                array->d0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->d0[CENTER_LOC][IX(i,j,k)];

                array->u[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->u[CENTER_LOC][IX(i,j,k)];
                array->v[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->v[CENTER_LOC][IX(i,j,k)];
                array->w[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->w[CENTER_LOC][IX(i,j,k)];

                array->u0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->d[CENTER_LOC][IX(i,j,k)];
                array->v0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->d[CENTER_LOC][IX(i,j,k)];
                array->w0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = chunk->d[CENTER_LOC][IX(i,j,k)];
            }
        }
    }

    //set the chunk pointer
    array->chunks[GCI(x,y,z)] = chunk;
}

/**
 * Adds a chunk to the sparse array
 */
LIBRARY_API void fluid_sparse_array_remove_chunk(SparseChunkArray * array, Chunk * chunk, int x, int y, int z){
    //solve chunk offsets
    int offsetX = solveOffset(x);
    int offsetY = solveOffset(y);
    int offsetZ = solveOffset(z);
    int minPos = (SPARSE_ARRAY_BORDER_SIZE / 2);
    int i, j, k;

    //we add +1 to the dimension to copy the neighbors into the chunk
    for(int m = 0; m < MAIN_ARRAY_DIM + 1; m++){
        for(int n = 0; n < MAIN_ARRAY_DIM + 1; n++){
            for(int o = 0; o < MAIN_ARRAY_DIM + 1; o++){
                i = m + 1;
                j = n + 1;
                k = o + 1;

                //zero density in the main array
                array->d[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = 0;
                array->d0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = 0;

                //zero velocity in the main array
                array->u[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = 0;
                array->v[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = 0;
                array->w[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )];

                //zero elocity deltas in the main array
                array->u0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = 0;
                array->v0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = 0;
                array->w0[GVI(
                    minPos + offsetX + m,
                    minPos + offsetY + n,
                    minPos + offsetZ + o
                )] = 0;
            }
        }
    }

    //set the chunk pointer
    array->chunks[GCI(x,y,z)] = NULL;
}

/**
 * Gets the index for a point in the chunk
 */
LIBRARY_API int fluid_sparse_array_get_index(SparseChunkArray * array, int x, int y, int z){
    return GVI(x,y,z);
}

/**
 * Cleans the sparse array
 */
LIBRARY_API void fluid_sparse_array_clean(SparseChunkArray * array){
    for(int x = 0; x < SPARSE_ARRAY_RAW_DIM; x++){
        for(int y = 0; y < SPARSE_ARRAY_RAW_DIM; y++){
            for(int z = 0; z < SPARSE_ARRAY_RAW_DIM; z++){
                array->d[GVI(x,y,z)] = 0;
                array->d0[GVI(x,y,z)] = 0;
                array->u[GVI(x,y,z)] = 0;
                array->v[GVI(x,y,z)] = 0;
                array->w[GVI(x,y,z)] = 0;
                array->u0[GVI(x,y,z)] = 0;
                array->v0[GVI(x,y,z)] = 0;
                array->w0[GVI(x,y,z)] = 0;
            }
        }
    }
    for(int x = 0; x < SPARSE_ARRAY_CHUNK_DIM; x++){
        for(int y = 0; y < SPARSE_ARRAY_CHUNK_DIM; y++){
            for(int z = 0; z < SPARSE_ARRAY_CHUNK_DIM; z++){
                array->chunks[GCI(x,y,z)] = NULL;
            }
        }
    }
}

/**
 * Gets the number of chunks in the sparse array
 */
LIBRARY_API int fluid_sparse_array_get_chunk_count(SparseChunkArray * array){
    int rVal = 0;
    for(int i = 0; i < SPARSE_ARRAY_TOTAL_CHUNKS; i++){
        if(array->chunks[i] != NULL){
            rVal++;
        }
    }
    return rVal;
}

/**
 * Gets the index of the chunk at a given position within the sparse array
 */
int GCI(int x, int y, int z){
    return x * SPARSE_ARRAY_CHUNK_DIM * SPARSE_ARRAY_CHUNK_DIM + y * SPARSE_ARRAY_CHUNK_DIM + z;
}

/**
 * Gets the index of the value at a given position within the sparse array
 */
int GVI(int x, int y, int z){
    return (x * SPARSE_ARRAY_RAW_DIM * SPARSE_ARRAY_RAW_DIM) + (y * SPARSE_ARRAY_RAW_DIM) + z;
}

/**
 * Solves the offset into the sparse chunk aray to iterate from for this chunk's position
 */
int solveOffset(int chunkPos){
    return (chunkPos * MAIN_ARRAY_DIM);
}
