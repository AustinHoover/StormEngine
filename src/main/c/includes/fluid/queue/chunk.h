#ifndef CHUNK_H
#define CHUNK_H

#include <jni.h>
#include <stdint.h>

#include "public.h"

/**
 * The minimum fluid value
 */
#define MIN_FLUID_VALUE 0.0f

/**
 * The maximum fluid value
 */
#define MAX_FLUID_VALUE 1.0f

/**
 * The cutoff value for the bounds array
 */
#define BOUND_CUTOFF_VALUE 0.0f

/**
 * Maximum value of bounds array for it to be considered a blocker
 */
#define BOUND_MAX_VALUE 1.0f

/**
 * The dimension of a single chunk's array
 */
#define DIM 18

/**
 * The spacing between chunks
 */
#define CHUNK_SPACING 16

/**
 * The maximum level of the interest tree
 */
#define CHUNK_MAX_INTEREST_LEVEL 4

/**
 * The dimensions of the interest tree at each level
 */
static char INTEREST_MODIFIER_DIMS[CHUNK_MAX_INTEREST_LEVEL+1] = {
    16,
    8,
    4,
    2,
    1,
};

/**
 * Gets the interest tree at a position and level
 */
#define INTEREST(tree,level,x,y,z) tree[level][x/INTEREST_MODIFIER_DIMS[level]*INTEREST_MODIFIER_DIMS[level]*INTEREST_MODIFIER_DIMS[level]+(y/INTEREST_MODIFIER_DIMS[level])*INTEREST_MODIFIER_DIMS[level]+(z/INTEREST_MODIFIER_DIMS[level])]

/**
 * The data for this chunk that is specific to the pressure cell method
 */
typedef struct {
    /**
     * The sum of density
     */
    double densitySum;
    /**
     * Ratio to normalize the density by
     */
    double normalizationRatio;

    /**
     * The amount of density that was recaptured
     */
    double recaptureDensity;

    /**
     * The total pressure of the chunk
     */
    double pressureTotal;

    /**
     * Total velocity magnitude of the chunk
     */
    double velocityMagTotal;

    /**
     * The density outgoing to neighbors
     */
    double outgoingDensity[9];

    /**
     * The pressure outgoing to neighbors
     */
    double outgoingPressure[9];
} PressureCellData;

/**
 * A chunk
*/
typedef struct {
    float * d[27];
    float * d0[27];
    float * u[27];
    float * v[27];
    float * w[27];
    float * u0[27];
    float * v0[27];
    float * w0[27];

    /**
     * Tracks which positions are bounds. Greater than 0 indicates a boundary, 0 or less indicates an open position.
     */
    float * bounds[27];

    /**
     * Caches the vector field divergence of this chunk for usage by neighbors next frame
     */
    float * divergenceCache[27];

    /**
     * Caches the scalar potential of this chunk for usage next frame
     */
    float * pressureCache[27];

    /**
     * Temp cache for storing density during current iteration
     */
    float * dTempCache;

    /**
     * Temp cache for storing u velocity during current iteration
     */
    float * uTempCache;

    /**
     * Temp cache for storing v velocity during current iteration
     */
    float * vTempCache;

    /**
     * Temp cache for storing w velocity during current iteration
     */
    float * wTempCache;

    /**
     * Temp cache for storing pressure during current iteration
     */
    float * pressureTempCache;

    /**
     * The bitmask which tracks valid neighbors
     */
    uint32_t chunkMask;

    /**
     * The raw java object corresponding to this chunk
     */
    jobject chunkJRaw;

    /**
     * The world x coordinate of this chunk
     */
    int x;

    /**
     * The world y coordinate of this chunk
     */
    int y;

    /**
     * The world z coordinate of this chunk
     */
    int z;

    /**
     * The level of detail to simulate the chunk with
     * NOTE: This is not a spatial LOD. It is a simulation LOD
     */
    int simLOD;

    /**
     * The convergence of this chunk
     */
    float projectionResidual;

    /**
     * The number of iterations this chunk took to project
     */
    int projectionIterations;

    /**
     * The amount of pressure outgoing to other chunks this frame
     */
    float outgoingPressure[27];

    /**
     * The amount of pressure incoming to other chunks this frame
     */
    float incomingPressure[27];

    /**
     * The amount of density outgoing to other chunks this frame
     */
    float outgoingDensity[27];

    /**
     * The amount of density incoming to other chunks this frame
     */
    float incomingDensity[27];

    /**
     * The data for pressure cell work in particular
     */
    PressureCellData pressureCellData;
    
} Chunk;

/**
 * Allocates a new chunk
 */
LIBRARY_API Chunk * chunk_create();

#endif