//Vertex Shader
#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/standarduniform.fs"


//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 4) in vec2 aTex;


//coordinate space transformation matrices
uniform mat4 model;



//output buffers
out vec3 Normal;
out vec3 FragPos;




void main() {
    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    vec4 FinalNormal = vec4(aNormal, 1.0);

    //time-of-day normal rotation
    float angle = 3.14 * standardUniforms.timeOfDay;
    mat4  rotationAboutZ = mat4(
    vec4( cos(angle), -sin(angle), 0.0,  0.0 ),
    vec4( sin(angle), cos(angle),  0.0,  0.0 ),
    vec4( 0.0,        0.0,         1.0,  0.0 ),
    vec4( 0.0,        0.0,         0.0,  1.0 ) ); 


    //push frag, normal, and texture positions to fragment shader
    FragPos = vec3(model * FinalVertex);
    Normal = mat3(transpose(inverse(model))) * mat3(rotationAboutZ) * aNormal;

    
    //set final position with opengl space
    gl_Position = standardUniforms.projection * standardUniforms.view * model * FinalVertex;
}
