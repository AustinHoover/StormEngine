#include <stdint.h>

//native interfaces
#include "native/electrosphere_server_physics_fluid_simulator_FluidAcceleratedSimulator.h"

//fluid lib
#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/sim/grid/mainFunctions.h"
#include "fluid/queue/chunk.h"
#include "fluid/sim/grid/simulation.h"
#include "fluid/sim/grid/solver_consts.h"

#ifndef SAVE_STEPS
#define SAVE_STEPS 0
#endif


#define REALLY_SMALL_VALUE 0.00001

#define DIFFUSION_CONSTANT 0.00001
#define VISCOSITY_CONSTANT 0.00001

char fileNameBuff[50];

//all chunks
Chunk ** chunks = NULL;

//jni help:
//https://stackoverflow.com/questions/39823375/clarification-about-getfieldid

static inline void saveStep(float * values, const char * name);
static inline void applyGravity(Chunk * currentChunk, Environment * environment);
static inline void clearArr(float ** d);

void fluid_grid_simulate(
    int numChunks,
    Chunk ** passedInChunks,
    Environment * environment,
    jfloat timestep
){
    chunks = passedInChunks;

    // printf("%p\n",chunks[0].d);
    // saveStep(chunks[0]->u[CENTER_LOC], "./chunks/beginU");
    // saveStep(chunks[0]->v[CENTER_LOC], "./chunks/beginV");
    // saveStep(chunks[0]->w[CENTER_LOC], "./chunks/beginW");
    // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/beginU0");
    // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/beginV0");
    // saveStep(chunks[0]->w0[CENTER_LOC], "./chunks/beginW0");

    //solve chunk mask
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = chunks[i];
        applyGravity(currentChunk,environment);
        addSourceToVectors(
            DIM,
            currentChunk->chunkMask,
            currentChunk->u,
            currentChunk->v,
            currentChunk->w,
            currentChunk->u0,
            currentChunk->v0,
            currentChunk->w0,
            DIFFUSION_CONSTANT,
            VISCOSITY_CONSTANT,
            timestep
        );
        // saveStep(currentChunk->u[CENTER_LOC], "./chunks/addSrcU");
        // saveStep(currentChunk->v[CENTER_LOC], "./chunks/addSrcV");
        // saveStep(currentChunk->w[CENTER_LOC], "./chunks/addSrcW");
        // saveStep(currentChunk->u0[CENTER_LOC], "./chunks/addSrcU0");
        // saveStep(currentChunk->v0[CENTER_LOC], "./chunks/addSrcV0");
        // saveStep(currentChunk->w0[CENTER_LOC], "./chunks/addSrcW0");
    }
    //swap all vector fields
    {
        //swap vector fields
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];

            float * tmpArr;
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->u[j];
                currentChunk->u[j] = currentChunk->u0[j];
                currentChunk->u0[j] = tmpArr;
            }
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->v[j];
                currentChunk->v[j] = currentChunk->v0[j];
                currentChunk->v0[j] = tmpArr;
            }
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->w[j];
                currentChunk->w[j] = currentChunk->w0[j];
                currentChunk->w0[j] = tmpArr;
            }
        }
        //copy neighbors
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w0);
        }
    }
    // saveStep(chunks[0]->u[CENTER_LOC], "./chunks/swapU");
    // saveStep(chunks[0]->v[CENTER_LOC], "./chunks/swapV");
    // saveStep(chunks[0]->w[CENTER_LOC], "./chunks/swapW");
    // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/swapU0");
    // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/swapV0");
    // saveStep(chunks[0]->w0[CENTER_LOC], "./chunks/swapW0");
    // printf("after swap vecs u\n");
    // printLayer(chunks[0]->u[CENTER_LOC],targetLayer);
    // printf("after swap vecs u0\n");
    // printLayer(chunks[0]->u0[CENTER_LOC],targetLayer);
    //solve vector diffusion
    {
        for(int l = 0; l < VECTOR_DIFFUSE_TIMES; l++){
            //solve vector diffusion
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                solveVectorDiffuse(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
            }
            // if(SAVE_STEPS){
            //     sprintf(fileNameBuff, "./chunks/diffuseUStep%dx", l);
            //     saveStep(chunks[0]->u[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseUStep%dx0", l);
            //     saveStep(chunks[0]->u0[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseVStep%dx", l);
            //     saveStep(chunks[0]->v[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseVStep%dx0", l);
            //     saveStep(chunks[0]->v0[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseWStep%dx", l);
            //     saveStep(chunks[0]->w[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseWStep%dx0", l);
            //     saveStep(chunks[0]->w0[CENTER_LOC], fileNameBuff);
            // }
            //update array for vectors
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u);
                setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v);
                setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w);
                // setBoundsToNeighborsRaw(DIM,chunkMask,1,currentChunk->u0);
                // setBoundsToNeighborsRaw(DIM,chunkMask,2,currentChunk->v0);
                // setBoundsToNeighborsRaw(DIM,chunkMask,3,currentChunk->w0);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->u);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->v);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->w);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->u0);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->v0);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->w0);
            }
            // if(SAVE_STEPS){
            //     sprintf(fileNameBuff, "./chunks/diffuseUStep%dxBnd", l);
            //     saveStep(chunks[0]->u[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseUStep%dx0Bnd", l);
            //     saveStep(chunks[0]->u0[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseVStep%dxBnd", l);
            //     saveStep(chunks[0]->v[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseVStep%dx0Bnd", l);
            //     saveStep(chunks[0]->v0[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseWStep%dxBnd", l);
            //     saveStep(chunks[0]->w[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/diffuseWStep%dx0Bnd", l);
            //     saveStep(chunks[0]->w0[CENTER_LOC], fileNameBuff);
            // }
        }
    }
    // saveStep(chunks[0]->u[CENTER_LOC], "./chunks/diffuseU");
    // saveStep(chunks[0]->v[CENTER_LOC], "./chunks/diffuseV");
    // saveStep(chunks[0]->w[CENTER_LOC], "./chunks/diffuseW");
    // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/diffuseU0");
    // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/diffuseV0");
    // saveStep(chunks[0]->w0[CENTER_LOC], "./chunks/diffuseW0");
    //solve projection
    {
        //update array for vectors
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            // setBoundsToNeighborsRaw(DIM,chunkMask,1,currentChunk->u);
            // setBoundsToNeighborsRaw(DIM,chunkMask,2,currentChunk->v);
            // setBoundsToNeighborsRaw(DIM,chunkMask,3,currentChunk->w);
            // setBoundsToNeighborsRaw(DIM,chunkMask,1,currentChunk->u0);
            // setBoundsToNeighborsRaw(DIM,chunkMask,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
        }
        //setup projection
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setupProjection(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
        }

        // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/setupProj1Div");
        // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/setupProj1P");

        //update array for vectors
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,0,currentChunk->u0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,0,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->v0);
        }

        // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/setupProj1DivBnd");
        // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/setupProj1PBnd");

        //samples u0, v0
        //sets u0
        //these should have just been mirrored in the above
        //
        //Perform main projection solver
        for(int l = 0; l < LINEARSOLVERTIMES; l++){
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                solveProjection(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
            }
            // if(SAVE_STEPS){
            //     sprintf(fileNameBuff, "./chunks/proj1Step%dx", l);
            //     saveStep(chunks[0]->u0[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/proj1Step%dx0", l);
            //     saveStep(chunks[0]->v0[CENTER_LOC], fileNameBuff);
            // }
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,0,currentChunk->u0);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->u0);
            }
            // if(SAVE_STEPS){
            //     sprintf(fileNameBuff, "./chunks/proj1Step%dxBnd", l);
            //     saveStep(chunks[0]->u0[CENTER_LOC], fileNameBuff);
            //     sprintf(fileNameBuff, "./chunks/proj1Step%dx0Bnd", l);
            //     saveStep(chunks[0]->v0[CENTER_LOC], fileNameBuff);
            // }
        }
        //samples u,v,w,u0
        //sets u,v,w
        //Finalize projection
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            finalizeProjection(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
        }

        // saveStep(chunks[0]->u[CENTER_LOC], "./chunks/finalizeProj1U");
        // saveStep(chunks[0]->v[CENTER_LOC], "./chunks/finalizeProj1V");
        // saveStep(chunks[0]->w[CENTER_LOC], "./chunks/finalizeProj1W");
        // exit(0);
        //set boundaries a final time for u,v,w
        //...
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w0);
        }
    }
    // saveStep(chunks[0]->u[CENTER_LOC], "./chunks/projU");
    // saveStep(chunks[0]->v[CENTER_LOC], "./chunks/projV");
    // saveStep(chunks[0]->w[CENTER_LOC], "./chunks/projW");
    // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/projU0");
    // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/projV0");
    // saveStep(chunks[0]->w0[CENTER_LOC], "./chunks/projW0");
    // exit(0);
    //swap all vector fields
    {
        //swap vector fields
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];

            float * tmpArr;
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->u[j];
                currentChunk->u[j] = currentChunk->u0[j];
                currentChunk->u0[j] = tmpArr;
            }
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->v[j];
                currentChunk->v[j] = currentChunk->v0[j];
                currentChunk->v0[j] = tmpArr;
            }
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->w[j];
                currentChunk->w[j] = currentChunk->w0[j];
                currentChunk->w0[j] = tmpArr;
            }
        }
        //copy neighbors
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w0);
        }
    }
    // saveStep(chunks[0]->u[CENTER_LOC], "./chunks/swap2U");
    // saveStep(chunks[0]->v[CENTER_LOC], "./chunks/swap2V");
    // saveStep(chunks[0]->w[CENTER_LOC], "./chunks/swap2W");
    // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/swap2U0");
    // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/swap2V0");
    // saveStep(chunks[0]->w0[CENTER_LOC], "./chunks/swap2W0");
    //advect vectors across boundaries
    {
        //update border arrs
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w0);
        }
        //advect
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            advectVectors(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
        }
        //update neighbor arr
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
        }
    }
    // saveStep(chunks[0]->u[CENTER_LOC], "./chunks/advectU");
    // saveStep(chunks[0]->v[CENTER_LOC], "./chunks/advectV");
    // saveStep(chunks[0]->w[CENTER_LOC], "./chunks/advectW");
    // saveStep(chunks[0]->u0[CENTER_LOC], "./chunks/advectU0");
    // saveStep(chunks[0]->v0[CENTER_LOC], "./chunks/advectV0");
    // saveStep(chunks[0]->w0[CENTER_LOC], "./chunks/advectW0");
    //solve projection
    {
        //update array for vectors
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
        }
        //setup projection
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setupProjection(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
        }
        //update array for vectors
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
        }
        //samples u0, v0
        //sets u0
        //these should have just been mirrored in the above
        //
        //Perform main projection solver
        for(int l = 0; l < LINEARSOLVERTIMES; l++){
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                solveProjection(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
            }
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,0,currentChunk->u0);
                copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->u0);
            }
        }
        //samples u,v,w,u0
        //sets u,v,w
        //Finalize projection
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            finalizeProjection(DIM,currentChunk->chunkMask,currentChunk->u,currentChunk->v,currentChunk->w,currentChunk->u0,currentChunk->v0,currentChunk->w0,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
        }
        //set boundaries a final time for u,v,w
        //...
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,1,currentChunk->u0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,2,currentChunk->v0);
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,3,currentChunk->w0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,1,currentChunk->u0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,2,currentChunk->v0);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,3,currentChunk->w0);
        }
    }




    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    ///------------------------------------------------------------------------------------------------------------------------------------------------------------------------




    //add density
    {
        double deltaDensity = 0;
        environment->state.existingDensity = 0;
        environment->state.newDensity = 0;
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            addDensity(environment,DIM,currentChunk->chunkMask,currentChunk->d,currentChunk->d0,timestep);
        }
    }
    //swap all density arrays
    {
        //swap vector fields
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];

            float * tmpArr;
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->d[j];
                currentChunk->d[j] = currentChunk->d0[j];
                currentChunk->d0[j] = tmpArr;
            }
        }
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->d);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->d0);
        }
    }
    //diffuse density
    {
        for(int l = 0; l < LINEARSOLVERTIMES; l++){
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                solveDiffuseDensity(DIM,currentChunk->chunkMask,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,DIFFUSION_CONSTANT,VISCOSITY_CONSTANT,timestep);
            }
            for(int i = 0; i < numChunks; i++){
                Chunk * currentChunk = chunks[i];
                setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,0,currentChunk->d);
            }
        }
    }
    //swap all density arrays
    {
        //swap vector fields
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            float * tmpArr;
            for(int j = 0; j < 27; j++){
                tmpArr = currentChunk->d[j];
                currentChunk->d[j] = currentChunk->d0[j];
                currentChunk->d0[j] = tmpArr;
            }
        }
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->d);
            copyNeighborsRaw(DIM,currentChunk->chunkMask,0,0,currentChunk->d0);
        }
    }
    //advect density
    {
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            advectDensity(currentChunk->chunkMask,DIM,currentChunk->d,currentChunk->d0,currentChunk->u,currentChunk->v,currentChunk->w,timestep);
        }
    }
    //mirror densities
    {
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            setBoundsToNeighborsRaw(DIM,currentChunk->chunkMask,0,currentChunk->d);
        }
    }
    //normalize densities
    {
        double transformedDensity = 0;
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            transformedDensity = transformedDensity + calculateSum(currentChunk->chunkMask,DIM,currentChunk->d);
        }
        float normalizationRatio = 0;
        if(transformedDensity != 0){
            normalizationRatio = (environment->state.existingDensity + environment->state.newDensity) / transformedDensity;
            environment->state.normalizationRatio = normalizationRatio;
        }
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            normalizeDensity(DIM,currentChunk->d,normalizationRatio);
        }
    }
    //clear delta arrays
    {
        for(int i = 0; i < numChunks; i++){
            Chunk * currentChunk = chunks[i];
            clearArr(currentChunk->d0);
            clearArr(currentChunk->u0);
            clearArr(currentChunk->v0);
            clearArr(currentChunk->w0);
        }
    }
}


