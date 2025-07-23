#version 330 core
out vec4 FragColor;
  
in vec2 TexCoords;


void main(){
    vec4(vec3(gl_FragCoord.z), 1.0);
}