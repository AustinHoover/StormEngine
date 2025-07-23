/*
MIT License

Copyright (c) 2022 railgunSR

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
/*
THIS MAKES USE OF OPENSIMPLEX2, A NOISE ALGORITHM CREATED BY THE FINE FOLKS 
OVER AT https://github.com/KdotJPG/OpenSimplex2
PLEASE GIVE THEM SOME LOVE.

THE FLAME FUNCTION IS ONE CREATED BY ME BLENDING A LOG2 INTO A EXPONENTIAL.
*/

#version 330 core

out vec4 fragColor;

uniform float time;

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;

vec4 openSimplex2_ImproveXY(vec3 X);
float flameTex(float x, float y);

void main(){
    
    float timeS = time * 0.003;
    
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = vec2(TexCoord.x,1-TexCoord.y);
    
    // vec4 openVec = openSimplex2_ImproveXY(vec3(uv.x,uv.y,time));
    vec4 openVec = openSimplex2_ImproveXY(vec3(uv.x*3.0,uv.y*3.0 - timeS,0.0));

    float nS = 3.0; //noise scale

    //compose textures
    float flameXScale = 2.0;
    float flameYScale = 2.0;
    float flameVal = flameTex(uv.x*flameXScale-1.0/flameXScale,uv.y*flameYScale-1.0/flameYScale);

    float flameComp1 = flameVal * openSimplex2_ImproveXY(vec3(uv.x * nS              ,uv.y * nS - timeS * 1.0,0.0)).x;
    nS = 3.0;
    float flameComp2 = flameVal * openSimplex2_ImproveXY(vec3(uv.x * nS              ,uv.y * nS - timeS * 2.0,0.0)).x;
    nS = 5.0;
    float flameComp3 = flameVal * openSimplex2_ImproveXY(vec3(uv.x * nS + timeS * 1.0,uv.y * nS - timeS * 3.0,0.0)).x;
    float flameComp4 = flameVal * openSimplex2_ImproveXY(vec3(uv.x * nS - timeS * 1.0,uv.y * nS - timeS * 3.0,0.0)).x;
    nS = 3.0;
    float flameComp5 = flameVal * openSimplex2_ImproveXY(vec3(uv.x * nS              ,uv.y * nS - timeS * 1.5,0.0)).x;
    float val = 
    flameVal * 3.0 +
    flameComp1 * 0.2 +
    flameComp2 * 0.2 +
    flameComp3 * 0.2 +
    flameComp4 * 0.2 +
    flameComp5 * 0.2
    ;
    
    vec4 color = vec4(
        min(val*2.0,1.0),
        min(val*0.8,1.0),
        min(val*0.2,1.0),
        min(val,1.0)
        );
    
    if(val < 0.3){
        discard;
    }

    // Output to screen
    fragColor = color;
}

//
//custom flame function
///

float flameTex(float x, float y){
    //flip y
    float t = 1.0 - y;
    //calculate vertical component
    float verticalFlameValue = pow(log(t+1.0),1.4) - step(0.5,t) * (pow((2.0 * (t - 0.5)),3.0) / pow(log(2.0),1.4));
    //calculate dist along horizontal from vertical component
    float dist = abs(x-0.5);
    //want to fade to nothing at dist >= vertical flame value
    //use exponent to get there
    //clamp range with min
    float v = max(2.0 * (verticalFlameValue - dist),0.0);
    //apply exponent to get value
    float rVal = pow(v,1.4);
    return rVal;
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