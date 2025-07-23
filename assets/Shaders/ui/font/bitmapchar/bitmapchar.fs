
#version 330 core
out vec4 FragColor;
  
in vec2 TexCoords;

uniform sampler2D screenTexture;
uniform vec4 color;

void main(){
    vec4 textColorModifier = color;
    if(color.x == 0 && color.y == 0 && color.z == 0){
        textColorModifier.x = 1;
        textColorModifier.y = 1;
        textColorModifier.z = 1;
    }
    vec4 sample = texture(screenTexture, TexCoords);
    float baseColor = sample.r;
    textColorModifier.a = textColorModifier.a * baseColor;
    FragColor = textColorModifier;
}