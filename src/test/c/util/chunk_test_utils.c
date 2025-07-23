#include <stdlib.h>
#include <math.h>

#include "stb/stb_ds.h"
#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/grid2/velocity.h"
#include "fluid/sim/grid2/utilities.h"
#include "chunk_test_utils.h"



int chunk_test_utils_kernelx[27] = {
    0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1,
    2, 2, 2, 2, 2, 2, 2, 2, 2,
};

int chunk_test_utils_kernely[27] = {
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
};

int chunk_test_utils_kernelz[27] = {
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
};

/**
 * Creates a chunk at a world position
 */
Chunk * chunk_create_pos(int x, int y, int z){
    Chunk * chunk1 = chunk_create();
    for(int i = 0; i < 27; i++){
        chunk1->d[i] = NULL;
        chunk1->d0[i] = NULL;
        chunk1->u[i] = NULL;
        chunk1->v[i] = NULL;
        chunk1->w[i] = NULL;
        chunk1->u0[i] = NULL;
        chunk1->v0[i] = NULL;
        chunk1->w0[i] = NULL;
        chunk1->bounds[i] = NULL;
        chunk1->pressureCache[i] = NULL;
        chunk1->divergenceCache[i] = NULL;
    }
    chunk1->d[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->d0[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->u[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->v[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->w[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->u0[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->v0[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->w0[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->bounds[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->pressureCache[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->divergenceCache[CENTER_LOC] = (float *)calloc(1,DIM * DIM * DIM * sizeof(float));
    chunk1->x = x;
    chunk1->y = y;
    chunk1->z = z;
    return chunk1;
}

/**
 * Frees a chunk
 */
void chunk_free(Chunk * chunk){
    free(chunk->d[CENTER_LOC]);
    free(chunk->d0[CENTER_LOC]);
    free(chunk->u[CENTER_LOC]);
    free(chunk->v[CENTER_LOC]);
    free(chunk->w[CENTER_LOC]);
    free(chunk->u0[CENTER_LOC]);
    free(chunk->v0[CENTER_LOC]);
    free(chunk->w0[CENTER_LOC]);
    free(chunk->bounds[CENTER_LOC]);
    free(chunk->pressureCache[CENTER_LOC]);
    free(chunk->divergenceCache[CENTER_LOC]);
    free(chunk);
}

/**
 * Creates a chunk queue
 * @param size The size of the queue to create
 */
Chunk ** chunk_create_queue(int size){
    Chunk ** rVal = NULL;
    for(int i = 0; i < size; i++){
        Chunk * chunk = chunk_create_pos(i,i,i);
        stbds_arrput(rVal,chunk);
    }
    return rVal;
}

/**
 * Frees a chunk queue
 */
void chunk_free_queue(Chunk ** chunks){
    int num = arrlen(chunks);
    for(int i = 0; i < num; i++){
        chunk_free(chunks[i]);
    }
    arrfree(chunks);
}

/**
 * Fills a chunk with a value
 * @param chunk The chunk to fill
 * @param val The value to fill
 */
void chunk_fill(Chunk * chunk, float val){
    for(int i = 0; i < DIM*DIM*DIM; i++){
        chunk->d[CENTER_LOC][i] = val;
        chunk->d0[CENTER_LOC][i] = val;
        chunk->u[CENTER_LOC][i] = val;
        chunk->v[CENTER_LOC][i] = val;
        chunk->w[CENTER_LOC][i] = val;
        chunk->u0[CENTER_LOC][i] = val;
        chunk->v0[CENTER_LOC][i] = val;
        chunk->w0[CENTER_LOC][i] = val;
        chunk->bounds[CENTER_LOC][i] = val;
        chunk->pressureCache[CENTER_LOC][i] = val;
        chunk->divergenceCache[CENTER_LOC][i] = val;
    }
}

/**
 * Fills a chunk with a value
 * @param chunk The chunk to fill
 * @param val The value to fill
 */
void chunk_fill_real(float * arr, float val){
    for(int x = 1; x < DIM - 1; x++){
        for(int y = 1; y < DIM - 1; y++){
            for(int z = 1; z < DIM - 1; z++){
                arr[IX(x,y,z)] = val;
            }
        }
    }
}


/**
 * Used in chunk_link_neighbors
 */
void chunk_link_by_index(Chunk * chunk1, Chunk * chunk2, int x, int y, int z){
    chunk1->d[CK(x,y,z)] = chunk2->d[CENTER_LOC];
    chunk1->d0[CK(x,y,z)] = chunk2->d0[CENTER_LOC];
    chunk1->u[CK(x,y,z)] = chunk2->u[CENTER_LOC];
    chunk1->v[CK(x,y,z)] = chunk2->v[CENTER_LOC];
    chunk1->w[CK(x,y,z)] = chunk2->w[CENTER_LOC];
    chunk1->u0[CK(x,y,z)] = chunk2->u0[CENTER_LOC];
    chunk1->v0[CK(x,y,z)] = chunk2->v0[CENTER_LOC];
    chunk1->w0[CK(x,y,z)] = chunk2->w0[CENTER_LOC];
    chunk1->bounds[CK(x,y,z)] = chunk2->bounds[CENTER_LOC];
    chunk1->pressureCache[CK(x,y,z)] = chunk2->pressureCache[CENTER_LOC];
    chunk1->divergenceCache[CK(x,y,z)] = chunk2->divergenceCache[CENTER_LOC];
}

/**
 * Links all neighbors in a chunk queue
 */
void chunk_link_neighbors(Chunk ** chunks){
    int num = arrlen(chunks);
    for(int i = 0; i < num; i++){
        for(int j = 0; j < num; j++){
            if(i == j){
                continue;
            }
            Chunk * chunk1 = chunks[i];
            Chunk * chunk2 = chunks[j];

            //one coord
            if(chunk1->x - 1 == chunk2->x && chunk1->y == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,1,1);
            }
            if(chunk1->x == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,0,1);
            }
            if(chunk1->x == chunk2->x && chunk1->y == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,1,0);
            }

            if(chunk1->x + 1 == chunk2->x && chunk1->y == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,1,1);
            }
            if(chunk1->x == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,2,1);
            }
            if(chunk1->x == chunk2->x && chunk1->y == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,1,2);
            }

            //two coords
            if(chunk1->x - 1 == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,0,1);
            }
            if(chunk1->x - 1 == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,2,1);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,0,1);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,2,1);
            }

            if(chunk1->x - 1 == chunk2->x && chunk1->y == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,1,0);
            }
            if(chunk1->x - 1 == chunk2->x && chunk1->y == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,1,2);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,1,0);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,1,2);
            }

            if(chunk1->x == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,0,0);
            }
            if(chunk1->x == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,0,2);
            }
            if(chunk1->x == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,2,0);
            }
            if(chunk1->x == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,1,2,2);
            }

            //three coords
            if(chunk1->x - 1 == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,0,0);
            }
            if(chunk1->x - 1 == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,0,2);
            }
            if(chunk1->x - 1 == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,2,0);
            }
            if(chunk1->x - 1 == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,0,2,2);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,0,0);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y - 1 == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,0,2);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z - 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,2,0);
            }
            if(chunk1->x + 1 == chunk2->x && chunk1->y + 1 == chunk2->y && chunk1->z + 1 == chunk2->z){
                chunk_link_by_index(chunk1,chunk2,2,2,2);
            }
        }
    }
}

