//Vertex Shader
#version 420 core



//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 4) in vec2 aTex;


//coordinate space transformation matrices
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform float time;



//output buffers
out vec3 FragPos;
out vec3 Normal;
out vec2 TexCoord;
out vec4 projCoord;
out vec4 modelCoord;



void main() {
    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * aNormal;
    TexCoord = aTex;
    projCoord = projection * view * model * FinalVertex;
    modelCoord = FinalVertex;
    
    //set final position with opengl space
    vec4 outVec = projection * view * model * FinalVertex;
    // outVec.z = outVec.z - 0.1f;
    // outVec.z = -0.9;
    gl_Position = outVec;
}
