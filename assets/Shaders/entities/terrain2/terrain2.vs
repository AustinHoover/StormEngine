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
layout (location = 5) in vec3 samplerIndices; //the indices in the atlas of textures to sample
layout (location = 6) in vec3 samplerRatioVectors; //the interpolated ratio of HOW MUCH to pull from each texture in the atlas


//coordinate space transformation matrices
uniform mat4 model;



//output buffers
out vec3 Normal;
out vec3 FragPos;
out vec3 ViewFragPos;
out vec2 texPlane1;
out vec2 texPlane2;
out vec2 texPlane3;
out vec4 FragPosLightSpace;
out vec3 samplerIndexVec; //the indices in the atlas of textures to sample
out vec3 samplerRatioVec; //the vector of HOW MUCH to pull from each texture in the atlas


float map(float value, float min1, float max1, float min2, float max2);

void main() {
    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    ViewFragPos = vec3(standardUniforms.view * model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * aNormal;
    
    // //clamp the aPos vector to just shy of its surrounding values
    // //this prevents sampling across into the next texture
    // vec3 clampedPos = vec3(map(aPos.x,0,1,0.02,0.98), map(aPos.y,0,1,0.02,0.98), map(aPos.z,0,1,0.02,0.98));
    //reference https://catlikecoding.com/unity/tutorials/advanced-rendering/triplanar-mapping/
    texPlane1 = aPos.zy * TEXTURE_MAP_SCALE;
    texPlane2 = aPos.xz * TEXTURE_MAP_SCALE;
    texPlane3 = aPos.xy * TEXTURE_MAP_SCALE;

    //flip first coordinate if the normal is negative
    //this minimizes texture flipping
    texPlane1.x = texPlane1.x * sign(Normal.x);
    texPlane2.x = texPlane2.x * sign(Normal.y);
    texPlane3.x = texPlane3.x * sign(Normal.z);

    //pass through what atlas'd textures to sample
    samplerIndexVec = samplerIndices;
    samplerRatioVec = samplerRatioVectors;


    //shadow map stuff
    FragPosLightSpace = standardUniforms.lightSpaceMatrix * vec4(FragPos, 1.0);

    
    //set final position with opengl space
    gl_Position = standardUniforms.projection * standardUniforms.view * model * FinalVertex;
}



float map(float value, float min1, float max1, float min2, float max2) {
  return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}