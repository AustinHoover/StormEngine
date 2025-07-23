//Vertex Shader
#version 330 core



//input buffers
layout (location =  0) in vec3 aPos;
layout (location =  1) in vec3 aNormal;
layout (location =  2) in vec4 aWeights;
layout (location =  4) in vec2 aTex;
layout (location =  5) in vec4 modelA;
layout (location =  6) in vec4 modelB;
layout (location =  7) in vec4 modelC;
layout (location =  8) in vec4 modelD;
layout (location =  9) in vec4 boneA;
layout (location = 10) in vec4 boneB;
layout (location = 11) in vec4 boneC;
layout (location = 12) in vec4 boneD;
layout (location = 13) in float baseSize;


//coordinate space transformation matrices
uniform mat4 transform;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceMatrix;



//output buffers
out vec3 Normal;
out vec3 FragPos;
out vec3 ViewFragPos;
out vec2 TexCoord;
out vec4 FragPosLightSpace;




void main() {

    mat4 model = mat4(modelA,modelB,modelC,modelD);

    mat4 scaleMatrix = mat4(
        baseSize,0,0,0,
        0,baseSize,0,0,
        0,0,baseSize,0,
        0,0,0,1
    );

    mat4 bone = mat4(boneA,boneB,boneC,boneD);
    mat4 BoneTransform = (bone * aWeights[0]) + (scaleMatrix * (1.0 - aWeights[0]));

    //normalize posiiton and normal
    vec4 FinalVertex = BoneTransform * vec4(aPos, 1.0);
    vec4 FinalNormal = BoneTransform * vec4(aNormal, 1.0);

    //make sure the W component is 1.0
    FinalVertex = vec4(FinalVertex.xyz, 1.0);
    FinalNormal = vec4(FinalNormal.xyz, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    ViewFragPos = vec3(view * model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * aNormal;
    TexCoord = aTex;


    //shadow map stuff
    FragPosLightSpace = lightSpaceMatrix * vec4(FragPos, 1.0);

    
    //set final position with opengl space
    gl_Position = projection * view * model * FinalVertex;
}
