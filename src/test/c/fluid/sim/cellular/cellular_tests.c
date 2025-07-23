#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "stb/stb_ds.h"

#include "fluid/queue/chunk.h"
#include "fluid/queue/sparse.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/env/utilities.h"
#include "fluid/queue/islandsolver.h"
#include "fluid/queue/boundsolver.h"
#include "fluid/dispatch/dispatcher.h"
#include "fluid/sim/simulator.h"
#include "fluid/sim/cellular/cellular.h"
#include "../../../util/test.h"
#include "../../../util/chunk_test_utils.h"



#define CELLULAR_TEST_PLACE_VAL (FLUID_CELLULAR_DIFFUSE_RATE2 + 0.13456f)

/**
 * Stricter threshold used for sparser chunks
 */
#define FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2 0.0001f

/**
 * Strictest threshold used for sparser chunks
 */
#define FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3 0.01f

int fluid_sim_cellular_cellular_tests_kernelx[27] = {
    0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1,
    2, 2, 2, 2, 2, 2, 2, 2, 2,
};

int fluid_sim_cellular_cellular_tests_kernely[27] = {
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
};

int fluid_sim_cellular_cellular_tests_kernelz[27] = {
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
};

Chunk * fluid_sim_cellular_test_get_chunk(Chunk ** queue, int x, int y, int z){
    return queue[x * 3 * 3 + y * 3 + z];
}

void fluid_sim_cellular_test_describe_chunks(Chunk ** queue){
    float chunkSum = 0;
    int compareX, compareZ;
    for(int i = 0; i < 3; i++){
        for(int j = 0; j < 3; j++){
            chunkSum = 0;
            for(int x = 1; x < DIM-1; x++){
                for(int z = 1; z < DIM-1; z++){
                    compareX = i;
                    compareZ = j;
                    float newVal = fluid_sim_cellular_test_get_chunk(queue,compareX,0,compareZ)->d[CENTER_LOC][IX(x,1,z)];
                    chunkSum = chunkSum + newVal;
                }
            }
            printf("[%d %d]   %f    \n",i,j,chunkSum);
        }
    }
}



void fluid_sim_cellular_test_diagnose_desync(Chunk ** queue){

    int realX, realZ, worldX, worldY, worldZ;

    int chunkCount = arrlen(queue);
    for(int i = 0; i < chunkCount; i++){
        Chunk * current = queue[i];
        float ** d = current->d;
        worldX = current->x;
        worldY = current->y;
        worldZ = current->z;
        if(worldX > 0 && worldY > 0 && worldZ > 0){
            float val0 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY + 0, worldZ + 0)->d[CENTER_LOC][IX(1,1,1)];
            float val1 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY + 0, worldZ + 0)->d[CENTER_LOC][IX(DIM-1,1,1)];
            float val2 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY - 1, worldZ + 0)->d[CENTER_LOC][IX(1,DIM-1,1)];
            float val3 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY + 0, worldZ - 1)->d[CENTER_LOC][IX(1,1,DIM-1)];
            float val4 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY - 1, worldZ - 1)->d[CENTER_LOC][IX(1,DIM-1,DIM-1)];
            float val5 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY + 0, worldZ - 1)->d[CENTER_LOC][IX(DIM-1,1,DIM-1)];
            float val6 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY - 1, worldZ + 0)->d[CENTER_LOC][IX(DIM-1,DIM-1,1)];
            float val7 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY - 1, worldZ - 1)->d[CENTER_LOC][IX(DIM-1,DIM-1,DIM-1)];
            if(
                val0 != val1 ||
                val0 != val2 ||
                val0 != val3 ||
                val0 != val4 ||
                val0 != val5 ||
                val0 != val6 ||
                val0 != val7
            ){
                printf("mismatch at [%d %d %d] <1,1,1>  \n",  worldX,worldY,worldZ);
            }
        }
        if(worldX > 0 && worldY < 2 && worldZ > 0){
            float val0 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY + 0, worldZ + 0)->d[CENTER_LOC][IX(     1,      DIM-1,      1)];
            float val1 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY + 0, worldZ + 0)->d[CENTER_LOC][IX( DIM-1,      DIM-1,      1)];
            float val2 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY + 1, worldZ + 0)->d[CENTER_LOC][IX(     1,          1,      1)];
            float val3 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY + 0, worldZ - 1)->d[CENTER_LOC][IX(     1,      DIM-1,  DIM-1)];
            float val4 = fluid_sim_cellular_test_get_chunk(queue,worldX + 0, worldY + 1, worldZ - 1)->d[CENTER_LOC][IX(     1,          1,  DIM-1)];
            float val5 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY + 0, worldZ - 1)->d[CENTER_LOC][IX( DIM-1,      DIM-1,  DIM-1)];
            float val6 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY + 1, worldZ + 0)->d[CENTER_LOC][IX( DIM-1,          1,      1)];
            float val7 = fluid_sim_cellular_test_get_chunk(queue,worldX - 1, worldY + 1, worldZ - 1)->d[CENTER_LOC][IX( DIM-1,          1,  DIM-1)];
            if(
                val0 != val1 ||
                val0 != val2 ||
                val0 != val3 ||
                val0 != val4 ||
                val0 != val5 ||
                val0 != val6 ||
                val0 != val7
            ){
                printf("mismatch at [%d %d %d] <1,1,1>  \n",  worldX,worldY,worldZ);
            }
        }

    }
}

