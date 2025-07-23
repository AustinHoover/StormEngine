

#include "fluid/tracking/tracking.h"

/**
 * Resets the tracking state for this frame
 */
LIBRARY_API void fluid_tracking_reset(Environment * environment){
    environment->state.existingDensity = 0;
    environment->state.normalizationRatio = 0;
}