/**
 * Sums the density of a chunk
 */
float chunk_sum_density(Chunk * chunk){
    float sum = 0;
    for(int x = 1; x < DIM - 1; x++){
        for(int y = 1; y < DIM - 1; y++){
            for(int z = 1; z < DIM - 1; z++){
                sum = sum + chunk->d[CENTER_LOC][IX(x,y,z)];
            }
        }
    }
    return sum;
}

/**
 * Sums the density of a chunk including its border values
 */
float chunk_sum_density_with_borders(Chunk * chunk){
    float sum = 0;
    for(int x = 0; x < DIM; x++){
        for(int y = 0; y < DIM; y++){
            for(int z = 0; z < DIM; z++){
                sum = sum + chunk->d[CENTER_LOC][IX(x,y,z)];
            }
        }
    }
    return sum;
}

/**
 * Sums density all chunks in a queue
 */
float chunk_queue_sum_density(Chunk ** chunks){
    float sum = 0;
    int chunkCount = arrlen(chunks);
    for(int i = 0; i < chunkCount; i++){
        Chunk * current = chunks[i];
        sum = sum + chunk_sum_density(current);
    }
    return sum;
}

/**
 * Sums velocity in all chunks in a queue
 */
float chunk_queue_sum_velocity(Chunk ** chunks, int axis){
    float sum = 0;
    int chunkCount = arrlen(chunks);
    for(int i = 0; i < chunkCount; i++){
        Chunk * current = chunks[i];
        for(int x = 1; x < DIM - 1; x++){
            for(int y = 1; y < DIM - 1; y++){
                for(int z = 1; z < DIM - 1; z++){
                    if(axis == FLUID_GRID2_DIRECTION_U){
                        sum = sum + current->u[CENTER_LOC][IX(x,y,z)];
                    } else if(axis == FLUID_GRID2_DIRECTION_V){
                        sum = sum + current->v[CENTER_LOC][IX(x,y,z)];
                    } else if(axis == FLUID_GRID2_DIRECTION_W){
                        sum = sum + current->w[CENTER_LOC][IX(x,y,z)];
                    }
                }
            }
        }
    }
    return sum;
}

