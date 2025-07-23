//Vertex Shader
#version 450 core
#extension GL_ARB_shading_language_include : require
#include "../../lib/material.fs"
#include "../../lib/standarduniform.fs"



//defines
#define PI 3.1415

#define grassWidth 0.01 //TODO: convert to uniform

/**
 * Number of variables per instance
 */
#define NUM_PER_INSTANCE_VARS 6


//input buffers
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 4) in vec2 aTex;


uniform Material material;

uniform sampler2D dataMap;

//coordinate space transformation matrices
uniform mat4 model;
uniform vec3 modelWorldPos;


/**
Size of a row of data
*/
uniform int rowSize;


//output buffers
out vec3 Normal;
out vec3 FragPos;
out vec3 ViewFragPos;
out vec2 TexCoord;
out vec4 FragPosLightSpace;
out vec3 normalRot1;
out vec3 normalRot2;


//defines
mat4 rotation3dX(float angle);
mat4 rotation3dY(float angle);
mat4 rotation3dZ(float angle);
vec3 rotateY(vec3 vector, float angle);
mat4 scale3d(float x, float y, float z);
float easeIn(float interpolator);
float easeOut(float interpolator);
float easeInOut(float interpolator);
float map(float value, float min1, float max1, float min2, float max2);
mat4 rotationMatrix(vec3 axis, float angle);

//lib defines
vec4 openSimplex2_Conventional(vec3 X);
vec4 openSimplex2_ImproveXY(vec3 X);


void main() {

    //0 = left, 1 = right
    float xDirection = mod(float(gl_VertexID), 2.0);

    ivec2 texSize = textureSize(material.diffuse,0);

    int sampleX = (gl_InstanceID % rowSize) * NUM_PER_INSTANCE_VARS;
    int sampleY = (gl_InstanceID / rowSize);

    //grab data out of texture
    float xOffset =     texelFetch(material.diffuse,ivec2(0 + sampleX,sampleY),0).r;
    float yOffset =     texelFetch(material.diffuse,ivec2(1 + sampleX,sampleY),0).r;
    float zOffset =     texelFetch(material.diffuse,ivec2(2 + sampleX,sampleY),0).r;
    float rotVar =      texelFetch(material.diffuse,ivec2(3 + sampleX,sampleY),0).r;
    float rotVar2 =     texelFetch(material.diffuse,ivec2(4 + sampleX,sampleY),0).r;
    float heightScale = texelFetch(material.diffuse,ivec2(5 + sampleX,sampleY),0).r;

    //
    //curve float noise
    vec4 worldPos = vec4(
        xOffset + modelWorldPos.x,
        yOffset + modelWorldPos.y,
        zOffset + modelWorldPos.z,
        1.0
    );
    float curveFloatNoiseSample = clamp(map(openSimplex2_ImproveXY(vec3(worldPos.x,worldPos.z,standardUniforms.time)).x,-1.0,1.0,0,1),0,1);

    //
    //calculate rotations
    float curveAmount = rotVar2 * aPos.y * 5 + curveFloatNoiseSample.x * 0.01;
    mat4 localRot = rotation3dY(rotVar);
    mat4 localRot2 = rotation3dZ(curveAmount);

    float windDirectionSpeedMagnitude = 0.05;
    vec3 windDirectionMovementOverTime = vec3(
        windDirectionSpeedMagnitude * standardUniforms.time,
        windDirectionSpeedMagnitude * standardUniforms.time,
        0
    );


    float windStrengthMagnitude = 0.2;
    vec3 windStrengthOverTime = vec3(
        windStrengthMagnitude * standardUniforms.time,
        windStrengthMagnitude * standardUniforms.time,
        0
    );

    //
    //rotate with wind
    float windDir = clamp(map(openSimplex2_ImproveXY(vec3(worldPos.x,worldPos.z,0) * 0.05 + windDirectionMovementOverTime).x,-1.0,1.0,0,1),0,1);
    windDir = map(windDir,0.0,1.0,0.0,PI * 2.0);
    //strength
    float windStrength = clamp(map(openSimplex2_ImproveXY(vec3(worldPos.x,worldPos.z,0) * 0.2 + windStrengthOverTime).x,-3.0,3.0,0,1),0,1);
    //try to shape with easeIn
    float windLeanAngle = map(windStrength, 0.0, 1.0, 0.1, 0.9);
    windLeanAngle = easeIn(windLeanAngle) * 1.25;
    mat4 windRot = rotationMatrix(vec3(cos(windDir),0,sin(windDir)),windLeanAngle);


    //
    //position transform
    //
    mat4 localTransform = mat4(
        1.0, 0.0, 0.0, 0.0, //column 1
        0.0, 1.0, 0.0, 0.0, //column 2
        0.0, 0.0, 1.0, 0.0, //column 3
        xOffset, yOffset, zOffset, 1.0 //column 4
    );

    //
    // Scales the blade of grass vertically
    //
    mat4 localScale = scale3d(1.0,heightScale,1.0);

    //normalize posiiton and normal
    vec4 FinalVertex = model * localTransform * localScale * windRot * localRot * localRot2 * vec4(aPos, 1.0);
    vec4 FinalNormal = windRot * localRot * localRot2 * vec4(aNormal, 1.0);
    // vec4 FinalNormal = transpose(inverse(localRot2 * localRot * model * localTransform)) * vec4(aNormal, 1.0);

    //normal offset
    normalRot1 = rotateY(FinalNormal.rgb,PI * 0.3);
    normalRot2 = rotateY(FinalNormal.rgb,PI * -0.3);


    //
    //shift in viewspace to make it feel slightly fuller
    //
    //dot view and normal
    vec3 viewDir = normalize(vec3(standardUniforms.viewPos) - FinalVertex.xyz);
    float viewDotNormal = clamp(dot(FinalNormal.xz,viewDir.xz),0,1);
    //calculate thinkening factor to shift verts slightly based on view angle
    float viewSpaceThickenFactor = easeOut(1.0 - viewDotNormal);
    //as blade starts to become parallel with camera, want to allow it to shrink into nothing
    viewSpaceThickenFactor *= smoothstep(0.0, 0.2, viewDotNormal);
    //finally, apply adjustment to actual vert output
    FinalVertex.x += viewSpaceThickenFactor * (xDirection - 0.5) * grassWidth;


    //
    //push frag, normal, and texture positions to fragment shader
    //
    FragPos = vec3(FinalVertex);
    ViewFragPos = vec3(standardUniforms.view * model * FinalVertex);
    Normal = vec3(FinalNormal);
    TexCoord = aTex;


    //shadow map stuff
    FragPosLightSpace = standardUniforms.lightSpaceMatrix * vec4(FragPos, 1.0);

    
    //set final position with opengl space
    gl_Position = standardUniforms.projection * standardUniforms.view * FinalVertex;
}

