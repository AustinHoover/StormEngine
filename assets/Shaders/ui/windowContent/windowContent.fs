#version 330 core
out vec4 FragColor;
  
in vec2 TexCoords;

uniform sampler2D screenTexture;

void main(){
    vec4 textureColor = texture(screenTexture, TexCoords);
    if(textureColor.a < 0.1){
        discard;
    }
    FragColor = textureColor;
}