void fluid_sim_cellular_test_print_slice(Chunk * chunk, int y){
    float * d = chunk->d[CENTER_LOC];
    for(int x = 0; x < DIM; x++){
        for(int z = 0; z < DIM; z++){
            printf("%.2f ", d[IX(x,y,z)]);
        }
        printf("\n");
    }
}

void fluid_sim_cellular_test_print_slice_big(Chunk ** queue, int y){
    for(int i = 0; i < 3; i++){
        for(int x = 1; x < DIM-1; x++){
            for(int j = 0; j < 3; j++){
                for(int z = 1; z < DIM-1; z++){
                    Chunk * chunk = queue[i * 3 * 3 + j];
                    float * d = chunk->d[CENTER_LOC];
                    printf("%.2f ", d[IX(x,y,z)]);
                }
            }
            printf("\n");
        }
    }
    // float * d = chunk->d[CENTER_LOC];
    // for(int x = 0; x < DIM; x++){
    //     for(int z = 0; z < DIM; z++){
    //         printf("%.2f ", d[IX(x,y,z)]);
    //     }
    //     printf("\n");
    // }
}

void fluid_sim_cellular_test_print_slice_big_vis(Chunk ** queue, int y){
    for(int z = 1; z < (DIM-2)*3+1; z++){
        printf(" %2d ",z);
    }
    printf("\n");
    for(int i = 0; i < 3; i++){
        for(int x = 1; x < DIM-1; x++){
            float sum = 0;
            for(int j = 0; j < 3; j++){
                for(int z = 1; z < DIM-1; z++){
                    Chunk * chunk = queue[i * 3 * 3 + j];
                    float * d = chunk->d[CENTER_LOC];
                    float val = d[IX(x,y,z)];
                    sum = sum + val;
                    if(val >= 0.95){
                        printf("  M ");
                    } else if(val < 0.05){
                        printf("  0 ");
                    } else {
                        printf("%0.1f ", d[IX(x,y,z)]);
                    }
                }
            }
            printf("  [%d]  %f  ",x,sum);
            printf("\n");
        }
    }
    // float * d = chunk->d[CENTER_LOC];
    // for(int x = 0; x < DIM; x++){
    //     for(int z = 0; z < DIM; z++){
    //         printf("%.2f ", d[IX(x,y,z)]);
    //     }
    //     printf("\n");
    // }
}

