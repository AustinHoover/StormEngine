//Vertex Shader
#version 410 core



//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 4) in vec2 aTex;
layout (location = 5) in vec4 aGroundTexIndices;


//coordinate space transformation matrices
uniform mat4 transform;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceMatrix;



//output buffers
out vec3 Normal;
out vec3 FragPos;
out vec2 TexCoord;
out vec4 FragPosLightSpace;
flat out ivec4 groundTexIndices;




void main() {
    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * FinalNormal.xyz;
    TexCoord = aTex;

    //push texure indices to fragment shader
    groundTexIndices = ivec4(int(aGroundTexIndices.x),int(aGroundTexIndices.y),int(aGroundTexIndices.z),int(aGroundTexIndices.w));
    // groundTexIndices = vec4(int(aGroundTexIndices.x),int(aGroundTexIndices.y),int(aGroundTexIndices.z),int(aGroundTexIndices.w));
    // groundTexIndices = ivec4(0,1,2,3);
    FragPosLightSpace = lightSpaceMatrix * vec4(FragPos, 1.0);
    
    //set final position with opengl space
    gl_Position = projection * view * model * FinalVertex;
}
