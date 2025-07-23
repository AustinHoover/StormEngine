package electrosphere.renderer.shader;

import static org.junit.jupiter.api.Assertions.*;

import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.RenderingTestTemplate;

/**
 * Tests for compute shaders
 */
public class ComputeShaderTests extends RenderingTestTemplate {
    
    /**
     * A simple compute shader
     */
    String simpleComputeShader = """
#version 430 core

layout (local_size_x = 10, local_size_y = 10, local_size_z = 1) in;

// ----------------------------------------------------------------------------
//
// uniforms
//
// ----------------------------------------------------------------------------

layout(rgba32f, binding = 0) uniform image2D imgOutput;

layout (location = 0) uniform float t;                 /** Time */

// ----------------------------------------------------------------------------
//
// functions
//
// ----------------------------------------------------------------------------

void main() {
	vec4 value = vec4(0.0, 0.0, 0.0, 1.0);
	ivec2 texelCoord = ivec2(gl_GlobalInvocationID.xy);
	float speed = 100;
	// the width of the texture
	float width = 1000;

	value.x = mod(float(texelCoord.x) + t * speed, width) / (gl_NumWorkGroups.x * gl_WorkGroupSize.x);
	value.y = float(texelCoord.y)/(gl_NumWorkGroups.y*gl_WorkGroupSize.y);
	imageStore(imgOutput, texelCoord, value);
}
    """;

    @IntegrationTest
    public void testCreation(){
        assertDoesNotThrow(() -> {
            ComputeShader.create(simpleComputeShader);
        });
    }

}