void fluid_sim_cellular_test_print_flow_big(Environment * environment, int y){
    printf("    ");
    for(int z = 1; z < (DIM-2)*3; z++){
        if(z % 16 == 1 && z > 1){
            printf("    ");
        }
        printf("%2d ",z);
    }
    printf("\n");
    for(int x = 1; x < (DIM-2)*3+1; x++){
        printf("%2d  ",x);
        for(int z = 1; z < (DIM-2)*3; z++){
            int flowX = fluid_cellular_get_flow_x(environment, x, y, z);
            int flowZ = fluid_cellular_get_flow_z(environment, x, y, z);
            if(z % 16 == 1 && z > 1){
                printf("    ");
            }
            if(flowX == -1){
                printf("^  ");
            } else if(flowX == 1){
                printf("V  ");
            } else if(flowZ == -1){
                printf("<  ");
            } else if(flowZ == 1){
                printf(">  ");
            } else {
                printf("   ");
            }
        }
        if(x % 16 == 0){
            printf("\n");
        }
        printf("\n");
    }
    // float * d = chunk->d[CENTER_LOC];
    // for(int x = 0; x < DIM; x++){
    //     for(int z = 0; z < DIM; z++){
    //         printf("%.2f ", d[IX(x,y,z)]);
    //     }
    //     printf("\n");
    // }
}



