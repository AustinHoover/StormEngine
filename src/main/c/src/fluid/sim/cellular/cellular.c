

#include "stb/stb_ds.h"
#include "fluid/sim/cellular/cellular.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"
#include "math/randutils.h"


#define FLUID_CELLULAR_DIFFUSE_RATE 0.001

#define FLUID_CELLULAR_KERNEL_SIZE 4
#define FLUID_CELLULAR_KERNEL_PERMUTATIONS 4

int fluid_cellular_kernel_x[FLUID_CELLULAR_KERNEL_PERMUTATIONS][FLUID_CELLULAR_KERNEL_SIZE] = {
    {-1,  0,  1,  0},
    { 0, -1,  0,  1},
    { 1,  0, -1,  0},
    { 0,  1,  0, -1},
};
int fluid_cellular_kernel_z[FLUID_CELLULAR_KERNEL_PERMUTATIONS][FLUID_CELLULAR_KERNEL_SIZE] = {
    { 0, -1,  0,  1},
    { 1,  0, -1,  0},
    { 0,  1,  0, -1},
    {-1,  0,  1,  0},
};

/**
 * Tracks whether the second-to-highest lateral transfer happened or not (for preventing desync)
 */
int fluid_cellular_lat_tracker[DIM][DIM];

/**
 * Simulates the cellular chunk queue
 * @param environment The environment storing the simulation queues
 */
