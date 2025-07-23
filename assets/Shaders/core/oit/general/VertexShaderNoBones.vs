//Vertex Shader
#version 400 core



//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 4) in vec2 aTex;


//coordinate space transformation matrices
uniform mat4 transform;
uniform mat4 model;
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
    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);


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
