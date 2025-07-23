#include <jni.h>

//library includes
//include stb ds
#define STB_DS_IMPLEMENTATION
#include "stb/stb_ds.h"

//local includes
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"
#include "fluid/queue/boundsolver.h"
#include "fluid/queue/metadatacalc.h"
#include "fluid/env/environment.h"
#include "fluid/env/utilities.h"
#include "fluid/sim/grid/simulation.h"
#include "fluid/sim/grid2/grid2.h"
#include "fluid/sim/simulator.h"
#include "fluid/tracking/tracking.h"
#include "fluid/dispatch/dispatcher.h"
#include "math/ode/multigrid.h"


//defines


//function defines
#define getChunk(i) (*env)->CallObjectMethod(env,chunkList,jListGet,i)
#define getBuffArr(buffId) (*env)->GetObjectField(env,chunkJRaw,buffId)
#define setBuffArr(buffId,value) (*env)->SetObjectField(env,chunkJRaw,buffId,value)



//declarations
int readInChunks(JNIEnv * env, jobject chunkList, Environment * environment);



//the list of chunks
Chunk ** chunkViewC = NULL;
//the number of chunks
int numChunks = 0;

//the environment data
Environment * environment = NULL;



/**
 * Gets the array pointer
 */
void * getArray(JNIEnv * env, jobjectArray arr, int index);


JNIEXPORT void JNICALL Java_electrosphere_server_physics_fluid_simulator_FluidAcceleratedSimulator_simulate(
    JNIEnv * env,
    jclass fluidSimClass,
    jobject chunkList,
    jfloat dt
    ){
    environment->consts.dt = dt;
    int numReadIn = readInChunks(env,chunkList,environment);
    fluid_tracking_reset(environment);
    fluid_solve_bounds(numReadIn,chunkViewC,environment);
    fluid_dispatch(numReadIn,chunkViewC,environment,FLUID_DISPATCHER_OVERRIDE_NONE);
    fluid_simulate(environment);
    updateMetadata(env,numReadIn,chunkViewC,environment);
    
    //solve bounds afterwards to properly push data back into real arrays
    //ie, if data is pushed out of bounds from one chunk to another, must
    //then copy it into the in-bounds chunk
    fluid_solve_bounds(numReadIn,chunkViewC,environment);
}

/**
 * Should clean up all native allocations and state
 */
JNIEXPORT void JNICALL Java_electrosphere_server_physics_fluid_simulator_FluidAcceleratedSimulator_free(
    JNIEnv * env,
    jclass fluidSimClass
    ){
    
    //free the c view of the chunks
    int storedChunks = stbds_arrlen(chunkViewC);
    for(int i = 0; i < storedChunks; i++){
        free(chunkViewC[i]);
    }

    //free the dynamic array
    stbds_arrfree(chunkViewC);
    chunkViewC = NULL;
    numChunks = 0;
}

/**
 * Initializes the library
 */