LIBRARY_API void fluid_cellular_simulate(Environment * environment){

    Chunk ** chunks = environment->queue.cellularQueue;
    int chunkCount = stbds_arrlen(chunks);
    int worldX, worldY, worldZ;
    int permuteX, permuteY, permuteZ;
    int frame = environment->state.frame;
    int permuteRand;
    int adjacentXKernel;
    int adjacentZKernel;

    double oldSum = 0;
    double newSum = 0;

    for(int cellIndex = 0; cellIndex < chunkCount; cellIndex++){
        Chunk * currentChunk = chunks[cellIndex];
        worldX = currentChunk->x;
        worldY = currentChunk->y;
        worldZ = currentChunk->z;

        //simulate here
        float *d  = currentChunk->d[CENTER_LOC];
        float * bounds = currentChunk->bounds[CENTER_LOC];
        float density;
        int transferred = 0;

        oldSum = 0;
        newSum = 0;

        // printf("%f %f %f %d %d %d\n",bounds[IX(0,1,1)],bounds[IX(1,0,1)],bounds[IX(1,1,0)],currentChunk->x,currentChunk->y,currentChunk->z);
        for(int y = 0; y < DIM; y++){
            if(y == 0){
                permuteY = DIM-2 + (CHUNK_SPACING * (worldY - 1));

            } else if(y == DIM-1){
                permuteY = 1 + (CHUNK_SPACING * (worldY + 1));

            } else {
                permuteY = y + (CHUNK_SPACING * worldY);
            }
            // int shift = randutils_map(randutils_rand2(environment->state.frame,permuteY),0,FLUID_CELLULAR_KERNEL_PERMUTATIONS - 1);
            int shift = frame % FLUID_CELLULAR_KERNEL_PERMUTATIONS;
            // int permutation = randutils_map(randutils_rand2(environment->state.frame,y + 1),0,FLUID_CELLULAR_KERNEL_PERMUTATIONS - 1);
            for(int x = 0; x < DIM; x++){
                for(int z = 0; z < DIM; z++){
                    oldSum = oldSum + d[IX(x,y,z)];
                    if(bounds[IX(x,y,z)] > BOUND_CUTOFF_VALUE){
                        continue;
                    }
                    if(d[IX(x,y,z)] <= MIN_FLUID_VALUE){
                        continue;
                    } else {

                        // [0 0 0]<1,1,17> -> 1,17
                        // [0 0 1]<1,1, 1> -> 1,17
                        // [0 0 0]<1,1,15> -> 1,15
                        // [0 0 0]<1,1,16> -> 1,16
                        // [0 0 1]<1,1, 0> -> 1,16
                        //calculate permutation based on the location of the cell
                        if(x == 0){
                            permuteX = DIM-2 + (CHUNK_SPACING * (worldX - 1));

                        } else if(x == DIM-1){
                            permuteX = 1 + (CHUNK_SPACING * (worldX + 1));

                        } else {
                            permuteX = x + (CHUNK_SPACING * worldX);
                        }
                        if(z == 0){
                            permuteZ = DIM-2 + (CHUNK_SPACING * (worldZ - 1));

                        } else if(z == DIM-1){
                            permuteZ = 1 + (CHUNK_SPACING * (worldZ + 1));
                            
                        } else {
                            permuteZ = z + (CHUNK_SPACING * worldZ);
                        }

                        //transfer straight down
                        if(y > 0){
                            if(permuteY % 16 == 16){
                                fluid_cellular_lat_tracker[x][z] = 0;
                            } else if(permuteY % 16 == 1 && fluid_cellular_lat_tracker[x][z] > 0){
                                continue;
                            }
                            float nBound = bounds[IX(x,y-1,z)];
                            if(nBound <= BOUND_CUTOFF_VALUE){
                                if(d[IX(x,y-1,z)] < MAX_FLUID_VALUE){
                                    float transfer = FLUID_CELLULAR_DIFFUSE_RATE_GRAV;
                                    if(d[IX(x,y,z)] < transfer){
                                        transfer = d[IX(x,y,z)];
                                    }
                                    if(FLUID_CELLULAR_DIFFUSE_RATE_GRAV < transfer){
                                        transfer = FLUID_CELLULAR_DIFFUSE_RATE_GRAV;
                                    }
                                    //lateral tracker
                                    if(permuteY % 16 == 0){
                                        fluid_cellular_lat_tracker[x][z] = 1;
                                    }
                                    // if(worldX == 0 && worldZ == 0 && x == 1 && z == 1){
                                    //     printf("vertical\n");
                                    //     printf("[%d %d %d] <%d,%d,%d> --> <%d,%d,%d>  \n",worldX,worldY,worldZ,x,y,z,x,y-1,z);
                                    //     printf("%f %d %d  -->  %d %d   \n",transfer,permuteX,permuteZ,permuteX,permuteZ);
                                    //     printf("%f    %f   \n",d[IX(x,y,z)],d[IX(x,y-1,z)]);
                                    // }
                                    // printf("vertical\n");
                                    // printf("[%d %d %d] <%d,%d,%d> --> <%d,%d,%d>     %f \n",worldX,worldY,worldZ,x,y,z,x,y-1,z,transfer);
                                    // printf("%d %d %d  -->  %d %d %d   \n",permuteX,y,permuteZ,permuteX,y-1,permuteZ);
                                    // printf("%f    %f   \n",d[IX(x,y,z)],d[IX(x,y-1,z)]);
                                    d[IX(x,y-1,z)] = d[IX(x,y-1,z)] + transfer;
                                    d[IX(x,y,z)] = d[IX(x,y,z)] - transfer;
                                    // printf("%f    %f   \n",d[IX(x,y,z)],d[IX(x,y-1,z)]);
                                    // printf("\n");
                                    continue;
                                }
                            }
                        }
                        //transfer laterally

                        if(d[IX(x,y,z)] < FLUID_CELLULAR_SURFACE_TENSION_CONST){
                            continue;
                        }

                        // float permutRand = randutils_rand3(permuteX,permuteZ,environment->state.frame);
                        int permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ,frame),0,4);
                        // int permutation = (permuteZ % (FLUID_CELLULAR_KERNEL_PERMUTATIONS / 2)) + (((permuteX % (FLUID_CELLULAR_KERNEL_PERMUTATIONS / 2))) * (FLUID_CELLULAR_KERNEL_PERMUTATIONS / 2));
                        int permutation = permuteRand;
                        int xKernel = fluid_cellular_kernel_x[shift][permutation];
                        int zKernel = fluid_cellular_kernel_z[shift][permutation];
                        int nX = x + xKernel;
                        int nZ = z + zKernel;
                        int realNeighborX = permuteX + xKernel;
                        int realNeighborZ = permuteZ + zKernel;
                        // if(permuteX == 2 && permuteZ == 3){
                        //     printf("[%d %d %d] <%d,%d,%d>\n",worldX,worldY,worldZ,x,y,z);
                        //     printf("%d %d  \n",permuteX,permuteZ);
                        //     printf("targeting %d %d  \n",xKernel,zKernel);
                        //     printf("\n");
                        // }
                        // if(realNeighborX == 2 && realNeighborZ == 3){
                        //     printf("[%d %d %d] <%d,%d,%d>\n",worldX,worldY,worldZ,x,y,z);
                        //     printf("%d %d  \n",permuteX,permuteZ);
                        //     printf("targeting %d %d \n",realNeighborX,realNeighborZ);
                        //     printf("\n");
                        // }
                        // printf("[%d %d %d] <%d,%d,%d>\n",worldX,worldY,worldZ,x,y,z);
                        // printf("%d %d  \n",permuteX,permuteZ);
                        if(nX < 0 || nX >= DIM || nZ < 0 || nZ >= DIM){
                            continue;
                        }
                        // int skip = 0;
                        // for(int q = 0; q < 2; q++){
                        //     if(permuteX % 16 == q){
                        //         skip = 1;
                        //     }
                        // }
                        // if(skip == 1){
                        //     continue;
                        // }


                        // |O                         ||
                        //-++-------------------------++
                        //-++-------------------------++
                        // |O                         ||
                        // |O                         ||
                        // |O                         ||
                        // |O                         ||
                        // |O                         ||
                        // |O                         ||
                        // |O                         ||
                        // |O                         ||
                        // |O                         ||
                        //-++-------------------------++
                        //-++-------------------------++
                        // |O                         ||

                        //
                        //we are dealing with the OOOOOOOOO here
                        //
                        
                        // if(permuteX % 16 > 1 && permuteZ % 16 == 1){
                        //     int pass = 0;
                        //     if(zKernel == 1){
                        //         pass = 1;
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-2,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel != 1){
                        //             pass = 0;
                        //         }
                        //     }
                        //     if(zKernel == -1){
                        //         pass = 1;
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-2,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == 1){
                        //             pass = 0;
                        //         }
                        //         permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ-1,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentXKernel == -1){
                        //             pass = 0;
                        //         }
                        //         permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ-1,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentXKernel == 1){
                        //             pass = 0;
                        //         }
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentXKernel == -1){
                        //             pass = 0;
                        //         }
                        //         if(adjacentXKernel == 1){
                        //             pass = 0;
                        //         }
                        //         if(adjacentZKernel == -1){
                        //             pass = 0;
                        //         }
                        //         if(adjacentZKernel == 1){
                        //             pass = 0;
                        //         }
                        //     }
                        //     if(pass == 0){
                        //         continue;
                        //     }
                        // }
                        // if(permuteX % 16 > 1 && permuteZ % 16 == 0){
                        //     int pass = 0;
                        //     if(pass == 0){
                        //         continue;
                        //     }
                        // }
                        // if(x < 2 || z < 1 || x > DIM-3 || z > DIM-2){
                        //     continue;
                        // }
                        // if((z < 2 || z > DIM-3) && (xKernel != 0)){
                        //     continue;
                        // }
                        // if((x < 2 || x > DIM-3) && (zKernel != 0)){
                        //     continue;
                        // }
                        // if(zKernel != 0){
                        //     continue;
                        // }
                        //15<-(16)<-17
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteX % 16 == 0 && xKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == -1){
                                continue;
                            }
                        }
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteZ % 16 == 0 && zKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+1,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == -1){
                                continue;
                            }
                        }
                        //     15
                        //     V
                        //15<-(16)
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteX % 16 == 0 && xKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == 1){
                                continue;
                            }
                        }
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteZ % 16 == 0 && zKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == 1){
                                continue;
                            }
                        }


                        //16<-(17)<-18
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteX % 16 == 1 && xKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == -1){
                                continue;
                            }
                        }
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteZ % 16 == 1 && zKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+1,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == -1){
                                continue;
                            }
                        }
                        

                        //
                        //     15,15   16,15   17,15   18,15
                        //           +---------------+
                        //     15,16 | 16,16   17,16 | 18,16
                        //           |               |
                        //     15,17 | 16,17   17,17 | 18,17
                        //           +---------------+
                        //     15,18   16,18   17,18   18,18
                        //
                        //every chunk is guaranteed to have the data in the box for its corners

                        // if(permuteZ % 16 == 15){
                        //     if(xKernel != 0){
                        //         continue;
                        //     }
                        // }
                        // if(permuteZ % 16 == 0 && permuteX % 16 == 15){
                        //     // if(xKernel != 0){
                        //         continue;
                        //     // }
                        // }
                        // if(permuteZ % 16 == 0 && permuteX % 16 == 0){
                        //     // if(xKernel != 0){
                        //         continue;
                        //     // }
                        // }
                        // if(permuteZ % 16 == 0 && permuteX % 16 == 1){
                        //     // if(xKernel != 0){
                        //         continue;
                        //     // }
                        // }
                        // if(permuteZ % 16 == 0 && permuteX % 16 == 2){
                        //     // if(xKernel != 0){
                        //         continue;
                        //     // }
                        // }
                        
                        // if(permuteZ % 16 == 1){
                        //     if(xKernel != 0){
                        //         continue;
                        //     }
                        // }
                        // if(permuteZ % 16 == 1){
                        //     if(xKernel != 0){
                        //         continue;
                        //     }
                        // }

                        // if(permuteX % 16 == 15){
                        //     if(zKernel != 0){
                        //         continue;
                        //     }
                        // }
                        // if(permuteX % 16 == 0){
                        //     if(zKernel != 0){
                        //         continue;
                        //     }
                        // }
                        // if(permuteX % 16 == 1){
                        //     if(zKernel != 0){
                        //         continue;
                        //     }
                        // }
                        // if(permuteX % 16 == 1){
                        //     if(zKernel != 0){
                        //         continue;
                        //     }
                        // }

                        // //  17,15 
                        // //    ^
                        // // (17,16)<- 18,16 
                        // //
                        // //
                        // if(permuteX % 16 == 1 && permuteZ % 16 == 0){
                        //     if(zKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //  17,15 
                        // //    V
                        // // (17,16)-> 18,16 
                        // //
                        // //
                        // if(permuteX % 16 == 1 && permuteZ % 16 == 0){
                        //     if(xKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == 1){
                        //             continue;
                        //         }
                        //     }
                        // }

                        // //
                        // //
                        // // (17,17)<- 18,17 
                        // //    V
                        // //  17,18 
                        // if(permuteX % 16 == 1 && permuteZ % 16 == 1){
                        //     if(zKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //
                        // //
                        // // (17,17)-> 18,17 
                        // //    ^
                        // //  17,18 
                        // if(permuteX % 16 == 1 && permuteZ % 16 == 1){
                        //     if(xKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }

                        // //          16,15 
                        // //            ^
                        // // 15,16 ->(16,16)
                        // //
                        // //
                        // if(permuteX % 16 == 0 && permuteZ % 16 == 0){
                        //     if(zKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == 1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //          16,15 
                        // //            V
                        // // 15,16 <-(16,16)
                        // //
                        // //
                        // if(permuteX % 16 == 0 && permuteZ % 16 == 0){
                        //     if(xKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == 1){
                        //             continue;
                        //         }
                        //     }
                        // }


                        // //
                        // //
                        // // 15,17 ->(16,17)
                        // //            V
                        // //          17,18 
                        // if(permuteX % 16 == 0 && permuteZ % 16 == 1){
                        //     if(zKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == 1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //
                        // //
                        // // 15,17 <-(16,17)
                        // //            ^
                        // //          17,18 
                        // if(permuteX % 16 == 0 && permuteZ % 16 == 1){
                        //     if(xKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //
                        // //
                        // // 15,17 ->(16,17)
                        // //            ^
                        // //          17,18 
                        // if(permuteX % 16 == 0 && permuteZ % 16 == 1){
                        //     if(xKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }



                        // //
                        // //     15,15   16,15   17,15   18,15
                        // //           +---------------+
                        // //     15,16 | 16,16   17,16 | 18,16
                        // //           |               |
                        // //     15,17 | 16,17   17,17 | 18,17
                        // //           +---------------+
                        // //     15,18   16,18   17,18   18,18
                        // //
                        // //every chunk is guaranteed to have the data in the box for its corners

                        // //
                        // //inserting into square from top edge
                        // //

                        // //         (17,15)
                        // //            V
                        // // 16,16 -> 17,16 
                        // //
                        // //
                        // if(permuteX % 16 == 1 && permuteZ % 16 == 15){
                        //     if(zKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ+1,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == 1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // // (16,15)
                        // //    V
                        // //  16,16 <- 17,16 
                        // //
                        // //
                        // if(permuteX % 16 == 0 && permuteZ % 16 == 15){
                        //     if(zKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ+1,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }


                        // //
                        // //inserting into square from left edge
                        // //

                        // //
                        // //
                        // //(15,16)-> 16,16 
                        // //            V
                        // //          16,17 
                        // if(permuteX % 16 == 15 && permuteZ % 16 == 0){
                        //     if(xKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ+1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == -11){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //          16,16 
                        // //            ^
                        // //(15,17)-> 16,17 
                        // //
                        // //
                        // if(permuteX % 16 == 15 && permuteZ % 16 == 0){
                        //     if(xKernel == 1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ-1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }


                        // //
                        // //inserting into square from bottom edge
                        // //
                        
                        // //
                        // //
                        // // 16,17 <- 17,17 
                        // //            ^
                        // //         (17,18)
                        // if(permuteX % 16 == 1 && permuteZ % 16 == 2){
                        //     if(zKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ-1,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //
                        // //
                        // //  16,17 -> 17,17 
                        // //    ^
                        // // (16,18)
                        // if(permuteX % 16 == 0 && permuteZ % 16 == 2){
                        //     if(zKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ-1,frame),0,4);
                        //         adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                        //         if(adjacentXKernel == 1){
                        //             continue;
                        //         }
                        //     }
                        // }


                        // //
                        // //inserting into square from right edge
                        // //

                        // //
                        // //
                        // //  17,16 <-(18,16)
                        // //    ^
                        // //  17,17 
                        // if(permuteX % 16 == 2 && permuteZ % 16 == 0){
                        //     if(xKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ+1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == -1){
                        //             continue;
                        //         }
                        //     }
                        // }
                        // //  17,16 
                        // //    V
                        // //  17,17 <-(18,17)
                        // //
                        // //
                        // if(permuteX % 16 == 2 && permuteZ % 16 == 1){
                        //     if(xKernel == -1){
                        //         permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ-1,frame),0,4);
                        //         adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                        //         if(adjacentZKernel == 1){
                        //             continue;
                        //         }
                        //     }
                        // }





                        //(15)->16->17
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteX % 16 == 15 && xKernel == 1){
                            permuteRand = randutils_map(randutils_rand3(permuteX+1,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == 1){
                                continue;
                            }
                        }

                        // (15)
                        //   V
                        //  16
                        //   V
                        //  17
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteZ % 16 == 15 && zKernel == 1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+1,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == 1){
                                continue;
                            }
                        }

                        // 16<-17<-(18)
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteX % 16 == 2 && xKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == -1){
                                continue;
                            }
                        }


                        //  16 
                        //   ^
                        //  17
                        //   ^
                        // (18)
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteZ % 16 == 2 && zKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == -1){
                                continue;
                            }
                        }




                        //16->(17)->18
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteX % 16 == 1 && xKernel == 1){
                            permuteRand = randutils_map(randutils_rand3(permuteX-1,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == 1){
                                continue;
                            }
                        }


                        // (16)
                        //   V
                        //  17
                        //   V
                        //  18
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteZ % 16 == 1 && zKernel == 1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == 1){
                                continue;
                            }
                        }
                        





























                        //15->16<-(17)
                        if(permuteX % 16 == 1 && xKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX-2,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == 1){
                                continue;
                            }
                        }
                        if(permuteZ % 16 == 1 && zKernel == -1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-2,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == 1){
                                continue;
                            }
                        }
                        //(16)->17<-18
                        if(permuteX % 16 == 0 && xKernel == 1){
                            permuteRand = randutils_map(randutils_rand3(permuteX+2,permuteZ,frame),0,4);
                            adjacentXKernel = fluid_cellular_kernel_x[shift][permuteRand];
                            if(adjacentXKernel == -1){
                                continue;
                            }
                        }
                        if(permuteZ % 16 == 0 && zKernel == 1){
                            permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+2,frame),0,4);
                            adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == -1){
                                continue;
                            }
                        }
                        if(permuteZ % 16 == 0 && zKernel == 1){
                            int permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ+2,frame),0,4);
                            int adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == -1){
                                continue;
                            }
                        }
                        //basically if we're about to pull negative and the NEXT voxel is also pulling negative, don't
                        //this prevents density desync between chunks
                        if(permuteZ % 16 == 2 && zKernel == -1){
                            int permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,3);
                            int adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                            if(adjacentZKernel == -1){
                                continue;
                            }
                        }

                        if(bounds[IX(nX,y,nZ)] <= BOUND_CUTOFF_VALUE){
                            if(d[IX(nX,y,nZ)] <= MAX_FLUID_VALUE - (FLUID_CELLULAR_DIFFUSE_RATE2 * 2) && d[IX(nX,y,nZ)] < d[IX(x,y,z)]){
                                float transfer = FLUID_CELLULAR_DIFFUSE_RATE2;
                                if(d[IX(x,y,z)] - d[IX(nX,y,nZ)] < FLUID_CELLULAR_DIFFUSE_RATE2){
                                    transfer = (d[IX(x,y,z)] - d[IX(nX,y,nZ)]) / 2.0f;
                                }
                                // if(realNeighborZ == 17 && realNeighborX == 8){
                                //     printf("lateral\n");
                                //     printf("[%d %d %d] <%d,%d,%d> --> <%d,%d,%d>  \n",worldX,worldY,worldZ,x,y,z,nX,y,nZ);
                                //     printf("%f %d %d  -->  %d %d   \n",transfer,permuteX,permuteZ,permuteX + xKernel,permuteZ + zKernel);
                                //     printf("%d %d \n",xKernel,zKernel);
                                //     printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                                //     int permuteRand = randutils_map(randutils_rand3(permuteX,permuteZ-1,frame),0,3);
                                //     int adjacentZKernel = fluid_cellular_kernel_z[shift][permuteRand];
                                //     printf("%d %d   \n",permuteRand,adjacentZKernel);
                                //     printf("transfer!\n");
                                // }
                                // if(permuteX == 45){
                                //     printf("lateral\n");
                                //     printf("[%d %d %d] <%d,%d,%d> --> <%d,%d,%d>  \n",worldX,worldY,worldZ,x,y,z,nX,y,nZ);
                                //     printf("%f %d %d  -->  %d %d   \n",transfer,permuteX,permuteZ,permuteX + xKernel,permuteZ + zKernel);
                                //     printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                                // }
                                // if(worldX == 0 && worldZ == 0){
                                //     printf("lateral\n");
                                //     printf("[%d %d %d] <%d,%d,%d> --> <%d,%d,%d>  \n",worldX,worldY,worldZ,x,y,z,nX,y,nZ);
                                //     printf("%f %d %d  -->  %d %d   \n",transfer,permuteX,permuteZ,permuteX + xKernel,permuteZ + zKernel);
                                //     printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                                // }
                                // printf("lateral\n");
                                // printf("[%d %d %d] <%d,%d,%d> --> <%d,%d,%d>  \n",worldX,worldY,worldZ,x,y,z,nX,y,nZ);
                                // printf("%f %d %d  -->  %d %d   \n",transfer,permuteX,permuteZ,permuteX + xKernel,permuteZ + zKernel);
                                // printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                                d[IX(nX,y,nZ)] = d[IX(nX,y,nZ)] + transfer;
                                d[IX(x,y,z)] = d[IX(x,y,z)] - transfer;
                                // if(worldX == 0 && worldZ == 0){
                                //     printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                                //     printf("\n");
                                // }
                                // printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                                // printf("\n");
                                // if(permuteX == 45){
                                //     printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                                //     printf("\n");
                                // }
                            }
                        }

                        // if(worldX == 2 && worldZ == 2 && x == 15 && z == 9){
                        //     printf("lateral\n");
                        //     printf("[%d %d %d] <%d,%d,%d> --> <%d,%d,%d>  \n",worldX,worldY,worldZ,x,y,z,nX,y,nZ);
                        //     printf("%d %d  -->  %d %d   \n",permuteX,permuteZ,permuteX + xKernel,permuteZ + zKernel);
                        //     printf("%f    %f   \n",d[IX(x,y,z)],d[IX(nX,y,nZ)]);
                        // }
                    }
                }
            }
        }
        // for(int x = 0; x < DIM; x++){
        //     for(int y = 0; y < DIM; y++){
        //         for(int z = 0; z < DIM; z++){
        //             newSum = newSum + d[IX(x,y,z)];
        //         }
        //     }
        // }
        // if(newSum > 0){
        //     double newRatio = oldSum / newSum;
        //     printf("newRatio: %lf  =   %lf /  %lf \n",newRatio,oldSum,newSum);
        //     for(int x = 0; x < DIM; x++){
        //         for(int y = 0; y < DIM; y++){
        //             for(int z = 0; z < DIM; z++){
        //                 d[IX(x,y,z)] = (float)(d[IX(x,y,z)] * newRatio);
        //             }
        //         }
        //     }
        // }
    }

}



/**
 * Gets the x velocity of a given position
 * @param environment The environment storing the simulation queues
 * @param x The x coordinate
 * @param y The y coordinate
 * @param z The z coordinate
 * @preturn The flow direction of x
 */
LIBRARY_API int fluid_cellular_get_flow_x(Environment * environment, int x, int y, int z){
    int shift = environment->state.frame % FLUID_CELLULAR_KERNEL_PERMUTATIONS;
    int permuteRand = randutils_map(randutils_rand3(x,z,environment->state.frame),0,4);
    int permutation = permuteRand;
    return fluid_cellular_kernel_x[shift][permutation];
}

/**
 * Gets the z velocity of a given position
 * @param environment The environment storing the simulation queues
 * @param x The x coordinate
 * @param y The y coordinate
 * @param z The z coordinate
 * @preturn The flow direction of z
 */
LIBRARY_API int fluid_cellular_get_flow_z(Environment * environment, int x, int y, int z){
    int shift = environment->state.frame % FLUID_CELLULAR_KERNEL_PERMUTATIONS;
    int permuteRand = randutils_map(randutils_rand3(x,z,environment->state.frame),0,4);
    int permutation = permuteRand;
    return fluid_cellular_kernel_z[shift][permutation];
}