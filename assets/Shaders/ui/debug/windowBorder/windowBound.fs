#version 330 core

//threshold on borders of window where to actually draw outline
#define threshold 0.01

out vec4 FragColor;

uniform vec3 color;

in vec2 texCoord;
  
void main(){
    if(abs(texCoord.x) > 1.0-threshold || abs(texCoord.y) > 1.0-threshold || abs(texCoord.x) < threshold || abs(texCoord.y) < threshold){
        FragColor = vec4(color,1.0);
    } else {
        FragColor = vec4(0.0);
    }
}