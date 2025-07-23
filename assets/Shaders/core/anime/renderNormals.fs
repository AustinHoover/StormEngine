#version 330 core

out vec3 FragColor;


in vec3 FragPos;
in vec3 Normal;


void main(){
    vec3 norm = normalize(Normal) / 2.0;
    norm = vec3(norm.x + 0.5,norm.y + 0.5,norm.z + 0.5);

    // float dist = gl_FragDepth;

    FragColor = norm;
}
