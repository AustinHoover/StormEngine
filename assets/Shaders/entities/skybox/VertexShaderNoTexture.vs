//Vertex Shader
#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/standarduniform.fs"



//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in float id;


//coordinate space transformation matrices
uniform mat4 model;



uniform vec3 colors[8];

out vec3 color;



void main()
{
    //normalize posiiton and normal
    vec4 FinalVertex = vec4(aPos, 1.0);
    //send color to the frag shader
    color = colors[int(id)];
    //set final position with opengl space
    vec4 pos = standardUniforms.projection * standardUniforms.view * model * FinalVertex;
    gl_Position = pos.xyww;
}
