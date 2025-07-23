#version 330 core

out vec4 fragColor;

uniform float time;

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec3 origCoord;

void main(){
    

    
    vec4 color = vec4(
        0.25 * origCoord.y,
        0.50 * origCoord.y,
        0.25 * origCoord.y,
        1.0
        );
    

    // Output to screen
    fragColor = color;
}
