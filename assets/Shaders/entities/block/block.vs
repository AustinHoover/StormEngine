//Vertex Shader
#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/standarduniform.fs"

//defines
#define TEXTURE_MAP_SCALE 1.0
#define MODEL_TOTAL_DIM 16.0


//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 4) in vec2 aTex;
layout (location = 5) in int samplerIndices;


//coordinate space transformation matrices
uniform mat4 model;



//output buffers
out vec3 Normal;
out vec3 FragPos;
out vec3 ViewFragPos;
out vec2 uv;
out vec4 FragPosLightSpace;
flat out int samplerIndexVec;




void main() {
    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    ViewFragPos = vec3(standardUniforms.view * model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * aNormal;
    uv = aTex;

    //pass through what atlas'd textures to sample
    samplerIndexVec = samplerIndices;


    //shadow map stuff
    FragPosLightSpace = standardUniforms.lightSpaceMatrix * vec4(FragPos, 1.0);

    
    //set final position with opengl space
    gl_Position = standardUniforms.projection * standardUniforms.view * model * FinalVertex;
}