static inline void saveStep(float * values, const char * name){
    if(SAVE_STEPS){
        FILE *fp;
        int N = DIM;

        // ... fill the array somehow ...

        fp = fopen(name, "w");
        // check for error here

        for(int x = 0; x < DIM; x++){
            for(int y = 0; y < DIM; y++){
                for(int z = 0; z < DIM; z++){
                    float val = values[IX(x,y,z)];
                    if(val < REALLY_SMALL_VALUE && val > -REALLY_SMALL_VALUE){
                        val = 0;
                    }
                    fprintf(fp, "%f\t", val);
                }
                fprintf(fp, "\n");
            }
            fprintf(fp, "\n");
        }

        fclose(fp);
    }
}

/**
 * Applies gravity to the chunk
 * @param currentChunk The chunk to apply on
 * @param environment The environment data of the world
 */
static inline void applyGravity(Chunk * currentChunk, Environment * environment){
    int N = DIM;
    for(int x = 0; x < DIM; x++){
        for(int y = 0; y < DIM; y++){
            for(int z = 0; z < DIM; z++){
                GET_ARR_RAW(currentChunk->v0,CENTER_LOC)[IX(x,y,z)] = GET_ARR_RAW(currentChunk->v0,CENTER_LOC)[IX(x,y,z)] + GET_ARR_RAW(currentChunk->d,CENTER_LOC)[IX(x,y,z)] * environment->consts.gravity;
            }
        }
    }
}

/**
 * Clears an array
 */
static inline void clearArr(float ** d){
    float * x = GET_ARR_RAW(d,CENTER_LOC);
    for(int j = 0; j < DIM * DIM * DIM; j++){
        x[j] = 0;
    }
}