mat4 rotation3dX(float angle) {
    float s = sin(angle);
    float c = cos(angle);

    mat4 rVal = mat4(
        1.0, 0.0, 0.0, 0.0,
        0.0,   c,   s, 0.0,
        0.0,  -s,   c, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    
    return rVal;
}

vec3 rotateY(vec3 vector, float angle){
    mat4 mat = rotation3dY(angle);
    return (mat * vec4(vector,1.0)).xyz;
}

mat4 rotation3dY(float angle) {
    float s = sin(angle);
    float c = cos(angle);

    mat4 rVal = mat4(
          c, 0.0,  -s, 0.0,
        0.0, 1.0, 0.0, 0.0,
          s, 0.0,   c, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    
    return rVal;
}

mat4 rotation3dZ(float angle) {
    float s = sin(angle);
    float c = cos(angle);

    mat4 rVal = mat4(
          c,   s, 0.0, 0.0,
         -s,   c, 0.0, 0.0,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    
    return rVal;
}

mat4 scale3d(float x, float y, float z){
    return mat4(
        x,      0.0,    0.0,    0.0,
        0.0,    y,      0.0,    3.0,
        0.0,    0.0,    z,      0.0,
        0.0,    0.0,    0.0,    1.0
    );
}


float easeIn(float interpolator){
    return interpolator * interpolator;
}

float easeOut(float interpolator){
    return 1 - easeIn(1 - interpolator);
}

float easeInOut(float interpolator){
    float easeInValue = easeIn(interpolator);
    float easeOutValue = easeOut(interpolator);
    return mix(easeInValue, easeOutValue, interpolator);
}

float map(float value, float min1, float max1, float min2, float max2) {
  return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}


mat4 rotationMatrix(vec3 axis, float angle){
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}


//////////////// K.jpg's Re-oriented 4-Point BCC Noise (OpenSimplex2) ////////////////
////////////////////// Output: vec4(dF/dx, dF/dy, dF/dz, value) //////////////////////

// Inspired by Stefan Gustavson's noise
vec4 permute(vec4 t) {
    return t * (t * 34.0 + 133.0);
}

// Gradient set is a normalized expanded rhombic dodecahedron
vec3 grad(float hash) {
    
    // Random vertex of a cube, +/- 1 each
    vec3 cube = mod(floor(hash / vec3(1.0, 2.0, 4.0)), 2.0) * 2.0 - 1.0;
    
    // Random edge of the three edges connected to that vertex
    // Also a cuboctahedral vertex
    // And corresponds to the face of its dual, the rhombic dodecahedron
    vec3 cuboct = cube;
    cuboct[int(hash / 16.0)] = 0.0;
    
    // In a funky way, pick one of the four points on the rhombic face
    float type = mod(floor(hash / 8.0), 2.0);
    vec3 rhomb = (1.0 - type) * cube + type * (cuboct + cross(cube, cuboct));
    
    // Expand it so that the new edges are the same length
    // as the existing ones
    vec3 grad = cuboct * 1.22474487139 + rhomb;
    
    // To make all gradients the same length, we only need to shorten the
    // second type of vector. We also put in the whole noise scale constant.
    // The compiler should reduce it into the existing floats. I think.
    grad *= (1.0 - 0.042942436724648037 * type) * 32.80201376986577;
    
    return grad;
}

// BCC lattice split up into 2 cube lattices
vec4 openSimplex2Base(vec3 X) {
    
    // First half-lattice, closest edge
    vec3 v1 = round(X);
    vec3 d1 = X - v1;
    vec3 score1 = abs(d1);
    vec3 dir1 = step(max(score1.yzx, score1.zxy), score1);
    vec3 v2 = v1 + dir1 * sign(d1);
    vec3 d2 = X - v2;
    
    // Second half-lattice, closest edge
    vec3 X2 = X + 144.5;
    vec3 v3 = round(X2);
    vec3 d3 = X2 - v3;
    vec3 score2 = abs(d3);
    vec3 dir2 = step(max(score2.yzx, score2.zxy), score2);
    vec3 v4 = v3 + dir2 * sign(d3);
    vec3 d4 = X2 - v4;
    
    // Gradient hashes for the four points, two from each half-lattice
    vec4 hashes = permute(mod(vec4(v1.x, v2.x, v3.x, v4.x), 289.0));
    hashes = permute(mod(hashes + vec4(v1.y, v2.y, v3.y, v4.y), 289.0));
    hashes = mod(permute(mod(hashes + vec4(v1.z, v2.z, v3.z, v4.z), 289.0)), 48.0);
    
    // Gradient extrapolations & kernel function
    vec4 a = max(0.5 - vec4(dot(d1, d1), dot(d2, d2), dot(d3, d3), dot(d4, d4)), 0.0);
    vec4 aa = a * a; vec4 aaaa = aa * aa;
    vec3 g1 = grad(hashes.x); vec3 g2 = grad(hashes.y);
    vec3 g3 = grad(hashes.z); vec3 g4 = grad(hashes.w);
    vec4 extrapolations = vec4(dot(d1, g1), dot(d2, g2), dot(d3, g3), dot(d4, g4));
    
    // Derivatives of the noise
    vec3 derivative = -8.0 * mat4x3(d1, d2, d3, d4) * (aa * a * extrapolations)
        + mat4x3(g1, g2, g3, g4) * aaaa;
    
    // Return it all as a vec4
    return vec4(derivative, dot(aaaa, extrapolations));
}

// Use this if you don't want Z to look different from X and Y
vec4 openSimplex2_Conventional(vec3 X) {
    
    // Rotate around the main diagonal. Not a skew transform.
    vec4 result = openSimplex2Base(dot(X, vec3(2.0/3.0)) - X);
    return vec4(dot(result.xyz, vec3(2.0/3.0)) - result.xyz, result.w);
}

// Use this if you want to show X and Y in a plane, then use Z for time, vertical, etc.
vec4 openSimplex2_ImproveXY(vec3 X) {
    
    // Rotate so Z points down the main diagonal. Not a skew transform.
    mat3 orthonormalMap = mat3(
        0.788675134594813, -0.211324865405187, -0.577350269189626,
        -0.211324865405187, 0.788675134594813, -0.577350269189626,
        0.577350269189626, 0.577350269189626, 0.577350269189626);
    
    vec4 result = openSimplex2Base(orthonormalMap * X);
    return vec4(result.xyz * orthonormalMap, result.w);
}

//////////////////////////////// End noise code ////////////////////////////////