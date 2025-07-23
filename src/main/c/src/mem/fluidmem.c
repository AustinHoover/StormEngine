#include <jni.h>
#include <stdlib.h>

#include "../../includes/native/electrosphere_server_physics_fluid_manager_ServerFluidChunk.h"
#include "../../includes/native/electrosphere_client_fluid_cache_FluidChunkData.h"

#include "fluid/queue/chunk.h"

#include "../../includes/mem/pool.h"

/**
 * Size of a single buffer
 */
#define BUFF_SIZE 18 *18 * 18 * 4

/**
 * The center position of the buffer array
 */
#define CENTER_POS 13

/**
 * Pool for the center array blocks
 */
POOL * centerArrPool = NULL;

/**
 * Pool for the field blocks
 */
POOL * fieldPool = NULL;

/**
 * Allocates the center buffer of a buffer array
 * @param env The JNI env
 * @param fluidObj The object containing the buffer arrays
 * @param arrFieldId The specific field to allocate
 */
void allocateCenterField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId);

/**
 * Frees the center buffer of a buffer array
 * @param env The JNI env
 * @param fluidObj The object containing the buffer arrays
 * @param arrFieldId The specific field to free
 */
void freeCenterField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId);

/**
 * Allocates the buffer of an object
 * @param env The JNI env
 * @param fluidObj The object containing the buffer field
 * @param arrFieldId The specific field to allocate
 */
void allocateField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId);

/**
 * Frees the center buffer of a buffer array
 * @param env The JNI env
 * @param fluidObj The object containing the buffer arrays
 * @param arrFieldId The specific field to free
 */
void freeField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId);

/**
 * Allocates the buffers for the fluid chunk
 * @param env The JNI env
 * @param fluidObj The fluid object
 */
