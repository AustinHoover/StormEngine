#version 330 core
out vec4 FragColor;
  
in vec2 TexCoords;

uniform sampler2D screenTexture;

uniform float channel;

void main(){
    switch(int(channel)){
        case 0:
        FragColor = vec4(texture(screenTexture, TexCoords).r,0,0,1);
        break;
        case 1:
        FragColor = vec4(0,texture(screenTexture, TexCoords).g,0,1);
        break;
        case 2:
        FragColor = vec4(0,0,texture(screenTexture, TexCoords).b,1);
        break;
        case 3:
        FragColor = vec4(texture(screenTexture, TexCoords).a,texture(screenTexture, TexCoords).a,texture(screenTexture, TexCoords).a,1);
        break;
        case 4:
        FragColor = vec4(vec3(texture(screenTexture, TexCoords)),1.0);
        default:
        FragColor = vec4(1,1,1,1);
        break;
    }
}