JNIEXPORT void JNICALL Java_electrosphere_server_physics_fluid_simulator_FluidAcceleratedSimulator_init(
    JNIEnv * env,
    jclass fluidSimClass,
    jfloat gravity
    ){
    
    //allocate if unallocated
    if(environment == NULL){
        environment = fluid_environment_create();
    }

    //store variables from java side
    environment->consts.gravity = gravity;
    environment->state.existingDensity = 0;
    environment->state.newDensity = 0;
    environment->state.normalizationRatio = 0;
    environment->state.frame = 0;

    //store jni lookup tables
    jclass listClass = (*env)->FindClass(env,"java/util/List");
    jclass fluidSimStorageClass = (*env)->FindClass(env,"electrosphere/server/physics/fluid/manager/ServerFluidChunk");
    environment->lookupTable.serverFluidChunkClass = fluidSimStorageClass;
    //JNIEnv *env, jclass clazz, const char *name, const char *sig
    environment->lookupTable.listTable.jListSize = (*env)->GetMethodID(env, listClass, "size", "()I");
    environment->lookupTable.listTable.jListGet = (*env)->GetMethodID(env, listClass, "get", "(I)Ljava/lang/Object;");
    environment->lookupTable.listTable.jListAdd = (*env)->GetMethodID(env, listClass, "add", "(Ljava/lang/Object;)Z");
    //ByteBuffer[]
    environment->lookupTable.serverFluidChunkTable.dJId = (*env)->GetFieldID(env,fluidSimStorageClass,"bWeights","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.d0JId = (*env)->GetFieldID(env,fluidSimStorageClass,"b0Weights","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.uJId = (*env)->GetFieldID(env,fluidSimStorageClass,"bVelocityX","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.vJId = (*env)->GetFieldID(env,fluidSimStorageClass,"bVelocityY","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.wJId = (*env)->GetFieldID(env,fluidSimStorageClass,"bVelocityZ","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.u0JId = (*env)->GetFieldID(env,fluidSimStorageClass,"b0VelocityX","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.v0JId = (*env)->GetFieldID(env,fluidSimStorageClass,"b0VelocityY","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.w0JId = (*env)->GetFieldID(env,fluidSimStorageClass,"b0VelocityZ","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.boundsId = (*env)->GetFieldID(env,fluidSimStorageClass,"bBounds","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.pressureCacheId = (*env)->GetFieldID(env,fluidSimStorageClass,"bPressureCache","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.divergenceCacheId = (*env)->GetFieldID(env,fluidSimStorageClass,"bDivergenceCache","[Ljava/nio/ByteBuffer;");
    environment->lookupTable.serverFluidChunkTable.worldXId = (*env)->GetFieldID(env,fluidSimStorageClass,"worldX","I");
    environment->lookupTable.serverFluidChunkTable.worldYId = (*env)->GetFieldID(env,fluidSimStorageClass,"worldY","I");
    environment->lookupTable.serverFluidChunkTable.worldZId = (*env)->GetFieldID(env,fluidSimStorageClass,"worldZ","I");
    environment->lookupTable.serverFluidChunkTable.neighborsId = (*env)->GetFieldID(env,fluidSimStorageClass,"neighbors","[Lelectrosphere/server/physics/fluid/manager/ServerFluidChunk;");
    environment->lookupTable.serverFluidChunkTable.chunkmaskJId = (*env)->GetFieldID(env,fluidSimStorageClass,"chunkMask","I");
    environment->lookupTable.serverFluidChunkTable.totalDensityId = (*env)->GetFieldID(env,fluidSimStorageClass,"totalDensity","F");
    environment->lookupTable.serverFluidChunkTable.updatedId = (*env)->GetFieldID(env,fluidSimStorageClass,"updated","Z");
    environment->lookupTable.serverFluidChunkTable.asleepId = (*env)->GetFieldID(env,fluidSimStorageClass,"asleep","Z");
    environment->lookupTable.serverFluidChunkTable.homogenousId = (*env)->GetFieldID(env,fluidSimStorageClass,"isHomogenous","Z");
    environment->lookupTable.serverFluidChunkTable.normalizationRatioId = (*env)->GetStaticFieldID(env,fluidSimStorageClass,"normalizationRatio","F");
    environment->lookupTable.serverFluidChunkTable.massCountId = (*env)->GetStaticFieldID(env,fluidSimStorageClass,"massCount","F");
    environment->lookupTable.serverFluidChunkTable.pressureTotalId = (*env)->GetFieldID(env,fluidSimStorageClass,"totalPressure","F");
    environment->lookupTable.serverFluidChunkTable.velocityMagTotalId = (*env)->GetFieldID(env,fluidSimStorageClass,"totalVelocityMag","F");
    environment->lookupTable.serverFluidChunkTable.pressureOutgoingId = (*env)->GetFieldID(env,fluidSimStorageClass,"pressureOutgoing","[F");
    environment->lookupTable.serverFluidChunkTable.pressureIncomingId = (*env)->GetFieldID(env,fluidSimStorageClass,"pressureIncoming","[F");
    environment->lookupTable.serverFluidChunkTable.densityOutgoingId = (*env)->GetFieldID(env,fluidSimStorageClass,"densityOutgoing","[F");
    environment->lookupTable.serverFluidChunkTable.densityIncomingId = (*env)->GetFieldID(env,fluidSimStorageClass,"densityIncoming","[F");
}

/**
 * Reads chunks into the dynamic array
 * @return The number of chunks that were successfully parsed
*/
int readInChunks(JNIEnv * env, jobject chunkList, Environment * environment){
    jclass listClass = (*env)->FindClass(env,"java/util/List");
    jclass fluidSimClass = (*env)->FindClass(env,"electrosphere/server/physics/fluid/manager/ServerFluidChunk");
    //JNIEnv *env, jclass clazz, const char *name, const char *sig
    jmethodID jListSize = environment->lookupTable.listTable.jListSize;
    jmethodID jListGet  = environment->lookupTable.listTable.jListGet;
    jmethodID jListAdd  = environment->lookupTable.listTable.jListAdd;

    //ByteBuffer[]
    jfieldID dJId = environment->lookupTable.serverFluidChunkTable.dJId;
    jfieldID d0JId = environment->lookupTable.serverFluidChunkTable.d0JId;
    jfieldID uJId = environment->lookupTable.serverFluidChunkTable.uJId;
    jfieldID vJId = environment->lookupTable.serverFluidChunkTable.vJId;
    jfieldID wJId = environment->lookupTable.serverFluidChunkTable.wJId;
    jfieldID u0JId = environment->lookupTable.serverFluidChunkTable.u0JId;
    jfieldID v0JId = environment->lookupTable.serverFluidChunkTable.v0JId;
    jfieldID w0JId = environment->lookupTable.serverFluidChunkTable.w0JId;
    jfieldID boundsId = environment->lookupTable.serverFluidChunkTable.boundsId;
    jfieldID pressureCacheId = environment->lookupTable.serverFluidChunkTable.pressureCacheId;
    jfieldID divergenceCacheId = environment->lookupTable.serverFluidChunkTable.divergenceCacheId;
    jfieldID chunkmaskJId = environment->lookupTable.serverFluidChunkTable.chunkmaskJId;
    jfieldID asleepId = environment->lookupTable.serverFluidChunkTable.asleepId;
    jfieldID worldXId = environment->lookupTable.serverFluidChunkTable.worldXId;
    jfieldID worldYId = environment->lookupTable.serverFluidChunkTable.worldYId;
    jfieldID worldZId = environment->lookupTable.serverFluidChunkTable.worldZId;

    //the number of chunks
    numChunks = (*env)->CallIntMethod(env,chunkList,jListSize);

    //current chunk (this)
    jobject chunkJRaw;
    //current chunk fields
    jobjectArray jd;
    jobjectArray jd0;
    jobjectArray u;
    jobjectArray v;
    jobjectArray w;
    jobjectArray u0;
    jobjectArray v0;
    jobjectArray w0;
    jobjectArray bounds;
    jobjectArray pressureCache;
    jobjectArray divergenceCache;
    int chunkMask;

    //solve chunk mask
    int cSideArrPos = 0;
    for(int i = 0; i < numChunks; i++){
        chunkJRaw = getChunk(i);

        //skip this chunk if the center array is not allocated (must not have been removed yet?)
        if(
            (*env)->GetBooleanField(env,chunkJRaw,asleepId) == JNI_TRUE ||
            getBuffArr(dJId) == NULL ||
            (*env)->GetObjectArrayElement(env,getBuffArr(dJId),CENTER_LOC) == NULL ||
            (*env)->GetDirectBufferAddress(env,(*env)->GetObjectArrayElement(env,getBuffArr(dJId),CENTER_LOC)) == NULL
        ){
            continue;
        }

        //calculate chunk mask
        chunkMask = calculateChunkMask(env,getBuffArr(dJId));
        (*env)->SetIntField(env,chunkJRaw,chunkmaskJId,chunkMask);

        Chunk * newChunk;
        if(cSideArrPos >= stbds_arrlen(chunkViewC)){
            // printf("allocate chunk %d\n",i);
            // fflush(stdout);
            newChunk = chunk_create();
            // printf("new chunk %p\n",newChunk);
            // fflush(stdout);
            stbds_arrput(chunkViewC,newChunk);
            // printf("new chunk %p\n",chunks[i]);
            // fflush(stdout);
        } else {
            newChunk = chunkViewC[cSideArrPos];
            // printf("get chunk %d: %p\n",i,newChunk);
            // fflush(stdout);
        }
        cSideArrPos++;
        jd = (*env)->GetObjectField(env,chunkJRaw,dJId);
        jd0 = (*env)->GetObjectField(env,chunkJRaw,d0JId);
        u = (*env)->GetObjectField(env,chunkJRaw,uJId);
        v = (*env)->GetObjectField(env,chunkJRaw,vJId);
        w = (*env)->GetObjectField(env,chunkJRaw,wJId);
        u0 = (*env)->GetObjectField(env,chunkJRaw,u0JId);
        v0 = (*env)->GetObjectField(env,chunkJRaw,v0JId);
        w0 = (*env)->GetObjectField(env,chunkJRaw,w0JId);
        bounds = (*env)->GetObjectField(env,chunkJRaw,boundsId);
        pressureCache = (*env)->GetObjectField(env,chunkJRaw,pressureCacheId);
        divergenceCache = (*env)->GetObjectField(env,chunkJRaw,divergenceCacheId);
        newChunk->chunkMask = chunkMask;
        newChunk->chunkJRaw = chunkJRaw;
        newChunk->x = (*env)->GetIntField(env,chunkJRaw,worldXId);
        newChunk->y = (*env)->GetIntField(env,chunkJRaw,worldYId);
        newChunk->z = (*env)->GetIntField(env,chunkJRaw,worldZId);
        for(int j = 0; j < 27; j++){
            if((chunkMask & CHUNK_INDEX_ARR[j]) > 0){
                newChunk->d[j] = getArray(env,jd,j);
                newChunk->d0[j] = getArray(env,jd0,j);
                newChunk->u[j] = getArray(env,u,j);
                newChunk->v[j] = getArray(env,v,j);
                newChunk->w[j] = getArray(env,w,j);
                newChunk->u0[j] = getArray(env,u0,j);
                newChunk->v0[j] = getArray(env,v0,j);
                newChunk->w0[j] = getArray(env,w0,j);
                newChunk->bounds[j] = getArray(env,bounds,j);
                newChunk->pressureCache[j] = getArray(env,pressureCache,j);
                newChunk->divergenceCache[j] = getArray(env,divergenceCache,j);
                newChunk->outgoingPressure[j] = (*env)->GetFloatArrayElements(env,(*env)->GetObjectField(env,chunkJRaw,environment->lookupTable.serverFluidChunkTable.pressureOutgoingId),JNI_FALSE)[j];
                newChunk->incomingPressure[j] = (*env)->GetFloatArrayElements(env,(*env)->GetObjectField(env,chunkJRaw,environment->lookupTable.serverFluidChunkTable.pressureIncomingId),JNI_FALSE)[j];
                newChunk->outgoingDensity[j] = (*env)->GetFloatArrayElements(env,(*env)->GetObjectField(env,chunkJRaw,environment->lookupTable.serverFluidChunkTable.densityOutgoingId),JNI_FALSE)[j];
                newChunk->incomingDensity[j] = (*env)->GetFloatArrayElements(env,(*env)->GetObjectField(env,chunkJRaw,environment->lookupTable.serverFluidChunkTable.densityIncomingId),JNI_FALSE)[j];
            } else {
                newChunk->d[j] = NULL;
                newChunk->d0[j] = NULL;
                newChunk->u[j] = NULL;
                newChunk->v[j] = NULL;
                newChunk->w[j] = NULL;
                newChunk->u0[j] = NULL;
                newChunk->v0[j] = NULL;
                newChunk->w0[j] = NULL;
                newChunk->bounds[j] = NULL;
                newChunk->pressureCache[j] = NULL;
                newChunk->divergenceCache[j] = NULL;
                newChunk->outgoingPressure[j] = 0;
                newChunk->incomingPressure[j] = 0;
                newChunk->outgoingDensity[j] = 0;
                newChunk->incomingDensity[j] = 0;
            }
        }
    }
    return cSideArrPos;
}

/**
 * Gets the array pointer
 */
void * getArray(JNIEnv * env, jobjectArray arr, int index){
    jobject arrayEl = (*env)->GetObjectArrayElement(env,arr,index);
    if(arrayEl == NULL){
        return NULL;
    }
    return (*env)->GetDirectBufferAddress(env,arrayEl);
}

