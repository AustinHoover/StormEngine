
/**
  * Bind point for the standard uniform SSBO
  */
#define STANDARD_UNIFORM_SSBO_BIND_POINT 4

/**
  * The standard uniforms
  */
struct StandardUniforms {
    /**
      * The view matrix
      */
    mat4 view;
    /**
      * The projection matrix
      */
    mat4 projection;
    /**
      * The light-space matrix
      */
    mat4 lightSpaceMatrix;
    /**
      * The view position
      */
    vec4 viewPos;
    /**
      * The current frame count
      */
    uint frame;
    /**
      * The current engine time
      */
    float time;
    /**
      * The time of day of the engine (range 0->1)
      */
    float timeOfDay;
};

/**
  * Cutoff for fragment alpha
  */
#define FRAGMENT_ALPHA_CUTOFF 0.001

layout(std430, binding = STANDARD_UNIFORM_SSBO_BIND_POINT) restrict buffer standardUniformSSBO {
    StandardUniforms standardUniforms;
};
