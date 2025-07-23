
#include "stb/stb_ds.h"

#include "fluid/queue/boundsolver.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/grid2/density.h"
#include "fluid/sim/grid2/solver_consts.h"
#include "fluid/sim/grid2/utilities.h"
#include "fluid/sim/grid2/velocity.h"
#include "math/ode/multigrid.h"
#include "../../../util/chunk_test_utils.h"
#include "../../../util/test.h"


int fluid_sim_grid2_density_diffuse_tests_kernelx[27] = {
    0, 0, 0, 0, 0, 0, 0, 0, 0,
    1, 1, 1, 1, 1, 1, 1, 1, 1,
    2, 2, 2, 2, 2, 2, 2, 2, 2,
};

int fluid_sim_grid2_density_diffuse_tests_kernely[27] = {
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
    0, 0, 0, 1, 1, 1, 2, 2, 2,
};

int fluid_sim_grid2_density_diffuse_tests_kernelz[27] = {
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
    0, 1, 2, 0, 1, 2, 0, 1, 2,
};









/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_diffuse_test1(){
    int rVal = 0;
    printf("fluid_sim_grid2_density_diffuse_test1\n");
    Environment * env = fluid_environment_create();
    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_grid2_density_diffuse_tests_kernelx[i],
            fluid_sim_grid2_density_diffuse_tests_kernely[i],
            fluid_sim_grid2_density_diffuse_tests_kernelz[i]
        ));
        chunk_fill(queue[i],0);
    }
    //link neighbors
    chunk_link_neighbors(queue);
    fluid_solve_bounds(1,queue,env);




    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;

    float * tmpArr;
    for(int j = 0; j < 27; j++){
        tmpArr = currentChunk->d[j];
        currentChunk->d[j] = currentChunk->d0[j];
        currentChunk->d0[j] = tmpArr;
    }
    //diffuse density
    for(int l = 0; l < FLUID_GRID2_LINEARSOLVERTIMES; l++){
            fluid_grid2_solveDiffuseDensity(env,currentChunk->d,currentChunk->d0,FLUID_GRID2_SIM_STEP);
            fluid_grid2_set_bounds(env,BOUND_SET_DENSITY_PHI,currentChunk->d[CENTER_LOC]);
    }
    //swap all density arrays
    //swap vector fields
    for(int j = 0; j < 27; j++){
        tmpArr = currentChunk->d[j];
        currentChunk->d[j] = currentChunk->d0[j];
        currentChunk->d0[j] = tmpArr;
    }

    //sum the result
    float afterSum = chunk_queue_sum_density(queue);

    rVal += assertEqualsFloat(afterSum,MAX_FLUID_VALUE,"Density diffuse step changed density sum!  %f %f  \n");

    return rVal;
}


/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_diffuse_test2(){
    int rVal = 0;
    printf("fluid_sim_grid2_density_diffuse_test2\n");
    Environment * env = fluid_environment_create();
    int chunkCount =  27;

    Chunk ** queue = NULL;
    for(int i = 0; i < chunkCount; i++){
        arrput(queue,chunk_create_pos(
            fluid_sim_grid2_density_diffuse_tests_kernelx[i],
            fluid_sim_grid2_density_diffuse_tests_kernely[i],
            fluid_sim_grid2_density_diffuse_tests_kernelz[i]
        ));
        chunk_fill(queue[i],0);
    }
    //link neighbors
    chunk_link_neighbors(queue);
    fluid_solve_bounds(1,queue,env);




    Chunk * currentChunk = queue[0];
    currentChunk->d[CENTER_LOC][IX(2,2,2)] = MAX_FLUID_VALUE;

    int frameCount = 50;
    for(int frame = 0; frame < frameCount; frame++){
        float * tmpArr;
        for(int j = 0; j < 27; j++){
            tmpArr = currentChunk->d[j];
            currentChunk->d[j] = currentChunk->d0[j];
            currentChunk->d0[j] = tmpArr;
        }
        //diffuse density
        for(int l = 0; l < FLUID_GRID2_LINEARSOLVERTIMES; l++){
                fluid_grid2_solveDiffuseDensity(env,currentChunk->d,currentChunk->d0,FLUID_GRID2_SIM_STEP);
                fluid_grid2_set_bounds(env,BOUND_SET_DENSITY_PHI,currentChunk->d[CENTER_LOC]);
        }
        //swap all density arrays
        //swap vector fields
        for(int j = 0; j < 27; j++){
            tmpArr = currentChunk->d[j];
            currentChunk->d[j] = currentChunk->d0[j];
            currentChunk->d0[j] = tmpArr;
        }
    }

    //sum the result
    float afterSum = chunk_queue_sum_density(queue);

    rVal += assertEqualsFloat(afterSum,MAX_FLUID_VALUE,"Density diffuse step changed density sum!  %f %f  \n");

    return rVal;
}



/**
 * Testing density diffusion
 */
int fluid_sim_grid2_density_diffuse_tests(int argc, char **argv){
    int rVal = 0;

    rVal += fluid_sim_grid2_density_diffuse_test1();
    rVal += fluid_sim_grid2_density_diffuse_test2();

    return rVal;
}