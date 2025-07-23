#ifndef FLUID_PRESSURECELL_VELOCITY_H
#define FLUID_PRESSURECELL_VELOCITY_H

#include "public.h"
#include "fluid/env/environment.h"
#include "fluid/queue/chunk.h"
#include "fluid/queue/chunkmask.h"



/**
 * Adds velocity from the delta buffer to this chunk
*/
LIBRARY_API void pressurecell_add_gravity(Environment * environment, Chunk * chunk);

/**
 * Adds velocity from the delta buffer to this chunk
*/
LIBRARY_API void pressurecell_add_velocity(Environment * environment, Chunk * chunk);

/**
 * Diffuses the velocity in this chunk
*/
LIBRARY_API void pressurecell_diffuse_velocity(Environment * environment, Chunk * chunk);

/**
 * Advects the velocity of this chunk
*/
LIBRARY_API void pressurecell_advect_velocity(Environment * environment, Chunk * chunk);

/**
 * Interpolates between the advected velocity and the previous frame's velocity by the pressure divergence amount
*/
LIBRARY_API double pressurecell_project_velocity(Environment * environment, Chunk * chunk);

/**
 * Copy temp velocities to next frame
*/
LIBRARY_API void pressurecell_copy_for_next_frame(Environment * environment, Chunk * chunk);


#endif