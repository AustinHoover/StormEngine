//Vertex Shader
#version 330 core



//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec4 aWeights;
layout (location = 3) in vec4 aIndex;


//coordinate space transformation matrices
uniform mat4 transform;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

//bone related variables
const int MAX_WEIGHTS = 4;
const int MAX_BONES = 100;
uniform mat4 bones[MAX_BONES];



//output buffers
out vec3 Normal;
out vec3 FragPos;




void main() {

    //apply bone transform to position vectors
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);

    //calculate bone transform
    mat4 BoneTransform = (bones[int(aIndex[0])] * aWeights[0]);
        BoneTransform = BoneTransform + (bones[int(aIndex[1])] * aWeights[1]);
        BoneTransform = BoneTransform + (bones[int(aIndex[2])] * aWeights[2]);
        BoneTransform = BoneTransform + (bones[int(aIndex[3])] * aWeights[3]);


    //apply bone transform to position vectors
    FinalVertex = BoneTransform * vec4(aPos, 1.0);
    FinalNormal = BoneTransform * vec4(aNormal, 1.0);
    
    //make sure the W component is 1.0
    FinalVertex = vec4(FinalVertex.xyz, 1.0);
    FinalNormal = vec4(FinalNormal.xyz, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * FinalNormal.xyz;


    //set final position with opengl space
    gl_Position = projection * view * model * FinalVertex;
}