#ifndef ENVIRONMENT_H
#define ENVIRONMENT_H

#include <jni.h>
#include "public.h"
#include "fluid/queue/chunk.h"
#include "math/ode/diffusion_ode.h"

/**
 * The List lookup table
 */
typedef struct {
    jmethodID jListSize;
    jmethodID jListGet;
    jmethodID jListAdd;
} ListLookupTable;

/**
 * The ServerFluidChunk lookup table
 */
typedef struct {
    jfieldID dJId;
    jfieldID d0JId;
    jfieldID uJId;
    jfieldID vJId;
    jfieldID wJId;
    jfieldID u0JId;
    jfieldID v0JId;
    jfieldID w0JId;
    jfieldID boundsId;
    jfieldID worldXId;
    jfieldID worldYId;
    jfieldID worldZId;
    jfieldID neighborsId;
    jfieldID divergenceCacheId;
    jfieldID pressureCacheId;
    jfieldID chunkmaskJId;
    jfieldID updatedId;
    jfieldID totalDensityId;
    jfieldID asleepId;
    jfieldID homogenousId;
    jfieldID normalizationRatioId;
    jfieldID massCountId;
    jfieldID pressureTotalId;
    jfieldID velocityMagTotalId;
    jfieldID pressureOutgoingId;
    jfieldID pressureIncomingId;
    jfieldID densityOutgoingId;
    jfieldID densityIncomingId;
} ServerFluidChunkLookupTable;

/**
 * Lookup table for various java fields, methods, etc
 */
typedef struct {
    ListLookupTable listTable;
    ServerFluidChunkLookupTable serverFluidChunkTable;
    jclass serverFluidChunkClass;
} JNILookupTable;

/**
 * Stores the different queues of cells to simulate
 */
typedef struct {
    Chunk ** cellularQueue;
    Chunk ** gridQueue;
    Chunk ** grid2Queue;
    Chunk ** pressurecellQueue;
} FluidSimQueue;

/**
 * Fluid sim consts provided by the host
 */
typedef struct {
    float gravity;
    float dt;
} FluidSimConsts;

/**
 * Time tracking for the fluid simulator
 */
typedef struct {
    //density data
    double densityTotal;
    double densityMaintenance;
    double densityDiffuse;
    double densityAdvect;

    //velocity data
    double velocityTotal;
    double velocityDiffuse;
    double velocityAdvect;
    double velocityProject;
} FluidTimeTracking;

/**
 * The state for the grid2 simulator
 */
typedef struct {
    /**
     * A grid that stores a mask of the border locations
     * Stores 1 where it is a border region
     * Stores 0 where it is an internal region
     */
    float * fluid_grid2_border_mask;

    /**
     * A grid that stores an inverted mask of the border locations
     * Stores 0 where it is a border region
     * Stores 1 where it is an internal region
     */
    float * fluid_grid2_border_mask_inverted;

    /**
     * An array that stores values for the neighbor's arrays
     */
    float * fluid_grid2_neighborArr_d;
    float * fluid_grid2_neighborArr_d0;
    float * fluid_grid2_neighborArr_u;
    float * fluid_grid2_neighborArr_v;
    float * fluid_grid2_neighborArr_w;
    float * fluid_grid2_neighborArr_u0;
    float * fluid_grid2_neighborArr_v0;
    float * fluid_grid2_neighborArr_w0;
    float * fluid_grid2_neighborArr_bounds;
    float * fluid_grid2_neighborArr_divergenceCache;
    float * fluid_grid2_neighborArr_scalarCache;
    
    /**
     * Data for computing diffusion ODEs
     */
    OdeDiffuseData diffuseData;
} FluidGrid2State;

/**
 * Used for tracking the change in density over time of the environment
 */
typedef struct {
    double existingDensity;
    double newDensity;
    double normalizationRatio;
    int frame;
    FluidTimeTracking timeTracking;
    FluidGrid2State grid2;
} FluidSimState;

/**
 * Stores data about the simulation environment
*/
typedef struct {
    JNILookupTable lookupTable;
    FluidSimQueue queue;
    FluidSimConsts consts;
    FluidSimState state;
} Environment;

/**
 * Creates an environment
 */
LIBRARY_API Environment * fluid_environment_create();

/**
 * Frees an environment
 * @param environment The environment to free
 */
LIBRARY_API void fluid_environment_free(Environment * environment);

#endif