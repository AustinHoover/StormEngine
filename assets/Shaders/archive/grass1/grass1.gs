#version 330 core


layout (triangles) in;
layout (triangle_strip, max_vertices = 200) out;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

//uniforms
uniform float time;

out vec3 origCoord;

void main() {

    vec4 triangle1Pos = gl_in[0].gl_Position;

    //
    //Each new vertex is a new triangle using the previous two
    //

    float timeS = time * 0.0051;

    float bladeHeight = 0.2;
    float rotateRadius = 0.01;

    for(float x = 0; x < 4; x++){
        for(float y = 0; y < 4; y++){
            float xPos = x / 20.0;
            float yPos = y / 20.0;

            float cOffX = sin(timeS + (x + y) * y) * rotateRadius;
            float cOffY = cos(timeS + (x + y) * y) * rotateRadius;

            origCoord = (triangle1Pos + vec4( 0.0 + xPos, 0.45, 0.0 + yPos, 0.0)).xyz;
            gl_Position = projection * view * model * (triangle1Pos + vec4( 0.0 + xPos, 0.45, 0.0 + yPos, 0.0)); 
            EmitVertex();
            origCoord = (triangle1Pos + vec4( 0.05 + xPos, 0.45, 0.0 + yPos, 0.0)).xyz;
            gl_Position = projection * view * model * (triangle1Pos + vec4( 0.05 + xPos, 0.45, 0.0 + yPos, 0.0));
            EmitVertex();
            origCoord = (triangle1Pos + vec4( 0.025 + xPos + cOffX, 0.95, 0.025 + yPos + cOffY, 0.0)).xyz;
            gl_Position = projection * view * model * (triangle1Pos + vec4( 0.025 + xPos + cOffX, 0.45 + bladeHeight, 0.025 + yPos + cOffY, 0.0));
            EmitVertex();
            origCoord = (triangle1Pos + vec4( 0.05 + xPos, 0.45, 0.05 + yPos, 0.0)).xyz;
            gl_Position = projection * view * model * (triangle1Pos + vec4( 0.05 + xPos, 0.45, 0.05 + yPos, 0.0));
            EmitVertex();
            origCoord = (triangle1Pos + vec4( 0.0 + xPos, 0.45, 0.05 + yPos, 0.0)).xyz;
            gl_Position = projection * view * model * (triangle1Pos + vec4( 0.0 + xPos, 0.45, 0.05 + yPos, 0.0));
            EmitVertex();
            origCoord = (triangle1Pos + vec4( 0.025 + xPos + cOffX, 0.95, 0.025 + yPos + cOffY, 0.0)).xyz;
            gl_Position = projection * view * model * (triangle1Pos + vec4( 0.025 + xPos + cOffX, 0.45 + bladeHeight, 0.025 + yPos + cOffY, 0.0));
            EmitVertex();
            origCoord = (triangle1Pos + vec4( 0.0 + xPos, 0.45, 0.0 + yPos, 0.0)).xyz;
            gl_Position = projection * view * model * (triangle1Pos + vec4( 0.0 + xPos, 0.45, 0.0 + yPos, 0.0)); 
            EmitVertex();
            EndPrimitive();
        }
        // EndPrimitive();
    }


    // gl_Position = projection * view * model * (gl_in[0].gl_Position + vec4( 1.0, 1.0, 0.0, 1.0)); 
    // EmitVertex();
    // gl_Position = projection * view * model * (gl_in[0].gl_Position + vec4( 0.0, 1.0, 0.0, 1.0));
    // EmitVertex();
    // gl_Position = projection * view * model * (gl_in[0].gl_Position + vec4( 0.0, 0.0, 0.0, 1.0));
    // EmitVertex();
    // EndPrimitive();

    // gl_Position = projection * view * model * (gl_in[1].gl_Position + vec4( 1.0, 0.0, 0.0, 1.0)); 
    // EmitVertex();
    // gl_Position = projection * view * model * (gl_in[1].gl_Position + vec4( 0.0, 1.0, 0.0, 1.0));
    // EmitVertex();
    // gl_Position = projection * view * model * (gl_in[1].gl_Position + vec4( 0.0, 0.0, 0.0, 1.0));
    // EmitVertex();
    // EndPrimitive();

    // gl_Position = projection * view * model * (gl_in[2].gl_Position + vec4( 1.0, 0.0, 0.0, 1.0)); 
    // EmitVertex();
    // gl_Position = projection * view * model * (gl_in[2].gl_Position + vec4( 0.0, 1.0, 0.0, 1.0));
    // EmitVertex();
    // gl_Position = projection * view * model * (gl_in[2].gl_Position + vec4( 0.0, 0.0, 0.0, 1.0));
    // EmitVertex();
    // EndPrimitive();
}  