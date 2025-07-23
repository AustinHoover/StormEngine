#include <immintrin.h>
#include <stdint.h>
#include <math.h>
#include <jni.h>

#include "fluid/env/utilities.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/metadatacalc.h"

#define UPDATE_THRESHOLD 0.1

/**
 * Updates the metadata for all chunks
 * @param env The java environment variable
 * @param numChunks The number of chunks
 * @param passedInChunks The chunks that were passed in
 * @param environment The environment data
 */
LIBRARY_API void updateMetadata(JNIEnv * env, int numChunks, Chunk ** passedInChunks, Environment * environment){
    jfieldID totalDensityId = environment->lookupTable.serverFluidChunkTable.totalDensityId;
    jfieldID updatedId = environment->lookupTable.serverFluidChunkTable.updatedId;
    int N = DIM;

    int i;
    int x, y, z;
    for(i = 0; i < numChunks; i++){

        //get previous total density
        Chunk * currentChunk = passedInChunks[i];
        jobject jObj = currentChunk->chunkJRaw;
        float prevDensity = (*env)->GetFloatField(env,jObj,totalDensityId);
        
        //calculate new total density
        //transform u direction
        float sum = 0;
        for(y=1; y<N-1; y++){
            for(z=1; z<N-1; z++){
                int n = 0;
                //solve as much as possible vectorized
                for(x = 1; x < N-1; x=x+8){
                    sum = sum + GET_ARR_RAW(currentChunk->d,CENTER_LOC)[IX(x,y,z)];
                }
            }
        }
        //get whether the chunk is currently homogenous or not
        int homogenous = sum <= 0 ? JNI_TRUE : JNI_FALSE;
        (*env)->SetBooleanField(env,jObj,environment->lookupTable.serverFluidChunkTable.homogenousId,homogenous);
        // (*env)->SetBooleanField(env,jObj,environment->lookupTable.serverFluidChunkTable.homogenousId,JNI_FALSE);

        //update total density
        (*env)->SetFloatField(env,jObj,totalDensityId,sum);

        //check if any neighbor is non-homogenous
        jobject neighborArr = (*env)->GetObjectField(env,jObj,environment->lookupTable.serverFluidChunkTable.neighborsId);
        int nonHomogenousNeighbor = JNI_FALSE;
        for(int j = 0; j < NEIGHBOR_ARRAY_COUNT; j++){
            if(j == CENTER_LOC){
                continue;
            }
            jobject neighborObj = (*env)->GetObjectArrayElement(env,neighborArr,j);
            if(neighborObj != NULL){
                int neighborHomogenous = (*env)->GetBooleanField(env,neighborObj,environment->lookupTable.serverFluidChunkTable.homogenousId);
                if(neighborHomogenous == JNI_FALSE){
                    nonHomogenousNeighbor = JNI_TRUE;
                    break;
                }
            }
        }

        //figure out if this chunk should sleep or not
        int shouldSleep = JNI_TRUE;
        if(nonHomogenousNeighbor == JNI_TRUE || homogenous == JNI_FALSE){
            shouldSleep = JNI_FALSE;
        }
        (*env)->SetBooleanField(env,jObj,environment->lookupTable.serverFluidChunkTable.asleepId,shouldSleep);

        //if this cell is awake AND non-homogenous, make sure all neighbors are awake
        if(shouldSleep == JNI_FALSE && homogenous == JNI_FALSE){
            (*env)->SetBooleanField(env,jObj,updatedId,JNI_TRUE);
            for(int j = 0; j < NEIGHBOR_ARRAY_COUNT; j++){
                if(j == CENTER_LOC){
                    continue;
                }
                jobject neighborObj = (*env)->GetObjectArrayElement(env,neighborArr,j);
                if(neighborObj != NULL){
                    (*env)->SetBooleanField(env,neighborObj,environment->lookupTable.serverFluidChunkTable.asleepId,JNI_FALSE);
                }
            }
        }
    }

    //alert java side to updated static values
    float normalizationRatio = environment->state.normalizationRatio;
    (*env)->SetStaticFloatField(
        env,
        environment->lookupTable.serverFluidChunkClass,
        environment->lookupTable.serverFluidChunkTable.normalizationRatioId,
        normalizationRatio
    );
    //mass
    float massCount = (float)environment->state.existingDensity;
    (*env)->SetStaticFloatField(
        env,
        environment->lookupTable.serverFluidChunkClass,
        environment->lookupTable.serverFluidChunkTable.massCountId,
        massCount
    );

    //set non-static metadata in each chunk
    for(int i = 0; i < numChunks; i++){
        Chunk * currentChunk = passedInChunks[i];
        //total pressure
        float pressureTotal = (float)currentChunk->pressureCellData.pressureTotal;
        (*env)->SetFloatField(
            env,
            currentChunk->chunkJRaw,
            environment->lookupTable.serverFluidChunkTable.pressureTotalId,
            pressureTotal
        );
        //total velocity magnitude
        float velocityMagTotal = (float)currentChunk->pressureCellData.velocityMagTotal;
        (*env)->SetFloatField(
            env,
            currentChunk->chunkJRaw,
            environment->lookupTable.serverFluidChunkTable.velocityMagTotalId,
            velocityMagTotal
        );
    }


    //update frame state
    environment->state.frame = environment->state.frame + 1;
}

