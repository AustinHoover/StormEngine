//Vertex Shader
#version 450 core
#extension GL_ARB_shading_language_include : require
#include "./lib/standarduniform.fs"



//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec4 aWeights;
layout (location = 3) in vec4 aIndex;
layout (location = 4) in vec2 aTex;


//coordinate space transformation matrices
uniform mat4 model;

//bone related variables
const int MAX_WEIGHTS = 4;
const int MAX_BONES = 100;
uniform mat4 bones[MAX_BONES];



//output buffers
out vec3 Normal;
out vec3 FragPos;
out vec3 ViewFragPos;
out vec2 TexCoord;
out vec4 FragPosLightSpace;




void main() {

    

    //calculate bone transform
    mat4 BoneTransform = (bones[int(aIndex[0])] * aWeights[0]);
        BoneTransform = BoneTransform + (bones[int(aIndex[1])] * aWeights[1]);
        BoneTransform = BoneTransform + (bones[int(aIndex[2])] * aWeights[2]);
        BoneTransform = BoneTransform + (bones[int(aIndex[3])] * aWeights[3]);

    
    //apply bone transform to position vectors
    vec4 FinalVertex = BoneTransform * vec4(aPos, 1.0);
    vec4 FinalNormal = BoneTransform * vec4(aNormal, 1.0);

    
    //make sure the W component is 1.0
    FinalVertex = vec4(FinalVertex.xyz, 1.0);
    FinalNormal = vec4(FinalNormal.xyz, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    ViewFragPos = vec3(standardUniforms.view * model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * FinalNormal.xyz;
    TexCoord = aTex;

    //shadow map stuff
    FragPosLightSpace = standardUniforms.lightSpaceMatrix * vec4(FragPos, 1.0);


    //set final position with opengl space
    gl_Position = standardUniforms.projection * standardUniforms.view * model * FinalVertex;
}