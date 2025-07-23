#ifndef FLUID_SIMULATOR_CELLULAR_H
#define FLUID_SIMULATOR_CELLULAR_H

#include "public.h"
#include "fluid/queue/chunk.h"
#include "fluid/env/environment.h"


/**
 * Lateral diffusion rate of the fluids
 */
#define FLUID_CELLULAR_DIFFUSE_RATE2 0.1f

/**
 * Gravity transfer rate/threshold
 */
#define FLUID_CELLULAR_DIFFUSE_RATE_GRAV 0.5f

/**
 * Minimum density for lateral movement to occur
 */
#define FLUID_CELLULAR_SURFACE_TENSION_CONST 0.4

/**
 * This is the expected amount of variance frame-to-frame for a chunk that is completely full of fluid
 */
#define FLUID_CELLULAR_TOLLERABLE_LOSS_THRESHOLD1 0.1f

/**
 * Simulates the cellular chunk queue
 * @param environment The environment storing the simulation queues
 */
LIBRARY_API void fluid_cellular_simulate(Environment * environment);

/**
 * Gets the x velocity of a given position
 * @param environment The environment storing the simulation queues
 * @param x The x coordinate
 * @param y The y coordinate
 * @param z The z coordinate
 * @preturn The flow direction of x
 */
LIBRARY_API int fluid_cellular_get_flow_x(Environment * environment, int x, int y, int z);

/**
 * Gets the z velocity of a given position
 * @param environment The environment storing the simulation queues
 * @param x The x coordinate
 * @param y The y coordinate
 * @param z The z coordinate
 * @preturn The flow direction of z
 */
LIBRARY_API int fluid_cellular_get_flow_z(Environment * environment, int x, int y, int z);




#endif