JNIEXPORT void JNICALL Java_electrosphere_server_physics_fluid_manager_ServerFluidChunk_allocate(
    JNIEnv * env,
    jobject fluidObj
    ){
    jclass serverFluidChunkClass = (*env)->GetObjectClass(env,fluidObj);
    jfieldID dId = (*env)->GetFieldID(env,serverFluidChunkClass,"bWeights","[Ljava/nio/ByteBuffer;");
    jfieldID d0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0Weights","[Ljava/nio/ByteBuffer;");
    jfieldID uId = (*env)->GetFieldID(env,serverFluidChunkClass,"bVelocityX","[Ljava/nio/ByteBuffer;");
    jfieldID vId = (*env)->GetFieldID(env,serverFluidChunkClass,"bVelocityY","[Ljava/nio/ByteBuffer;");
    jfieldID wId = (*env)->GetFieldID(env,serverFluidChunkClass,"bVelocityZ","[Ljava/nio/ByteBuffer;");
    jfieldID u0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0VelocityX","[Ljava/nio/ByteBuffer;");
    jfieldID v0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0VelocityY","[Ljava/nio/ByteBuffer;");
    jfieldID w0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0VelocityZ","[Ljava/nio/ByteBuffer;");
    jfieldID boundsId = (*env)->GetFieldID(env,serverFluidChunkClass,"bBounds","[Ljava/nio/ByteBuffer;");
    jfieldID pressureCacheId = (*env)->GetFieldID(env,serverFluidChunkClass,"bPressureCache","[Ljava/nio/ByteBuffer;");
    jfieldID divergenceCacheId = (*env)->GetFieldID(env,serverFluidChunkClass,"bDivergenceCache","[Ljava/nio/ByteBuffer;");
    allocateCenterField(env,fluidObj,dId);
    allocateCenterField(env,fluidObj,d0Id);
    allocateCenterField(env,fluidObj,uId);
    allocateCenterField(env,fluidObj,vId);
    allocateCenterField(env,fluidObj,wId);
    allocateCenterField(env,fluidObj,u0Id);
    allocateCenterField(env,fluidObj,v0Id);
    allocateCenterField(env,fluidObj,w0Id);
    allocateCenterField(env,fluidObj,boundsId);
    allocateCenterField(env,fluidObj,pressureCacheId);
    allocateCenterField(env,fluidObj,divergenceCacheId);
}

/**
 * Allocates the center buffer of a buffer array
 * @param env The JNI env
 * @param fluidObj The object containing the buffer arrays
 * @param arrFieldId The specific field to allocate
 */
void allocateCenterField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId){
    if(centerArrPool == NULL){
        centerArrPool = pool_create(BUFF_SIZE);
    }
    //actually allocate
    void * buffer = pool_get(centerArrPool);
    if (buffer == NULL) {
        // Handle allocation failure
        return;
    }
    // Create a direct ByteBuffer
    jobject byteBuffer = (*env)->NewDirectByteBuffer(env, buffer, BUFF_SIZE);
    if (byteBuffer == NULL) {
        // Handle ByteBuffer creation failure
        pool_return(centerArrPool, buffer);
        jobject jd = (*env)->GetObjectField(env,fluidObj,arrFieldId);
        (*env)->SetObjectArrayElement(env,jd,CENTER_POS,NULL);
        return;
    }

    //assign to array
    jobject jd = (*env)->GetObjectField(env,fluidObj,arrFieldId);
    (*env)->SetObjectArrayElement(env,jd,CENTER_POS,byteBuffer);

    //zero out the data
    float * floatView = (float *)buffer;
    for(int x = 0; x < DIM * DIM * DIM; x++){
        floatView[x] = 0;
    }
}

/**
 * Frees the buffers for the fluid chunk
 * @param env The JNI env
 * @param fluidObj The fluid object
 */
JNIEXPORT void JNICALL Java_electrosphere_server_physics_fluid_manager_ServerFluidChunk_free(
    JNIEnv * env,
    jobject fluidObj
    ){
    jclass serverFluidChunkClass = (*env)->GetObjectClass(env,fluidObj);
    jfieldID dId = (*env)->GetFieldID(env,serverFluidChunkClass,"bWeights","[Ljava/nio/ByteBuffer;");
    jfieldID d0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0Weights","[Ljava/nio/ByteBuffer;");
    jfieldID uId = (*env)->GetFieldID(env,serverFluidChunkClass,"bVelocityX","[Ljava/nio/ByteBuffer;");
    jfieldID vId = (*env)->GetFieldID(env,serverFluidChunkClass,"bVelocityY","[Ljava/nio/ByteBuffer;");
    jfieldID wId = (*env)->GetFieldID(env,serverFluidChunkClass,"bVelocityZ","[Ljava/nio/ByteBuffer;");
    jfieldID u0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0VelocityX","[Ljava/nio/ByteBuffer;");
    jfieldID v0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0VelocityY","[Ljava/nio/ByteBuffer;");
    jfieldID w0Id = (*env)->GetFieldID(env,serverFluidChunkClass,"b0VelocityZ","[Ljava/nio/ByteBuffer;");
    jfieldID boundsId = (*env)->GetFieldID(env,serverFluidChunkClass,"bBounds","[Ljava/nio/ByteBuffer;");
    jfieldID pressureCacheId = (*env)->GetFieldID(env,serverFluidChunkClass,"bPressureCache","[Ljava/nio/ByteBuffer;");
    jfieldID divergenceCacheId = (*env)->GetFieldID(env,serverFluidChunkClass,"bDivergenceCache","[Ljava/nio/ByteBuffer;");
    freeCenterField(env,fluidObj,dId);
    freeCenterField(env,fluidObj,d0Id);
    freeCenterField(env,fluidObj,uId);
    freeCenterField(env,fluidObj,vId);
    freeCenterField(env,fluidObj,wId);
    freeCenterField(env,fluidObj,u0Id);
    freeCenterField(env,fluidObj,v0Id);
    freeCenterField(env,fluidObj,w0Id);
    freeCenterField(env,fluidObj,boundsId);
    freeCenterField(env,fluidObj,pressureCacheId);
    freeCenterField(env,fluidObj,divergenceCacheId);
}

/**
 * Frees the center buffer of a buffer array
 * @param env The JNI env
 * @param fluidObj The object containing the buffer arrays
 * @param arrFieldId The specific field to free
 */
void freeCenterField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId){
    //grab the buffer
    jobject jd = (*env)->GetObjectField(env,fluidObj,arrFieldId);
    jobject buff = (*env)->GetObjectArrayElement(env,jd,CENTER_POS);

    //actually free
    void *buffer = (*env)->GetDirectBufferAddress(env, buff);
    if (buffer != NULL) {
        // Free the allocated memory
        pool_return(centerArrPool, buffer);
    }

    //null the array element
    (*env)->SetObjectArrayElement(env,jd,CENTER_POS,NULL);
}

/**
 * Allocates a fluid chunk's data
 */
JNIEXPORT void JNICALL Java_electrosphere_client_fluid_cache_FluidChunkData_allocate(
    JNIEnv * env,
    jobject fluidObj
    ){
    jclass fluidChunkDataClass = (*env)->GetObjectClass(env,fluidObj);
    jfieldID dId = (*env)->GetFieldID(env,fluidChunkDataClass,"bWeights","Ljava/nio/ByteBuffer;");
    jfieldID uId = (*env)->GetFieldID(env,fluidChunkDataClass,"bVelocityX","Ljava/nio/ByteBuffer;");
    jfieldID vId = (*env)->GetFieldID(env,fluidChunkDataClass,"bVelocityY","Ljava/nio/ByteBuffer;");
    jfieldID wId = (*env)->GetFieldID(env,fluidChunkDataClass,"bVelocityZ","Ljava/nio/ByteBuffer;");
    allocateField(env,fluidObj,dId);
    allocateField(env,fluidObj,uId);
    allocateField(env,fluidObj,vId);
    allocateField(env,fluidObj,wId);
}

/**
 * Allocates the buffer of an object
 * @param env The JNI env
 * @param fluidObj The object containing the buffer field
 * @param arrFieldId The specific field to allocate
 */
void allocateField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId){
    if(fieldPool == NULL){
        fieldPool = pool_create(BUFF_SIZE);
    }
    //actually allocate
    void *buffer = pool_get(fieldPool);
    if (buffer == NULL) {
        // Handle allocation failure
        return;
    }
    // Create a direct ByteBuffer
    jobject byteBuffer = (*env)->NewDirectByteBuffer(env, buffer, BUFF_SIZE);
    if (byteBuffer == NULL) {
        // Handle ByteBuffer creation failure
        pool_return(fieldPool,buffer);
        (*env)->SetObjectField(env,fluidObj,arrFieldId,NULL);
        return;
    }

    //assign to array
    (*env)->SetObjectField(env,fluidObj,arrFieldId,byteBuffer);
}

/**
 * Frees a fluid chunk's data
 */
JNIEXPORT void JNICALL Java_electrosphere_client_fluid_cache_FluidChunkData_free(
    JNIEnv * env,
    jobject fluidObj
    ){
    jclass fluidChunkDataClass = (*env)->GetObjectClass(env,fluidObj);
    jfieldID dId = (*env)->GetFieldID(env,fluidChunkDataClass,"bWeights","Ljava/nio/ByteBuffer;");
    jfieldID uId = (*env)->GetFieldID(env,fluidChunkDataClass,"bVelocityX","Ljava/nio/ByteBuffer;");
    jfieldID vId = (*env)->GetFieldID(env,fluidChunkDataClass,"bVelocityY","Ljava/nio/ByteBuffer;");
    jfieldID wId = (*env)->GetFieldID(env,fluidChunkDataClass,"bVelocityZ","Ljava/nio/ByteBuffer;");
    freeField(env,fluidObj,dId);
    freeField(env,fluidObj,uId);
    freeField(env,fluidObj,vId);
    freeField(env,fluidObj,wId);
}

/**
 * Frees the center buffer of a buffer array
 * @param env The JNI env
 * @param fluidObj The object containing the buffer arrays
 * @param arrFieldId The specific field to free
 */
void freeField(JNIEnv * env, jobject fluidObj, jfieldID arrFieldId){
    //grab the buffer
    jobject buff = (*env)->GetObjectField(env,fluidObj,arrFieldId);

    //actually free
    void *buffer = (*env)->GetDirectBufferAddress(env, buff);
    if (buffer != NULL) {
        // Free the allocated memory
        pool_return(fieldPool,buffer);
    }

    //null the array element
    (*env)->SetObjectField(env,fluidObj,arrFieldId,NULL);
}