/**
 * Empty test launcher
 */
int util_chunk_test_utils(){
    return 0;
}


/**
 * Creates a grid of chunks with the specified dimensions
 * @param env The simulation environment
 * @param width The width of the grid in number of chunks
 * @param height The height of the grid in number of chunks
 * @param length The length of the grid in number of chunks
 * @return The list of chunks
 */
Chunk ** createChunkGrid(Environment * env, int width, int height, int length){
    Chunk ** rVal = NULL;
    for(int x = 0; x < width; x++){
        for(int y = 0; y < height; y++){
            for(int z = 0; z < length; z++){
                Chunk * chunk = chunk_create_pos(x,y,z);
                arrput(rVal,chunk);
                chunk_fill(chunk,0);
            }
        }
    }
    int numChunks = arrlen(rVal);
    //link neighbors
    chunk_link_neighbors(rVal);
    //set bounds
    fluid_solve_bounds(numChunks,rVal,env);
    return rVal;
}



/**
 * Creates a convection cell for testing advection
 */
void advection_setup_convection_cell(Chunk ** queue, int center){
    int chunkCount = arrlen(queue);
    int realX, realY, realZ;
    int worldX, worldY, worldZ;
    for(int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++){
        Chunk * chunk = queue[chunkIndex];
        worldX = chunk->x;
        worldY = chunk->y;
        worldZ = chunk->z;
        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    double angle = ((x - center),(y - center));
                    if(x == 0){
                        realX = DIM-2 + (CHUNK_SPACING * (worldX - 1));

                    } else if(x == DIM-1){
                        realX = 1 + (CHUNK_SPACING * (worldX + 1));

                    } else {
                        realX = x + (CHUNK_SPACING * worldX);
                    }
                    if(y == 0){
                        realY = DIM-2 + (CHUNK_SPACING * (worldY - 1));

                    } else if(y == DIM-1){
                        realY = 1 + (CHUNK_SPACING * (worldY + 1));

                    } else {
                        realY = y + (CHUNK_SPACING * worldY);
                    }
                    if(z == 0){
                        realZ = DIM-2 + (CHUNK_SPACING * (worldZ - 1));

                    } else if(z == DIM-1){
                        realZ = 1 + (CHUNK_SPACING * (worldZ + 1));
                        
                    } else {
                        realZ = z + (CHUNK_SPACING * worldZ);
                    }
                    chunk->u[CENTER_LOC][IX(x,y,z)] = (float)sin(angle + CHUNK_TEST_UTILS_SMALL_VALUE);
                    chunk->v[CENTER_LOC][IX(x,y,z)] = (float)cos(angle + CHUNK_TEST_UTILS_SMALL_VALUE);
                    chunk->w[CENTER_LOC][IX(x,y,z)] = 0;
                }
            }
        }
    }
}



