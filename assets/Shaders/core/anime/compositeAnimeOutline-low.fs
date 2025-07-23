#version 330 core

// shader outputs
layout (location = 0) out vec4 frag;

// color accumulation buffer
uniform sampler2D texture;

void main(){
	// fragment coordination
	ivec2 coords = ivec2(gl_FragCoord.xy);
 
	// fragment color
	vec4 color = texelFetch(texture, coords, 0);

    float val = color.r;

    // if(color.r < 0.5){z
    //     discard;
    // }
	
	vec4 outColor = vec4(0);

	if(val == 1){
		outColor = vec4(0,0,0,0.5);
		// outColor.a = 1;
	}

	frag = outColor;
}