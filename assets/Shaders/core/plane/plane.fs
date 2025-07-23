#version 430 core
out vec4 FragColor;
  
in vec2 TexCoords;

uniform sampler2D screenTexture;
uniform vec4 color;

void main(){
    vec4 textureColor = texture(screenTexture, TexCoords);
    textureColor.r = textureColor.r * color.r;
    textureColor.g = textureColor.g * color.g;
    textureColor.b = textureColor.b * color.b;
    textureColor.a = textureColor.a * color.a;
    if(textureColor.a < 0.1){
        discard;
    }
    FragColor = textureColor;
}