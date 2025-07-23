#version 330 core

uniform float linearCoef;
uniform float quadCoef;

uniform float near;
uniform float far;

float LinearizeDepth(float depth);

void main(){
    // float coord = gl_FragCoord.x;
    // if(coord != 1){
    //     gl_FragDepth = 0;
    // } else {
    //     gl_FragDepth = 1;
    // }

    float depthRaw = gl_FragCoord.z;

    // if(depthRaw != 1){
    //     depthRaw = 0;
    // }
    
    float finalValue = LinearizeDepth(depthRaw) / sqrt(far);//min(depthRaw * linearCoef + depthRaw * depthRaw * quadCoef,1);

    gl_FragDepth = finalValue;
}


//
//Util
//
float LinearizeDepth(float depth){
    float z = depth * 2.0 - 1.0; // back to NDC 
    return (2.0 * near * far) / (far + near - z * (far - near));	
}