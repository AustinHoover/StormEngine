//version
#version 330 core

//macros
#extension GL_ARB_explicit_uniform_location : enable

//output
out vec4 fragColor;

//input
in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;
in vec4 projCoord;
in vec4 modelCoord;

//uniforms
uniform float time;
uniform mat4 model;
uniform vec3 viewPos;

const float CONTACT_TEST_MARGIN = -0.001;

/*
Main method
*/
void main(){
    
    float timeS = time * 0.01;
    
    // Normalized pixel coordinates (from 0 to 1)
    vec3 projCoordNorm = projCoord.xyz / projCoord.w / 2.0 + 0.5;
    //make vec2
    vec2 finalProd = projCoordNorm.xy;

    //get vector that describes the movement through the cube
    vec3 viewDir = normalize(viewPos - FragPos);
    //need to transform to model space
    //get inverse of model transform
    mat4 inverseModel = inverse(model);
    //apply to view dir
    vec4 modelViewDir = normalize(inverseModel * vec4(viewDir,1.0));

    //where the vector first hits the cube
    vec4 contactPoint = modelCoord;

    
    float red = 1.0;
    float green = 1.0;
    float blue = 1.0;

    red = modelViewDir.x;
    green = modelViewDir.y;
    blue = modelViewDir.z;


    //need to calculate exit point
    //if we hit one of the X edges
    vec4 backPoint = vec4(1.0);
    if(abs(contactPoint.x)-1.0>CONTACT_TEST_MARGIN){
        red = 1.0;
        //discard if backface
        if(sign(contactPoint.x)!=sign(modelViewDir.x)){
            discard;
        }
        //calculate distance to nearest face
        float distX = 2.0;
        //this is some really weird logic that I believe works to calculate distance to nearest face
        float distY = abs(sign(modelViewDir.y) - contactPoint.y);
        float distZ = abs(sign(modelViewDir.z) - contactPoint.z);
        //calculate number of times to add viewDir to get dist
        float scaleX = distX / modelViewDir.x;
        float scaleY = distY / modelViewDir.y;
        float scaleZ = distZ / modelViewDir.z;
        //this is the smallest distance of all of them
        float minScale = min(scaleX,min(scaleY,scaleZ));
        backPoint = contactPoint + (modelViewDir * minScale);
        float dist = length(abs(vec3(contactPoint) - vec3(backPoint)))/1.0;
        red = green = blue = dist;
    } else if(abs(contactPoint.y)-1.0>CONTACT_TEST_MARGIN){
        green = 1.0;
        //discard if backface
        if(sign(contactPoint.y)!=sign(modelViewDir.y)){
            discard;
        }
    } else if(abs(contactPoint.z)-1.0>CONTACT_TEST_MARGIN){
        blue = 1.0;
        //discard if backface
        if(sign(contactPoint.z)!=sign(modelViewDir.z)){
            discard;
        }
    }


    vec4 color = vec4(
        red,
        green,
        blue,
        0.3
        );
        
    fragColor = color;
}