int fluid_sim_cellular_bounds_test1(){
    int rVal = 0;
    printf("fluid_sim_cellular_bounds_test1\n");

    Environment * env = fluid_environment_create();
    env->state.frame += 1;

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //set border of 0,0,0 to push a value into z
    queue[0]->d[CENTER_LOC][IX(1,1,DIM-2)] = CELLULAR_TEST_PLACE_VAL;

    //call bounds setter
    fluid_solve_bounds(chunkCount,queue,env);

    {
        float borderVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-2)];
        float transferedVal = queue[1]->d[CENTER_LOC][IX(1,1,0)];
        rVal += assertEqualsFloat(borderVal,CELLULAR_TEST_PLACE_VAL,"Border value was overwritten! -- %f %f \n");
        rVal += assertEqualsFloat(transferedVal,CELLULAR_TEST_PLACE_VAL,"Value was not transfered from border! -- %f %f \n");
    }

    //dispatch and simulate
    fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
    fluid_simulate(env);

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //assert that the density moved
    {
        float borderVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-2)];
        float orderBorderVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-1)];
        float transferedVal = queue[1]->d[CENTER_LOC][IX(1,1,0)];
        rVal += assertEqualsFloat(borderVal,CELLULAR_TEST_PLACE_VAL - FLUID_CELLULAR_DIFFUSE_RATE2,"Border value has not changed! -- %f %f \n");
        rVal += assertEqualsFloat(orderBorderVal,FLUID_CELLULAR_DIFFUSE_RATE2,"Border value has not moved! -- %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_bounds_test2(){
    int rVal = 0;
    printf("fluid_sim_cellular_bounds_test2\n");

    Environment * env = fluid_environment_create();
    env->state.frame += 1;

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //set border of 0,0,0 to push a value into z
    queue[1]->d[CENTER_LOC][IX(1,1,1)] = CELLULAR_TEST_PLACE_VAL;

    //call bounds setter
    fluid_solve_bounds(chunkCount,queue,env);

    {
        float borderVal = queue[1]->d[CENTER_LOC][IX(1,1,1)];
        float transferedVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-1)];
        rVal += assertEqualsFloat(borderVal,CELLULAR_TEST_PLACE_VAL,"Border value was overwritten! -- %f %f \n");
        rVal += assertEqualsFloat(transferedVal,CELLULAR_TEST_PLACE_VAL,"Value was not transfered from border! -- %f %f \n");
    }

    //dispatch and simulate
    fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
    fluid_simulate(env);

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //assert that the density moved
    {
        float borderOldVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-1)];
        float borderNewVal = queue[0]->d[CENTER_LOC][IX(1,1,DIM-2)];
        float transferedVal = queue[1]->d[CENTER_LOC][IX(1,1,0)];
        rVal += assertEqualsFloat(borderOldVal,CELLULAR_TEST_PLACE_VAL - FLUID_CELLULAR_DIFFUSE_RATE2,"Border old val has not changed! -- %f %f \n");
        rVal += assertEqualsFloat(borderNewVal,FLUID_CELLULAR_DIFFUSE_RATE2,"Border new val not occupied! -- %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test1(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test1\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //set border of 0,0,0 to push a value into z
    queue[1]->d[CENTER_LOC][IX(1,1,1)] = CELLULAR_TEST_PLACE_VAL;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);


    //dispatch and simulate
    int frameCount = 100;
    for(int frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        if(currentSum != originalSum){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    // float transferedValue = queue[0]->d[CENTER_LOC][IX(1,1,16)];
    // printf("transfered value: %f \n",transferedValue);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");

    printf("\n");
    return rVal;
}



int fluid_sim_cellular_stability_test2(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test2\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //set border of 0,0,0 to push a value into z
    queue[13]->d[CENTER_LOC][IX(5,5,5)] = CELLULAR_TEST_PLACE_VAL;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);


    //dispatch and simulate
    int frameCount = 100;
    for(int frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        if(currentSum != originalSum){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    // float transferedValue = queue[0]->d[CENTER_LOC][IX(1,1,16)];
    // printf("transfered value: %f \n",transferedValue);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");

    printf("\n");
    return rVal;
}



int fluid_sim_cellular_stability_test3(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test3\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    queue[13]->d[CENTER_LOC][IX(5,5,5)] = MAX_FLUID_VALUE;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test4(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test4\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    queue[13]->d[CENTER_LOC][IX(1,1,1)] = MAX_FLUID_VALUE;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d   ---   %f    \n",frameCounter,delta);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test5(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test5\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    queue[13]->d[CENTER_LOC][IX(1,1,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(DIM-2,1,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(1,DIM-2,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(1,1,DIM-2)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(1,DIM-2,DIM-2)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(DIM-2,DIM-2,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(DIM-2,DIM-2,DIM-2)] = MAX_FLUID_VALUE;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test6(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test6\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    queue[13]->d[CENTER_LOC][IX(1,1,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(2,1,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(1,2,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(1,1,2)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(1,2,2)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(2,1,2)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(2,2,1)] = MAX_FLUID_VALUE;
    queue[13]->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test7(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test7\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    int lowerBound = 1;
    int upperBound = 16;
    for(int x = lowerBound; x < upperBound+1; x++){
        for(int y = lowerBound; y < upperBound+1; y++){
            for(int z = lowerBound; z < upperBound+1; z++){
                queue[13]->d[CENTER_LOC][IX(x,y,z)] = MAX_FLUID_VALUE;
            }
        }
    }

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        float postSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - postSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(postSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync on frame %d \n",frameCounter);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test8(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test8\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    fluid_sim_cellular_test_get_chunk(queue,1,1,1)->d[CENTER_LOC][IX(1,1,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,1,1)->d[CENTER_LOC][IX(DIM-2,1,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,0,1)->d[CENTER_LOC][IX(1,DIM-2,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,1,0)->d[CENTER_LOC][IX(1,1,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,0,0)->d[CENTER_LOC][IX(1,DIM-2,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,1,0)->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,0,1)->d[CENTER_LOC][IX(DIM-2,DIM-2,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,0,0)->d[CENTER_LOC][IX(DIM-2,DIM-2,DIM-2)] = MAX_FLUID_VALUE;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}


int fluid_sim_cellular_stability_test9(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test9\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    fluid_sim_cellular_test_get_chunk(queue,1,2,1)->d[CENTER_LOC][IX(1,1,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,2,1)->d[CENTER_LOC][IX(DIM-2,1,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,1,1)->d[CENTER_LOC][IX(1,DIM-2,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,2,0)->d[CENTER_LOC][IX(1,1,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,1,0)->d[CENTER_LOC][IX(1,DIM-2,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,2,0)->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,1,1)->d[CENTER_LOC][IX(DIM-2,DIM-2,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,0,1,0)->d[CENTER_LOC][IX(DIM-2,DIM-2,DIM-2)] = MAX_FLUID_VALUE;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test10(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test10\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    fluid_sim_cellular_test_get_chunk(queue,2,2,2)->d[CENTER_LOC][IX(1,1,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,2,2)->d[CENTER_LOC][IX(DIM-2,1,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,2,1,2)->d[CENTER_LOC][IX(1,DIM-2,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,2,2,1)->d[CENTER_LOC][IX(1,1,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,2,1,1)->d[CENTER_LOC][IX(1,DIM-2,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,2,1)->d[CENTER_LOC][IX(DIM-2,1,DIM-2)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,1,2)->d[CENTER_LOC][IX(DIM-2,DIM-2,1)] = MAX_FLUID_VALUE;
    fluid_sim_cellular_test_get_chunk(queue,1,1,1)->d[CENTER_LOC][IX(DIM-2,DIM-2,DIM-2)] = MAX_FLUID_VALUE;

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD2){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            fluid_sim_cellular_test_diagnose_desync(queue);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD3){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test11(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test11\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the 10th chunk
    chunk_fill_real(queue[10]->d[CENTER_LOC],CELLULAR_TEST_PLACE_VAL);

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test12(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test12\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the bottom plane of chunks
    for(int x = 0; x < 3; x++){
        for(int z = 0; z < 3; z++){
            chunk_fill_real(queue[x * 3 * 3 + z]->d[CENTER_LOC],MAX_FLUID_VALUE);
        }
    }

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test13(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test13\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the bottom plane of chunks
    for(int x = 0; x < 3; x++){
        for(int z = 0; z < 3; z++){
            float ** d = queue[x * 3 * 3 + z]->d;
            for(int i = 1; i < DIM-1; i++){
                for(int j = 1; j < DIM-1; j++){
                    d[CENTER_LOC][IX(i,1,j)] = MAX_FLUID_VALUE;
                }
            }
        }
    }

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        // printf("\n");
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test14(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test14\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the bottom plane of chunks
    for(int x = 0; x < 3; x++){
        for(int z = 0; z < 3; z++){
            float ** d = queue[x * 3 * 3 + z]->d;
            for(int i = 1; i < DIM-2; i++){
                for(int j = 1; j < DIM-2; j++){
                    d[CENTER_LOC][IX(i,1,j)] = MAX_FLUID_VALUE;
                }
            }
        }
    }

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    float slice[16 * 3][16 * 3];
    int compareX = 2;
    int compareZ = 2;

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
            fluid_sim_cellular_test_print_slice_big_vis(queue,1);
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    // printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test15(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test15\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the bottom plane of chunks
    for(int x = 0; x < 3; x++){
        for(int z = 0; z < 3; z++){
            float ** d = queue[x * 3 * 3 + z]->d;
            for(int i = 1; i < DIM-2; i++){
                for(int j = 1; j < DIM-2; j++){
                    d[CENTER_LOC][IX(i,1,j)] = MAX_FLUID_VALUE;
                    d[CENTER_LOC][IX(i,2,j)] = MAX_FLUID_VALUE;
                }
            }
        }
    }

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    float slice[16 * 3][16 * 3];
    int compareX = 2;
    int compareZ = 2;

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
            printf("SLICE 1\n\n");
            fluid_sim_cellular_test_print_slice_big_vis(queue,1);
            printf("\n\n\n");
            printf("SLICE 2\n\n");
            fluid_sim_cellular_test_print_slice_big_vis(queue,2);
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    // printf("\n");
    return rVal;
}

int fluid_sim_cellular_stability_test16(){
    int rVal = 0;
    printf("fluid_sim_cellular_stability_test16\n");

    Environment * env = fluid_environment_create();

    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_cellular_cellular_tests_kernelx[i],
            fluid_sim_cellular_cellular_tests_kernely[i],
            fluid_sim_cellular_cellular_tests_kernelz[i]
        ));
    }

    //link neighbors
    chunk_link_neighbors(queue);

    //fill them with values
    for(int i = 0; i < chunkCount; i++){
        chunk_fill(queue[i],0);
    }
    
    //fill the bottom plane of chunks
    for(int x = 0; x < 3; x++){
        for(int z = 0; z < 3; z++){
            float ** d = queue[x * 3 * 3 + z]->d;
            for(int i = 2; i < DIM-2; i++){
                for(int j = 2; j < DIM-2; j++){
                    d[CENTER_LOC][IX(i,1,j)] = MAX_FLUID_VALUE;
                }
            }
        }
    }

    //check sum beforehand
    float originalSum = chunk_queue_sum_density(queue);

    float slice[16 * 3][16 * 3];
    int compareX = 2;
    int compareZ = 2;

    //dispatch and simulate
    int frameCount = 100;
    int frameCounter;
    for(frameCounter = 0; frameCounter < frameCount; frameCounter++){
        float currentSum = chunk_queue_sum_density(queue);
        float delta = fabs(originalSum - currentSum);
        if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
            fluid_sim_cellular_test_print_slice_big_vis(queue,1);
            printf("Failed to equal sums! \n");
            rVal += assertEqualsFloat(currentSum,originalSum,"Sums are not identical! %f %f  \n");
            printf("desync by frame %d \n",frameCounter);
            break;
        }
        // printf("frame: %d   --- %f   \n", frameCounter,currentSum);
        fluid_solve_bounds(chunkCount,queue,env);
        fluid_dispatch(chunkCount,queue,env,FLUID_DISPATCHER_OVERRIDE_CELLULAR);
        fluid_simulate(env);
        fluid_solve_bounds(chunkCount,queue,env);
        env->state.frame++;
    }

    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(chunkCount,queue,env);

    //check sum beforehand
    float afterSum = chunk_queue_sum_density(queue);

    //diff the sums to see if we've changed value a lot
    float delta = fabs(originalSum - afterSum);
    if(delta > FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1){
        rVal += assertEqualsFloat(originalSum,afterSum,"cellular sim was unstable! %f %f \n");
    }

    // printf("\n");
    return rVal;
}


int fluid_sim_cellular_cellular_tests(int argc, char **argv){
    int rVal = 0;

    // rVal += fluid_sim_cellular_bounds_test1();
    // rVal += fluid_sim_cellular_bounds_test2();
    rVal += fluid_sim_cellular_stability_test1();
    rVal += fluid_sim_cellular_stability_test2();
    rVal += fluid_sim_cellular_stability_test3();
    rVal += fluid_sim_cellular_stability_test4();
    rVal += fluid_sim_cellular_stability_test5();
    rVal += fluid_sim_cellular_stability_test6();
    // rVal += fluid_sim_cellular_stability_test7();
    rVal += fluid_sim_cellular_stability_test8();
    rVal += fluid_sim_cellular_stability_test9();
    rVal += fluid_sim_cellular_stability_test10();
    // rVal += fluid_sim_cellular_stability_test11();
    rVal += fluid_sim_cellular_stability_test12();
    rVal += fluid_sim_cellular_stability_test13();
    // rVal += fluid_sim_cellular_stability_test14();
    // rVal += fluid_sim_cellular_stability_test15();
    // rVal += fluid_sim_cellular_stability_test16();

    return rVal;
}
