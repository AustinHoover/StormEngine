//Vertex Shader
#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/standarduniform.fs"

/**
Bind points for different SSBOs
*/
#define PARTICLE_SSBO_BIND_POINT 4

/**
A point light
*/
struct ParticleData {
    dmat4 model;
    vec4 color;
    vec4 texture;
};


//input buffers
layout (location =  0) in vec3 aPos;
layout (location =  1) in vec3 aNormal;
layout (location =  4) in vec2 aTex;

layout(std430, binding = PARTICLE_SSBO_BIND_POINT) restrict buffer particleSSBO {
    ParticleData particleData[];
};



//output buffers
out vec3 Normal;
out vec3 FragPos;
out vec3 ViewFragPos;
out vec2 TexCoord;
out vec4 FragPosLightSpace;
out vec4 instanceColor;




void main() {
    ParticleData currentParticle = particleData[gl_InstanceID];

    mat4 model = mat4(currentParticle.model);

    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);

    //make sure the W component is 1.0
    FinalVertex = vec4(FinalVertex.xyz, 1.0);
    FinalNormal = vec4(FinalNormal.xyz, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    ViewFragPos = vec3(standardUniforms.view * model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * aNormal;

    //offset based on data stored in particle data
    TexCoord = (aTex * currentParticle.texture.xy) + currentParticle.texture.zw;
    instanceColor = currentParticle.color;


    //shadow map stuff  
    FragPosLightSpace = standardUniforms.lightSpaceMatrix * vec4(FragPos, 1.0);

    
    //set final position with opengl space
    gl_Position = standardUniforms.projection * standardUniforms.view * model * FinalVertex;
}
