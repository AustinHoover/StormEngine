#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 2) in vec4 aWeights;
layout (location = 3) in vec4 aIndex;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

//bone related variables
const int MAX_WEIGHTS = 4;
const int MAX_BONES = 100;
uniform mat4 bones[MAX_BONES];

void main()
{
    //calculate bone transform
    mat4 BoneTransform = (bones[int(aIndex[0])] * aWeights[0]);
        BoneTransform = BoneTransform + (bones[int(aIndex[1])] * aWeights[1]);
        BoneTransform = BoneTransform + (bones[int(aIndex[2])] * aWeights[2]);
        BoneTransform = BoneTransform + (bones[int(aIndex[3])] * aWeights[3]);

    
    //apply bone transform to position vectors
    vec4 FinalVertex = BoneTransform * vec4(aPos, 1.0);

    
    //make sure the W component is 1.0
    FinalVertex = vec4(FinalVertex.xyz, 1.0);

    //have to account for if dont have bones
    FinalVertex = vec4(aPos, 1.0);

    gl_Position = projection * view * model * FinalVertex;
}  