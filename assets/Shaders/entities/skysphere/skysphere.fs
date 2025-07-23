

#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/standarduniform.fs"
#include "../../lib/math.fs"


/**
transparency
*/
#define SMALL_EPSILON 0.001

#define EASING_POWER 5


in vec3 FragPos;
in vec3 Normal;

uniform vec3 viewPos;

//light depth map
uniform sampler2D shadowMap;

out vec4 FragColor;


void main(){
    //grab light intensity
    vec3 norm = normalize(Normal);

    //colors for different times of day
    vec3 color1 = vec3(0,0,0);
    vec3 color2= vec3(0.68,0.93,0.93);

    //calculate color
    float timeOfDay = standardUniforms.timeOfDay;
    vec3 skyColor = mix(
        color1,
        color2,
        abs((0.5 - timeOfDay)) * 2
    );

    //calculate final color
    vec3 finalColor = skyColor;//norm.rgb;
    FragColor = vec4(finalColor, 1);
}
